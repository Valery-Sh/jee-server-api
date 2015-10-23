package org.netbeans.modules.jeeserver.ant.base.embedded;
import org.netbeans.modules.jeeserver.base.deployment.BaseJ2eePlatformImpl;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl;

/**
 * Mandatory factory class for producing J2eePlatformImpl. 
 * Plugin is required to register instance of this class in module layer in 
 * the J2EE/DeploymentPlugins/{plugin_name} folder.
 * 
 * @author V. Shyshkin
 */
public class EmbJ2eePlatformFactory extends J2eePlatformFactory {
    /**
     * Return J2eePlatformImpl for the given DeploymentManager.
     * @param manager deployment manager
     * @return J2eePlatformImpl
     */
    
    @Override
    public J2eePlatformImpl getJ2eePlatformImpl(DeploymentManager manager) {
        return new BaseJ2eePlatformImpl((BaseDeploymentManager) manager);
    }
}
