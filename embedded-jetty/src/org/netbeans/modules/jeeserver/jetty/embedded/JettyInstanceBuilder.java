package org.netbeans.modules.jeeserver.jetty.embedded;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.jeeserver.base.deployment.specifics.InstanceBuilder;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.netbeans.modules.jeeserver.base.embedded.project.wizard.ServerInstanceAntBuildExtender;
import org.netbeans.modules.jeeserver.base.embedded.EmbeddedInstanceBuilder;
import org.netbeans.modules.jeeserver.base.embedded.project.wizard.ServerInstanceBuildExtender;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteConstants;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

/**
 *
 * @author V. Shyshkin
 */
public class JettyInstanceBuilder extends EmbeddedInstanceBuilder {

    private static final Logger LOG = Logger.getLogger(JettyInstanceBuilder.class.getName());

    @StaticResource
    public static final String zipAntTemplatePath = "org/netbeans/modules/jeeserver/jetty/embedded/resources/JettyEmbeddedAntTemplate.zip";//JettyServerInstanceProject.zip";    

    public JettyInstanceBuilder(Properties configProps, InstanceBuilder.Options options) {
        super(configProps, options);
    }

    @Override
    public Set instantiate() {
        Set set = new HashSet();
        try {
            if (getOptions().equals(InstanceBuilder.Options.NEW)) {
                instantiateProjectDir(set);
            } else {
                File dirF = FileUtil.normalizeFile((File) getWizardDescriptor().getProperty("projdir"));
//                String name = (String) getWizardDescriptor().getProperty("name");                
//                dirF = new File( dirF.getPath() + "/" + name);
                FileObject dir = FileUtil.toFileObject(dirF);
                Project p = ProjectManager.getDefault().findProject(dir);
                set.add(p);
                createOrUpdateNbDeployment(set);
            }
            instantiateServerProperties(set);
            instantiateServerInstanceDir(set);

            ServerInstanceBuildExtender extender;
            if (!isMavenbased()) {
                extender = new ServerInstanceAntBuildExtender(findProject(set));
            } else {
                extender = new ServerInstanceBuildExtender(findProject(set));
            }
            extender.enableExtender();

            modifyClasspath(set);

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return set;
    }

    @Override
    public InputStream getZipTemplateInputStream() {
        return getClass().getClassLoader().getResourceAsStream("/"
                + zipAntTemplatePath);
    }

    protected FileObject getSrcDir(Project p) {
        FileObject fo = p.getProjectDirectory().getFileObject("src");
        return fo;
    }

    @Override
    protected FileObject getLibDir(Project p) {
        FileObject fo;
        fo = p.getProjectDirectory().getFileObject(SuiteConstants.ANT_LIB_PATH + "/ext");
        return fo;
    }

    @Override
    public void createOrUpdateNbDeployment(Set result) {
        Project proj = findProject(result);

        String classpackage = (String) getWizardDescriptor()
                .getProperty("package");

        FileObject libFolder = getLibDir(proj);

        if (libFolder == null) {
            libFolder = createLib(proj);
        } else {
            return;
        }

        addCommandManagerJar(libFolder);

        try {
            copyBuildXml(libFolder);
        } catch (IOException ex) {
            LOG.log(Level.INFO, ex.getMessage()); //NOI18N
        }

        modifyPomXml(proj);
        updateServerInstanceProperties(proj);
        //
        // Plugin jar => we can create a class from template
        //
        DataObject template;
        DataFolder outputFolder;

        Map<String, Object> templateParams = new HashMap<>(1);
        try {

            FileObject srcFo = getSrcDir(proj);

            FileObject toDelete = srcFo.getFileObject("javaapplication0");
            if (toDelete != null) {
                toDelete.delete();
            }
            FileObject targetFo = srcFo;

            if (classpackage != null) {
                String path = classpackage.replace(".", "/");
                targetFo = FileUtil.createFolder(targetFo, path);

            } else {
                classpackage = "org.embedded.server";
                targetFo = srcFo.createFolder("org")
                        .createFolder("embedded")
                        .createFolder("server");
            }

            outputFolder = DataFolder.findFolder(targetFo);

            template = DataObject.find(
                    FileUtil.getConfigFile("Templates/jetty9/JettyEmbeddedServer"));
            templateParams.put("port", getWizardDescriptor().getProperty(BaseConstants.HTTP_PORT_PROP));
            templateParams.put("comStart", "");
            templateParams.put("comEnd", "");
            templateParams.put("classpackage", classpackage);
            templateParams.put("command.manager.param", getCommandManagerJarTemplateName());

            template.createFromTemplate(
                    outputFolder,
                    "JettyEmbeddedServer.java",
                    templateParams);
            //setMainClass(projectDir);
        } catch (IOException ex) {
            LOG.log(Level.INFO, ex.getMessage()); //NOI18N
        }
    }

    protected void addCommandManagerJar(FileObject targetFolder) {
        BaseUtil.out("1 JettyInstanceBulder addCommandManagerJar targetFo=" + targetFolder);                    
        String cmJarPath = getCommandManagerJarTemplateName();
        
        if (cmJarPath == null || targetFolder.getFileObject(cmJarPath + ".jar") != null) {
            return;
        }

        String cmIn = "/org/netbeans/modules/jeeserver/jetty/embedded/resources/" + cmJarPath + ".jar";
        BaseUtil.out("2 JettyInstanceBulder addCommandManagerJar targetFo=" + targetFolder);            
        FileObject cmFo;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(cmIn)) {
            //cmFo = targetFolder.createData(cmJarPath, "jar");
            Path targetPath = Paths.get(targetFolder.getPath(), cmJarPath + ".jar");
            BaseUtil.out("JettyInstanceBulder addCommandManagerJar targetPath=" + targetPath);            
//            try (OutputStream os = cmFo.getOutputStream(); InputStream is = getClass().getClassLoader().getResourceAsStream(cmIn)) {
            //FileUtil.copy(is, os); // Not close files sometimes
            Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);
            cmFo = FileUtil.toFileObject(targetPath.toFile());

        } catch (IOException ex) {
            BaseUtil.out("JettyInstanceBulder addCommandManagerJar EXCEPTION ex=" + ex.getMessage());
            LOG.log(Level.INFO, ex.getMessage()); //NOI18N
            return;
        }
        Project proj = FileOwnerQuery.getOwner(targetFolder);

