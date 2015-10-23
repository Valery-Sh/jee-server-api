package org.netbeans.modules.jeeserver.base.deployment.lc;

import java.awt.Component;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import static org.openide.WizardDescriptor.PROP_CONTENT_DISPLAYED;

// An example action demonstrating how the wizard could be called from within
// your code. You can move the code below wherever you need, or register an action:
// @ActionID(category="...", id="org.netbeans.modules.jeeserver.base.deployment.utils.license.LicenseWizardAction")
// @ActionRegistration(displayName="Open License Wizard")
// @ActionReference(path="Menu/Tools", position=...)
public final class LicenseWizard {
    
    public static String PROP_LICENCE_ACCEPTED = "license.accepted";
    

    public boolean showLicenceDialog() {
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>();
        panels.add(new LicenseWizardPanel());
        String[] steps = new String[panels.size()];
        
        WizardDescriptor wiz = new WizardDescriptor(new WizardDescriptor.ArrayIterator<>(panels));
        wiz.putProperty(PROP_CONTENT_DISPLAYED, false);
        wiz.putProperty(PROP_LICENCE_ACCEPTED, false);
        
        
        for (int i = 0; i < panels.size(); i++) {
            
            Component c = panels.get(i).getComponent();
            // Default step name to component name of panel.
            steps[i] = c.getName();
            
            if (c instanceof LicenseVisualPanel) { // assume Swing components
                LicenseVisualPanel jc = (LicenseVisualPanel) c;
                jc.wiz = wiz;
                
                jc.putClientProperty(PROP_LICENCE_ACCEPTED, false);
                
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
            }
        }
        
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle("...dialog title...");
        
        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
            return (boolean)wiz.getProperty(PROP_LICENCE_ACCEPTED);
        } else {
            return false;
        }
        
    }

}
