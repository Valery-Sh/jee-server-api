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
package org.netbeans.modules.jeeserver.jetty.project.nodes;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static javax.swing.Action.NAME;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.common.CreateCapability;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.Listener;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.jeeserver.base.deployment.ServerInstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.base.deployment.config.ServerInstanceAvailableModules;
import org.netbeans.modules.jeeserver.base.deployment.config.WebModuleConfig;
import static org.netbeans.modules.jeeserver.base.deployment.progress.BaseRunProgressObject.LOG;
import org.netbeans.modules.jeeserver.base.deployment.specifics.StartServerPropertiesProvider;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.netbeans.modules.jeeserver.base.deployment.utils.Info;
import org.netbeans.modules.jeeserver.jetty.deploy.JettyServerPlatformImpl;
import org.netbeans.modules.jeeserver.jetty.project.nodes.libs.LibrariesAction;
import org.netbeans.modules.jeeserver.jetty.project.template.AbstractJettyInstanceIterator;
import org.netbeans.modules.jeeserver.jetty.project.template.JettyProperties;
import org.netbeans.modules.jeeserver.jetty.util.JettyConstants;
import org.netbeans.modules.jeeserver.jetty.util.StartIni;
import org.netbeans.modules.jeeserver.jetty.util.Utils;
import static org.netbeans.modules.jeeserver.jetty.util.Utils.propertiesOf;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.actions.PropertiesAction;
import org.openide.awt.DynamicMenuContent;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOColors;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 * Represents the root node of the logical view of the serverProject's folder
 * named {@literal webapps}. Every jetty server serverProject may contain a
 * child folder named {@literal webapps}. Its logical name is
 * {@literal Web Applications}.
 *
 * @author V. Shyshkin
 */
public class JettyBaseRootNode extends FilterNode {

    private static final Logger LOG = Logger.getLogger(JettyBaseRootNode.class.getName());

    private FileChangeHandler fileChangeHander;

    //private HttpIniFileChangeHandler httpIniChangeHander;

   // private StartIniFileChangeHandler startIniChangeHander;

    @StaticResource
    private static final String IMAGE = "org/netbeans/modules/jeeserver/jetty/resources/config-file.gif";

    /**
     * Creates a new instance of the class for a specified serverProject.
     *
     * @param serverProj a serverProject which is used to create an instance of
     * the class.
     * @throws DataObjectNotFoundException
     */
    public JettyBaseRootNode(Project serverProj) throws DataObjectNotFoundException {
        super(DataObject.find(serverProj.getProjectDirectory()
                .getFileObject(JettyConstants.JETTYBASE_FOLDER))
                .getNodeDelegate(),
                new WebAppKeys(serverProj));
    }

    /**
     * Creates an instance of class {@link FileChangeHandler} and adds it as a
     * listener of the {@literal FileEvent } to the {@literal FileObject}
     * associated with a {@literal server-instance-config} folder.
     *
     * @param serverProj
     */
    protected final void init(Project serverProj) {
        fileChangeHander = new JettyBaseRootNode.FileChangeHandler(serverProj, this);
        serverProj.getProjectDirectory().getFileObject(JettyConstants.JETTYBASE_FOLDER)
                .addFileChangeListener(fileChangeHander);
    }

    /**
     * Returns the logical name of the node.
     *
     * @return the value "Web Application"
     */
    @Override
    public String getDisplayName() {
        return "jetty.base";
    }

