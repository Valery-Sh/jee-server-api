/**
 * This file is part of Jetty Server suppport in NetBeans IDE.
 *
 * Jetty Server suppport in NetBeans IDE is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the License,
 * or (at your option) any later version.
 *
 * Jetty Server suppport in NetBeans IDE is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should see the GNU General Public License here:
 * <http://www.gnu.org/licenses/>.
 */
package org.jetty.custom.handlers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.deploy.AppLifeCycle;
import org.eclipse.jetty.deploy.DeploymentManager;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
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
public class CommandManager extends AbstractHandler implements LifeCycle.Listener {

    private static String WELD_INIT_PARAMETER = "org.jboss.weld.environment.container.class";
    
    
    /**
     * When {@literal true} the the server supports annotations.
     */
    protected boolean annotationsSupported;
    /**
     * When {@literal true} then the server supports jsf.
     */
    //protected boolean jsfSupported;
    /**
     * When {@literal true} then the server supports Weld.
     */
    //protected boolean weldSupported;

    /**
     * When {@literal true} then the server supports jsf.
     */
    //protected boolean jsfActivated;
    /**
     * When {@literal true} then the server supports Weld.
     */
    protected boolean weldActivated;

    //private File jettyBase;
    private String messageOption = "NO";

    private static CommandManager commandManager;

    protected CommandManager(String msgOption) {
        this();

        switch (msgOption.toUpperCase()) {
            case "NO":
            case "FALSE":
                break;
            default:
                messageOption = "YES";
        }

        System.out.println("NB-DEPLOYER: CommandManager set verbouse=" + msgOption);
    }

    protected CommandManager() {
        super();

/*        String path = new File(".").getAbsolutePath();
        if (path.endsWith("/.")) {
            path = path.substring(0, path.indexOf("/."));
        } else if (path.endsWith("\\.")) {
            path = path.substring(0, path.indexOf("\\."));
        }
        jettyBase = new File(path);
*/        
//        jsfActivated = new File(jettyBase + "/start.d/jsf.ini").exists();
        init();
    }

    public static CommandManager getInstance() {
        if (commandManager == null) {
            commandManager = new CommandManager();
        }
        return commandManager;
    }
    public static CommandManager getInstance(String msgOption) {
        if (commandManager == null) {
            commandManager = new CommandManager();
        }
        commandManager.messageOption = msgOption;
        return commandManager;
    }
    
    public static boolean isCDIEnabled(ContextHandler ctx) {
        return ctx.getInitParameter(WELD_INIT_PARAMETER) != null;
    }   
    
    public static boolean isCDIEnabled() {
        
        boolean result = false;
        
        Collection<DeploymentManager> dms = commandManager.getServer().getBeans(DeploymentManager.class);
        DeploymentManager dm = null;
        int i = 0;
        if (dms != null && !dms.isEmpty()) {
            for (DeploymentManager m : dms) {
                dm = m;
                i++;
            }
        }
        if (dm != null) {
            for (AppLifeCycle.Binding b : dm.getLifeCycleBindings()) {
                if ( "org.eclipse.jetty.cdi.servlet.WeldDeploymentBinding".equals(b.getClass().getName()) ) {
                    result = true;
                    break;
                }
            }
        }
        
        return result;
    }
    public static boolean isJSFEnabled() {
        return IniModules.isJSFEnabled();
    }
    /**
     *
     */
    private void init() {
        if ( commandManager == null ) {
            commandManager = this;
        }
        addLifeCycleListener(new ManagerLifeCycleListener(this));
    }

    public String getMessageOption() {
        return messageOption;
    }

    public void out(String msg, boolean... always) {
        if (always.length > 0) {
            System.out.println(msg);
            return;
        }
        String opt = messageOption.toUpperCase();
        switch (opt) {
            case "NO":
            case "FALSE":
                break;
            default:
                System.out.println(msg);
        }
    }

    public void error(String msg, boolean... always) {
        if (always.length > 0) {
            System.err.println(msg);
            return;
        }
        String opt = messageOption.toUpperCase();
        switch (opt) {
            case "NO":
            case "FALSE":
                break;
            default:
                System.err.println(msg);
        }

    }

