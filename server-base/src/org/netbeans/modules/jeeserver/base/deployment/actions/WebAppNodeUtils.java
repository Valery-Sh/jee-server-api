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

import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;

/**
 *
 * @author V. Shyshkin
 */
public class WebAppNodeUtils {
    public static final String PROFILE = "Profile";
    
    public static Action[] getActions(Project webProject) {
        Action openInnerAction = WebAppOpenInnerProjectAction.getOpenInnerProjectAction(webProject.getProjectDirectory().getLookup());
        Action profileAction = new WebAppCommandActions.ProfileAction(webProject);
        Action deployAction = ProjectSensitiveActions.projectCommandAction("redeploy", "Deploy", null);

        Action runAction = new WebAppCommandActions.RunAction(webProject);
        Action debugAction = new WebAppCommandActions.DebugAction(webProject);
        Action cleanAction = new WebAppCommandActions.CleanAction(webProject);
        Action buildAction = new WebAppCommandActions.BuildAction(webProject);
        Action cleanAndBuildAction = new WebAppCommandActions.CleanAndBuildAction(webProject);
        

        Action[] actions = CommonProjectActions.forType("org-netbeans-modules-web-project");
        for (Action a : actions) {
            if (a == null || a.getValue(Action.NAME) == null) {
                continue;
            }
            switch ((String) a.getValue(Action.NAME)) {
                case PROFILE:
                    profileAction = a;
                    break;
            }
        }
        return new Action[]{
            openInnerAction,
            runAction,
            debugAction,
            deployAction,
            profileAction,
            null, // separator

            cleanAction,
            buildAction,
            cleanAndBuildAction,
            null, // separator

            CommonProjectActions.newFileAction(),
            CommonProjectActions.copyProjectAction(),
            CommonProjectActions.moveProjectAction(),
            CommonProjectActions.renameProjectAction(),
            CommonProjectActions.deleteProjectAction(), // Put here more Actions .... 
            null,
            CommonProjectActions.customizeProjectAction(), // Properties
        };
    }
    
}
