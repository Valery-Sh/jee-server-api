package org.netbeans.modules.jeeserver.jetty.embedded;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.embedded.server.project.wizards.ServerInstanceAntBuildExtender;
import org.netbeans.modules.jeeserver.base.embedded.server.project.wizards.EmbeddedInstanceBuilder;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteConstants;
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

    public JettyInstanceBuilder(Properties configProps) {
        super(configProps);
    }

    @Override
    public Set instantiate() {
        Set set = new HashSet();
        try {
            instantiateProjectDir(set);
            instantiateServerProperties(set);
            instantiateServerInstanceDir(set);
            ServerInstanceAntBuildExtender extender = new ServerInstanceAntBuildExtender(findProject(set));
            extender.enableExtender();

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
    public void finishInstantiateProjectDir(Set result) {
        Project proj = findProject(result);
        FileObject projectDir = proj.getProjectDirectory();
        
        String actualServerId = (String) getWizardDescriptor()
                .getProperty(SuiteConstants.SERVER_ACTUAL_ID_PROP);
        String cmOut = actualServerId + "-command-manager";
        String cmIn = "/org/netbeans/modules/jeeserver/jetty/embedded/resources/" + actualServerId + "-command-manager.jar";

        FileObject libExt = getLibDir(proj);
        
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
        } catch (IOException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        }
    }

}
