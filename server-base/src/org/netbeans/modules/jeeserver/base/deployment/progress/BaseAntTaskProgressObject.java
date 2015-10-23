/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jeeserver.base.deployment.progress;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import static org.netbeans.modules.jeeserver.base.deployment.progress.AbstractProgressObject.RP;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Valery
 */
public class BaseAntTaskProgressObject implements Runnable {

    public static final Logger LOG = Logger.getLogger(BaseAntTaskProgressObject.class.getName());
    public static final String WAIT_TIMEOUT = "wait.finished.timeout";
    public static final String ANT_TARGET = "ant.target";
    public static final String BUILD_XML = "build.xml";
    
    private ExecutorTask task = null;

    final private Properties executeProperties;
    final BaseDeploymentManager manager;

    /**
     * Creates an instance of the class for a given deployment manager.
     *
     * @param manager the deployment manager the instance of the class is
     * created for.
     */
    public BaseAntTaskProgressObject(BaseDeploymentManager manager, Properties executeProperties) {
        this.executeProperties = executeProperties;
        this.manager = manager;
    }

    /**
     * Starts deployment process in the specified mode.
     *
     * @return this object
     */
    public RequestProcessor.Task execute() {
        return RP.post(this, 0, Thread.NORM_PRIORITY);
    }
    
    @Override
    public void run() {

        FileObject buildXml = FileUtil.toFileObject(new File(executeProperties.getProperty("build.xml")));

        String[] targets = new String[]{executeProperties.getProperty(ANT_TARGET)};
        String  waitProp = executeProperties.getProperty("wait.finished.timeout");
        long timeout = 0;
        if ( waitProp != null ) {
            timeout = Long.parseLong(waitProp);
        }
        try {
            task = ActionUtils.runTarget(buildXml, targets, executeProperties);
            if ( waitProp != null) {
                if ( timeout != 0 ) {
                    task.waitFinished(timeout);            
                } else {
                    task.waitFinished();
                }
            }
        } catch (InterruptedException | IOException | IllegalArgumentException ex) {
            LOG.log(Level.INFO, ex.getMessage());
        }
    }

    public ExecutorTask getTask() {
        return task;
    }

    public Properties getExecuteProperties() {
        return executeProperties;
    }


}//class
