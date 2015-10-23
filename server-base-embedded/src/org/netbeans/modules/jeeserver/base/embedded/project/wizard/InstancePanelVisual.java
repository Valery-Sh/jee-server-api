package org.netbeans.modules.jeeserver.base.embedded.project.wizard;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;

/**
 *
 * @author V. Shyshkin
 */
public abstract class InstancePanelVisual extends JPanel implements ChangeListener{
    public abstract void read(WizardDescriptor wiz);
    public abstract void store(WizardDescriptor wiz);
    public abstract boolean valid(WizardDescriptor wiz);
    public abstract JLabel getMessageLabel();
    public abstract JButton getSaveButton();
    
    
    public void validate(WizardDescriptor wiz) {
        
    }
    
    
}
