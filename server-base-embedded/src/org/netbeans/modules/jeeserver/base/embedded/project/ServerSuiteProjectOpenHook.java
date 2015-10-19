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
package org.netbeans.modules.jeeserver.base.embedded.project;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteConstants;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteUtil;
import org.netbeans.modules.jeeserver.base.embedded.webapp.DistributedWebAppManager;

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
        String uid = SuiteUtil.getSuiteUID(projectDir);
        if ( uid == null ) {
            uid = createSuiteUID();
        } else {
            updateSuiteLocation(uid);
        }
        if ( true ) {
            return;
        }
        List<Project> projects = getServerInstances(uid);
        projects.forEach(p -> {
            DistributedWebAppManager.getInstance(p).refresh();
        });
        
    }
    
    protected void updateSuiteLocation(String uid) {
        String[] uris = Deployment.getDefault().getServerInstanceIDs();
        for ( String uri : uris) {
            if ( uri.endsWith(SuiteConstants.UID_PROPERTY_NAME + uid)) {
                InstanceProperties ip = InstanceProperties.getInstanceProperties(uri);
                String s = SuiteUtil.getSuiteProjectLocation(ip);
                if ( s == null ) {
                    continue;
                }
                Path oldPath = Paths.get(SuiteUtil.getSuiteProjectLocation(ip));
                Path newPath = Paths.get(projectDir.getPath());
                if ( newPath.equals(oldPath)) {
                    continue;
                }
                SuiteUtil.setSuiteProjectLocation(ip, newPath.toString());
            }
            
            
        }
    }
    
    protected List<Project> getServerInstances(String uid) {
        String[] uris = Deployment.getDefault().getServerInstanceIDs();
        List<Project> result = new ArrayList<>();
        
        for ( String uri : uris) {
            if ( uri.endsWith(SuiteConstants.UID_PROPERTY_NAME + uid)) {
                InstanceProperties ip = InstanceProperties.getInstanceProperties(uri);
                String s = BaseUtil.getServerLocation(ip);
                if ( s == null ) {
                    continue;
                }
                FileObject projDir = FileUtil.toFileObject(new File(s));
                if ( projDir != null ) {
                    Project proj = FileOwnerQuery.getOwner(projDir);
                    if ( proj != null ) {
                        result.add(proj);
                    }
                }
            }
        }
        return result;
    }
    
    protected String createSuiteUID() {
        String uid = null;
        FileObject suitePropsFo = projectDir.getFileObject(SuiteConstants.SUITE_PROPERTIES_LOCATION);
        Properties suiteProps = new Properties();
        try {
            if (suitePropsFo != null) {
                suiteProps = BaseUtil.loadProperties(projectDir.getFileObject(SuiteConstants.SUITE_PROPERTIES_LOCATION));
                uid = suiteProps.getProperty(SuiteConstants.UID_PROPERTY_NAME);
                if (uid != null) {
                    return uid;
                }
                suitePropsFo.delete();
                uid = UUID.randomUUID().toString();
            } else {
                uid = UUID.randomUUID().toString();
            }
            suiteProps.setProperty(SuiteConstants.UID_PROPERTY_NAME, uid);
            BaseUtil.storeProperties(suiteProps, projectDir.getFileObject(SuiteConstants.SUITE_CONFIG_FOLDER),
                    SuiteConstants.SUITE_PROPERTIES);
        } catch (IOException ex) {
            LOG.log(Level.INFO, "ServerSuiteProjectOpenHook. {0}", ex.getMessage());
        }
        return uid;
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
