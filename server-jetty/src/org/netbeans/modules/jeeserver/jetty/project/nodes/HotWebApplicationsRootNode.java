/**
 * This file is part of Jetty Server support in NetBeans IDE.
 *
 * Jetty Server support in NetBeans IDE is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * Jetty Server support in NetBeans IDE is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should see the GNU General Public License here:
 * <http://www.gnu.org/licenses/>.
 */
package org.netbeans.modules.jeeserver.jetty.project.nodes;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import javax.swing.Action;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.netbeans.modules.jeeserver.jetty.project.JettyProjectLogicalView;
import static org.netbeans.modules.jeeserver.jetty.project.nodes.Bundle.HotWebApplicationsNode_shortDescription;
import static org.netbeans.modules.jeeserver.jetty.project.nodes.Bundle.HotWebApplicationsNode_webApps;
import org.netbeans.modules.jeeserver.jetty.project.nodes.actions.HotDeployedWebAppsNodeActionFactory;
import org.netbeans.modules.jeeserver.jetty.util.JettyConstants;
import org.netbeans.modules.jeeserver.jetty.util.Utils;
import org.openide.actions.PropertiesAction;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Represents the root node of the logical view of the project's folder named
 * {@literal webapps}. Every jetty server project may contain a child folder
 * named {@literal webapps}. Its logical name is {@literal Web Applications}.
 *
 * @author V. Shyshkin
 */
@NbBundle.Messages({
    "HotWebApplicationsNode.shortDescription=Hod deployed applications for this server.",
    "HotWebApplicationsNode.webApps=Hot Deployed Applications"    
})

public class HotWebApplicationsRootNode extends FilterNode {

    private FileChangeHandler fileChangeHandler;

    @StaticResource
    private static final String IMAGE = "org/netbeans/modules/jeeserver/jetty/resources/web-pages-badge.png";

