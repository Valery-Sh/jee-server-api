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
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.netbeans.modules.jeeserver.jetty.deploy.JettyServerPlatformImpl;

/**
 *
 * @author V. Shyshkin
 */
public class JettyServerPlatformFactory extends J2eePlatformFactory {
    
    private static JettyServerPlatformFactory platformFactory;
    
    private JettyServerPlatformFactory() {
        //BaseUtil.out("JettyServerPlatformFactory CONSTRUCTOR ");
    }
    
    public static JettyServerPlatformFactory getInstance() {
        if ( platformFactory == null ) {
            platformFactory = new JettyServerPlatformFactory();
        }
        return platformFactory;
    }
    
    @Override
    public J2eePlatformImpl getJ2eePlatformImpl(DeploymentManager manager) {
        return JettyServerPlatformImpl.getInstance((BaseDeploymentManager) manager);
    }
    
}
