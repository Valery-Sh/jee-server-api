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
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.base.deployment.BaseTargetModuleID;
import org.netbeans.modules.j2ee.deployment.plugins.spi.DeploymentContext;
import org.netbeans.modules.jeeserver.base.deployment.BaseTarget;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author V. Shyshkin
 */
public class BaseIncrementalProgressObject extends BaseDeployProgressObject {

    public BaseIncrementalProgressObject(BaseDeploymentManager manager) {
        super(manager);
    }
    /**
     * Starts web module.
     *
     * @param module
     * @return 
     */
    public BaseIncrementalProgressObject start(BaseTargetModuleID module) {
        return start(module, false);
    }
    public BaseIncrementalProgressObject start(BaseTargetModuleID module, boolean completeImmediately) {
        BaseUtils.out("BaseIncrementalProgressObject.start completeImmeduatly=" + completeImmediately);
        
        setTargetModuleID(module);
        setCompleteImmediately(completeImmediately);
        
        command = "start";
        this.setTargetModuleID(module);
        setMode(getManager().getCurrentDeploymentMode());
        if ( completeImmediately ) {
BaseUtils.out("BaseIncrementalProgressObject.start is completeImmeduatly RETURN");
            
            fireCompleted(CommandType.START, getManager().getDefaultTarget().getName());            
            return this;
        }
        fireRunning(CommandType.START, getManager().getDefaultTarget().getName());
        requestProcessor().post(this, 0, Thread.NORM_PRIORITY);
        return this;
    }

    /**
     * Stops web module.
     * @param module
     * @return 
     */
    public BaseIncrementalProgressObject stop(BaseTargetModuleID module) {
        return this.stop(module,false);
    }
    public BaseIncrementalProgressObject stop(BaseTargetModuleID module, boolean immediately) {
        setTargetModuleID(module);
        setCompleteImmediately(immediately);
        command = "stop";
        this.setTargetModuleID(module);
        setMode(getManager().getCurrentDeploymentMode());
        fireRunning(CommandType.STOP, getManager().getDefaultTarget().getName());
        requestProcessor().post(this, 0, Thread.NORM_PRIORITY);
        return this;
    }

    public ProgressObject initialDeploy(Target target, DeploymentContext context) {
        BaseTargetModuleID module = getManager().getModule(FileUtil.toFileObject(context.getModuleFile()));
        return deploy(module);
    }
    
}
