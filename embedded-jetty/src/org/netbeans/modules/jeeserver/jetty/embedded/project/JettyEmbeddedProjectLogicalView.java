/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jeeserver.jetty.embedded.project;

import java.awt.Image;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.modules.jeeserver.base.deployment.actions.StartServerAction;
import org.netbeans.modules.jeeserver.base.deployment.actions.StopServerAction;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;

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

public class JettyEmbeddedProjectLogicalView implements LogicalViewProvider {

    private static final Logger LOG = Logger.getLogger(JettyEmbeddedProjectLogicalView.class.getName());

    //@StaticResource()
    //public static final String JETTY_ICON = "org/netbeans/modules/jeeserver/jetty/resources/jetty01-16x16.jpg";

    private final JettyEmbeddedProject project;
    private ProjectNode projectNode;
    private FilterNode webApplicationsNode;
    private FilterNode librariesRootNode;

    public JettyEmbeddedProjectLogicalView(JettyEmbeddedProject project) {
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
//            String folderPath = project.getLookup()
//                                    .lookup(ServerInstanceProperties.class)
//                                    .getLayerProjectFolderPath();
            BaseUtils.out("%%%%%%%%%%%%%%%% createLogicalView");
            projectNode = new ProjectNode(nodeOfProjectFolder, project);
            return projectNode;
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
        return projectNode;
    }

    @NbBundle.Messages({
        "JettyEmbeddedProjectLogicalView.shortDescription=Server in {0}",})
    protected final class ProjectNode extends FilterNode {

        final JettyEmbeddedProject project;

        public ProjectNode(Node node, JettyEmbeddedProject project, int a)
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
//"Projects/org-jetty-embedded-instance-project/Nodes",                            
        public ProjectNode(Node node, JettyEmbeddedProject project)
                throws DataObjectNotFoundException {
            
            super(node,
                    NodeFactorySupport.createCompositeChildren(
                            project,
                            "Projects/org-jetty-embedded-instance-project/Nodes"
                    ),                                                        
//                            project.getLookup()
//                                    .lookup(ServerInstanceProperties.class)
//                                    .getLayerProjectFolderPath()),
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
//               JettyEmbeddedProjectLogicalView_shortDescription(project.getProjectDirectory().getPath()));
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
BaseUtils.out("^^^^^^^^^^^^^^ imagePath" + this.project.getIconImagePath());
            return ImageUtilities.loadImage(this.project.getIconImagePath());
        }
        
        
        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        @Override
        public String getDisplayName() {
BaseUtils.out("^^^^^^^^^^^^^^ displayName" + this.project.getProjectInformation().getDisplayName());
            
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
