package org.netbeans.modules.jeeserver.base.embedded.project.wizard;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 *
 * @author V. Shyshkin
 */
public class ServerInstanceCustomizer extends ServerInstanceConnectorVisualPanel {

    private ServerInstanceConnectorWizardPanel panel;
    private Lookup context;

    private ServerInstanceCustomizerWizardAction wizardAction;

    public ServerInstanceCustomizer(Lookup context) {
        this(new ServerInstanceConnectorWizardPanel(), context);
    }

    public ServerInstanceCustomizer(ServerInstanceConnectorWizardPanel panel, Lookup context) {
        super(panel);
        this.panel = panel;
        this.context = context;
        init();
    }

    private void init() {
        File instanceFile = FileUtil.toFile(context.lookup(FileObject.class));
        wizardAction = new ServerInstanceCustomizerWizardAction(context, instanceFile);
        final WizardDescriptor wiz = wizardAction.initialize(panel);
        panel.setComponent(this);
        this.getSaveButton().setVisible(true);
        this.getSaveButton().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                panel.getComponent().store(wiz);
                wizardAction.save();
            }
        });
        panel.readSettings(wiz); // to fill component's fields
        //InstancePanelVisual c = (InstancePanelVisual)panel.getComponent();
        //panel.addChangeListener(c);
        this.stateChanged(null);

    }
}
