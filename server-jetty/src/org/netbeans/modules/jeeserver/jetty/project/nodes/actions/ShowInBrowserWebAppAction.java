/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jeeserver.jetty.project.nodes.actions;

import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.netbeans.modules.jeeserver.base.deployment.utils.Copier;
import static org.netbeans.modules.jeeserver.jetty.project.nodes.actions.AbstractHotDeployedContextAction.CONTEXTPATH;
import org.netbeans.modules.jeeserver.jetty.util.Utils;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;

/**
 *
 * @author Valery
 */
public class ShowInBrowserWebAppAction extends AbstractAction implements ContextAwareAction {

    private static final Logger LOG = Logger.getLogger(ShowInBrowserWebAppAction.class.getName());

    public ShowInBrowserWebAppAction() {
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
        return new ShowInBrowserContextAction(context, "showinbrowser");

    }

    public static class ShowInBrowserContextAction extends AbstractHotDeployedContextAction implements ProgressListener {

        private String urlStr;

        public ShowInBrowserContextAction(Lookup context, String command) {
            super(context, command);
        }

        @Override
        protected Properties getContextProperties(FileObject webFo) {
            Properties props = new Properties();

            props.setProperty(CONTEXTPATH, webFo.getNameExt());
            //props.setProperty(WAR, webFo.getPath());

            FileObject jettyxml = webFo;

            switch (webFo.getExt()) {
                case "xml": {
                    //props = Utils.getJettyXmlProperties(project, webFo);
                    props = Utils.getContextProperties(jettyxml);
                    break;
                }
                case "war": {
                    // we must extract jetty-web.xml if exists
                    String s = Copier.ZipUtil.getZipEntryAsString(FileUtil.toFile(webFo), "WEB-INF/jetty-web.xml");
                    props = Utils.getContextProperties(s);
                    if ( props == null ) {
                        props = new Properties();
                        props.setProperty(BaseConstants.CONTEXTPATH_PROP, webFo.getName());
                    }

                    break;
                }
                default: {
                    // we must extract jetty-web.xml if exists
                    if (webFo.isFolder()) {
                        props = Utils.getContextProperties(webFo.getFileObject("WEB-INF/jetty-web.xml"));
                    }
                    break;
                }

            }
            return props;

        }

        @Override
        protected String getMenuItemName() {
            return "Show in Browser";
        }

        @Override
        protected void runActionPerformed() {
            String contextPath = contextProps.getProperty(BaseConstants.CONTEXTPATH_PROP);

            if (!contextPath.startsWith("/")) {
                contextPath = "/" + contextPath;
            }

            //BaseUtils.out(" %%%%%%% contextPath= " + contextPath);

            String port = manager.getInstanceProperties().getProperty(BaseConstants.HTTP_PORT_PROP);
            String host = manager.getInstanceProperties().getProperty(BaseConstants.HOST_PROP);
            urlStr = "http://" + host + ":" + port + contextPath;

            if (manager.getSpecifics().pingServer(manager)) {
                for (int i = 0; i < 100; i++) {
                    String state = Utils.getState(manager, contextPath);
                    if (state != null) {
                        String[] a = state.split(" ");
                        state = a[0];
                    }
                    
                    if ("STARTED".equals(state)) {
                        break;
                    }
                    BaseUtil.sleep(100);
                }
            }

            if (AbstractHotDeployedContextAction.startServer(manager, project, this) == null) {
                show();
            }
        }

        void show() {
            try {
                HtmlBrowser.URLDisplayer.getDefault().showURL(new URL(urlStr));
            } catch (MalformedURLException ex) {
                LOG.log(Level.INFO, null, ex);
            }
        }

        @Override
        public void handleProgressEvent(ProgressEvent pe) {
            if (pe.getDeploymentStatus().isCompleted()) {
                show();
            }
        }

    }// class ShowInBrowserContextAction

}//class
