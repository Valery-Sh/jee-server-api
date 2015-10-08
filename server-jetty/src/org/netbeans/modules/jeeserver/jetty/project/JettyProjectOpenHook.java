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
package org.netbeans.modules.jeeserver.jetty.project;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.jeeserver.base.deployment.ServerInstanceProperties;

import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceCreationException;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.netbeans.modules.jeeserver.jetty.project.actions.PropertiesAction;
import org.netbeans.modules.jeeserver.jetty.util.Utils;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Allows to hook open and close project actions.
 */
public class JettyProjectOpenHook extends ProjectOpenedHook {

    private static final Logger LOG = Logger.getLogger(JettyProjectOpenHook.class.getName());

    private final FileObject projectDir;
    private final ServerInstanceProperties serverProperties;

    public JettyProjectOpenHook(FileObject projectDir, ServerInstanceProperties serverProperties) {
        this.projectDir = projectDir;
        this.serverProperties = serverProperties;
    }

    @Override
    protected void projectOpened() {
        String uri = Utils.buildUri(projectDir);
        BaseUtil.out("projectOpened uri=" + uri);
        try {
            InstanceProperties ip = InstanceProperties.getInstanceProperties(uri);
            if (ip == null) {
                BaseUtil.out("projectOpened 1");

                Map<String, String> map = getDefaultPropertyMap();
                ip = InstanceProperties.createInstanceProperties(uri, null, null, projectDir.getNameExt(), map);
                // to update with InstanceProperties. 
                // (The method getLookup() is oveeridden for JettyProject)
                FileOwnerQuery.getOwner(projectDir).getLookup().lookup(ServerInstanceProperties.class);
                //Action a = new PropertiesAction().createContextAwareInstance(getProject().getLookup());                
            }
            FileObject fo = null;
            String homeDir = ip.getProperty(BaseConstants.HOME_DIR_PROP);
            if (homeDir != null) {
                fo = FileUtil.toFileObject(new File(homeDir));
            }

            if (fo == null || !fo.isFolder() || fo.getFileObject("bin") == null || fo.getFileObject("lib") == null) {
                Project p = FileOwnerQuery.getOwner(projectDir);
                if ( ! PropertiesAction.performAndModify(getProject().getLookup()) ) {
                    
                    OpenProjects.getDefault().close(new Project[] {FileOwnerQuery.getOwner(projectDir)});
                    InstanceProperties.removeInstance(uri);
                    ServerInstanceProperties sip = p.getLookup().lookup(ServerInstanceProperties.class);
                    if ( sip != null ) {
                        sip.setValid(false);
                    }
                    
                    throw new InstanceCreationException("Invalid jetty home.dir");
                } else {
                    ServerInstanceProperties sip = p.getLookup().lookup(ServerInstanceProperties.class);
                    if ( sip != null ) {
                        sip.setValid(true);
                    }
                }
            }

            BaseUtil.out("projectOpened 2");

        } catch (InstanceCreationException ex) {
            LOG.log(Level.INFO, ex.getMessage());
            OpenProjects.getDefault().close(new Project[] {FileOwnerQuery.getOwner(projectDir)});            
        }
    }

    private Map<String, String> getDefaultPropertyMap() {
        return Utils.getDefaultPropertyMap(projectDir);
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
