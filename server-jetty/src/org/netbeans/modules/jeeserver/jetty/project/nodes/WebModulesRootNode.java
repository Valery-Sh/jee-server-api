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
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.jeeserver.base.deployment.config.ModulesChangeEvent;
import static org.netbeans.modules.jeeserver.base.deployment.config.ModulesChangeEvent.DELETED;
import static org.netbeans.modules.jeeserver.base.deployment.config.ModulesChangeEvent.DISPOSE;
import org.netbeans.modules.jeeserver.base.deployment.config.ModulesChangeListener;
import org.netbeans.modules.jeeserver.base.deployment.config.ServerInstanceAvailableModules;
import org.netbeans.modules.jeeserver.base.deployment.config.WebModuleConfig;
import static org.netbeans.modules.jeeserver.jetty.project.nodes.Bundle.WebModulesRootNode_availableWebApps;
import static org.netbeans.modules.jeeserver.jetty.project.nodes.Bundle.WebModulesRootNode_shortDescription;
import org.netbeans.modules.jeeserver.jetty.util.JettyConstants;
import org.openide.actions.PropertiesAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle.Messages;

/**
 * Represents the root node of the logical view of the serverProject's folder
 * named {@literal webapps}. Every jetty server serverProject may contain a
 * child folder named {@literal webapps}. Its logical name is
 * {@literal Web Applications}.
 *
 * @author V. Shyshkin
 */
@Messages({
    "WebModulesRootNode.shortDescription=Registered applications for this server",
    "WebModulesRootNode.availableWebApps=Available Web Applications"
})
public class WebModulesRootNode extends FilterNode {
    private static final Logger LOG = Logger.getLogger(WebModulesRootNode.class.getName());

    private ModulesChangeListener modulesChangeListener;

    @StaticResource
    private static final String IMAGE = "org/netbeans/modules/jeeserver/jetty/resources/web-pages-badge.png";

    /**
     * Creates a new instance of the class for a specified serverProject.
     *
     * @param serverProj a serverProject which is used to create an instance of
     * the class.
     * @throws DataObjectNotFoundException
     */
    public WebModulesRootNode(Project serverProj) throws DataObjectNotFoundException {
        super(DataObject.find(serverProj.getProjectDirectory().
                getFileObject(JettyConstants.WEBAPPS_FOLDER)).getNodeDelegate(),
                new RootChildrenKeys(serverProj));

    }

    public WebModulesRootNode(Project serverProj, Children children) throws DataObjectNotFoundException {
        super(DataObject.find(serverProj.getProjectDirectory().
                getFileObject(JettyConstants.WEBAPPS_FOLDER))
                .getNodeDelegate(), children);

    }

    /**
     * Creates an instance of class {@link FileChangeHandler} and adds it as a
     * listener of the {@literal FileEvent } to the {@literal FileObject}
     * associated with a {@literal server-instance-config} folder.
     *
     * @param serverProj
     */
    protected final void init(Project serverProj) {
        modulesChangeListener = new ModulesChangeHandler(serverProj, this);
        serverProj.getLookup().lookup(ServerInstanceAvailableModules.class)
                .addModulesChangeListener(modulesChangeListener);

        setShortDescription(WebModulesRootNode_shortDescription());
    }

