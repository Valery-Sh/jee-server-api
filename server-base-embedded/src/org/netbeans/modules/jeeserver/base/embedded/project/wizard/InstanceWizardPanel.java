package org.netbeans.modules.jeeserver.base.embedded.project.wizard;

import java.awt.Component;
import java.util.HashSet;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;

/**
 *
 * @author V. Shyshkin
 */
public abstract class InstanceWizardPanel  implements WizardDescriptor.Panel,
        WizardDescriptor.ValidatingPanel, WizardDescriptor.FinishablePanel, ChangeListener {

    protected static final String VISITED_PROP = "visited_con_prop";
    
    protected WizardDescriptor wiz;
    protected InstancePanelVisual component;
    
    public InstanceWizardPanel() {
        init();
    }
    
    private void init() {
        addChangeListener(this);
    }
    @Override
    public abstract Component getComponent();

    public void setComponent(InstancePanelVisual component) {
        this.component = component;
    }
    
    

/*    public HelpCtx getHelp() {
        return new HelpCtx(EmbeddedServerProjectWizardPanel.class);
    }
*/
    @Override
    public boolean isValid() {
        getComponent();
        return component.valid(wiz);
    }

    public WizardDescriptor getDescriptor() {
        return wiz;
    }
    
    private final Set<ChangeListener> listeners = new HashSet<>(1); // or can use ChangeSupport in NB 6.0

    @Override
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
        
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    public final void fireChangeEvent() {
        Set<ChangeListener> ls;
        synchronized (listeners) {
            ls = new HashSet<>(listeners);
        }
        ChangeEvent ev = new ChangeEvent(this);
        ls.stream().forEach((l) -> {
            l.stateChanged(ev);
        });
    }

    @Override
    public void readSettings(Object settings) {
        wiz = (WizardDescriptor) settings;
        component.read(wiz);
    }

    @Override
    public void storeSettings(Object settings) {
        WizardDescriptor d = (WizardDescriptor) settings;
        component.store(d);
    }

    @Override
    public boolean isFinishPanel() {
        return true;
    }

    @Override
    public void validate() throws WizardValidationException {
        getComponent();
        component.validate(wiz);
    }

    @Override
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }
}
