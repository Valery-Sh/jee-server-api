package org.netbeans.modules.jeeserver.base.embedded.project;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.embedded.actions.AddHtmRefAction;
import org.netbeans.modules.jeeserver.base.embedded.actions.AddWarRefAction;
import org.netbeans.modules.jeeserver.base.embedded.utils.EmbConstants;
import org.netbeans.modules.jeeserver.base.embedded.actions.AddWebRefAction;
import org.netbeans.modules.jeeserver.base.embedded.actions.NewWebAppAction;
import org.netbeans.modules.jeeserver.base.embedded.project.nodes.WebAppChildFactory;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
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

/**
 * Represents the root node of the logical view of the project's
 * folder named {@literal server-instance-config}.
 * Each embedded server project must contain a child folder named 
 * {@literal server-instance-config}. Its logical name is {@literal Web Applications}.
 * @author V. Shyshkin
 */
public class WebApplicationsNode extends FilterNode {

    private FileChangeHandler fileChangeHandler;
    
    @StaticResource
    private static final String IMAGE = "org/netbeans/modules/jeeserver/base/embedded/resources/webPagesBadge.gif";
    /**
     * Creates a new instance of the class for a specified project.
     * 
     * @param serverProj a project which is used to create an instance of the class.
     * @throws DataObjectNotFoundException 
     */
    public WebApplicationsNode(Project serverProj) throws DataObjectNotFoundException {
        super(DataObject.find(serverProj.getProjectDirectory().
                getFileObject(EmbConstants.REG_WEB_APPS_FOLDER)).getNodeDelegate(),
                new WebAppKeys(serverProj));
    }
    /**
     * Creates an instance of class {@link FileChangeHandler} and adds it
     * as a listener of the {@literal FileEvent } to the {@literal FileObject}
     * associated with a {@literal server-instance-config} folder.
     * 
     * @param serverProj 
     */
    protected final void init(Project serverProj) {
        fileChangeHandler = new FileChangeHandler(serverProj, this);
        serverProj.getProjectDirectory().getFileObject(EmbConstants.REG_WEB_APPS_FOLDER)
                .addFileChangeListener(fileChangeHandler);

    }
    /**
     * Create an array of child nodes associated with keys.
     * @param <T>
     * @param serverProj
     * @return 
     */
/*    public static <T> Children.Keys<T> childs(Project serverProj) {
        Children.Keys<T> keys = new WebAppKeys(serverProj);
        //Children.Keys<String> 
        return keys;
    }
*/
    /**
     * Returns the logical name of the node.
     * @return the value "Web Application"
     */
    @Override
    public String getDisplayName() {
        return "Web Applications";
    }
    /**
     * Returns an array of actions associated with the node.
     * @param ctx 
     * @return an array of the following actions:
     * <ul>
     *   <li>
     *      Add Existing Web Application 
     *   </li>
     *   <li>
     *      Add Archive .war File
     *   </li>
     *   <li>
     *      New Web Application 
     *   </li>
     *   <li>
     *      Properties
     *   </li>
     * </ul>
     */
    @Override
    public Action[] getActions(boolean ctx) {
        List<Action> actions = new ArrayList<>(2);

        Action propAction = null;
        Action newAppAction;
        Action addWebAppRefAction;
        Action addWarAppRefAction;
        Action addHtmAppRefAction;

        for (Action a : super.getActions(ctx)) {
            if (a instanceof PropertiesAction) {
                propAction = a;
                break;
            }
        }


        Project server = ((WebAppKeys) getChildren()).getServerProj();
        newAppAction = NewWebAppAction.getNewWebAppAction(server.getLookup());
        addWebAppRefAction = AddWebRefAction.getAddWebRefAction(server.getLookup());

        if (addWebAppRefAction != null) {
            actions.add(addWebAppRefAction);
        }

        addWarAppRefAction = AddWarRefAction.getAddWarRefAction(server.getLookup());

        if (addWarAppRefAction != null) {
            actions.add(addWarAppRefAction);
        }
        
        addHtmAppRefAction = AddHtmRefAction.getAddHtmRefAction(server.getLookup());
        if (addHtmAppRefAction != null) {
            actions.add(addHtmAppRefAction);
        }
        
        if (newAppAction != null) {
            actions.add(newAppAction);
            if (propAction != null) {
                actions.add(null); // delemeter
            }
        }
        if (propAction != null) {
            actions.add(propAction);
        }

        return actions.toArray(new Action[actions.size()]);

    }
    //Next, we add icons, for the default state, which is
    //closed, and the opened state; we will make them the same. 
    //
    //Icons in serverProj logical views are
    //based on combinations--you can combine the node's own icon
    //with a distinguishing badge that is merged with it. Here we
    //first obtain the icon from a data folder, then we add our
    //badge to it by merging it via a NetBeans API utility method:
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
     * The implementation of the Children.Key of the {@literal Web Applications} node.
     * @param <T> 
     */
    public static class WebAppKeys<T> extends Children.Keys<T> {

