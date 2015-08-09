/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jeeserver.jetty.project.nodes.libs;

import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.swing.Action;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.common.api.J2eeLibraryTypeProvider;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.netbeans.modules.jeeserver.jetty.deploy.JettyServerPlatformImpl;
import org.netbeans.modules.jeeserver.jetty.project.JettyProjectLogicalView;

import static org.netbeans.modules.jeeserver.jetty.project.nodes.libs.NodeOptions.FILE_IN_EXT;
import static org.netbeans.modules.jeeserver.jetty.project.nodes.libs.NodeOptions.FOLDER_IN_EXT;
import static org.netbeans.modules.jeeserver.jetty.project.nodes.libs.NodeOptions.FOLDER_IN_ROOT;
import static org.netbeans.modules.jeeserver.jetty.project.nodes.libs.NodeOptions.LIB_EXT;
import static org.netbeans.modules.jeeserver.jetty.project.nodes.libs.NodeOptions.ROOT;
import org.netbeans.modules.jeeserver.jetty.util.JettyConstants;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.util.ImageUtilities;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Valery
 */
public class LibUtil {

    private static final Logger LOG = Logger.getLogger(LibraryNodeFactory.class.getName());

    private static final RequestProcessor RP = new RequestProcessor(LibrariesAction.class);

    @StaticResource
    private static final String ROOT_IMAGE = "org/netbeans/modules/jeeserver/jetty/resources/libraries-badge.png";
    @StaticResource
    private static final String LIB_EXT_IMAGE = "org/netbeans/modules/jeeserver/jetty/resources/libraries.png";
    @StaticResource
    private static final String LIBRARY_IMAGE = "org/netbeans/modules/jeeserver/jetty/resources/library.png";

    public static void updateLibraries(Project server) {
        BaseDeploymentManager manager = (BaseDeploymentManager) BaseUtils.managerOf(server);
        final JettyServerPlatformImpl platform = JettyServerPlatformImpl.getInstance(manager);

        RP.post(() -> {
            platform.notifyLibrariesChanged(false);
            platform.fireChangeEvents();
            LibrariesFileNode ln = (LibrariesFileNode) manager.getServerProject().getLookup()
                    .lookup(JettyProjectLogicalView.class)
                    .getLibrariesRootNode();
            if (ln != null) {
                ((LibrariesFileNode.FileKeys) ln.getChildrenKeys()).addNotify();
            }

        });

    }

    public static LibrariesFileNode getLibrariesRootNode(Project server) {
        JettyProjectLogicalView view = server.getLookup().lookup(JettyProjectLogicalView.class);
        return (LibrariesFileNode) view.getLibrariesRootNode();

    }

    public static Image getIcon(LibrariesFileNode node, int type) {

        NodeOptions opt = node.getOptions();
        if (opt == ROOT) {
            return getRootIcon(type);
        } else if (opt == LIB_EXT) {
            //return ImageUtilities.loadImage(LIB_EXT_IMAGE);
            return getExtIcon(type);
        } else if (node.getLibProperties() != null) {
            return ImageUtilities.loadImage(LIBRARY_IMAGE);
        } else {
            return node.getOriginal().getIcon(type);
        }
    }

    public static Image getOpenedIcon(LibrariesFileNode node, int type) {
        return getIcon(node, type);
    }

    public static Image getRootIcon(int type) {
        DataFolder root = DataFolder.findFolder(FileUtil.getConfigRoot());
        Image original = root.getNodeDelegate().getIcon(type);
        return ImageUtilities.mergeImages(original,
                ImageUtilities.loadImage(ROOT_IMAGE), 7, 7);
    }

    public static Image getExtIcon(int type) {
        Image original = ImageUtilities.loadImage(LIB_EXT_IMAGE);
        return ImageUtilities.mergeImages(original,
                ImageUtilities.loadImage(ROOT_IMAGE), 7, 7);
    }

    public static String getDisplayName(LibrariesFileNode node) {
        NodeOptions options = node.getOptions();
        return node.getKey() == null ? "null" : node.getKey().toString();
    }

