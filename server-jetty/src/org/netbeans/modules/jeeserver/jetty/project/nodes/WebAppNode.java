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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.jeeserver.base.deployment.config.ServerInstanceAvailableModules;
import org.netbeans.modules.jeeserver.base.deployment.config.WebModuleConfig;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import static org.netbeans.modules.jeeserver.jetty.project.nodes.Bundle.WebAppNode_shortDescription;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author V. Shyshkin
 */
@NbBundle.Messages({
    "WebAppNode.shortDescription=Web project in {0}"
})
public class WebAppNode extends BaseWebAppNode {

    private static final Logger LOG = Logger.getLogger(WebModulesNodeFactory.class.getName());

    @StaticResource
    private static final String IMAGE = "org/netbeans/modules/jeeserver/jetty/resources/web-proj-icon.png";

    @StaticResource
    private static final String IMAGE_BADGE = "org/netbeans/modules/jeeserver/jetty/resources/web-pages-badge.png";

    /**
     * Creates a new instance of the class for the specified project and node
     * key an child nodes keys. The node created has child nodes.
     *
     * @param serverProj
     * @param key
     * @param childrenKeys keys of child nodes
     *
     * @throws DataObjectNotFoundException should never occur
     */
    protected WebAppNode(Project serverProj, Object key, FilterNode.Children.Keys childrenKeys) throws DataObjectNotFoundException {
        super(serverProj, key, childrenKeys);
        init();
    }

    protected WebAppNode(Project serverProj, Object key) throws DataObjectNotFoundException {
        super(serverProj, key);
        init();
    }

    private void init() {
        String path = ((WebModuleConfig) getWebAppKey())
                .getWebProjectPath();
        setShortDescription(WebAppNode_shortDescription(path));
    }

    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage(IMAGE);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public String getDisplayName() {
        return super.getDisplayName();
    }

    public static class ProjectViewKeys<T> extends FilterNode.Children.Keys<T> {

        private final Project serverProj;
        private final Project webProj;

        /**
         * Created a new instance of the class for the specified server
         * serverProject.
         *
         * @param serverProj the serverProject which is used to create an
         * instance for.
         */
        public ProjectViewKeys(Project serverProj, Project webProj) {
            this.serverProj = serverProj;
            this.webProj = webProj;
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

        /**
         * Called when children of the {@code Web Applications} are first asked
         * for nodes. For each child node of the folder named
         * {@literal "server-instance-config"} gets the name of the child file
         * with extension and adds to a List of Strings. Then invokes the method {@literal setKeys(List)
         * }.
         */
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
    }//class

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
                BaseUtils.out("PPPPPPPPPPPPPPPP webProjectPath= " + c.getWebProjectPath());
                Project webProject = FileOwnerQuery.getOwner(FileUtil.toFileObject(new File(c.getWebProjectPath())));
                getSourceRoot(webProject);
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

}
