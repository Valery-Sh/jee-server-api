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
package org.netbeans.modules.jeeserver.jetty.project.nodes.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;

/**
 *
 * @author V. Shyshkin
 */
/*@ActionID(
        category = "Project",
        id = "org.netbeans.modules.jeeserver.jetty.project.nodes.actions.StartWebAppAction")
@ActionRegistration(
        displayName = "#CTL_StartWebAppAction")
@ActionReference(path = "Projects/Actions", position = 0)
@NbBundle.Messages("CTL_StartWebAppAction=Start")
*/
public class StartHotDeployedWebAppAction extends AbstractAction implements ContextAwareAction {

    public StartHotDeployedWebAppAction() {
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
    /**
     * Creates an action for the given context.
     *
     * @param context a lookup that contains the server project instance of type
     * {@literal Project}.
     * @return a new instance of type {@link #ContextAction}
     */
    @Override
    public Action createContextAwareInstance(Lookup context) {
        return new BaseHotDeployedContextAction(context,"starthotdeployed");
    }

}//class
