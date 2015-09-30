/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jeeserver.base.embedded.server.project.wizards;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.specifics.InstanceBuilder;
import org.netbeans.modules.jeeserver.base.deployment.specifics.ServerSpecifics;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.netbeans.modules.jeeserver.base.embedded.server.project.ServerSuiteManager;
import org.netbeans.modules.jeeserver.base.embedded.server.project.nodes.ChildrenKeysModel;
import org.netbeans.modules.jeeserver.base.embedded.server.project.nodes.SuiteNodeModel;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteConstants;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

// An example action demonstrating how the wizard could be called from within
// your code. You can move the code below wherever you need, or register an action:
// @ActionID(category="...", id="org.netbeans.modules.jeeserver.base.embedded.server.project.wizards.ServerInstanceWizardAction")
// @ActionRegistration(displayName="Open ServerInstance Wizard")
// @ActionReference(path="Menu/Tools", position=...)
public class ExistingServerInstanceWizardAction extends AbstractAction implements ActionListener {

    private static final Logger LOG = Logger.getLogger(ExistingServerInstanceWizardAction.class.getName());

    public static final boolean[] panelVisited = new boolean[]{false, false};

    protected Lookup context;
    protected File instanceProjectDir;
    protected List<WizardDescriptor.Panel<WizardDescriptor>> panels;
    protected WizardDescriptor wiz;        
    
    public ExistingServerInstanceWizardAction(Lookup context, File instanceProjectDir) {
        this.context = context;
        this.instanceProjectDir = instanceProjectDir;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        
        wiz = initialize(new ServerInstanceConnectorWizardPanel());

        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
            save();
        }
    }

    public WizardDescriptor initialize(WizardDescriptor.Panel<WizardDescriptor> p) {

        panels = new ArrayList<>();
        panels.add(p);
        String[] steps = new String[panels.size()];
        for (int i = 0; i < panels.size(); i++) {
            Component c = panels.get(i).getComponent();
            // Default step name to component name of panel.
            steps[i] = c.getName();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                jc.putClientProperty("WizardPanel_contentSelectedIndex", i);
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps);

            }
        }
        wiz = new WizardDescriptor(new WizardDescriptor.ArrayIterator<>(panels));
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle("...dialog title...");
        wiz.putProperty("projdir", instanceProjectDir);
        wiz.putProperty("name", instanceProjectDir.getName());

        fillWizardDescriptor(wiz);

        return wiz;
    }

    public void save() {
        
        String serverId = (String) wiz.getProperty(SuiteConstants.SERVER_ID_PROP);
        ServerSpecifics ss = BaseUtils.getServerSpecifics(serverId);

        FileObject instancesDir = gerServerInstancesDir(context); //context.lookup(FileObject.class);

        Properties props = new Properties();
        props.setProperty("project.based.type", "ant");
        props.setProperty(SuiteConstants.SERVER_INSTANCES_DIR_PROP, instancesDir.getPath());

        InstanceBuilder eib = getBuilder(ss, props);
        eib.setWizardDescriptor(wiz);
        eib.instantiate();
        
        String uri = (String) wiz.getProperty(BaseConstants.URL_PROP);        
        SuiteNodeModel suiteModel = ServerSuiteManager.getServerSuiteProject(uri)
                .getLookup().lookup(SuiteNodeModel.class);
        suiteModel.modelChanged();
        suiteModel.propertyChange(new PropertyChangeEvent(suiteModel, BaseConstants.DISPLAY_NAME_PROP, 
                    null, wiz.getProperty(BaseConstants.DISPLAY_NAME_PROP)));
        
        //
        // We cannot use context as it may be build artificially for customizer
        // See org.netbeans.modules.jeeserver.base.embedded.nodes.EmbManagerNode
        //
/*        if ( lk != null ) {
            ChildrenKeysModel m = lk.lookup(ChildrenKeysModel.class);
            m.modelChanged();
        }
*/        

    }

    protected InstanceBuilder getBuilder(ServerSpecifics specifics, Properties props) {
        return (EmbeddedInstanceBuilder) specifics.getInstanceBuilder(props, InstanceBuilder.Options.EXISTING);
    }

    protected FileObject gerServerInstancesDir(Lookup context) {
        return context.lookup(FileObject.class);
    }

    protected void fillWizardDescriptor(WizardDescriptor wiz) {

    }
}
