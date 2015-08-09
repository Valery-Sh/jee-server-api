package org.netbeans.modules.jeeserver.jetty.project.nodes.libs;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.jetty.project.nodes.libs.LibrariesFileNode.FileKeys;
import static org.netbeans.modules.jeeserver.jetty.project.nodes.libs.NodeOptions.FILE_IN_EXT;
import static org.netbeans.modules.jeeserver.jetty.project.nodes.libs.NodeOptions.FILE_IN_ROOT;
import static org.netbeans.modules.jeeserver.jetty.project.nodes.libs.NodeOptions.FOLDER_IN_EXT;
import static org.netbeans.modules.jeeserver.jetty.project.nodes.libs.NodeOptions.FOLDER_IN_ROOT;
import static org.netbeans.modules.jeeserver.jetty.project.nodes.libs.NodeOptions.LIB_EXT;
import static org.netbeans.modules.jeeserver.jetty.project.nodes.libs.NodeOptions.ROOT;
import org.netbeans.modules.jeeserver.jetty.util.JettyConstants;
import org.netbeans.modules.jeeserver.jetty.util.Utils;
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
 * @author V. Shyshkin
 */
@NodeFactory.Registration(projectType = "org-jetty-server-instance-project")
public class LibraryNodeFactory implements NodeFactory {

    private static final Logger LOG = Logger.getLogger(LibraryNodeFactory.class.getName());

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
            case LIB_EXT:
                nodes = createChildsForRootExt(server, key);
                break;
            case FOLDER_IN_ROOT:
                nodes = createChildsForFolderRoot(server, key);
                break;
            case FOLDER_IN_EXT:
                nodes = createChildsForFolderExt(server, key);
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
        if (!Utils.isJettyServer(server)) {
            return NodeFactorySupport.fixedNodeList();
        }
//        if (server.getProjectDirectory().getFileObject(JettyConstants.WEBAPPS_FOLDER) != null) {
        FileObject lib = server.getProjectDirectory().
                getFileObject(JettyConstants.JETTYBASE_FOLDER + "/lib");

        try {
            NodeOptions opts = ROOT;
            Node originalNode = DataObject.find(lib).getNodeDelegate();
            FileKeys keys = new FileKeys(server, lib, opts);
            LibrariesFileNode node = new LibrariesFileNode(opts, server, originalNode, lib.getPath(), keys);
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
     * @return
     */
    public static Node[] createChildsForRoot(Project server, Object key) {
        boolean isfolder = new File(key.toString()).isDirectory();
        Path extLib = Paths.get(server.getProjectDirectory().getPath(),
                JettyConstants.JETTYBASE_FOLDER, "lib/ext");
        Path keyPath = Paths.get(key.toString());
        if (keyPath.equals(extLib)) {
            return createNodes(LIB_EXT, server, key);
        }
        if (isfolder) {
            return createNodes(FOLDER_IN_ROOT, server, key);
        } else {
            return createNodes(FILE_IN_ROOT, server, key);
        }
    }

    public static Node[] createNodes(NodeOptions options, Project server, Object key) {
        if (!Utils.isJettyServer(server)) {
            return new Node[0];
        }

        if (server.getProjectDirectory().getFileObject(JettyConstants.WEBAPPS_FOLDER) != null) {
            try {
                FileObject keyFo = FileUtil.toFileObject(new File(key.toString()));
                Node originalNode = DataObject.find(keyFo).getNodeDelegate();

                LibrariesFileNode node;
                boolean isfolder = new File(key.toString()).isDirectory();

                if (isfolder) {
                    FileKeys keys = new FileKeys(server, keyFo, options);
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
