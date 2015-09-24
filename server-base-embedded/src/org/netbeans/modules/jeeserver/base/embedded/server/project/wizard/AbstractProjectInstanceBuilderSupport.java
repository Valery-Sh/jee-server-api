/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jeeserver.base.embedded.server.project.wizard;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceCreationException;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.specifics.ProjectWizardBuilder;
import org.netbeans.modules.jeeserver.base.deployment.specifics.ServerSpecificsProvider;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.netbeans.modules.jeeserver.base.embedded.server.project.wizards.ServerInstanceConnectorWizardPanel;
import org.netbeans.modules.jeeserver.base.embedded.server.project.wizards.ServerInstanceProjectWizardPanel;
import org.netbeans.modules.jeeserver.base.embedded.specifics.EmbeddedServerSpecifics;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Valery
 */
public abstract class AbstractProjectInstanceBuilderSupport {

    private static final Logger LOG = Logger.getLogger(AbstractProjectInstanceIterator.class.getName());

    public static final boolean MAVEN_BASED = false;

    private int index;
    private WizardDescriptor.Panel[] panels;
    private WizardDescriptor wiz;

    protected ProjectWizardBuilder wizBuilder;

    protected final boolean ismavenbased;
    protected final FileObject serverSuiteDir;
    
    public AbstractProjectInstanceBuilderSupport(FileObject serverSuiteDir, boolean mavenBased) {
        this.ismavenbased = mavenBased;
        this.serverSuiteDir = serverSuiteDir;
        init();
    }

    private void init() {
        this. wizBuilder = getServerSpecifics().getWizardBuilder();
    }

    protected EmbeddedServerSpecifics getServerSpecifics() {

        EmbeddedServerSpecifics specifics = null;

        DeploymentFactory[] fs = DeploymentFactoryManager.getInstance().getDeploymentFactories();

        for (DeploymentFactory f : fs) {
            if (!(f instanceof ServerSpecificsProvider)) {
                continue;
            }
            ServerSpecificsProvider ssp = (ServerSpecificsProvider) f;
            if (!(ssp.getSpecifics() instanceof EmbeddedServerSpecifics)) {
                continue;
            }
            //specifics = (EmbeddedServerSpecifics) ssp.getSpecifics();
            if (! (ssp.getSpecifics() instanceof EmbeddedServerSpecifics ) ) {
                continue;
            }
        }

        return specifics;
    }

    protected WizardDescriptor.Panel[] createDefaultPanels() {

        WizardDescriptor.Panel[] result = null;

        if (! MAVEN_BASED) {
            result = new WizardDescriptor.Panel[]{
                new ServerInstanceProjectWizardPanel(false),
                new ServerInstanceConnectorWizardPanel()
            };
        }
        return result;
    }

    public abstract void instantiate() throws IOException;


    protected FileObject runInstantiateProjectDir() throws IOException {
        File dirF = FileUtil.normalizeFile((File) wiz.getProperty("projdir"));
        dirF.mkdirs();

        FileObject dir = FileUtil.toFileObject(dirF);
        if ( MAVEN_BASED ) {
            wizBuilder.unzipMavenProjectTemplate(dir);
        } else {
            wizBuilder.unzipAntProjectTemplate(dir);
        }
        // Always open top dir as a project:
        //EmbServerSpecifics s;
        File parent = dirF.getParentFile();
        if (parent != null && parent.exists()) {
            ProjectChooser.setProjectsFolder(parent); // Last used folder with a new project
        }

        return dir;

    }

/*    public void createBuildXml(FileObject projectDir) throws IOException {

        String buildxml = Utils.stringOf(AbstractProjectInstanceIterator.class.getResourceAsStream("/org/netbeans/modules/jeeserver/jetty/resources/build.template"));
        buildxml = buildxml.replace("${jetty_server_instance_name}", "\"" + projectDir.getNameExt() + "\"");

        FileObject fo = projectDir.getFileObject(JettyConstants.JETTYBASE_FOLDER + "/build.xml");
        InputStream is = new ByteArrayInputStream(buildxml.getBytes());
        OutputStream os = fo.getOutputStream();

        try {
            FileUtil.copy(is, os);
        } finally {
            try {
                is.close();
                os.close();
            } catch (IOException ex) {
                LOG.log(Level.INFO, ex.getMessage()); //NOI18N
            }
        }
    }
*/
    /**
     * !!! NOT USED
     *
     * @param projectDir
     * @throws IOException
     */
    /*    public void modifyNPNModule(FileObject projectDir) throws IOException {
     String npn = (String) wiz.getProperty(PROP_ENABLE_NPN);
     boolean enabledNPN = true;

     if (npn == null || !Boolean.parseBoolean(npn)) {
     enabledNPN = false;
     }

     FileObject npnFo = projectDir.getFileObject("jetty.base/modules/npn.mod");
     FileObject npnOldFo = projectDir.getFileObject("jetty.base/modules/npn.old.mod");

     if (enabledNPN && npnOldFo != null) {
     npnFo.delete();
     }

     }
     */
    
