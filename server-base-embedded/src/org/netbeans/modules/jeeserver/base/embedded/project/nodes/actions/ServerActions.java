/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jeeserver.base.embedded.project.nodes.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static javax.swing.Action.NAME;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.base.deployment.ServerInstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.netbeans.modules.jeeserver.base.embedded.project.SuiteManager;
import org.netbeans.modules.jeeserver.base.embedded.project.wizard.ServerInstanceCustomizerWizardAction;
import org.netbeans.modules.jeeserver.base.embedded.project.wizard.ServerInstanceAddExistingWizardAction;
import org.netbeans.modules.jeeserver.base.embedded.project.wizard.ServerInstanceAntBuildExtender;
import org.netbeans.modules.jeeserver.base.embedded.project.wizard.InstanceWizardAction;
import org.netbeans.modules.jeeserver.base.embedded.project.wizard.MainClassChooserPanelVisual;
import org.netbeans.modules.jeeserver.base.embedded.project.wizard.ServerInstanceBuildExtender;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteConstants;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteUtil;
//import org.netbeans.modules.project.uiapi.Utilities;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.DialogDescriptor;
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
public class ServerActions {

    public static class NewMavenProjectAction extends AbstractAction implements ContextAwareAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            assert false;
        }

        @Override
        public Action createContextAwareInstance(Lookup context) {
            return new NewMavenProjectAction.ContextAction(context);
        }

        public static Action getContextAwareInstance(Lookup context) {
            return new NewMavenProjectAction.ContextAction(context);
        }

        private static final class ContextAction extends InstanceWizardAction { //implements ProgressListener {

            public ContextAction(Lookup context) {
                super(context);
                putValue(NAME, "&New Server Instance  as Maven Project");
            }

            @Override
            protected boolean isMavenBased() {
                return true;
            }
        }//class
    }//class

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

        private static final class ContextAction extends InstanceWizardAction { //implements ProgressListener {

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

            private final RequestProcessor.Task task;
            private final Lookup context;

            public ContextAction(Lookup context) {
                this.context = context;
                putValue(NAME, "&Add  Existing Project");
                task = new RequestProcessor("AddBody").create(new Runnable() { // NOI18N
                    @Override
                    public void run() {
                        JFileChooser fc = ProjectChooser.projectChooser();
                        int choosed = fc.showOpenDialog(null);
                        if (choosed == JFileChooser.APPROVE_OPTION) {
                            File selectedFile = fc.getSelectedFile();
                            FileObject appFo = FileUtil.toFileObject(selectedFile);
                            String msg = ProjectFilter.check(appFo);
                            if (msg != null) {
                                NotifyDescriptor d
                                        = new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE);
                                DialogDisplayer.getDefault().notify(d);
                                return;
                            }

                            ServerInstanceAddExistingWizardAction action
                                    = new ServerInstanceAddExistingWizardAction(context, selectedFile);
                            action.actionPerformed(null);

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

            private Lookup context;

            public ContextAction(Lookup context) {
                this.context = context;
                putValue(NAME, "&Remove Server Instance");
                //dm = BaseUtils.managerOf(context);

            }

            @Override
            public void actionPerformed(ActionEvent e) {
                BaseDeploymentManager dm = context.lookup(ServerInstanceProperties.class).getManager();
                Project instanceProject = dm.getServerProject();

                if (instanceProject != null) {
                    ServerInstanceBuildExtender extender;
                    if (BaseUtil.isAntProject(instanceProject)) {
                        extender = new ServerInstanceAntBuildExtender(instanceProject);
                    } else {
                        extender = new ServerInstanceBuildExtender(instanceProject);
                    }

                    extender.disableExtender();
                }
                SuiteManager.removeInstance(context.lookup(ServerInstanceProperties.class).getUri());
            }
        }
    }
//        Utilities.getBuildExecutionSupportImplementation().    

    public static class InstancePropertiesAction extends AbstractAction implements ContextAwareAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            assert false;
        }

        @Override
        public Action createContextAwareInstance(Lookup context) {
            return new InstancePropertiesAction.ContextAction(context);
        }

        public static Action getContextAwareInstance(Lookup context) {
            return new InstancePropertiesAction.ContextAction(context);
        }

        private static final class ContextAction extends AbstractAction { //implements ProgressListener {

            private final Lookup context;
            private final RequestProcessor.Task task;

            public ContextAction(Lookup context) {
                this.context = context;
                putValue(NAME, "&Properties");
                task = new RequestProcessor("AddBody").create(new Runnable() { // NOI18N
                    @Override
                    public void run() {

                        ServerInstanceCustomizerWizardAction action
                                = new ServerInstanceCustomizerWizardAction(context, FileUtil.toFile(context.lookup(FileObject.class)));
                        action.actionPerformed(null);
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
    }

    public static class ProjectFilter {

        public static String check(FileObject appFo) {

            if (appFo == null) {
                return "Cannot be null";
            }
            String msg = "The selected project is not a Project ";
            Project proj = FileOwnerQuery.getOwner(appFo);
            if (proj == null) {
                return msg;
            }

            FileObject fo = proj.getProjectDirectory().getFileObject("nbproject/project.xml");
            if (fo != null && SuiteUtil.projectTypeByProjectXml(fo).equals(SuiteConstants.HTML5_PROJECTTYPE)) {
                return "The selected project is an Html5 Project ";
            }
            if (fo != null && SuiteUtil.projectTypeByProjectXml(fo).equals(SuiteConstants.WEB_PROJECTTYPE)) {
                return "The selected project is an Web Application";
            }
            if (BaseUtil.isMavenWebProject(proj)) {
                return "The selected project is a Maven  Web Application";
            }
            if (SuiteManager.getManager(proj) != null) {
                return "The selected project allready registered as a Server Instance";
            }
            if (BaseUtil.isMavenProject(proj) || BaseUtil.isAntProject(proj)) {
                return null;
            }

            return "The selected project is a Maven  Web Application";
        }
    }

    public static class DefineMainClassAction extends AbstractAction implements ContextAwareAction {

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
            return new DefineMainClassAction.ContextAction(context);
        }

        /**
         *
         * @param context a Lookup of the ServerInstancesRootNode object
         * @return
         */
        public static Action getContextAwareInstance(Lookup context) {
            return new DefineMainClassAction.ContextAction(context);
        }

        private static final class ContextAction extends AbstractAction { //implements ProgressListener {

            private final RequestProcessor.Task task;
            private final Lookup context;

            //private static final String CANCEL = "CANCEL";
            private static final String NO_MAIN_CLASS_FOUND = "No Main Class Found";

            public ContextAction(Lookup context) {
                this.context = context;
                FileObject fo = context.lookup(FileObject.class);
                final Project instanceProject = FileOwnerQuery.getOwner(fo);

                if (BaseUtil.isAntProject(instanceProject)) {
                    setEnabled(false);
                } else {
                    setEnabled(true);
                }
                
                putValue(NAME, "&Assign Main Class (maven projects only)");
                
                task = new RequestProcessor("AddBody").create(new Runnable() { // NOI18N
                    @Override
                    public void run() {
                        JButton sb = createSelectButton();
                        JButton cb = createCancelButton();
                        
                       // MainClassChooserPanelVisual panel = new MainClassChooserPanelVisual(sb,cb);
                        MainClassChooserPanelVisual panel = new MainClassChooserPanelVisual(sb,cb);                        
                        
                        String[] classes = BaseUtil.getMavenMainClasses(instanceProject);

                        if (classes.length == 0) {
                            classes = new String[]{NO_MAIN_CLASS_FOUND};
                            sb.setEnabled(false);
                        }
                        panel.getMainClassesList().setListData(classes);
                        String msg = "Select Main Class for Server Execution";
                        DialogDescriptor dd = new DialogDescriptor(panel, msg,
                                true, new Object[]{sb, cb}, cb, DialogDescriptor.DEFAULT_ALIGN, null, null);
//                                true, new Object[]{"Select Main Class", "Cancel"}, "Cancel", DialogDescriptor.DEFAULT_ALIGN, null, null);

                        DialogDisplayer.getDefault().notify(dd);

                        if (dd.getValue() == sb ) {
                            int idx = panel.getMainClassesList().getSelectedIndex();
                            if ( idx < 0 ) {
                                return;
                            }
                            String mainClass = (String)panel.getMainClassesList().getSelectedValue();
                            String uri = SuiteManager.getManager(instanceProject).getUri();
                            InstanceProperties.getInstanceProperties(uri)
                                    .setProperty( SuiteConstants.MAVEN_MAIN_CLASS_PROP, mainClass);
                            
                        }

                    }
                });
            }

            protected JButton createSelectButton() {
                JButton button = new javax.swing.JButton();
                button.setName("SELECT");
                org.openide.awt.Mnemonics.setLocalizedText(button, "Select Main Class");
                button.setEnabled(false);
                return button;

            }

            protected JButton createCancelButton() {
                JButton button = new javax.swing.JButton();
                button.setName("CANCEL");
                org.openide.awt.Mnemonics.setLocalizedText(button, "Cancel");
                return button;
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

}//class
