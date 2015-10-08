package org.netbeans.modules.jeeserver.base.embedded.webapp.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Properties;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static javax.swing.Action.NAME;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;

import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

@ActionID(
        category = "Project",
        id = "org.netbeans.modules.embedded.actions.ESAddWarRefAction")
@ActionRegistration(
        displayName = "CTL_ESAddWarRefAppAction",lazy=false)
@ActionReference(path = "Projects/Actions", position = 0)
@Messages("CTL_ESAddWarRefAction=Add .war Achive")
public final class AddWarRefAction extends AbstractAction implements ContextAwareAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        assert false;
    }

    @Override
    public Action createContextAwareInstance(Lookup context) {
        return new AddWarRefAction.ContextAction(context);
    }

    public static Action getAddWarRefAction(Lookup context) {
        return new AddWarRefAction.ContextAction(context);
    }

    private static final class ContextAction extends AbstractAction {

        private RequestProcessor.Task task;
        private final Project project;

        public ContextAction(Lookup context) {
            project = context.lookup(Project.class);
            // TODO state for which projects action should be enabled
            //boolean isEmbedded = SuiteUtil.isEmbedded(project);
            boolean isEmbedded = false;
            setEnabled(isEmbedded);
            // we need to hide when disabled putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);            
            setEnabled(isEmbedded);
            // we need to hide when disabled putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);            
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, !isEmbedded);
            // TODO menu item label with optional mnemonics
            putValue(NAME, "&Add Archive .war File");

            task = new RequestProcessor("AddProjectBody").create(new Runnable() { // NOI18N
                @Override
                public void run() {
                    File baseDir = FileUtil.toFile(project.getProjectDirectory().getParent());
                    FileChooserBuilder fcb = new FileChooserBuilder("")
                            .setTitle("Choose war-archive file")
                            .setFilesOnly(true)
                            .setDefaultWorkingDirectory(baseDir)
                            .addFileFilter(new FileNameExtensionFilter("Archive files(*.war)", "war"));
                    JFileChooser fc = fcb.createFileChooser();
                    //fc.setFileFilter(new ProjectFilter());
                    int choosed = fc.showOpenDialog(null);
                    if (choosed == JFileChooser.APPROVE_OPTION) {
                        File selectedFile = fc.getSelectedFile();
                        FileObject webappFo = FileUtil.toFileObject(selectedFile);

                        Properties props = new Properties();
                        props.setProperty("webAppLocation", FileUtil.normalizePath(selectedFile.getAbsolutePath()));
                        String selectedPath = FileUtil.normalizePath(webappFo.getPath());

                        FileObject targetFolder = project.getProjectDirectory().getFileObject(SuiteConstants.REG_WEB_APPS_FOLDER);
                        String selectedFileName = webappFo.getName() + "." + SuiteConstants.WAR_REF;//".warref";                        
                        String fileName = selectedFileName;
                        if (targetFolder.getFileObject(selectedFileName) != null) {
                            props = BaseUtil.loadProperties(targetFolder.getFileObject(selectedFileName));
                            String existingPath = FileUtil.normalizePath(props.getProperty("webAppLocation"));
                            if (selectedPath.equals(existingPath)) {
                                return;
                            }
                            fileName = FileUtil.findFreeFileName(targetFolder, selectedFile.getName(), SuiteConstants.WAR_REF);
                            fileName += "." + SuiteConstants.WAR_REF;
                        }
                        BaseUtil.storeProperties(props, targetFolder, fileName);
                        
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
