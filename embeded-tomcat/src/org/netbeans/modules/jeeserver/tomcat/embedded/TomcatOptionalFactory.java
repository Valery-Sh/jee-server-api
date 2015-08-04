/**
 * This file is part of Tomcat Server Embedded support in NetBeans IDE.
 *
 * Tomcat Server Embedded support in NetBeans IDE is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * Tomcat Server Embedded suppport in NetBeans IDE is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should see the GNU General Public License here:
 * <http://www.gnu.org/licenses/>.
 */
package org.netbeans.modules.jeeserver.tomcat.embedded;

import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.deployment.plugins.spi.FindJSPServlet;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.spi.OptionalDeploymentManagerFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
import org.netbeans.modules.jeeserver.base.deployment.ide.BaseOptionalDeploymentFactory;

/**
 *
 * @author Valery
 */
public class TomcatOptionalFactory   extends OptionalDeploymentManagerFactory{
    private final BaseOptionalDeploymentFactory delegate;
    
    public TomcatOptionalFactory() {
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
    public FindJSPServlet getFindJSPServlet(DeploymentManager dm) {
        return delegate.getFindJSPServlet(dm);
    }
    
    
}
