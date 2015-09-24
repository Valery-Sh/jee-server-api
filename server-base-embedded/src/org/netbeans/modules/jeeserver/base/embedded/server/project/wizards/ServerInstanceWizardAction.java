/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jeeserver.base.embedded.server.project.wizards;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import org.netbeans.modules.jeeserver.base.deployment.specifics.ServerSpecifics;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.netbeans.modules.jeeserver.base.embedded.server.project.nodes.ServerInstancesRootNode;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteConstants;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteUtil;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

// An example action demonstrating how the wizard could be called from within
// your code. You can move the code below wherever you need, or register an action:
// @ActionID(category="...", id="org.netbeans.modules.jeeserver.base.embedded.server.project.wizards.ServerInstanceWizardAction")
// @ActionRegistration(displayName="Open ServerInstance Wizard")
// @ActionReference(path="Menu/Tools", position=...)
public abstract class ServerInstanceWizardAction extends AbstractAction implements ActionListener {

    private static final Logger LOG = Logger.getLogger(ServerInstanceWizardAction.class.getName());

    public static final boolean[] panelVisited = new boolean[]{false, false};

    protected Lookup context;

    public ServerInstanceWizardAction(Lookup context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        panelVisited[0] = false;
        panelVisited[1] = false;

        //List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
        panels.add(new ServerInstanceProjectWizardPanel(isMavenBased()));
        panels.add(new ServerInstanceConnectorWizardPanel());
        String[] steps = new String[panels.size()];
        for (int i = 0; i < panels.size(); i++) {
            Component c = panels.get(i).getComponent();
            // Default step name to component name of panel.
            steps[i] = c.getName();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                /*                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                 jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                 jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
                 jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
                 jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
                 */
                jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i));
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps);

            }
        }
        WizardDescriptor wiz = new WizardDescriptor(new WizardDescriptor.ArrayIterator<WizardDescriptor>(panels));
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle("...dialog title...");

        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
            // do something
            WizardDescriptor d = ((InstanceWizardPanel) panels.get(0)).getDescriptor();

            SuiteUtil.isServerProject(null);

            String serverId = (String) wiz.getProperty(SuiteConstants.SERVER_ID_PROP);
            ServerSpecifics ss = BaseUtils.getServerSpecifics(serverId);

            FileObject instanciesDir = context.lookup(FileObject.class);

            Properties props = new Properties();
            props.setProperty("project.based.type", "ant");
            props.setProperty(SuiteConstants.SERVER_INSTANCES_DIR_PROP, instanciesDir.getPath());

            EmbeddedInstanceBuilder eib = (EmbeddedInstanceBuilder) ss.getInstanceBuilder(props);
            eib.setWizardDescriptor(wiz);

            eib.instantiate();

            context.lookup(ServerInstancesRootNode.class)
                    .getChildKeys().addNotify();
        }
    }

    protected abstract boolean isMavenBased();
}
