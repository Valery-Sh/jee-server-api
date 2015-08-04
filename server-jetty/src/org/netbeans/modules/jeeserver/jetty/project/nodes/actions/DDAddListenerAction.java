package org.netbeans.modules.jeeserver.jetty.project.nodes.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static javax.swing.Action.NAME;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.base.deployment.actions.StartServerAction;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;

import org.netbeans.modules.jeeserver.jetty.util.JettyConstants;
import org.netbeans.modules.jeeserver.jetty.util.StartIni;
import org.netbeans.modules.jeeserver.jetty.util.Utils;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Valery
 */
public abstract class DDAddListenerAction extends AbstractAction implements ContextAwareAction {

    private static final Logger LOG = Logger.getLogger(DDAddListenerAction.class.getName());

    private static final RequestProcessor RP = new RequestProcessor(DDAddListenerAction.class);

    @Override
    public void actionPerformed(ActionEvent e) {
        assert false;
    }

    @Override
    public Action createContextAwareInstance(Lookup context) {
        assert false;
        return null;
    }

    /**
     *
     * @param serverProj
     * @param webProject
     * @param context a Lookup of the {@code FileObject} which represents the
     * inner web project directory.
     * @return
     */
    public static Action getDDAddListenerAction(Project serverProj, Project webProject) {
        return new DDAddListenerAction.ContextAction(serverProj, webProject);
    }

    private static final class ContextAction extends AbstractAction {

        private final Project webProject;
        private final Project serverProject;

        public ContextAction(final Project serverProject, final Project webProject) {
            this.webProject = webProject;
            this.serverProject = serverProject;
            
            File f = Paths.get(serverProject.getProjectDirectory().getPath(), JettyConstants.JETTY_START_INI).toFile();

            StartIni startIni = new StartIni(f);
            
            String jsfModule = startIni.getEnabledJsfModuleName();
            boolean enabled  = jsfModule != null && ! DDHelper.hasJsfListener(serverProject, webProject);
            setEnabled(enabled);

            //putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
            String s = isEnabled() ? "(" + jsfModule + ") " : ""; 
            putValue(NAME, "Add Listener " + s + " to web.xml ");

        }

        public @Override
        void actionPerformed(ActionEvent e) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    DDHelper.addJsfListener(serverProject, webProject);
                }
            });
        }//class
    }//class
}
