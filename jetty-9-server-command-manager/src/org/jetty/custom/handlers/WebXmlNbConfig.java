/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jetty.custom.handlers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.EnumSet;
import java.util.EventListener;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Resource;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.webapp.AbstractConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 *
 * @author Valery
 */
public class WebXmlNbConfig extends AbstractConfiguration {

    private CommandManager cm;

    protected void out(String msg) {

        if (cm == null || "NO".equals(cm.getMessageOption())) {
            return;
        }
        System.out.println("NB-DEPLOER: WebXmlNbConfig: " + msg);
    }

    /**
     *
     * @param context
     * @throws java.lang.Exception
     */
    @Override
    public void preConfigure(WebAppContext context) throws Exception {
        
        if (context.getTempDirectory() != null) {
            context.getTempDirectory().deleteOnExit();
        }

        Map<String, ? extends FilterRegistration> srf = (Map<String, FilterRegistration>) context.getServletContext().getFilterRegistrations();
        int n = 0;
        if (srf != null) {
            n = srf.size();
        }

        Handler[] hs = context.getServer().getChildHandlersByClass(CommandManager.class);
        if (hs.length > 0) {
            cm = (CommandManager) hs[0];
        }

        out(" ============ PRECONFIGURE WebAppContext.contextPath " + context.getContextPath());
        
        out(" temp dir = " + context.getTempDirectory());
        out(" addFilter(" + JsfFilter.class.getName() + ")");

        out(" ------------ SystemClasses  for WebAppContext.contextPath " + context.getClassPath() + ";");
        out(" --- addSystemClass(com.sun.faces.)");
        out(" --- addSystemClass(javax.faces.)");
        out(" --- addSystemClass(com.google.common.)");

        out(" ------------ Prepend Server Classes  for WebAppContext.contextPath " + context.getClassPath() + ";");
        out(" --- prependServerClass(-com.sun.faces.)");
        out(" --- prependServerClass(-javax.faces.)");
        out(" --- prependServerClass(-com.google.common.)");

        //
        // webapp cannot change / replace jsf classes        
        //
        context.addSystemClass("com.sun.faces.");
        context.addSystemClass("javax.faces.");
        context.addSystemClass("com.google.common.");
        //
        // don't hide jsf classes from webapps 
        // (allow webapp to use ones from system classloader)        
        //
        context.prependServerClass("-com.sun.faces.");
        context.prependServerClass("-javax.faces.");
        context.prependServerClass("-com.google.common.");

            out(" Is CDI enabled = " + IniModules.isCDIEnabled());
        
        if (IniModules.isCDIEnabled()) {
            context.getServletContext().addListener("org.jboss.weld.environment.servlet.Listener");
            out(" --- addListener org.jboss.weld.environment.servlet.Listener");
            context.getServletContext().setAttribute("org.jboss.weld.environment.servlet.listenerUsed", true);
            out(" --- setAttribute(org.jboss.weld.environment.servlet.listenerUsed, true");
            if (context.getInitParameter("WELD_CONTEXT_ID_KEY") == null) {
                if (!"/WEB_APP_FOR_CDI_WELD".equals(context.getContextPath())) {
                    out(" --- setInitParameter(WELD_CONTEXT_ID_KEY, UUID.randomUUID()");
                    
                    UUID id = UUID.randomUUID();
                    context.setInitParameter("WELD_CONTEXT_ID_KEY", id.toString());
                }
                //}
            }
            //context.getServletContext().addListener("org.jboss.weld.environment.servlet.EnhancedListener");            
        }
        //
        // add config listener for an active jsf module
        //
        if (IniModules.isJSFEnabled()) {
            EnumSet<DispatcherType> es = EnumSet.of(DispatcherType.REQUEST);
            context.addFilter(JsfFilter.class, "/", es);

            String className = IniModules.getJsfListenerClassName();

            if (className != null) {
                context.getServletContext().addListener(className);
                out(" add config listener for WebAppContext.contextPath=" + context.getClassPath() + "; configure class=" + className);
            }
            EventListener[] els = context.getEventListeners();
            for (EventListener el : els) {
                out("   " + el.getClass().getName());
            }
        }

    }

