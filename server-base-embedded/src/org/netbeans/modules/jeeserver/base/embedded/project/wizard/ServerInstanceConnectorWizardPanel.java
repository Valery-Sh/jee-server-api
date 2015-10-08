package org.netbeans.modules.jeeserver.base.embedded.project.wizard;

import java.awt.Color;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ServerInstanceConnectorWizardPanel extends InstanceWizardPanel implements ChangeListener
{

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     * @return 
     */
    //private ServerInstanceConnectorVisualPanel component;

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public InstancePanelVisual getComponent() {
        if (component == null) {
            component = new ServerInstanceConnectorVisualPanel(this);
        }
        return component;
    }
    
    @Override
    public void stateChanged(ChangeEvent e) {
        boolean isValid = isValid();
        String msg = "";
        getComponent().getMessageLabel().setVisible(false);

        if (!isValid) {
            msg = (String) wiz.getProperty("WizardPanel_errorMessage");
            getComponent().getMessageLabel().setForeground(Color.red);
            getComponent().getMessageLabel().setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/jeeserver/base/embedded/resources/error_16.png"))); // NOI18N);
            getComponent().getMessageLabel().setVisible(true);
        } else if (wiz.getProperty("WizardPanel_warningMessage") != null) {
            msg = (String) wiz.getProperty("WizardPanel_warningMessage");
            getComponent().getMessageLabel().setForeground(Color.black);
            getComponent().getMessageLabel().setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/jeeserver/base/embedded/resources/warning_16.png"))); // NOI18N);
            getComponent().getMessageLabel().setVisible(true);
        }

        //getSaveButton().setEnabled(isValid);

        wiz.putProperty("WizardPanel_errorMessage", null);
        wiz.putProperty("WizardPanel_warningMessage", null);
        getComponent().getMessageLabel().setText(msg);

    }

}
