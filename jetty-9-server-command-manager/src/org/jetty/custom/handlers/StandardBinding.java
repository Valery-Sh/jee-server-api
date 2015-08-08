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
System.out.println(" !!!!!!!!!!!! getBindingTargets");        
        return new String[]{"deploying", "deployed", "starting", "started"};
    }

    @Override
    public void processBinding(Node node, App app) throws Exception {
        ContextHandler handler = app.getContextHandler();
        System.out.println(" Procesing Bindings handler = " + handler);
        if (handler == null) {
            throw new NullPointerException("No Handler created for App: " + app);
        }
System.out.println(" Procesing Bindings handler.class = " + handler.getClass());        
        if (handler instanceof WebAppContext) {
            WebAppContext webapp = (WebAppContext) handler;
            System.out.println(" Procesing Bindings webapp: " + webapp);
            Map<String, ? extends FilterRegistration> srf = (Map<String, FilterRegistration>) webapp.getServletContext().getFilterRegistrations();
            for (Map.Entry<String, ? extends FilterRegistration> en : srf.entrySet()) {
                if ("javax.faces.webapp.FacesServlet".equals(en.getValue().getClassName())) {
                    //regs = en.getValue().getMappings();
//                    System.out.println(" ------------ Bindings: filter key=" + en.getKey()
//                            + "; value=" + en.getValue());
                }
            }
//            System.out.println(" ------------ Bindings: filter count=" + srf.size());
//            System.out.println(" ------------ Bindings: node =" + node.getName());
            EnumSet<DispatcherType> es = EnumSet.of(DispatcherType.REQUEST);
            if ("deployed".equals(node.getName())) {
                webapp.addFilter(JsfFilter.class, "/", es);
                for (Map.Entry<String, ? extends FilterRegistration> en : srf.entrySet()) {
                    if ("javax.faces.webapp.FacesServlet".equals(en.getValue().getClassName())) {
                        //regs = en.getValue().getMappings();
//                        System.out.println(" ------------ Bindings: after add filter key=" + en.getKey()
//                                + "; value=" + en.getValue());
                    }
                }

            }
            String[] wf = webapp.getWelcomeFiles();
            if (wf != null) {
                System.out.println("NODE = " + node.getName());                
                for (String s : wf) {
                    System.out.println("NODE = " + node.getName() + "; StandardBinding welcomeFile=" + s);
                }
            }

        }
    }
}
