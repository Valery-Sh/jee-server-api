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

import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.status.DeploymentStatus;

/**
 *
 * @author V. Shyshkin
 */
public class BaseDeploymentStatus implements DeploymentStatus {

    private final ActionType action;
    private final CommandType command;
    private final StateType state;
    private final String message;
    
    /** 
     * Creates a new instance of the class for the given action type,
     * the command type, the state type and the message.
     * @param action
     * @param command
     * @param state
     * @param message
     */
    public BaseDeploymentStatus(ActionType action, CommandType command, StateType state, String message) {
        this.action = action;
        this.command = command;
        this.state = state;
        this.message = message;
        
    }
    /**
     * Retrieve any additional information about the status of this event.
     * @return additional information about the status of this event.
     */
    @Override
    public String getMessage() {
        return message;
    }
    /**
     * Retrieve the StateType value.
     * @return the StateType object
     */
    @Override
    public StateType getState() {
        return state;
    }
    /**
     * Retrieve the deployment  CommandType value.
     * @return the CommandType object
     */
    @Override
    public CommandType getCommand() {
        return command;
    }
    /**
     * Retrieve the deployment  ActionType value.
     * @return the ActionType object
     */
    @Override
    public ActionType getAction() {
        return action;
    }
    /**
     * A convenience method to report if the operation is in the running state.
     * @return {@code true} if the state equals to  {@code StateType.RUNNING}
     */
    @Override
    public boolean isRunning() {
        return StateType.RUNNING.equals(state);
    }
    /**
     * A convenience method to report if the operation is in the failed state.
     * @return true if this command has failed
     */
    @Override
    public boolean isFailed() {
        return StateType.FAILED.equals(state);
    }
    /**
     * A convenience method to report if the operation is in the completed state.
     * @return true if this command has completed successfully
     */
    @Override
    public boolean isCompleted() {
        return StateType.COMPLETED.equals(state);
    }

    
}
