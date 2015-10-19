/**
 * This file is part of Base JEE Server support in NetBeans IDE.
 *
 * Base JEE Server support in NetBeans IDE is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * Base JEE Server support in NetBeans IDE is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should see the GNU General Public License here:
 * <http://www.gnu.org/licenses/>.
 */
package org.netbeans.plugin.support.embedded.jetty;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.AbstractHandlerContainer;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ShutdownHandler;
import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 *
 * @author V. Shyshkin
 */
public class NbDeployHandler extends AbstractHandler implements LifeCycle.Listener {

    public static final String HTTP_PORT_PROP = "httpportnumber";

    private static final Logger LOG = Logger.getLogger(Utils.class.getName());

    private static final String WELD_INIT_PARAMETER = "org.jboss.weld.environment.container.class";

    private static final String[] REQUIRED_BEANS_XML_PATHS = new String[]{
        "/WEB-INF/beans.xml",
        "/META-INF/beans.xml",
        "/WEB-INF/classes/META-INF/beans.xml"
    };


    private final String shutdownKey = "netbeans";
    /**
     * Explicitly (hard coded) defined instances of the {@literal WebAppContext}
     * class.
     */
    protected Map<String, WebAppContext> explicitApps = new HashMap<>();
    protected Map<String, WebAppContext> explicitDynApps = new HashMap<>();

    /**
     * When {@literal true} the the server supports annotations.
     */
    protected boolean annotationsSupported;
    /**
     * When {@literal true} then the server supports jsf.
     */
    protected boolean jsfSupported;
    /**
     * When {@literal true} then the server supports Weld.
     */
    protected boolean weldSupported;

    /**
     * When {@literal true} then deployment commands are accessible at runtime.
     */
    protected boolean runtimeDeploymentSupported;
    /**
     * When {@literal null} then a {@literal ShutdowmHandler} is unaccessible.
     */
    protected String runtimeShutdownToken;

    private boolean verbose;

    private ServerConfig serverConfig;

    /**
     * Create a new instance of the class.
     */
    public NbDeployHandler() {
        super();
        annotationsSupported = false;
    }
    
    /**
     * Determines whether the application runs from within NetBeans IDE.
     * @return {@literal true } if the the class loaded in netBeas IDE and 
     *   {@literal false } otherwise.
     */
    public static boolean isDevelopmentMode() {
        boolean b = false;
        Path p = Paths.get(System.getProperty("user.dir"), "nbproject", "project.xml");

        if (Files.exists(p)) {
            b = true;
        } else {
            p = Paths.get(System.getProperty("user.dir"), "target/classes");
            Path p1 = Paths.get(System.getProperty("user.dir"), "pom.xml");
            if (Files.exists(p) && Files.exists(p1)) {
                b = true;
            }
        }
        return b;
    }

    /**
     *
     * @return null if the method {@link createHotDeploymentServer}
     */
    public HotDeployer getHotDeployer() {
        return HotDeployer.create();
    }

    public static void enableAnnotationsJspJNDI(Server server) {
        org.eclipse.jetty.webapp.Configuration.ClassList classlist
                = org.eclipse.jetty.webapp.Configuration.ClassList
                .setServerDefault(server);
        classlist.addAfter(
                "org.eclipse.jetty.webapp.FragmentConfiguration",
                "org.eclipse.jetty.plus.webapp.EnvConfiguration",
                "org.eclipse.jetty.plus.webapp.PlusConfiguration");

        classlist.addBefore(
                "org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
                "org.eclipse.jetty.annotations.AnnotationConfiguration");
    }

    public static void enableAnnotationsJspJNDI(WebAppContext webapp) {
        webapp.setConfigurationClasses(new String[]{
            "org.eclipse.jetty.webapp.WebInfConfiguration",
            "org.eclipse.jetty.webapp.WebXmlConfiguration",
            "org.eclipse.jetty.webapp.MetaInfConfiguration",
            "org.eclipse.jetty.webapp.FragmentConfiguration",
            "org.eclipse.jetty.plus.webapp.EnvConfiguration",
            "org.eclipse.jetty.plus.webapp.PlusConfiguration",
            "org.eclipse.jetty.annotations.AnnotationConfiguration",
            "org.eclipse.jetty.webapp.JettyWebXmlConfiguration"}
        );
    }

