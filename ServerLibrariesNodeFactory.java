/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jeeserver.jetty.embedded.project.nodes;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.netbeans.modules.jeeserver.base.embedded.project.nodes.libs.LibrariesFileNode;
import org.netbeans.modules.jeeserver.base.embedded.project.nodes.libs.NodeOptions;
import static org.netbeans.modules.jeeserver.base.embedded.project.nodes.libs.NodeOptions.FILE_IN_EXT;
import static org.netbeans.modules.jeeserver.base.embedded.project.nodes.libs.NodeOptions.FILE_IN_ROOT;
import static org.netbeans.modules.jeeserver.base.embedded.project.nodes.libs.NodeOptions.FOLDER_IN_EXT;
import static org.netbeans.modules.jeeserver.base.embedded.project.nodes.libs.NodeOptions.FOLDER_IN_ROOT;
import org.netbeans.modules.jeeserver.base.embedded.utils.EmbConstants;
import org.netbeans.modules.jeeserver.base.embedded.utils.EmbUtils;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;

/**
 *
 * @author Valery
 */

@NodeFactory.Registration(projectType = "org-jetty-embedded-instance-project", position = 0)
public class ServerLibrariesNodeFactory implements NodeFactory {

    private static final Logger LOG = Logger.getLogger(ServerLibrariesNodeFactory.class.getName());

    /**
     * Returns an object of type Node[] for a given {@link NodeOptions }, and
     * key. Is called from the method @ {@link #createNodes(Project) }
     *
     * @param options
     * @param server
     * @param key
     * @return
     */
    public static Node[] getNodes(NodeOptions options, Project server, Object key) {
        Node[] nodes = new Node[0];
        switch (options) {
            case ROOT:
                nodes = createChildsForRoot(server, key);
                break;
            case FOLDER_IN_ROOT:
                nodes = createChildsForFolderRoot(server, key);
                break;
        }
        return nodes;
    }

    /**
     * Creates child nodes for the root node of the Libraries.
     *
     * @param server Jetty server of type Project
     * @return
     */
    @Override
    public NodeList<?> createNodes(Project server) {
        
        PackageView p;
        Node node = null;
        if (!EmbUtils.isEmbedded(server)) {
            return NodeFactorySupport.fixedNodeList();
        }
//        if (server.getProjectDirectory().getFileObject(JettyConstants.WEBAPPS_FOLDER) != null) {
        FileObject activeModules = server.getProjectDirectory().
               getFileObject(EmbConstants.SERVER_ACTIVE_MODULES_FOLDER);

        try {
            NodeOptions opts = NodeOptions.ROOT;
            Node originalNode = DataObject.find(activeModules).getNodeDelegate();
            //
            // FileKeys keys model for Server Libraries node
            //
            LibrariesFileNode.FileKeys keys = new LibrariesFileNode.FileKeys(server, activeModules, opts);
            
            LibrariesFileNode node = new LibrariesFileNode(opts, server, originalNode, activeModules.getPath(), keys);
            return NodeFactorySupport.fixedNodeList(node);
        } catch (DataObjectNotFoundException ex) {
            LOG.log(Level.INFO, ex.getMessage());
        }
        //If the above try/catch fails, e.g.,
        //our item isn't in the lookup,
        //then return an empty list of nodes:
        return NodeFactorySupport.fixedNodeList();

    }

    /**
     * Creates child nodes for the root node of the Libraries.
     *
     * @param server Jetty server of type Project
     * @return
     */
    public static Node[] createChildsForRootExt(Project server, Object key) {
        boolean isfolder = new File(key.toString()).isDirectory();

        if (isfolder) {
            return createNodes(FOLDER_IN_EXT, server, key);
        } else {
            return createNodes(FILE_IN_EXT, server, key);
        }
    }

    public static Node[] createChildsForFolderExt(Project server, Object key) {
        boolean isfolder = new File(key.toString()).isDirectory();

        if (isfolder) {
            return createNodes(FOLDER_IN_EXT, server, key);
        } else {
            return createNodes(FILE_IN_EXT, server, key);
        }
    }

    public static Node[] createChildsForFolderRoot(Project server, Object key) {
        boolean isfolder = new File(key.toString()).isDirectory();

        if (isfolder) {
            return createNodes(FOLDER_IN_ROOT, server, key);
        } else {
            return createNodes(FILE_IN_ROOT, server, key);
        }
    }

    /**
     * Creates child nodes for the root node of the Libraries.
     *
     * @param server Jetty server of type Project
     * @param key
     * @return
     */
    public static Node[] createChildsForRoot(Project server, Object key) {
        boolean isfolder = new File(key.toString()).isDirectory();
        if (isfolder) {
            return createNodes(FOLDER_IN_ROOT, server, key);
        } else {
            return createNodes(FILE_IN_ROOT, server, key);
        }
    }

    public static Node[] createNodes(NodeOptions options, Project server, Object key) {
        if (!EmbUtils.isEmbedded(server) ) {
            return new Node[0];
        }

        if (server.getProjectDirectory().getFileObject(EmbConstants.SERVER_ACTIVE_MODULES_FOLDER) != null) {
            try {
                FileObject keyFo = FileUtil.toFileObject(new File(key.toString()));
                if ( keyFo == null ) {
                    BaseUtils.out("1 LibraryNodeFactory key = " + key);
                }
                if ( DataObject.find(keyFo) == null ) {
                    BaseUtils.out("2 LibraryNodeFactory key = " + key);
                }
                //
                // server-config/active-modules folder node
                //
                Node originalNode = DataObject.find(keyFo).getNodeDelegate();

                LibrariesFileNode node;
                
                boolean isfolder = new File(key.toString()).isDirectory();

                if (isfolder) {
                    LibrariesFileNode.FileKeys keys = new LibrariesFileNode.FileKeys(server, keyFo, options);
                    node = new LibrariesFileNode(options, server, originalNode, key, keys);
                } else {
                    node = new LibrariesFileNode(options, server, originalNode, key);
                }
                return new Node[]{node};
            } catch (DataObjectNotFoundException ex) {
                LOG.log(Level.INFO, ex.getMessage());
            }
        }
        //If the above try/catch fails, e.g.,
        //our item isn't in the lookup,
        //then return an empty list of nodes:
        return new Node[0];
    }

}
