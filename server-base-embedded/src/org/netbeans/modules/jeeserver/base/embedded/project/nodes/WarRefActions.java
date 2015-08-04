package org.netbeans.modules.jeeserver.base.embedded.project.nodes;

import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static javax.swing.Action.NAME;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.deployment.actions.StartServerAction;
import org.netbeans.modules.jeeserver.base.embedded.utils.EmbConstants;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.base.embedded.utils.EmbUtils;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

public class WarRefActions {

    protected static ProgressObject startServer(BaseDeploymentManager dm, Project serverProject, ProgressListener l) {
        ProgressObject result = null;
        if (!dm.isServerRunning()) {
            result = StartServerAction.perform(serverProject.getLookup());
            result.addProgressListener(l);
        }
        return result;
    }

    public static class WarRefRunAction extends AbstractAction implements ContextAwareAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            assert false;
        }

        @Override
        public Action createContextAwareInstance(Lookup context) {
            return new WarRefRunAction.ContextAction(context);
        }

        public static Action getAddWarRefRunAction(Lookup context) {
            return new WarRefRunAction.ContextAction(context);
        }

        private static final class ContextAction extends AbstractAction implements ProgressListener {

            private final Project project;
            private final FileObject warrefFo;
            private final BaseDeploymentManager dm;
            private String contextPath;
            private String warLocation;
            private String urlStr;

            public ContextAction(Lookup context) {
                DataObject wardo = context.lookup(DataObject.class);
                warrefFo = wardo.getPrimaryFile();
                project = FileOwnerQuery.getOwner(warrefFo);
                putValue(NAME, "&Run war");
                dm = BaseUtils.managerOf(project);

            }

            public @Override
            void actionPerformed(ActionEvent e) {
                Properties props = BaseUtils.loadProperties(warrefFo);
                if (props == null) {
                    return;
                }
                contextPath = props.getProperty(EmbConstants.CONTEXTPATH_PROP);
                if (contextPath == null) {
                    contextPath = "/" + warrefFo.getName();
                }
                if (!contextPath.startsWith("/")) {
                    contextPath = "/" + contextPath;
                }
                warLocation = props.getProperty(EmbConstants.WEB_APP_LOCATION_PROP);
                if (warLocation == null) {
                    return;
                }
                String port = dm.getInstanceProperties().getProperty(EmbConstants.HTTP_PORT_PROP);
                String host = dm.getInstanceProperties().getProperty(EmbConstants.HOST_PROP);
                urlStr = "http://" + host + ":" + port;// + contextPath;

                if (WarRefActions.startServer(dm, project, this) == null) {
                    show();
                }

            }

            void show() {
                try {
                    String cp = WarRefDeployAction.perform(dm, contextPath, warLocation);
                    if ( cp != null && cp.length() != 0 ) {
                        urlStr += cp;
                    } else {
                        urlStr += contextPath; 
                    }
                    HtmlBrowser.URLDisplayer.getDefault().showURL(new URL(urlStr));
                } catch (MalformedURLException ex) {
                    Logger.getLogger(org.netbeans.modules.jeeserver.base.embedded.project.nodes.WarRefActions.class.getName()).log(Level.INFO, null, ex);
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
    /////////////////////////                //////////////////////  
    /////////////////////////  DEPLOY ACTION //////////////////////
    /////////////////////////                //////////////////////  

    public static class WarRefDeployAction extends AbstractAction implements ContextAwareAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            assert false;
        }

        /**
         * The method is used by the {@link WarRefRunAction }.
         *
         * @param dm
         * @param contextPath
         * @param warLocation
         */
        public static String perform(final BaseDeploymentManager dm, final String contextPath, final String warLocation) {
            final String[] ar = new String[1];
            RequestProcessor.Task rp = RequestProcessor.getDefault().post(new Runnable() {

                @Override
                public void run() {
                    if (!dm.pingServer()) {
                        ar[0] = null;
                        return;
                    }
                    ar[0] = dm.getSpecifics().execCommand(dm.getServerProject(), EmbUtils.createCommand("deploywar", contextPath, warLocation));
                }
            });
            
            rp.waitFinished();
            return ar[0];// realClassPath;
        }

        @Override
        public Action createContextAwareInstance(Lookup context) {
            return new WarRefDeployAction.ContextAction(context);
        }

        private static final class ContextAction extends AbstractAction implements ProgressListener {

            private final Project project;
            private final FileObject warrefFo;
            private final BaseDeploymentManager dm;
            private String contextPath;
            private String warLocation;

            public ContextAction(Lookup context) {

                DataObject wardo = context.lookup(DataObject.class);
                warrefFo = wardo.getPrimaryFile();
                project = FileOwnerQuery.getOwner(warrefFo);
                dm = BaseUtils.managerOf(project);
                putValue(NAME, "&Deploy");
            }

            public @Override
            void actionPerformed(ActionEvent e) {

                Properties props = BaseUtils.loadProperties(warrefFo);
                if (props == null) {
                    return;
                }
                contextPath = props.getProperty(EmbConstants.CONTEXTPATH_PROP);
                if (contextPath == null) {
                    contextPath = "/" + warrefFo.getName();
                }
                if (!contextPath.startsWith("/")) {
                    contextPath = "/" + contextPath;
                }
                warLocation = props.getProperty(EmbConstants.WEB_APP_LOCATION_PROP);
                if (warLocation == null) {
                    return;
                }

                final String cp = contextPath;

                if (WarRefActions.startServer(dm, project, this) == null) {
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
                        dm.getSpecifics().execCommand(dm.getServerProject(), EmbUtils.createCommand("deploywar", contextPath, warLocation));
                    }
                });

            }
        }//class

    }//class

    /////////////////////////                   //////////////////////  
    /////////////////////////  UNDEPLOY ACTION  //////////////////////
    /////////////////////////                   //////////////////////  
    public static final class WarRefUndeployAction extends AbstractAction implements ContextAwareAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            assert false;
        }

        public static void perform(final BaseDeploymentManager dm, final String contextPath, final String warLocation) {

            RequestProcessor.Task rp = RequestProcessor.getDefault().post(new Runnable() {

                @Override
                public void run() {
                    if (!dm.pingServer()) {
                        return;
                    }
                    dm.getSpecifics().execCommand(dm.getServerProject(), EmbUtils.createCommand("undeploywar", contextPath, warLocation));

                }
            });

        }

        @Override
        public Action createContextAwareInstance(Lookup context) {
            return new WarRefUndeployAction.ContextAction(context);
        }

        public static Action getAddWarrefUndeployAction(Lookup context) {
            return new WarRefUndeployAction.ContextAction(context);
        }

        private static final class ContextAction extends AbstractAction implements ProgressListener {

            private RequestProcessor.Task task;
            private final Project project;
            private final FileObject warrefFo;
            private final BaseDeploymentManager dm;
            private String contextPath;
            private String warLocation;

            public ContextAction(Lookup context) {

                DataObject wardo = context.lookup(DataObject.class);
                warrefFo = wardo.getPrimaryFile();
                project = FileOwnerQuery.getOwner(warrefFo);
                dm = BaseUtils.managerOf(project);
                putValue(NAME, "&Undeploy");
            }

            public @Override
            void actionPerformed(ActionEvent e) {

                Properties props = BaseUtils.loadProperties(warrefFo);
                if (props == null) {
                    return;
                }
                contextPath = props.getProperty(EmbConstants.CONTEXTPATH_PROP);
                if (contextPath == null) {
                    contextPath = "/" + warrefFo.getName();
                }
                if (!contextPath.startsWith("/")) {
                    contextPath = "/" + contextPath;
                }
                warLocation = props.getProperty(EmbConstants.WEB_APP_LOCATION_PROP);
                if (warLocation == null) {
                    return;
                }
                final BaseDeploymentManager dm = BaseUtils.managerOf(project);

                if (WarRefActions.startServer(dm, project, this) == null) {
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
                        String cp = contextPath;
                        dm.getSpecifics().execCommand(dm.getServerProject(), EmbUtils.createCommand("undeploywar", cp, warLocation));

                    }
                });

            }
        }//class

    }//class

}//class
