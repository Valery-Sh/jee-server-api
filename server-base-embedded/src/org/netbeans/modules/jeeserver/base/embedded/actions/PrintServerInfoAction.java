package org.netbeans.modules.jeeserver.base.embedded.actions;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static javax.swing.Action.NAME;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.base.embedded.utils.EmbUtils;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * The class provides implementations of the context aware action to be
 * performed to start the server from the server project's pop up menu.
 *
 * @author V. Shyshkin
 */
@ActionID(
        category = "Project",
        id = "org.netbeans.modules.embedded.actions.PrintServerInfoAction")
@ActionRegistration(
        displayName = "#CTL_PrintServerInfoAction",lazy=false)
@ActionReference(path = "Projects/Actions", position = 0)
@NbBundle.Messages("CTL_PrintServerInfoAction=Print Server Info")
public final class PrintServerInfoAction extends AbstractAction implements ContextAwareAction {

    private static final Logger LOG = Logger.getLogger(PackageMainAction.class.getName());
    
    /**
     * Never called.
     *
     * @param e
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        assert false;
    }

    /**
     * Creates an action for the given context.
     *
     * @param context a lookup that contains the server project instance of type
     * {@literal Project}.
     * @return a new instance of type {@link #ContextAction}
     */
    @Override
    public Action createContextAwareInstance(Lookup context) {
        return new ContextAction(context);
    }

    private static final class ContextAction extends AbstractAction {

        private final Project project;
        //private RequestProcessor.Task task;
        private BaseDeploymentManager manager;

        public ContextAction(Lookup context) {
            project = context.lookup(Project.class);

            String name = ProjectUtils.getInformation(project).getDisplayName();
            // TODO state for which projects action should be enabled
            boolean isEmbedded = EmbUtils.isEmbedded(project);
            if (isEmbedded) {
                loadManager();
            }
            //setEnabled(isEmbedded && !manager.isStopped());
            setEnabled(isEmbedded);
            // we need to hide when disabled putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);            
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, !isEmbedded);
            // TODO menu item label with optional mnemonics
            //putValue(NAME, "&Package Single Jar (" + name + ")");
            putValue(NAME, "&Print Server Info");
        }

        private void loadManager() {
            manager = BaseUtils.managerOf(project);
        }

        public @Override
        void actionPerformed(ActionEvent e) {
            if (!manager.pingServer()) {
                BaseUtils.out(EmbUtils.getServerInfo(project).toString());
            } else {
                try {
                    String text = manager.getSpecifics().execCommand(project, "cmd=printinfo");
                    Reader r = new StringReader(text != null ? text : "");
                    
                    BufferedReader b = new BufferedReader(r);
                    String line;
                    while( (line = b.readLine()) != null ) {
                        BaseUtils.out(line);
                    }
                } catch (IOException ex) {
                    LOG.log(Level.INFO, ex.getMessage());
                }
            }
        }
    }//class
}//class
