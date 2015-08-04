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

import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.base.deployment.BaseTargetModuleID;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerProgress;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author V. Shyshkin
 */
public abstract class AbstractProgressObject extends ServerProgress implements Runnable{
    private final BaseDeploymentManager manager;
    private static RequestProcessor requestProcessor;
    private Deployment.Mode mode;
    
    private BaseTargetModuleID targetModuleID;
    private BaseTargetModuleID oldTargetModuleID; 
    private boolean completeImmediately;
    private int listenerCount;
    /**
     * Returns an instance of RequestProcessor.
     * @return 
     */
    protected static synchronized RequestProcessor requestProcessor() {
        if (requestProcessor == null) {
            requestProcessor = new RequestProcessor("Server processor", 1);
        }
        return requestProcessor;
    }
    /**
     * Create a new instance of the class for a specified deployment manager.
     * @param manager 
     */
    public AbstractProgressObject(BaseDeploymentManager manager) {
        super(manager);
        
        this.manager = manager;
        listenerCount = 0;
    }
    public void fireRunning(CommandType command,String message) {
        notify(createProgressEvent(command, modifyMessage(command, message),StateType.RUNNING));
    }
    public void fireCompleted(CommandType command,String message) {
        notify(createProgressEvent(command, modifyMessage(command, message),StateType.COMPLETED));
    }
    public void fireFailed(CommandType command,String message) {
        notify(createProgressEvent(command, modifyMessage(command, message),StateType.FAILED));
    }
    
    protected ProgressEvent createProgressEvent(CommandType command,String message,StateType state) {
        return new ProgressEvent(this, getTargetModuleID(), createDeploymentStatus(command, message, state));
    }
    /**
     * Report event to any registered listeners.
     *
     * @param message
     * @param command an object of type {@code CommandType}. May be one of
     * {@code START, STOP, DISTRIBUTE}.
     * @return 
     */
    protected String modifyMessage(CommandType command, String message) {

        String msg = NbBundle.getMessage(BaseRunProgressObject.class, "MSG_DEPLOYING", message);
        //boolean value = true;
        if (mode == Deployment.Mode.RUN && CommandType.START == command) {
            msg = NbBundle.getMessage(AbstractProgressObject.class, "MSG_START", message);
        }
        if (mode == Deployment.Mode.DEBUG && CommandType.START == command) {
            msg = NbBundle.getMessage(AbstractProgressObject.class, "MSG_START_DEBUGGING", message);
        }
        if (mode == Deployment.Mode.PROFILE) {
            msg = NbBundle.getMessage(AbstractProgressObject.class, "MSG_PROFILE", message);
        }

        if (CommandType.STOP == command) {
            msg = NbBundle.getMessage(AbstractProgressObject.class, "MSG_STOP", message);
            //value = false;
        }
        return msg;
    }
    

    @Override
    public abstract void run();
    
    public boolean hasProgressListener() {
        return listenerCount > 0;
    }
    @Override
    public void addProgressListener(ProgressListener pol) {
        super.addProgressListener(pol);
        listenerCount++;
    }

    @Override
    public void removeProgressListener(ProgressListener pol) {
        super.addProgressListener(pol);
        listenerCount--;        
    }
    
    @Override
    public boolean isCancelSupported() {
        return false;
    }
    /**
     * Returns the value of the the deployment mode, 
     * in which the process is running.
     * 
     * @return an object of type {@code Deployment.Mode}. May be one of
     * {@code RUN, DEBUG, PROFILE}
     * 
     */
    public Deployment.Mode getMode() {
        return mode;
    }
    /**
     * Set the value of the the deployment mode, 
     * in which the process is running.
     * 
     * @param an object of type {@code Deployment.Mode}. May be one of
     * {@code RUN, DEBUG, PROFILE}
     * 
     */
    public void setMode(Deployment.Mode mode) {
        this.mode = mode;
    }
    /**
     * @return an object of type {@code BaseDeploymentManager} for which  
     * this object is created.
     */
    public BaseDeploymentManager getManager() {
        return manager;
    }
    /**
     * Determines whether an http port is in use.
     * The port obtained from the deployment manager is used
     * to check.
     * @return {@code true} if port is in use. {@code false} otherwise.
     */
    protected boolean isHttpPortBusy() {
        return getManager().pingServer();
    }
    /**
     * Returns the value of the property {@code targetModuleId}.
     * @return the object of type {@code BaseTargetModuleID}
     */
    public BaseTargetModuleID getTargetModuleID() {
        return targetModuleID;
    }
    /**
     * Assigns the given value to the property {@code targetModuleId}.
     * @param targetModuleID a value to be set
     */
    public void setTargetModuleID(BaseTargetModuleID targetModuleID) {
        this.targetModuleID = targetModuleID;
    }
    public BaseTargetModuleID getOldTargetModuleID() {
        return oldTargetModuleID;
    }

    public void setOldTargetModuleID(BaseTargetModuleID oldTargetModuleID) {
        this.oldTargetModuleID = oldTargetModuleID;
    }

    public boolean isCompleteImmediately() {
        return completeImmediately;
    }

    public void setCompleteImmediately(boolean completeImmediately) {
        this.completeImmediately = completeImmediately;
    }
}//class
