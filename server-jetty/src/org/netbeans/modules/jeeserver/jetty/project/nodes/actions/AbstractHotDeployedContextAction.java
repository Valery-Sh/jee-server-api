/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jeeserver.jetty.project.nodes.actions;

import java.awt.event.ActionEvent;
import java.util.Properties;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.swing.AbstractAction;
import static javax.swing.Action.NAME;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.base.deployment.actions.StartServerAction;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.netbeans.modules.jeeserver.jetty.util.Utils;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Valery
 */
public abstract class AbstractHotDeployedContextAction extends AbstractAction {
    
    private static final Logger LOG = Logger.getLogger(BaseHotDeployedContextAction.class.getName());
    private static final RequestProcessor RP = new RequestProcessor(AbstractHotDeployedContextAction.class);

    protected static final String CONTEXTPATH = "contextPath";
    protected static final String WAR = "war";

    protected Project project;
    protected BaseDeploymentManager manager;
    protected Properties contextProps;
    protected Lookup context;
    protected String command;

    public AbstractHotDeployedContextAction(Lookup context, String command) {
        this.command = command;
        this.context = context;
        init();
    }
    
    protected final void init() {
        boolean isJettyServer;// = false;
        setEnabled(false);
        putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);

        FileObject fo = context.lookup(FileObject.class);
        if (fo != null && FileOwnerQuery.getOwner(fo) != null) {
            project = FileOwnerQuery.getOwner(fo);
            isJettyServer = Utils.isJettyServer(project);
            if (isJettyServer) {
                loadManager();

                contextProps = getContextProperties(fo);
                
                boolean isActionEnabled = isActionEnabled();
                
                setEnabled(isActionEnabled);
                putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, !isJettyServer);
            }
        }

        putValue(NAME, "&" + getMenuItemName());
        
    }
    protected static ProgressObject startServer(BaseDeploymentManager dm, Project serverProject, ProgressListener l) {
        ProgressObject result = null;
        if (!dm.isServerRunning()) {
            result = StartServerAction.perform(serverProject.getLookup());
            result.addProgressListener(l);
        }
        return result;
    }
    
    protected boolean isServerRunning() {
        return manager != null && !manager.isStopped();        
    }
    
    protected abstract Properties getContextProperties(FileObject webFo);
    
    protected abstract String getMenuItemName();
    
    protected boolean isActionEnabled() {
        return true;
    }
    
    private void loadManager() {
        manager = BaseUtils.managerOf(project);
    }

    public @Override
    void actionPerformed(ActionEvent e) {
        //RequestProcessor rp = new RequestProcessor("Server Action", 1);
        final RequestProcessor.Task task = RP.post(() -> {
            runActionPerformed();
        }, 0, Thread.NORM_PRIORITY);

    }
    
    protected abstract void runActionPerformed();
    
    protected void executeServerCommand(String command, Properties props) {
        if (!manager.pingServer()) {
            return;
        }

        manager.getSpecifics().execCommand(project, createCommand(command, props));
    }

    /**
     *
     * @param command may be one of
     * {@code "starthotdeployed", "stophotdeployed", "getstate"}
     * @param props
     * @return
     */
    protected String createCommand(String command, Properties props) {
        StringBuilder sb = new StringBuilder();
        sb.append("cmd=");
        sb.append(command);
        sb.append("&cp=");
        sb.append(BaseUtils.encode(props.getProperty(CONTEXTPATH)));
        sb.append("&dir=");
        sb.append(BaseUtils.encode(props.getProperty(WAR)));

        return sb.toString();
    }

}
