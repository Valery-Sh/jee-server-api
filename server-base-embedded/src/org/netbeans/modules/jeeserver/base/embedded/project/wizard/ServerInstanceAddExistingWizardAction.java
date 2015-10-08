package org.netbeans.modules.jeeserver.base.embedded.project.wizard;

import org.netbeans.modules.jeeserver.base.embedded.EmbeddedInstanceBuilder;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.deployment.specifics.InstanceBuilder;
import org.netbeans.modules.jeeserver.base.deployment.specifics.ServerSpecifics;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.netbeans.modules.jeeserver.base.embedded.project.SuiteManager;
import org.netbeans.modules.jeeserver.base.embedded.project.nodes.SuiteNotifier;
//import static org.netbeans.modules.jeeserver.base.embedded.server.project.wizard.InstanceWizardAction.PANEL_VISITED_PROP;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteConstants;
import static org.netbeans.modules.jeeserver.base.embedded.utils.SuiteConstants.PANEL_VISITED_PROP;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

// An example action demonstrating how the wizard could be called from within
// your code. You can move the code below wherever you need, or register an action:
// @ActionID(category="...", id="org.netbeans.modules.jeeserver.base.embedded.server.project.wizards.ServerInstanceWizardAction")
// @ActionRegistration(displayName="Open ServerInstance Wizard")
// @ActionReference(path="Menu/Tools", position=...)
public class ServerInstanceAddExistingWizardAction extends AbstractAction implements ActionListener {

    private static final Logger LOG = Logger.getLogger(ServerInstanceAddExistingWizardAction.class.getName());

    public static final boolean[] panelVisited = new boolean[]{false, false};

    protected Lookup context;
    protected File instanceProjectDir;
    protected List<WizardDescriptor.Panel<WizardDescriptor>> panels;
    protected WizardDescriptor wiz;        
    
    public ServerInstanceAddExistingWizardAction(Lookup context, File instanceProjectDir) {
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
    
    public  WizardDescriptor getWizardDescriptor() {
        return wiz;
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
        wiz.putProperty(PANEL_VISITED_PROP, new boolean[] {false,false});
        
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
        ServerSpecifics ss = BaseUtil.getServerSpecifics(serverId);

        FileObject instancesDir = gerServerInstancesDir(context); //context.lookup(FileObject.class);

        Properties props = new Properties();
        FileObject projFo = FileUtil.toFileObject(instanceProjectDir);
        
        Project instanceProj = FileOwnerQuery.getOwner(projFo);
        if ( BaseUtil.isAntProject(instanceProj)) {       
            props.setProperty("project.based.type", "ant");
        } else {
            props.setProperty("project.based.type", "maven");
        }            
        props.setProperty(SuiteConstants.SERVER_INSTANCES_DIR_PROP, instancesDir.getPath());

        InstanceBuilder eib = getBuilder(ss, props);
        eib.setWizardDescriptor(wiz);
        eib.instantiate();

        notifySettingChange(context);
        
    }
    
    protected void notifySettingChange(Lookup context) {
        String uri = (String) wiz.getProperty(BaseConstants.URL_PROP);        
        
        SuiteNotifier suiteNotifier = SuiteManager.getServerSuiteProject(uri)
                .getLookup().lookup(SuiteNotifier.class);
        
        suiteNotifier.instancesChanged();        
        suiteNotifier.displayNameChange(uri, (String) wiz.getProperty(BaseConstants.DISPLAY_NAME_PROP));         
        
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
