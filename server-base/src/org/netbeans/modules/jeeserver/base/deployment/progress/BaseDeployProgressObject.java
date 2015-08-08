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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.spi.TargetModuleID;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.base.deployment.BaseTarget;
import org.netbeans.modules.jeeserver.base.deployment.BaseTargetModuleID;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Pair;

/**
 * Starts and tracks a deployment process in {@literal DISTRIBUTE} mode.
 *
 * @see AbstractProgressObject
 * @see BaseRunProgressObject
 * @see BaseStopProgressObject
 *
 * @author V. Shyshkin
 */
public class BaseDeployProgressObject extends AbstractProgressObject {

    private static final Logger LOG = Logger.getLogger(BaseDeployProgressObject.class.getName());

    protected String command;

    /**
     * Creates an instance of the class for a given deployment manager.
     *
     * @param manager the deployment manager the instance of the class is
     * created for.
     */
    public BaseDeployProgressObject(BaseDeploymentManager manager) {
        super(manager);
    }

    /**
     * Starts deployment process in the {@literal DISTRIBUTE} mode.
     *
     * @param module a module that represents a web application to be
     * distributed.
     * @return this object
     */
    public BaseDeployProgressObject deploy(BaseTargetModuleID module) {
        return deploy(module, false);
    }

    public BaseDeployProgressObject deploy(BaseTargetModuleID module, boolean completeImmediately) {
        command = "deploy";
        this.setTargetModuleID(module);
        setCompleteImmediately(completeImmediately);
        setMode(getManager().getCurrentDeploymentMode());
        fireRunning(CommandType.DISTRIBUTE, getManager().getDefaultTarget().getName());
        requestProcessor().post(this, 10, Thread.NORM_PRIORITY);
        return this;
    }

    public BaseDeployProgressObject undeploy(BaseTargetModuleID module, FileObject projDir) {
        return undeploy(module, projDir, false);
    }

    public BaseDeployProgressObject undeploy(BaseTargetModuleID module, FileObject projDir, boolean completeImmediately) {
        command = "undeploy";
        this.setTargetModuleID(module);
        setCompleteImmediately(completeImmediately);
        setMode(getManager().getCurrentDeploymentMode());
        BaseUtils.out("command = 'undeploy'");
        fireRunning(CommandType.UNDEPLOY, getManager().getDefaultTarget().getName());
        requestProcessor().post(this, 0, Thread.NORM_PRIORITY);
        return this;
    }

    public BaseDeployProgressObject destroy(BaseTargetModuleID module, FileObject projDir) {
        return destroy(module, projDir, false);
    }

    public BaseDeployProgressObject destroy(BaseTargetModuleID module, FileObject projDir, boolean completeImmediately) {
        command = "destroy";
        this.setTargetModuleID(module);
        setCompleteImmediately(completeImmediately);
        setMode(getManager().getCurrentDeploymentMode());
        BaseUtils.out("command = 'destroy'");
        fireRunning(CommandType.UNDEPLOY, getManager().getDefaultTarget().getName());
        requestProcessor().post(this, 0, Thread.NORM_PRIORITY);
        return this;
    }

    public BaseDeployProgressObject redeploy(BaseTargetModuleID oldModule) {
        return redeploy(oldModule, false);
    }

    public BaseDeployProgressObject redeploy(BaseTargetModuleID oldModule, boolean completeImmediately) {
BaseUtils.out("BaseDeployProgressObject REDEPLOY completeImmediately=" + completeImmediately);
        command = "redeploy";
        BaseTarget target = getManager().getDefaultTarget();
        FileObject projDir = FileUtil.toFileObject(new File(oldModule.getProjectDir()));

        String contextPath = WebModule.getWebModule(projDir).getContextPath();
        BaseTargetModuleID newModule = BaseTargetModuleID.getInstance(getManager(), target, contextPath, projDir.getPath());

        this.setOldTargetModuleID(oldModule);
        this.setTargetModuleID(newModule);
        setCompleteImmediately(completeImmediately);
        setMode(getManager().getCurrentDeploymentMode());
        fireRunning(CommandType.REDEPLOY, getManager().getDefaultTarget().getName());
        requestProcessor().post(this, 0, Thread.NORM_PRIORITY);
        return this;
    }

    public BaseDeployProgressObject redeploy(BaseTargetModuleID oldModule, BaseTargetModuleID newModule) {
        return redeploy(oldModule, newModule,false);
    }

    public BaseDeployProgressObject redeploy(BaseTargetModuleID oldModule,BaseTargetModuleID newModule, boolean completeImmediately) {
        command = "redeploy";

        this.setOldTargetModuleID(oldModule);
        this.setTargetModuleID(newModule);
        setCompleteImmediately(completeImmediately);
        setMode(getManager().getCurrentDeploymentMode());
        fireRunning(CommandType.REDEPLOY, getManager().getDefaultTarget().getName());
        requestProcessor().post(this, 0, Thread.NORM_PRIORITY);
        return this;
    }
    
    
    
