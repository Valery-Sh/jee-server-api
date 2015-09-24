/**
 * This file is part of Jetty Server support in NetBeans IDE.
 *
 * Jetty Server support in NetBeans IDE is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the License,
 * or (at your option) any later version.
 *
 * Jetty Server support in NetBeans IDE is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should see the GNU General Public License here:
 * <http://www.gnu.org/licenses/>.
 */
package org.netbeans.modules.jeeserver.base.embedded.server.project;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.base.deployment.ServerInstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteConstants;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteUtil;

import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Allows to hook open and close project actions.
 */
public class ServerSuiteProjectOpenHook extends ProjectOpenedHook {

    private static final Logger LOG = Logger.getLogger(ServerSuiteProjectOpenHook.class.getName());

    private final FileObject projectDir;
    //private final ServerInstanceProperties serverProperties;

    public ServerSuiteProjectOpenHook(FileObject projectDir) {
        this.projectDir = projectDir;
    }

    @Override
    protected void projectOpened() {
        Project p = getProject();
        String suiteLocation = p.getProjectDirectory().getPath();
        //BaseDeploymentManager dm = SuiteUtil.managerOf(p);
        InstanceContexts contexts = p.getLookup().lookup(InstanceContexts.class);

        Deployment d = Deployment.getDefault();

        if (d == null || d.getServerInstanceIDs() == null) {
            return;
        }

        for (String uri : d.getServerInstanceIDs()) {
            InstanceProperties ip = InstanceProperties.getInstanceProperties(uri);
            String instanceLocation = ip.getProperty(BaseConstants.SERVER_LOCATION_PROP);
            
            if (instanceLocation == null) {
                continue;
            }
            Project instanceProject = FileOwnerQuery.getOwner(FileUtil.toFileObject(new File(instanceLocation)));
            if (instanceProject == null) {
                continue;
            }
            String ipSuiteLocation = ip.getProperty(SuiteConstants.SUITE_PROJECT_LOCATION);
            if ( ipSuiteLocation == null ) {
                continue;
            }
            if (Paths.get(suiteLocation).equals(Paths.get(ipSuiteLocation))) {
BaseUtils.out("projectOpened: " + ip.getProperty(BaseConstants.URL_PROP));
                contexts.put(ip.getProperty(BaseConstants.URL_PROP));
            }
        }
        
/*        FileObject[] instances = projectDir.getFileObject(SuiteConstants.SERVER_INSTANCES_FOLDER).getChildren();
        Project p = getProject();
        InstanceContexts contexts = p.getLookup().lookup(InstanceContexts.class);
        for ( FileObject fo : instances) {
            if ( ! fo.isFolder() ) {
                continue;
            }
            contexts.put(fo);
        }
*/        
    }

    @Override
    protected void projectClosed() {
        if (projectDir == null) {
            return;
        }

        ProjectManager.getDefault().clearNonProjectCache();
        try {
            ProjectManager.getDefault().saveProject(getProject());
        } catch (IOException ex) {
            LOG.log(Level.INFO, ex.getMessage());
        }
    }

    protected Project getProject() {
        return FileOwnerQuery.getOwner(projectDir);
    }

}