    public static String getHtmlDisplayName(LibrariesFileNode node) {
        String tx = "";
        switch (node.getOptions()) {
            case ROOT:
                tx = "<font color='!textText'>" + "Libraries" + "</font>";
                break;
            case LIB_EXT:
                tx = "<font color='!textText'>" + "External Libraries" + "</font>";
                break;
            case FOLDER_IN_ROOT:
            case FILE_IN_ROOT:
                tx = getHtmlDisplayName(node.getServer(), node.getKey());
                break;
            case FOLDER_IN_EXT:
                Path p = Paths.get(node.getKey().toString());
                String nm = p.getFileName().toString();
                Properties props = node.getLibProperties();
                if (props != null && props.getProperty("displayName") != null) {
                    nm = props.getProperty("displayName");
                }

                tx = "<font color='!textText'>" + nm + "</font>";
                break;

            case FILE_IN_EXT:
                p = Paths.get(node.getKey().toString());
                tx = "<font color='!textText'>" + p.getFileName() + "</font>";
                break;

        }
        return tx;
    }

    public static String getHtmlDisplayName(Project server, Object key) {

        final String jh = BaseUtils.managerOf(server).getInstanceProperties()
                .getProperty(BaseConstants.HOME_DIR_PROP);
        final String jb = server.getProjectDirectory()
                .getFileObject(JettyConstants.JETTYBASE_FOLDER).getPath();

        final Path jettyHome = Paths.get(jh);
        final Path jettyBase = Paths.get(jb);

        Path p = Paths.get(key.toString());
        String nm = p.toFile().getName();

        String s = "${jetty.base}";
        if (p.startsWith(jettyHome)) {
            s = "${jetty.Home}";
            s += jettyHome.relativize(p);
        } else {
            s += jettyBase.relativize(p);
        }

        nm = "<font color='!textText'>" + s + "</font>";
        return nm;
    }

    public static Action[] getActions(LibrariesFileNode node) {
        Action[] actions = new Action[0];
        switch (node.getOptions()) {
            case FOLDER_IN_EXT:
            case FILE_IN_EXT:
                actions = new Action[]{
                    new LibrariesAction.RemoveFileContextAction(node.getServer(), node)
                };
                break;
            case ROOT:
            case LIB_EXT:
                actions = new Action[]{
                    new LibrariesAction.AddLibraryContextAction(node.getServer(), node),
                    new LibrariesAction.AddJarFolderContextAction(node.getServer(), node)
                };
                break;
        }
        return actions;
    }

    public static List addNotify(Project server, FileObject baseKey, NodeOptions options) {
        List list = new ArrayList();
        switch (options) {
            case ROOT:
                list = addRootNotify(server);
                break;
            case LIB_EXT:
                list = addRootExtNotify(server);
                break;
            case FOLDER_IN_ROOT:
            case FOLDER_IN_EXT:
                list = addFolderNotify(server, baseKey, options);
                break;
        }
        return list;
    }

