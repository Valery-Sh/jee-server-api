package org.embedded.ide.tomcat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import static org.embedded.ide.tomcat.Utils.isBuildOfMavenProject;

/**
 *
 * @author V. Shyshkin
 */
public class CommandManager extends HttpServlet implements LifecycleListener {

    public static final String SHUTDOWN_KEY = "netbeans";

    final protected Map<String, StandardContext> explicitApps = new HashMap<>();
    final private Tomcat tomcat;
    /**
     * If {@code true} then there is a class
     * {@code com.sun.faces.config.ConfigureListener} on the {@code classpath}.
     */
    protected boolean jsfSupported;
    /**
     * If {@code true} then there is a class
     * {@code org.jboss.weld.environment.servlet.Listener} on the
     * {@code classpath}.
     */
    protected boolean weldSupported;

    private static CommandManager commandManager = null;

    //
    // Dynamic Config properties. May be set on request level
    // 
    /**
     * When {@literal true} then deployment commands are accessible at runtime.
     */
    private boolean runtimeDeploymentSupported;
    /**
     * When {@literal null} then a {@literal ShutdowmHandler} is unaccessible.
     */
    private String runtimeShutdownToken;

    /**
     *
     * @param tomcat
     */
    private CommandManager(Tomcat tomcat) {
        
        this.tomcat = tomcat;
    }

    public static CommandManager getInstance() {
        return commandManager;
    }

    public static CommandManager start(Tomcat tomcat) {
        if (commandManager != null) {
            return commandManager;
        }
        commandManager = new CommandManager(tomcat);
        /*        if (!commandManager.isDevelopmentMode()) {
         ClassLoader cl = ClassLoader.getSystemClassLoader();
         //URL url = commandManager.getClass().getClassLoader().getResource(Utils.WEB_APPS_PACK);
         URL url = cl.getResource(Utils.WEB_APPS_PACK);            
         if (url != null) {
         commandManager.unpackRegisteredWebapps(url); //warref, webref and internal web apps
         }
         }
         */
        tomcat.getServer().addLifecycleListener(commandManager);

        return commandManager;
    }

    public boolean isRuntimeDeploymentSupported() {
        return runtimeDeploymentSupported;
    }

    public void setRuntimeDeploymentSupported(boolean runtimeDeploymentSupported) {
        this.runtimeDeploymentSupported = runtimeDeploymentSupported;
    }

    public String getRuntimeShutdownToken() {
        return runtimeShutdownToken;
    }

    public void setRuntimeShutdownToken(String runtimeShutdownToken) {
        this.runtimeShutdownToken = runtimeShutdownToken;
    }

    protected int getShutdownPort() {
        String prop = null;

        File f;
        Properties props = Utils.loadServerProperties(isDevelopmentMode());
        if (props != null) {
            prop = props.getProperty(Utils.SHUTDOWN_PORT_PROP);
        }
        int p = -1;
        return prop == null ? -1 : Integer.parseInt(prop);
    }

    protected void setShutdownPort() {
        if (!isDevelopmentMode()) {
            return;
        }
        int port = getShutdownPort();
        if (port > 0) {
            tomcat.getServer().setPort(port);
            if (!isDevelopmentMode() && getRuntimeShutdownToken() == null) {
                return;
            }
            String token = Utils.SHUTDOWN_KEY;
            if (!isDevelopmentMode()) {
                token = getRuntimeShutdownToken();
            }
            tomcat.getServer().setShutdown(token);
        }
    }

    public static boolean isDevelopmentMode() {
        return new File("./" + Utils.SERVER_PROJECT_XML_FILE).exists();
    }

