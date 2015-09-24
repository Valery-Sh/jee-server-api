package org.netbeans.modules.jeeserver.base.embedded.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.deployment.actions.CommandActionProgress;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.netbeans.modules.jeeserver.base.deployment.utils.Copier;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.windows.InputOutput;


/**
 * Provides a set of static methods used mainly to implement packaging actions.
 *
 * @see org.netbeans.modules.embedded.actions.PackageMainAction
 *
 * @author V. Shyshkin
 */
public class EmbPackageUtils {

    private static final Logger LOG = Logger.getLogger(EmbPackageUtils.class.getName());


    /**
     *
     * @param str
     * @param fo
     * @throws IOException
     */
    private static void writeFile(ZipInputStream str, FileObject fo) throws IOException {
        try (OutputStream out = fo.getOutputStream()) {
            FileUtil.copy(str, out);
        }
    }

    /**
     * Deletes the directory named {@literal package-dist} used as a package
     * distribution. the {@literal package-dist} directory is a child of the
     * server project root directory.
     *
     * @param serverProject
     */
/*    public static void deletePackageDist(Project serverProject) {

        final FileObject dir = serverProject.getProjectDirectory().getFileObject(SuiteConstants.PACKAGE_DIST);
        if (dir == null) {
            return;
        }
        FileUtil.runAtomicAction(new Runnable() {
            @Override
            public void run() {
                try {
                    dir.delete();
                } catch (IOException ex) {
                    LOG.log(Level.INFO, ex.getMessage());
                } finally {
                }
            }
        });
    }
*/
    /**
     * Deletes the directory named {@literal buid} of the specified Ant-based
     * web project.
     *
     * @param webProject a web project whose {@literal build) directory is to be deleted.
     * }
     */
    private static void deleteBuildDir(Project webProject) {

        final FileObject dir = webProject.getProjectDirectory().getFileObject("build");
        if (dir == null) {
            return;
        }

        FileUtil.runAtomicAction(new Runnable() {
            @Override
            public void run() {
                try {
                    dir.delete();
                } catch (IOException ex) {
                    LOG.log(Level.INFO, ex.getMessage());
                } finally {
                }
            }
        });
    }

    /**
     * Unpack the specified war and puts it's content to the specified folder.
     *
     * @param source input stream corresponding to the war archive
     * @param destRoot the destination folder for the war archive content.
     * @return
     */
    private static boolean unwar(final InputStream source, final FileObject destRoot) {
        boolean result = true;
        try {
            ZipInputStream str = new ZipInputStream(source);
            ZipEntry entry;
            while ((entry = str.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    FileUtil.createFolder(destRoot, entry.getName());
                } else {
                    FileObject fo = FileUtil.createData(destRoot, entry.getName());
                    writeFile(str, fo);
                }
            }
        } catch (IOException ex) {
            LOG.log(Level.INFO, ex.getMessage());
            result = false;
        } finally {
            try {
                source.close();
            } catch (IOException ex) {
                LOG.log(Level.INFO, ex.getMessage());
                result = false;
            }
        }
        return result;
    }

