/**
 * This file is part of Jetty Server support in NetBeans IDE.
 *
 * Jetty Server support in NetBeans IDE is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * Jetty Server support in NetBeans IDE is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should see the GNU General Public License here:
 * <http://www.gnu.org/licenses/>.
 */
package org.netbeans.modules.jeeserver.jetty.project.actions;

import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static javax.swing.Action.NAME;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.deployment.ServerInstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceCreationException;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.netbeans.modules.jeeserver.jetty.customizer.JettyServerCustomizer;
import org.netbeans.modules.jeeserver.jetty.util.Utils;
import static org.netbeans.modules.jeeserver.jetty.util.Utils.getDefaultPropertyMap;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import sun.util.calendar.BaseCalendar;

/**
 * The class provides implementations of the context aware action to be
 * performed to start the server from the server project's pop up menu.
 *
 * @author V. Shyshkin
 */
public final class PropertiesAction extends AbstractAction implements ContextAwareAction {

    private static final Logger LOG = Logger.getLogger(PropertiesAction.class.getName());
    
    public PropertiesAction() {
    }

    /**
     * Never called.
     *
     * @param e
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        assert false;
    }

    public static void perform(Lookup context) {
        PropertiesAction pa = new PropertiesAction();
        PropertiesAction.ContextAction action = (PropertiesAction.ContextAction) pa.createContextAwareInstance(context);
        action.perform();
    }

    public static boolean performAndModify(Lookup context) {
        PropertiesAction pa = new PropertiesAction();
        PropertiesAction.ContextAction action = (PropertiesAction.ContextAction) pa.createContextAwareInstance(context);
        return action.performAndModify();
    }
    
    /**
     * Creates an action for the given context.
     *
     * @param context a lookup that contains the server project instance of type
     * {@literal Project}.
     * @return a new instance of type {@link #ContextAction}
     */
    @Override
    public Action createContextAwareInstance(Lookup context) {
        return new ContextAction(context);
    }

    private static final class ContextAction extends AbstractAction {

        private final Project project;
        private BaseDeploymentManager manager;

        public ContextAction(Lookup context) {

            project = context.lookup(Project.class);

            boolean isServer = Utils.isJettyServer(project);
            
            if (isServer) {
                loadManager();
            }
            
            setEnabled(isServer && manager != null);
            // we need to hide when disabled putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);            
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, !isServer);

            putValue(NAME, "&Properties");
        }

        private void loadManager() {
            manager = BaseUtil.managerOf(project.getLookup());
        }

        public @Override
        void actionPerformed(ActionEvent e) {
            perform();
        }

        public void perform() {
            if (manager == null) {
                createServerInstance();
                manager = BaseUtil.managerOf(project.getLookup());
            }

            JettyServerCustomizer c = new JettyServerCustomizer(manager);
            // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
            c.setName("Jetty Server Properties");
            c.getWizardDescriptor().setTitleFormat(new MessageFormat("{0}"));
            c.getWizardDescriptor().setTitle("Jetty Server Properties");
            c.stateChanged(null);
            if (DialogDisplayer.getDefault().notify(c.getWizardDescriptor()) == WizardDescriptor.FINISH_OPTION) {
                
            }

        }
        public boolean performAndModify() {
            JettyServerCustomizer c = new JettyServerCustomizer(manager);
            // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
            c.setName("Jetty Server Properties");
            c.getWizardDescriptor().setTitleFormat(new MessageFormat("{0}"));
            c.getWizardDescriptor().setTitle("Jetty Server Properties");
            c.stateChanged(null);
            if (DialogDisplayer.getDefault().notify(c.getWizardDescriptor()) == WizardDescriptor.FINISH_OPTION) {
                c.saveChanges();
                return true;
//                manager.getInstanceProperties().setProperty(BaseConstants.HOME_DIR_PROP,
//                        (String)c.getWizardDescriptor().getProperty(BaseConstants.HOME_DIR_PROP));
            } else {
                return false;
            }

        }

        
        protected void createServerInstance() {
            String uri = Utils.buildUri(project.getProjectDirectory());
            FileObject projectDir = project.getProjectDirectory();
            try {
                InstanceProperties ip = InstanceProperties.getInstanceProperties(uri);
                if (ip == null) {
                    Map<String, String> map = getDefaultPropertyMap(projectDir);
                    InstanceProperties.createInstanceProperties(uri, null, null, projectDir.getNameExt(), map);
                    // to update with InstanceProperties                
                    FileOwnerQuery.getOwner(projectDir).getLookup().lookup(ServerInstanceProperties.class);
                }

            } catch (InstanceCreationException ex) {
                LOG.log(Level.INFO, ex.getMessage());
            }
        }

    }//class
}//class
