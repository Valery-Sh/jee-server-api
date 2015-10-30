/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jeeserver.base.embedded.project.nodes;

import java.awt.Image;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.ServerInstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import static org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants.*;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.netbeans.modules.jeeserver.base.embedded.project.SuiteManager;
import org.netbeans.modules.jeeserver.base.embedded.project.nodes.actions.ServerActions;
import org.netbeans.modules.jeeserver.base.embedded.project.nodes.actions.ServerActions.BuildProjectActions;
import org.netbeans.modules.jeeserver.base.embedded.project.nodes.actions.ServerActions.InstancePropertiesAction;
import org.netbeans.modules.jeeserver.base.embedded.project.nodes.actions.ServerActions.RemoveInstanceAction;
import org.netbeans.modules.jeeserver.base.embedded.project.nodes.actions.ServerActions.StartStopAction;
import org.netbeans.modules.jeeserver.base.embedded.webapp.DistributedWebAppManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Valery
 */
public class InstanceNode extends FilterNode implements ChildrenNotifier {

    private static final Logger LOG = Logger.getLogger(InstanceNode.class.getName());

    private final InstanceContent lookupContents;
    private InstanceNodeChildrenKeys childKeys;
    private final String key;
    private String displayName;

    public InstanceNode(Node original, String key) {
        this(original, key, new InstanceContent(), new InstanceNodeChildrenKeys(key));
    }

    public InstanceNode(Node original, String key, InstanceContent content, InstanceNodeChildrenKeys childKeys) {
        super(original, childKeys, new AbstractLookup(content));
        this.key = key;
        lookupContents = content;
        lookupContents.add(original.getLookup().lookup(FileObject.class));
        this.childKeys = childKeys;
        init();
    }

    private void init() {

        InstanceProperties props = InstanceProperties.getInstanceProperties(key);
        ServerInstanceProperties sip = new ServerInstanceProperties();
        sip.setServerId(props.getProperty(BaseConstants.SERVER_ID_PROP));
        sip.setUri(props.getProperty(BaseConstants.URL_PROP));

        lookupContents.add(sip);
        lookupContents.add(this);
        lookupContents.add(childKeys);
        lookupContents.add(this);
        this.displayName = props.getProperty(DISPLAY_NAME_PROP);
    }

    public String getKey() {
        return key;
    }

    /**
     * Returns the logical name of the node.
     *
     * @return the value "Server Instances"
     */
    @Override
    public String getDisplayName() {
        return "Instance: " + displayName;
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
        //ServerInstanceProperties sip = getLookup().lookup(ServerInstanceProperties.class);

        Action[] actions
                = new Action[]{
//                    BuildProjectActions.getContextAwareInstance("rebuild-all", getLookup()),
//                    null,
                    //new StartServerAction().createContextAwareInstance(getLookup()),
                    StartStopAction.getAction("start", getLookup()),
                    StartStopAction.getAction("stop", getLookup()),
                    null,
                    RemoveInstanceAction.getContextAwareInstance(getLookup()),
                    null,
                    BuildProjectActions.getContextAwareInstance("build", getLookup()),
                    BuildProjectActions.getContextAwareInstance("rebuild", getLookup()),
                    BuildProjectActions.getContextAwareInstance("clean", getLookup()),
                    null,
                    //ServerActions.DefineMainClassAction.getContextAwareInstance(getLookup()),
                    ServerActions.DownLoadJarsAction.getContextAwareInstance(getLookup()),
                    ServerActions.AddDependenciesAction.getContextAwareInstance(getLookup()),
                    null,
                    InstancePropertiesAction
                    .getContextAwareInstance(getLookup()),
                    null,
                    BuildProjectActions.getContextAwareInstance("developer", getLookup()),
                    null,};
        List<Action> alist = Arrays.asList(actions);
        List<Action> list = new ArrayList<>(alist);
        if (list.get(0) == null) {
            // Ant-based project
            list.remove(0);
            list.remove(0);
        }
        Action[] result = list.toArray(new Action[list.size()]);

        return result;
    }