    public static Properties createHtml5Properties(Project html5Proj) {
        
        FileObject fo = html5Proj.getProjectDirectory();
        Properties html5Props = BaseUtils.loadHtml5ProjectProperties(fo.getPath());

        String siteRoot = BaseUtils.resolve(SuiteConstants.HTML5_SITE_ROOT_PROP, html5Props);
        if (siteRoot == null) {
            siteRoot = BaseConstants.HTML5_DEFAULT_SITE_ROOT_PROP;
        }
        Properties props = new Properties();
        String contextPath = BaseUtils.resolve(BaseConstants.HTML5_WEB_CONTEXT_ROOT_PROP, html5Props);
        
        if (contextPath == null) {
            contextPath = fo.getNameExt();
        }

        props.setProperty(BaseConstants.CONTEXTPATH_PROP, contextPath);
        props.setProperty(BaseConstants.HTML5_SITE_ROOT_PROP, siteRoot);
        

        return props;

    }
    public static void html5ProjectToWar(Project html5Proj, FileObject serverDir, String webappsFolderName,InputOutput io) {

        try {
            String web_apps_pack = webappsFolderName;
            
            String web_app_config = SuiteConstants.WEBAPP_CONFIG_FILE;
            FileObject package_dist = serverDir.getFileObject(SuiteConstants.PACKAGE_DIST);
            File serverJar = Paths.get(package_dist.getPath(),
                    serverDir.getNameExt() + ".jar").toFile();

            FileObject fo = html5Proj.getProjectDirectory();
            io.getOut().println("Package Html5 project " + fo.getNameExt() + ". " + new Date());
            Properties props = createHtml5Properties(html5Proj);
            
            File siteRoot = FileUtil.toFile(fo.getFileObject(props.getProperty(SuiteConstants.HTML5_SITE_ROOT_PROP)));
            
            File webappsFolder = Paths.get(package_dist.getPath(),web_apps_pack).toFile();
            
            //Copier copier = new Copier(siteRoot, io);
            
            File warFile = new File( webappsFolder.getPath() + "/" + fo.getNameExt() +".war");
            //File target = copier.copyTo(webappsFolder, fo.getNameExt());
            //Path meta_inf = Files.createDirectories(target.toPath().resolve("META-INF"));
            Copier.ZipUtil.copy(siteRoot, warFile);

            BaseUtils.storeProperties(props, package_dist, web_app_config);
            File tmpFile = new File(package_dist.getPath() + "/" + web_app_config);

            Copier.ZipUtil.copy(tmpFile, warFile, "WEB-INF");

            tmpFile.delete();

            io.getOut().println(" --- Copy  config file. " + new Date());

        } catch (Exception ex) {
            LOG.log(Level.INFO, ex.getMessage());
            io.getOut().println(" --- Copy  EXCEPTION. " + ex.getMessage() + ". " + new Date());

        }
    }
    
    public static void html5ProjectToFolder(Project html5Proj, FileObject serverDir, String webappsFolderName,InputOutput io) {

        try {
            String web_apps_pack = webappsFolderName;
            
            String web_app_config = SuiteConstants.WEBAPP_CONFIG_FILE;
            FileObject package_dist = serverDir.getFileObject(SuiteConstants.PACKAGE_DIST);
            File serverJar = Paths.get(package_dist.getPath(),
                    serverDir.getNameExt() + ".jar").toFile();

            FileObject fo = html5Proj.getProjectDirectory();
            io.getOut().println("Package Html5 project " + fo.getNameExt() + ". " + new Date());
            Properties props = createHtml5Properties(html5Proj);
            
            File siteRoot = FileUtil.toFile(fo.getFileObject(props.getProperty(SuiteConstants.HTML5_SITE_ROOT_PROP)));
            
            File webappsFolder = Paths.get(package_dist.getPath(),web_apps_pack).toFile();
            Copier copier = new Copier(siteRoot, io);
            File target = copier.copyTo(webappsFolder, fo.getNameExt());
            Path meta_inf = Files.createDirectories(target.toPath().resolve("META-INF"));
//            ZipUtil.copyToZip(siteRoot, serverJar, web_apps_pack + "/" + fo.getNameExt());

            BaseUtils.storeProperties(props, FileUtil.toFileObject(meta_inf.toFile()), web_app_config);
//            File tmpFile = new File(package_dist.getPath() + "/" + web_app_config);

//            ZipUtil.copyToZip(tmpFile, serverJar, web_apps_pack + "/" + fo.getNameExt() + "/META-INF");

//            tmpFile.delete();

            io.getOut().println(" --- Copy  config file. " + new Date());

        } catch (Exception ex) {
            LOG.log(Level.INFO, ex.getMessage());
            io.getOut().println(" --- Copy  EXCEPTION. " + ex.getMessage() + ". " + new Date());

        }
    }

