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
package org.netbeans.modules.jeeserver.base.embedded.project;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.jeeserver.base.deployment.ServerInstanceProperties;

import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceCreationException;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.specifics.ServerSpecifics;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
//import org.netbeans.modules.jeeserver.jetty.project.actions.PropertiesAction;
//import org.netbeans.modules.jeeserver.jetty.util.Utils;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;

/**
 * Allows to hook open and close project actions.
 */
public class EmbeddedProjectOpenHook extends ProjectOpenedHook {

    private static final Logger LOG = Logger.getLogger(EmbeddedProjectOpenHook.class.getName());
    
    private final  FileObject projectDir;
    private final ServerInstanceProperties serverProperties;

    public EmbeddedProjectOpenHook(FileObject projectDir, ServerInstanceProperties serverProperties) {
        this.projectDir = projectDir;
        this.serverProperties = serverProperties;
        
    }

    @Override
    protected void projectOpened() {
        
        String uri = serverProperties.getUri();

        try {
            InstanceProperties ip = InstanceProperties.getInstanceProperties(uri);
            if (ip == null) {
                Map<String, String> map = getDefaultPropertyMap(projectDir);
                ip = InstanceProperties.createInstanceProperties(uri, null, null,  projectDir.getNameExt(), map);
                // to update with InstanceProperties. 
                // (The method getLookup() is oveeridden for JettyProject)
                FileOwnerQuery.getOwner(projectDir).getLookup().lookup(ServerInstanceProperties.class);                
                //Action a = new PropertiesAction().createContextAwareInstance(getProject().getLookup());                
//                if (ip.getProperty(BaseConstants.HOME_DIR_PROP) == null) {
//                    PropertiesAction.perform(getProject().getLookup());
//                }
            }
            
        } catch (InstanceCreationException ex) {
            LOG.log(Level.INFO, ex.getMessage());
        }
    }

    private Map<String, String> getDefaultPropertyMap(FileObject projectDir) {
        Map<String, String> map = new HashMap<>();
        map.put(BaseConstants.SERVER_ID_PROP, serverProperties.getServerId());
        map.put(BaseConstants.URL_PROP, serverProperties.getUri());
        map.put(BaseConstants.HOST_PROP, "localhost");
        ServerSpecifics spec = BaseUtils.getServerSpecifics(serverProperties.getServerId());
        map.put(BaseConstants.DEBUG_PORT_PROP, String.valueOf(spec.getDefaultDebugPort()));
        map.put(BaseConstants.DISPLAY_NAME_PROP, projectDir.getNameExt());
        map.put(BaseConstants.SERVER_LOCATION_PROP, projectDir.getPath());
        return map;
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
