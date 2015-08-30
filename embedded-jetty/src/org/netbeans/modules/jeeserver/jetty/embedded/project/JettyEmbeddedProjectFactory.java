/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jeeserver.jetty.embedded.project;

import org.netbeans.modules.jeeserver.base.embedded.project.EmbServerProjectFactory;
import org.netbeans.modules.jeeserver.base.embedded.project.EmbeddedProject;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author V. Shyshkin
 */
@ServiceProvider(service=ProjectFactory.class)
public class JettyEmbeddedProjectFactory extends EmbServerProjectFactory{

    public JettyEmbeddedProjectFactory() {
    }

    public JettyEmbeddedProjectFactory(FileObject dir, ProjectState state) {
        super(dir, state);
    }

    @Override
    protected EmbeddedProject getEmbeddedProject(FileObject dir, ProjectState state) {
        return new JettyEmbeddedProject(dir, state);
    }
    
}