    public static void html5ProjectToServerJar(Project html5Proj, FileObject serverDir, InputOutput io) {

        try {
            String web_apps_pack = SuiteConstants.WEB_APPS_PACK;
            String web_app_config = SuiteConstants.WEBAPP_CONFIG_FILE;
            FileObject package_dist = serverDir.getFileObject(SuiteConstants.PACKAGE_DIST);
            File serverJar = Paths.get(package_dist.getPath(),
                    serverDir.getNameExt() + ".jar").toFile();

            FileObject fo = html5Proj.getProjectDirectory();
            io.getOut().println("Package Html5 project " + fo.getNameExt() + ". " + new Date());
            Properties props = createHtml5Properties(html5Proj);
            
            File siteRoot = FileUtil.toFile(fo.getFileObject(props.getProperty(SuiteConstants.HTML5_SITE_ROOT_PROP)));
            Copier.ZipUtil.copy(siteRoot, serverJar, web_apps_pack + "/" + fo.getNameExt());

            BaseUtils.storeProperties(props, package_dist, web_app_config);
            File tmpFile = new File(package_dist.getPath() + "/" + web_app_config);

            Copier.ZipUtil.copy(tmpFile, serverJar, web_apps_pack + "/" + fo.getNameExt() + "/META-INF");

            tmpFile.delete();

            io.getOut().println(" --- Copy  config file. " + new Date());

        } catch (Exception ex) {
            LOG.log(Level.INFO, ex.getMessage());
            io.getOut().println(" --- Copy  EXCEPTION. " + ex.getMessage() + ". " + new Date());

        }
    }

    /**
     * Unpack the specified war and places it's content into the specified
     * folder.
     *
     * @param war a file object corresponding to the war archive
     * @param destDir
     * @return
     */
    /**
     * Recreate the specified directory. Deletes the directory and then create
     * the new one.
     *
     * @param destDir the directory to be recreated
     * @return the new (recreated) directory
     */
    public static FileObject recreateDir(File destDir) {

        if (destDir.exists()) {
            deleteDir(FileUtil.toFileObject(destDir));
        }

        FileObject result = createDir(destDir);
        return result;
    }

    /**
     * Creates a new directory for the specified file.
     *
     * @param dir the file used to create a new directory
     * @return an object of type {@literal FileObject} with represents a newly
     * created directory
     */
    public static FileObject createDir(final File dir) {
        FileObject result = null;
        try {
            result = FileUtil.createFolder(dir);
        } catch (IOException ex) {
            LOG.log(Level.INFO, ex.getMessage());
        }
        return result;
    }

    /**
     * Deletes the specified directory.
     *
     * @param dir
     */
    public static void deleteDir(final FileObject dir) {
        FileLock lock = null;
        try {
            dir.lock();
            dir.delete(lock);
        } catch (IOException ex) {
            LOG.log(Level.INFO, ex.getMessage());
        } finally {
            if (lock != null) {
                lock.releaseLock();
            }
        }
    }

    /**
     * Rebuilds all web projects registered in the embedded server.
     *
     * @param serverProject embedded server project
     * @param io an object of type {@literal InputOutput} to notify user
     * @return a list of web projects which cannot be rebuild for some reason.
     */
    public static List<String> rebuildWebProjects(Project serverProject, InputOutput io) {

        List<String> list = new ArrayList<>();
        List<Project> projects = getWebProjects(serverProject);
        for (Project webapp : projects) {
            //String projName = p.getProjectDirectory().getName();
            //CommandActionProgress actionProgress = rebuildWebProject(p);

            CommandActionProgress.invokeAction(webapp, ActionProvider.COMMAND_BUILD);
        }
        return list;
    }

/*    private static CommandActionProgress rebuildWebProject(Project webapp) {
        return CommandActionProgress.invokeAction(webapp, ActionProvider.COMMAND_BUILD);
    }
*/
    /**
     * Deletes {@literal build} directory of all registered web projects.
     *
     * @param serverProject an embedded server project
     */
    public static void deleteWebBuildDirs(Project serverProject) {
        List<Project> webprojects = getWebProjects(serverProject);
        for (Project p : webprojects) {
            deleteBuildDir(p);
        }

    }

