/**
 * This file is part of Base JEE Server support in NetBeans IDE.
 *
 * Base JEE Server support in NetBeans IDE is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * Base JEE Server support in NetBeans IDE is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should see the GNU General Public License here:
 * <http://www.gnu.org/licenses/>.
 */
package org.netbeans.modules.jeeserver.base.deployment;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;

import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;

/**
 * Each Server project contains a instance of this class in its Lookup.
 * The presence of such an object in a project lookup means that the project is 
 * a Server. In addition it contains a set of configuration properties.
 * 
 * @author V. Shyshkin
 */
public class ServerInstanceProperties {
    
    private static final Logger LOG = Logger.getLogger(ServerInstanceProperties.class.getName());
    private Deployment.Mode currentDeploymentMode;
    private String uri;
    private String serverId;
    private String actualServerId;
    
    
    /**
     * Returns the current deployment mode. 
     * The value is assigned when the method {@link ESDeploymentManager#setCurrentDeploymentMode(org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment.Mode) 
     * is invoked.
     * 
     * @return an object of type {@literal Deployment.Mode}
     */
    public Deployment.Mode getCurrentDeploymentMode() {
        return currentDeploymentMode;
    }
    /**
     * Set the current deployment mode. 
     * The value is assigned when the method {@link ESDeploymentManager#setCurrentDeploymentMode(org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment.Mode) 
     * is invoked.
     * 
     * @param currentDeploymentMode  an object of type {@literal Deployment.Mode}
     *  that represents the current deployment mode. 
     */
    public void setCurrentDeploymentMode(Deployment.Mode currentDeploymentMode) {
        this.currentDeploymentMode = currentDeploymentMode;
    }
    /**
     * Returns the server identifier.
     * @return serverIdentifier. For example embedded jetty server identifier
     * is a string value "jetty9".
     */
    public String getServerId() {
        return serverId;
    }
    
    
    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getActualServerId() {
        return actualServerId;
    }

    public void setActualServerId(String actualServerId) {
        this.actualServerId = actualServerId;
    }

    /**
     * Return a string prefix that is used to create a unique server URI.
     * @return server URI prefix. For example: "jetty9:deploy:server"
     */
    public String getUriPrefix() {
        return getServerId() + BaseConstants.URIPREFIX_NO_ID;
    }
    /**
     * Returns a string URI that uniquely identifies a server.
     * @return a string URI of the server
     */
    public String getUri() {
        return uri;
    }
    /**
     * Returns a deployment manager that manages the server project it
     * represents.
     * @return an object of type {@literal BaseDeploymentManager}
     */
    public BaseDeploymentManager getManager()  {
        BaseDeploymentManager dm = null;
        try {
            dm = (BaseDeploymentManager) DeploymentFactoryManager.getInstance().getDisconnectedDeploymentManager(getUri());
        } catch (DeploymentManagerCreationException ex) {
            LOG.log(Level.INFO, ex.getMessage());
        }
        return dm;
    }
    /**
     * The method is used by {@link ESServerIconAnnotator}.
     * The value returned may affect only the visual representation of the
     * project in the Explore View. And the method uses a very simple criteria
     * to determine the state of the server.
     * 
     * @return {@literal true} if the property {@link #currentDeploymentMode}
     *  is not {code null} and {@literal false} otherwise.
     */
    public boolean isServerRunning() {
        return currentDeploymentMode != null;
    }

    /**
     * Returns a string representation of the server's {@literal http port number}.
     * @return an {@literal http port}
     */
    public String getHttpPort() {
        return getManager().getInstanceProperties().getProperty(BaseConstants.HTTP_PORT_PROP);
    }
    /**
     * Returns a string representation of the server's {@literal remote debug port number}.
     * @return a {@literal remote debug port}
     */
    public String getDebugPort() {
       return getManager().getInstanceProperties().getProperty(BaseConstants.DEBUG_PORT_PROP);
    }
    /**
     * Returns a string representation of the server's {@literal remote shutdown port number}.
     * @return a {@literal remote shutdown port}. May be null.
     */
    public String getShutdownPort() {
       return getManager().getInstanceProperties().getProperty(BaseConstants.SHUTDOWN_PORT_PROP);
    }
    
    /**
     * Returns  the server's host name.
     * @return the server's host name.
     */
    public String getHost() {
       return getManager().getInstanceProperties().getProperty(BaseConstants.HOST_PROP);
    }
    /**
     * Returns the server project's directory.
     * @return an absolute path of the project's directory
     */
    public String getServerProjectLocation() {
       return getManager().getInstanceProperties().getProperty(BaseConstants.SERVER_LOCATION_PROP);
    }

    /**
     * Returns the server project's directory.
     * @return an absolute path of the project's directory
     */
    public String getHomeDir() {
       return getManager().getInstanceProperties().getProperty(BaseConstants.HOME_DIR_PROP);
    }
    /**
     * Returns the server version.
     * @return server version. May return null.
     */
    public String getServerVersion() {
       return getManager().getInstanceProperties().getProperty(BaseConstants.SERVER_VERSION_PROP);
    }
    
    public String getDisplayName() {
       return getManager().getInstanceProperties().getProperty(BaseConstants.DISPLAY_NAME_PROP);
    }
}
