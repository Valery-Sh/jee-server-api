/**
 * This file is part of Jetty Server support in NetBeans IDE.
 *
 * Jetty Server support in NetBeans IDE is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * Jetty Server support in NetBeans IDE is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should see the GNU General Public License here:
 * <http://www.gnu.org/licenses/>.
 */
package org.netbeans.modules.jeeserver.jetty.deploy.ide;

import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.jeeserver.base.deployment.ide.BaseOptionalDeploymentFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.FindJSPServlet;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.spi.OptionalDeploymentManagerFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.jetty.deploy.JettyServerFindJspServlet;
import org.netbeans.modules.jeeserver.jetty.project.template.JettyAddInstanceIterator;
import org.openide.WizardDescriptor;
import org.openide.util.Exceptions;

/**
 *
 * @author V. Shyshkin
 */
public class JettyServerOptionalFactory   extends OptionalDeploymentManagerFactory{
    private final BaseOptionalDeploymentFactory delegate;
    
    public JettyServerOptionalFactory() {
        delegate = new BaseOptionalDeploymentFactory();
    }
    @Override
    public StartServer getStartServer(DeploymentManager manager) {
        return delegate.getStartServer(manager);
    }

    @Override
    public IncrementalDeployment getIncrementalDeployment(DeploymentManager dm) {
        return delegate.getIncrementalDeployment(dm);
    }
    @Override
    protected void finalize() {
        try {
            super.finalize();
//BaseUtils.out(" FFFFFFFFFFFFF Finalize");
        } catch (Throwable ex) {
            Exceptions.printStackTrace(ex);
        }
        
    }
    @Override
    public FindJSPServlet getFindJSPServlet(DeploymentManager dm) {
        
        return new JettyServerFindJspServlet((BaseDeploymentManager) dm);
    }
    
    @Override
    public WizardDescriptor.InstantiatingIterator getAddInstanceIterator() {
        return new JettyAddInstanceIterator();
    }
}
