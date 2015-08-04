package org.netbeans.modules.jeeserver.base.embedded.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static javax.swing.Action.NAME;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.embedded.project.web.EmbNewWebAppWizardPerformer;
import org.netbeans.modules.jeeserver.base.embedded.utils.EmbUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

@ActionID( 
        category = "Project",
        id = "org.netbeans.modules.embedded.actions.ESNewWebAppAction")
@ActionRegistration(
        displayName = "#CTL_ESNewWebAppAction",lazy=false)
@ActionReference(path = "Projects/Actions", position = 0)
@Messages("CTL_ESNewWebAppAction=New Web Application")
public final class NewWebAppAction extends AbstractAction implements ContextAwareAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        assert false;
    }

    @Override
    public Action createContextAwareInstance(Lookup context) {
        return new NewWebAppAction.ContextAction(context);
    }
    
    public static Action getNewWebAppAction(Lookup context) {
        return new NewWebAppAction.ContextAction(context);
    }
    
    private static final class ContextAction extends AbstractAction {

        private RequestProcessor.Task task;
        private final Project project;
        
        public ContextAction(Lookup context) {
            project = context.lookup(Project.class);
           // String name = ProjectUtils.getInformation(project).getDisplayName();
            // TODO state for which projects action should be enabled
            boolean isEmbedded = EmbUtils.isEmbedded(project);
            // we need to hide when disabled putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);            
            setEnabled(isEmbedded);
            // we need to hide when disabled putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);            
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, ! isEmbedded);
            // TODO menu item label with optional mnemonics
            putValue(NAME, "&New Web Application");

            task = new RequestProcessor("NewProjectBody").create(new Runnable() { // NOI18N
                @Override
                public void run() {
                    EmbNewWebAppWizardPerformer wa = new EmbNewWebAppWizardPerformer(project);
                    wa.perform();
                }
            });

        }

        public @Override
        void actionPerformed(ActionEvent e) {
            //FileObject projDir = project.getProjectDirectory();
            task.schedule(0);

            if ("waitFinished".equals(e.getActionCommand())) {
                task.waitFinished();
            }

        }
    }//class
}
