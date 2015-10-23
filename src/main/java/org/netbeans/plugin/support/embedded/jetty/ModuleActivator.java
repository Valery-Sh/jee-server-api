/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.plugin.support.embedded.jetty;

import org.eclipse.jetty.deploy.DeploymentManager;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.Configuration;

/**
 *
 * @author V. Shyshkin
 */
public interface ModuleActivator {

    void configServer(Server server);

    public static class Annotations implements ModuleActivator {

        @Override
        public void configServer(Server server) {
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

    }//class Annotations
    public static class WeldCDI implements ModuleActivator {

        @Override
        public void configServer(Server server) {
            DeploymentManager dm = CommandManager.getInstance().getHotDeployer().getDeployer();
            if ( dm == null ) {
                return;
            }
            dm.addLifeCycleBinding(new org.eclipse.jetty.cdi.servlet.WeldDeploymentBinding());
            Configuration.ClassList classlist = Configuration.ClassList
                    .setServerDefault(server);
            classlist.addAfter(
                    "org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
                    "org.embedded.ide.jetty.WebNbCdiConfig");            
        }
    }//class WeldCDI    
    public static class JSF implements ModuleActivator {

        @Override
        public void configServer(Server server) {
            Configuration.ClassList classlist = Configuration.ClassList
                    .setServerDefault(server);
            classlist.addAfter(
                    "org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
                    "org.embedded.ide.jetty.WebNbJsfConfig");            
        }
    }//class JSF
    
}
