/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jeeserver.base.embedded.webapp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.netbeans.modules.jeeserver.base.embedded.project.SuiteManager;
import org.netbeans.modules.jeeserver.base.embedded.project.nodes.SuiteNotifier;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteConstants;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Valery
 */
public class DistributedWebAppManager implements FileChangeListener {

    private static final Logger LOG = Logger.getLogger(DistributedWebAppManager.class.getName());

    public static final int SUCCESS = 0;

    public static final int ALREADY_EXISTS = 2;

    public static final int CREATE_FOLDER_ERROR = 4;

    public static final int NOT_FOUND = 6;

    public static final int CONTEXTPATH_NOT_FOUND = 6;

    private final Project serverInstance;

    protected DistributedWebAppManager(Project serverInstance) {
        this.serverInstance = serverInstance;
    }

    public static DistributedWebAppManager getInstance(Project instanceProject) {
        DistributedWebAppManager d = new DistributedWebAppManager(instanceProject);
        return d;
    }

    public Project getProject() {
        return serverInstance;
    }
    public boolean isRegistered(Project webApp) {
        List<FileObject> list = getWebAppFileObjects();
        if ( list.contains(webApp.getProjectDirectory())) {
            return true;
        } else {
            return false;
        }
    }
    public int register(Project webApp) {
        int result = SUCCESS;
        Path serverDir = Paths.get(serverInstance.getProjectDirectory().getPath());
        String root = serverDir.getRoot().toString().replaceAll(":", "_");
        if (root.startsWith("/")) {
            root = root.substring(1);
        }
        Path targetPath = serverDir.getRoot().relativize(serverDir);
        String tmp = System.getProperty("java.io.tmpdir");

        Path target = Paths.get(tmp, SuiteConstants.TMP_DIST_WEB_APPS, root, targetPath.toString());

        File file = target.toFile();
        if (!file.exists()) {
            try {
                FileUtil.createFolder(file);
            } catch (IOException ex) {
                result = CREATE_FOLDER_ERROR;
                LOG.log(Level.INFO, ex.getMessage());
            }
        }

        FileObject propsFo = FileUtil.toFileObject(target.toFile()).getFileObject(SuiteConstants.SERVER_INSTANCE_WEB_APPS_PROPS);
        Properties props = new Properties();
        if (propsFo != null) {
            props = BaseUtil.loadProperties(propsFo);
            try {
                propsFo.delete();
            } catch (IOException ex) {
                result = CREATE_FOLDER_ERROR;
                LOG.log(Level.INFO, ex.getMessage());
            }
        }

        WebModule wm = WebModule.getWebModule(webApp.getProjectDirectory());

        String cp = wm.getContextPath();

        if (cp != null) {
            props.setProperty(cp, webApp.getProjectDirectory().getPath());
            FileObject targetFo = FileUtil.toFileObject(target.toFile());
        } else {
            result = CONTEXTPATH_NOT_FOUND;
        }
        if ( result == SUCCESS) {
            BaseUtil.storeProperties(props, FileUtil.toFileObject(target.toFile()), SuiteConstants.SERVER_INSTANCE_WEB_APPS_PROPS );
            String uri = SuiteManager.getManager(serverInstance).getUri();
            SuiteNotifier sn = SuiteManager.getServerSuiteProject(uri).getLookup().lookup(SuiteNotifier.class);
            sn.childrenChanged(this, webApp);
            //sn.childrenChanged();
        }
        return result;
    }

