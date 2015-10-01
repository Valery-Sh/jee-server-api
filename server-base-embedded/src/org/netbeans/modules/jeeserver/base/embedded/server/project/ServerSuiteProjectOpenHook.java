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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;

import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;

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
