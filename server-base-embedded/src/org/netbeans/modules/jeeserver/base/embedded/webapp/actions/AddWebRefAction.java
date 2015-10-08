package org.netbeans.modules.jeeserver.base.embedded.webapp.actions;

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
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteConstants;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteUtil;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.spi.project.AuxiliaryProperties;
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
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

@ActionID(
        category = "Project",
        id = "org.netbeans.modules.embedded.actions.ESAddWebRefAction")
@ActionRegistration(
        displayName = "CTL_ESAddWebRefAppAction", lazy = false)
@ActionReference(path = "Projects/Actions", position = 0)
@Messages("CTL_ESAddWebRefAction=Add Web Application")
public final class AddWebRefAction extends AbstractAction implements ContextAwareAction {

    private static final Logger LOG = Logger.getLogger(AddWebRefAction.class.getName());

    @Override
    public void actionPerformed(ActionEvent e) {
        assert false;
    }

    @Override
    public Action createContextAwareInstance(Lookup context) {
        return new AddWebRefAction.ContextAction(context, false);
    }

    public static Action getAddWebRefAction(Lookup context) {
        return new AddWebRefAction.ContextAction(context, true);
    }

    private static final class ContextAction extends AbstractAction {

        private RequestProcessor.Task task;
        private final Project project;

        public ContextAction(Lookup context, boolean enabled) {
            project = context.lookup(Project.class);
            //boolean isEmbedded = SuiteUtil.isEmbedded(project);
            boolean isEmbedded = false;
            setEnabled(isEmbedded);
            // we need to hide when disabled putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);            
            // we need to hide when disabled putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);            
            //putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, (! isEmbedded) || ! enabled);
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, !isEmbedded);
            // TODO menu item label with optional mnemonics
            putValue(NAME, "&Add Existing Web Application");

