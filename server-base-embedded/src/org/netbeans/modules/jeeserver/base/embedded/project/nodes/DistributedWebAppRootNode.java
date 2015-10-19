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
package org.netbeans.modules.jeeserver.base.embedded.project.nodes;

import java.awt.Image;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.ServerInstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.netbeans.modules.jeeserver.base.embedded.project.SuiteManager;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteConstants;
import org.netbeans.modules.jeeserver.base.embedded.webapp.DistributedWebAppManager;
import org.netbeans.modules.jeeserver.base.embedded.webapp.actions.AddDistWebAppAction;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.actions.PropertiesAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * Represents the root node of the logical view of the serverProject's folder
 * named {@literal webapps}. Every jetty server serverProject may contain a
 * child folder named {@literal webapps}. Its logical name is
 * {@literal Web Applications}.
 *
 * @author V. Shyshkin
 */
/*@Messages({
 "ServerInstanciesRootNode.shortDescription=Server Instances for this Server",
 "ServerInstanciesRootNode.availableWebApps=Available Web Applications"
 })
 */
public class DistributedWebAppRootNode extends FilterNode implements ChildrenNotifier {

    private static final Logger LOG = Logger.getLogger(DistributedWebAppRootNode.class.getName());

    private final InstanceContent lookupContents;

    private DistRootChildrenKeys childKeys;
//    private ModulesChangeListener modulesChangeListener;

    /**
     * Creates a new instance of the class for a specified serverProject.
     *
     * @param serverProj a serverProject which is used to create an instance of
     * the class.
     * @throws DataObjectNotFoundException
     */
    public DistributedWebAppRootNode(Project suiteProj, Project serverProj) throws DataObjectNotFoundException {
        this(DataObject.find(serverProj.getProjectDirectory()).getNodeDelegate(),
                new DistRootChildrenKeys(serverProj), new InstanceContent());

    }

    public DistributedWebAppRootNode(Node node, DistRootChildrenKeys childKeys, InstanceContent instanceContent) throws DataObjectNotFoundException {
        super(node, childKeys, new AbstractLookup(instanceContent));
        this.childKeys = childKeys;
        this.lookupContents = instanceContent;

        FileObject instanciesDir = node.getLookup().lookup(FileObject.class);
        lookupContents.add(instanciesDir);
        lookupContents.add(childKeys.getServerProj());

        lookupContents.add(childKeys);
        init();
    }

    private void init() {
BaseUtil.out("DistributedWebAppRootNode children.class" + this.getChildren().getClass().getName());
        String uri = SuiteManager.getManager(childKeys.getServerProj()).getUri();
        InstanceProperties props = InstanceProperties.getInstanceProperties(uri);
        ServerInstanceProperties sip = new ServerInstanceProperties();
        sip.setServerId(props.getProperty(BaseConstants.SERVER_ID_PROP));
        sip.setUri(props.getProperty(BaseConstants.URL_PROP));

        lookupContents.add(sip);
        lookupContents.add(this);
    }

    public DistRootChildrenKeys getChildKeys() {
        return this.childKeys;
    }

    public Project getServerProject() {
        return childKeys.getServerProj();
    }

