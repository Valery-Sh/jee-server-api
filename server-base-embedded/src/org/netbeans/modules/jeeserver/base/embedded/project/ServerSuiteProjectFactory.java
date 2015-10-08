/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jeeserver.base.embedded.project;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author V. Shyshkin
 */
@ServiceProvider(service=ProjectFactory.class)
public class ServerSuiteProjectFactory implements ProjectFactory{

    public ServerSuiteProjectFactory() {
    }

    public ServerSuiteProjectFactory(FileObject dir, ProjectState state) {
    }

    protected ServerSuiteProject getSuiteProject(FileObject dir, ProjectState state) {
        return new ServerSuiteProject(dir, state);
    }

    /**
     * Specifies when a project is a project, i.e., if the following
     * files are present in a folder:
     * <ul>
     *  <li>start.ini file;</li>
     *  <li>modules folder</li>
     *  <li>etc-cm folder</li>
     * </ul>
     * @param projectDirectory
     * @return 
     */
    @Override
    public boolean isProject(FileObject projectDirectory) {
        return projectDirectory.getFileObject("nbconfig/embedded-server-project-type.xml") != null ; 
    }

    /**
     * Specifies when the project will be opened, i.e., if the project exists.
     * @param dir
     * @param state
     * @return
     * @throws IOException 
     */
    @Override
    public Project loadProject(FileObject dir, ProjectState state) throws IOException {
        return isProject(dir) ? getSuiteProject(dir, state) : null;
    }
    
    
    @Override
    public void saveProject(final Project suite) throws IOException, ClassCastException {
        // leave unimplemented for the moment
    }
    
}
