package org.netbeans.modules.jeeserver.jetty.embedded;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
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
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.netbeans.modules.jeeserver.base.deployment.utils.LibrariesFileLocator;
import org.netbeans.modules.jeeserver.base.embedded.server.project.wizards.ServerInstanceAntBuildExtender;
import org.netbeans.modules.jeeserver.base.embedded.server.project.wizards.EmbeddedInstanceBuilder;
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
                copyCommandManagerLib(set);
            }
            instantiateServerProperties(set);
            instantiateServerInstanceDir(set);
            ServerInstanceAntBuildExtender extender = new ServerInstanceAntBuildExtender(findProject(set));
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

    private Project findProject(Set result) {
        Project p = null;
        for (Object o : result) {
            if (o instanceof Project) {
                p = (Project) o;
            }
        }
        return p;
    }

    protected FileObject getSrcDir(Project p) {
        return p.getProjectDirectory().getFileObject("src");
    }

    protected FileObject getLibDir(Project p) {
        return p.getProjectDirectory().getFileObject("lib/ext");
    }

    @Override
    public void copyCommandManagerLib(Set result) {
        Project proj = findProject(result);

        String actualServerId = (String) getWizardDescriptor()
                .getProperty(SuiteConstants.SERVER_ACTUAL_ID_PROP);
        String cmOut = actualServerId + "-command-manager";
        String cmIn = "/org/netbeans/modules/jeeserver/jetty/embedded/resources/" + actualServerId + "-command-manager.jar";

        FileObject libExt = getLibDir(proj);
        if (libExt == null) {
            libExt = createLib(result);
        }
        FileObject cmFo;// = null;
        try {
            cmFo = libExt.createData(cmOut, "jar");
            try (OutputStream os = cmFo.getOutputStream(); InputStream is = getClass().getClassLoader().getResourceAsStream(cmIn)) {
                FileUtil.copy(is, os);
            }

        } catch (IOException ex) {
            LOG.log(Level.INFO, ex.getMessage()); //NOI18N
        }

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
            FileObject targetFo = srcFo.createFolder("org")
                    .createFolder("embedded")
                    .createFolder("server");
            outputFolder = DataFolder.findFolder(targetFo);
            template = DataObject.find(
                    FileUtil.getConfigFile("Templates/jetty9/JettyEmbeddedServer"));
            templateParams.put("port", getWizardDescriptor().getProperty(BaseConstants.HTTP_PORT_PROP));
            templateParams.put("comStart", "");
            templateParams.put("comEnd", "");

            template.createFromTemplate(
                    outputFolder,
                    "JettyEmbeddedServer.java",
                    templateParams);
            //setMainClass(projectDir);
        } catch (IOException ex) {
            LOG.log(Level.INFO, ex.getMessage()); //NOI18N
        }
    }

    public FileObject createLib(Set result) {
        FileObject libFo = null;
        Project p = findProject(result);
        File libFolder = new File(p.getProjectDirectory().getPath() + "/lib/ext");
        FileObject fo = p.getProjectDirectory().getFileObject("lib/ext");
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
        String actualServerId = (String) getWizardDescriptor()
                .getProperty(SuiteConstants.SERVER_ACTUAL_ID_PROP);
        String cm = actualServerId + "-command-manager.jar";
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

    protected String getCommandManagerJarName() {
        String actualServerId = (String) getWizardDescriptor()
                .getProperty(SuiteConstants.SERVER_ACTUAL_ID_PROP);
        return actualServerId + "-command-manager.jar";

    }
    /*    public void modifyClasspath(Set result) {
     Project p = findProject(result);
     FileObject libExt = getLibDir(p);
     if (libExt == null) {
     return;
     }        
     String jarName = getCommandManagerJarName();
        
     final File jar = new File(libExt.getPath() + "/" + jarName);
     if (jar == null || ! jar.exists() ) {
     return;
     }
     HashMap<String, List<URL>> map = new HashMap<>();
     List<URL> list = new ArrayList<>();
     try {
     list.add(Utilities.toURI(jar).toURL());
     map.put("classpath", list);
     Library lib = LibraryManager.getDefault().createLibrary("j2se", jarName, map);
     ProjectClassPathModifier.addLibraries(new Library[]{lib}, p.getProjectDirectory(), ClassPath.COMPILE);
     } catch (IOException ex) {
     LOG.log(Level.INFO, ex.getMessage()); //NOI18N
     }

     }
     */

    public void modifyClasspath(Set result) {

        Project p = findProject(result);

        FileObject libExt = getLibDir(p);
        if (libExt == null) {
            return;
        }

        String jarName = getCommandManagerJarName();
        final File jar = new File(libExt.getPath() + "/" + jarName);
        if (jar == null || !jar.exists()) {
            return;
        }

        FileObject root = getSourceRoot(p);
        if (root == null) {
            return;
        }
        //
        // Now check if there is allredy the jar in the classpath of the project
        //
        ClassPath cp = ClassPath.getClassPath(root, ClassPath.COMPILE);
        if ( cp == null ) {
            return;
        }
        for (Entry e : cp.entries()) {
            File entryJar = LibrariesFileLocator.getFile(e.getURL());
            if (jar.equals(entryJar)) {
                return;
            }
        }
        //
        // Add jar to classpath
        //
        URI[] uri = new URI[]{Utilities.toURI(jar)};

        try {
            ProjectClassPathModifier.addRoots(uri, root, ClassPath.COMPILE);
        } catch (IOException ex) {
            LOG.log(Level.FINE, ex.getMessage());
        }
    }

    protected FileObject getSourceRoot(Project p) {
        Sources sources = ProjectUtils.getSources(p);
        SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        FileObject root = null;
        try {
            for (SourceGroup sourceGroup : sourceGroups) {
                root = sourceGroup.getRootFolder();
                break;

            }
        } catch (UnsupportedOperationException ex) {
            LOG.log(Level.INFO, ex.getMessage());
        }
        return root;
    }

}
