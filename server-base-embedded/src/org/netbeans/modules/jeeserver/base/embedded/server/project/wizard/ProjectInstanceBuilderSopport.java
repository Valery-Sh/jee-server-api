/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jeeserver.base.embedded.server.project.wizard;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Valery
 */
public class ProjectInstanceBuilderSopport extends AbstractProjectInstanceBuilderSupport{
    
    public ProjectInstanceBuilderSopport(FileObject serverSuiteDir, boolean mavenBased) {
        super(serverSuiteDir, mavenBased);
    }

/*    public static ServerProjectInstanceIterator createIterator() {
        return new ServerProjectInstanceIterator();
    }
*/
    @Override
    public void instantiate() throws IOException {
        //final Set<FileObject> fileObjectSet = new LinkedHashSet<>();
        instantiateProjectDir();
        instantiateInstanceProperties();
        runInstantiateServerInstanceDir();
        
    }
    
}
