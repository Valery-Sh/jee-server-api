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
import static javax.swing.Action.NAME;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ActionProvider;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 * The class contains a single static method to invoke a command action.
 * 
 * @author V. Shyshkin
 */
public class WebAppCommandActions {
    private static final RequestProcessor RP = new RequestProcessor(WebAppCommandActions.class);    
    
    /**
     * Creates an instance of {@code RequestProcessor } for the given
     * action command and invokes the action in a separate thread.
     * 
     * @param actionCommand one of the following command action names:
     * <ul>
     *  <li>ActionProvider.COMMAND_CLEAN</li>
     *  <li>ActionProvider.COMMAND_BUILD</li>
     *  <li>ActionProvider.COMMAND_REBUILD</li>
     *  <li>ActionProvider.COMMAND_RUN</li>
     *  <li>ActionProvider.COMMAND_DEBUG</li>
     *  <li>ActionProvider.COMMAND_REDEPLOY</li>
     * </ul>
     * For each command action the class contains  
     * the corresponding inner class.
     * 
     * @param webProject web project for which an action will be performed.
     * @return an instance of type {@code RequestProcessor.Task}
  */
    public static RequestProcessor.Task doInvokeAction(final String actionCommand, final Project webProject) {
//        RequestProcessor rp = new RequestProcessor();
        
        return RP.post(new Runnable() {
            @Override
            public void run() {
                ActionProvider ap = webProject.getLookup().lookup(ActionProvider.class);
                ap.invokeAction(actionCommand, webProject.getLookup());
            }
        });

    }
    /**
     * Represents the {@code ActionProvider.COMMAND_CLEAN} action command.
     */
    public static class CleanAction extends BaseAction {

        public CleanAction(Project webProject) {
            super(webProject);
            putValue(NAME, "Clean");
            ContextAwareAction cc;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            doInvokeAction(ActionProvider.COMMAND_CLEAN, getWebProject().getLookup());
        }
    }//class    
    /**
     * Represents the {@code ActionProvider.COMMAND_BUILD} action command.
     */
    public static class BuildAction extends BaseAction {

        public BuildAction(Project webProject) {
            super(webProject);
            putValue(NAME, "Build");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            doInvokeAction(ActionProvider.COMMAND_BUILD, getWebProject().getLookup());
        }
    }//class    
    /**
     * Represents the {@code ActionProvider.COMMAND_REBUILD} action command.
     */
    public static class CleanAndBuildAction extends BaseAction {

        public CleanAndBuildAction(Project webProject) {
            super(webProject);
            putValue(NAME, "Clean and Build");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            doInvokeAction(ActionProvider.COMMAND_REBUILD, getWebProject().getLookup());
        }
    }//class    
    /**
     * Represents the {@code ActionProvider.RUN} action command.
     */
    public static class RunAction extends BaseAction {

        public RunAction(Project webProject) {
            super(webProject);
            putValue(NAME, "Run");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            doInvokeAction(ActionProvider.COMMAND_RUN, getWebProject().getLookup());

        }
    }//class
    /**
     * Represents the {@code ActionProvider.COMMAND_DEBUG} action command.
     */
    public static class DebugAction extends BaseAction {

        public DebugAction(Project webProject) {
            super(webProject);
            putValue(NAME, "Debug");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            doInvokeAction(ActionProvider.COMMAND_DEBUG, getWebProject().getLookup());

        }
    }//class    
    /**
     * Represents the {@code ActionProvider.REDEPLOY} action command.
     */
    public static class DeployAction extends BaseAction {

        public DeployAction(Project webProject) {
            super(webProject);
            putValue(NAME, "Deploy");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            doInvokeAction("redeploy", getWebProject().getLookup());
        }
    }//class    

    public static class ProfileAction extends BaseAction {

        public ProfileAction(Project webProject) {
            super(webProject);
            putValue(NAME, "Profile");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            doInvokeAction("profile", getWebProject().getLookup());

        }
    }//class    

    public static class BaseAction extends AbstractAction {

        private final Project webProject;

        public BaseAction(Project webProject) {
            this.webProject = webProject;
        }

        @Override
        public void actionPerformed(ActionEvent e) {

        }

        public Project getWebProject() {
            return webProject;
        }

        public void doInvokeAction(final String actionCommand, final Lookup context) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    ActionProvider provider = webProject.getLookup().lookup(ActionProvider.class);
                    provider.invokeAction(actionCommand, context);                    
                }
            });   
        }
    }//class   

}//class