        private Project serverProj;
        /**
         * Created a new instance of the class for the specified 
         * server project.
         * 
         * @param serverProj the project which is used to create an instance for.
         */
        public WebAppKeys(Project serverProj) {
            this.serverProj = serverProj;
        }
        /**
         * Creates an array of nodes for a given key.
         * 
         * @param key the value used to create nodes.
         * @return an array with a single element. So, there is one node
         *  for the specified key
         */
        @Override
        protected Node[] createNodes(T key) {
            return new Node[]{WebAppChildFactory.getNode(serverProj, key)};
        }
        /**
         * Called when children of the {@Web Applications} are first asked for nodes. 
         * For each child node of the folder named {@literal "server-instance-config"} gets the name of the
         * child file with extension and adds to a List of Strings. 
         * Then invokes the method {@literal setKeys(List) }.
         */
        @Override
        protected void addNotify() {
            FileObject rootFolder = serverProj.getProjectDirectory().getFileObject(EmbConstants.REG_WEB_APPS_FOLDER);
            FileObject[] files = rootFolder.getChildren();
            List keyArray = new ArrayList<>(files.length);
            for (FileObject fo : files) {
                keyArray.add(fo.getNameExt());
            }
            this.setKeys(keyArray);
        }
        /**
         * Called when all the children Nodes are freed from memory. 
         * The implementation just invokes 
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
     * A handler of the {@literal FileEvent }  that is registered on the 
     * {@literal FileObject} that is associated with a  {@literal server-instance-config} folder.
     */
    protected static class FileChangeHandler extends FileChangeAdapter {

        private Project project;
        private WebApplicationsNode node;

        public FileChangeHandler(Project project, WebApplicationsNode node) {
            this.project = project;
            this.node = node;
        }
        /**
         * Called when a child file or folder of the {@literal web-app} folder is deleted.
         * When the folder {@literal server-instance-config} is deleted the registered 
         * ServerInstance associated with the server project is removed.
         * @param ev the event describing context where action has taken place
         */
        @Override
        public void fileDeleted(FileEvent ev) {
            
            if (null == project.getProjectDirectory().getFileObject(EmbConstants.REG_WEB_APPS_FOLDER)) {
                InstanceProperties.removeInstance(BaseUtils.getServerInstanceId(project));        
            } else {
                ((WebApplicationsNode.WebAppKeys) node.getChildren()).addNotify();
            }
        }
        /**
         * Called when a file is changed.
         * Does nothing.
         * 
         * @param ev the event describing context where action has taken place
         */
        @Override
        public void fileChanged(FileEvent ev) {
            System.out.println("Contents of " + ev.getFile() + " changed.");
        }
        
        /**
         * Called when a new file is created. 
         * This action can only be listened in folders containing 
         * the created file up to the root.     
         * Invokes the method {@literal addNotify } of the class 
         * {@link WebApplicationsNode} to enforce the IDE to update Logical View.
         * 
         * @param ev the event describing context where action has taken place
         */
        @Override
        public void fileDataCreated(FileEvent ev) {
            
            System.out.println("File " + ev.getFile() + " created.");
            ((WebApplicationsNode.WebAppKeys) node.getChildren()).addNotify();
        }
        /**
         * Called when a new folder is created. This action can only be listened to in folders containing 
         * the created folder up to the root.
         * Invokes the method {@literal addNotify } of the class 
         * {@link WebApplicationsNode} to enforce the IDE to update Logical View.
         * @param ev the event describing context where action has taken place
         */
        @Override
        public void fileFolderCreated(FileEvent ev) {
            System.out.println("File " + ev.getFile() + " created.");
            ((WebApplicationsNode.WebAppKeys) node.getChildren()).addNotify();
        }
    }//class
}//class