    //Next, we add icons, for the default state, which is
//closed, and the opened state; we will make them the same. 
//
//Icons in nodeModel logical views are
//based on combinations--you can combine the node's own icon
//with a distinguishing badge that is merged with it. Here we
//first obtain the icon from a data folder, then we add our
//badge to it by merging it via a NetBeans API utility method:
    @StaticResource
    private static final String RUNNING_IMAGE = "org/netbeans/modules/jeeserver/base/embedded/resources/running.png";

    @Override
    public Image getIcon(int type) {

        ServerInstanceProperties sp = getLookup().lookup(ServerInstanceProperties.class
        );
        Image image = sp.getManager()
                .getSpecifics()
                .getServerImage(null);

        if (isServerRunning()) {
            image = ImageUtilities.mergeImages(image, ImageUtilities.loadImage(RUNNING_IMAGE), 16, 8);
        }
        return image;
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    boolean serverRunning;

    protected boolean isServerRunning() {
        return serverRunning;
    }

    @Override
    public void iconChange(String uri, boolean newValue) {
        serverRunning = newValue;
        fireIconChange();
    }

    @Override
    public void displayNameChange(String uri, String newName) {
        String newValue = newName;
        String oldValue = displayName;
        displayName = newValue;
        fireDisplayNameChange(oldValue, newValue);
    }

    @Override
    public void childrenChanged() {
        childKeys.addNotify();

    }

    @Override
    public void childrenChanged(Object source, Object... params) {
        if (childKeys == null) {
            return;
        }

        if (source instanceof DistributedWebAppManager) {
            BaseUtil.out("InstanceNode childrenChanged");
            DistributedWebAppRootNode distNode = childKeys.getDistributedWebAppRootNode();
            if (distNode != null) {
                distNode.childrenChanged(source, params);
            }
        }
    }

    /**
     * The implementation of the Children.Key of the {@literal Server Libraries}
     * node.
     *
     */
    public static class InstanceNodeChildrenKeys extends FilterNode.Children.Keys<String> {
        //private InstanceNode instNode;

        private final String uri;

        /**
         * Created a new instance of the class for the specified server
         * instance.
         *
         * @param uri
         */
        public InstanceNodeChildrenKeys(String uri) {
            this.uri = uri;

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
            InstanceProperties ip = InstanceProperties.getInstanceProperties(key);
            Project serverSuite = SuiteManager.getServerSuiteProject(uri);
            String projDir = BaseUtil.getServerLocation(ip);
            Project instProj = FileOwnerQuery.getOwner(FileUtil.toFileObject(new File(projDir)));
            Node instProjView = InstanceChildNode.InstanceProjectLogicalView.create(instProj);
            Node distNode = null;
            try {
                distNode = new DistributedWebAppRootNode(serverSuite, instProj);
            } catch (DataObjectNotFoundException ex) {
                LOG.log(Level.INFO, ex.getMessage());

            }
//            return new Node[]{distNode};
            return new Node[]{distNode, instProjView};

            //return new Node[]{};
        }

        public DistributedWebAppRootNode getDistributedWebAppRootNode() {
            Node[] nodes = getNodes();
            if (nodes != null && nodes.length > 0) {
                for (Node node : nodes) {
                    if (node instanceof DistributedWebAppRootNode) {
                        return (DistributedWebAppRootNode) node;
                    }
                }
            }
            return null;
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
            Node n = getNode();
            List keyArray = new ArrayList<>();

            keyArray.add(uri);
            this.setKeys(keyArray);
        }

        /**
         * Called when all the children Nodes are freed from memory. The
         * implementation just invokes {@literal setKey(Collections.EMPTY_LIST)
         * }.
         */
        @Override
        protected void removeNotify() {
            this.setKeys(Collections.EMPTY_LIST);
        }

        @Override
        protected void destroyNodes(Node[] destroyed) {
        }

    }//class

}
