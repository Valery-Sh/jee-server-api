package org.netbeans.modules.jeeserver.base.embedded.project.wizard;

import java.awt.Color;
import javax.swing.event.ChangeEvent;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteConstants;

public class ServerInstanceProjectWizardPanel  extends InstanceWizardPanel {
    
    protected final boolean ismavenbased;
    
/*    public ServerInstanceProjectWizardPanel(ServerInstanceWizardAction owner) {
        this(false);
    }
*/    
    public ServerInstanceProjectWizardPanel(boolean mavenBased) {
        this.ismavenbased = mavenBased;
    }

    @Override
    public InstancePanelVisual getComponent() {
        if (component == null) {
            if ( ismavenbased ) {
                component = new ServerInstanceMavenProjectVisualPanel(this);
            } else {
                component = new ServerInstanceAntProjectVisualPanel(this);
            }
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
    
    @Override
    public boolean isFinishPanel() {
        boolean[] v =  (boolean[])wiz.getProperty(SuiteConstants.PANEL_VISITED_PROP); 
        return v[1];
        //return ServerInstanceWizardAction.panelVisited[1] ;
        
    }
}