    public static List addRootNotify(Project server) {
        BaseUtils.out("addRootNotify");
        BaseDeploymentManager manager = (BaseDeploymentManager) BaseUtils.managerOf(server);
        final String jh = manager.getInstanceProperties()
                .getProperty(BaseConstants.HOME_DIR_PROP);
        final String jb = server.getProjectDirectory()
                .getFileObject(JettyConstants.JETTYBASE_FOLDER).getPath();

        final Path jettyHome = Paths.get(jh);
        final Path jettyBase = Paths.get(jb);

        final Path extLib = Paths.get(jb, "lib/ext");

        JettyServerPlatformImpl platform = JettyServerPlatformImpl.getInstance(manager);
        LibraryImplementation[] libs = platform.getLibraries();
        final List jars = new ArrayList<>();
        if (libs != null) {
            for (LibraryImplementation lib : libs) {
                List<URL> urls = lib.getContent(J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH);

                for (URL url : urls) {
                    jars.add(FileUtil.archiveOrDirForURL(url).getPath());
                }

            }
        }
        if (Files.exists(extLib)) {
            jars.add(0, extLib.toString());
        }

        jars.sort((o1, o2) -> {
            if (o1 == null && o2 == null) {
                return 0;
            }
            if (o1 == null) {
                return -1;
            }
            if (o2 == null) {
                return 1;
            }
            Path p1 = Paths.get(o1.toString());
            Path p2 = Paths.get(o2.toString());
            if (p1.startsWith(jettyBase) && p2.startsWith(jettyHome)) {
                return -1;
            } else if (p1.startsWith(jettyHome) && p2.startsWith(jettyBase)) {
                return 1;
            }
            if (p1.compareTo(p2) == 0) {
                return 0;
            }
            Path rp1;
            Path rp2;
            if (p1.compareTo(extLib) == 0) {
                return -1;
            }
            if (p2.compareTo(extLib) == 0) {
                return 1;
            }

            if (p1.startsWith(jettyBase)) {
                rp1 = jettyBase.relativize(p1);
                rp2 = jettyBase.relativize(p2);
            } else {
                rp1 = jettyHome.relativize(p1);
                rp2 = jettyHome.relativize(p2);
            }
            return rp1.compareTo(rp2);
        });
        return jars;

    }

    public static List addFolderNotify(Project server, FileObject baseKey, NodeOptions options) {
        BaseDeploymentManager manager = (BaseDeploymentManager) BaseUtils.managerOf(server);
        final String jb = server.getProjectDirectory()
                .getFileObject(JettyConstants.JETTYBASE_FOLDER).getPath();

        final Path keypath = Paths.get(baseKey.getPath());
        final List jars = new ArrayList<>();
        FileObject[] files = FileUtil.toFileObject(keypath.toFile()).getChildren();

        for (FileObject fo : files) {
            Path path = Paths.get(fo.getPath());
            if (!path.equals(keypath)) {
                if (Files.isDirectory(path)) {
                    jars.add(path.toString());
//                        walkStep(jars, path, keypath);
                } else {
                    Path rp = keypath.relativize(path);
                    Path nm = Paths.get(path.toFile().getName());
                    if (rp.equals(nm)) {
                        jars.add(path.toString());
                    }
                }
            }
        }

        return jars;
    }

    public static List addRootExtNotify(Project server) {
        final String jb = server.getProjectDirectory()
                .getFileObject(JettyConstants.JETTYBASE_FOLDER).getPath();

        final Path extLib = Paths.get(jb, "lib/ext");
        final List jars = new ArrayList<>();
        try {
            Stream<Path> stream = Files.walk(extLib, 1);
            stream.forEach((path) -> {
                if (!path.equals(extLib)) {
                    if (Files.isDirectory(path)) {
                        jars.add(path.toString());
                    } else {
                        Path rp = extLib.relativize(path);
                        Path nm = Paths.get(path.toFile().getName());
                        if (rp.equals(nm)) {
                            jars.add(path.toString());
                        }
                    }
                }
            });
        } catch (IOException ex) {
            LOG.log(Level.INFO, ex.getMessage());

        }
        return jars;
    }

    public static List addRootExtNotify_old(Project server) {
        final String jb = server.getProjectDirectory()
                .getFileObject(JettyConstants.JETTYBASE_FOLDER).getPath();

        final Path extLib = Paths.get(jb, "lib/ext");
        final List jars = new ArrayList<>();
        try {
            Stream<Path> stream = Files.walk(extLib);
            stream.forEach((path) -> {
                if (!path.equals(extLib)) {
                    if (Files.isDirectory(path)) {
                        jars.add(path.toString());
                    } else {
                        Path rp = extLib.relativize(path);
                        Path nm = Paths.get(path.toFile().getName());
                        if (rp.equals(nm)) {
                            jars.add(path.toString());
                        }
                    }
                }
            });
        } catch (IOException ex) {
            LOG.log(Level.INFO, ex.getMessage());
        }
        return jars;
    }
}
