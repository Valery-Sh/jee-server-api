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
package org.netbeans.modules.jeeserver.base.deployment.ide;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.jeeserver.base.deployment.INFO;
import org.netbeans.modules.jeeserver.base.deployment.BaseTarget;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;

/**
 * Server lifecycle services from the IDE. The server will use these
 * services to start or stop server during deployment or debugging or profiling
 * execution.
 *
 * @author V.Shyshkin
 */
public class BaseStartServer extends StartServer {

    private static final Logger LOG = Logger.getLogger(BaseStartServer.class.getName());
    
    private final BaseDeploymentManager manager;

    /**
     * Creates an instance of the class for a given
     * {@literal DeploymentManager}.
     *
     * @param manager the deployment manager
     */
    public BaseStartServer(BaseDeploymentManager manager) {
        this.manager = manager;
    }

    /**
     * Determines whether the {@code admin server} is also the given target server.
     *
     * @param target the target server in question; could be null.
     * @return always returns {@literal true}
     */
    @Override
    public boolean isAlsoTargetServer(Target target) {
        return true;
    }

    /**
     * Determine whether the server can be started.
     *
     * @return always returns true
     */
    @Override
    public boolean supportsStartDeploymentManager() {
        return true;
    }

    /**
     * Determine whether the specified target server can be started in the debug
     * mode
     *
     * @param target the target server
     * @return always returns true
     */
    @Override
    public boolean supportsStartDebugging(Target target) {
        return true;
    }

    /**
     * Determine whether the specified target server can be started in the
     * profile mode
     *
     * @param target the target server
     * @return always returns true
     */
    @Override
    public boolean supportsStartProfiling(Target target) {
        return true;
    }

    /**
     * Start or restart the target in profile mode.
     *
     * @param target the target server
     * @return Progress object to monitor progress on start operation
     */
    @Override
    public ProgressObject startProfiling(Target target) {
        INFO.log("----------------------------------------------");
        INFO.log("-               START PROFILING              -");
        INFO.log("----------------------------------------------");
        if (isRunning() && manager.getCurrentDeploymentMode() == Deployment.Mode.PROFILE ) {
            ProgressObject po = stopDeploymentManager();
            while ( ! (po.getDeploymentStatus().isCompleted() || po.getDeploymentStatus().isFailed()) ) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    LOG.log(Level.INFO, ex.getMessage());
                }
                
            }
        }
        return manager.startServerProfile();
    }

    /**
     * Start or restart the target in normal mode.
     *
     * @return Progress object to monitor progress on start operation
     */
    @Override
    public synchronized ProgressObject startDeploymentManager() {
        INFO.log("----------------------------------------------");
        INFO.log("-            START DEPLOYMENT MANAGER         -");
        INFO.log("----------------------------------------------");
        return manager.startServer();
    }

    /**
     * Stops the server.
     *
     * @return Progress object to monitor progress on stop operation
     */
    @Override
    public ProgressObject stopDeploymentManager() {
        INFO.log("----------------------------------------------");
        INFO.log("-            STOP DEPLOYMENT MANAGER         -");
        INFO.log("----------------------------------------------");
        return manager.stopServer();
    }
    boolean needsRestart;

    /**
     * Determines whether the specified server must restart if already running.
     * Always return {@literal false}.
     *
     * @param target
     * @return {@literal false}
     */
    @Override
    public boolean needsRestart(Target target) {
        return false;
    }

    /**
     * Always return {@literal false}.
     *
     * @return {@literal false}
     */
    @Override
    public boolean needsStartForConfigure() {
        return false;
    }

    /**
     * Always return {@literal false}.
     *
     * @return {@literal false}
     */
    @Override
    public boolean needsStartForTargetList() {
        return false;
    }

    /**
     * Always return {@literal false}.
     *
     * @return {@literal false}
     */
    @Override
    public boolean needsStartForAdminConfig() {
        return false;
    }
    
    
    /**
     * Returns the running state of the server. Checks the server state by
     * socket ping.
     *
     * @return {@literal true} if the server is running. {@literal false}
     * otherwise
     */
    @Override
    public boolean isRunning() {
        return manager.isServerRunning();
    }
    /**
     * Determines whether the given target is in debug mode. The server is not
     * in debug mode if at least one of the following conditions are met:
     * <ul>
     * <li>The server is not running</li>
     * <li>manager.currentDeploymentMode == null</li>
     * <li>manager.currentDeploymentMode == Deployment.Mode.RUN</li>
     * <li>manager.currentDeploymentMode == Deployment.Mode.PROFILE</li>
     * </ul>
     *
     * @param target
     * @return {@literal true} if the server is in debug mode. {@literal false}
     * otherwise
     */
    @Override
    public boolean isDebuggable(Target target) {
        if (!isRunning() || manager.getCurrentDeploymentMode() == null
                || manager.getCurrentDeploymentMode() == Deployment.Mode.RUN
                || manager.getCurrentDeploymentMode() == Deployment.Mode.PROFILE) {
            return false;

        }
        return true;
    }

    /**
     * Start or restart the target in debug mode.
     *
     * @param target the target server
     * @return Progress object to monitor progress on start operation
     */
    @Override
    public ProgressObject startDebugging(Target target) {
        INFO.log("----------------------------------------------");
        INFO.log("-              START DEBUGGING               -");
        if (target != null) {
            INFO.log("-      target.name=" + target.getName() + "  -");
        }
        if (target != null && (target instanceof BaseTarget)) {
            INFO.log("-      target.uri=" + ((BaseTarget) target).getUri() + "  -");
        }
        INFO.log("----------------------------------------------");
        return manager.startServerDebug();
    }

    /**
     * Returns the host/port necessary for connecting to the server's debug
     * information.
     *
     * @param target target server;
     * @return Returns the host/port necessary for connecting to the server's
     * debug information.
     */
    @Override
    public ServerDebugInfo getDebugInfo(Target target) {
        String host = manager.getInstanceProperties().getProperty(BaseConstants.HOST_PROP);
        String debugPort = manager.getInstanceProperties().getProperty(BaseConstants.DEBUG_PORT_PROP);
        return new ServerDebugInfo(
                host,
                Integer.parseInt(debugPort));
    }
}
