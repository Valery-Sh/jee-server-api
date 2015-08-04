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
package org.netbeans.modules.jeeserver.base.deployment.specifics;

import java.awt.Image;
import java.util.Map;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.plugins.spi.FindJSPServlet;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;

/**
 *
 * @author V. Shyshkin
 */
public interface ServerSpecifics {
    
    boolean pingServer(Project serverProject);
    boolean shutdownCommand(Project serverProject);
    String execCommand(Project serverProject, String cmd);
    
    /**
     * 
     * @param dm
     * @return May return null.
     */
    FindJSPServlet getFindJSPServlet(DeploymentManager dm);
    Image getProjectImage(Project serverProject);

    /**
     *
     * @param projDir
     * @param props
     */
    void projectCreated(FileObject projDir,Map<String,Object> props);
    boolean needsShutdownPort();
    int getDefaultPort();
    int getDefaultDebugPort();
    int getDefaultShutdownPort();

    /**
     *
     * @param manager
     */
    default void serverStarted(DeploymentManager manager) {
        
    }
    default void serverStarting(DeploymentManager manager) {
        
    }
    
    WizardDescriptorPanel getAddonCreateProjectPanel(WizardDescriptor wiz);
    
}