    /**
     * Returns the logical name of the node.
     *
     * @return the value "Web Application"
     */
    @Override
    public String getDisplayName() {
        return WebModulesRootNode_availableWebApps();
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

        for (Action a : super.getActions(ctx)) {
            if (a instanceof PropertiesAction) {
                propAction = a;
                break;
            }
        }

        Project server = ((RootChildrenKeys) getChildren()).getServerProj();
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

    public static class ModulesChangeHandler implements ModulesChangeListener {

        private final Project server;
        private final WebModulesRootNode node;
        private ModulesChangeEvent event;

        public ModulesChangeHandler(Project server, WebModulesRootNode node) {
            this.server = server;
            this.node = node;
        }

        @Override
        public void availableModulesChanged(ModulesChangeEvent event) {
            this.event = event;
            if (event.getEventType() == DISPOSE || event.getEventType() == DELETED) {
                ((RootChildrenKeys) node.getChildren()).removeNotify();
            }
            ((RootChildrenKeys) node.getChildren()).addNotify();

        }

    }

/*    public static class ProjectViewKeys<T> extends FilterNode.Children.Keys<T> {

        private final Project serverProj;
        private final Project webProj;
      public ProjectViewKeys(Project serverProj, Project webProj) {
            this.serverProj = serverProj;
            this.webProj = webProj;
        }

        @Override
        protected Node[] createNodes(T key) {
            Node node = null;
            if (key instanceof SourceGroup) {
                node = PackageView.createPackageView((SourceGroup) key);
            } else {
                ServerInstanceAvailableModules am = serverProj.getLookup().lookup(ServerInstanceAvailableModules.class);
                WebModuleConfig wmc = am.getModuleConfig(webProj);
                String fp = wmc.getWebFolderPath();
                if (fp != null) {
                    Path p1 = Paths.get(fp);
                    Path p2 = Paths.get(key.toString());
                    if (p1.equals(p2)) {

                        FileObject webFo = FileUtil.toFileObject(new File(fp));
                        DataObject dobj;
                        try {
                            dobj = DataObject.find(webFo);
                            if (dobj != null && dobj.getNodeDelegate() != null) {

                                node = new WebFolderNode(dobj.getNodeDelegate());
                            }
                        } catch (DataObjectNotFoundException ex) {
                            LOG.log(Level.INFO, ex.getMessage());
                        }
                    }
                }
            }
            return new Node[]{node};
        }
        @Override
        protected void addNotify() {
            List keyArray = new ArrayList();
            for (SourceGroup sg : getSourceGroups(webProj)) {
                keyArray.add(sg);
            }
            keyArray.add(getWebFolder());
            this.setKeys(keyArray);
        }

        protected String getWebFolder() {
            ServerInstanceAvailableModules am = serverProj.getLookup().lookup(ServerInstanceAvailableModules.class);
            WebModuleConfig wmc = am.getModuleConfig(webProj);
            return wmc.getWebFolderPath();
        }

        protected List<SourceGroup> getSourceGroups(Project webProject) {
            Sources sources = ProjectUtils.getSources(webProject);
            SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            SourceGroup[] resourcesSourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_RESOURCES);
            List<SourceGroup> list = Arrays.asList(sourceGroups);
            list = new ArrayList<SourceGroup>(list);
            for (SourceGroup sg : resourcesSourceGroups) {
                list.add(sg);
            }
            return list;
        }

        protected FileObject getSourceRoot(Project webProject) {
            Sources sources = ProjectUtils.getSources(webProject);
            SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            FileObject result = null;
            try {
                for (SourceGroup sourceGroup : sourceGroups) {
                    result = sourceGroup.getRootFolder();
                }
            } catch (UnsupportedOperationException ex) {
                LOG.log(Level.INFO, ex.getMessage());
            }
            return result;
        }

        @Override
        protected void removeNotify() {
            this.setKeys(Collections.EMPTY_LIST);
        }

        public Project getServerProj() {
            return serverProj;
        }
    }//class
*/
    /**
     * The implementation of the Children.Key of the {@literal Server Libraries}
     * node.
     *
     * @param <T>
     */
    public static class RootChildrenKeys<T> extends FilterNode.Children.Keys<T> {

        private final Project serverProj;

        /**
         * Created a new instance of the class for the specified server
         * serverProject.
         *
         * @param serverProj the serverProject which is used to create an
         * instance for.
         */
        public RootChildrenKeys(Project serverProj) {
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
            return new Node[]{WebModulesNodeFactory.getNode(serverProj, key)};
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
            ServerInstanceAvailableModules avm = serverProj.getLookup().lookup(ServerInstanceAvailableModules.class);

            WebModuleConfig[] list = avm.getModuleList();

            List keyArray = new ArrayList<>(list.length);
            for (WebModuleConfig c : list) {
                Project webProject = FileOwnerQuery.getOwner(FileUtil.toFileObject(new File(c.getWebProjectPath())));

                //getSourceRoot(webProject);
                keyArray.add(c);
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
         * @return the serverProject
         */
        public Project getServerProj() {
            return serverProj;
        }

        protected FileObject getSourceRoot(Project webProject) {
            Sources sources = ProjectUtils.getSources(webProject);
            SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            FileObject result = null;
            try {
                for (SourceGroup sourceGroup : sourceGroups) {
                    result = sourceGroup.getRootFolder();
                }
            } catch (UnsupportedOperationException ex) {
                LOG.log(Level.INFO, ex.getMessage());
            }
            return result;
        }

    }//class

}//class