    /**
     * Creates a new instance of the class for a specified project.
     *
     * @param serverProj a project which is used to create an instance of the
     * class.
     * @throws DataObjectNotFoundException
     */
    public HotWebApplicationsRootNode(Project serverProj) throws DataObjectNotFoundException {
        super(DataObject.find(serverProj.getProjectDirectory().
                getFileObject(JettyConstants.WEBAPPS_FOLDER)).getNodeDelegate(),
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
        fileChangeHandler = new FileChangeHandler(serverProj, this);
        serverProj.getProjectDirectory().getFileObject(JettyConstants.WEBAPPS_FOLDER)
                .addFileChangeListener(fileChangeHandler);
        serverProj.getLookup().lookup(JettyProjectLogicalView.class)
                .setWebApplicationsNode(this);
        
        this.setShortDescription(HotWebApplicationsNode_shortDescription());
    }
//!! 29.06
    public static Node getNode(Project project, Object key) {
        String name = key.toString();
        Node node = null;

        FileObject fo = project.getProjectDirectory().getFileObject(JettyConstants.WEBAPPS_FOLDER).getFileObject(name);
        try {
            if (fo.isFolder()) {
                node = new HotWebFolderChildNode(project, key);
            } else if ("war".equals(fo.getExt())) {
                node = new HotWarArchiveChildNode(project, key);
            } else if ("xml".equals(fo.getExt())) {
                node = new HotXmlChildNode(project, key);
            }
        } catch (DataObjectNotFoundException e) {
        }
        return node;
    }

    /**
     * Returns the logical name of the node.
     *
     * @return the value "Hot Deployed Application"
     */
    @Override
    public String getDisplayName() {
        return HotWebApplicationsNode_webApps();
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
        
        Action addWarAction = new HotDeployedWebAppsNodeActionFactory()
                .getAddWarAction(getLookup());
        Action addHtml5WarAction = new HotDeployedWebAppsNodeActionFactory()
                .getAddHtml5WarAction(getLookup());
        Action addHtml5Action = new HotDeployedWebAppsNodeActionFactory()
                .getAddHtml5Action(getLookup());
        Action addXmlAction = new HotDeployedWebAppsNodeActionFactory()
                .getAddXmlAction(getLookup());
        Action addWebFolderAction = new HotDeployedWebAppsNodeActionFactory()
                .getAddWebFolderAction(getLookup());
                

        for (Action a : super.getActions(ctx)) {
            if (a instanceof PropertiesAction) {
                propAction = a;
                break;
            }
        }
        actions.add(addWarAction);
        actions.add(addWebFolderAction);                
        actions.add(null);
        actions.add(addXmlAction);        
        actions.add(null);
        actions.add(addHtml5WarAction);                        
        actions.add(addHtml5Action);                        
        
        actions.add(null);

        if (propAction != null) {
            actions.add(propAction);
        }

        return actions.toArray(new Action[actions.size()]);
    }

    @Override
    public Image getIcon(int type) {
        DataFolder root = DataFolder.findFolder(FileUtil.getConfigRoot());
        Image original = root.getNodeDelegate().getIcon(type);
        return ImageUtilities.mergeImages(original,
                ImageUtilities.loadImage(IMAGE), 7, 7);
    }

    @Override
    public Image getOpenedIcon(int type) {
        DataFolder root = DataFolder.findFolder(FileUtil.getConfigRoot());
        Image original = root.getNodeDelegate().getIcon(type);
        return ImageUtilities.mergeImages(original,
                ImageUtilities.loadImage(IMAGE), 7, 7);
    }

    /**
     * The implementation of the Children.Key of the {@literal Web Applications}
     * node.
     *
     * @param <T>
     */
    public static class WebAppKeys<T> extends Children.Keys<T> {

        private final Project serverProj;

        /**
         * Created a new instance of the class for the specified server project.
         *
         * @param serverProj the project which is used to create an instance
         * for.
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
            SourceGroup sg;
            //return new Node[]{WebAppChildFactory.getNode(serverProj, key)};
            return new Node[]{HotWebApplicationsRootNode.getNode(serverProj, key)};
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
            FileObject rootFolder = serverProj.getProjectDirectory().getFileObject(JettyConstants.WEBAPPS_FOLDER);
            FileObject[] files = rootFolder.getChildren();
            List keyArray = new ArrayList<>(files.length);
            for (FileObject fo : files) {
                //keyArray.add(fo.getNameExt());
                keyArray.add(new WebappsChildKey(fo));
            }
            this.setKeys(keyArray);
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
         * @return the embedded server project
         */
        public Project getServerProj() {
            return serverProj;
        }
    }//class

    /**
     * A handler of the {@literal FileEvent } that is registered on the
     * {@literal FileObject} that is associated with a
     * {@literal server-instance-config} folder.
     */
    protected static class FileChangeHandler extends FileChangeAdapter {

        private final Project project;
        private final HotWebApplicationsRootNode node;

        public FileChangeHandler(Project project, HotWebApplicationsRootNode node) {
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

            if (null == project.getProjectDirectory().getFileObject(JettyConstants.WEBAPPS_FOLDER)) {
                InstanceProperties.removeInstance(Utils.getServerInstanceId(project));
            } else {
                ((HotWebApplicationsRootNode.WebAppKeys) node.getChildren()).addNotify();
            }
        }

        /**
         * Called when a file is changed. Does nothing.
         *
         * @param ev the event describing context where action has taken place
         */
        @Override
        public void fileChanged(FileEvent ev) {
            Node[] nodes = ((HotWebApplicationsRootNode.WebAppKeys) node.getChildren()).getNodes();
            if (nodes == null) {
                return;
            }
            FileObject keyFileObject = ev.getFile(); // it is a FileObject actually

            for (Node n : nodes) {
                if (!(n instanceof HotXmlChildNode)) {
                    continue;
                }

                HotXmlChildNode xmlNode = (HotXmlChildNode) n;
                WebappsChildKey key = (WebappsChildKey) xmlNode.getWebAppKey();
                Properties props = Utils.getContextProperties(keyFileObject);
                if (props != null) {
                    key.setContextPath(props.getProperty("contextPath"));
                    key.setWar(props.getProperty("war"));
                }
            }
        }

        /**
         * Called when a new file is created. This action can only be listened
         * in folders containing the created file up to the root. Invokes the
         * method {@literal addNotify } of the class {@link HotWebApplicationsRootNode}
         * to enforce the IDE to update Logical View.
         *
         * @param ev the event describing context where action has taken place
         */
        @Override
        public void fileDataCreated(FileEvent ev) {
            ((HotWebApplicationsRootNode.WebAppKeys) node.getChildren()).addNotify();
        }

        /**
         * Called when a new folder is created. This action can only be listened
         * to in folders containing the created folder up to the root. Invokes
         * the method {@literal addNotify } of the class
         * {@link HotWebApplicationsRootNode} to enforce the IDE to update Logical
         * View.
         *
         * @param ev the event describing context where action has taken place
         */
        @Override
        public void fileFolderCreated(FileEvent ev) {
            ((HotWebApplicationsRootNode.WebAppKeys) node.getChildren()).addNotify();
        }
    }//class

    public static class WebappsChildKey {

        private final FileObject keyFileObject;
        private String contextPath;
        private String war;

        public WebappsChildKey(FileObject keyFileObject) {
            this.keyFileObject = keyFileObject;
            if (!keyFileObject.isFolder() && "xml".equals(keyFileObject.getExt())) {
                Properties props = Utils.getContextProperties(keyFileObject);
                this.contextPath = props.getProperty("contextPath");
                this.war = props.getProperty("war");
            }
        }

        @Override
        public String toString() {
            return keyFileObject.getNameExt();
        }

        public String getContextPath() {
            return contextPath;
        }

        public void setContextPath(String contextPath) {
            this.contextPath = contextPath;
        }

        public String getWar() {
            return war;
        }

        public void setWar(String war) {
            this.war = war;
        }

    }
}//class