    /**
     * Returns an array with a single element. The element of the array is the
     * one that accepted as a parameter of null null null null null null null     {@link #deploy(BaseTargetModuleID, org.openide.filesystems.FileObject)  
     *
     * @return an array
     */
    @Override
    public TargetModuleID[] getResultTargetModuleIDs() {
        return new TargetModuleID[]{getTargetModuleID()};
    }

    @Override
    public void run() {
        String command = this.command;
BaseUtils.out("BaseDeployProgressObject RUN completeImmediately=" + isCompleteImmediately());
        if (!isCompleteImmediately()) {
BaseUtils.out("BaseDeployProgressObject RUN EXECUTE SEEVER COMMAND");            
            //
            // actual execution
            //
            executeServerCommand();
        }
        CommandType commandType = CommandType.DISTRIBUTE;
        switch (command) {
            case "deploy":
                onDeploy();
                commandType = CommandType.DISTRIBUTE;
                break;
            case "undeploy":
                onUndeploy();
                commandType = CommandType.UNDEPLOY;
                break;
            case "redeploy":
                onRedeploy();
                commandType = CommandType.REDEPLOY;
                break;
            case "start":
                commandType = CommandType.START;
                break;
            case "stop":
                commandType = CommandType.STOP;
                break;
        }
        try {
            fireCompleted(commandType, getManager().getDefaultTarget().getName());
        } catch (Throwable e) {
            BaseUtils.out("EXCEPTION!");
            LOG.log(Level.INFO, e.getMessage());
        }

    }

    protected void executeServerCommand() {
        if (!getManager().pingServer()) {
            return;
        }
        getManager().getSpecifics().execCommand(getManager().getServerProject(), createCommand());
    }

    /**
     * Translates a context path string into
     * <code>application/x-www-form-urlencoded</code> format.
     */
    private static String encode(String str) {
        try {
            StringTokenizer st = new StringTokenizer(str, "/"); // NOI18N
            if (!st.hasMoreTokens()) {
                return str;
            }
            StringBuilder result = new StringBuilder();
            while (st.hasMoreTokens()) {
                result.append("/").append(URLEncoder.encode(st.nextToken(), "UTF-8")); // NOI18N
            }
            return result.toString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e); // this should never happen
        }
    }

    protected String createCommand() {
        StringBuilder sb = new StringBuilder();
        sb.append("cmd=");
        sb.append(command);
        sb.append("&cp=");
        sb.append(encode(getTargetModuleID().getContextPath()));
        sb.append("&dir=");
        sb.append(encode(getTargetModuleID().getProjectDir()));

        if ("redeploy".equals(command)) {
            sb.append("&oldcp=");
            sb.append(encode(getOldTargetModuleID().getContextPath()));
            sb.append("&olddir=");
            sb.append(encode(getOldTargetModuleID().getProjectDir()));
        }
        return sb.toString();
    }

    private void onDeploy() {
        if (isCompleteImmediately()) {
            return;
        }

        List<Pair<BaseTargetModuleID, BaseTargetModuleID>> modules = getManager().getInitialDeployedModulesOld();
        Project wp = FileOwnerQuery.getOwner(FileUtil.toFileObject(new File(getTargetModuleID().getProjectDir())));

        int i = 0;
        for (Pair<BaseTargetModuleID, BaseTargetModuleID> pair : modules) {
            if (pair.first().getContextPath().equals(getTargetModuleID().getContextPath())) {
                modules.set(i, Pair.of(getTargetModuleID(), (BaseTargetModuleID) null));
                return;
            }
            i++;
        }
        i = 0;
        for (Pair<BaseTargetModuleID, BaseTargetModuleID> pair : modules) {
            if (pair.first().getProjectDir().equals(getTargetModuleID().getProjectDir())) {
                modules.set(i, Pair.of(getTargetModuleID(), (BaseTargetModuleID) null));
                return;
            }
            i++;
        }

        modules.add(Pair.of(getTargetModuleID(), (BaseTargetModuleID) null));
        //      }
    }

    private void onRedeploy() {
        onUndeploy();
        onDeploy();

    }

    private void onUndeploy() {
        if (isCompleteImmediately()) {
            return;
        }
        List<Pair<BaseTargetModuleID, BaseTargetModuleID>> modules = getManager().getInitialDeployedModulesOld();

        String cpdel = getTargetModuleID().getContextPath();
        int i = 0;
        for (Pair<BaseTargetModuleID, BaseTargetModuleID> p : modules) {
            String cp = p.first().getContextPath();
            //String dir = p.first().getProjectDir();
            if (cpdel.equals(cp)) {
                modules.remove(i);
                break;
            }
            i++;
        }
    }

}//class