    private void init(Server server) {

        if (server.getHandler() == null) {
            HandlerCollection hc = new HandlerCollection();

            hc.addHandler(this);
            ContextHandlerCollection chc = new ContextHandlerCollection();
            hc.addHandler(chc);
            addShutdownHandler(server, hc);
            server.setHandler(hc);
        } else if ((server.getHandler() instanceof HandlerCollection) && !((server.getHandler() instanceof ContextHandlerCollection))) {
            ContextHandlerCollection chc = findContextHandlerCollection(server);
            if (chc == null) {
                chc = new ContextHandlerCollection();
                ((HandlerCollection) server.getHandler()).addHandler(chc);
                ((HandlerCollection) server.getHandler()).addHandler(this);
                addShutdownHandler(server, ((HandlerCollection) server.getHandler()));
            } else {
                ((HandlerCollection) server.getHandler()).addHandler(this);
                addShutdownHandler(server, ((HandlerCollection) server.getHandler()));
            }
        } else {
            Handler h = server.getHandler();
            HandlerCollection hc = new HandlerCollection();
            server.setHandler(hc);
            hc.addHandler(h);

            hc.addHandler(this);
            ContextHandlerCollection chc = new ContextHandlerCollection();
            hc.addHandler(chc);
            addShutdownHandler(server, hc);
        }
        HotDeployer hd = HotDeployer.getInstance();
        if (hd != null) {
            Handler[] all = server.getChildHandlersByClass(ContextHandlerCollection.class);
            ContextHandlerCollection contexts = hd.getContextHandlers();

            boolean found = false;
            for (Handler h : all) {
                if (h == contexts) {
                    found = true;
                }
            }
            if (!found) {
                ((HandlerCollection) server.getHandler()).addHandler(contexts);
            }
        }
        //
        // Place all handlers of type DefaultHandler  at the end of HandlerCollection.
        // Otherwise some handlers (ShutdownHandker for example) may become unreachable.
        // 
        //Handler[] all = server.getChildHandlersByClass(DefaultHandler.class);
        Handler[] all = server.getChildHandlersByClass(DefaultHandler.class);
        Handler[] containers = server.getChildHandlersByClass(AbstractHandlerContainer.class);
        if (all == null || all.length == 0) {
            return;
        }
        for (Handler container : containers) {
            if (!(container instanceof HandlerCollection)) {
                continue;
            }

            HandlerCollection hc = (HandlerCollection) container;
            for (Handler h : all) {
                hc.removeHandler(h);
            }
        }

        for (Handler h : all) {
            ((HandlerCollection) server.getHandler()).addHandler(h);
        }
    }

    /*    protected boolean isRuntimeDeploymentSupported() {
     return runtimeDeploymentSupported;
     }

     protected void setRuntimeDeploymentSupported(boolean runtimeDeploymentSupported) {
     this.runtimeDeploymentSupported = runtimeDeploymentSupported;
     }

     public String getRuntimeShutdownToken() {
     return runtimeShutdownToken;
     }

     public void setRuntimeShutdownToken(String runtimeShutdownToken) {
     this.runtimeShutdownToken = runtimeShutdownToken;
     }
     */
    protected void addShutdownHandler(Server server, HandlerCollection to) {

//        if (!isDevelopmentMode() && getRuntimeShutdownToken() == null) {
        if (!isDevelopmentMode()) {
            return;
        }
        String token = shutdownKey;
//        if (!isDevelopmentMode()) {
//            token = getRuntimeShutdownToken();
//        }
        ShutdownHandler sh;
        if ((sh = findShutdownHandler(server)) == null) {
            sh = new ShutdownHandler(token);
        }
        to.addHandler(sh);
    }

