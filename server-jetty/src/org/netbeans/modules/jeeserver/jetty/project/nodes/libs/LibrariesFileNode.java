package org.netbeans.modules.jeeserver.jetty.project.nodes.libs;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.netbeans.modules.jeeserver.base.deployment.utils.LibrariesFileLocator;
import org.netbeans.modules.jeeserver.jetty.project.JettyProjectLogicalView;
import static org.netbeans.modules.jeeserver.jetty.project.nodes.libs.NodeOptions.ROOT;
import org.netbeans.modules.jeeserver.jetty.util.JettyConstants;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;

/**
 *
 * @author V. Shyshkin
 */
public class LibrariesFileNode extends FilterNode {

    private static final Logger LOG = Logger.getLogger(LibrariesFileNode.class.getName());

    private final Project server;
    private final Object key;
    private final Path keyPath;
    private final NodeOptions option;
    private FilterNode.Children.Keys childrenKeys;

    protected LibrariesFileNode(NodeOptions option, Project server, Node nodeForKey, Object key, FilterNode.Children.Keys childrenKeys) throws DataObjectNotFoundException {
        super(nodeForKey, childrenKeys);
        this.option = option;
        this.server = server;
        this.key = key;
        this.childrenKeys = childrenKeys;
        if (key != null) {
            keyPath = Paths.get(key.toString());
        } else {
            keyPath = null;
        }
        init(server, option);
    }
    private void init(Project server, NodeOptions options) {
        if ( option ==  ROOT) {
            JettyProjectLogicalView view = server.getLookup().lookup(JettyProjectLogicalView.class);
            view.setLibrariesRootNode(this);
            LibUtil.updateLibraries(server);
        }
    }
    
    protected LibrariesFileNode(NodeOptions option, Project server, Node nodeForKey, Object key) throws DataObjectNotFoundException {
        super(nodeForKey);
        this.server = server;
        this.option = option;
        this.key = key;
        if (key != null) {
            keyPath = Paths.get(key.toString());
        } else {
            keyPath = null;
        }
    }

    public LibrariesFileNode findChildByKey(Object key) {
        org.openide.nodes.Children c = getChildren();
        Node[] nodes = c.getNodes();
        if (nodes == null || nodes.length == 0) {
            return null;
        }
        for (Node node : nodes) {
            if (node instanceof LibrariesFileNode) {
                Path p1 = Paths.get(key.toString());
                Path p2 = Paths.get(((LibrariesFileNode) node).getKey().toString());
                if (p1.equals(p2)) {
                    return (LibrariesFileNode) node;
                }
            }
        }

        return null;

    }

    public boolean isExtNode() {
        Path p1 = FileUtil.toFile(getExtFolder()).toPath();
        Path p2 = Paths.get(key.toString());
        return p1.equals(p2);
    }
    public boolean isLibNode() {
        Path p1 = FileUtil.toFile(getExtFolder().getParent()).toPath();
        Path p2 = Paths.get(key.toString());
        return p1.equals(p2);
    }

    public void addLibraries(Set<Library> libs) {

        for (Library lib : libs) {
            String name = lib.getName();
            String displayName = lib.getDisplayName();
            List<File> fileList = LibrariesFileLocator.findFiles(lib);
            if (fileList.isEmpty()) {
                continue;
            }
            FileObject extLib = getExtFolder();
            Properties props = new Properties();
            if (extLib.getFileObject(name) != null) {
                continue;
            }
            FileObject libFolder = null;
            try {
                libFolder = FileUtil.createFolder(extLib, name);
            } catch (IOException ex) {
                LOG.log(Level.INFO, ex.getMessage());
                continue;
            }

            for (File f : fileList) {
                if (!f.exists()) {
                    continue;
                }
                try {
                    FileObject fo = FileUtil.toFileObject(f);
                    if (libFolder.getFileObject(fo.getNameExt()) != null) {
                        continue;
                    }
                    fo.copy(libFolder, fo.getName(), fo.getExt());
                    props.setProperty("name", name);
                    props.setProperty("displayName", displayName);

                    BaseUtil.storeProperties(props, libFolder, JettyConstants.LIBRARY_FILE);
                } catch (IOException ex) {
                    LOG.log(Level.INFO, ex.getMessage());
                    continue;
                }

            }

        }
    }

    public Properties getLibProperties() {
        Path p = Paths.get(key.toString());
        if (!Files.isDirectory(p)) {
            return null;
        }
        FileObject fo = FileUtil.toFileObject(p.toFile());
        FileObject propFo = fo.getFileObject(JettyConstants.LIBRARY_FILE);
        if (propFo == null) {
            return null;
        }
        return BaseUtil.loadProperties(propFo);
    }

    protected FileObject getExtFolder() {
        return server.getProjectDirectory().getFileObject(JettyConstants.JETTYBASE_FOLDER + "/lib/ext");
    }

    public void addJarOrFolder(File file) {

    }

    public Project getServer() {
        return server;
    }

    public Object getKey() {
        return key;
    }

    public Children.Keys getChildrenKeys() {
        return childrenKeys;
    }

    public Path getKeyPath() {
        return keyPath;
    }

    public NodeOptions getOptions() {
        return option;
    }

    @Override
    public Action[] getActions(boolean context) {
        return LibUtil.getActions(this);
    }

    @Override
    public Node getOriginal() {
        return super.getOriginal();
    }

    @Override
    public Image getIcon(int type) {
        return LibUtil.getIcon(this, type);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return LibUtil.getOpenedIcon(this, type);
    }

    @Override
    public String getDisplayName() {
        return LibUtil.getDisplayName(this);
    }

    @Override
    public String getHtmlDisplayName() {
        return LibUtil.getHtmlDisplayName(this);
    }

    public static class FileKeys<T> extends FilterNode.Children.Keys<T> {

        private final Project server;
        private final NodeOptions options;
        private final FileObject forFile;

        /**
         * Created a new instance of the class for the specified server server.
         *
         * @param serverProj the server which is used to create an instance for.
         */
        public FileKeys(Project server, FileObject forFile, NodeOptions options) {
            this.server = server;
            this.options = options;
            this.forFile = forFile;
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
            return LibraryNodeFactory.getNodes(options, getServer(), key);
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
            removeNotify();
            setKeys(LibUtil.addNotify(server, forFile, options));
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
         * @return the server
         */
        protected Project getServer() {
            return server;
        }
    }//class

}//class