    /**
     * 
     * @param resultSet
     * @return
     * @throws IOException 
     */
    protected FileObject runInstantiateServerInstanceDir() throws IOException {
        Map <String, String> map = getPropertyMap();
        String name = new File(map.get(BaseConstants.SERVER_LOCATION_PROP)).getName();
        Path p  = Paths.get(serverSuiteDir.getPath(),name);
        Files.createDirectory(p);
        FileObject dir = FileUtil.toFileObject(p.toFile());
        Properties iprops = new Properties();
        iprops.setProperty(BaseConstants.URL_PROP, map.get(BaseConstants.URL_PROP));
        BaseUtils.storeProperties(iprops, dir, name);
        return dir;
    }    
    
    protected FileObject[] instantiateProjectDir() throws IOException {
        final FileObject[] ar = new FileObject[2];
        FileUtil.runAtomicAction(new Runnable() {
            @Override
            public void run() {
                try {
                    ar[0] = runInstantiateProjectDir();
                    ar[1] = runInstantiateServerInstanceDir();
                } catch (IOException ex) {
                    LOG.log(Level.FINE, ex.getMessage()); //NOI18N
                }
            }
        });
        return ar;
    }

    protected Set<InstanceProperties> instantiateInstanceProperties() {
        Set<InstanceProperties> result = new HashSet<>();
        Map<String, String> ipmap = getPropertyMap();
        String url = ipmap.get(BaseConstants.URL_PROP);
        String displayName = ipmap.get(BaseConstants.DISPLAY_NAME_PROP);

        try {
            InstanceProperties ip = InstanceProperties.createInstanceProperties(url, null, null, displayName, ipmap);
            result.add(ip);
        } catch (InstanceCreationException ex) {
            LOG.log(Level.SEVERE, ex.getMessage()); //NOI18N
        }

        return result;
    }

    protected Properties mapToProperties(Map<String, String> map) {
        Properties props = new Properties();
        for (String key : map.keySet()) {
            props.setProperty(key, map.get(key));
        }
        return props;
    }
    
    
    /**
     * We do not need {@literal maven} properties such as groupId, artifactId, version.
     * User can change them and we can parse (@literal pom.xml} anytime we need them.
     * @return 
     */
    protected Map<String, String> getPropertyMap() {

        Map<String, String> ip = new HashMap<>();
        FileObject projectDir = FileUtil.toFileObject(FileUtil.normalizeFile((File) wiz.getProperty("projdir")));
        String serverId = (String) wiz.getProperty(BaseConstants.SERVER_ID_PROP);
        String url = serverId + ":" + BaseConstants.URIPREFIX_NO_ID + ":" + projectDir.getPath() + "-" + serverSuiteDir.getParent().getPath().replace("\\", "/");

        String displayName = projectDir.getNameExt();

        ip.put(BaseConstants.SERVER_ID_PROP, serverId);
        ip.put(BaseConstants.DISPLAY_NAME_PROP, displayName);

        ip.put(BaseConstants.HOST_PROP, (String) wiz.getProperty(BaseConstants.HOST_PROP));
        ip.put(BaseConstants.HTTP_PORT_PROP, (String) wiz.getProperty(BaseConstants.HTTP_PORT_PROP));
        ip.put(BaseConstants.DEBUG_PORT_PROP, (String) wiz.getProperty(BaseConstants.DEBUG_PORT_PROP));
        ip.put(BaseConstants.SHUTDOWN_PORT_PROP, (String) wiz.getProperty(BaseConstants.SHUTDOWN_PORT_PROP));
        ip.put(BaseConstants.URL_PROP, url);
        ip.put(BaseConstants.SERVER_LOCATION_PROP, projectDir.getPath());
        //ip.put(BaseConstants.SERVER_VERSION_PROP, jettyVersion);
        
        
        return ip;
    }

}
