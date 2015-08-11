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
        return new String[]{"deploying", "deployed", "starting", "started"};
    }

    @Override
    public void processBinding(Node node, App app) throws Exception {
        ContextHandler handler = app.getContextHandler();
        System.out.println("NB-DEPLOYER:  Procesing Bindings handler = " + handler);
        if (handler == null) {
            throw new NullPointerException("NB-DEPLOYER: No Handler created for App: " + app);
        }
        if (handler instanceof WebAppContext) {
            WebAppContext webapp = (WebAppContext) handler;
            Map<String, ? extends FilterRegistration> srf = (Map<String, FilterRegistration>) webapp.getServletContext().getFilterRegistrations();
            EnumSet<DispatcherType> es = EnumSet.of(DispatcherType.REQUEST);
            if ("deployed".equals(node.getName())) {
                webapp.addFilter(JsfFilter.class, "/", es);
            }
            String[] wf = webapp.getWelcomeFiles();
            if (wf != null) {
            }

        }
    }
}
