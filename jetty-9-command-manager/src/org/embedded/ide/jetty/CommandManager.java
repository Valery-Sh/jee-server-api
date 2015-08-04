package org.embedded.ide.jetty;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.EnumSet;
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
import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.AbstractHandlerContainer;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ShutdownHandler;
import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.webapp.WebAppContext;
import static org.embedded.ide.jetty.Utils.isBuildOfMavenProject;

/**
 *
 * @author V. Shyshkin
 */
public class CommandManager extends AbstractHandler implements LifeCycle.Listener {

    private Server server;

    private final String shutdownKey = "netbeans";
    protected Map<String, WebAppContext> explicitApps = new HashMap<>();
    protected Map<String, WebAppContext> explicitDynApps = new HashMap<>();

    private static CommandManager commandManager = null;

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

    protected CommandManager() {
        super();
        annotationsSupported = false;
    }

    public static CommandManager getInstance() {
        return commandManager;
    }

    public static CommandManager start(Server server) {
        if (commandManager != null) {
            return commandManager;
        }
        commandManager = new CommandManager();
        commandManager.server = server;
        //initHandlers(server);
        server.addLifeCycleListener(commandManager);
        return commandManager;
    }

    public static Server createHotDeploymentServer() {
        HotDeployer hd = HotDeployer.create();
        CommandManager.start(hd.getServer());
        hd.setCommandManager(CommandManager.getInstance());
        //hd.getHandlers().addHandler(hd.getCommandManager());

        return hd.getServer();
    }

    /**
     *
     * @return null if the method {@link createHotDeploymentServer}
     */
    public static HotDeployer getHotDeployer() {
        CommandManager cm = CommandManager.getInstance();
        assert cm != null;
        return HotDeployer.create();

    }

    /*protected Server getServer() {
     return server;
     }
     */
    public static int getHttpPort() {
        Properties props = Utils.loadServerProperties(isDevelopmentMode());
        return Integer.parseInt(props.getProperty(Utils.HTTP_PORT_PROP));
    }

    protected void tryLoadFacesServlet() {
        jsfSupported = true;
        try {
            getClass().getClassLoader().loadClass("javax.faces.webapp.FacesServlet");
            getClass().getClassLoader().loadClass("com.sun.faces.config.ConfigureListener");
        } catch (ClassNotFoundException ex) {
            jsfSupported = false;
        }
    }
    /*
     protected void tryLoadWeldListener() {
     weldSupported = true;
     try {
     getClass().getClassLoader().loadClass("org.jboss.weld.environment.servlet.Listener");
     } catch (ClassNotFoundException ex) {
     weldSupported = false;
     }
     }
     */

/*    public boolean isAnnotationsSupported() {
        //System.out.println("anotat: " + annotationsSupported + "; jsf=" + jsfSupported + "; weld=" + weldSupported);
        return annotationsSupported || jsfSupported || weldSupported;
    }
*/
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
            "org.eclipse.jetty.webapp.JettyWebXmlConfiguration" }
        );
    }

