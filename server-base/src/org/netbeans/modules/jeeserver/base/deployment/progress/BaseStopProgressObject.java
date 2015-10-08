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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.base.deployment.specifics.ServerSpecifics;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.util.NbBundle;

/**
 * Stops running server and tracks the process.
 *
 * @see ESAbstractProgressObject
 * @see ESDeployProgressObject
 * @see ESRunProgressObject
 * @see BaseStopProgressObject
 * @author V. Shyshkin
 */
public class BaseStopProgressObject extends AbstractProgressObject {

    public static final Logger LOG = Logger.getLogger(BaseStopProgressObject.class.getName());

    /**
     * Creates an instance of the class for a given deployment manager.
     *
     * @param manager the deployment manager the instance of the class is
     * created for.
     */
    public BaseStopProgressObject(BaseDeploymentManager manager) {
        super(manager);
    }

    /**
     * Stops deployment process.
     *
     * @return this object
     */
    public BaseStopProgressObject execute() {
        setMode(null);
        // Needs only to create DeploymentStatus
        setStatusStopRunning(getManager().getDefaultTarget().getName());
        RP.post(this, 0, Thread.NORM_PRIORITY);
        return this;

    }

    private boolean httpRequestShutdown() {
        ServerSpecifics s = getManager().getSpecifics();
        return s.shutdownCommand(getManager());
    }

    @Override
    public void run() {
        if (getManager().getServerTask() == null) {
            // For future release?
            stopStartedExternally(); // For example using 'Run' menu item
        } else {
            stopStartedFromIDE();
        }
    }

    protected boolean tryStop() {
        ExecutorTask task = getManager().getServerTask();
        if (task == null || task.isFinished()) {
            return true;
        }

        boolean shutdown = httpRequestShutdown();
        //
        // We don't know for sure wether the server developer provides code
        // that supports shutdown handler?
        //
        if (!shutdown) {
            try {
                getManager().getServerTask().stop();
                shutdown = getManager().getServerTask().waitFinished(BaseConstants.SERVER_TIMEOUT_DELAY);
            } catch (InterruptedException ex) {
                BaseUtil.out("STOP SERVER EXCEPTION");
                shutdown = false;
            }
        }
        return shutdown;
    }

    protected void stopStartedFromIDE() {
        final String msg = getManager().getDefaultTarget().getName();
        if (tryStop()) {
            getManager().setServerTask(null);
            getManager().setCurrentDeploymentMode(null);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                LOG.log(Level.INFO, ex.getMessage());
            }
            getManager().getInstanceProperties().refreshServerInstance();
            setStatusStopCompleted(msg);
            getManager().getInstanceProperties().refreshServerInstance();

        } else {
            setStatusStopFailed(msg);
        }

    }
    /**
     * For future release ?.
     */
    protected void stopStartedExternally() {

        httpRequestShutdown();

        long timeout = System.currentTimeMillis() + 5000;
        boolean success = true;

        while (true) {
            //NotificationDisplayer.getDefault().notify("RUNNING", null, "Running...", null);                    
            if (getManager().pingServer()) {
                // give server a few secs to finish its shutdown, not responding
                // does not necessarily mean its is still not running
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                }
            } else {
                break;
            }
            setStatusStopRunning(getManager().getDefaultTarget().getName());
            if (System.currentTimeMillis() > timeout) {
                success = false;
                break;
            }
        }//while

        String msg = ProjectUtils.getInformation(getManager().getServerProject()).getName();
        if (!success && !hasProgressListener()) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(
                    NbBundle.getMessage(BaseStopProgressObject.class, "MSG_STOP", msg),
                    (int) NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }

        if (success) {

            getManager().setCurrentDeploymentMode(null);
            getManager().getInstanceProperties().refreshServerInstance();
            setStatusStopCompleted(msg);

        } else {
            setStatusStopFailed(msg);
        }

    }
}
