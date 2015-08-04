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
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static javax.swing.Action.NAME;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * The class provides implementations of the  context aware action 
 * to be performed to start the server from the server project's 
 * pop up menu.
 * 
 * @author V. Shyshkin
 */
@ActionID(
        category = "Project",
        id = "org.netbeans.modules.jee.server.deployment.actions.StartServerAction")
@ActionRegistration(
        displayName = "#CTL_StartServerAction",
        lazy=false)
@ActionReference(path = "Projects/Actions", position = 0)
@NbBundle.Messages("CTL_StartServerAction=Start Server")
public final class StartServerAction extends AbstractAction implements ContextAwareAction {
    
    public StartServerAction() {
    }
    /**
     * Never called.
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
     * @param context a lookup that contains the server project instance of type
     *  {@literal Project}.
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

            boolean isServerProject = BaseUtils.isServerProject(project);
            if ( isServerProject ) { 
                loadManager();
            }
            setEnabled(isServerProject && manager != null && manager.isStopped());
            // we need to hide when disabled putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);            
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, !isServerProject);

            putValue(NAME, "&Start Server");
        
        }

        private void loadManager() {
            manager = BaseUtils.managerOf(project);
        }

        public @Override
        void actionPerformed(ActionEvent e) {
            manager.startServer();
        }
        public ProgressObject perform() {
            return manager.startServer();
        }
    }//class
}//class