/*    public CommandManager annotations(boolean supported) {
        this.annotationsSupported = supported;
        return this;
    }
*/    
    /*
     protected static void initHandlers(Server server) {
     if (server.getHandler() == null) {
     HandlerCollection hc = new HandlerCollection();
     ContextHandlerCollection chc = new ContextHandlerCollection();
     hc.addHandler(chc);
     server.setHandler(hc);
     } else if (server.getHandler() instanceof HandlerCollection) {
     ContextHandlerCollection chc = server.getChildHandlerByClass(ContextHandlerCollection.class);
     if (chc == null) {
     System.out.println("NNNNNNNNNNNNNNNNNNNN initHandlers(server)");
                
     chc = new ContextHandlerCollection();
     ((HandlerCollection) server.getHandler()).addHandler(chc);
     }
     } else {
     Handler h = server.getHandler();
     HandlerCollection hc = new HandlerCollection();
     server.setHandler(hc);
     hc.addHandler(h);
     ContextHandlerCollection chc = new ContextHandlerCollection();
     hc.addHandler(chc);
     }

     }
     */

    protected final void init(Server server) {
        System.out.println("init(server)");
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

    protected boolean isRuntimeDeploymentSupported() {
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

    public static boolean isDevelopmentMode() {
        return new File("./" + Utils.SERVER_PROJECT_XML_FILE).exists();
    }

    protected void addShutdownHandler(Server server, HandlerCollection to) {

        if (!isDevelopmentMode() && getRuntimeShutdownToken() == null) {
            return;
        }
        String token = shutdownKey;
        if (!isDevelopmentMode()) {
            token = getRuntimeShutdownToken();
        }
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
        baseRequest.setHandled(true);

        response.getWriter().println(text);
    }

    protected void redeploy(HttpServletRequest request) {
        if (!isDevelopmentMode() && !isRuntimeDeploymentSupported()) {
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
            Logger.getLogger(CommandManager.class.getName()).log(Level.SEVERE, null, ex);
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

        printInfoAfter("undeploy");
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

        printInfoAfter("undeploy");
        return c;
    }

    protected void start(HttpServletRequest request) {
        if (!isDevelopmentMode() && !isRuntimeDeploymentSupported()) {
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
        if (!isDevelopmentMode() && !isRuntimeDeploymentSupported()) {
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

    protected void printInfoBefore(String command) {
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

    protected String deployWar(String contextPath, String webDir) {
        if (!isDevelopmentMode() && !isRuntimeDeploymentSupported()) {
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
        if (!isDevelopmentMode() && !isRuntimeDeploymentSupported()) {
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
            Logger.getLogger(CommandManager.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("undeploywar failed: " + ex.getMessage());
            return result;
        }

        System.out.println("undeploywar: success");

        printInfoAfter("undeploywar");
        return result;
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
            //  undeployed = true;
        }
        //
        // webDir specifies a web project directory. We need 
        // webDir + "/build/web" for ant-based and ... for  maven-based
        //
        String path = getWarPath(webDir); // to deploy

        System.out.println("deploy: PATH=" + path);

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
        System.out.println("deploy: !!!!!!!!!!!!!  cp=" + contextPath);

        if (explicitWebapp == null) {

            explicitWebapp = explicitDynApps.get(contextPath);
            System.out.println("deploy  explicitDynApps=" + explicitWebapp);
        }

//        if (webapp == null ) {
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
            System.out.println("****** NOT COPY");
            webapp.setCopyWebDir(antiResource);
        }

        applyJsfSupport(webapp);

        ((ContextHandlerCollection) contextHandlers[0]).addHandler(webapp);

        System.out.println("deploy: success");

        System.out.println(new Reporter().buildTextInfo());
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
        System.out.println("deploy(WebAppContext) started for cp=" + ctx.getContextPath() + "; path=" + path);
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
            System.out.println("****** NOT COPY");
            webapp.setCopyWebDir(antiResource);
        }

        applyJsfSupport(webapp);

        ((ContextHandlerCollection) contextHandlers[0]).addHandler(webapp);
        try {
            webapp.start();
        } catch (Exception ex) {
            System.err.println("deploy(WebAppContext) exception msg=" + ex.getMessage());
        }
        System.out.println("deploy(WebAppContext): success");

        System.out.println(new Reporter().buildTextInfo());
    }

    public static void addWebApp(WebAppContext wc) {
        CommandManager cm = CommandManager.getInstance(); // Now  handlers are created
        if (wc == null || wc.getContextPath() == null) {
            return;
        }
        Handler[] contextHandlers = cm.getServer().getChildHandlersByClass(ContextHandlerCollection.class);
        if (contextHandlers == null || contextHandlers.length == 0) {
            System.out.println("addWebApp: no handler found. addWebApp finished");
            return;// null;
        }
        WebAppContext webapp = cm.findWebAppContext(wc.getContextPath());
        if (webapp != null) {
            System.out.println("addWebApp: there is a handler with the same contextPath=" + webapp.getContextPath() + " (webDir=" + webapp.getWar());
            return;// null;
        }

        ((ContextHandlerCollection) contextHandlers[0]).addHandler(webapp);

    }

    public static void addWebApp(String warDir, String contextPath) {
        CommandManager cm = CommandManager.getInstance(); // Now  handlers are created
        if (warDir == null || contextPath == null) {
            return;
        }
        Handler[] contextHandlers = cm.getServer().getChildHandlersByClass(ContextHandlerCollection.class);
        if (contextHandlers == null || contextHandlers.length == 0) {
            System.out.println("addWebApp: no handler found. addWebApp finished");
            return;// null;
        }
        WebAppContext webapp = cm.findWebAppContext(contextPath);
        if (webapp != null) {
            System.out.println("addWebApp: there is a handler with the same contextPath=" + webapp.getContextPath() + " (webDir=" + webapp.getWar());
            return;// null;
        }
        webapp = new WebAppContext(warDir, contextPath);
        ((ContextHandlerCollection) contextHandlers[0]).addHandler(webapp);
    }

    /**
     * {@literal Not deployment mode} only.
     *
     * @param contextPath
     * @param webDir
     * @return
     */
    protected Handler runtimeDeploy(String contextPath, String webDir) {
        System.out.println("runtime deploy started for cp=" + contextPath + "; webDir=" + webDir);

        Handler[] contextHandlers = getServer().getChildHandlersByClass(ContextHandlerCollection.class);
        if (contextHandlers == null || contextHandlers.length == 0) {
            return null;
        }
        WebAppContext webapp = findWebAppContext(contextPath);
        if (webapp != null) {
            System.out.println("runtime deploy: there is a handler with the same contextPath=" + webapp.getContextPath() + " (webDir=" + webapp.getWar());
            return null;
        }

        String path = webDir; // to deploy

        WebAppContext c = findWebApps(path);
        if (c != null) {
            System.out.println("deploy: there is a handler with the same webDir=" + webDir + ". Execute undeploy");
            undeploy(contextPath, webDir);
        }

        webapp = explicitApps.get(contextPath);
        if (webapp == null) {
            webapp = new WebAppContext();
        }
        webapp.setContextPath(contextPath);
        webapp.setWar(path);

        webapp.getInitParams().put("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");

        ((ContextHandlerCollection) contextHandlers[0]).addHandler(webapp);

        System.out.println("runtime deploy: success");

        System.out.println(new Reporter().buildTextInfo());
        return webapp;
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

    protected String getWarPath(String webDir) {
        String path = new File(webDir).getAbsolutePath();

        File file = new File(path + "/build/web");

        if (file.exists()) {
            path = file.getAbsolutePath();
        } else {
            path = Utils.getMavenBuildDir(path);
        }
        return path;
    }

    public static void addContext(WebAppContext dynctx) {

        CommandManager cm = getInstance();
        String newPath;
        String newContextPath = dynctx.getContextPath();

        if (isDevelopmentMode()) {
            String[] p = PathResolver.dtPathFor(dynctx.getWar());
            newPath = p[1];
            newPath = newPath.replace("\\", "/");
            newPath = newPath.replace("/./", "/");
            if (p[0] != null) {
                newContextPath = p[0];
            }
        } else {
            String[] p = PathResolver.rtPathFor(dynctx);
            newContextPath = p[0];
            newPath = p[1];
        }

        dynctx.setWar(newPath);
        dynctx.setContextPath(newContextPath);

        if (isDevelopmentMode()) {
            cm.configWebapp(dynctx);
        }

        WebAppContext oldctx = cm.explicitDynApps.get(dynctx.getContextPath());
        if (oldctx != null && cm.server.isStarted()) {
            cm.undeploy(oldctx);
        }
        cm.explicitDynApps.put(dynctx.getContextPath(), dynctx);
        if (cm.server.isStarted()) {
            cm.deploy(dynctx);
        }

    }

    /**
     *
     * @param lc actual type is {@literal Server}.
     */
    @Override
    public void lifeCycleStarting(LifeCycle lc) {
        //tryLoadFacesServlet();
        //tryLoadWeldListener();

        System.out.println("========== LIFECYCLE STARTING ========");
        /*        if (isAnnotationsSupported()) {
         Configuration.ClassList classList = Configuration.ClassList.setServerDefault((Server) lc);
         if (classList != null) {
         if (!classList.contains("org.eclipse.jetty.annotations.AnnotationConfiguration")) {
         classList.addAfter("org.eclipse.jetty.webapp.FragmentConfiguration", "org.eclipse.jetty.plus.webapp.EnvConfiguration", "org.eclipse.jetty.plus.webapp.PlusConfiguration");
         classList.addBefore("org.eclipse.jetty.webapp.JettyWebXmlConfiguration", "org.eclipse.jetty.annotations.AnnotationConfiguration");
         }
         }
         }
         */
        init((Server) lc);

        Map<WebAppContext, ContextHandlerCollection> map = findWebApps();
        //
        // Scan all explicitly (hard coded) defined WebAppContexts 
        //
        for (WebAppContext webapp : map.keySet()) {
            System.out.println("LIFE: WAR=" + webapp.getWar());
            // 20.09String newPath = pr.pathFor(webapp.getWar(), isDevelopmentMode());
            String newPath;
            String newContextPath = webapp.getContextPath();

            if (isDevelopmentMode()) {
                String[] p = PathResolver.dtPathFor(webapp.getWar());
                newPath = p[1];
                newPath = newPath.replace("\\", "/");
                newPath = newPath.replace("/./", "/");
                if (p[0] != null) {
                    newContextPath = p[0];
                }

            } else {
                //newPath = PathResolver.rtPathFor(webapp.getContextPath());
                String[] p = PathResolver.rtPathFor(webapp);
                newContextPath = p[0];
                newPath = p[1];
            }

            webapp.setWar(newPath);
            webapp.setContextPath(newContextPath);
            System.out.println("============= Explicitly Defined WebAppslifeCycleStarting() ======");
            System.out.println("   --- cp  = " + newContextPath);
            System.out.println("   --- war = " + newPath);
            System.out.println("==================================================================");
//            System.out.println("CommandManager.lifeCycleStarting() newContextpath=" + newContextPath);
//            System.out.println("CommandManager.lifeCycleStarting() newPath=" + newPath);

            if (isDevelopmentMode()) {
                configWebapp(webapp);
            }
            //
            // adds JsfFilter and ServletContextListener of class
            // 
            //applyJsfSupport(webapp);  //1.12
            //applyWeldSupport(webapp);  //4.12

            explicitApps.put(webapp.getContextPath(), webapp);

            System.out.println("HardCoded: cp=" + webapp.getContextPath()
                    + "; path=" + newPath);
        }

        for (String key : explicitApps.keySet()) {
            if (explicitDynApps.containsKey(key)) {
                explicitDynApps.remove(key);
            }
        }

        for (Map.Entry<String, WebAppContext> e : explicitDynApps.entrySet()) {
            findContextHandlerCollection(server).addHandler(e.getValue());
        }

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

            /*            Set<WebAppContext> set = findWebApps1();
             for (WebAppContext webapp : set) {
             System.out.println("----------------- END of LIFECYCLE ---------");
             System.out.println("    --- cp = " + webapp.getContextPath());
             System.out.println("    --- war = " + webapp.getWar());
             System.out.println("-----------------------------------------");
             }
             */
        }
    }

    protected void deployRegisteredWebapps() {
        System.out.println("------- Deploy registered web apps -------");

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
                        Logger.getLogger(CommandManager.class.getName()).log(Level.SEVERE, null, ex);
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
            applyJsfSupport(webapp);
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

                applyJsfSupport(webapp);
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
            Logger.getLogger(CommandManager.class.getName()).log(Level.SEVERE, null, ex);
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

    protected void applyJsfSupport(final WebAppContext webapp) {
        if (jsfSupported || weldSupported) {
            webapp.addLifeCycleListener(new AbstractLifeCycleListener() {
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
        lc.removeLifeCycleListener(this);
        System.out.println("==============  LIFECYCLE STARTED ================");

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
    }

    /*    public static class MavenContextLifeCycleListener implements LifeCycle.Listener {

     public MavenContextLifeCycleListener() {
     }

     @Override
     public void lifeCycleStarting(LifeCycle lc) {

     }

     @Override
     public void lifeCycleStarted(LifeCycle lc) {
     WebAppContext webapp = (WebAppContext) lc;
     try {
     System.out.println("======== MavenApp=" + webapp.getWar() + " LIFECYCLE started ==============");
     System.out.println("MavenApp LIFECYCLE lc.class=" + lc.getClass().getName());
     System.out.println("MavenApp LIFECYCLE lc.isRunning()=" + lc.isRunning());
     System.out.println("MavenApp LIFECYCLE lc.isStarted()=" + lc.isStarted());
     System.out.println("MavenApp LIFECYCLE lc.isStarting()=" + lc.isStarting());
     //File f = new File(webapp.getWar()).getParentFile();
     //f = new File(f.getAbsolutePath() + "/classes");
     //webapp.setResourceBase(f.getAbsolutePath());
     if (Utils.isBuildOfMavenProject(webapp.getWar())) {
     copyChangedClasses(webapp);
     }
     } catch (Exception ex) {
     System.out.println("MavenApp LIFECYCLE starting EXCEPTION " + ex.getMessage());
     Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
     }

     }

     @Override
     public void lifeCycleFailure(LifeCycle lc, Throwable thrwbl) {
     }

     @Override
     public void lifeCycleStopping(LifeCycle lc) {
     }

     @Override
     public void lifeCycleStopped(LifeCycle lc) {
     }

     public void copyChangedClasses(WebAppContext webapp) throws IOException {

     System.out.println("MAVEN: copyChangedClasses webapp.isCopyWebDir()=" + webapp.isCopyWebDir());
     System.out.println("MAVEN: copyChangedClasses webapp.getTempDirectory()=" + webapp.getTempDirectory());
     System.out.println("MAVEN: copyChangedClasses webapp.getWebInf()=" + webapp.getWebInf());
     //22.o6 if (!webapp.isCopyWebDir() || webapp.getTempDirectory() == null || webapp.getWebInf() == null || !webapp.getWebInf().exists()) {
     if (!webapp.isCopyWebInf() || webapp.getTempDirectory() == null || webapp.getWebInf() == null || !webapp.getWebInf().exists()) {
     return;
     }
     File to = new File(webapp.getWebInf().getFile().getAbsoluteFile() + "/classes");
     //File to = new File(webapp.getWebInf().getFile().getAbsoluteFile() + "/classes");

     File from = new File(webapp.getWar());

     from = new File(from.getParent() + "/classes");
     System.out.println("FROM MAVEN: copyChangedClasses from=" + from.getAbsolutePath());
     System.out.println("TO MAVEN: copyChangedClasses to=" + to.getAbsolutePath());
     IO.copyDir(from, to);
     }

     }//class
     */
    public class Reporter {

        public String buildTextInfo() {
            if (!isDevelopmentMode()) {
                return "";
            }
            StringBuilder sb = new StringBuilder();
            //Registered in the Web Applications folder
            // key -> project name
            // value -> project dir or .webref || .warref properties file
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
            File file;
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
                String reg = registered.containsKey(appName) ? ". Registered as '.webref' || '.warref'" : ". Not registered as '.webref' || '.warref' || '.htmref'";
                String dep = deployed.containsKey(appName) ? ". Deployed" : ". Not Deployed";
                File appDir = e.getValue();
                Properties props = Utils.getContextPropertiesByBuildDir(appDir.getAbsolutePath());
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
                String reg = registered.containsKey(appName) ? ". Registered as '.webref' || '.warref' || '.htmref'"
                        : ". Not registered as '.webref' || '.warref' || '.htmref'";
                String dep = ". Deployed";
                String contextPath;
                String based;
                Properties props;

                File appDir = e.getValue();
                if (appDir.getAbsolutePath().endsWith(".war")) {
                    based = "War archive";
                    String nm = appDir.getName();
                    contextPath = "/" + nm.substring(0, nm.length() - 4);

                } else {
                    props = Utils.getContextPropertiesByBuildDir(appDir.getAbsolutePath());

                    contextPath = props.getProperty("contextPath");
                    based = " Html5 project";
                    if (isAntWebProject(appDir.getAbsolutePath())) {
                        based = "Ant-based";
                    } else if (Utils.isBuildOfMavenProject(appDir.getAbsolutePath())) {
                        based = " Maven-based";
                    }
                }
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
            for (Map.Entry<String, WebAppContext> e : explicitApps.entrySet()) {
                File projDir = new File(e.getValue().getWar());
                String projName = getProjectNameByWarPath(e.getValue().getWar());
                map.put(projName, projDir);
            }
            return map;
        }

        protected Map<String, File> getDeployedApps() {
            Map<String, File> map = new HashMap<>();
            Handler[] handlers = getServer().getChildHandlersByClass(WebAppContext.class);
            for (Handler h : handlers) {
                WebAppContext app = (WebAppContext) h;
                File projDir = new File(app.getWar());
                System.out.println("DEPLOYED PROJECT DIR=" + projDir.getPath());
                String projName = getProjectNameByWarPath(app.getWar());
                System.out.println("DEPLOYED PROJECT NAME=" + projName);
                map.put(projName, projDir);
            }
            return map;
        }

        public String getProjectNameByWarPath(String path) {
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
