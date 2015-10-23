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
import static javax.swing.Action.NAME;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * The class provides implementations of the  context aware action 
 * to be performed to start the server in debug mode from the server 
 * project's pop up menu.
 * 
 * @author V. Shyshkin
 */
@ActionID(
        category = "Project",
        id = "org.netbeans.modules.jee.server.deployment.actions.StartServerDebugAction")
@ActionRegistration(
        displayName = "#CTL_StartServerDebugAction",
        lazy=false)
@ActionReference(path = "Projects/Actions", position = 0)
@NbBundle.Messages("CTL_StartServerDebugAction=Start in Debug Mode")
public final class StartServerDebugAction extends AbstractAction implements ContextAwareAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        assert false;
    }
    /**
     * Creates an action for the given context.
     * @param context a lookup that contains the server project instance of type
     *  {@literal Project}.
     * @return a new instance of type {@link #ContextAction}
     */
    @Override
    public Action createContextAwareInstance(Lookup context) {
        return new StartServerDebugAction.ContextAction(context);
    }

    private static final class ContextAction extends AbstractAction {

        //private RequestProcessor.Task task;
        private BaseDeploymentManager manager;

        public ContextAction(Lookup context) {
            manager = BaseUtil.managerOf(context);
            // TODO state for which projects action should be enabled
            
            setEnabled(manager != null && manager.isStopped());
            // we need to hide when disabled putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);            
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, manager==null);
            // we need to hide when disabled putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);            
            // TODO menu item label with optional mnemonics
            putValue(NAME, "&Start in Debug Mode");
        }


        public @Override
        void actionPerformed(ActionEvent e) {
            manager.startServerDebug();
        }

    }//class
}//class