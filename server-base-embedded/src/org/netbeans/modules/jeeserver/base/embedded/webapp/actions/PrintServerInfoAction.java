package org.netbeans.modules.jeeserver.base.embedded.webapp.actions;

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
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteUtil;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
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


        private BaseDeploymentManager manager;

        public ContextAction(Lookup context) {
            manager = BaseUtil.managerOf(context);

            //String name = ProjectUtils.getInformation(manager.getServerProject()).getDisplayName();
            String name = "aaaa";
            // TODO state for which projects action should be enabled

            //setEnabled(isEmbedded && !manager.isStopped());
            setEnabled(manager != null);
            // we need to hide when disabled putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);            
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, manager==null);
            // TODO menu item label with optional mnemonics
            //putValue(NAME, "&Package Single Jar (" + name + ")");
            putValue(NAME, "&Print Server Info");
        }

        public @Override
        void actionPerformed(ActionEvent e) {
            if (!manager.pingServer()) {
                BaseUtil.out(SuiteUtil.getServerInfo(manager.getServerProject()).toString());
            } else {
                try {
                    String text = manager.getSpecifics().execCommand(manager, "cmd=printinfo");
                    Reader r = new StringReader(text != null ? text : "");
                    
                    BufferedReader b = new BufferedReader(r);
                    String line;
                    while( (line = b.readLine()) != null ) {
                        BaseUtil.out(line);
                    }
                } catch (IOException ex) {
                    LOG.log(Level.INFO, ex.getMessage());
                }
            }
        }
    }//class
}//class
