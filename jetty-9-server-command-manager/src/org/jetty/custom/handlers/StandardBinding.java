package org.jetty.custom.handlers;

import java.util.EnumSet;
import java.util.Map;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import org.eclipse.jetty.deploy.App;
import org.eclipse.jetty.deploy.AppLifeCycle;
import org.eclipse.jetty.deploy.graph.Node;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Is here as an example. Is not used.
 *
 * @author Valery
 */
public class StandardBinding implements AppLifeCycle.Binding {

    @Override
    public String[] getBindingTargets() {
        return new String[]{"stopping", "stopped", "deploying", "deployed", "starting", "started"};
    }

    @Override
    public void processBinding(Node node, App app) throws Exception {
        ContextHandler handler = app.getContextHandler();
        System.out.println("NB-BINDING:  Procesing Bindings lifecycle_name=" + node.getName() + "; handler = " + handler);
        if (handler == null) {
            throw new NullPointerException("NB-BINDING: No Handler created for App: " + app);
        }
        if (handler instanceof WebAppContext) {
            
            WebAppContext webapp = (WebAppContext) handler;
        System.out.println("NB-BINDING:  Procesing Bindings webapp.cp=" + webapp.getContextPath());
        System.out.println("NB-BINDING:  Procesing Bindings ATTR=" + webapp.getAttribute("org.jboss.weld.environment.servlet.javax.enterprise.inject.spi.BeanManager"));
        
            
/*            Map<String, ? extends FilterRegistration> srf = (Map<String, FilterRegistration>) webapp.getServletContext().getFilterRegistrations();
            EnumSet<DispatcherType> es = EnumSet.of(DispatcherType.REQUEST);
            if ("deployed".equals(node.getName())) {
                webapp.addFilter(JsfFilter.class, "/", es);
            }
            String[] wf = webapp.getWelcomeFiles();
            if (wf != null) {
            }
*/
        }
    }
}
