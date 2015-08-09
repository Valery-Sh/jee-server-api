
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jeeserver.jetty.project.nodes.libs;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static javax.swing.Action.NAME;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryChooser;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.netbeans.modules.jeeserver.jetty.deploy.JettyServerPlatformImpl;
import org.netbeans.modules.jeeserver.jetty.project.nodes.libs.LibrariesFileNode.FileKeys;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 * @author V. Shyshkin
 */
public class LibrariesAction extends AbstractAction implements ContextAwareAction {

    private static final Logger LOG = Logger.getLogger(LibrariesAction.class.getName());

    private static final RequestProcessor RP = new RequestProcessor(LibrariesAction.class);

    @Override
    public void actionPerformed(ActionEvent e) {
        assert false;
    }

    @Override
    public Action createContextAwareInstance(Lookup context) {
        assert false;
        return null;
    }

    public static Action createAddLibraryContextAwareInstance(Project serverProject, LibrariesFileNode node) {
        return new AddLibraryContextAction(serverProject, node);
    }

    public static Action createAddJarFolderContextAwareInstance(Project serverProject, LibrariesFileNode node) {

        return new AddJarFolderContextAction(serverProject, node);
    }

    public static final class AddLibraryContextAction extends AbstractAction {

        private final Project server;
        private final LibrariesFileNode node;

        public AddLibraryContextAction(Project server, LibrariesFileNode node) {
            this.server = server;
            this.node = node;
            putValue(NAME, "Add Library... ");

        }

        public @Override
        void actionPerformed(ActionEvent e) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    
                    Set<Library> libs = LibraryChooser.showDialog(LibraryManager.getDefault(), null, null);
                    if (libs != null) {
                        node.addLibraries(libs);
                        LibrariesFileNode ln = node;
                        if (node.isLibNode()) {
                            ln = node.findChildByKey(node.getExtFolder().getPath());
                        }

                        ((FileKeys) ln.getChildrenKeys()).addNotify();

                        BaseDeploymentManager manager = (BaseDeploymentManager) BaseUtils.managerOf(server);
                        JettyServerPlatformImpl platform = (JettyServerPlatformImpl) manager.getPlatform();
                        if (platform == null) {
                            platform = JettyServerPlatformImpl.getInstance(manager);
                        }
                        BaseUtils.out("addJarFolderAction CALL  NOTIFY " + System.currentTimeMillis());
                        platform.notifyLibrariesChanged();

                    }
                }
            });
        }

    }//class AddLibraryContextAction

    public static final class AddJarFolderContextAction extends AbstractAction {

        private final Project serverProject;
        private final LibrariesFileNode node;

        public AddJarFolderContextAction(final Project serverProject, LibrariesFileNode node) {
            this.serverProject = serverProject;
            this.node = node;

            putValue(NAME, "Add JAR/Folder... ");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    String jettyHome = BaseUtils.getServerProperties(serverProject).getHomeDir();

                    File basePath = new File(jettyHome);
                    File fc = new FileChooserBuilder("jetty.home")
                            .setAcceptAllFileFilterUsed(false)
                            .setTitle("Choose a folder or jar file")
                            .setDefaultWorkingDirectory(basePath)
                            .addFileFilter(new FileFilter() {
                                @Override
                                public boolean accept(File f) {
                                    String name = f.getName().toLowerCase();
                                    return name.endsWith(".jar") || f.isDirectory();
                                }

                                @Override
                                public String getDescription() {
                                    return "Classpath Entry (folder or jar file)";
                                }
                            })
                            .setApproveText("Open").showOpenDialog();
                    //if ( fc == null  
                    LibUtil.updateLibraries(serverProject);
                    ((FileKeys) node.getChildrenKeys()).addNotify();
                }
            });
        }
    }//class

    public static final class RemoveFileContextAction extends AbstractAction {

        private final Project serverProject;
        private final LibrariesFileNode node;
        private final File target;

        public RemoveFileContextAction(final Project serverProject, LibrariesFileNode node) {
            this.serverProject = serverProject;
            this.node = node;
            target = new File(node.getKey().toString());
            String value = target.isDirectory() ? "Folder" : "File";
            putValue(NAME, "Remove " + value);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        Node pnode = node.getParentNode();
                        LibrariesFileNode parentNode = null;
                        if (pnode instanceof LibrariesFileNode) {
                            parentNode = (LibrariesFileNode) pnode;
                        }

                        FileUtil.toFileObject(target).delete();

                        if (parentNode != null) {
                            FileKeys keys = (FileKeys) parentNode.getChildrenKeys();
                            if (keys != null) {
                                keys.addNotify();
                            }
                        }
                        BaseDeploymentManager manager = (BaseDeploymentManager) BaseUtils.managerOf(serverProject);
                        JettyServerPlatformImpl platform = (JettyServerPlatformImpl) manager.getPlatform();
                        platform = JettyServerPlatformImpl.getInstance(manager);
                        platform.notifyLibrariesChanged();

                    } catch (IOException ex) {
                        LOG.log(Level.INFO, ex.getMessage());
                    }
                }
            }
            );
        }
    }//class

}
