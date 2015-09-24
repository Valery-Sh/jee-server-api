/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jeeserver.base.embedded;

import java.io.File;
import org.netbeans.modules.jeeserver.base.deployment.FactoryDelegate;
import org.netbeans.modules.jeeserver.base.deployment.specifics.ServerSpecifics;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteConstants;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Valery
 */
public class EmbeddedFactoryDelegate extends FactoryDelegate{

    public EmbeddedFactoryDelegate(String serverId, ServerSpecifics specifics) {
        super(serverId, specifics);
    }
    /**
     * Determine whether a server exists under the specified location.
     *
     * @param instanceFO an absolute path of the server project directory
     * @return {@literal true } if the server project exists. {@literal false}
     * otherwise
     */
    @Override
    protected boolean existsServer(FileObject instanceFO) {

        if ( super.existsServer(instanceFO)) {
            return true;
        }
        String suiteDir = (String) instanceFO.getAttribute(SuiteConstants.SUITE_PROJECT_LOCATION);        

        if (suiteDir == null) {
            return false;
        }

        if (! new File(suiteDir).exists()) {
            return false;
        }
        
        return true;
    }
    
}
