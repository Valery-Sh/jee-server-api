/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jeeserver.base.embedded.server.project.nodes;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import javax.swing.Action;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.ServerInstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.actions.StartServerAction;
import org.netbeans.modules.jeeserver.base.deployment.actions.StopServerAction;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.netbeans.modules.jeeserver.base.embedded.server.project.nodes.actions.ServerInstanciesActions.RemoveInstanceAction;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteConstants;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Valery
 */
public class InstanceNode extends FilterNode implements PropertyChangeListener{

    private final InstanceContent lookupContents;

    private final String key;
    private final Project serverSuite;
    private String displayName;
    //private Properties instanceProps;

    public InstanceNode(Node original, String key, Project serverSuite) {
        this(original, key, serverSuite, new InstanceContent());

//        this.key = key;
//        this.serverSuite = serverSuite;
//        instanceProps = BaseUtils.loadProperties(key.getFileObject("instance.properties"));
    }

    public InstanceNode(Node original, String key, Project serverSuite, InstanceContent content) {
        super(original, new InstanceNodeChildrenKeys(key), new AbstractLookup(content));
        this.key = key;
        this.serverSuite = serverSuite;
        //instanceProps = BaseUtils.loadProperties(key.getFileObject("instance.properties"));
        lookupContents = content;
        init();
    }
    private void init() {
        Lookup lk = BaseUtils.managerOf(key).getServerContext();
        
        ServerInstanceProperties sip = lk.lookup(ServerInstanceProperties.class);
        
        sip.addPropertyChangeListener(this);
        
        lookupContents.add(sip);
        lookupContents.add(this);
        
        InstanceProperties props = InstanceProperties.getInstanceProperties(key);
        this.displayName = props.getProperty(SuiteConstants.SERVER_INSTANCE_NAME_PROP);
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

        Action[] actions = 
            new Action[]{
                new StartServerAction().createContextAwareInstance(getLookup()),
                new StopServerAction().createContextAwareInstance(getLookup()),
                null, //new PropertiesAction().createContextAwareInstance(project.getLookup())
                RemoveInstanceAction
                        .getContextAwareInstance(getLookup())
        };
        
        return actions;
        /*        List<Action> actions = new ArrayList<>(2);

         Action newAntProject = ServerInstanciesActions.NewAntProjectAction.getContextAwareInstance(getLookup());

         //DataObject wardo = context.lookup(DataObject.class);        
         Action propAction = null;

         for (Action a : super.getActions(ctx)) {
         if (a instanceof PropertiesAction) {
         propAction = a;
         break;
         }
         }

         actions.add(newAntProject);

         Project server = ((ServerInstancesRootNode.InstanceNodeChildrenKeys) getChildren()).getServerProj();
         if (propAction != null) {
         actions.add(propAction);
         }

         return actions.toArray(new Action[actions.size()]);
         */
    }

    //Next, we add icons, for the default state, which is
    //closed, and the opened state; we will make them the same. 
    //
    //Icons in serverSuite logical views are
    //based on combinations--you can combine the node's own icon
    //with a distinguishing badge that is merged with it. Here we
    //first obtain the icon from a data folder, then we add our
    //badge to it by merging it via a NetBeans API utility method:
    @StaticResource
    private static final String RUNNING_IMAGE = "org/netbeans/modules/jeeserver/base/embedded/resources/running.png";    
    

    @Override
    public Image getIcon(int type) {
        
        ServerInstanceProperties sp = getLookup().lookup(ServerInstanceProperties.class);        
        Image image = sp
                .getManager()
                .getSpecifics()
                .getProjectImage(null);
    
        if (sp.isServerRunning()) {
            image = ImageUtilities.mergeImages(image, ImageUtilities.loadImage(RUNNING_IMAGE), 16, 8);
        }
        return image;
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ( "serverRunning".equals(evt.getPropertyName())) {
            fireIconChange();
        }
    }

    /**
     * The implementation of the Children.Key of the {@literal Server Libraries}
     * node.
     *
     * @param <T>
     */
    public static class InstanceNodeChildrenKeys extends FilterNode.Children.Keys<String> {
        //private InstanceNode instNode;

        private final String uri;

        /**
         * Created a new instance of the class for the specified server
         * instance.
         *
         * @param instNode
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

            String projDir = ip.getProperty(BaseConstants.SERVER_LOCATION_PROP);
            Project instProj = FileOwnerQuery.getOwner(FileUtil.toFileObject(new File(projDir)));
            Node instProjView = InstanceChildNode.InstanceProjectLogicalView.create(instProj);

            return new Node[]{instProjView};

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
        protected void addNotify() {
            Node n = getNode();
            List keyArray = new ArrayList<>();

            keyArray.add(uri);
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

        @Override
        protected void destroyNodes(Node[] destroyed) {
            for (Node node : destroyed) {
                BaseUtils.out("destroyNodes  node.name = node.getName=" + node.getName());
            }
        }
    }//class

}
