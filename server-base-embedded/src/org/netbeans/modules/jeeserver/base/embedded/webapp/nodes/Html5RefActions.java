package org.netbeans.modules.jeeserver.base.embedded.webapp.nodes;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static javax.swing.Action.NAME;
import javax.xml.transform.TransformerException;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteConstants;
import org.netbeans.modules.jeeserver.base.deployment.actions.WebAppCommandActions;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteUtil;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.netbeans.modules.jeeserver.base.embedded.project.PomXmlUtil;
import org.netbeans.spi.project.ActionProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.EditableProperties;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

public class Html5RefActions {

    public static boolean assignHtml5ExternalServer(BaseDeploymentManager dm, FileObject webappFo) {
        boolean success = true;

        //
        // Change private/private.properties in the Html5 application
        //
        Properties html5Props = BaseUtil.loadProperties(webappFo.getFileObject("nbproject/project.properties"));
        String appName = webappFo.getNameExt();
        FileObject fo = webappFo.getFileObject("nbproject/private/private.properties");
        EditableProperties ep = BaseUtil.loadEditableProperties(fo);

        String contextPath = BaseUtil.resolve(SuiteConstants.HTML5_WEB_CONTEXT_ROOT_PROP, html5Props);
        if (contextPath == null) {
            contextPath = "/" + appName;
        }
        if ( dm.buildUrl() == null ) {
            return false;
        }
        String url = dm.buildUrl() + contextPath;

        boolean accept = true;
        if (ep.getProperty("server").equals("EXTERNAL")) {
            String eurl = ep.getProperty("external.project.url");
            if (!url.equals(eurl)) {
                StringBuilder sb = new StringBuilder();
                sb.append("The Html5 Application '")
                        .append(appName)
                        .append("' is already using an external server: ")
                        .append(eurl)
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
        if (accept) {
            ep.setProperty("server", "EXTERNAL");
            ep.setProperty("external.project.url", url);
            SuiteUtil.storeEditableProperties(ep, fo);
            webappFo.getFileObject("nbproject/private/private.properties").refresh();
        }

        return success;
    }

    public static class HtmRefRunAction extends AbstractAction implements ContextAwareAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            assert false;
        }

        @Override
        public Action createContextAwareInstance(Lookup context) {
            return new HtmRefRunAction.ContextAction(context);
        }

        public static Action getAddHtmRefRunAction(Lookup context) {
            return new HtmRefRunAction.ContextAction(context);
        }

        private static final class ContextAction extends AbstractAction implements ProgressListener {

            //private final Project serverProject;
            private final FileObject refFo;
            private final BaseDeploymentManager dm;
            private String webAppLocation;
            private String urlStr;
            private String contextPath;
            private Project html5Project;

            public ContextAction(Lookup context) {
                DataObject dataObj = context.lookup(DataObject.class);
                refFo = dataObj.getPrimaryFile();
                //serverProject = FileOwnerQuery.getOwner(refFo);
                putValue(NAME, "&Run");
                dm = BaseUtil.managerOf(context);

            }

            public @Override
            void actionPerformed(ActionEvent e) {
                Properties props = BaseUtil.loadProperties(refFo);
                if (props == null) {
                    return;
                }
                webAppLocation = props.getProperty(SuiteConstants.WEB_APP_LOCATION_PROP);
                if (webAppLocation == null) {
                    return;
                }
                
                FileObject html5ProjFo = FileUtil.toFileObject(new File(webAppLocation));
                if ( ! assignHtml5ExternalServer(dm, html5ProjFo)){
                    return;
                }                
                html5Project = FileOwnerQuery.getOwner(html5ProjFo);
                Properties html5ProjProps = BaseUtil.loadProperties(html5ProjFo.getFileObject("nbproject/project.properties"));
                contextPath = html5ProjProps.getProperty("web.context.root");
                if (contextPath == null) {
                    contextPath = "/" + html5ProjFo.getName();// + "/" + html5ProjProps.getProperty(SuiteConstants.HTML5_SITE_ROOT_PROP);
                }

                String port = dm.getInstanceProperties().getProperty(SuiteConstants.HTTP_PORT_PROP);
                String host = dm.getInstanceProperties().getProperty(SuiteConstants.HOST_PROP);
                urlStr = "http://" + host + ":" + port + contextPath;

                if (WarRefActions.startServer(dm, dm.getServerProject(), this) == null) {
                    HtmRefDeployAction.perform(dm, contextPath, webAppLocation);
                    WebAppCommandActions.doInvokeAction(ActionProvider.COMMAND_RUN, html5Project);
                }

            }

            @Override
            public void handleProgressEvent(ProgressEvent pe) {
                if (pe.getDeploymentStatus().isCompleted()) {
                    HtmRefDeployAction.perform(dm, contextPath, webAppLocation);
                    WebAppCommandActions.doInvokeAction(ActionProvider.COMMAND_RUN, html5Project);
                }
            }
        }//class

    }//class    
    /////////////////////////                //////////////////////  
    /////////////////////////  DEPLOY ACTION //////////////////////
    /////////////////////////                //////////////////////  

