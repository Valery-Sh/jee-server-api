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
package org.netbeans.modules.jeeserver.base.embedded.server.project.nodes;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.netbeans.modules.jeeserver.base.embedded.server.project.ServerSuiteManager;
import static org.netbeans.modules.jeeserver.base.embedded.server.project.nodes.Bundle.ServerInstanciesRootNode_shortDescription;
import org.netbeans.modules.jeeserver.base.embedded.server.project.nodes.actions.ServerInstanciesActions;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteConstants;
import org.openide.actions.PropertiesAction;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle.Messages;
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
@Messages({
    "ServerInstanciesRootNode.shortDescription=Server Instances for this Server",
    "ServerInstanciesRootNode.availableWebApps=Available Web Applications"
})
public class ServerInstancesRootNode extends FilterNode implements ChildrenKeysModel {

    private static final Logger LOG = Logger.getLogger(ServerInstancesRootNode.class.getName());
    
    private final InstanceContent lookupContents;

    private RootChildrenKeys childKeys;
//    private ModulesChangeListener modulesChangeListener;

    /**
     * Creates a new instance of the class for a specified serverProject.
     *
     * @param serverProj a serverProject which is used to create an instance of
     * the class.
     * @throws DataObjectNotFoundException
     */
    public ServerInstancesRootNode(Project serverProj) throws DataObjectNotFoundException {
        this(DataObject.find(serverProj.getProjectDirectory().
                getFileObject(SuiteConstants.SERVER_INSTANCES_FOLDER)).getNodeDelegate(),
                new RootChildrenKeys(serverProj), new InstanceContent());
    }

    public ServerInstancesRootNode(Node node, RootChildrenKeys childKeys, InstanceContent instanceContent) throws DataObjectNotFoundException {
        super(node, childKeys, new AbstractLookup(instanceContent));
        this.childKeys = childKeys;
        this.lookupContents = instanceContent;
        lookupContents.add(new NodeModel(this));
    }

    public RootChildrenKeys getChildKeys() {
        return this.childKeys;
    }

    /**
     * Creates an instance of class {@link FileChangeHandler} and adds it as a
     * listener of the {@literal FileEvent } to the {@literal FileObject}
     * associated with a {@literal server-instance-config} folder.
     *
     * @param serverSuite
     */
    protected final void init(Project serverSuite) {
        serverSuite.getLookup().lookup(NodeModel.class).init(this);

        setShortDescription(ServerInstanciesRootNode_shortDescription());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        BaseUtils.out("ServerInstanceRootNode: propertyChange new = " + evt.getNewValue());
        if (childKeys != null && "server-running".equals(evt.getPropertyName()) )  {
            if (childKeys != null) {
                childKeys.propertyChange(evt);
            }
        }

    }

    /**
     * Returns the logical name of the node.
     *
     * @return the value "Server Instances"
     */
    @Override
    public String getDisplayName() {
        return "Server Instances";
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

        Action newAntProject = ServerInstanciesActions.NewAntProjectAction.getContextAwareInstance(getLookup());
        Action addExistingProject = ServerInstanciesActions.AddExistingProjectAction.getContextAwareInstance(getLookup());
        Action propAction = null;

        for (Action a : super.getActions(ctx)) {
            if (a instanceof PropertiesAction) {
                propAction = a;
                break;
            }
        }

        actions.add(newAntProject);
        actions.add(addExistingProject);
        actions.add(null);

//        Project server = ((RootChildrenKeys) getChildren()).getServerProj();
        if (propAction != null) {
            actions.add(propAction);
        }

        return actions.toArray(new Action[actions.size()]);

    }

    //Next, we add icons, for the default state, which is
    //closed, and the opened state; we will make them the same. 
    //
    //Icons in suiteProj logical views are
    //based on combinations--you can combine the node's own icon
    //with a distinguishing badge that is merged with it. Here we
    //first obtain the icon from a data folder, then we add our
    //badge to it by merging it via a NetBeans API utility method:
    @Override
    public Image getIcon(int type) {
        /*        DataFolder root = DataFolder.findFolder(FileUtil.getConfigRoot());
         Image original = root.getNodeDelegate().getIcon(type);
         return Icons.mergeImages(original,
         ImageUtilities.loadImage(IMAGE), 7, 7);
         */
        return ImageUtilities.loadImage(SuiteConstants.SERVER_INSTANCES_ICON);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return ImageUtilities.loadImage(SuiteConstants.SERVER_INSTANCES_ICON);
    }

    @Override
    public void modelChanged() {
        if ( childKeys != null ) {
            childKeys.addNotify();
        }
    }

/*    @Override
    public List<InstanceNode> getInstanceNodes() {
        if (childKeys == null) {
            return null;
        }
        List<InstanceNode> list = null;
        Node[] nodes = childKeys.getNodes();
        if (nodes == null) {
            return null;
        }
        list = new ArrayList<>();
        for (Node node : nodes) {
            if (node instanceof InstanceNode) {
                list.add((InstanceNode) node);
            }
        }
        return list;
    }
*/
    /**
     * The implementation of the Children.Key of the {@literal Server Libraries}
     * node.
     *
     * @param <T>
     */
    public static class RootChildrenKeys extends FilterNode.Children.Keys<String> {

        private final Project suiteProj;

        /**
         * Created a new instance of the class for the specified server
         * serverProject.
         *
         * @param suiteProj the serverProject which is used to create an
         * instance for.
         */
        public RootChildrenKeys(Project suiteProj) {
            this.suiteProj = suiteProj;

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
            return new Node[]{InstanceNodeFactory.getNode(key, suiteProj)};

            //return new Node[]{};
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

            List<String> uris = ServerSuiteManager.getServerInstanceIds(suiteProj);
            this.setKeys(uris);
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

        @Override
        protected void destroyNodes(Node[] destroyed) {
            for (Node node : destroyed) {
                BaseUtils.out("destroyNodes  node.name = node.getName=" + node.getName());
            }
        }

        public void propertyChange(PropertyChangeEvent evt) {
            Node[] nodes = this.getNodes();
            if (nodes == null || nodes.length == 0) {
                return;
            }
            int i = 0;
            for (Node node : nodes) {
                if (node instanceof InstanceNode) {
                    String key = ((InstanceNode) node).getKey();
                    Object o = evt.getSource();
                    if (o != null && o instanceof BaseDeploymentManager) {
                        String uri = ((BaseDeploymentManager) o).getUri();
                        if (uri.equals(key)) {
                            ((InstanceNode) node).propertyChange(evt);
                        }
                    }
                }
            }
        }

        /**
         * @return the serverProject
         */
        public Project getServerProj() {
            return suiteProj;
        }

    }//class

}//class