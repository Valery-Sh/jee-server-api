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
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import static org.netbeans.modules.jeeserver.base.deployment.actions.StartServerAction.RP;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.execution.ExecutorTask;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

/**
 * The class provides implementations of the context aware action to be
 * performed to stop the server from the server project's pop up menu.
 *
 * @author V. Shyshkin
 */
@ActionID(
        category = "Project",
        id = "org.netbeans.modules.jee.server.deployment.actions.StopServerAction")
@ActionRegistration(
        displayName = "#CTL_StopServerAction", lazy = false)
@ActionReference(path = "Projects/Actions", position = 0)
@Messages("CTL_StopServerAction=Stop Server")
public final class StopServerAction extends AbstractAction implements ContextAwareAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        assert false;
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

    public static Action getStopServerAction(Project serverProj) {
        return new ContextAction(serverProj.getLookup());
    }

    private static final class ContextAction extends AbstractAction {

        protected static final RequestProcessor RP = new RequestProcessor(BaseDeploymentManager.class);

        private BaseDeploymentManager manager;

        public ContextAction(Lookup context) {
            manager = BaseUtils.managerOf(context);

            boolean show = false;
            if (manager != null) {
                //manager.updateServerIconAnnotator();
                /*                show = ! isStopped();
                 boolean isStopped = isStopped();
                 boolean running = manager.isActuallyRunning();
                 //manager.
                 if (running && isStopped || (!running) && !isStopped) {
                 show = manager.isServerRunning();
                 }
                 */
                show = manager.isServerRunning();
                // we need to hide when disabled putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);            
                putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, manager == null);

            }
            setEnabled(show);

            //setEnabled(show);
            // we need to hide when disabled putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);            
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, manager == null);
            putValue(NAME,
                    "&Stop Server");
        }

        public boolean isStopped() {

            ExecutorTask task = manager.getServerTask();

            boolean stopped = false;

            if (task == null || (task != null && task.isFinished())) {
                stopped = true;
            }
            return stopped;
        }

        public @Override
        void actionPerformed(ActionEvent e) {
            manager.stopServer();
        }
    }
}
