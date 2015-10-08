package org.netbeans.modules.jeeserver.base.embedded.project.wizard;

import org.netbeans.modules.jeeserver.base.embedded.EmbeddedInstanceBuilder;
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
import org.netbeans.modules.jeeserver.base.deployment.specifics.InstanceBuilder;
import org.netbeans.modules.jeeserver.base.deployment.specifics.ServerSpecifics;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.netbeans.modules.jeeserver.base.embedded.project.nodes.ChildrenNotifier;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteConstants;
import static org.netbeans.modules.jeeserver.base.embedded.utils.SuiteConstants.PANEL_VISITED_PROP;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

public abstract class InstanceWizardAction extends AbstractAction implements ActionListener {

    private static final Logger LOG = Logger.getLogger(InstanceWizardAction.class.getName());

//    public static final boolean[] panelVisited = new boolean[]{false, false};
    
//    public static final String PANEL_VISITED_PROP = "panel.visited";

    protected Lookup context;

    public InstanceWizardAction(Lookup context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>();
        panels.add(new ServerInstanceProjectWizardPanel(isMavenBased()));
        panels.add(new ServerInstanceConnectorWizardPanel());
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
        WizardDescriptor wiz = new WizardDescriptor(new WizardDescriptor.ArrayIterator<>(panels));
        
        wiz.putProperty(PANEL_VISITED_PROP, new boolean[] {false,false});
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle("...dialog title...");

        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {

//            SuiteUtil.isServerProject(null);

            String serverId = (String) wiz.getProperty(SuiteConstants.SERVER_ID_PROP);
            ServerSpecifics ss = BaseUtil.getServerSpecifics(serverId);

            FileObject instancesDir = context.lookup(FileObject.class);

            Properties props = new Properties();
            if ( ! isMavenBased() ) {
                props.setProperty("project.based.type", "ant");
            } else {
                props.setProperty("project.based.type", "maven");
            }
            props.setProperty(SuiteConstants.SERVER_INSTANCES_DIR_PROP, instancesDir.getPath());
            if ( isMavenBased() ) {
                props.setProperty("groupId", (String)wiz.getProperty("groupId"));
                props.setProperty("artifactId", (String)wiz.getProperty("artifactId"));                
                props.setProperty("artifactVersion", (String)wiz.getProperty("artifactVersion"));                                
                props.setProperty("package", (String)wiz.getProperty("package"));
                if ( wiz.getProperty(SuiteConstants.MAVEN_MAIN_CLASS_PROP) != null) {
                    props.setProperty(SuiteConstants.MAVEN_MAIN_CLASS_PROP, (String)wiz.getProperty(SuiteConstants.MAVEN_MAIN_CLASS_PROP));                                                                
                }
            }
            
            EmbeddedInstanceBuilder eib = (EmbeddedInstanceBuilder) ss.getInstanceBuilder(props, InstanceBuilder.Options.NEW);
            eib.setWizardDescriptor(wiz);

            eib.instantiate();

            context.lookup(ChildrenNotifier.class).childrenChanged();
        }
    }

    protected abstract boolean isMavenBased();
}
