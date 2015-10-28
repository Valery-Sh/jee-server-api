package org.netbeans.modules.jeeserver.base.embedded;

import java.util.logging.Logger;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.jeeserver.base.deployment.config.AbstractModuleConfiguration;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.netbeans.modules.jeeserver.base.embedded.project.SuiteManager;
import org.netbeans.modules.jeeserver.base.embedded.webapp.DistributedWebAppManager;

/**
 *
 * @author V. Shyshkin
 */
public abstract class EmbeddedModuleConfiguration extends AbstractModuleConfiguration {

    private static final Logger LOG = Logger.getLogger(EmbeddedModuleConfiguration.class.getName());

    protected EmbeddedModuleConfiguration(J2eeModule module, String[] contextFilePaths) {
        super(module, contextFilePaths);
    }
    /**
     * The server calls this method when it is done using this
     * ModuleConfiguration instance.
     */
    @Override
    public void dispose() {
        BaseUtil.out("EmbeddedModuleConfiguration dispose ");
        // notifyAvailableModule(serverInstanceId, true);
        //
        // Ebedded servers
        //
        notifyDistributedWebAppChange(serverInstanceId, true); // false means old assigned server                
    }
/*    @Override
    protected void notifyCreate() {
        BaseUtil.out("EmbeddedModuleConfiguration notifyCreate module=" + this.getJ2eeModule().getUrl());
        
        notifyAvailableModule(serverInstanceId, false); // false means new assigned server
        //
        // Ebedded servers
        //
        notifyDistributedWebAppChange(serverInstanceId, false); // false means new assigned server        
    }
*/    
    @Override
    protected void notifyServerChange(String oldInstanceId, String newInstanceId) {
        BaseUtil.out("EmbeddedModuleConfiguration notifyServerChange oldInstanceId=" + oldInstanceId + "; newInstanceId=" + newInstanceId);

//        notifyAvailableModule(oldInstanceId, true);
//        notifyAvailableModule(newInstanceId, false);
        //
        // Ebedded servers
        //
        notifyDistributedWebAppChange(oldInstanceId, true); // true means old assigned server        
        notifyDistributedWebAppChange(newInstanceId, false); // false means new assigned server        

    }
    
    protected void notifyDistributedWebAppChange(String serverInstanceId, boolean dispose) {
        BaseUtil.out("EmbeddedModuleConfiguration notifyDistributedWebAppChange serverInstanceId=" + serverInstanceId + "; dispose=" + dispose);
        if (serverInstanceId == null || webProject == null || ! SuiteManager.isEmbeddedServer(serverInstanceId) ) {
            return;
        }
        DistributedWebAppManager distManager = DistributedWebAppManager
                .getInstance(SuiteManager
                .getManager(serverInstanceId)
                .getServerProject());
        if ( dispose && distManager.isRegistered(webProject)) {
            //
            // old server
            //
            distManager.unregister(webProject);
        } else if ( ! dispose && ! distManager.isRegistered(webProject)) {
            //
            // new server
            //
            distManager.register(webProject);
        }
    }
}
