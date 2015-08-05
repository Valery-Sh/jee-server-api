/**
 * This file is part of Base JEE Server support in NetBeans IDE.
 *
 * Base JEE Server support in NetBeans IDE is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * Base JEE Server support in NetBeans IDE is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should see the GNU General Public License here:
 * <http://www.gnu.org/licenses/>.
 */
package org.netbeans.modules.jeeserver.base.deployment.ide;

import org.netbeans.modules.jeeserver.base.deployment.BaseIncrementalDeployment;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.spi.FindJSPServlet;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.spi.OptionalDeploymentManagerFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;

/**
 *
 * @author V. Shyshkin
 */
public class BaseOptionalDeploymentFactory extends OptionalDeploymentManagerFactory {

    /**
     * Create {@literal StartServer} for given {@literal DeploymentManager).
     * }@param manager a deployment manager the {@literal StartServer} is
     * created for.
     *
     * @return
     */
    @Override
    public StartServer getStartServer(DeploymentManager manager) {
        return new BaseStartServer((BaseDeploymentManager) manager);
    }
    /**
     * 
     * @param dm
     * @return 
     */
    @Override
    public IncrementalDeployment getIncrementalDeployment(DeploymentManager dm) {
        //BaseUtils.out("getIncrementalDeployment ");
        boolean b = false;
        InstanceProperties ip = InstanceProperties.getInstanceProperties(((BaseDeploymentManager) dm).getUri());
        if (ip == null) {
            b = true;
        } else {
            String s = ip.getProperty(BaseConstants.INCREMENTAL_DEPLOYMENT);
            if (s == null || "true".equals(s)) {
                b = true;
            }
        }
        return b ? new BaseIncrementalDeployment(dm) : null;
    }

    /**
     * Create {@literal FindJSPServlet} for given {@literal DeploymentManager}.
     *
     * @param dm a deployment manager the {@literal FindJSPServlet} is created
     * for.
     * @return
     */
    @Override
    public FindJSPServlet getFindJSPServlet(DeploymentManager dm) {
        return ((BaseDeploymentManager) dm).getSpecifics().getFindJSPServlet(dm);
    }

}
