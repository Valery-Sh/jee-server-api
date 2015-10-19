package org.netbeans.plugin.support.embedded.jetty;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.EnumSet;
import java.util.EventListener;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import org.eclipse.jetty.webapp.AbstractConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 *
 * @author V. Shyhkin
 */
public class WebNbJsfConfig extends AbstractConfiguration {


    protected void out(String msg) {

        if (! CommandManager.getInstance().isVerbose()) {
            return;
        }
        System.out.println("NB-DEPLOER: WebNbJsfConfig: " + msg);
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


        out(" ============ JSF PRECONFIGURE WebAppContext.contextPath = " + context.getContextPath());

        out(" temp dir = " + context.getTempDirectory());

        out(" IsJsfEnabled()" + CommandManager.isJSFEnabled());

        //
        // add config listener for an active jsf module
        //
        if (CommandManager.isJSFEnabled()) {
            out(" addFilter(" + JsfFilter.class.getName() + ")");

            out(" ------------ SystemClasses  for WebAppContext.contextPath " + context.getClassPath() + ";");
            out(" --------------------------------------------------------------------------------------------");
            
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
        
            // don't hide jsf classes from webapps 
            // (allow webapp to use ones from system classloader)        
            //
            
            context.prependServerClass("-com.sun.faces.");
            context.prependServerClass("-javax.faces.");
            context.prependServerClass("-com.google.common.");

            EnumSet<DispatcherType> es = EnumSet.of(DispatcherType.REQUEST);
            context.addFilter(JsfFilter.class, "/", es);

            String className = CommandManager.getInstance().getServerConfig().getJsfListener();
            out(" --- JSF Listener name=" + className);
            if (className != null) {
                context.getServletContext().addListener(className);
                out(" --- add config listener for WebAppContext.contextPath=" + context.getClassPath() + "; configure class=" + className);
            }
            EventListener[] els = context.getEventListeners();
            out(" --------------------------------------------------------------------------------------------");
        }

    }

    /**
     * Process web-default.xml, web.xml, override-web.xml
     *
     * @param context
     */
    @Override
    public void configure(WebAppContext context) throws Exception {
    }

    /* ------------------------------------------------------------------------------- */
    protected Resource findWebXml(WebAppContext context) throws IOException, MalformedURLException {
        return null;
    }


    /* ------------------------------------------------------------------------------- */
    @Override
    public void deconfigure(WebAppContext context) throws Exception {
    }

    @Override
    public void postConfigure(WebAppContext context) throws Exception {
    }

}
