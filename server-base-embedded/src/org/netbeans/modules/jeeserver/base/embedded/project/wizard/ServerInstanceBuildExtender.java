/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jeeserver.base.embedded.project.wizard;

import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.embedded.project.SuiteManager;
import org.netbeans.modules.jeeserver.base.embedded.webapp.DistributedWebAppManager;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Valery
 */
public class ServerInstanceBuildExtender {

    private static final Logger LOG = Logger.getLogger(ServerInstanceAntBuildExtender.class.getName());

    protected final Project project;

    public ServerInstanceBuildExtender(Project project) {
        this.project = project;
    }

    /**
     * Creates or updates the build script extension.
     */
    public void enableExtender() {
        updateNbDeploymentFile();
    }

    public void updateNbDeploymentFile() {
        DistributedWebAppManager distManager = DistributedWebAppManager.getInstance(project);
        InstanceProperties ip = SuiteManager.getManager(project).getInstanceProperties();
        distManager.setServerInstanceProperty(BaseConstants.HTTP_PORT_PROP, ip.getProperty(BaseConstants.HTTP_PORT_PROP));
        String shutdownPort = ip.getProperty(BaseConstants.SHUTDOWN_PORT_PROP);
        if (shutdownPort == null) { // Cannot be
            shutdownPort = String.valueOf(Integer.MAX_VALUE);
        }
        distManager.setServerInstanceProperty(BaseConstants.SHUTDOWN_PORT_PROP, shutdownPort);        
        
//            props.setProperty(BaseConstants.HTTP_PORT_PROP, ip.getProperty(BaseConstants.HTTP_PORT_PROP));
//            BaseUtil.storeProperties(props, toDir, SuiteConstants.INSTANCE_PROPERTIES_FILE);
    }

    public void updateNbDeploymentFile(FileObject nbDir) {
        DistributedWebAppManager distManager = DistributedWebAppManager.getInstance(project);

        //FileObject propsFo = nbDir.getFileObject(SuiteConstants.INSTANCE_PROPERTIES_FILE);
        InstanceProperties ip = SuiteManager.getManager(project).getInstanceProperties();
        distManager.setServerInstanceProperty(BaseConstants.HTTP_PORT_PROP, ip.getProperty(BaseConstants.HTTP_PORT_PROP));
        //BaseUtil.updateProperties(props, nbDir, SuiteConstants.INSTANCE_PROPERTIES_FILE);
        String shutdownPort = ip.getProperty(BaseConstants.SHUTDOWN_PORT_PROP);
        if (shutdownPort == null) { // Cannot be
            shutdownPort = String.valueOf(Integer.MAX_VALUE);
        }
        distManager.setServerInstanceProperty(BaseConstants.SHUTDOWN_PORT_PROP, shutdownPort);        
        
    }

    public void disableExtender() {
    }

}