    /**
     * Check whether web project references are valid. If a web project
     * registered in the embedded server with a help of {@literal .webref} file
     * then the file must contain a property named (@code webAppLocation}. The
     * value of the property must point to an existing web project.<p>
     * A war archive file can be registered in the embedded server with a help
     * of {@literal .warref} file. Then the property (@code webAppLocation}
     * value must point to an existing war file.<p>
     *
     * @param serverProject an embedded server project
     * @return a list of file names which contains invalid references
     */
    public static List<String> vaidateWebProjects(Project serverProject) {

        List<String> list = new ArrayList<>();

        FileObject appsFo = serverProject.getProjectDirectory().getFileObject(SuiteConstants.REG_WEB_APPS_FOLDER);
        for (FileObject fo : appsFo.getChildren()) {
            String projName = fo.getName();
            String msg;
            if (SuiteConstants.WEB_REF.equals(fo.getExt())) {
                msg = validateWebRef(fo);
            } else if (SuiteConstants.WAR_REF.equals(fo.getExt())) {
                msg = validateWarRef(fo);
            } else if (SuiteConstants.HTML_REF.equals(fo.getExt())) {
                msg = validateHtmRef(fo);
            } else if (fo.isFolder()) {
                msg = validateWebProject(fo);
            } else {
                continue;
            }

            if (msg != null) {
                list.add(projName + msg);
            }

        }
        return list;
    }

    /**
     * Returns a war archive file for the specified web project directory.
     *
     * @param webProjDir a web project directory where the war archive must
     * reside.
     *
     * @return a war archive file object or {@literal null} if the war file
     * cannot be found.
     */
    public static FileObject getWarFile(FileObject webProjDir) {
        FileObject war = null;
        if (webProjDir.isFolder()) {
            war = BaseUtils.getWar(FileOwnerQuery.getOwner(webProjDir));
        }
        return war;
    }

    /**
     * Returns a list of all war files for all registered web projects and
     * registered war archives ({@literal warref}.
     *
     * @param serverProject an embedded server project
     *
     * @return a list of names of web projects for which the
     * {@literal war-archive} is not found
     *
     */
    public static List<FileObject> getWarFiles(Project serverProject) {
        List<FileObject> list = new ArrayList<>();
        FileObject webapps = serverProject.getProjectDirectory().getFileObject(SuiteConstants.REG_WEB_APPS_FOLDER);
        for (FileObject fo : webapps.getChildren()) {
            FileObject war = getWarFile(fo);
            if (war != null) {
                list.add(war);
                continue;
            }
            if (!fo.isFolder() && SuiteConstants.WEB_REF.equals(fo.getExt())) {
                Properties props = BaseUtils.loadProperties(fo);
                String location = props.getProperty(SuiteConstants.WEB_APP_LOCATION_PROP);
                war = getWarFile(FileUtil.toFileObject(new File(location)));
                if (war != null) {
                    list.add(war);
                }
                continue;
            }

            if (!fo.isFolder() && SuiteConstants.WAR_REF.equals(fo.getExt())) {
                Properties props = BaseUtils.loadProperties(fo);
                String location = props.getProperty(SuiteConstants.WEB_APP_LOCATION_PROP);
                if (new File(location).exists()) {
                    war = FileUtil.toFileObject(new File(location));
                    if (war != null) {
                        list.add(war);
                    }
                }
            }
        }
        return list;
    }

    /**
     * Returns a list of all web projects registered in the server project. The
     * list includes all "inner" web projects and projects referenced by
     * {@literal .webref}
     *
     * @param serverProject an embedded server project
     * @return a list of all registered web projects
     */
    public static List<Project> getWebProjects(Project serverProject) {
        List<Project> list = new ArrayList<>();

        FileObject appsFo = serverProject.getProjectDirectory().getFileObject(SuiteConstants.REG_WEB_APPS_FOLDER);
        for (FileObject fo : appsFo.getChildren()) {
            if (fo.isFolder()) {
                list.add(FileOwnerQuery.getOwner(fo));
            }
            if (SuiteConstants.WEB_REF.equals(fo.getExt())) {
                Properties props = BaseUtils.loadProperties(fo);
                String location = props.getProperty(SuiteConstants.WEB_APP_LOCATION_PROP);
                FileObject webref = FileUtil.toFileObject(new File(location));
                list.add(FileOwnerQuery.getOwner(webref));
            }
        }
        return list;
    }

