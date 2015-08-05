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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.DispatcherType;
import javax.servlet.ServletRegistration;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ShutdownHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 *
 * @author V. Shyshkin
 */
public class CommandManager extends AbstractHandler implements LifeCycle.Listener {

    public static final String WELD_LISTENER_CLASS_NAME = "org.jboss.weld.environment.servlet.Listener";

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
    protected boolean jsfActivated;
    /**
     * When {@literal true} then the server supports Weld.
     */
    protected boolean weldActivated;

    private File jettyBase;

    public CommandManager() {
        super();
        System.err.println("CommandManager Started");

        String path = new File(".").getAbsolutePath();
        if (path.endsWith("/.")) {
            path = path.substring(0, path.indexOf("/."));
        } else if (path.endsWith("\\.")) {
            path = path.substring(0, path.indexOf("\\."));
        }
        jettyBase = new File(path);
        jsfActivated = new File(jettyBase + "/start.d/jsf.ini").exists();
        // weldActivated = new File(jettyBase + "/start.d/cdi.ini").exists();
        init();
    }

    /**
     *
     */
    private void init() {
        addLifeCycleListener(new ManagerLifeCycleListener(this));
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {// throws IOException, ServletException {

        response.setContentType("text/html;charset=utf-8");

        if (!"/jeeserver/manager".equals(target)) {
            return;
        }
        System.out.println("handle target=" + target);
        String cp = request.getParameter("cp");
        System.out.println("--- handle cp=" + cp);
        String cmd = request.getParameter("cmd");
        System.out.println("--- handle cmd=" + cmd);

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
                    stopHotDeployed(request);
                    break;
                case "undeployhotdeployed":
                    undeployHotDeployed(request);
                    break;

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

        try {
            webapp.stop();
        } catch (Exception ex) {
            Logger.getLogger(CommandManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("redeploy: success");
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
                map.put((WebAppContext) h, chs);
            }//for
        }
        return map;
    }

    protected WebAppContext findWebApps(String warPath) {
        Handler[] contextHandlers = getServer().getChildHandlersByClass(ContextHandlerCollection.class);
        for (Handler ch : contextHandlers) {
            ContextHandlerCollection chs = (ContextHandlerCollection) ch;
            Handler[] hs = chs.getChildHandlersByClass(WebAppContext.class);
            for (Handler h : hs) {
                WebAppContext w = (WebAppContext) h;
                File f1 = new File(w.getWar());
                File f2 = new File(warPath);
                if (f1.equals(f2)) {
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
        //      return 
        undeploy(contextPath, webDir);
    }

    protected void undeploy(String contextPath, String webDir) {

        System.out.println("undeploy started for cp=" + contextPath + "; webDir=" + webDir);

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
            System.out.println("undeploy remove handler ");
            map.get(c).removeHandler(c);
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            System.err.println("undeploy failed: " + ex.getMessage());
            return;
        }

        System.out.println("undeploy: success");

        printInfoAfter("undeploy");
    }

    protected void undeployHotDeployed(HttpServletRequest request) {
        String webDir = request.getParameter("dir");
        webDir = new File(webDir).getAbsolutePath();

        String contextPath = request.getParameter("cp");
        undeployHotDeployed(contextPath, webDir);
    }

    protected void undeployHotDeployed(String contextPath, String webDir) {

        System.out.println("undeployhotdeployed started for cp=" + contextPath + "; webDir=" + webDir);
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
                System.out.println("undeployhotdeployed stopping... ");
                webapp.stop();
            }
            System.out.println("undeployhotdeployed remove handler ");
            map.get(webapp).removeHandler(webapp);
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            System.err.println("undeployhotdeployed failed: " + ex.getMessage());
            return;
        }
        System.out.println("undeployhotdeployed: success");

    }

    protected void startHotDeployed(HttpServletRequest request) {

        String contextPath = request.getParameter("cp");
        String webDir = request.getParameter("dir");
        webDir = new File(webDir).getAbsolutePath();

        startHotDeployed(contextPath, webDir);
    }

    protected void startHotDeployed(String contextPath, String webDir) {
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
            if (webapp.isStopped()) {
                webapp.start();
            }
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            System.err.println("starthotdeployed: failed " + ex.getMessage());
            return;// webapp;
        }
        System.out.println("starthotdeployed: success (isAvailable: " + webapp.isAvailable() + "; isStopped=" + webapp.isStopped() + ")");

    }

    protected void stopHotDeployed(HttpServletRequest request) {

        String webDir = request.getParameter("dir");
        webDir = new File(webDir).getAbsolutePath();
        String contextPath = request.getParameter("cp");

        stopHotDeployed(contextPath, webDir);
    }

