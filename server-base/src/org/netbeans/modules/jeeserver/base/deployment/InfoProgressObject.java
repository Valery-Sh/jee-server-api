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
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.openide.util.NbBundle;

/**
 *
 * @author V. Shyshkin
 */
public class InfoProgressObject implements ProgressObject{
    
    private BaseDeploymentStatus status;
    
    public InfoProgressObject(BaseDeploymentStatus status) {
        this.status = status;
    }
    public InfoProgressObject(boolean failed) {
        String msg = null;
        if ( failed ) {
            msg = NbBundle.getMessage(InfoProgressObject.class, "MSG_Server_Failed");
        }
        status = new BaseDeploymentStatus(ActionType.EXECUTE, CommandType.START, failed ? StateType.FAILED : StateType.COMPLETED, msg);        
    }
    
    @Override
    public TargetModuleID[] getResultTargetModuleIDs() {
        return new TargetModuleID[] {};
    }

    @Override
    public ClientConfiguration getClientConfiguration(TargetModuleID arg0) {
        return null;
    }

    @Override
    public boolean isCancelSupported() {
        return false;
    }

    @Override
    public void cancel() throws OperationUnsupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isStopSupported() {
        return false;
    }

    @Override
    public void stop() throws OperationUnsupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addProgressListener(ProgressListener arg0) {
    }

    @Override
    public void removeProgressListener(ProgressListener arg0) {
    }

    @Override
    public DeploymentStatus getDeploymentStatus() {
        return status;
    }

}
