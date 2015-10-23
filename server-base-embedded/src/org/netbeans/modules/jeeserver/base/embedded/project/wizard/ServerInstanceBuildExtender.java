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
        //FileObject projFo = project.getProjectDirectory();
/*        FileObject d = projFo.getFileObject(SuiteConstants.INSTANCE_NBDEPLOYMENT_FOLDER);
        if (d != null) {
            updateNbDeploymentFile(d);
            return;
        }
*/        
//            FileObject toDir = projFo.createFolder(SuiteConstants.INSTANCE_NBDEPLOYMENT_FOLDER);
//            Properties props = new Properties();
        DistributedWebAppManager distManager = DistributedWebAppManager.getInstance(project);
        InstanceProperties ip = SuiteManager.getManager(project).getInstanceProperties();
        distManager.setServerInstanceProperty(BaseConstants.HTTP_PORT_PROP, ip.getProperty(BaseConstants.HTTP_PORT_PROP));
//            props.setProperty(BaseConstants.HTTP_PORT_PROP, ip.getProperty(BaseConstants.HTTP_PORT_PROP));
//            BaseUtil.storeProperties(props, toDir, SuiteConstants.INSTANCE_PROPERTIES_FILE);
    }

    public void updateNbDeploymentFile(FileObject nbDir) {
        DistributedWebAppManager distManager = DistributedWebAppManager.getInstance(project);

        //FileObject propsFo = nbDir.getFileObject(SuiteConstants.INSTANCE_PROPERTIES_FILE);
        InstanceProperties ip = SuiteManager.getManager(project).getInstanceProperties();
        distManager.setServerInstanceProperty(BaseConstants.HTTP_PORT_PROP, ip.getProperty(BaseConstants.HTTP_PORT_PROP));
        //BaseUtil.updateProperties(props, nbDir, SuiteConstants.INSTANCE_PROPERTIES_FILE);
    }

    public void disableExtender() {
/*        try {
            FileObject projFo = project.getProjectDirectory();
            FileObject toDelete = projFo.getFileObject(SuiteConstants.INSTANCE_NBDEPLOYMENT_FOLDER);
            if (toDelete != null) {
                toDelete.delete();
                ProjectManager.getDefault().saveProject(project);
            }
        } catch (IOException ex) {
            LOG.log(Level.INFO, ex.getMessage());
        }
*/
    }

}
