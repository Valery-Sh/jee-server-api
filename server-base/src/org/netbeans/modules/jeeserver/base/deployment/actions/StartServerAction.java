/**
 * This file is part of Base JEE Server support in NetBeans IDE.
 *
 * Base JEE Server support in NetBeans IDE is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * Base JEE Server support in NetBeans IDE is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should see the GNU General Public License here:
 * <http://www.gnu.org/licenses/>.
 */
package org.netbeans.modules.jeeserver.base.deployment.actions;

import java.awt.event.ActionEvent;
import java.util.Properties;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static javax.swing.Action.NAME;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * The class provides implementations of the context aware action to be
 * performed to start the server from the server project's pop up menu.
 *
 * @author V. Shyshkin
 */
@ActionID(
        category = "Project",
        id = "org.netbeans.modules.jee.server.deployment.actions.StartServerAction")
@ActionRegistration(
        displayName = "#CTL_StartServerAction",
        lazy = false)
@ActionReference(path = "Projects/Actions", position = 0)
@NbBundle.Messages("CTL_StartServerAction=Start Server")
public final class StartServerAction extends AbstractAction implements ContextAwareAction {
    
    public static final String ACTION_ENABLED_PROP = "set.disabled";
    
    public StartServerAction() {
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

    public static ProgressObject perform(Lookup context) {
        StartServerAction ssa = new StartServerAction();
        StartServerAction.ContextAction action = (StartServerAction.ContextAction) ssa.createContextAwareInstance(context);
        return action.perform();
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

    public Action createContextAwareInstance(Lookup context, Properties actionProperties) {
        return new ContextAction(context,actionProperties);
    }
    
    protected static final RequestProcessor RP = new RequestProcessor(BaseDeploymentManager.class);

    private static final class ContextAction extends AbstractAction {

        private final BaseDeploymentManager manager;
        private final Properties actionProperties;
                
        public ContextAction(Lookup context, Properties actionProperties) {
            manager = BaseUtil.managerOf(context);
            this.actionProperties = actionProperties;
            boolean show = false;
            if (manager != null) {
                show = !manager.isServerRunning();
                // we need to hide when disabled putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);            
                putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, manager == null);

            }
            setEnabled( isActionEnabled() && show);
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, manager == null);
            
            putValue(NAME, "&Start Server");
            
        }
        
        public ContextAction(Lookup context) {
            this(context, null);
        }

        public @Override
        void actionPerformed(ActionEvent e) {
            manager.startServer();
        }

        public ProgressObject perform() {
            return manager.startServer();
        }
        
        protected boolean isActionEnabled() {
            boolean b = false;
            if ( actionProperties == null  ) {
                b = true;
            } else if ( actionProperties.getProperty(ACTION_ENABLED_PROP) == null) {
                b = true;
            }
            return b;
        }
        
    }//class
}//class
