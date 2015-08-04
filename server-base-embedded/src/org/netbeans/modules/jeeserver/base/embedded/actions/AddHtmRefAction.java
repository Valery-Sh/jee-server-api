package org.netbeans.modules.jeeserver.base.embedded.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static javax.swing.Action.NAME;
import javax.swing.JFileChooser;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.embedded.utils.EmbConstants;
import org.netbeans.modules.jeeserver.base.embedded.utils.EmbUtils;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.EditableProperties;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

@ActionID(
        category = "Project",
        id = "org.netbeans.modules.embedded.actions.ESAddHtmRefAction")
@ActionRegistration(
        displayName = "ESAddHtmRefAction", lazy = false)
@ActionReference(path = "Projects/Actions", position = 0)
@Messages("ESAddHtmRefAction=Add Html5 Application")
public final class AddHtmRefAction extends AbstractAction implements ContextAwareAction {

    private static final Logger LOG = Logger.getLogger(AddHtmRefAction.class.getName());

    @Override
    public void actionPerformed(ActionEvent e) {
        assert false;
    }

    @Override
    public Action createContextAwareInstance(Lookup context) {
        return new AddHtmRefAction.ContextAction(context, false);
    }

    public static Action getAddHtmRefAction(Lookup context) {
        return new AddHtmRefAction.ContextAction(context, true);
    }

    private static final class ContextAction extends AbstractAction {

        private final RequestProcessor.Task task;
        private final Project serverProject;

        public ContextAction(Lookup context, boolean enabled) {
            serverProject = context.lookup(Project.class);

            boolean isEmbedded = EmbUtils.isEmbedded(serverProject);
            setEnabled(isEmbedded);
            // we need to hide when disabled putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);            
            // we need to hide when disabled putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);            
            //putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, (! isEmbedded) || ! enabled);
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, !isEmbedded);
            // TODO menu item label with optional mnemonics
            putValue(NAME, "&Add Existing Html5 Application");

            task = new RequestProcessor("AddProjectBody").create(new Runnable() { // NOI18N
                @Override
                public void run() {
                    JFileChooser fc = ProjectChooser.projectChooser();
                    int choosed = fc.showOpenDialog(null);
                    if (choosed == JFileChooser.APPROVE_OPTION) {
                        File selectedFile = fc.getSelectedFile();
                        FileObject webappFo = FileUtil.toFileObject(selectedFile);
                        String msg = ProjectFilter.accept(serverProject, webappFo);
                        if (msg != null) {
                            NotifyDescriptor nd = new NotifyDescriptor.Message(msg);
                            nd.setTitle(webappFo.getNameExt());
                            DialogDisplayer.getDefault().notify(nd);
                            return;
                        }
                        assignHtml5ExternalServer(serverProject, selectedFile);
                    } else {
                        System.out.println("File access cancelled by user.");
                    }
                }
            });

        }

        public boolean assignHtml5ExternalServer(Project serverProject, File html5AppFile) {

            String html5RefName = html5AppFile.getName() + "." + EmbConstants.HTML_REF;
            FileObject html5AppFo = FileUtil.toFileObject(html5AppFile);

            Properties props = new Properties();

            String html5AppFoPath = FileUtil.normalizePath(html5AppFo.getPath());
            props.setProperty(EmbConstants.WEB_APP_LOCATION_PROP, html5AppFoPath);

            FileObject targetFolder = serverProject.getProjectDirectory().getFileObject(EmbConstants.WEBAPPLICATIONS_FOLDER);

            String fileName = html5RefName;

            if (targetFolder.getFileObject(html5RefName) != null) {
                // Allready registered  ( but we checked it earlier)
                fileName = FileUtil.findFreeFileName(targetFolder, html5AppFile.getName(), EmbConstants.HTML_REF);
                fileName += "." + EmbConstants.HTML_REF;
            }

            //
            // Change private/private.properties in the Html5 application
            //
            //FileObject fo = FileUtil.toFileObject(new File(html5AppFoPath));
            Properties html5Props = BaseUtils.loadProperties(html5AppFo.getFileObject("nbproject/project.properties"));

            String appName = html5AppFo.getNameExt();
            FileObject privateFo = html5AppFo.getFileObject("nbproject/private/private.properties");
            EditableProperties ep;
            if (privateFo == null) {
                if (html5AppFo.getFileObject("nbproject/private") == null) {
                    try {
                        privateFo = html5AppFo.getFileObject("nbproject").createFolder("private");
                        privateFo.createData("private", "properties");
                    } catch (IOException ex) {
                        LOG.log(Level.INFO, ex.getMessage());
                        return false;
                    }
                }
            }

            ep = BaseUtils.loadEditableProperties(privateFo);
            String contextPath = BaseUtils.resolve(BaseConstants.HTML5_WEB_CONTEXT_ROOT_PROP, html5Props);
            if (contextPath == null) {
                contextPath = "/" + appName;
            }

            String url = BaseUtils.managerOf(serverProject)
                    .buildUrl() + contextPath;

            String oldServerUrl = null;

            boolean accept = true;
            if (ep.getProperty("server") != null && ep.getProperty("server").equals("EXTERNAL")) {
                oldServerUrl = ep.getProperty("external.project.url");
                if (!url.equals(oldServerUrl)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("The Html5 Application '")
                            .append(appName)
                            .append("' is already using an external server: ")
                            .append(oldServerUrl)
                            .append(System.lineSeparator())
                            .append("Do you want to change it to : ")
                            .append(url)
                            .append(" ?");

                    NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                            sb,
                            NotifyDescriptor.YES_NO_OPTION);
                    nd.setTitle(appName);
                    if (DialogDisplayer.getDefault().notify(nd) != NotifyDescriptor.YES_OPTION) {
                        accept = false;
                    }
                }

            }
            if (!accept) {
                return false;
            }
            