    /**
     * Returns an array of actions associated with the node.
     *
     * @param ctx
     * @return an array of the following actions:
     * <ul>
     * <li>
     * Add Existing Web Application
     * </li>
     * <li>
     * Add Archive .war File
     * </li>
     * <li>
     * New Web Application
     * </li>
     * <li>
     * Properties
     * </li>
     * </ul>
     */
    @Override
    public Action[] getActions(boolean ctx) {
        List<Action> actions = new ArrayList<>(2);

        Action propAction = null;
//        Action newAppAction;
//        Action addWebAppRefAction;
//        Action addWarAppRefAction;
//        Action addHtmAppRefAction;

        for (Action a : super.getActions(ctx)) {
            if (a instanceof PropertiesAction) {
                propAction = a;
                break;
            }
        }

        Project server = ((WebAppKeys) getChildren()).getServerProj();
        if (propAction != null) {
            actions.add(propAction);
        }
        return actions.toArray(new Action[actions.size()]);

    }

    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage(IMAGE);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return ImageUtilities.loadImage(IMAGE);
    }

    /**
     * The implementation of the Children.Key of the {@literal Web Applications}
     * node.
     *
     * @param <T>
     */
    public static class WebAppKeys<T> extends FilterNode.Children.Keys<T> {

        private Project serverProj;

        /**
         * Created a new instance of the class for the specified server
         * serverProject.
         *
         * @param serverProj the serverProject which is used to create an
         * instance for.
         */
        public WebAppKeys(Project serverProj) {
            this.serverProj = serverProj;
        }

        /**
         * Creates an array of nodes for a given key.
         *
         * @param key the value used to create nodes.
         * @return an array with a single element. So, there is one node for the
         * specified key
         */
        @Override
        protected Node[] createNodes(T key) {
            Node node = null;
            try {
                node = new JettyBaseRootChildNode(serverProj, key);
            } catch (DataObjectNotFoundException ex) {
                LOG.log(Level.INFO, ex.getMessage());
            }
            if (node == null) {
                return new Node[]{};
            }
            return new Node[]{node};

        }

        /**
         * Called when children of the {@code Web Applications} are first asked
         * for nodes. For each child node of the folder named
         * {@literal "server-instance-config"} gets the name of the child file
         * with extension and adds to a List of Strings. Then invokes the method {@literal setKeys(List)
         * }.
         */
        @Override
        protected void addNotify() {
            FileObject rootFolder = serverProj.getProjectDirectory()
                    .getFileObject(JettyConstants.JETTYBASE_FOLDER);
            if (rootFolder == null) {
                return;
            }
            FileObject[] files = rootFolder.getChildren();
            List keyArray = new ArrayList<>(files.length);
            for (FileObject fo : files) {
                keyArray.add(new JettyBaseRootNode.WebappsChildKey(fo));
            }
            WebAppKeys.this.setKeys(keyArray);
        }

        /**
         * Called when all the children Nodes are freed from memory. The
         * implementation just invokes 
         * {@literal setKey(Collections.EMPTY_LIST) }.
         */
        @Override
        protected void removeNotify() {
            this.setKeys(Collections.EMPTY_LIST);
        }

        /**
         * @return the embedded server serverProject
         */
        public Project getServerProj() {
            return serverProj;
        }
    }//class

    public static class WebappsChildKey {

        private final FileObject keyFileObject;

        public WebappsChildKey(FileObject keyFileObject) {
            this.keyFileObject = keyFileObject;
        }

        @Override
        public String toString() {
            return keyFileObject.getNameExt();
        }

        public FileObject getKeyFileObject() {
            return keyFileObject;
        }

    }

    /**
     * A handler of the {@literal FileEvent } that is registered on the
     * {@literal FileObject} that is associated with a
     * {@literal server-instance-config} folder.
     */
    protected static class HttpIniFileChangeHandler extends FileChangeAdapter {

        private final Project project;
        private final JettyBaseRootNode node;

        public HttpIniFileChangeHandler(Project project, JettyBaseRootNode node) {
            this.project = project;
            this.node = node;
        }

        /**
         * Called when a file is changed. Does nothing.
         *
         * @param ev the event describing context where action has taken place
         */
        @Override
        public void fileChanged(FileEvent ev) {
            JettyProperties jvs = JettyProperties.getInstance(project);
            String portProp = jvs.getHttpPortPropertyName();

            String port = BaseUtils.getServerProperties(project).getHttpPort();
            Properties props = BaseUtils.loadProperties(ev.getFile());
            if (port.equals(props.getProperty(portProp))) {
                return;
            }
            String uri = BaseUtils.getServerProperties(project).getUri();
            InstanceProperties ip = InstanceProperties.getInstanceProperties(uri);
            ip.setProperty(BaseConstants.HTTP_PORT_PROP, props.getProperty(portProp));
        }
    }

    /**
     * A handler of the {@literal FileEvent } that is registered on the
     * {@literal FileObject} that is associated with a
     * {@literal server-instance-config} folder.
     */
    protected static class FileChangeHandler extends FileChangeAdapter {

        private final Project project;
        private final JettyBaseRootNode node;

        public FileChangeHandler(Project project, JettyBaseRootNode node) {
            this.project = project;
            this.node = node;
        }

        /**
         * Called when a child file or folder of the {@literal web-app} folder
         * is deleted. When the folder {@literal server-instance-config} is
         * deleted the registered ServerInstance associated with the server
         * project is removed.
         *
         * @param ev the event describing context where action has taken place
         */
        @Override
        public void fileDeleted(FileEvent ev) {
            if (node != null && node.getChildren() != null) {
                ((JettyBaseRootNode.WebAppKeys) node.getChildren()).addNotify();
            }
        }

        /**
         * Called when a file is changed. Does nothing.
         *
         * @param ev the event describing context where action has taken place
         */
        @Override
        public void fileChanged(FileEvent ev) {
        }

        /**
         * Called when a new file is created. This action can only be listened
         * in folders containing the created file up to the root. Invokes the
         * method {@literal addNotify } of the class {@link WebApplicationsNode}
         * to enforce the IDE to update Logical View.
         *
         * @param ev the event describing context where action has taken place
         */
        @Override
        public void fileDataCreated(FileEvent ev) {
            ((JettyBaseRootNode.WebAppKeys) node.getChildren()).addNotify();
        }

        /**
         * Called when a new folder is created. This action can only be listened
         * to in folders containing the created folder up to the root. Invokes
         * the method {@literal addNotify } of the class
         * {@link WebApplicationsNode} to enforce the IDE to update Logical
         * View.
         *
         * @param ev the event describing context where action has taken place
         */
        @Override
        public void fileFolderCreated(FileEvent ev) {
            ((JettyBaseRootNode.WebAppKeys) node.getChildren()).addNotify();
        }
    }//class

}//class