    /**
     * Returns a list of all web projects registered in the server project. The
     * list includes all "inner" web projects and projects referenced by
     * {@literal .webref}
     *
     * @param serverProject an embedded server project
     * @return a list of all registered web projects
     */
    public static List<Project> getHtml5Projects(Project serverProject) {
        List<Project> list = new ArrayList<>();

        FileObject appsFo = serverProject.getProjectDirectory().getFileObject(SuiteConstants.REG_WEB_APPS_FOLDER);
        for (FileObject fo : appsFo.getChildren()) {
            if (SuiteConstants.HTML_REF.equals(fo.getExt())) {
                Properties props = BaseUtils.loadProperties(fo);
                String location = props.getProperty(SuiteConstants.WEB_APP_LOCATION_PROP);
                FileObject ref = FileUtil.toFileObject(new File(location));
                list.add(FileOwnerQuery.getOwner(ref));
            }
        }
        return list;
    }
    /**
     * Checks whether the specified file objects references a valid web project.
     *
     * @param webRef a file objects that represents a file with
     * {@literal webref} extention
     * @return {
     * @null} if references is valid. Error message otherwise
     */
    public static String validateWebRef(FileObject webRef) {
        String msg;
        Properties props = BaseUtils.loadProperties(webRef);
        String location = props.getProperty(SuiteConstants.WEB_APP_LOCATION_PROP);
        String msg1 = ".webref refers to ";
        File file = new File(location);
        if (location == null || !file.exists()) {
            msg = msg1 + " not existing project.";
        } else {
            FileObject fo = FileUtil.toFileObject(file);
            if (fo.getFileObject("nbproject/project.xml") == null
                    && fo.getFileObject("pom.xml") == null) {
                msg = msg1 + " not a project directory";
            } else {
                msg = validateWebProject(fo);
            }
        }
        return msg;
    }

    /**
     * Checks whether the specified file objects references a valid war-archive
     * file.
     *
     * @param warRef a file objects that represents a file with
     * {@literal warref} extention
     * @return {
     * @null} if references is valid. Error message otherwise
     */
    public static String validateWarRef(FileObject warRef) {
        String msg = null;
        Properties props = BaseUtils.loadProperties(warRef);
        String location = props.getProperty(SuiteConstants.WEB_APP_LOCATION_PROP);
        String msg1 = ".warref refers to ";
        File file = new File(location);
        if (location == null || !file.exists()) {
            msg = msg1 + " not existing war file.";
        }
        return msg;
    }

    /**
     * Checks whether the specified file objects references a valid war-archive
     * file.
     *
     * @param ref a file objects that represents a file with {@literal warref}
     * extention
     * @return {
     * @null} if references is valid. Error message otherwise
     */
    public static String validateHtmRef(FileObject ref) {
        String msg = null;
        Properties props = BaseUtils.loadProperties(ref);
        String location = props.getProperty(SuiteConstants.WEB_APP_LOCATION_PROP);
        String msg1 = ".htmref refers to ";
        File file = new File(location);
        FileObject fo = FileUtil.toFileObject(file);
        if (fo == null) {
            msg = msg1 + " not existing Html5 project.";
        } else if (fo.getFileObject("nbproject/project.xml") == null) {
            msg = msg1 + " not existing Html5 project.";
        } else {
            String type = BaseUtils.projectTypeByProjectXml(fo.getFileObject("nbproject/project.xml"));
            if (!SuiteConstants.HTML5_PROJECTTYPE.equals(type)) {
                msg = msg1 + " not an Html5 project.";
            }
        }

        return msg;
    }

    /**
     * Checks whether the specified file object represents a valid web project.
     * The project must contain {@literal META-INF/context.properties}
     * configuration file.
     *
     * @param projDir a web project directory to be checked
     * @return {
     * @null} if success. An error message otherwise
     */
    public static String validateWebProject(FileObject projDir) {
        String msg = null;
        return msg;
    }
}
