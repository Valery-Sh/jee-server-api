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
import org.netbeans.modules.jeeserver.base.deployment.actions.StartServerAction;
import org.netbeans.modules.jeeserver.base.deployment.actions.StopServerAction;
//import static org.netbeans.modules.jeeserver.base.embedded.server.project.Bundle.EmbeddedServerProjectLogicalView_shortDescription;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteConstants;

import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.FilterNode;
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
public class ServerSuiteProjectLogicalView implements LogicalViewProvider {

    private static final Logger LOG = Logger.getLogger(ServerSuiteProjectLogicalView.class.getName());

    private final ServerSuiteProject suite;


    private ProjectNode suiteNode;
    private FilterNode webApplicationsNode;
    private FilterNode librariesRootNode;

    public ServerSuiteProjectLogicalView(ServerSuiteProject suite) {
        this.suite = suite;
    }

    @Override
    public Node createLogicalView() {
        try {
            //Obtain the suite directory's node:
            FileObject projectDirectory = suite.getProjectDirectory();
            DataFolder projectFolder = DataFolder.findFolder(projectDirectory);
            Node nodeOfProjectFolder = projectFolder.getNodeDelegate();
            suiteNode = new ProjectNode(nodeOfProjectFolder, suite);
            return suiteNode;
        } catch (DataObjectNotFoundException ex) {
            LOG.log(Level.INFO, ex.getMessage());
            return new AbstractNode(FilterNode.Children.LEAF);
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
        return suiteNode;
    }

//    @NbBundle.Messages({
//        "EmbeddedServerProjectLogicalView.shortDescription=Server in {0}",})
    protected final class ProjectNode extends FilterNode {

        final ServerSuiteProject project;

        public ProjectNode(Node node, ServerSuiteProject suite, int a)
                throws DataObjectNotFoundException {
            super(node,
                    new FilterNode.Children(node),
                    new ProxyLookup(
                            new Lookup[]{
                                Lookups.singleton(suite),
                                node.getLookup()
                            }));
            this.project = suite;
            init();
        }
//"Projects/org-jetty-embedded-instance-suite/Nodes",                            
        public ProjectNode(Node node, ServerSuiteProject suite)
                throws DataObjectNotFoundException {
            
            super(node,
                    NodeFactorySupport.createCompositeChildren(suite,
                            "Projects/org-netbeans-modules-jeeserver-base-embedded-project/Nodes"
                    ),                                                        
//                            suite.getLookup()
//                                    .lookup(ServerInstanceProperties.class)
//                                    .getLayerProjectFolderPath()),
                    // new FilterNode.Children(node),
                    new ProxyLookup(
                            new Lookup[]{
                                Lookups.singleton(suite),
                                node.getLookup()
                            }));
            this.project = suite;
            init();
        }

        private void init() {
//            setShortDescription(
//               EmbeddedServerProjectLogicalView_shortDescription(project.getProjectDirectory().getPath()));
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
//                new PropertiesAction().createContextAwareInstance(suite.getLookup()),
            };
        }

        @Override
        public Image getIcon(int type) {
            return ImageUtilities.loadImage(SuiteConstants.SERVER_PROJECT_ICON);
        }
        
        
        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        @Override
        public String getDisplayName() {
            return project.getProjectInformation().getDisplayName();
        }

    }

    @Override
    public Node findPath(Node root, Object target) {
        //leave unimplemented for now
        return null;
    }

}
