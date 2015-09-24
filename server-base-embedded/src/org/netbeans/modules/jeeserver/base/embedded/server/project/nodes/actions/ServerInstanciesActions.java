/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jeeserver.base.embedded.server.project.nodes.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Properties;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static javax.swing.Action.NAME;
import javax.swing.JFileChooser;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.base.deployment.ServerInstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.netbeans.modules.jeeserver.base.embedded.actions.AddWebRefAction;
import org.netbeans.modules.jeeserver.base.embedded.server.project.InstanceContexts;
import org.netbeans.modules.jeeserver.base.embedded.server.project.nodes.ChildrenAccessor;
import org.netbeans.modules.jeeserver.base.embedded.server.project.wizards.ServerInstanceAntBuildExtender;
import org.netbeans.modules.jeeserver.base.embedded.server.project.wizards.ServerInstanceWizardAction;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteConstants;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteUtil;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 * @author V. Shyshkin
 */
public class ServerInstanciesActions {

    public static class NewAntProjectAction extends AbstractAction implements ContextAwareAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            assert false;
        }

        @Override
        public Action createContextAwareInstance(Lookup context) {
            return new NewAntProjectAction.ContextAction(context);
        }

        public static Action getContextAwareInstance(Lookup context) {
            return new NewAntProjectAction.ContextAction(context);
        }

        private static final class ContextAction extends ServerInstanceWizardAction { //implements ProgressListener {

            public ContextAction(Lookup context) {
                super(context);
                putValue(NAME, "&New Server Instance  as Ant Project");
            }

            @Override
            protected boolean isMavenBased() {
                return false;
            }
        }//class
    }//class

    public static class AddExistingProjectAction extends AbstractAction implements ContextAwareAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            assert false;
        }

        /**
         *
         * @param context a Lookup of the ServerInstancesRootNode object
         * @return
         */
        @Override
        public Action createContextAwareInstance(Lookup context) {
            return new AddExistingProjectAction.ContextAction(context);
        }

        /**
         *
         * @param context a Lookup of the ServerInstancesRootNode object
         * @return
         */
        public static Action getContextAwareInstance(Lookup context) {
            return new AddExistingProjectAction.ContextAction(context);
        }

        private static final class ContextAction extends AbstractAction { //implements ProgressListener {

            private RequestProcessor.Task task;
            private final Lookup context;

            public ContextAction(Lookup context) {
                this.context = context;
                putValue(NAME, "&Add  Existing Project");
                task = new RequestProcessor("AddProjectBody").create(new Runnable() { // NOI18N
                    @Override
                    public void run() {
                        JFileChooser fc = ProjectChooser.projectChooser();
                        int choosed = fc.showOpenDialog(null);
                        if (choosed == JFileChooser.APPROVE_OPTION) {
                            File selectedFile = fc.getSelectedFile();
                            FileObject appFo = FileUtil.toFileObject(selectedFile);
                            /*                        String msg = ProjectFilter.check(appFo);
                             if (msg != null) {
                             NotifyDescriptor d
                             = new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE);
                             DialogDisplayer.getDefault().notify(d);
                             return;
                             }

                             if (!AddWebRefAction.ProjectFilter.accept(project, appFo)) {
                             // All error messages are shown
                             return;
                             }
                             String contextPath = WebModule.getWebModule(appFo).getContextPath();
                             Properties props = new Properties();
                             props.setProperty("contextPath", contextPath);
                             props.setProperty("webAppLocation", FileUtil.normalizePath(appFo.getPath()));
                             String selectedPath = FileUtil.normalizePath(appFo.getPath());

                             FileObject targetFolder = project.getProjectDirectory().getFileObject(SuiteConstants.REG_WEB_APPS_FOLDER);
                             */
                            String selectedFileName = selectedFile.getName();//".webref";                        
                        } else {
                            System.out.println("File access cancelled by user.");
                        }
                    }
                });

            }

            public @Override
            void actionPerformed(ActionEvent e) {
                task.schedule(0);

                if ("waitFinished".equals(e.getActionCommand())) {
                    task.waitFinished();
                }

            }
        }//class
    }//class

    public static class RemoveInstanceAction extends AbstractAction implements ContextAwareAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            assert false;
        }

        @Override
        public Action createContextAwareInstance(Lookup context) {
            return new RemoveInstanceAction.ContextAction(context);
        }

        public static Action getContextAwareInstance(Lookup context) {
            return new RemoveInstanceAction.ContextAction(context);
        }

        private static final class ContextAction extends AbstractAction { //implements ProgressListener {

//            private final Project project;
//            private final FileObject warrefFo;
//            private final BaseDeploymentManager dm;
//            private String contextPath;
//            private String warLocation;
//            private String urlStr;
            private Lookup context;

            public ContextAction(Lookup context) {
                this.context = context;
                //DataObject wardo = context.lookup(DataObject.class);
                //warrefFo = wardo.getPrimaryFile();
                //project = FileOwnerQuery.getOwner(warrefFo);
                putValue(NAME, "&Remove Server Instance");
                //dm = BaseUtils.managerOf(context);

            }

            @Override
            public void actionPerformed(ActionEvent e) {
                BaseDeploymentManager dm = context.lookup(ServerInstanceProperties.class).getManager();
                Project instanceProject = dm.getServerProject();

                Project suite = SuiteUtil.getServerSuite(dm);
                String uri = dm.getUri();
                InstanceContexts contexts = suite.getLookup().lookup(InstanceContexts.class);

                contexts.remove(uri);
                InstanceProperties.removeInstance(uri);
                ServerInstanceAntBuildExtender extender = new ServerInstanceAntBuildExtender(instanceProject);
                extender.disableExtender();
                Lookup lk = contexts.getServerInstancesContext();

                lk.lookup(ChildrenAccessor.class).addNotify();

            }
        }
    }

    public static class ProjectFilter {

        public static String check(FileObject appFo) {

            if (appFo == null) {
                return "Cannot be null";
            }
            String msg = "The selected project is not a Web Project ";
            Project webProj = FileOwnerQuery.getOwner(appFo);
            if (webProj == null) {
                return msg + "(not a project)";
            }
            FileObject fo = webProj.getProjectDirectory().getFileObject("nbproject/project.xml");
            if (fo != null && SuiteUtil.projectTypeByProjectXml(fo).equals(SuiteConstants.HTML5_PROJECTTYPE)) {
                return "The selected project is a Html5 Project ";
            }
            fo = appFo.getParent();
            if (fo != null && fo.isFolder() && fo.getNameExt().equals(SuiteConstants.REG_WEB_APPS_FOLDER)) {
                fo = fo.getParent();
                fo = fo.getFileObject(SuiteConstants.INSTANCE_PROPERTIES_PATH);
                if (fo != null) {
                    msg = " The selected project is an inner project of an embedded serverProject";
                    return msg;
                }
            }

            return null;
        }
    }
}//class