    @Override
    public void lifecycleEvent(LifecycleEvent e) {
        System.out.println("LIFECYCLEEVENT STATE=" + e.getLifecycle().getState().name());

        /**
         * ****************** INITIALIZING PHASE ********************
         */
        if (e.getLifecycle().getState().equals(LifecycleState.INITIALIZING)) {

            tryLoadFacesServlet();
            //tryLoadWeldListener();
            Utils.clearInstanceCopyBaseDir(tomcat);

            File baseDir = new File(System.getProperty("java.io.tmpdir"));
            tomcat.addContext("", baseDir.getAbsolutePath());

            tomcat.addServlet("", "CommandManager", this)
                    .addMapping("/jeeserver/manager");
            setShutdownPort();

            if (!isDevelopmentMode()) {
                URL url = getClass().getClassLoader().getResource(Utils.WEB_APPS_PACK);
                if (url != null) {
                    unpackRegisteredWebapps(url); //warref, webref and internal web apps
                }
            }

            // We must create and add handlers for web apps that are 
            // registered as webref, warref or are internal projects.
            for (Container c : tomcat.getHost().findChildren()) {
                if ((c instanceof StandardContext) && !c.getName().trim().isEmpty()) {
                    StandardContext sc = (StandardContext) c;
                    explicitApps.put(c.getName(), sc);
                    addApplicationListeners(sc);
                    sc.addLifecycleListener(new AppLifecycleListener(this));
                }
            }//for
        }// if LifecycleState.INITIALIZING

        /**
         * ****************** STARTED PHASE ********************
         */
        if (e.getLifecycle().getState().equals(LifecycleState.STARTED)) {

            if (!isDevelopmentMode()) {
                URL url = getClass().getClassLoader().getResource(Utils.WEB_APPS_PACK);
                if (url != null) {
                    deployRegisteredWebapps(Utils.getUnpackedWebAppsDir(tomcat)); //warref, webref and internal web apps
                } else {
                    String webappsDir = Utils.WEBAPPS_DEFAULT_DIR_NAME;
                    Properties serverProps = Utils.loadServerProperties(isDevelopmentMode());
                    if (serverProps != null && serverProps.getProperty(Utils.WEBAPPS_DIR_PROP) != null) {
                        webappsDir = serverProps.getProperty(Utils.WEBAPPS_DIR_PROP);
                    }
                    deployRegisteredWebapps("./" + webappsDir);
                }
            }
            tomcat.getServer().removeLifecycleListener(this);
        }

    }

