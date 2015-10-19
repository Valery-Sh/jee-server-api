/**
 * This file is part of Jetty Server support in NetBeans IDE.
 *
 * Jetty Server support in NetBeans IDE is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the License,
 * or (at your option) any later version.
 *
 * Jetty Server support in NetBeans IDE is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should see the GNU General Public License here:
 * <http://www.gnu.org/licenses/>.
 */
package org.netbeans.modules.jeeserver.jetty.project.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static javax.swing.Action.NAME;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import static org.netbeans.modules.jeeserver.base.deployment.progress.BaseRunProgressObject.LOG;
import org.netbeans.modules.jeeserver.base.deployment.specifics.StartServerPropertiesProvider;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.netbeans.modules.jeeserver.jetty.project.nodes.libs.LibUtil;
import org.netbeans.modules.jeeserver.jetty.util.Utils;
import org.openide.awt.DynamicMenuContent;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 * The class provides implementations of the context aware action to be
 * performed to download libraries as specified by Jetty modules. The menu
 * action is accessible from the server project's pop up menu.
 *
 * @author V. Shyshkin
 */
/*@ActionID(
 category = "Project",
 id = "org.netbeans.modules.jeeserver.jetty.project.actions")
 @ActionRegistration(
 displayName = "#CTL_RefreshLibrariesAction",lazy=false)
 @ActionReference(path = "Projects/Actions", position = 0)
 @NbBundle.Messages("CTL_RefreshLibrariesAction=Properties")
 */
public final class CreateFilesAction extends AbstractAction implements ContextAwareAction {
    private static final RequestProcessor RP = new RequestProcessor(CreateFilesAction.class);

    public CreateFilesAction() {
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

            putValue(NAME, "&Create Files (start.jar --create-files)");
        }

        private void loadManager() {
            manager = BaseUtil.managerOf(project.getLookup());
        }

        public @Override
        void actionPerformed(ActionEvent e) {
            perform();
        }

        public void perform() {
            manager.getSpecifics().licensesAccepted(manager);
                //IniModules.CDISupport.showLicenseDialog(project);
            RP.post(new RunnableImpl(), 0, Thread.NORM_PRIORITY);
        }

        protected String getStartJar() {
            InstanceProperties ip = manager.getInstanceProperties();
            return ip.getProperty(BaseConstants.HOME_DIR_PROP) + "/start.jar";
        }

        private class RunnableImpl implements Runnable {

            @Override
            public void run() {
                String serverDir = BaseUtil.getServerLocation(manager.getInstanceProperties());
                File f = new File(serverDir);
                Project project = manager.getServerProject();

                ExecutorTask task;

                StartServerPropertiesProvider pp = project.getLookup().lookup(StartServerPropertiesProvider.class);

                FileObject buildXml = FileUtil.toFileObject(f).getFileObject("build.xml");

                Properties props;

                if (pp != null) {
                    buildXml = pp.getBuildXml(project);
                }

                props = new Properties();
                props.setProperty("start.jar", getStartJar());

                String[] targets = new String[]{"pre-run"};

                try {
                    task = ActionUtils.runTarget(buildXml, targets, props);
                    task.waitFinished();
                    //JettyServerPlatformImpl platform = (JettyServerPlatformImpl) manager.getPlatform();
                    //if (platform == null) {
                    LibUtil.updateLibraries(project);
                    
/*                    JettyServerPlatformImpl platform = JettyServerPlatformImpl.getInstance(manager);
                    BaseUtils.out("CreateFilesAction CALL  NOTIFY " + System.currentTimeMillis());
                    
                    platform.notifyLibrariesChanged();
                    
                    LibrariesFileNode node = LibUtil
                            .getLibrariesRootNode(project);
                    if ( node != null ) {
                        ((LibrariesFileNode.FileKeys) LibUtil
                                .getLibrariesRootNode(project)
                                .getChildrenKeys())
                                .addNotify();
                    }
*/
                } catch (IOException | IllegalArgumentException ex) {
                    LOG.log(Level.INFO, ex.getMessage());
                }
            }
        }//class
    }//class

}