    /**
     * Process web-default.xml, web.xml, override-web.xml
     *
     * @param context
     */
    @Override
    public void configure(WebAppContext context) throws Exception {
        out(" --- configure() WebAppContext.contextPath " + context.getContextPath());
    }

    /* ------------------------------------------------------------------------------- */
    protected Resource findWebXml(WebAppContext context) throws IOException, MalformedURLException {
        return null;
    }


    /* ------------------------------------------------------------------------------- */
    @Override
    public void deconfigure(WebAppContext context) throws Exception {
        out(" --- deconfigure() contextPath " + context.getContextPath());
    }

    @Override
    public void postConfigure(WebAppContext context) throws Exception {
/*        context.getServletContext().setAttribute("org.jboss.weld.environment.servlet.listenerUsed", true);
        EventListener[] elold = context.getEventListeners();
        EventListener[] elnew = new EventListener[elold.length + 1];
        EventListener enh = null;
        out(" --- Post Configure Listeners BEFORE  WebAppContext.contextPath " + context.getClassPath() + ";");
        EventListener[] els = context.getEventListeners();
        for (EventListener el : els) {
            out("   " + el);
        }

        out(" --- Post Configure  elold.length=" + elold.length);
        int n = 0;
        for (int i = 0; i < elold.length; i++) {
            elnew[i] = elold[i];
            /*            out(" --- Post Configure 1  i=" + i + "; n=" + n);
             out(" --- Post Configure 2 " + elold[i].getClass().getName());
             if (elold[i].getClass().getName().equals("org.jboss.weld.servlet.WeldTerminalListener")) {
             out(" --- Post Configure 3 i=" + i + "; n=" + n);
             enh = elold[i];
             continue;
             } else {
             out(" --- Post Configure 4 i=" + i + "; n=" + n);
             elnew[n] = elold[i];
             }
             n++;
        }
        if (enh != null) {
            out(" --- Post Configure 2");
            //elnew[elnew.length - 1] = enh;

        }
        elnew[elnew.length - 1] = new MyListener();
        //    context.setEventListeners(elnew);

        out(" --- Post Configure Listeners for WebAppContext.contextPath " + context.getClassPath() + ";");
        els = context.getEventListeners();
        for (EventListener el : els) {
            out("   " + el);
        }

        out(" -----------------------------");
        out(" --- Post Configure WellCome Files  for WebAppContext.contextPath " + context.getClassPath() + ";");
        String[] wfs = context.getWelcomeFiles();
        for (String wf : wfs) {
            out("   " + wf);
        }
        out(" -----------------------------");
*/        

        //context.getTempDirectory().deleteOnExit();
    }

    public static class MyListener implements ServletContextListener, ServletRequestListener, HttpSessionListener {

        @Override
        public void contextInitialized(ServletContextEvent sce) {
            System.out.println("NB-DEPLOER: WebXmlNbConfig.MyListener contextInitialized");
        }

        @Override
        public void contextDestroyed(ServletContextEvent sce) {
            System.out.println("NB-DEPLOER: WebXmlNbConfig.MyListener contextDestroyed");
        }

        @Override
        public void requestDestroyed(ServletRequestEvent sre) {
            System.out.println("NB-DEPLOER: WebXmlNbConfig.MyListener requestDestroyed");
        }

        @Override
        public void requestInitialized(ServletRequestEvent sre) {
            System.out.println("NB-DEPLOER: WebXmlNbConfig.MyListener requestInitialized");
        }

        @Override
        public void sessionCreated(HttpSessionEvent hse) {
            System.out.println("NB-DEPLOER: WebXmlNbConfig.MyListener sessionCreated");
        }

        @Override
        public void sessionDestroyed(HttpSessionEvent hse) {
            System.out.println("NB-DEPLOER: WebXmlNbConfig.MyListener sessionDestroyed");
        }

    }
}
