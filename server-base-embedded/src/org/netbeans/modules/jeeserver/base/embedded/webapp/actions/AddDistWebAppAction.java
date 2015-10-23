package org.netbeans.modules.jeeserver.base.embedded.webapp.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static javax.swing.Action.NAME;
import javax.swing.JFileChooser;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.netbeans.modules.jeeserver.base.embedded.project.SuiteManager;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteConstants;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteUtil;
import org.netbeans.modules.jeeserver.base.embedded.webapp.DistributedWebAppManager;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/*@ActionID(
 category = "Project",
 id = "org.netbeans.modules.jeeserver.base.embedded.webapp.actions.AddDistWebAppAction")
 @ActionRegistration(
 displayName = "CTL_AddDistWebAppAction", lazy = false)
 @ActionReference(path = "Projects/Actions", position = 0)
 @NbBundle.Messages("CTL_AddDistWebAppAction=Add Web Application")
 */
public final class AddDistWebAppAction extends AbstractAction implements ContextAwareAction {

    private static final Logger LOG = Logger.getLogger(AddWebRefAction.class.getName());

    public static final int ACCEPTED = 0;
    public static final int NOT_ACCEPTED = 2;
    public static final int NEEDS_CHANGE_SERVER = 4;

    @Override
    public void actionPerformed(ActionEvent e) {
        assert false;
    }

    @Override
    public Action createContextAwareInstance(Lookup context) {
        return new AddDistWebAppAction.ContextAction(context, false);
    }

    public static Action getAddDistWebAppAction(Lookup context) {
        return new AddDistWebAppAction.ContextAction(context, true);
    }

    private static final class ContextAction extends AbstractAction {

        private final RequestProcessor.Task task;
        private final Project serverInstance;

        public ContextAction(Lookup context, boolean enabled) {
            serverInstance = context.lookup(Project.class);
            // we need to hide when disabled putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);            
            // we need to hide when disabled putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);            
            //putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, (! isEmbedded) || ! enabled);
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, false);
            // TODO menu item label with optional mnemonics

            setEnabled(true);
            putValue(NAME, "&Add Web Application to Distribute");
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, false);

            task = new RequestProcessor("AddProjectBody").create(new Runnable() { // NOI18N
                @Override
                public void run() {
                    JFileChooser fc = ProjectChooser.projectChooser();
                    int choosed = fc.showOpenDialog(null);
                    if (choosed != JFileChooser.APPROVE_OPTION) {
                        return;
                    }
                    File selectedFile = fc.getSelectedFile();
                    FileObject webappFo = FileUtil.toFileObject(selectedFile);
                    Project webProj = FileOwnerQuery.getOwner(webappFo);
                    String msg = ProjectFilter.check(context, webappFo);
                    if (msg != null) {
                        NotifyDescriptor d
                                = new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE);
                        DialogDisplayer.getDefault().notify(d);
                        return;
                    }
                    String webappUri;

                    int accept = ProjectFilter.accept(context, webappFo);

                    if (accept == NOT_ACCEPTED) {
                        return;
                    } else if (accept == NEEDS_CHANGE_SERVER) {
                        webappUri = ProjectFilter.changeServer(context, webappFo);
                        if (webappUri == null) {
                            return;
                        }
                        BaseDeploymentManager m = SuiteManager.getManager(webappUri);
                        if ( m != null ) {
                            Project oldServer = m.getServerProject();
                            DistributedWebAppManager distManager = DistributedWebAppManager.getInstance(oldServer);
                            distManager.unregister(webProj);
                        }

                    }
                    DistributedWebAppManager distManager = DistributedWebAppManager.getInstance(serverInstance);
                    distManager.register(webProj);
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

        public static String check(Lookup context, FileObject webappFo) {

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
                return "The selected project is an Html5 Project ";
            }
            return null;
        }

        public static final String HINT_DEPLOY_J2EE_SERVER_ID = "netbeans.deployment.server.id";

        public static int accept(Lookup context, FileObject webappFo) {

            Project webProj = FileOwnerQuery.getOwner(webappFo);
            J2eeModuleProvider provider = SuiteUtil.getJ2eeModuleProvider(webProj);

            if (provider == null) {
                return NOT_ACCEPTED;
            }
            String webappUri = provider.getServerInstanceID();
            String uri = BaseUtil.getServerInstanceId(context);
            if (uri.equals(webappUri)) {
                return ACCEPTED;
            }
            return NEEDS_CHANGE_SERVER;
        }

        public static String changeServer(Lookup context, FileObject webappFo) {

            Project webProj = FileOwnerQuery.getOwner(webappFo);
            J2eeModuleProvider provider = SuiteUtil.getJ2eeModuleProvider(webProj);
            if (provider == null) {
                return null;
            }

            String webappUri = provider.getServerInstanceID(); // old server

            String uri = BaseUtil.getServerInstanceId(context); // target server
            
            if (webappUri != null && InstanceProperties.getInstanceProperties(webappUri) != null) {
                //
                // we need confirmation dialog
                //
                String suiteLoc = InstanceProperties.getInstanceProperties(webappUri).getProperty(SuiteConstants.SUITE_PROJECT_LOCATION);
                String displayAsServerName = provider.getServerID();

                if (suiteLoc != null) {
                    displayAsServerName = InstanceProperties.getInstanceProperties(webappUri).getProperty(BaseConstants.SERVER_LOCATION_PROP);
                    if (displayAsServerName != null) {
                        displayAsServerName = new File(displayAsServerName).getName();
                    }
                }

                if (displayAsServerName == null) {
                    displayAsServerName = "";
                } else {
                    displayAsServerName = "(" + displayAsServerName + ")";
                }
                boolean confirmed = notifyAccept(webappFo.getNameExt(), displayAsServerName);
                if (!confirmed) {
                    return null;
                }
            }
            //
            // Now set target server
            //
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

            return webappUri;

        }

/*        private static Project findServerByWebProjectUri(String webappUri, Project serverProject) {

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
*/
        private static boolean notifyAccept(String webapp, String serverId) {
            String notifyMsg = NbBundle.getMessage(ProjectFilter.class,
                    "MSG_Web_Project", webapp);
            //notifyMsg += "\t" + msg + System.lineSeparator();
            notifyMsg += NbBundle.getMessage(ProjectFilter.class,
                    "MSG_Already_registered", serverId);
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