        FileLock lock;
        try {
            lock = cmFo.lock();
        } catch (IOException ex) {
            BaseUtil.out("Try again later; perhaps display a warning dialog");
            return;
        }
        String newName = cmFo.getName() + "-"
                + getCommandManagerVersion(proj);
        BaseUtil.out("JettyInstanceBulder addCommandManagerJar newName=" + newName);
        try {
            cmFo.rename(lock, newName, "jar");
        } catch (IOException ex) {
            LOG.log(Level.INFO, ex.getMessage()); //NOI18N
        } finally {
            // Always put this in a finally block!
            lock.releaseLock();
        }
        BaseUtil.out("JettyInstanceBulder addCommandManagerJar cmFo=" + cmFo);

    }

    protected Properties getPomProperties(Project project) {
        FileObject jarFo = getLibDir(project).getFileObject(getCommandManagerJarTemplateName() + ".jar");
        if (jarFo == null) {
            return null;
        }
        Properties props = BaseUtil.getPomProperties(jarFo);

        return props;
    }

    protected String getCommandManagerVersion(Project project) {
        String v = null;
        Properties p = getPomProperties(project);
        if (p == null || p.getProperty("version") == null) {
            return "1.0.0";
        }
        return p.getProperty("version");

    }

    /**
     *
     * @param project
     */
    protected void modifyPomXml(Project project) {
    }

    public FileObject createLib(Project project) {
        FileObject libFo = null;

        File libFolder;
        FileObject fo;
        libFolder = new File(project.getProjectDirectory().getPath() + "/lib/ext");
        fo = project.getProjectDirectory().getFileObject("lib/ext");

        if (fo == null) {
            try {
                libFo = FileUtil.createFolder(libFolder);
            } catch (IOException ex) {
                LOG.log(Level.INFO, ex.getMessage()); //NOI18N
            }
        }
        return libFo;
    }

    @Override
    public void removeCommandManager(Project proj) {
//        String actualServerId = (String) getWizardDescriptor()
//                .getProperty(SuiteConstants.SERVER_ACTUAL_ID_PROP);
//        String cm = actualServerId + Jetty9Specifics.JETTY_JAR_POSTFIX;
        String cm = getCommandManagerJarTemplateName();
        FileObject libExt = getLibDir(proj);
        if (libExt != null) {
            FileObject cmFo = libExt.getFileObject(cm);
            if (cmFo != null) {
                try {
                    cmFo.delete();
                } catch (IOException ex) {
                    LOG.log(Level.INFO, ex.getMessage()); //NOI18N
                }
            }
        }

    }

    @Override
    protected String getCommandManagerJarTemplateName() {
        String actualServerId = (String) getWizardDescriptor()
                .getProperty(SuiteConstants.SERVER_ACTUAL_ID_PROP);
        return actualServerId + BaseConstants.COMMAND_MANAGER_JAR_POSTFIX;
    }
}
