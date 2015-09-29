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
package org.netbeans.modules.jeeserver.base.embedded.project;

import java.awt.Image;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.modules.jeeserver.base.deployment.ServerInstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.actions.StartServerAction;
import org.netbeans.modules.jeeserver.base.deployment.actions.StopServerAction;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
//import static org.netbeans.modules.jeeserver.base.embedded.project.Bundle.EmbeddedProjectLogicalView_shortDescription;

import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.FilterNode;
import org.openide.nodes.FilterNode.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author V. Shyshkin
 */
public class EmbeddedProjectLogicalView implements LogicalViewProvider {

    private static final Logger LOG = Logger.getLogger(EmbeddedProjectLogicalView.class.getName());

    //@StaticResource()
    //public static final String JETTY_ICON = "org/netbeans/modules/jeeserver/jetty/resources/jetty01-16x16.jpg";

    private final EmbeddedProject project;
    private ProjectNode projectNode;
    private FilterNode webApplicationsNode;
    private FilterNode librariesRootNode;

    public EmbeddedProjectLogicalView(EmbeddedProject project) {
        this.project = project;
    }

    @Override
    public Node createLogicalView() {
        try {
            //Obtain the project directory's node:
            FileObject projectDirectory = project.getProjectDirectory();
            DataFolder projectFolder = DataFolder.findFolder(projectDirectory);
            Node nodeOfProjectFolder = projectFolder.getNodeDelegate();
            //Decorate the project directory's node:
/*            String folderPath = project.getLookup()
                                    .lookup(ServerInstanceProperties.class)
                                    .getLayerProjectFolderPath();
        
            BaseUtils.out("%%%%%%%%%%%%%%%% folderPath=" + folderPath);
*/        
            projectNode = new ProjectNode(nodeOfProjectFolder, project);
            return projectNode;
        } catch (DataObjectNotFoundException ex) {
            LOG.log(Level.INFO, ex.getMessage());
            return new AbstractNode(Children.LEAF);
        }
    }

    public FilterNode getWebApplicationsNode() {
        return webApplicationsNode;
    }

    public void setWebApplicationsNode(FilterNode webApplicationsNode) {
        this.webApplicationsNode = webApplicationsNode;
    }

    public FilterNode getLibrariesRootNode() {
        return librariesRootNode;
    }

    public void setLibrariesRootNode(FilterNode librariesRootNode) {
        this.librariesRootNode = librariesRootNode;
    }

    public ProjectNode getProjectNode() {
        return projectNode;
    }

    @NbBundle.Messages({
        "EmbeddedProjectLogicalView.shortDescription=Server in {0}",})
    protected final class ProjectNode extends FilterNode {

        final EmbeddedProject project;

        public ProjectNode(Node node, EmbeddedProject project, int a)
                throws DataObjectNotFoundException {
            super(node,
                    new FilterNode.Children(node),
                    new ProxyLookup(
                            new Lookup[]{
                                Lookups.singleton(project),
                                node.getLookup()
                            }));
            this.project = project;
            init();
        }

        public ProjectNode(Node node, EmbeddedProject project)
                throws DataObjectNotFoundException {
            
            super(node,
                    NodeFactorySupport.createCompositeChildren(
                            project,
                            project.getLookup()
                                    .lookup(ServerInstanceProperties.class)
                                    .getLayerProjectFolderPath()),
                    // new FilterNode.Children(node),
                    new ProxyLookup(
                            new Lookup[]{
                                Lookups.singleton(project),
                                node.getLookup()
                            }));
            this.project = project;
            init();
        }

        private void init() {
//            setShortDescription(
//               EmbeddedProjectLogicalView_shortDescription(project.getProjectDirectory().getPath()));
        }

        @Override
        public Action[] getActions(boolean arg0) {
            return new Action[]{
                new StartServerAction().createContextAwareInstance(project.getLookup()),
                new StopServerAction().createContextAwareInstance(project.getLookup()),
                null,
                CommonProjectActions.newFileAction(),
                CommonProjectActions.copyProjectAction(),
                CommonProjectActions.deleteProjectAction(),
                CommonProjectActions.closeProjectAction(),
                CommonProjectActions.setAsMainProjectAction(),
                null,
//                new PropertiesAction().createContextAwareInstance(project.getLookup()),
            };
        }

        @Override
        public Image getIcon(int type) {
            //return project.getProjectInformation().getIcon();
            return ImageUtilities.loadImage(this.project.getIconImagePath());
        }
        
        
        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        @Override
        public String getDisplayName() {
            //return project.getProjectDirectory().getName();
            return project.getProjectInformation().getDisplayName();
        }

    }

    @Override
    public Node findPath(Node root, Object target) {
        //leave unimplemented for now
        return null;
    }

}