    public int unregister(Project webApp) {
        int result = SUCCESS;
        Path serverDir = Paths.get(serverInstance.getProjectDirectory().getPath());
        String root = serverDir.getRoot().toString().replaceAll(":", "_");
        if (root.startsWith("/")) {
            root = root.substring(1);
        }
        Path targetPath = serverDir.getRoot().relativize(serverDir);
        String tmp = System.getProperty("java.io.tmpdir");

        Path target = Paths.get(tmp, SuiteConstants.TMP_DIST_WEB_APPS, root, targetPath.toString());

        File file = target.toFile();
        if (!file.exists()) {
            try {
                FileUtil.createFolder(file);
            } catch (IOException ex) {
                result = CREATE_FOLDER_ERROR;
                LOG.log(Level.INFO, ex.getMessage());
            }
        }

        FileObject propsFo = FileUtil.toFileObject(target.toFile()).getFileObject(SuiteConstants.SERVER_INSTANCE_WEB_APPS_PROPS);
        Properties props = new Properties();
        if (propsFo != null) {
            props = BaseUtil.loadProperties(propsFo);
            try {
                propsFo.delete();
            } catch (IOException ex) {
                result = CREATE_FOLDER_ERROR;
                LOG.log(Level.INFO, ex.getMessage());
            }
        }

        WebModule wm = WebModule.getWebModule(webApp.getProjectDirectory());

        String cp = wm.getContextPath();

        if (cp != null) {
            props.remove(cp);
            FileObject targetFo = FileUtil.toFileObject(target.toFile());
        } else {
            result = CONTEXTPATH_NOT_FOUND;
        }
        if ( result == SUCCESS) {
            BaseUtil.storeProperties(props, FileUtil.toFileObject(target.toFile()), SuiteConstants.SERVER_INSTANCE_WEB_APPS_PROPS );
            String uri = SuiteManager.getManager(serverInstance).getUri();
            SuiteNotifier sn = SuiteManager.getServerSuiteProject(uri).getLookup().lookup(SuiteNotifier.class);
            sn.childrenChanged(this, webApp);
            //sn.childrenChanged();
        }
        return result;

    }

    private FileObject getTmpWebAppsDir() {
        Path serverDir = Paths.get(serverInstance.getProjectDirectory().getPath());
        String root = serverDir.getRoot().toString().replaceAll(":", "_");
        if (root.startsWith("/")) {
            root = root.substring(1);
        }
        Path targetPath = serverDir.getRoot().relativize(serverDir);
        String tmp = System.getProperty("java.io.tmpdir");

        Path target = Paths.get(tmp, SuiteConstants.TMP_DIST_WEB_APPS, root, targetPath.toString());
        return FileUtil.toFileObject(target.toFile());
    }

    public List<FileObject> getWebAppFileObjects() {
        List<FileObject> list = new ArrayList<>();
        Properties props = getWebAppsProperties();
        props.forEach((k,v) -> {
            FileObject fo = FileUtil.toFileObject(new File((String) v));
            if ( fo != null ) {
                Project p = FileOwnerQuery.getOwner(fo);
                if ( p != null ) {
                    list.add(fo);
                }
            }
        });
        
        return list;
    }

    protected Properties getWebAppsProperties() {
        Properties props = new Properties();
        FileObject target = getTmpWebAppsDir();
        if (target == null) {
            return props;
        }
        FileObject propsFo = target.getFileObject(SuiteConstants.SERVER_INSTANCE_WEB_APPS_PROPS);
        if (propsFo == null) {
            return props;
        }
        props = BaseUtil.loadProperties(propsFo);
        return props;
    }

    /**
     *
     */
    public void refresh() {

        Properties props = getWebAppsProperties();
        props.forEach((k, v) -> {
            File f = new File((String) v);
            Project p = FileOwnerQuery.getOwner(FileUtil.toFileObject(f));
            WebModule wm = WebModule.getWebModule(p.getProjectDirectory());
            String cp = wm.getContextPath();
        });

    }

    public boolean exists(Project webApp) {
        boolean result = false;
        return result;
    }

    public FileObject findByContextpath(String cp) {
        FileObject result = null;

        return result;
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
    }

    @Override
    public void fileChanged(FileEvent fe) {
    }

    @Override
    public void fileDeleted(FileEvent fe) {
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) {
    }
}