            task = new RequestProcessor("AddProjectBody").create(new Runnable() { // NOI18N
                @Override
                public void run() {
                    JFileChooser fc = ProjectChooser.projectChooser();
                    int choosed = fc.showOpenDialog(null);
                    if (choosed == JFileChooser.APPROVE_OPTION) {
                        File selectedFile = fc.getSelectedFile();
                        FileObject webappFo = FileUtil.toFileObject(selectedFile);
                        String msg = ProjectFilter.check(project, webappFo);
                        if (msg != null) {
                            NotifyDescriptor d
                                    = new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE);
                            DialogDisplayer.getDefault().notify(d);
                            return;
                        }

                        if (!ProjectFilter.accept(project, webappFo)) {
                            // All error messages are shown
                            return;
                        }
                        String contextPath = WebModule.getWebModule(webappFo).getContextPath();
                        Properties props = new Properties();
                        props.setProperty("contextPath", contextPath);
                        props.setProperty("webAppLocation", FileUtil.normalizePath(webappFo.getPath()));
                        String selectedPath = FileUtil.normalizePath(webappFo.getPath());

                        FileObject targetFolder = project.getProjectDirectory().getFileObject(SuiteConstants.REG_WEB_APPS_FOLDER);
                        String selectedFileName = selectedFile.getName() + "." + SuiteConstants.WEB_REF;//".webref";                        
                        String fileName = selectedFileName;

                        if (targetFolder.getFileObject(selectedFileName) != null) {

                            props = BaseUtil.loadProperties(targetFolder.getFileObject(selectedFileName));
                            String existingPath = FileUtil.normalizePath(props.getProperty("webAppLocation"));

                            if (selectedPath.equals(existingPath)) {
                                return;
                            }
                            fileName = FileUtil.findFreeFileName(targetFolder, selectedFile.getName(), SuiteConstants.WEB_REF);
                            fileName += "." + SuiteConstants.WEB_REF;
                        }
                        SuiteUtil.storeProperties(props, targetFolder, fileName);

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

    public boolean assignHtml5ExternalServer(Project serverProject, File selectedFile) {
        boolean success = true;

        return success;
    }

    public static class ProjectFilter {

        public static String check(Project serverProject, FileObject webappFo) {

            if (webappFo == null) {
                return "Cannot be null";
            }
            String msg = "The selected project is not a Web Project ";
            Project webProj = FileOwnerQuery.getOwner(webappFo);
            if (webProj == null) {
                return msg + "(not a project)";
            }
            FileObject fo = webProj.getProjectDirectory().getFileObject("nbproject/project.xml");
            if (fo != null && SuiteUtil.projectTypeByProjectXml(fo).equals(SuiteConstants.HTML5_PROJECTTYPE)) {
                return "The selected project is a Html5 Project ";
            }
            fo = webappFo.getParent();
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

        public static final String HINT_DEPLOY_J2EE_SERVER_ID = "netbeans.deployment.server.id";

        public static boolean accept(Project serverProject, FileObject webappFo) {

            Project webProj = FileOwnerQuery.getOwner(webappFo);
            J2eeModuleProvider provider = SuiteUtil.getJ2eeModuleProvider(webProj);

            if (provider == null) {
                return false;
            }

            String webappUri = provider.getServerInstanceID();
            String uri = BaseUtil.getServerInstanceId(serverProject.getLookup());

            if (uri.equals(webappUri)) {
                return true;
            }
            //
            // webappFo represents a Web Project registered on another server
            //
           // String serverId = provider.getServerID();
            //web app registered to another server
            Project p = findServerByWebProjectUri(webappUri, serverProject);

            // We found a server project the web project is registered on.
            // if it is webref we must delete webref.
            // Anyway we show dialog to ask a developer
            boolean confirmed = p == null ? true : notifyAccept(webappFo.getNameExt(), p.getProjectDirectory().getNameExt());

            if (confirmed && p != null) {
                deleteWebRef(serverProject, webappFo);
            }
            
            if (!confirmed) {
                return false;
            }

            if (BaseUtil.isMavenProject(webProj)) {
                AuxiliaryProperties ap = webProj.getLookup().lookup(AuxiliaryProperties.class);
                String mavenId = ap.get("org-netbeans-modules-maven-j2ee.netbeans_2e_deployment_2e_server_2e_id", false);

                if (mavenId != null) {
                    ap.put("org-netbeans-modules-maven-j2ee.netbeans_2e_deployment_2e_server_2e_id", uri, false);
                } else {
                    mavenId = ap.get("org-netbeans-modules-maven-j2ee.netbeans_2e_deployment_2e_server_2e_id", true);
                    if (mavenId != null) {
                        ap.put("org-netbeans-modules-maven-j2ee.netbeans_2e_deployment_2e_server_2e_id", uri, true);
                    } else {
                        mavenId = ap.get(HINT_DEPLOY_J2EE_SERVER_ID, true);
                        if (mavenId != null) {
                            ap.put(HINT_DEPLOY_J2EE_SERVER_ID, uri, true);
                        } else {
                            mavenId = ap.get(HINT_DEPLOY_J2EE_SERVER_ID, false);
                            if (mavenId != null) {
                                ap.put(HINT_DEPLOY_J2EE_SERVER_ID, uri, false);
                            }
                        }
                    }
                }
            }

            provider.setServerInstanceID(uri);
            provider.getConfigSupport().ensureConfigurationReady();

            //webProj.getLookup().lookup(WebModuleProviderImpl.class);            
            //provider = SuiteUtil.getJ2eeModuleProvider(webProj);
            //BaseUtils.out(provider.getDeploymentName());
            return true;

        }
        
        private static void deleteWebRef(Project serverProject, FileObject webappFo) {
                FileObject fo = serverProject.getProjectDirectory().getFileObject(SuiteConstants.REG_WEB_APPS_FOLDER);
                // the FileObject fo maybe null when another server is not an embedded server
                if (fo != null) {
                    for (FileObject f : fo.getChildren()) {
                        if (!f.isFolder() && f.getNameExt().equals(webappFo.getName() + "." + SuiteConstants.WEB_REF)) {
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
        private static Project findServerByWebProjectUri(String webappUri, Project serverProject) {
            Project[] ps = OpenProjects.getDefault().getOpenProjects();
            Project result = null;
            for (Project p : ps) {
                if (serverProject != p && SuiteUtil.isServerProject(p)
                        && webappUri.equals(BaseUtil.getServerInstanceId(p.getLookup()))) {
                    result = p;
                    break;
                }
            }
            return result;
        }

        private static boolean notifyAccept(String webapp, String server) {
            String notifyMsg = NbBundle.getMessage(ProjectFilter.class,
                    "MSG_Web_Project", webapp);
            //notifyMsg += "\t" + msg + System.lineSeparator();
            notifyMsg += NbBundle.getMessage(ProjectFilter.class,
                    "MSG_Already_registered", server);
            notifyMsg += System.lineSeparator()
                    + NbBundle.getMessage(ProjectFilter.class,
                            "MSG_Ask_change_server");
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                    notifyMsg,
                    NotifyDescriptor.YES_NO_OPTION);
            //nd.setTitle(NbBundle.getMessage(ProjectFilter.class,
            //        "TITLE_Not_unzipped_wars"));
            nd.setTitle(webapp);
            if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.YES_OPTION) {
                return true;
            } else {
                return false;
            }
        }
    }
}//class