    /**
     * The method is called only when web applications are packaged in the
     * server jar.
     *
     * @param url the resource base directory {@code entry} in the server jar
     * which can be obtained by applying 
     *  {@code getClass().getClassLoader().getResource(Utils.WEB_APPS_PACK) }
     */
    public void unpackRegisteredWebapps(URL url) {
        try {
            System.out.println("---- unpackRegisteredWebapps--- url=" + url);
            String serverJar = url.getPath().substring(6, url.getPath().indexOf("!"));
            System.out.println("CM: ==== unpackRegisteredWebapps serverJar=" + serverJar);
            String tmpDir = Utils.getUnpackedWebAppsDir(tomcat);
            File targetFolder = new File(tmpDir);
            if (targetFolder.exists()) {
                try {
                    ZipUtil.deleteDirs(targetFolder.toPath());
                } catch (IOException ex) {
                    Logger.getLogger(CommandManager.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("---- unpackRegisteredWebapps: delete temp dir exception ---");
                }
            }
            String webAppsEntry = "/" + Utils.WEB_APPS_PACK;
            ZipUtil.unzip(new File(serverJar), webAppsEntry, targetFolder);
            //
            // Now unzip each .war file
            //
            for (File f : targetFolder.listFiles()) {
                System.out.println("FILE: " + f.getAbsolutePath());
                if (f.getName().endsWith(".war") && f.isFile() ) {
                    String dir = f.getName().substring(0,f.getName().length()-4);
                    ZipUtil.unzip(f, "/", new File(targetFolder.getAbsolutePath() + "/" + dir));
                    f.delete();
                }
            }
            //deployRegisteredWebapps(tmpDir);
        } catch (IOException ex) {
            Logger.getLogger(CommandManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected void deployRegisteredWebapps(String webappsDir) {
        File dir = new File(webappsDir);
        System.out.println("---- deployRegisteredWebapps: DIR !!!!!! = " + dir);

        if (!dir.exists()) {
            return;
        }
        System.out.println("---- deployRegisteredWebapps: DIR EXISTS !!!!!! = " + dir);

        File[] fileList = dir.listFiles();
        for (File f : fileList) {
            String contextPath;
            Properties props = new Properties();
            if (f.isDirectory()) {
                contextPath = "/" + f.getName();
                File propFile = new File(dir.getAbsolutePath() + "/" + f.getName() + "/META-INF/" + Utils.WEBAPP_CONFIG_FILE);

                if (propFile.exists()) {
                    try (FileInputStream fis = new FileInputStream(propFile)) {
                        // OLDprops = Utils.getContextProperties(propFile);
                        props = Utils.getContextProperties(fis);
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(CommandManager.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(CommandManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    contextPath = props.getProperty(Utils.CONTEXTPATH_PROP);
                }
            } else if (f.getName().endsWith(".war")) {
                contextPath = extractContextPath(f);
            } else {
                continue;
            }
            if (explicitApps.containsKey(contextPath)) {
                continue;
            }
//            System.out.println("before runtumeDeploy f.contextPath" + contextPath + "; f.absPath=" + f.getAbsolutePath());
            runtimeDeploy(contextPath, f.getAbsolutePath());

        }
    }

    protected void addApplicationListeners(StandardContext ctx) {
        if (jsfSupported) {
            //ctx.addApplicationListener("com.sun.faces.config.ConfigureListener");
        }
        if (weldSupported) {
            //((StandardContext) c).addApplicationListener("org.jboss.weld.environment.servlet.Listener");
        }

    }

    protected void runtimeDeploy(String contextPath, String webDir) {

        System.out.println("runtimeDeploy started for cp=" + contextPath + "; webDir=" + webDir);
        if (true) {
            //deploy(contextPath, webDir);
        }
        Container[] chs = tomcat.getHost().findChildren();
        if (chs == null) {
            return;
        }
        for (Container c : chs) {
            if (contextPath.equals(c.getName())) {
                System.out.println("runtimeDeploy: there is a handler with the same contextPath=" + contextPath + "( webDir=" + webDir + ")");
                return;
            }
        }

        try {

            StandardContext c = createWebApp(contextPath, webDir);
            c.setOriginalDocBase(webDir);
            System.out.println("runtimeDeploy: ADD JSF SUPPORT");
            addApplicationListeners(c);
            tomcat.getHost().addChild(c);
        } catch (Exception ex) {
            Logger.getLogger(CommandManager.class
                    .getName()).log(Level.SEVERE, null, ex);
            System.out.println("initialDeploy() Exception " + ex.getMessage());

        }

        //info("After RUNTIMEDEPLOY");
    }

    public StandardContext createWebApp(String contextPath, String docBase) {
        StandardContext ctx = (StandardContext) tomcat.addWebapp(TomcatStubHost.getHost(tomcat.getHost()), contextPath, docBase);
        ctx.setUnpackWAR(true);
        addApplicationListeners(ctx);
        return ctx;
    }

    protected void tryLoadWeldListener() {
        weldSupported = true;
        try {
            tomcat.getClass().getClassLoader().loadClass("org.jboss.weld.environment.servlet.Listener");
        } catch (ClassNotFoundException ex) {
            weldSupported = false;
        }
    }

    protected void tryLoadFacesServlet() {
        jsfSupported = true;
        try {
            tomcat.getClass().getClassLoader().loadClass("javax.faces.webapp.FacesServlet");
            tomcat.getClass().getClassLoader().loadClass("com.sun.faces.config.ConfigureListener");
        } catch (ClassNotFoundException ex) {
            jsfSupported = false;
        }
    }

    protected String extractContextPath(File war) {
        String appName = war.getName().substring(0, war.getName().length() - 4);
        String contextPath = "/" + appName;
        try {
            ZipFile zipFile = new ZipFile(war);

            ZipEntry e = zipFile.getEntry("META-INF/" + Utils.WEBAPP_CONFIG_FILE);
            if (e != null) {
                try (InputStream in = zipFile.getInputStream(e)) {
                    Properties props = Utils.getContextProperties(in);
                    contextPath = props.getProperty(Utils.CONTEXTPATH_PROP);
                }
            }
            
        } catch (IOException ex) {
            Logger.getLogger(CommandManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        return contextPath;
    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String cmd = request.getParameter("cmd");
        String text = "";
 
        System.out.println("*** cmd=" + cmd);
        switch (cmd) {
            case "getcopydir":
                text = getCopyDir(request);
                break;
            case "start":
                start(request);
                break;
            case "stop":
                stop(request);
                break;
            case "deploy":
                deploy(request);
                break;
            case "undeploy":
                undeploy(request);
                break;
            case "redeploy":
                redeploy(request);
                break;
            case "info":
                info();
                break;
            case "printinfo":
                text = new Reporter().buildTextInfo();
                System.out.println(text);
                break;
            case "deploywar":
                text = deployWar(request);
                break;
            case "undeploywar":
                undeployWar(request);
                break;

        }//switch
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(text);
    }

    protected String getCopyDir(HttpServletRequest request) {
        System.out.println("COMMAND = getcopydir");
        String contextPath = request.getParameter("cp");
        String webDir = request.getParameter("dir");
        webDir = new File(webDir).getAbsolutePath();

        String s = getCopyDir(contextPath, webDir);
        if (s == null) {
            s = "";
        }
        s = s.replace("\\", "/");
        return s;

    }

    protected String getCopyDir(String contextPath, String webDir) {

        System.out.println("getcopydir started for cp=" + contextPath + "; webDir=" + webDir);

        StandardContext webapp = (StandardContext) tomcat.getHost().findChild(contextPath);
        if (webapp == null) {
            System.out.println("getcopydir: no handler found");
            return null;
        }
        System.out.println("commandManager.getcopydir: docBase=" + webapp.getDocBase());
        return webapp.getDocBase();
    }

    public void start(HttpServletRequest request) {
        if (!isDevelopmentMode() && !isRuntimeDeploymentSupported()) {
            return;
        }

        String contextPath = request.getParameter("cp");
        String webDir = request.getParameter("dir");
        webDir = new File(webDir).getAbsolutePath();

        start(contextPath, webDir);

    }

    public void start(String contextPath, String webDir) {

        System.out.println("start command for cp=" + contextPath + "; webDir=" + webDir);

        Container[] chs = tomcat.getHost().findChildren();
        if (chs == null) {
            return;
        }
        for (Container c : chs) {
            if (contextPath.equals(c.getName())) {
                StandardContext sc = (StandardContext) c;

                try {
                    if (!sc.getState().equals(LifecycleState.STARTED)) {
                        sc.start();
                    }

                } catch (LifecycleException ex) {
                    Logger.getLogger(CommandManager.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
                break;
            }
        }

        info("After START");
    }

    protected void stop(HttpServletRequest request) {
        if (!isDevelopmentMode() && !isRuntimeDeploymentSupported()) {
            return;
        }

        String contextPath = request.getParameter("cp");
        String webDir = request.getParameter("dir");
        webDir = new File(webDir).getAbsolutePath();

        stop(contextPath, webDir);
    }

    protected void stop(String contextPath, String webDir) {
        System.out.println("stop command started for cp=" + contextPath + "; webDir=" + webDir);

        Container[] chs = tomcat.getHost().findChildren();
        if (chs == null) {
            return;
        }
        for (Container c : chs) {
            if (contextPath.equals(c.getName())) {
                StandardContext sc = (StandardContext) c;
                try {
                    c.stop();

                } catch (LifecycleException ex) {
                    Logger.getLogger(CommandManager.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
                break;
            }
        }
        info("After STOP");
    }

    protected String deployWar(HttpServletRequest request) {
        if (!isDevelopmentMode() && !isRuntimeDeploymentSupported()) {
            return null;
        }
        String contextPath = request.getParameter("cp");
        String webDir = request.getParameter("dir");
        webDir = new File(webDir).getAbsolutePath();

        return deployWar(contextPath, webDir);
    }

    protected String deployWar(String contextPath, String webDir) {

        System.out.println("deploywar command started for cp=" + contextPath + "; webDir=" + webDir);

        Container[] chs = tomcat.getHost().findChildren();
        if (chs == null) {
            return null;
        }
        for (Container c : chs) {
            if (contextPath.equals(c.getName())) {
                System.out.println("deploy: there is a handler with the same contextPath=" + contextPath + "( webDir=" + webDir + ")");
                return null;
            }
        }

        for (Container c : chs) {
            if (!(c instanceof StandardContext)) {
                continue;
            }
            if (c.getName() == null || c.getName().trim().isEmpty()) {
                continue;
            }
            String docBase = new File(((Context) c).getDocBase()).getAbsolutePath();
            if (webDir.equals(docBase)) {
                System.out.println("deploy: there is a handler with the same webDir=" + webDir + ". Execute undeploy");
                undeploy(contextPath, webDir);
            }
        }
        String result = extractContextPath(new File(webDir));
        try {
            StandardContext c = (StandardContext) tomcat.addWebapp(result, webDir);
        } catch (ServletException ex) {
            Logger.getLogger(CommandManager.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        info("After DEPLOYWAR");
        return result;
    }

    protected void undeployWar(HttpServletRequest request) {
        if (!isDevelopmentMode() && !isRuntimeDeploymentSupported()) {
            return;
        }
        String webDir = request.getParameter("dir");
        webDir = new File(webDir).getAbsolutePath();

        String contextPath = request.getParameter("cp");
        undeployWar(contextPath, webDir);
    }

    protected void undeployWar(String contextPath, String webDir) {

        System.out.println("undeploywar command started for cp=" + contextPath + "; webDir=" + webDir);

        Container[] chs = tomcat.getHost().findChildren();
        if (chs == null) {
            return;
        }
        for (Container c : chs) {
            if (contextPath.equals(c.getName())) {
                tomcat.getHost().removeChild(c);
                break;
            }
        }
        info("After UNDEPLOYWAR");
    }

    protected void deploy(HttpServletRequest request) {
        if (!isDevelopmentMode() && !isRuntimeDeploymentSupported()) {
            return;
        }
        String contextPath = request.getParameter("cp");
        String webDir = request.getParameter("dir");
        webDir = new File(webDir).getAbsolutePath();

        String projectType = request.getParameter("projtype");
        System.out.println("PROJECT TYPE=" + projectType);
        if (Utils.DEPLOY_HTML5_PROJECTTYPE.equals(projectType)) {
            deployHtml5(contextPath, webDir);
        } else {
            deploy(contextPath, webDir);
        }
    }

    protected void deployHtml5(String contextPath, String webDir) {
        System.out.println("deploy command started for cp=" + contextPath + "; webDir=" + webDir);

        Container[] chs = tomcat.getHost().findChildren();
        if (chs == null) {
            return;
        }
        for (Container c : chs) {
            if (c instanceof StandardContext) {
                System.out.println("deploy: docBase = " + ((StandardContext) c).getDocBase());
            }
            if (contextPath.equals(c.getName())) {
                StandardContext old = explicitApps.get(c.getName());
                if (old == c) {
                    undeploy(contextPath, webDir);
                    break;
                }
                System.out.println("deploy: there is a handler with the same contextPath=" + contextPath + "( webDir=" + webDir + ")");
                return;
            }
        }
        String path = getHtml5WarPath(webDir); // to deploy
        System.out.println("deploy: path=" + path);
        for (Container c : chs) {
            if (!(c instanceof StandardContext)) {
                continue;
            }
            if (c.getName() == null || c.getName().trim().isEmpty()) {
                continue;
            }
            String docBase = new File(((Context) c).getDocBase()).getAbsolutePath();
            if (path.equals(docBase)) {
                System.out.println("deploy: there is a handler with the same webDir=" + webDir + ". Execute undeploy");
                undeploy(contextPath, webDir);
            }
        }
        try {
            String copyDir = path;
            System.out.println("deploy: copyDir=" + path);
            
            StandardContext c = (StandardContext) tomcat.addWebapp(contextPath, copyDir);
            c.setOriginalDocBase(path);
            //3.12 c.setReloadable(true);

        } catch (ServletException ex) {
            Logger.getLogger(CommandManager.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        info("After DEPLOY");
    }

    protected void deploy(String contextPath, String webDir) {

        System.out.println("deploy command started for cp=" + contextPath + "; webDir=" + webDir);

        Container[] chs = tomcat.getHost().findChildren();
        if (chs == null) {
            return;
        }
        for (Container c : chs) {
            if (c instanceof StandardContext) {
                System.out.println("deploy docBase = " + ((StandardContext) c).getDocBase());
            }
            if (contextPath.equals(c.getName())) {
                StandardContext old = explicitApps.get(c.getName());
                if (old == c) {
                    undeploy(contextPath, webDir);
                    break;
                }
                System.out.println("deploy: there is a handler with the same contextPath=" + contextPath + "( webDir=" + webDir + ")");
                return;
            }
        }
        String path = getWarPath(webDir); // to deploy

        for (Container c : chs) {
            if (!(c instanceof StandardContext)) {
                continue;
            }
            if (c.getName() == null || c.getName().trim().isEmpty()) {
                continue;
            }
            String docBase = new File(((Context) c).getDocBase()).getAbsolutePath();
            if (path.equals(docBase)) {
                System.out.println("deploy: there is a handler with the same webDir=" + webDir + ". Execute undeploy");
                undeploy(contextPath, webDir);
            }
        }
        boolean antiResource = Utils.isWindows();
        Properties props = Utils.getContextProperties(path);
        String s = props.getProperty(Utils.ANTI_LOCK_PROP_NAME);
        //System.out.println("deploy: ANTI_LOCK_PROP_NAME = " + s );
        
        if (s != null) {
            antiResource = Boolean.parseBoolean(s);
        }
        try {
            String copyDir = getWarPath(webDir);
            if (antiResource) {
                copyDir = Utils.copyBaseDir(tomcat, copyDir);
            }

            //
            // We use the custom method createWebApp instead of tomcat.addWebApp
            // because we want to apply the method 
            // addApplicationListener(com.sun.faces.config.ConfigureListener).
            //
            StandardContext c = createWebApp(contextPath, copyDir);
            tomcat.getHost().addChild(c);
            c.setOriginalDocBase(getWarPath(webDir));
            c.setAntiResourceLocking(antiResource);
        } catch (Exception ex) {
            Logger.getLogger(CommandManager.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        info("After DEPLOY");
    }

    protected void undeploy(HttpServletRequest request) {
        if (!isDevelopmentMode() && !isRuntimeDeploymentSupported()) {
            return;
        }

        String webDir = request.getParameter("dir");
        webDir = new File(webDir).getAbsolutePath();

        String contextPath = request.getParameter("cp");
        undeploy(contextPath, webDir);
    }

    protected void undeploy(String contextPath, String webDir) {

        System.out.println("undeploy command started for cp=" + contextPath + "; webDir=" + webDir);

        Container[] chs = tomcat.getHost().findChildren();
        if (chs == null) {
            return;
        }
        for (Container c : chs) {
            if (contextPath.equals(c.getName())) {
                StandardContext sc = (StandardContext) c;
                tomcat.getHost().removeChild(c);
                break;
            }
        }
        info("After UNDEPLOY");
    }

    protected void redeploy(HttpServletRequest request) {
        if (!isDevelopmentMode() && !isRuntimeDeploymentSupported()) {
            return;
        }

        String contextPath = request.getParameter("cp");
        String webDir = request.getParameter("dir");
        webDir = new File(webDir).getAbsolutePath();

        redeploy(contextPath, webDir);
    }

    protected void redeploy(String contextPath, String webDir) {

        System.out.println("redeploy command started for cp=" + contextPath + "; webDir=" + webDir);

        Container[] chs = tomcat.getHost().findChildren();
        if (chs == null) {
            return;
        }
        for (Container c : chs) {
            if (contextPath.equals(c.getName()) && (c instanceof Context)) {
                System.out.println("deploy: there is a handler with the same contextPath=" + contextPath + "( webDir=" + webDir + ")");
                ((Context) c).reload();
            }
        }
        info("After REDEPLOY");
    }

    public void info() {
        info("");
    }

    public void info(String msg) {

        Container[] chs = tomcat.getHost().findChildren();

        System.out.println("========== " + msg + " Registered Web Applications ==========");
        for (int i = 0; i < chs.length; i++) {
            int n = i + 1;
            if (!(chs[i] instanceof StandardContext)) {
                continue;
            }
            System.out.println("contextPath= " + chs[i].getName());
            System.out.println("docBase= " + ((StandardContext) chs[i]).getDocBase());
            if (i != chs.length - 1) {
                System.out.println("-------------------------------------------");
            }
        }
    }

    protected String getWarPath(String webDir) {
        String path = new File(webDir).getAbsolutePath();

        File file = new File(path + "/build/web");

        if (file.exists()) {
            path = file.getAbsolutePath();
        } else {
            path = Utils.getMavenBuildDir(webDir);
        }
        return path;
    }

    protected String getWarPathOld(String webDir) {
        String path = new File(webDir).getAbsolutePath();
        String appName = new File(path).getName();
        if (appName.endsWith(".war")) {
            return webDir;
        }
        File file = new File(path + "/build/web");

        if (file.exists()) {
            path = file.getAbsolutePath();
        } else {
            File target = new File(path + "/target");
            File targetDist = null;
            for (File f : target.listFiles()) {
                if (f.isDirectory() && f.getName().startsWith(appName)) {
                    targetDist = f;
                    break;
                }
            }
            if (targetDist != null) {
                path = targetDist.getAbsolutePath();
            }
        }
        return path;
    }

    protected String getHtml5WarPath(String webDir) {
        String path = new File(webDir).getAbsolutePath();

        Properties props = Utils.loadHtml5ProjectProperties(webDir);
        if (props == null) {
            return path;
        }
        String siteRoot = Utils.resolve(Utils.HTML5_SITE_ROOT_PROP, props);
        if (siteRoot == null) {
            siteRoot = Utils.HTML5_DEFAULT_SITE_ROOT_PROP;
        }
        return new File(path + "/" + siteRoot).getAbsolutePath();
    }

    public static class AppLifecycleListener implements LifecycleListener {

        final CommandManager cm;

        public AppLifecycleListener(CommandManager cm) {//String copyDir) {
            this.cm = cm;
        }

        @Override
        public void lifecycleEvent(LifecycleEvent le) {
            System.out.println("!!! ********* AppLifecycleListener STATE=" + le.getLifecycle().getStateName());
            if (le.getLifecycle().getState().equals(LifecycleState.STARTING_PREP)) {
                StandardContext c = (StandardContext) le.getSource();
                c.setUnpackWAR(false);
            }

            //
            // -------------  STARTED ------------
            //
            if (le.getLifecycle().getState().equals(LifecycleState.STARTED)) {
                StandardContext c = (StandardContext) le.getSource();
                c.removeLifecycleListener(this);
            }
            //
            // -------------  INITIALIZING ------------
            //
            if (le.getLifecycle().getState().equals(LifecycleState.INITIALIZING)) {
                PathResolver pathResolver = null;
                StandardContext c = (StandardContext) le.getSource();
                if (!CommandManager.isDevelopmentMode()) {
                    URL url = getClass().getClassLoader().getResource(Utils.WEB_APPS_PACK);
                    if (url != null) {
                        //cm.unpackRegisteredWebapps(url); //warref, webref and internal web apps
                        String webDir = Utils.getUnpackedWebAppsDir(cm.tomcat);
                        System.out.println("AppLificycleListener webappDrr=" + webDir);
                        pathResolver = new PathResolver(Utils.getUnpackedWebAppsDir(cm.tomcat));
                    }
                }
                if (pathResolver == null) {
                    pathResolver = new PathResolver(null);
                }

                String newPath = pathResolver.pathFor(c.getDocBase(), CommandManager.isDevelopmentMode());
                System.out.println("AppLifecycleListener new resolvedPath=" + newPath);
                c.setDocBase(newPath); // actual path

                if (!CommandManager.isDevelopmentMode()) {
                    return;
                }
                boolean antiResource = Utils.isWindows();
                System.out.println("****** 1");
                Properties props = Utils.getContextProperties(c.getDocBase());
                String s = props.getProperty(Utils.ANTI_LOCK_PROP_NAME);
                if (s != null) {
                    antiResource = Boolean.parseBoolean(s);
                }

                if (antiResource) {

                    String copyDir = Utils.copyBaseDir(cm.tomcat, c.getDocBase());

                    c.setOriginalDocBase(c.getDocBase());
                    c.setDocBase(copyDir);
                }
                c.setAntiResourceLocking(antiResource);
            }
        }
    }

    public class Reporter {

        public String buildTextInfo() {
            StringBuilder sb = new StringBuilder();
            //Registered in the Web Applications folder
            // key -> project name
            // value -> project dir or .webref|| .warref properties file
            Map<String, File> registered = Utils.getRegisteredApps();
            //Defined in the server code
            Map<String, File> hardCoded = getHardCodedApps();
            // All deployed web applications
            Map<String, File> deployed = getDeployedApps();
            sb.append(getServerInfo())
                    .append(getHardCodedInfo(hardCoded, registered, deployed))
                    .append(getDynamicallyStartedInfo(hardCoded, registered, deployed));

            return sb.toString();
        }

        protected StringBuilder getServerInfo() {
            Properties props = Utils.loadServerProperties(isDevelopmentMode());
            StringBuilder sb = new StringBuilder();
            int len = 50;
            String sep = System.lineSeparator();
            File file = null;
            try {
                file = new File(".").getCanonicalFile();
            } catch (IOException ex) {
                Logger.getLogger(CommandManager.class.getName()).log(Level.SEVERE, null, ex);
                return sb;
            }

            //String s = new String();
            sb.append(replicate('=', len))
                    .append(sep)
                    .append("SERVER: ")
                    .append("\t\t")
                    .append(props.getProperty(Utils.SERVER_ID_PROP))
                    .append(sep)
                    .append("--- Name:\t\t")
                    .append(file.getName())
                    .append(sep)
                    .append("--- Location:\t\t")
                    .append(file.getAbsolutePath())
                    .append(sep)
                    .append("--- Host:\t\t")
                    .append(props.getProperty(Utils.HOST_PROP))
                    .append(sep)
                    .append("--- Http Port:\t\t")
                    .append(props.getProperty(Utils.HTTP_PORT_PROP))
                    .append(sep)
                    .append("--- Debug Port:\t\t")
                    .append(props.getProperty(Utils.DEBUG_PORT_PROP))
                    .append(sep)
                    .append("--- Incr Deploy:\t")
                    .append(props.getProperty(Utils.INCREMENTAL_DEPLOYMENT))
                    .append(sep)
                    .append(replicate('=', len))
                    .append(sep);

            return sb;
        }

        protected StringBuilder getHardCodedInfo(Map<String, File> hardCoded, Map<String, File> registered, Map<String, File> deployed) {
            StringBuilder sb = new StringBuilder();
            int len = 50;
            String sep = System.lineSeparator();
            sb.append("HARD CODED WEB APPLICATIONS (Count=")
                    .append(hardCoded.size())
                    .append(")")
                    .append(sep)
                    .append(replicate('_', len))
                    .append(sep);

            int i = 1;
            for (Map.Entry<String, File> e : hardCoded.entrySet()) {
                String appName = e.getKey();
                String reg = registered.containsKey(appName) ? ". Registered as '.webref' || '.warref'" : ". Not registered as '.webref' || '.warref'";
                String dep = deployed.containsKey(appName) ? ". Deployed" : ". Not Deployed";
                File appDir = e.getValue();
                System.out.println("****** 3");
                Properties props = Utils.getContextProperties(appDir.getAbsolutePath());
                String contextPath = props.getProperty("contextPath");
                String based = " Html5 project";
                if (isAntWebProject(appDir.getAbsolutePath())) {
                    based = "Ant-based";
                } else if (Utils.isBuildOfMavenProject(appDir.getAbsolutePath())) {
                    based = " Maven-based";
                }
                StringBuilder lastLine = i == hardCoded.size() ? replicate('=', len) : replicate('_', len);
                sb.append(i++)
                        .append(")  ")
                        .append(based)
                        .append(dep)
                        .append(reg)
                        .append(sep)
                        .append("--- Name:\t\t")
                        .append(appName)
                        .append(sep)
                        .append("--- Context Path:\t")
                        .append(contextPath)
                        .append(sep)
                        .append("--- Location:\t\t")
                        .append(appDir.getAbsolutePath())
                        .append(sep)
                        .append(lastLine)
                        .append(sep);

            }

            return sb;
        }

        protected StringBuilder getDynamicallyStartedInfo(Map<String, File> hardCoded, Map<String, File> registered, Map<String, File> deployed) {
            StringBuilder sb = new StringBuilder();
            int len = 50;
            String sep = System.lineSeparator();
            int count = deployed.size() - hardCoded.size();
            if (count <= 0) {
                return sb;
            }
            sb.append("DYNAMICALLY STARTED WEB APPLICATIONS (Count=")
                    .append(count)
                    .append(")")
                    .append(sep)
                    .append(replicate('_', len))
                    .append(sep);

            int i = 1;
            for (Map.Entry<String, File> e : deployed.entrySet()) {

                String appName = e.getKey();
                if (hardCoded.containsKey(appName)) {
                    continue;
                }
                String reg = registered.containsKey(appName) ? ". Registered as '.webref' || '.warref'" : ". Not registered as '.webref' || '.warref'";
                String dep = ". Deployed";
                File appDir = e.getValue();

                String based = " Html5 project";
                if (isAntWebProject(appDir.getAbsolutePath())) {
                    based = "Ant-based";
                } else if (Utils.isBuildOfMavenProject(appDir.getAbsolutePath())) {
                    based = " Maven-based";
                }
                System.out.println("****** 4");
                Properties props = Utils.getContextProperties(appDir.getAbsolutePath());
                String contextPath = props.getProperty("contextPath");

                sb.append(i++)
                        .append(")  ")
                        .append(based)
                        .append(dep)
                        .append(reg)
                        .append(sep)
                        .append("--- Name:\t\t")
                        .append(appName)
                        .append(sep)
                        .append("--- Context Path:\t")
                        .append(contextPath)
                        .append(sep)
                        .append("--- Location:\t\t")
                        .append(appDir.getAbsolutePath())
                        .append(sep)
                        .append(replicate('_', len))
                        .append(sep);

            }

            return sb;
        }

        protected StringBuilder replicate(char c, int count) {
            StringBuilder sb = new StringBuilder(count);
            for (int i = 0; i < count; i++) {
                sb.append(c);
            }
            return sb;
        }

        
        protected Map<String, File> getHardCodedApps() {
            Map<String, File> map = new HashMap<>();
            for (Map.Entry<String, StandardContext> e : explicitApps.entrySet()) {
                File projDir = new File(e.getValue().getDocBase());
                String projName = getProjectNameByDocBase(e.getValue().getDocBase());
                map.put(projName, projDir);
            }
            return map;
        }

        protected Map<String, File> getDeployedApps() {
            Map<String, File> map = new HashMap<>();
            for (Container c : tomcat.getHost().findChildren()) {
                if ((c instanceof StandardContext) && !c.getName().trim().isEmpty()) {
                    File projDir = new File(((StandardContext) c).getOriginalDocBase());
                    String projName = getProjectNameByDocBase(((StandardContext) c).getOriginalDocBase());
                    map.put(projName, projDir);
                }
            }
            return map;
        }

        public String getProjectNameByDocBase(String path) {
            File f = new File(path);
            if (f.exists() && path.endsWith(".war")) {
                String nm = f.getName();
                nm = nm.substring(0, nm.length() - 4);
                return nm;
            }
            while (!isProjectDir(f)) {
                f = f.getParentFile();
            }

            return f.getName();
        }

        private boolean isProjectDir(File file) {
            boolean result = false;

            if (isAntWebProject(file.getPath()) || isBuildOfMavenProject(file.getPath())) {
                result = true;
            } else if (new File(file.getPath() + "/nbproject/project.xml").exists()) {
                result = true;
            }
            return result;
        }

        public boolean isAntWebProject(String path) {
            boolean b = false;

            File war = new File(path);
            if (war.exists() && war.isDirectory()) {
                if (new File(path + "/nbproject/project.xml").exists()) {
                    if (new File(path + "build/web").exists()) {
                        b = true;
                    }
                }
            }
            return b;
        }

    }//class

}//class