    public static class HtmRefDeployAction extends AbstractAction implements ContextAwareAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            assert false;
        }

        /**
         * The method is used by the {@link WarRefRunAction }.
         *
         * @param dm
         * @param contextPath
         * @param webAppLocation
         */
        public static void perform(final BaseDeploymentManager dm, final String contextPath, final String webAppLocation) {

            RequestProcessor.Task rp = RequestProcessor.getDefault().post(new Runnable() {

                @Override
                public void run() {
                    if (!dm.pingServer()) {
                        return;
                    }
                    dm.getSpecifics().execCommand(dm, SuiteUtil.createCommand("deploy", contextPath, webAppLocation, SuiteConstants.DEPLOY_HTML5_PROJECTTYPE));
                }
            });

            rp.waitFinished();

        }

        @Override
        public Action createContextAwareInstance(Lookup context) {
            return new HtmRefDeployAction.ContextAction(context);
        }

        private static final class ContextAction extends AbstractAction implements ProgressListener {

            private final Project serverProject;
            private final FileObject refFo;
            private final BaseDeploymentManager dm;
            private String contextPath;
            private String webAppLocation;
            private Project html5Project;

            public ContextAction(Lookup context) {

                DataObject wardo = context.lookup(DataObject.class);
                refFo = wardo.getPrimaryFile();
                serverProject = FileOwnerQuery.getOwner(refFo);
                dm = BaseUtil.managerOf(context);
                putValue(NAME, "&Deploy");
            }

            public @Override
            void actionPerformed(ActionEvent e) {

                Properties props = BaseUtil.loadProperties(refFo);
                if (props == null) {
                    return;
                }
                webAppLocation = props.getProperty(SuiteConstants.WEB_APP_LOCATION_PROP);
                if (webAppLocation == null) {
                    return;
                }
                
                        
                FileObject html5ProjFo = FileUtil.toFileObject(new File(webAppLocation));
//                Html5RefActions.assignHtml5ExternalServer(serverProject, html5ProjFo);
                if ( ! assignHtml5ExternalServer(dm, html5ProjFo)){
                    return;
                }                
                
                html5Project = FileOwnerQuery.getOwner(html5ProjFo);
                Properties html5ProjProps = BaseUtil.loadProperties(html5ProjFo.getFileObject("nbproject/project.properties"));
                contextPath = html5ProjProps.getProperty("web.context.root");
                if (contextPath == null) {
                    contextPath = "/" + html5ProjFo.getName();// + "/" + html5ProjProps.getProperty(SuiteConstants.HTML5_SITE_ROOT_PROP);
                }

                if (WarRefActions.startServer(dm, serverProject, this) == null) {
                    handleProgressEvent(null);
                }

            }

            @Override
            public void handleProgressEvent(ProgressEvent pe) {
                if (!(pe == null || pe.getDeploymentStatus().isCompleted())) {
                    return;
                }
                RequestProcessor.Task rp = RequestProcessor.getDefault().post(new Runnable() {
                    @Override
                    public void run() {
                        if (!dm.pingServer()) {
                            return;
                        }
                        dm.getSpecifics().execCommand(dm, SuiteUtil.createCommand("deploy", contextPath, webAppLocation, SuiteConstants.DEPLOY_HTML5_PROJECTTYPE));
                    }
                });

            }
        }//class

    }//class

    /////////////////////////                   //////////////////////  
    /////////////////////////  UNDEPLOY ACTION  //////////////////////
    /////////////////////////                   //////////////////////  
    public static final class HtmRefUndeployAction extends AbstractAction implements ContextAwareAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            assert false;
        }

        public static void perform(final BaseDeploymentManager dm, final String contextPath, final String webApprLocation) {

            RequestProcessor.Task rp = RequestProcessor.getDefault().post(new Runnable() {

                @Override
                public void run() {
                    if (!dm.pingServer()) {
                        return;
                    }
                    dm.getSpecifics().execCommand(dm, SuiteUtil.createCommand("undeploy", contextPath, webApprLocation, SuiteConstants.DEPLOY_HTML5_PROJECTTYPE));
                }
            });

        }

        @Override
        public Action createContextAwareInstance(Lookup context) {
            return new HtmRefUndeployAction.ContextAction(context);
        }

        public static Action getAddHtmrefUndeployAction(Lookup context) {
            return new HtmRefUndeployAction.ContextAction(context);
        }

        private static final class ContextAction extends AbstractAction implements ProgressListener {

            private final Project serverProject;
            private final FileObject refFo;
            private final BaseDeploymentManager dm;
            private String webAppLocation;
            private String contextPath;
            private Project html5Project;

            public ContextAction(Lookup context) {

                DataObject dataObj = context.lookup(DataObject.class);
                refFo = dataObj.getPrimaryFile();
                serverProject = FileOwnerQuery.getOwner(refFo);
                dm = BaseUtil.managerOf(context);
                putValue(NAME, "&Undeploy");
            }

            public @Override
            void actionPerformed(ActionEvent e) {
                FileObject pd = serverProject.getProjectDirectory();
Path p = Paths.get(pd.getPath(),"server-project/pom.xml");
PomXmlUtil pu = new PomXmlUtil(p);
Path t = Paths.get(pd.getPath(),"server-project/pom-new.xml");
                try {
                    pu.save1(t);
                } catch (TransformerException ex) {
                    BaseUtil.out("^^^^^^^^ EXCEPTION pomXml" + ex.getMessage());
                }

                Properties props = BaseUtil.loadProperties(refFo);
                if (props == null) {
                    return;
                }
                webAppLocation = props.getProperty(SuiteConstants.WEB_APP_LOCATION_PROP);
                if (webAppLocation == null) {
                    return;
                }

                FileObject html5ProjFo = FileUtil.toFileObject(new File(webAppLocation));
                html5Project = FileOwnerQuery.getOwner(html5ProjFo);
                Properties html5ProjProps = BaseUtil.loadProperties(html5ProjFo.getFileObject("nbproject/project.properties"));
                contextPath = html5ProjProps.getProperty("web.context.root");
                if (contextPath == null) {
                    contextPath = "/" + html5ProjFo.getName();// + "/" + html5ProjProps.getProperty(SuiteConstants.HTML5_SITE_ROOT_PROP);
                }

                if (WarRefActions.startServer(dm, serverProject, this) == null) {
                    handleProgressEvent(null);
                }

            }

            @Override
            public void handleProgressEvent(ProgressEvent pe) {

                if (!(pe == null || pe.getDeploymentStatus().isCompleted())) {
                    return;
                }

                RequestProcessor.Task rp = RequestProcessor.getDefault().post(new Runnable() {

                    @Override
                    public void run() {
                        if (!dm.pingServer()) {
                            return;
                        }
                        dm.getSpecifics().execCommand(dm, SuiteUtil.createCommand("undeploy", contextPath, webAppLocation, SuiteConstants.DEPLOY_HTML5_PROJECTTYPE));
                    }
                });

            }
        }//class

    }//class

    public static class HtmRefShowBrowserAction extends AbstractAction implements ContextAwareAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            assert false;
        }

        @Override
        public Action createContextAwareInstance(Lookup context) {
            return new HtmRefShowBrowserAction.ContextAction(context);
        }

        public static Action getAddHtmRefShowBrowserAction(Lookup context) {
            return new HtmRefShowBrowserAction.ContextAction(context);
        }

        private static final class ContextAction extends AbstractAction implements ProgressListener {

            private final Project project;
            private final FileObject htmrefFo;
            private final BaseDeploymentManager dm;
            private String webAppLocation;
            private String urlStr;
            private String contextPath;

            public ContextAction(Lookup context) {
                DataObject dataObj = context.lookup(DataObject.class);
                htmrefFo = dataObj.getPrimaryFile();
                project = FileOwnerQuery.getOwner(htmrefFo);
                putValue(NAME, "&Show in browser");
                dm = BaseUtil.managerOf(context);

            }

            public @Override
            void actionPerformed(ActionEvent e) {
                Properties props = BaseUtil.loadProperties(htmrefFo);
                if (props == null) {
                    return;
                }
                webAppLocation = props.getProperty(SuiteConstants.WEB_APP_LOCATION_PROP);
                if (webAppLocation == null) {
                    return;
                }
                FileObject html5ProjFo = FileUtil.toFileObject(new File(webAppLocation));
                //Properties html5ProjProps = BaseUtils.loadProperties(html5ProjFo.getFileObject("nbproject/project.properties"));
                contextPath = "/" + html5ProjFo.getName();// + "/" + html5ProjProps.getProperty(SuiteConstants.HTML5_SITE_ROOT_PROP);
                String port = dm.getInstanceProperties().getProperty(SuiteConstants.HTTP_PORT_PROP);
                String host = "localhost";
                urlStr = "http://" + host + ":" + port + contextPath;

                if (WarRefActions.startServer(dm, project, this) == null) {
                    show();
                }

            }

            void show() {
                try {
                    HtmRefDeployAction.perform(dm, contextPath, webAppLocation);
                    BaseUtil.out("ShowBrowser: urlStr=" + urlStr);
                    HtmlBrowser.URLDisplayer.getDefault().showURL(new URL(urlStr));
                } catch (MalformedURLException ex) {
                    BaseUtil.out("ShowBrowser: urlStr=" + urlStr + "; EXCEPTION "  + ex.getMessage());                    
                    Logger.getLogger(org.netbeans.modules.jeeserver.base.embedded.webapp.nodes.WarRefActions.class.getName()).log(Level.INFO, null, ex);
                }
            }

            @Override
            public void handleProgressEvent(ProgressEvent pe) {
                if (pe.getDeploymentStatus().isCompleted()) {
                    show();
                }
            }
        }//class

    }//class    

}//class