            if ("EXTERNAL".equals(ep.getProperty("server"))) {
                deleteHtmlRef(html5AppFo);
            }

            FileObject publicFo = html5AppFo.getFileObject("nbproject/project.properties");

            if (publicFo != null) {
                EditableProperties publicProps = BaseUtils.loadEditableProperties(publicFo);
                publicProps.setProperty(EmbConstants.HTML5_SERVER_URI_PROP, BaseUtils.getUri(serverProject));
                BaseUtils.storeEditableProperties(publicProps, publicFo);
                publicFo.refresh();
            }
            ep.setProperty("server", "EXTERNAL");
            ep.setProperty("external.project.url", url);

            BaseUtils.storeEditableProperties(ep, privateFo);
            BaseUtils.storeProperties(props, targetFolder, fileName);
            html5AppFo.getFileObject("nbproject/private/private.properties").refresh();

            return true;
        }

        private void deleteHtmlRef(FileObject html5AppFo) {
            EditableProperties html5Props = EmbUtils.loadEditableProperties(html5AppFo.getFileObject("nbproject/project.properties"));
            String uri = html5Props.getProperty(EmbConstants.HTML5_SERVER_URI_PROP);
            if (uri != null) {
                InstanceProperties ip = InstanceProperties.getInstanceProperties(uri);
                if (ip != null) {
                    String location = ip.getProperty(EmbConstants.SERVER_LOCATION_PROP);
                    File file = new File(location);
                    if (location != null && new File(location).exists()) {
                        FileObject projDir = FileUtil.toFileObject(new File(location));
                        deleteHtmlRef(projDir, html5AppFo);
                    }
                }
            }

        }

        private void deleteHtmlRef(FileObject projFo, FileObject htmlappFo) {
            FileObject fo = projFo.getFileObject(EmbConstants.WEBAPPLICATIONS_FOLDER);
            // the FileObject fo maybe null when another server is not an embedded server
            if (fo != null) {
                for (FileObject f : fo.getChildren()) {
                    if (!f.isFolder() && f.getNameExt().equals(htmlappFo.getName() + "." + EmbConstants.HTML_REF)) {
                        try {
                            f.delete();
                        } catch (IOException ex) {
                            LOG.log(Level.INFO, ex.getMessage());
                        }
                        break;
                    }
                }
            }
        }

        public @Override
        void actionPerformed(ActionEvent e) {
            task.schedule(0);

            if ("waitFinished".equals(e.getActionCommand())) {
                task.waitFinished();
            }

        }
    }//class

    public static class ProjectFilter {

        static String accept(Project serverProject, FileObject webappFo) {
            String result = null;

            if (webappFo == null) {
                return "Cannot be null";
            }

            Project webProj = FileOwnerQuery.getOwner(webappFo);
            if (webProj == null) {
                result = "The selected file '" + webappFo.getNameExt() + "' is not a Project file";
            } else if (webappFo.getFileObject("nbproject/project.xml") != null) {
                String type = BaseUtils.projectTypeByProjectXml(webappFo.getFileObject("nbproject/project.xml"));
                if (!EmbConstants.HTML5_PROJECTTYPE.equals(type)) {
                    result = "The selected project '" + webappFo.getNameExt() + "' is not an Html5 Project ";
                }
            }

            if (result == null) {
                FileObject targetFolder = serverProject.getProjectDirectory().getFileObject(EmbConstants.WEBAPPLICATIONS_FOLDER);
                String selectedFileName = webappFo.getName() + "." + EmbConstants.HTML_REF;
                String selectedPath = FileUtil.normalizePath(webappFo.getPath());
                if (targetFolder.getFileObject(selectedFileName) != null) {

                    Properties props = BaseUtils.loadProperties(targetFolder.getFileObject(selectedFileName));
                    String existingPath = FileUtil.normalizePath(props.getProperty("webAppLocation"));
                    if (selectedPath.equals(existingPath)) {
                        result = "The selected project '" + webappFo.getNameExt() + "' has already been registered  ";

                    }
                }
            }
            return result;
        }

    }
}//class