    protected ContextHandlerCollection findContextHandlerCollection(Server server) {
        ContextHandlerCollection contextHandlerCollection = null;

        Handler[] hcs = server.getChildHandlersByClass(ContextHandlerCollection.class);
        if (hcs != null && hcs.length > 0) {
            contextHandlerCollection = (ContextHandlerCollection) hcs[0];
        }
        return contextHandlerCollection;

    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {// throws IOException, ServletException {
        System.out.println("target   =   " + target);
        response.setContentType("text/html;charset=utf-8");
        if (!"/jeeserver/manager".equals(target)) {
            return;
        }

        String cp = request.getParameter("cp");
        System.out.println("handle cp=" + cp);
        String cmd = request.getParameter("cmd");
        System.out.println("handle cmd=" + cmd);

        String text = "";
        if (cmd == null) {
            start(request);
            response.setStatus(HttpServletResponse.SC_OK);
            baseRequest.setHandled(true);

            response.getWriter().println(text);

        }
        switch (cmd) {
            case "deploy":
                deploy(request);
                break;
            case "getcopydir":
                text = getCopyDir(request);
                break;
            case "redeploy":
                redeploy(request);
                break;
            case "undeploy":
                undeploy(request);
                break;
            case "start":
                start(request);
                break;
            case "stop":
                stop(request);
                break;
            case "deploywar":
                text = deployWar(request);
                break;
            case "undeploywar":
                undeployWar(request);
                break;

        }//switch
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        response.getWriter().println(text);
    }

    protected void redeploy(HttpServletRequest request) {
//        if (!isDevelopmentMode() && !isRuntimeDeploymentSupported()) {
        if (!isDevelopmentMode()) {
            return;
        }

        Map<WebAppContext, ContextHandlerCollection> map = findWebApps();

        if (map.isEmpty()) {
            System.out.println("redeploy: no handler found. redeploy finished.");
            return;// null;
        }
        String oldContextPath = request.getParameter("oldcp");
        String oldWebDir = request.getParameter("olddir");
        if (oldWebDir != null) {
            oldWebDir = new File(oldWebDir).getAbsolutePath();
        }

        String contextPath = request.getParameter("cp");
        String webDir = request.getParameter("dir");
        webDir = new File(webDir).getAbsolutePath();

        System.out.println("redeploy started. Old web app: for oldcp=" + oldContextPath + "; oldWebDir=" + oldWebDir);
        System.out.println("redeploy started. New web app: for cp=" + contextPath + "; webDir=" + webDir);

        WebAppContext webapp = findWebAppContext(oldContextPath);
        if (webapp != null) {
            undeploy(request);
        }

        deploy(request);
        //start(request);
        //webapp.removeLifeCycleListener(mll);
        try {
            webapp.stop();
        } catch (Exception ex) {
            Logger.getLogger(NbDeployHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("redeploy: success");
    }

    protected WebAppContext findWebAppContext(String contextPath) {
        Handler[] contextHandlers = getServer().getChildHandlersByClass(ContextHandlerCollection.class);
        for (Handler ch : contextHandlers) {
            ContextHandlerCollection chs = (ContextHandlerCollection) ch;
            Handler[] hs = chs.getChildHandlersByClass(WebAppContext.class);
            for (Handler h : hs) {
                WebAppContext w = (WebAppContext) h;
                if (contextPath.equals(w.getContextPath())) {
                    return w;
                }
            }//for
        }
        return null;
    }

    /*protected Set<WebAppContext> findWebApps1() {
     Set<WebAppContext> map = new HashSet<>();

     Handler[] handlers = getServer().getChildHandlersByClass(WebAppContext.class);
     WebAppContext c = null;
     for (Handler ch : handlers) {
     System.out.println("&&&&&&  findWebApps1 class=" + ch.getClass());
     System.out.println("&&&&&&  findWebApps1 cp=" + ((WebAppContext)ch).getContextPath());
     map.add((WebAppContext) ch);
     }
     return map;
     }
     */
    protected Map<WebAppContext, ContextHandlerCollection> findWebApps() {
        Map<WebAppContext, ContextHandlerCollection> map = new HashMap<>();
        Handler[] contextHandlers = getServer().getChildHandlersByClass(ContextHandlerCollection.class);
        if (contextHandlers != null) {
            for (Handler ch : contextHandlers) {
                ContextHandlerCollection chs = (ContextHandlerCollection) ch;
                Handler[] hs = chs.getChildHandlersByClass(WebAppContext.class);
                for (Handler h : hs) {
                    map.put((WebAppContext) h, chs);
                }//for
            }
        }
        return map;
    }

    protected WebAppContext findWebApps(String warPath) {
        Handler[] contextHandlers = getServer().getChildHandlersByClass(ContextHandlerCollection.class);
        if (contextHandlers != null) {
            for (Handler ch : contextHandlers) {
                ContextHandlerCollection chs = (ContextHandlerCollection) ch;
                Handler[] hs = chs.getChildHandlersByClass(WebAppContext.class);
                for (Handler h : hs) {
                    return (WebAppContext) h;
                }//for
            }
        }
        return null;
    }

    protected ShutdownHandler findShutdownHandler(Server server) {
        Handler[] sh = server.getChildHandlersByClass(ShutdownHandler.class);
        return (ShutdownHandler) (sh == null || sh.length == 0 ? null : sh[0]);
    }

    protected void undeploy(HttpServletRequest request) {
        String webDir = request.getParameter("dir");
        webDir = new File(webDir).getAbsolutePath();

        String contextPath = request.getParameter("cp");
        //      return 
        undeploy(contextPath, webDir);
    }

    protected WebAppContext undeploy(WebAppContext ctx) {

        System.out.println("undeploy(WebAppContext) started for  cp=" + ctx.getContextPath() + "; war=" + ctx.getWar());

        Map<WebAppContext, ContextHandlerCollection> map = findWebApps();

        if (map.isEmpty()) {
            return null;
        }

        String contextPath = ctx.getContextPath();

        WebAppContext c = findWebAppContext(contextPath);
        if (c == null) {
            return null;
        }

        File f = c.getTempDirectory();
        try {
            if (!c.isStopped()) {
                c.stop();
            }

            System.out.println("undeploy remove handler ");
            map.get(c).removeHandler(c);
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            System.err.println("undeploy failed: " + ex.getMessage());
            return null;
        }
        System.out.println("undeploy: success");

//        printInfoAfter("undeploy");
        return c;
    }

    protected WebAppContext undeploy(String contextPath, String webDir) {

        System.out.println("undeploy started for cp=" + contextPath + "; webDir=" + webDir);

        Map<WebAppContext, ContextHandlerCollection> map = findWebApps();

        if (map.isEmpty()) {
            return null;
        }

        WebAppContext c = findWebAppContext(contextPath);
        if (c == null) {
            return null;
        }

        File f = c.getTempDirectory();
        try {
            if (!c.isStopped()) {
                c.stop();
            }

            System.out.println("undeploy remove handler ");
            map.get(c).removeHandler(c);
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            System.err.println("undeploy failed: " + ex.getMessage());
            return null;
        }
        System.out.println("undeploy: success");

//        printInfoAfter("undeploy");
        return c;
    }

    protected void start(HttpServletRequest request) {
//        if (!isDevelopmentMode() && !isRuntimeDeploymentSupported()) {
        if (!isDevelopmentMode()) {
            return;
        }

        String contextPath = request.getParameter("cp");
        String webDir = request.getParameter("dir");
        webDir = new File(webDir).getAbsolutePath();

        start(contextPath, webDir);
    }

    protected void start(String contextPath, String webDir) {

        System.out.println("start command for cp=" + contextPath + "; webDir=" + webDir);
        Map<WebAppContext, ContextHandlerCollection> map = findWebApps();

        if (map.isEmpty()) {
            return;// null;
        }
        WebAppContext webapp = findWebAppContext(contextPath);
        if (webapp == null) {
            return;// null;
        }

        try {
            copyMavenChangedClasses(webapp);
            webapp.start();
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            System.err.println("start: failed " + ex.getMessage());
            return;// webapp;
        }

        System.out.println("start: success");
    }

    protected void stop(HttpServletRequest request) {
//        if (!isDevelopmentMode() && !isRuntimeDeploymentSupported()) {
        if (!isDevelopmentMode()) {
            return;
        }

        String webDir = request.getParameter("dir");
        webDir = new File(webDir).getAbsolutePath();

        String contextPath = request.getParameter("cp");

        stop(contextPath, webDir);
    }

    protected void stop(String contextPath, String webDir) {
        System.out.println("stop command for cp=" + contextPath + "; webDir=" + webDir);
        Map<WebAppContext, ContextHandlerCollection> map = findWebApps();

        if (map.isEmpty()) {
            return;// null;
        }
        WebAppContext webapp = findWebAppContext(contextPath);
        if (webapp == null) {
            return;// null;
        }

        try {
            webapp.stop();
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            System.err.println("stop: failed " + ex.getMessage());
            return;// webapp;
        }
        System.out.println("stop: success");
    }

    protected String getCopyDir(HttpServletRequest request) {

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
        String copyDir;//= null;
        Map<WebAppContext, ContextHandlerCollection> map = findWebApps();

        if (map.isEmpty()) {
            System.out.println("getcopydir: no handler found");
            return null;
        }
        WebAppContext webapp = findWebAppContext(contextPath);
        if (webapp == null) {
            System.out.println("getcopydir: no handler found");
            return null;
        }

        System.out.println("getcopydir: found contextPath=" + webapp.getContextPath() + " (webDir=" + webapp.getWar());
        System.out.println("getcopydir: found tempDir=" + webapp.getTempDirectory());
        copyDir = webapp.getTempDirectory().getAbsolutePath() + "/webapp";
        System.out.println("getcopydir: success");
        return copyDir;
    }

    /*    public void copyChangedClasses(WebAppContext webapp) throws IOException {

     if (!webapp.isCopyWebDir() || webapp.getTempDirectory() == null || webapp.getWebInf() == null || !webapp.getWebInf().exists()) {
     return;
     }
     File to = new File(webapp.getWebInf().getFile().getAbsoluteFile() + "/classes");

     File from = new File(webapp.getWar());

     from = new File(from.getParent() + "/classes");
     //        System.out.println("GCD: copyChangedClasses from=" + from.getAbsolutePath());
     //        System.out.println("GCD: copyChangedClasses to=" + to.getAbsolutePath());
     IO.copyDir(from, to);
     }
     */
    protected void copyMavenChangedClasses(WebAppContext webapp) throws IOException {

        if (Utils.isMavenProject(webapp.getWar())) {
            return;
        }
        if (!webapp.isCopyWebDir()) {
            return;
        }

        File to = new File(webapp.getWar() + "/WEB-INF/classes");

        File from = new File(webapp.getWar());

        from = new File(from.getParent() + "/classes");
        IO.copyDir(from, to);
    }

    /*    protected void printInfoBefore(String command) {
     Map<WebAppContext, ContextHandlerCollection> map = findWebApps();

     if (map.isEmpty()) {
     System.out.println(command + ": no handler found");
     return;
     }

     System.out.println("==========  Existing Handlers ==========");
     int i = 0;
     for (WebAppContext webapp : map.keySet()) {
     System.out.println((++i) + ". cp=" + webapp.getContextPath() + "; webDir=" + webapp.getWar());
     }
     System.out.println("===================================");
     }

     protected void printInfoAfter(String command) {
     Map<WebAppContext, ContextHandlerCollection> map = findWebApps();

     if (map.isEmpty()) {
     System.out.println("==========  Command " + command + " Result  ==========");
     System.out.println(command + ": no handler found");
     return;
     }

     int i = 0;
     for (WebAppContext webapp : map.keySet()) {
     System.out.println((++i) + ". cp=" + webapp.getContextPath() + "; webDir=" + webapp.getWar());
     }
     System.out.println("===================================");
     }
     */
    protected String deployWar(String contextPath, String webDir) {
//        if (!isDevelopmentMode() && !isRuntimeDeploymentSupported()) {
        if (!isDevelopmentMode()) {
            return null;
        }

        System.out.println("deploywar started for cp=" + contextPath + "; webDir=" + webDir);

        Handler[] contextHandlers = getServer().getChildHandlersByClass(ContextHandlerCollection.class);
        if (contextHandlers == null || contextHandlers.length == 0) {
            return null;// null;
        }
        WebAppContext webapp = findWebAppContext(contextPath);
        if (webapp != null) {
            System.out.println("deploy war: there is a handler with the same contextPath=" + webapp.getContextPath() + " (webDir=" + webapp.getWar());
            return webapp.getContextPath();// null;
        }
        webapp = new WebAppContext();
        String path = webDir; // to deploy

        webapp.setContextPath(contextPath);
        webapp.setWar(path);

        ((ContextHandlerCollection) contextHandlers[0]).addHandler(webapp);

        System.out.println("deploywar: success");
        start(contextPath, webDir);
        return webapp.getContextPath();// webapp;
    }

    protected String deployWar(HttpServletRequest request) {
//        if (!isDevelopmentMode() && !isRuntimeDeploymentSupported()) {
        if (!isDevelopmentMode()) {
            return null;
        }

        String contextPath = request.getParameter("cp");
        String webDir = request.getParameter("dir");
        webDir = new File(webDir).getAbsolutePath();

//        return 
        return deployWar(contextPath, webDir);
    }

    protected Handler undeployWar(HttpServletRequest request) {
        String webDir = request.getParameter("dir");
        webDir = new File(webDir).getAbsolutePath();

        String contextPath = request.getParameter("cp");
        return undeployWar(contextPath, webDir);
    }

    protected Handler undeployWar(String contextPath, String webDir) {
        System.out.println("undeploywar started for cp=" + contextPath + "; webDir=" + webDir);

        Map<WebAppContext, ContextHandlerCollection> map = findWebApps();

        //Handler[] handlers = helper.getContextHandlers().getChildHandlersByClass(WebAppContext.class);
        Handler result = null;
        if (map.isEmpty()) {
            return null;
        }
        WebAppContext c = findWebAppContext(contextPath);
        if (c == null) {
            return null;
        }

        try {
            if (!c.isStopped()) {
                c.stop();
            }
            System.out.println("undeploywar remove handler ");
            map.get(c).removeHandler(c);
            result = c;
        } catch (Exception ex) {
            Logger.getLogger(NbDeployHandler.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("undeploywar failed: " + ex.getMessage());
            return result;
        }

        System.out.println("undeploywar: success");

//        printInfoAfter("undeploywar");
        return result;
    }

    protected void deploy(HttpServletRequest request) {

//        if (!isDevelopmentMode() && !isRuntimeDeploymentSupported()) {
        if (!isDevelopmentMode()) {
            return;
        }

        String contextPath = request.getParameter("cp");
        String webDir = request.getParameter("dir");
        webDir = new File(webDir).getAbsolutePath();

        String projectType = request.getParameter("projtype");
        System.out.println("PROJECT TYPE=" + projectType);
        if (Utils.DEPLOY_HTML5_PROJECTTYPE.equals(projectType)) {
            //return 
            deployHtml5(contextPath, webDir);
        } else {
            //return 
            deploy(contextPath, webDir);
        }
    }

    /**
     * {@literal Deployment mode} only.
     *
     * @param contextPath
     * @param webDir
     */
    protected void deploy(String contextPath, String webDir) {
        System.out.println("deploy started for cp=" + contextPath + "; webDir=" + webDir);

        Handler[] contextHandlers = getServer().getChildHandlersByClass(ContextHandlerCollection.class);
        if (contextHandlers == null || contextHandlers.length == 0) {
            System.out.println("deploy: no handler found. deploy finished");
            return;
        }
        WebAppContext webapp = findWebAppContext(contextPath);
        //boolean undeployed = false;
        if (webapp != null) {
            System.out.println("deploy: there is a handler with the same contextPath="
                    + webapp.getContextPath() + " (webDir="
                    + webapp.getWar() + ". Try to undeploy");
            undeploy(contextPath, webDir);
        }
        //
        // webDir specifies a web project directory. We need 
        // webDir + "/build/web" for ant-based and ... for  maven-based
        //
        String path = Utils.getWarPath(webDir); // to deploy

        boolean antiResource = Utils.isWindows();
        Properties props = Utils.getContextPropertiesByBuildDir(path);
        String s = props.getProperty("copyWebDir");
        if (s != null) {
            antiResource = Boolean.parseBoolean(s);
        }
        //
        // Check if there is a WebAppContext among hard-coded ones
        //

        //webapp = explicitApps.get(contextPath);
        WebAppContext explicitWebapp = explicitApps.get(contextPath);

        if (explicitWebapp == null) {
            explicitWebapp = explicitDynApps.get(contextPath);
        }

        if (explicitWebapp != null) {
            webapp = explicitWebapp;
        } else {
            webapp = new WebAppContext();
        }

        if (props.getProperty(Utils.CONTEXTPATH_PROP) != null) {
            webapp.setContextPath(props.getProperty(Utils.CONTEXTPATH_PROP));
        } else {
            webapp.setContextPath(contextPath);
        }

        webapp.setWar(path);

        webapp.getInitParams().put("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");
        if (!webapp.isCopyWebDir()) {
            webapp.setCopyWebDir(antiResource);
        }

        ((ContextHandlerCollection) contextHandlers[0]).addHandler(webapp);

        System.out.println("deploy: success");

    }

    /**
     * {@literal Deployment mode} only.
     *
     * @param ctx
     */
    protected void deploy(WebAppContext ctx) {
        System.out.println("deploy(WebAppContext) started for cp=" + ctx.getContextPath() + "; webDir=" + ctx.getWar());

        Handler[] contextHandlers = getServer().getChildHandlersByClass(ContextHandlerCollection.class);
        if (contextHandlers == null || contextHandlers.length == 0) {
            System.out.println("deploy: no handler found. deploy finished");
            return;
        }
        String contextPath = ctx.getContextPath();

        WebAppContext webapp = findWebAppContext(contextPath);
        //boolean undeployed = false;
        if (webapp != null) {
            System.out.println("deploy(WebAppContext): there is a handler with the same contextPath="
                    + webapp.getContextPath() + " (webDir="
                    + webapp.getWar() + ". Try to undeploy");
            undeploy(webapp);
            //  undeployed = true;
        }

        String path = ctx.getWar();

        boolean antiResource = Utils.isWindows();
        Properties props = Utils.getContextPropertiesByBuildDir(path);
        String s = props.getProperty("copyWebDir");
        if (s != null) {
            antiResource = Boolean.parseBoolean(s);
        }
        //
        // Check whether a WebAppContext is hard-coded
        //

        WebAppContext explicitWebapp = explicitApps.get(contextPath);
        if (explicitWebapp == null) {
            explicitWebapp = explicitDynApps.get(contextPath);
        }

        if (explicitWebapp != null) {
            webapp = explicitWebapp;
        } else {
            webapp = new WebAppContext();
        }

        if (props.getProperty(Utils.CONTEXTPATH_PROP) != null) {
            webapp.setContextPath(props.getProperty(Utils.CONTEXTPATH_PROP));
        } else {
            webapp.setContextPath(contextPath);
        }

        webapp.setWar(path);

        webapp.getInitParams().put("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");
        if (!webapp.isCopyWebDir()) {
            webapp.setCopyWebDir(antiResource);
        }

        ((ContextHandlerCollection) contextHandlers[0]).addHandler(webapp);
        try {
            webapp.start();
        } catch (Exception ex) {
            System.err.println("deploy(WebAppContext) exception msg=" + ex.getMessage());
        }
        System.out.println("deploy(WebAppContext): success");

    }


    protected Handler deployHtml5(String contextPath, String webDir) {
        System.out.println("deploy html5 started for cp=" + contextPath + "; projectType=" + Utils.DEPLOY_HTML5_PROJECTTYPE + "; webDir=" + webDir);

        Handler[] contextHandlers = getServer().getChildHandlersByClass(ContextHandlerCollection.class);
        if (contextHandlers == null || contextHandlers.length == 0) {
            System.out.println("deploy: no handler found. deploy finished");
            return null;
        }
        WebAppContext webapp = findWebAppContext(contextPath);
        if (webapp != null) {
            System.out.println("deploy: there is a handler with the same contextPath=" + webapp.getContextPath() + " (webDir=" + webapp.getWar()
                    + ". Try to undeploy");
            undeploy(contextPath, webDir);
        }

        String path = Utils.getHtml5BuildDir(webDir); // to deploy

        System.out.println("PATH= " + path);

        webapp = explicitApps.get(contextPath);

        if (webapp == null) {
            webapp = new WebAppContext();
        }
        webapp.setContextPath(contextPath);
        webapp.setWar(path.replace("\\", "/"));

        webapp.getInitParams().put("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");

        ((ContextHandlerCollection) contextHandlers[0]).addHandler(webapp);
        //
        // It's for Html5 projects that we must start WebAppContext
        //
        start(contextPath, webDir);

        System.out.println("deploy: success");

        return webapp;
    }

    /**
     *
     * @param lc actual type is {@literal Server}.
     */
    @Override
    public void lifeCycleStarting(LifeCycle lc) {

        System.out.println("========== LIFECYCLE STARTING ========");
        init((Server) lc);
        if (isDevelopmentMode()) {
            Connector[] cs = ((Server) lc).getConnectors();
            for (Connector c : cs) {
                if (c instanceof ServerConnector) {
                    int programPort = ((ServerConnector) c).getPort();
                    
                    Properties props = Utils.loadInstanceProperties();
                    if ( props == null ) {
                        break;
                    }
                    String port = props.getProperty(HTTP_PORT_PROP);
                    if ( port == null ) {
                        break;
                    }
                    int realPort = Integer.parseInt(port);
                    
                    if ( realPort != programPort ) {
                        ((ServerConnector) c).setPort(realPort);
                        notifyInvalidPortNumber(programPort, realPort);
                    }
                    break;
                }
            }
        }
        Map<WebAppContext, ContextHandlerCollection> map = findWebApps();
        //
        // Scan all explicitly (hard coded) defined WebAppContexts 
        //
        for (WebAppContext webapp : map.keySet()) {

            String newPath;

            if (isDevelopmentMode()) {
                DevModePathResolver r = DevModePathResolver.getInstance(webapp);
                newPath = r.getPath();
            } else {
                //newPath = PathResolver.rtPathFor(webapp.getContextPath());
                String[] p = PathResolver.rtPathFor(webapp);
                newPath = p[1];
            }

            webapp.setWar(newPath);
            System.out.println("============= Explicitly Defined WebAppslifeCycleStarting() ======");
            System.out.println("   --- war = " + newPath);
            System.out.println("==================================================================");

            if (isDevelopmentMode()) {
                configWebapp(webapp);
            }

            explicitApps.put(webapp.getContextPath(), webapp);

            System.out.println("HardCoded: cp=" + webapp.getContextPath()
                    + "; path=" + newPath);
        }

        explicitApps.keySet().stream().filter((key) -> (explicitDynApps.containsKey(key))).forEach((key) -> {
            explicitDynApps.remove(key);
        });

        explicitDynApps.entrySet().stream().forEach((e) -> {
            findContextHandlerCollection((Server) lc).addHandler(e.getValue());
        });

        //
        // We must create and add handlers for web apps that are 
        // registered as webref, warref or are internal projects.
        //
        if (!isDevelopmentMode()) {

            URL url = getClass().getClassLoader().getResource(Utils.WEB_APPS_PACK);
            if (url != null) {
                deployRegisteredWebapps(url); //warref, webref and internal web apps
            } else {
                deployRegisteredWebapps();
            }
        }
    }
    
    protected void notifyInvalidPortNumber(int programPort, int realPort) {
        System.err.println("===================================");        
        System.err.println("   Programm defined port:    " + programPort);
        System.err.println("   NetBeans registered port: " + realPort);
        System.err.println("   ---------------------------");
        System.err.println("   Server starts on    port: " + realPort);
        System.err.println("===================================");        
        
    }
    protected void deployRegisteredWebapps() {
        System.out.println("============== Deploy Registered Web Applicatiobs ================");

        String webappsDir = Utils.WEBAPPS_DEFAULT_DIR_NAME;
        Properties serverProps = Utils.loadServerProperties(isDevelopmentMode());
        if (serverProps != null && serverProps.getProperty(Utils.WEBAPPS_DIR_PROP) != null) {
            webappsDir = serverProps.getProperty(Utils.WEBAPPS_DIR_PROP);
        }
        File dir = new File("./" + webappsDir);
        System.out.println("DIR: " + dir.getAbsolutePath());
        if (!dir.exists()) {
            return;
        }
        File[] fileList = dir.listFiles();
        for (File f : fileList) {
            System.out.println("fileList f: " + f.getAbsolutePath());

            String contextPath;
            Properties props = new Properties();
            if (f.isDirectory()) {
                System.out.println("CM: f.getName=" + f.getName());
                contextPath = "/" + f.getName();
                File propFile = new File(f.getAbsolutePath() + "/META-INF/context.properties");// + Utils.WEBAPP_CONFIG_FILE);
                if (propFile.exists()) {
                    //
                    // It's an Html5 project
                    // 
                    try (FileInputStream fis = new FileInputStream(propFile)) {
                        props.load(fis);
//                    } catch (FileNotFoundException ex) {
//                        Logger.getLogger(CommandManager.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(NbDeployHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    contextPath = props.getProperty(Utils.CONTEXTPATH_PROP);
                } else {
                    File jettyXmlFile = new File(f.getAbsolutePath() + "/WEB-INF/" + Utils.WEBAPP_CONFIG_FILE);
                    if (jettyXmlFile.exists()) {
                        props = Utils.getContextProperties(jettyXmlFile);
                        contextPath = props.getProperty(Utils.CONTEXTPATH_PROP);
                    }
                }
            } else if (f.getName().endsWith(".war")) {
                contextPath = PathResolver.getContextPathFromWar(f);
            } else {
                continue;
            }
            System.out.println("   --- found cp  = " + contextPath);
            System.out.println("   --- found appName = " + f.getName());
            System.out.println("================================================");

            System.out.println("CM: contextPath=" + contextPath);
            if (explicitApps.containsKey(contextPath)) {
                System.out.println("    ***** DEPLOY REJECTED cp = " + contextPath + "; appName=" + f.getName());
                System.out.println("          Already explicitly deployed");
                System.out.println("================================================");

                continue;
            }
            if (explicitDynApps.containsKey(contextPath)) {
                System.out.println("    ***** DEPLOY REJECTED cp = " + contextPath + "; appName=" + f.getName());
                System.out.println("          Already dynamicaly deployed");
                System.out.println("================================================");

                continue;
            }

            ContextHandlerCollection chc = getServer().getChildHandlerByClass(ContextHandlerCollection.class);
            WebAppContext webapp = new WebAppContext();
            webapp.setContextPath(contextPath);
            webapp.setWar(f.getAbsolutePath());

            chc.addHandler(webapp);
        }
    }

    /**
     * @param url
     */
    protected void deployRegisteredWebapps(URL url) {
        System.out.println("---- deployRegisteredWebapps--- url=" + url);

        ContextHandlerCollection chc = getServer().getChildHandlerByClass(ContextHandlerCollection.class);
        String jarPath = url.getPath().substring(5, url.getPath().indexOf("!"));

        System.out.println("============== Deploy Registered Web Applicatiobs ================");

        try {

            JarFile jarFile = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));

            Enumeration<JarEntry> entries = jarFile.entries();
            Set<JarEntry> apps = new HashSet<>(); // registered as .webref or .htmref, but not .warref
            Set<JarEntry> wars = new HashSet<>(); // registered as  .warref (may be without context config file) 
            while (entries.hasMoreElements()) {
                JarEntry e = entries.nextElement();
//                System.out.println("---- deployRegisteredWebapps--- entry.name=" + e.getName());
                if (e.getName().startsWith(Utils.WEB_APPS_PACK) && e.getName().endsWith("WEB-INF/" + Utils.WEBAPP_CONFIG_FILE)) {
//                    System.out.println("---- deployRegisteredWebapps--- ADDED entry.name=" + e.getName());
                    apps.add(e);
                } else if (e.getName().startsWith(Utils.WEB_APPS_PACK) && e.getName().endsWith("META-INF/context.properties")) {
                    //
                    // Html5 project
                    //
//                    System.out.println("---- deployRegisteredWebapps--- ADDED entry.name=" + e.getName());
                    apps.add(e);
                } else if (e.getName().startsWith(Utils.WEB_APPS_PACK)) {
                    String warName = e.getName().substring(Utils.WEB_APPS_PACK.length() + 1);
                    //
                    // check whether warName represents a folder or a .war file
                    // We do not consider folders which has no config file
                    // if no "/" character - the it's just an entry of 
                    // Utils.WEB_APPS_PACK
                    if (warName.contains("/") && warName.endsWith(".war")) {
                        wars.add(e);
                    }
                }
            }//while

            //
            // Now apps contains Jar Entries every of which corresponds 
            // to s jetty-web.xml (std web project) or context.properties fo
            // Html5 applications
            //
            for (JarEntry e : apps) {
                String appName = PathResolver.getAppNameByJarEntry(e.getName());
                final Properties props;
                try (InputStream is = getClass().getClassLoader().getResourceAsStream(e.getName())) {
                    if (e.getName().startsWith(Utils.WEB_APPS_PACK) && e.getName().endsWith("WEB-INF/" + Utils.WEBAPP_CONFIG_FILE)) {
                        props = Utils.getContextProperties(is);
                    } else {
                        props = new Properties();
                        props.load(is);
                    }
                }
                String contextPath = props.getProperty(Utils.CONTEXTPATH_PROP);

                System.out.println("   --- found cp  = " + contextPath);
                System.out.println("   --- found appName = " + appName);
                System.out.println("================================================");

                if (explicitApps.containsKey(contextPath)) {
                    System.out.println("    ***** DEPLOY REJECTED cp = " + contextPath + "; appName=" + appName);
                    System.out.println("          Already explicitly deployed");
                    System.out.println("================================================");

                    continue;
                }
                WebAppContext webapp = new WebAppContext();
                webapp.setContextPath(contextPath);

                webapp.setWar(getClass().getClassLoader().getResource(Utils.WEB_APPS_PACK + "/" + appName + "/").toExternalForm());
                System.out.println("   --- new cp  = " + contextPath);
                System.out.println("   --- new war = " + webapp.getWar());
                System.out.println("================================================");

//                applyJsfSupport(webapp);
                chc.addHandler(webapp);

            }

            //
            // We must treat .warref separatly as they may not contain 
            // context config file or even contains the one for another server
            //
            for (JarEntry e : wars) {
                String appName = e.getName().substring(Utils.WEB_APPS_PACK.length() + 1);
                String contextPath = appName;
                if (explicitApps.containsKey(contextPath)) {
                    continue;
                }
                if (explicitDynApps.containsKey(contextPath)) {
                    continue;
                }

                WebAppContext webapp = new WebAppContext();
                webapp.setContextPath(contextPath);

                webapp.setWar(getClass().getClassLoader().getResource(Utils.WEB_APPS_PACK + "/" + appName).toExternalForm());

                chc.addHandler(webapp);

            }

        } catch (IOException ex) {
            Logger.getLogger(NbDeployHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected void addServletContextListener(WebAppContext webapp, String className) {

        EventListener[] listeners = webapp.getEventListeners();
        //System.out.println("!!! *** addSevletContextListener " + listeners + ";  length=" + listeners.length);
/*        if (listeners != null && listeners.length > 0) {
         for (EventListener l : listeners) {
         System.out.println("*** addSevletContextListener " + l.getClass().getName());
         }
         }
         */
        boolean found = false;
        if (listeners != null && listeners.length > 0) {
            for (EventListener l : listeners) {
                System.out.println("l=" + l.getClass().getName());
                if (className.equals(l.getClass().getName())) {
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            webapp.getServletContext().addListener(className);
        }
    }
    /*
     protected void applyJsfSupport(final WebAppContext webapp) {
     if (jsfSupported || weldSupported) {
     webapp.addLifeCycleListener(new AbstractLifeCycle.AbstractLifeCycleListener() {
     @Override
     public void lifeCycleStarting(LifeCycle ev) {

     if (jsfSupported) {
     EnumSet<DispatcherType> es = EnumSet.of(DispatcherType.REQUEST);
     webapp.addFilter(JsfFilter.class, "/", es);
     //webapp.getServletContext().
     addServletContextListener(webapp, "com.sun.faces.config.ConfigureListener");
     }
     if (weldSupported) {
     addServletContextListener(webapp, "org.jboss.weld.environment.servlet.BeanManagerResourceBindingListener");
     addServletContextListener(webapp, "org.jboss.weld.environment.servlet.Listener");
     }
     }

     @Override
     public void lifeCycleStarted(LifeCycle ev) {
     webapp.removeLifeCycleListener(this);
     }
     });
     }
     }
     */

    /**
     * Only in {@code development mode}
     *
     * @param webapp
     */
    protected void configWebapp(WebAppContext webapp) {

        boolean antiResource = Utils.isWindows();

        Properties props = Utils.getContextPropertiesByBuildDir(webapp.getWar());

        if (props == null) {
            // May be Html5 project 
            webapp.getInitParams().put("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");
            return;
        }
        String s = props.getProperty(Utils.ANTI_LOCK_PROP_NAME);
        if (s != null) {
            antiResource = Boolean.parseBoolean(s);
        }

        if (antiResource && Utils.isBuildOfMavenProject(webapp.getWar())) {
            //webapp.addLifeCycleListener(new MavenContextLifeCycleListener());
        }
        webapp.getInitParams().put("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");
        if (!Utils.isHtml5Project(webapp.getWar())) {
            webapp.setCopyWebDir(antiResource);
        }

    }

    @Override
    public void lifeCycleStarted(LifeCycle lc) {
        /*        String dir = System.getProperty("user.dir");
         Path p = Paths.get(dir,"nb-server-instance-started");
         if ( Files.exists(p) ) {
         return;
         }
         try {
         Files.createFile(p);
         } catch (IOException ex) {
         Logger.getLogger(NbDeployHandler.class.getName()).log(Level.SEVERE, null, ex);
         }
         */
        lc.removeLifeCycleListener(this);
//        System.out.println("==============  LIFECYCLE STARTED ================");

    }

    @Override
    public void lifeCycleFailure(LifeCycle lc, Throwable thrwbl) {
        System.out.println("==============  LIFECYCLE FAILURE ================");
    }

    @Override
    public void lifeCycleStopping(LifeCycle lc) {
    }

    @Override
    public void lifeCycleStopped(LifeCycle lc) {
        /*        String dir = System.getProperty("user.dir");
         Path p = Paths.get(dir,"nb-server-instance-started");
         if ( ! Files.exists(p) ) {
         return;
         }
         try {
         Files.delete(p);
         } catch (IOException ex) {
         Logger.getLogger(NbDeployHandler.class.getName()).log(Level.SEVERE, null, ex);
         }
         */
    }

}//class
