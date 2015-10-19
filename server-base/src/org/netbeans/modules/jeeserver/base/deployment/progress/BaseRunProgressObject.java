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
package org.netbeans.modules.jeeserver.base.deployment.progress;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.StateType;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.deployment.specifics.StartServerPropertiesProvider;

import org.netbeans.modules.jeeserver.base.deployment.INFO;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.profiler.api.ProfilerSupport;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;

import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Starts and tracks a deployment process in {@literal RUN, DEBUG} or
 * {@literal PROFILE} mode.
 *
 * @see AbstractProgressObject
 * @see BaeDeployProgressObject
 * @see BaseStopProgressObject
 *
 * @author V. Shyshkin
 */
public class BaseRunProgressObject extends AbstractProgressObject {

    public static final Logger LOG = Logger.getLogger(BaseRunProgressObject.class.getName());

    /**
     * Creates an instance of the class for a given deployment manager.
     *
     * @param manager the deployment manager the instance of the class is
     * created for.
     */
    public BaseRunProgressObject(BaseDeploymentManager manager) {
        super(manager);
    }

    /**
     * Starts deployment process in the specified mode.
     *
     * @param toMode the mode to start deployment.
     * @return this object
     */
    public BaseRunProgressObject execute(Deployment.Mode toMode) {
        setMode(toMode);
        // Needs only to create DeploymentStatus
        setStatusStartRunning("");
        RP.post(this, 0, Thread.NORM_PRIORITY);
        return this;
    }

    public static String stateOf(int state) {
        String s = "NULL";
        switch (state) {
            case ProfilerSupport.STATE_BLOCKING:
                s = "STATE_BLOCKING";
                break;
            case ProfilerSupport.STATE_INACTIVE:
                s = "STATE_INACTIVE";
                break;
            case ProfilerSupport.STATE_PROFILING:
                s = "STATE_PROFILING";
                break;
            case ProfilerSupport.STATE_RUNNING:
                s = "STATE_RUNNING";
                break;
            case ProfilerSupport.STATE_STARTING:
                s = "STATE_STARTING";
                break;
        }
        return s;
    }

    @Override
    public void run() {
        String msg = getManager().getDefaultTarget().getName();
        String httpPort = getManager().getInstanceProperties().getProperty(BaseConstants.HTTP_PORT_PROP);

        StateType result = StateType.FAILED;

        getManager().setServerTask(null);
        getManager().setWaiting(false);

        ExecutorTask task = null;

        String serverDir = getManager().getServerProjectDirectory().getPath();
        File f = new File(serverDir);
        Project project = getManager().getServerProject();

        Properties props = new Properties();
        FileObject buildXml = FileUtil.toFileObject(f).getFileObject("build.xml");
        String[] targets = new String[]{"run"};
        StartServerPropertiesProvider propProvider = getManager().getLookup().lookup(StartServerPropertiesProvider.class);

        if (propProvider != null) {
            buildXml = propProvider.getBuildXml(project);
        }
        if (getMode() == Deployment.Mode.RUN) {
            if (propProvider != null) {
                props = propProvider.getStartProperties(project);
                targets[0] = props.getProperty("target");
                //targets[0] = propProvider.getStartProperties(project).getProperty("target");
            }

        } else if (getMode() == Deployment.Mode.DEBUG) {
            if (propProvider != null) {
                props = propProvider.getDebugProperties(project);
                targets[0] = propProvider.getDebugProperties(project).getProperty("target");
            } else {
                props = new Properties();
                props.setProperty("server.debug.port", getManager().getInstanceProperties().getProperty(BaseConstants.DEBUG_PORT_PROP));
                props.setProperty("server.debug.transport", "dt_socket");
                targets = new String[]{"debug-embedded-server"};
            }
        } else if (getMode() == Deployment.Mode.PROFILE) {
            if (propProvider != null) {
                props = propProvider.getProfileProperties(project);
                targets = new String[]{"profile"};
                props.remove("target");
            } else {
                props = new Properties();
                targets = new String[]{"profile-embedded-server"};
                String args = BaseUtil.getProfileArgs(getManager());
                props.setProperty("profiler.args", args);
            }
        }
        try {
            task = ActionUtils.runTarget(buildXml, targets, props);
        } catch (IOException | IllegalArgumentException ex) {
//        } catch (Exception ex) {
            LOG.log(Level.INFO, ex.getMessage());
        }

        long timeout = System.currentTimeMillis() + BaseConstants.SERVER_TIMEOUT_DELAY;

        if (getMode() == Deployment.Mode.PROFILE) {

            while (true) {
                int state = ProfilerSupport.getState();
                INFO.log("** RUN Mode.PROFILE state=" + stateOf(state));
                if (state == ProfilerSupport.STATE_BLOCKING || getManager().pingServer()) {
                    getManager().setServerTask(task);
                    getManager().setWaiting(true);
                    result = StateType.COMPLETED;
                    break;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    LOG.log(Level.INFO, ex.getMessage());
                }
                if (System.currentTimeMillis() > timeout) {
                    break;
                }
                setStatusStartRunning(msg);

            }//while
        }//if getMode() == Deployment.Mode.PROFILE && currentMode == null
        else {
            while (true) {
                if (getManager().pingServer()) {
                    // server started successfully
                    getManager().setServerTask(task);
                    result = StateType.COMPLETED;
                    break;
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    LOG.log(Level.INFO, ex.getMessage());
                }
                if (System.currentTimeMillis() > timeout) {
                    break;
                }
                setStatusStartRunning(msg);

            }//while
        }
        if (result == StateType.COMPLETED) {
            getManager().setCurrentDeploymentMode(getMode());

        } else if (result == StateType.FAILED) {
            if (task != null && !task.isFinished()) {
                task.stop();
                task.waitFinished();
                getManager().setServerTask(null);
                msg = msg + ". "
                        + NbBundle.getMessage(AbstractProgressObject.class, "MSG_CHECK_HTTP_PORT", httpPort);
            }
        }

        if (result == StateType.FAILED) {
            setStatusStartFailed(msg);
        } else if (result == StateType.COMPLETED) {
            setStatusStartCompleted(msg);
        }
        if (result == StateType.RUNNING) {
            setStatusStartRunning(msg);
        }
        BaseDeploymentManager dm = getManager();
        dm.getSpecifics().serverStarted(dm);

    }
}//class
