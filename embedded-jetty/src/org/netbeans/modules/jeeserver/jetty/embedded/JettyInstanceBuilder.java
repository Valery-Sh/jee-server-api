package org.netbeans.modules.jeeserver.jetty.embedded;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.ClassPath.Entry;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.jeeserver.base.deployment.specifics.InstanceBuilder;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.netbeans.modules.jeeserver.base.deployment.utils.LibrariesFileLocator;
import org.netbeans.modules.jeeserver.base.embedded.project.wizard.ServerInstanceAntBuildExtender;
import org.netbeans.modules.jeeserver.base.embedded.EmbeddedInstanceBuilder;
import org.netbeans.modules.jeeserver.base.embedded.project.wizard.ServerInstanceBuildExtender;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteConstants;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

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
        BaseUtil.out("JettyInstanceBuilder srcDir fo=" + fo);
//        if ( ! BaseUtil.isAntProject(p)) {
//            fo = p.getProjectDirectory().getFileObject("src/main/java");
//        }
        return fo;
    }

    @Override
    protected FileObject getLibDir(Project p) {
        FileObject fo;
//        if (BaseUtil.isAntProject(p)) {
        fo = p.getProjectDirectory().getFileObject("lib/ext");
//        } else {
//            fo = p.getProjectDirectory().getFileObject("nbdeployment/lib");
//        }
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
            templateParams.put("command.manager.param", getCommandManagerJarName());

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
        String cmOut = getCommandManagerJarName();
        if ( cmOut == null || targetFolder.getFileObject(cmOut + ".jar") != null ) {
            return;
        }
        
        String cmIn = "/org/netbeans/modules/jeeserver/jetty/embedded/resources/" + cmOut + ".jar";

        FileObject cmFo;
        try {
            cmFo = targetFolder.createData(cmOut, "jar");
            try (OutputStream os = cmFo.getOutputStream(); InputStream is = getClass().getClassLoader().getResourceAsStream(cmIn)) {
                FileUtil.copy(is, os);
            }
        } catch (IOException ex) {
            LOG.log(Level.INFO, ex.getMessage()); //NOI18N
        }
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
        BaseUtil.out("JettyInstanceBuilder createLib libfo=" + libFo);
        return libFo;
    }

    @Override
    public void removeCommandManager(Project proj) {
//        String actualServerId = (String) getWizardDescriptor()
//                .getProperty(SuiteConstants.SERVER_ACTUAL_ID_PROP);
//        String cm = actualServerId + Jetty9Specifics.JETTY_JAR_POSTFIX;
        String cm = getCommandManagerJarName();
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
    protected String getCommandManagerJarName() {
        String actualServerId = (String) getWizardDescriptor()
                .getProperty(SuiteConstants.SERVER_ACTUAL_ID_PROP);
        return actualServerId + Jetty9Specifics.JETTY_JAR_POSTFIX;
    }
}