    public void error(String msg, Exception ex) {
        Logger.getLogger(CommandManager.class.getName()).log(Level.SEVERE, null, ex);
        System.err.println(msg + ". Exception.message=" + ex.getMessage());
    }

//Logger.getLogger(CommandManager.class.getName()).log(Level.SEVERE, null, ex);    
    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {// throws IOException, ServletException {

        response.setContentType("text/html;charset=utf-8");

        if (!"/jeeserver/manager".equals(target)) {
            return;
        }
        String cp = request.getParameter("cp");
        String cmd = request.getParameter("cmd");

        out("NB-DEPLOYER: handle: " + target + "; cp=" + cp + "cmd=" + cmd);

        String text = "";

        if (cmd != null) {
            switch (cmd) {
                case "deploy":
                    deploy(request);
                    break;
                case "getcopydir":
                    text = getCopyDir(request);
                    break;
                case "getstate":
                    text = getLifecycleState(request);
                    break;
                case "getstatebycontextpath":
                    text = getLifecycleStateByContextPath(request);
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
                case "starthotdeployed":
                    startHotDeployed(request);
                    break;
                case "stophotdeployed":
                    //stopHotDeployed(request);
                    shutdownHotDeployed(request);
                    break;
                /*                case "undeployhotdeployed":
                 undeployHotDeployed(request);
                 break;
                 */

            }//switch
        }
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
        if (text == null || text.isEmpty()) {
            text = "<h1>Hello, from CommandManager</h1>";
        }
        response.getWriter().println(text);
    }

    @Override
    public void lifeCycleStarting(LifeCycle lc) {
    }

    @Override
    public void lifeCycleStarted(LifeCycle lc) {

    }

    @Override
    public void lifeCycleFailure(LifeCycle lc, Throwable thrwbl) {
    }

    @Override
    public void lifeCycleStopping(LifeCycle lc
    ) {
    }

    @Override
    public void lifeCycleStopped(LifeCycle lc) {
    }