    /**
     * Returns the logical name of the node.
     *
     * @return the value "Server Instances"
     */
    @Override
    public String getDisplayName() {
        return "WebApplications to Distribute";
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
        Action addDistWebAppAction = AddDistWebAppAction.getAddDistWebAppAction(getLookup());
        //Action newAntProjectAction = ServerActions.NewAntProjectAction.getContextAwareInstance(getLookup());
        //Action newMavenProjectAction = ServerActions.NewMavenProjectAction.getContextAwareInstance(getLookup());
        //Action addExistingProject = ServerActions.AddExistingProjectAction.getContextAwareInstance(getLookup());
        Action propAction = null;

        for (Action a : super.getActions(ctx)) {
            if (a instanceof PropertiesAction) {
                propAction = a;
                break;
            }
        }
        actions.add(addDistWebAppAction);
        /*        actions.add(newAntProjectAction);
         actions.add(newMavenProjectAction);
         actions.add(null);
         actions.add(addExistingProject);
         actions.add(null);
         */
//        Project server = ((DistRootChildrenKeys) getChildren()).getServerProj();
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
                ImageUtilities.loadImage(SuiteConstants.DIST_WEB_APPS_BADGE_ICON), 7, 7);
    }

    @Override
    public Image getOpenedIcon(int type) {
        DataFolder root = DataFolder.findFolder(FileUtil.getConfigRoot());
        Image original = root.getNodeDelegate().getIcon(type);
        return ImageUtilities.mergeImages(original,
                ImageUtilities.loadImage(SuiteConstants.DIST_WEB_APPS_BADGE_ICON), 7, 7);
    }

    @Override
    public void childrenChanged() {
        if (childKeys != null) {
            childKeys.addNotify();
        }
    }

    @Override
    public void childrenChanged(Object source, Object... params) {
        if (childKeys == null) {
            return;
        }

        if (source instanceof DistributedWebAppManager) {
            childKeys.childrenChanged(source, params);
        }
    }

    @Override
    public void iconChange(String uri, boolean newValue) {
    }

    @Override
    public void displayNameChange(String uri, String newValue) {
    }

    /**
     * The implementation of the Children.Key of the {@literal Server Libraries}
     * node.
     *
     */
    public static class DistRootChildrenKeys extends FilterNode.Children.Keys<String> {

        private final Project instanceProj;

        /**
         * Created a new instance of the class for the specified server
         * serverProject.
         *
         * @param instanceProj the serverProject which is used to create an
         * instance for.
         */
        public DistRootChildrenKeys(Project instanceProj) {
            
            this.instanceProj = instanceProj;

        }

        /**
         * Creates an array of nodes for a given key.
         *
         * @param key the value used to create nodes.
         * @return an array with a single element. So, there is one node for the
         * specified key
         */
        @Override
        protected Node[] createNodes(String key) {
            Node node = null;
            FileObject fo = FileUtil.toFileObject(new File(key));
            Project webProj = FileOwnerQuery.getOwner(fo);
            
            try {
                LogicalViewProvider lvp = webProj.getLookup().lookup(LogicalViewProvider.class);
                node = lvp.createLogicalView();
            } catch (Exception ex) {
                LOG.log(Level.INFO, ex.getMessage());
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
        public void addNotify() {
            
            DistributedWebAppManager distManager = DistributedWebAppManager.getInstance(instanceProj);
            List<FileObject> list = distManager.getWebAppFileObjects();
            List<String> keys = new ArrayList<>();
            list.forEach(fo -> {
                keys.add(fo.getPath());
            });

            this.setKeys(keys);
        }

        public void childrenChanged(Object source, Object... params) {
            if (source instanceof DistributedWebAppManager) {
                addNotify();
            }
        }

        /**
         * Called when all the children Nodes are freed from memory. The
         * implementation just invokes 
         * {@literal setKey(Collections.EMPTY_LIST) }.
         */
        @Override
        public void removeNotify() {
            this.setKeys(Collections.EMPTY_LIST);
        }

/*        @Override
        protected void destroyNodes(Node[] destroyed) {
        }
*/
        public void iconChange(String uri, boolean newValue) {
            InstanceNode node = findInstanceNode(uri);
            if (node == null) {
                return;
            }
            node.iconChange(uri, newValue);
        }

        public void displayNameChange(String uri, String newValue) {
            InstanceNode node = findInstanceNode(uri);
            if (node == null) {
                return;
            }
            node.displayNameChange(uri, newValue);

        }

        protected InstanceNode findInstanceNode(String uri) {
            Node[] nodes = this.getNodes();

            if (nodes == null || nodes.length == 0) {
                return null;
            }

            int i = 0;
            InstanceNode result = null;
            for (Node node : nodes) {
                if (node instanceof InstanceNode) {

                    String key = ((InstanceNode) node).getKey();

                    if (uri.equals(key)) {
                        result = (InstanceNode) node;
                        break;
                    }
                }
            }
            return result;

        }

        public Project getServerProj() {
            return instanceProj;
        }

    }//class

}//class