    protected void stopHotDeployed(String contextPath, String webDir) {

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
            if (webapp.isAvailable() && !webapp.isStopped()) {
                webapp.stop();
            }
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            System.err.println("stophotdeployed: failed " + ex.getMessage());
            return;// webapp;
        }
        System.out.println("stophotdeployed: success (isAvailable: " + webapp.isAvailable() + "; isStopped=" + webapp.isStopped() + ")");

    }

    protected String getLifecycleState(HttpServletRequest request) {

        String webDir = request.getParameter("dir");
        webDir = new File(webDir).getAbsolutePath();
        return getLifecycleState(webDir);
    }

    /**
     *
     * @param webDir the path of a deployed application in a {@code webapps}
     * @return directory.
     */
    protected String getLifecycleState(String webDir) {

//        System.out.println("getstate command for webDir=" + webDir);
        Handler[] handlers = getServer().getChildHandlersByClass(WebAppContext.class);
        File webDirFile = new File(webDir);
        String state = null;
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
//            System.out.println("getstate: success (result: " + state + ")");
        } else {
            state = "UNAVAILABLE UNAVAILABLE";
//            System.out.println("getstate: success (result: UNAVAILABLE)");
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
        //System.out.println("GCD MAVEN: copyMavenChangedClasses webapp.isCopyWebInf()=" + webapp.isCopyWebInf());
        //System.out.println("GCD MAVEN: copyMavenChangedClasses webapp.getTempDirectory()=" + webapp.getTempDirectory());
        //System.out.println("GCD MAVEN: copyMavenChangedClasses webapp.getWar()=" + webapp.getWar());

        if (Utils.isMavenProject(webapp.getWar())) {
            return;
        }
        if (!webapp.isCopyWebDir()) {
            return;
        }

        String mavenBuildDir = Utils.getMavenBuildDir(webapp.getWar());
        //System.out.println("GCD MAVEN: mavenBuildDir");
        File to = new File(webapp.getWar() + "/WEB-INF/classes");
        //File to = new File(webapp.getWebInf().getFile().getAbsoluteFile() + "/classes");

        File from = new File(webapp.getWar());

        from = new File(from.getParent() + "/classes");
        //System.out.println("GCD: copyMavenChangedClasses from=" + from.getAbsolutePath());
        //System.out.println("GCD: copyMavenChangedClasses to=" + to.getAbsolutePath());
        IO.copyDir(from, to);
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
//System.out.println("start: before copy maven");
        try {
            copyMavenChangedClasses(webapp);
            webapp.start();
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            System.err.println("start: failed " + ex.getMessage());
            return;// webapp;
        }
        System.out.println("start: success");

        //return;// webapp;
    }

    protected void stop(HttpServletRequest request) {

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
        String copyDir;// = null;
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
        if (!webapp.isCopyWebDir()) {
            System.out.println("getcopydir: Inplace Deployment");
            return "inplace";
        }
        copyDir = webapp.getTempDirectory().getAbsolutePath() + "/webapp";
        System.out.println("getcopydir: success");
        return copyDir;
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

    protected void deploy(HttpServletRequest request) {

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

    /**
     * {@literal Deployment mode} only.
     *
     * @param contextPath
     * @param webDir
     */
    protected void deploy(String contextPath, String webDir) {
        System.out.println("!!! deploy started for cp=" + contextPath + "; webDir=" + webDir);
        Handler[] contextHandlers = getServer().getChildHandlersByClass(ContextHandlerCollection.class);
        if (contextHandlers == null || contextHandlers.length == 0) {
            System.out.println("deploy: no handler found. deploy finished");
            return;// null;
        }
        WebAppContext webapp = findWebAppContext(contextPath);
        if (webapp != null) {
            System.out.println("deploy: there is a handler with the same contextPath=" + webapp.getContextPath() + " (webDir=" + webapp.getWar());
            return;// null;
        }

        String path = getWarPath(webDir); // to deploy

        WebAppContext c = findWebApps(path);

        if (c != null) {
            System.out.println("deploy: there is a handler with the same webDir=" + webDir + ". Execute undeploy");
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
            //EnumSet<DispatcherType> es = EnumSet.of(DispatcherType.REQUEST);
        }

        webapp.setWar(path);

        webapp.setContextPath(contextPath);
        webapp.getInitParams()
                .put("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");
        webapp.setCopyWebDir(antiResource);

        ((ContextHandlerCollection) contextHandlers[0]).addHandler(webapp);
        System.out.println("deploy: success");
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
        System.out.println("deployHtml5 started for cp=" + contextPath + "; projectType=" + Utils.DEPLOY_HTML5_PROJECTTYPE + "; webDir=" + webDir);
        Handler[] contextHandlers = getServer().getChildHandlersByClass(ContextHandlerCollection.class
        );
        if (contextHandlers == null || contextHandlers.length
                == 0) {
            return null;
        }
        WebAppContext webapp = findWebAppContext(contextPath);
        if (webapp != null) {
            System.out.println("deploy: there is a handler with the same contextPath=" + webapp.getContextPath() + " (webDir=" + webapp.getWar());
            return null;
        }

        String path = getHtml5WarPath(webDir); // to deploy

        System.out.println("PATH= " + path);
        WebAppContext c = findWebApps(path);
        if (c != null) {
            System.out.println("deploy: there is a handler with the same webDir=" + webDir + ". Execute undeploy");
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

        System.out.println("deploy html5: success");

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

}
