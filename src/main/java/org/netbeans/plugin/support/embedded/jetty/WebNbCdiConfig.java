package org.netbeans.plugin.support.embedded.jetty;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Resource;
import javax.servlet.FilterRegistration;
import org.eclipse.jetty.webapp.AbstractConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 *
 * @author V. Shyhkin
 */
public class WebNbCdiConfig extends AbstractConfiguration {

    protected void out(String msg) {

        if (! CommandManager.getInstance().isVerbose() ) {
            return;
        }
        System.out.println("NB-DEPLOER: WebNbCdiConfig: " + msg);
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

        CommandManager cm = CommandManager.getInstance();

        out(" ============ PRECONFIGURE WebAppContext.contextPath = " + context.getContextPath());

        out(" temp dir = " + context.getTempDirectory());

        out(" IsCDIEnabled(" + context.getContextPath() + ") for a WebAppContext = " + CommandManager.isCDIEnabled(context)
                + " (needs beans.xml file)");
        out(" IsCDIEnabled() as defined in start.ini = " + CommandManager.isCDIEnabled());

        //
        // Here we must use isCDIEnabled() without parameter. So each webapp is processed
        //
        if (CommandManager.isCDIEnabled()) {
            //context.getServletContext().addListener("org.jboss.weld.environment.servlet.Listener");
            //out(" --- addListener org.jboss.weld.environment.servlet.Listener");
            //context.getServletContext().setAttribute("org.jboss.weld.environment.servlet.listenerUsed", true);
            //out(" --- setAttribute(org.jboss.weld.environment.servlet.listenerUsed, true");
            if (context.getInitParameter("WELD_CONTEXT_ID_KEY") == null) {
                if (!"/WEB_APP_FOR_CDI_WELD".equals(context.getContextPath())) {
                    UUID id = UUID.randomUUID();
                    context.setInitParameter("WELD_CONTEXT_ID_KEY", id.toString());
                    out(" --- setInitParameter(WELD_CONTEXT_ID_KEY, UUID.randomUUID()) = " + id.toString());
                }
            }
            //context.getServletContext().addListener("org.jboss.weld.environment.servlet.EnhancedListener");            
        }
        out(" --------------------------------------------------------------------------------------------");


    }

    /**
     * Process web-default.xml, web.xml, override-web.xml
     *
     * @param context
     * @throws java.lang.Exception
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