    protected void redeploy(HttpServletRequest request) {
        out("NB-DEPLOYER: redeploy: starting");
        Map<WebAppContext, ContextHandlerCollection> map = findWebApps();

        if (map.isEmpty()) {
            out("NB-DEPLOYER: redeploy: no handler found. redeploy finished.");
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

        out("NB-DEPLOYER: redeploy started. Old web app: for oldcp=" + oldContextPath + "; oldWebDir=" + oldWebDir);
        out("NB-DEPLOYER: redeploy started. New web app: for cp=" + contextPath + "; webDir=" + webDir);

        WebAppContext webapp = findWebAppContext(oldContextPath);
        if (webapp != null) {
            out("NB-DEPLOYER: redeploy undeploy old for cp=" + oldContextPath + "; oldWebDir=" + oldWebDir);
            undeploy(oldContextPath, oldWebDir);
        }

        deploy(contextPath, webDir);

        try {
            webapp.stop();
        } catch (Exception ex) {
            error("NB-DEPLOYER: EXCEPTION redeploy: stop", ex);
            //Logger.getLogger(CommandManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        out("NB-DEPLOYER: redeploy: success !!!");
        return;// webapp;
    }

    protected WebAppContext findWebAppContext(String contextPath) {
        Handler[] hs = getServer().getChildHandlersByClass(WebAppContext.class);
        for (Handler h : hs) {
            WebAppContext w = (WebAppContext) h;
            if (contextPath.equals(w.getContextPath())) {
                return w;
            }
        }//for

        return null;
    }

    protected Map<WebAppContext, ContextHandlerCollection> findWebApps() {
        Map<WebAppContext, ContextHandlerCollection> map = new HashMap<>();

        Handler[] contextHandlers = getServer().getChildHandlersByClass(ContextHandlerCollection.class);
        for (Handler ch : contextHandlers) {
            ContextHandlerCollection chs = (ContextHandlerCollection) ch;
            Handler[] hs = chs.getChildHandlersByClass(WebAppContext.class);
            for (Handler h : hs) {
                out("NB-DEPLOYER: findWebApps() WebAppContext contextPath=: " + ((WebAppContext) h).getContextPath());
                map.put((WebAppContext) h, chs);
            }//for
        }
        return map;
    }

    protected WebAppContext findWebApps(String warPath) {
        out("NB-DEPLOYER: findWebApps(String) param warPath=" + warPath);
        Handler[] contextHandlers = getServer().getChildHandlersByClass(ContextHandlerCollection.class);
        for (Handler ch : contextHandlers) {
            ContextHandlerCollection chs = (ContextHandlerCollection) ch;
            Handler[] hs = chs.getChildHandlersByClass(WebAppContext.class);
            for (Handler h : hs) {
                WebAppContext w = (WebAppContext) h;
                File f1 = new File(w.getWar());
                File f2 = new File(warPath);
                if (f1.equals(f2)) {
                    out("NB-DEPLOYER: findWebApps(String) found contextPath=" + w.getContextPath());
                    return (WebAppContext) h;
                }
            }//for
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

        undeploy(contextPath, webDir);
    }

    protected void undeploy(String contextPath, String webDir) {

        out("NB-DEPLOYER: undeploy started for cp=" + contextPath + "; webDir=" + webDir);

        Map<WebAppContext, ContextHandlerCollection> map = findWebApps();

        if (map.isEmpty()) {
            return; // Nothing to undeploy
        }
        WebAppContext c = findWebAppContext(contextPath);
        if (c == null) {
            return; // Nothing to undeploy
        }
        try {
            if (!c.isStopped()) {
                c.stop();
            }
            map.get(c).removeHandler(c);
        } catch (Exception ex) {
            //Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            error("NB-DEPLOYER: undeploy failed: ");
            return;
        }

        out("NB-DEPLOYER: undeploy: success");

        //printInfoAfter("undeploy");
    }

    /*    protected void undeployHotDeployed(HttpServletRequest request) {
     String webDir = request.getParameter("dir");
     webDir = new File(webDir).getAbsolutePath();

     String contextPath = request.getParameter("cp");
     undeployHotDeployed(contextPath, webDir);
     }
     */
    protected void shutdownHotDeployed(String contextPath) {

        out("NB-DEPLOYER: shutdownHotDeployed started for cp=" + contextPath);

        Map<WebAppContext, ContextHandlerCollection> map = findWebApps();

        if (map.isEmpty()) {
            return; // Nothing to undeploy
        }
        WebAppContext c = findWebAppContext(contextPath);
        if (c == null) {
            return; // Nothing to undeploy
        }
        c.shutdown();

        out("NB-DEPLOYER:  AFTER SHUT DOWN isStopped=" + c.isStopped());
        out("NB-DEPLOYER:  AFTER SHUT DOWN c.isRunning()=" + c.isRunning());
        out("NB-DEPLOYER:  AFTER SHUT DOWN isAvailable=" + c.isAvailable());
        out("NB-DEPLOYER:  AFTER SHUT DOWN isShutdown=" + c.isShutdown());
        out("NB-DEPLOYER:  AFTER SHUT DOWN isStopping=" + c.isStopping());

        out("NB-DEPLOYER: shutdown : success");

        //printInfoAfter("undeploy");
    }

    /*    protected void undeployHotDeployed(String contextPath, String webDir) {

     out("NB-DEPLOYER:  undeployhotdeployed started for cp=" + contextPath + "; webDir=" + webDir);
     Map<WebAppContext, ContextHandlerCollection> map = findWebApps();

     if (map.isEmpty()) {
     return; // Nothing to undeploy
     }

     Handler[] handlers = getServer().getChildHandlersByClass(WebAppContext.class);
     File webDirFile = new File(webDir);
     WebAppContext webapp = null;
     for (Handler h : handlers) {
     WebAppContext c = (WebAppContext) h;
     File f = new File(c.getWar());
     if (f.equals(webDirFile)) {
     webapp = c;
     break;
     }
     }
     if (webapp == null) {
     return;
     }

     try {
     if (!webapp.isStopped()) {
     out("NB-DEPLOYER: undeployhotdeployed stopping... ");
     webapp.stop();
     }
     map.get(webapp).removeHandler(webapp);
     } catch (Exception ex) {
     error("NB-DEPLOYER: undeployhotdeployed failed: ");
     return;
     }
     out("NB-DEPLOYER: undeployhotdeployed: success");

     }
     */
    protected void startHotDeployed(HttpServletRequest request) {

        String contextPath = request.getParameter("cp");
        startHotDeployed(contextPath);
    }

    protected void startHotDeployed(String contextPath) {
        out("NB-DEPLOYER: starthotdeployed command. contextPath=" + contextPath);

        Handler[] handlers = getServer().getChildHandlersByClass(WebAppContext.class);
        WebAppContext webapp = null;
        for (Handler h : handlers) {
            WebAppContext c = (WebAppContext) h;
            if (c.getContextPath().equals(contextPath)) {
                webapp = c;
                break;
            }
        }

        if (webapp == null) {
            return;
        }
        try {
            if (webapp.isStopped()) {
                out("NB-DEPLOYER: startHot isStopped");
                webapp.start();
            } else if (webapp.isShutdown()) {
                webapp.setAvailable(true);
            }
        } catch (Exception ex) {
            error("NB-DEPLOYER: starthotdeployed: failed: ");
            return;// webapp;
        }
        out("NB-DEPLOYER: starthotdeployed: success (isAvailable: " + webapp.isAvailable() + "; isStopped=" + webapp.isStopped() + ")");

    }

    protected void shutdownHotDeployed(HttpServletRequest request) {

        String contextPath = request.getParameter("cp");
        shutdownHotDeployed(contextPath);
    }

    /*    protected void stopHotDeployed(HttpServletRequest request) {

     String webDir = request.getParameter("dir");
     if ( webDir != null ) {
     webDir = new File(webDir).getAbsolutePath();
     }
     String contextPath = request.getParameter("cp");

     stopHotDeployed(contextPath, webDir);
     }

     protected void stopHotDeployed(String contextPath, String webDir) {
     undeployHotDeployed(contextPath,webDir);
     if ( true ) {
     return;
     }
     Handler[] handlers = getServer().getChildHandlersByClass(WebAppContext.class);
     File webDirFile = new File(webDir);

     WebAppContext found = null;
     for (Handler h : handlers) {
     WebAppContext c = (WebAppContext) h;
     File f = new File(c.getWar());
     if (f.equals(webDirFile)) {
     //webapp = c;
     found = c;
     break;
     }
     }
     if (found == null) {
     return;
     }
     final WebAppContext webapp = found;
     try {
     if (webapp.isAvailable() && !webapp.isStopped()) {
     out("NB-DEPLOYER: stophotdeployed: before stop() (isAvailable: " + webapp.isAvailable() + "; isStopped=" + webapp.isStopped() + ")");
     try {
     printListeners(webapp," BEFORE ");
     sleep(500);
                    
     webapp.stop();
     printListeners(webapp," AFTER ");
     } catch(Exception ex) {
     error("1) NB-DEPLOYER: stophotdeployed: failed: ", ex);
     }
     new Thread(() -> {
     while (!webapp.isStopped()) {
     try {
     Thread.sleep(100);
     } catch (InterruptedException ex) {
     Logger.getLogger(CommandManager.class.getName()).log(Level.SEVERE, null, ex);
     }
     }
     File tmp = webapp.getTempDirectory();
     //tmp.deleteOnExit();
     webapp.setTempDirectory(null);
     ClassLoader cl = webapp.getClassLoader();
     try {
     //webapp.setClassLoader(null);
     //webapp.setParentLoaderPriority(true);
     } catch (Exception ex) {
     out("NB-DEPLOYER: stophotdeployed: EX 1 " + ex.getMessage());
     }
     try {
     //IO.delete(tmp);                    
     } catch (Exception ex) {
     out("NB-DEPLOYER: stophotdeployed: EX 2 " + ex.getMessage());
     }
     try {
     //webapp.setClassLoader(cl);
     //webapp.setParentLoaderPriority(false);
     } catch (Exception ex) {
     out("NB-DEPLOYER: stophotdeployed: EX 3 " + ex.getMessage());
     }
     });
     out("NB-DEPLOYER: stophotdeployed: after stop() temp dir: " + webapp.getTempDirectory());
     }
     } catch (Exception ex) {
     error("NB-DEPLOYER: stophotdeployed: failed: ", ex);
     return;// webapp;
     }
     out("NB-DEPLOYER: stophotdeployed: success (isAvailable: " + webapp.isAvailable() + "; isStopped=" + webapp.isStopped() + ")");

     }
     */
    protected String getLifecycleState(HttpServletRequest request) {
        String webDir = request.getParameter("dir");
        webDir = new File(webDir).getAbsolutePath();
        return getLifecycleState(webDir);
    }

    protected String getLifecycleStateByContextPath(HttpServletRequest request) {
        String cp = request.getParameter("cp");
        return getLifecycleStateByContextPath(cp);
    }

    /**
     *
     * @param webDir the path of a deployed application in a {@code webapps}
     * @return directory.
     */
    protected String getLifecycleState(String webDir) {

        Handler[] handlers = getServer().getChildHandlersByClass(WebAppContext.class);
        File webDirFile = new File(webDir);
        String state;// = null;
        WebAppContext webapp = null;
        for (Handler h : handlers) {
            WebAppContext c = (WebAppContext) h;
            File f = new File(c.getWar());
            if (f.equals(webDirFile)) {
                webapp = c;
                break;
            }
        }
        if (webapp != null) {
            state = webapp.getState() + " " + webapp.getContextPath();
        } else {
            state = "UNAVAILABLE UNAVAILABLE";
        }

        return state;
    }

    /**
     *
     * @param cp the path of a deployed application in a {@code webapps}
     * @return directory.
     */
    protected String getLifecycleStateByContextPath(String cp) {

        out("NB-DEPLOER: getLifecycleStateByContextPath param contextPath=" + cp);

        Handler[] handlers = getServer().getChildHandlersByClass(ContextHandler.class);
        String state;// = null;
        WebAppContext webapp = null;

        for (Handler h : handlers) {
            WebAppContext c = (WebAppContext) h;
            if (cp.equals(c.getContextPath())) {
                webapp = c;
                break;
            }
        }
        if (webapp != null) {
            out("NB-DEPLOYER: getLifecycleStateByContextPath FOUND state=" + webapp.getState());
            if (webapp.isShutdown()) {
                return "SHUTDOWN" + " " + webapp.getContextPath();
            }
            state = webapp.getState() + " " + webapp.getContextPath();
        } else {
            out("NB-DEPLOYER: getLifecycleStateByContextPath NOT FOUND state=UNAVAILABLE");
            state = "UNAVAILABLE UNAVAILABLE";
        }

        return state;
    }

    protected void start(HttpServletRequest request) {

        String contextPath = request.getParameter("cp");
        String webDir = request.getParameter("dir");
        webDir = new File(webDir).getAbsolutePath();

        start(contextPath, webDir);
    }

    public void copyMavenChangedClasses(WebAppContext webapp) throws IOException {

        if (Utils.isMavenProject(webapp.getWar())) {
            return;
        }
        if (!webapp.isCopyWebDir()) {
            return;
        }

        String mavenBuildDir = Utils.getMavenBuildDir(webapp.getWar());
        File to = new File(webapp.getWar() + "/WEB-INF/classes");
        File from = new File(webapp.getWar());

        from = new File(from.getParent() + "/classes");
        IO.copyDir(from, to);
    }

    protected void start(String contextPath, String webDir) {

        out("NB-DEPLOYER: start command for cp=" + contextPath + "; webDir=" + webDir);
        Map<WebAppContext, ContextHandlerCollection> map = findWebApps();

        if (map.isEmpty()) {
            out("NB-DEPLOYER: start: map is empty. No webapp found to start");
            return;// null;
        }
        WebAppContext webapp = findWebAppContext(contextPath);
        if (webapp == null) {
            out("NB-DEPLOYER:  No webapp found to start");
            return;// null;
        }
        if (webapp.isStarted()) {
            out("NB-DEPLOYER:  webapp has already started");
            return;
        }
        try {
            copyMavenChangedClasses(webapp);
            webapp.start();
            out("NB-DEPLOYER: copyMavenChangedClasses, start ");
        } catch (Exception ex) {
            error("NB-DEPLOYER: start: failed: ");
            return;// webapp;
        }
        out("NB-DEPLOYER: start: success");
    }

    protected void stop(HttpServletRequest request) {

        String webDir = request.getParameter("dir");
        webDir = new File(webDir).getAbsolutePath();

        String contextPath = request.getParameter("cp");
        stop(contextPath, webDir);
    }

    protected void stop(String contextPath, String webDir) {
        out("NB-DEPLOYER: stop command for cp=" + contextPath + "; webDir=" + webDir);
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
            /*            while (!webapp.isStopped()) {
             sleep(100);
             out("NB-DEPLOYER: sleep");
             }
             */
        } catch (Exception ex) {
            error("NB-DEPLOYER: stop: failed: ");
            return;// webapp;
        }
        out("NB-DEPLOYER: stop: success");
    }

    public static void sleep(long msec) {
        Long time = System.currentTimeMillis();
        while (System.currentTimeMillis() < time + msec) {
        }

    }

    protected String getCopyDir(HttpServletRequest request) {

        out("NB-DEPLOYER: COMMAND = getcopydir");
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

        out("NB-DEPLOYER: getcopydir started for cp=" + contextPath + "; webDir=" + webDir);
        String copyDir;// = null;
        Map<WebAppContext, ContextHandlerCollection> map = findWebApps();

        if (map.isEmpty()) {
            out("NB-DEPLOYER: getcopydir: no handler found");
            return null;
        }
        WebAppContext webapp = findWebAppContext(contextPath);
        if (webapp == null) {
            out("NB-DEPLOYER: getcopydir: no handler found");
            return null;
        }
        if (!webapp.isCopyWebDir()) {
            out("NB-DEPLOYER: getcopydir: Inplace Deployment");
            return "inplace";
        }
        copyDir = webapp.getTempDirectory().getAbsolutePath() + "/webapp";
        out("NB-DEPLOYER: getcopydir: success");
        return copyDir;
    }

    protected void printInfoBefore(String command) {
        Map<WebAppContext, ContextHandlerCollection> map = findWebApps();

        if (map.isEmpty()) {
            System.out.println("NB-DEPLOYER: " + "command : no handler found");
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
            System.out.println("NB-DEPLOYER: " + command + ": no handler found");
            return;
        }

        int i = 0;
        for (WebAppContext webapp : map.keySet()) {
            System.out.println("NB-DEPLOYER: " + (++i) + ". cp=" + webapp.getContextPath() + "; webDir=" + webapp.getWar());
        }
        System.out.println("===================================");
    }

    protected void deploy(HttpServletRequest request) {

        String contextPath = request.getParameter("cp");
        String webDir = request.getParameter("dir");
        webDir = new File(webDir).getAbsolutePath();

        String projectType = request.getParameter("projtype");
        out("NB-DEPLOYER:  PROJECT TYPE=" + projectType);
        if (Utils.DEPLOY_HTML5_PROJECTTYPE.equals(projectType)) {
            deployHtml5(contextPath, webDir);
        } else {
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
        out("NB-DEPLOYER: deploy started for cp=" + contextPath + "; webDir=" + webDir);
        Handler[] contextHandlers = getServer().getChildHandlersByClass(ContextHandlerCollection.class);
        if (contextHandlers == null || contextHandlers.length == 0) {
            out("NB-DEPLOYER: deploy: no handler found. deploy finished");
            return;// null;
        }
        WebAppContext webapp = findWebAppContext(contextPath);
        if (webapp != null) {
            out("NB-DEPLOYER: deploy: there is a handler with the same contextPath=" + webapp.getContextPath() + " (webDir=" + webapp.getWar());
            return;// null;
        }

        String path = getWarPath(webDir); // to deploy

        WebAppContext c = findWebApps(path);
        if (c != null) {
            out("NB-DEPLOYER: deploy: there is a handler with the same webDir=" + webDir + ". Execute undeploy");
            undeploy(contextPath, webDir);
        }
        boolean antiResource = Utils.isWindows();
        Properties props = Utils.getContextProperties(path);
        String s = props.getProperty("copyWebDir");

        if (s != null) {
            antiResource = Boolean.parseBoolean(s);
        }

        if (webapp == null) {
            webapp = new WebAppContext();
        }

        webapp.setWar(path);

        webapp.setContextPath(contextPath);
        webapp.getInitParams()
                .put("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");
        webapp.setCopyWebDir(antiResource);

        ((ContextHandlerCollection) contextHandlers[0]).addHandler(webapp);
        out("NB-DEPLOYER: deploy: success");
    }

    public static void enableAnnotationsJspJNDI(WebAppContext webapp) {
        webapp.setConfigurationClasses(new String[]{
            "org.eclipse.jetty.webapp.WebInfConfiguration",
            "org.eclipse.jetty.webapp.WebXmlConfiguration",
            "org.jetty.custom.handlers.WebXmlNbConfig",
            "org.eclipse.jetty.webapp.MetaInfConfiguration",
            "org.eclipse.jetty.webapp.FragmentConfiguration",
            "org.eclipse.jetty.plus.webapp.EnvConfiguration",
            "org.eclipse.jetty.plus.webapp.PlusConfiguration",
            "org.eclipse.jetty.annotations.AnnotationConfiguration",
            "org.eclipse.jetty.webapp.JettyWebXmlConfiguration"
        }
        );
    }

    protected Handler deployHtml5(String contextPath, String webDir) {
        out("NB-DEPLOYER: deployHtml5 started for cp=" + contextPath + "; projectType=" + Utils.DEPLOY_HTML5_PROJECTTYPE + "; webDir=" + webDir);
        Handler[] contextHandlers = getServer().getChildHandlersByClass(ContextHandlerCollection.class
        );
        if (contextHandlers == null || contextHandlers.length
                == 0) {
            return null;
        }
        WebAppContext webapp = findWebAppContext(contextPath);
        if (webapp != null) {
            out("NB-DEPLOYER: deploy: there is a handler with the same contextPath=" + webapp.getContextPath() + " (webDir=" + webapp.getWar());
            return null;
        }

        String path = getHtml5WarPath(webDir); // to deploy

        WebAppContext c = findWebApps(path);
        if (c != null) {
            out("NB-DEPLOYER: deploy: there is a handler with the same webDir=" + webDir + ". Execute undeploy");
            undeploy(contextPath, webDir);
        }

        if (webapp == null) {
            webapp = new WebAppContext();
        }

        webapp.setContextPath(contextPath);

        webapp.setWar(path.replace("\\", "/"));

        webapp.getInitParams()
                .put("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");

        ((ContextHandlerCollection) contextHandlers[0]).addHandler(webapp);

        out("NB-DEPLOYER: deploy html5: success");

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

    public static class ManagerLifeCycleListener extends AbstractLifeCycleListener {

        CommandManager cm;

        public ManagerLifeCycleListener(CommandManager cm) {
            this.cm = cm;
        }

        @Override
        public void lifeCycleStarting(LifeCycle lc) {
            updateServerHandlers();
        }

        @Override
        public void lifeCycleStarted(LifeCycle lc) {
            if (IniModules.isCDIEnabled()) {
                Handler[] hs = cm.getServer().getChildHandlersByClass(CustomWebAppContext.class);
                if (hs.length == 0) {

                    WebAppContext ctx = new CustomWebAppContext(cm);
                    Handler[] chc = cm.getServer().getChildHandlersByClass(ContextHandlerCollection.class);
                    ((ContextHandlerCollection) chc[0]).addHandler(ctx);
                    try {
                        ctx.start();
                    } catch (Exception ex) {
                        cm.error("NB-DEPLOYER: EXCEPTION server.lifeCycleStarted(): " + ex.getMessage());
                    }
                }

            }
        }

        protected void updateServerHandlers() {

            HandlerCollection hc = (HandlerCollection) cm.getServer().getHandler();
            Handler[] dhs = hc.getChildHandlersByClass(DefaultHandler.class);
            //
            // Remove all DefaultHandlers and the add them at the end
            //
            if (dhs != null) {
                for (Handler h : dhs) {
                    hc.removeHandler(h);
                }
                for (Handler h : dhs) {
                    hc.addHandler(h);
                }
            }
        }

    }

    protected static class CustomWebAppContext extends WebAppContext {

        private static final String CONTEXT_PATH = "/WEB_APP_FOR_CDI_WELD";
        private static final String PREFIX = "jetty_cdi_weld_support_webapp_stub_";
        private static final String STUB_FILE_NAME = ".donotdelete";

        public CustomWebAppContext(CommandManager cm) {
            super();
            init(cm);
        }

        private void init(CommandManager cm) {
            File tmp = getTempDirectory();
            if (tmp == null) {
                tmp = new File(System.getProperty("java.io.tmpdir"));
            }

            cm.out("NB-DEPLOYER: CustomWebAppContext system temp file = " + tmp);

            Path stub = Paths.get(System.getProperty(Utils.JETTY_BASE), "resources", STUB_FILE_NAME);

            cm.out("NB-DEPLOYER: CustomWebAppContext stub  = " + stub);

            Path dirs = Paths.get(tmp.getPath(), PREFIX + "_DIR");
            Path war = Paths.get(dirs.toString(), CONTEXT_PATH.substring(1) + "_" + System.currentTimeMillis() + ".war");
            if (!Files.exists(war)) {
                try {
                    if (!Files.exists(dirs)) {
                        Files.createDirectories(dirs);
                    }
                    Files.copy(stub, war);

                } catch (IOException ex) {
                    cm.error("NB-DEPLOYER: CustomWebAppContext create directoriesexception ", ex);
                }
            }
            setContextPath(CONTEXT_PATH);
            setWar(war.toString());
            war.toFile().deleteOnExit();
            cm.out("NB-DEPLOYE: CustomWebAppContext cp=" + CONTEXT_PATH + "war=" + war);

        }
    }

}
