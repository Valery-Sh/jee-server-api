package org.netbeans.modules.jeeserver.base.embedded.project.wizard;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.netbeans.modules.jeeserver.base.embedded.project.SuiteManager;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteConstants;
import org.netbeans.modules.jeeserver.base.embedded.webapp.DistributedWebAppManager;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.filesystems.FileObject;
import org.openide.xml.XMLUtil;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Extends the embedded server project's build script with new targets. A server
 * project is a standard j2se project and it's build script provides only
 * standalone debug and profile capabilities. The class extends the project's
 * build script to provide remote debugging and profiling.
 * <ul>
 * <li>
 * The new target with a name {@code debug-embedded-server} allows the server to
 * run in debug mode.
 * </li>
 * <li>
 * The new target with a name {@code profile-embedded-server} allows the server
 * to run in profile mode.
 * </li>
 * <li>
 * The new target with a name {@code package-dist-jar} allows to package web
 * projects and server classes in a single jar archive.
 * </li>
 * <li>
 * The new target with a name {@code package-dist-wars} allows to package war
 * archives to the specified folder in the file system.
 * </li>
 * </ul>
 *
 *
 * .
 *
 * @author V. Shyshkin
 */
public class ServerInstanceAntBuildExtender extends ServerInstanceBuildExtender {

    private static final Logger LOG = Logger.getLogger(ServerInstanceAntBuildExtender.class.getName());

    /**
     * The <code>build-impl.xml</code> target tag whose <code>depends</code>
     * attribute is to be extended with a
     * <code>-server-embedded-macrodef-debug</code> and
     * <code>-server-embedded-macrodef-profile</code> items.
     * <p>
     *
     * <code>value = "-pre-pre-compile"</code>
     */
    private static final String PRE_PRE_TARGET_NAME = "-pre-pre-compile"; // NOI18N
    private static final String EMBEDDED_ID = "embedded-server"; // NOI18N
    private static final String EMBEDDED_BUILDXML_PATH = "nbproject/server-build.xml"; // NOI18N
    private static final String SERVER_BUILDXML_NAME = "server-build.xml"; // NOI18N    
    private static final String BUILD_IMPL_XML = "nbproject/build-impl.xml"; // NOI18N

    /**
     * <code>value = "org/netbeans/modules/embedded/extender/es-build.xsl"</code>
     */
    @StaticResource
    private static final String BUILD_XSL = "org/netbeans/modules/jeeserver/base/embedded/project/wizard/es-build.xsl"; // NOI18N

    /**
     * Creates a new instance of the class for a given project.
     *
     * @param project
     */
    public ServerInstanceAntBuildExtender(Project project) {
        super(project);
    }

    /**
     * Creates or updates the build script extension.
     */
    @Override
    public void enableExtender() {
        boolean m = ProjectManager.getDefault().isModified(project);
        addBuildScript();
        try {
            ProjectManager.getDefault().saveProject(project);
        } catch (IOException ex) {
            LOG.log(Level.INFO, ex.getMessage());

        }
        if (!isValid()) {
            rebuild();
        }
        updateNbDeploymentFile();
    }

    public void updateNbDeploymentFile() {
        FileObject projFo = project.getProjectDirectory();
/*        FileObject d = projFo.getFileObject(SuiteConstants.INSTANCE_NBDEPLOYMENT_FOLDER);
        if (d != null) {
            updateNbDeploymentFile(d);
            return;
        }
*/        
//            FileObject toDir = projFo.createFolder(SuiteConstants.INSTANCE_NBDEPLOYMENT_FOLDER);
//            Properties props = new Properties();
        DistributedWebAppManager distManager = DistributedWebAppManager.getInstance(project);
        InstanceProperties ip = SuiteManager.getManager(project).getInstanceProperties();
        distManager.setServerInstanceProperty(BaseConstants.HTTP_PORT_PROP, ip.getProperty(BaseConstants.HTTP_PORT_PROP));
//            props.setProperty(BaseConstants.HTTP_PORT_PROP, ip.getProperty(BaseConstants.HTTP_PORT_PROP));
//            BaseUtil.storeProperties(props, toDir, SuiteConstants.INSTANCE_PROPERTIES_FILE);
    }

    @Override
    public void updateNbDeploymentFile(FileObject nbDir) {
        DistributedWebAppManager distManager = DistributedWebAppManager.getInstance(project);

        //FileObject propsFo = nbDir.getFileObject(SuiteConstants.INSTANCE_PROPERTIES_FILE);
        InstanceProperties ip = SuiteManager.getManager(project).getInstanceProperties();
        distManager.setServerInstanceProperty(BaseConstants.HTTP_PORT_PROP, ip.getProperty(BaseConstants.HTTP_PORT_PROP));
        //BaseUtil.updateProperties(props, nbDir, SuiteConstants.INSTANCE_PROPERTIES_FILE);
    }

    protected void rebuild() {
        try {
            project.getProjectDirectory().getFileObject(BUILD_IMPL_XML).delete();
            OpenProjects.getDefault().close(new Project[]{project});
            OpenProjects.getDefault().open(new Project[]{project}, true);
        } catch (IOException ex) {
            LOG.log(Level.INFO, ex.getMessage());
        }
        addBuildScript();
    }

    protected boolean isValid() {

        boolean valid = false;

        FileObject buildimplFo = project.getProjectDirectory().getFileObject(BUILD_IMPL_XML);

        try (InputStream is = buildimplFo.getInputStream();) {
            //---------------------------------------------------------------------
            // Pay attension tht third parameter. It's true to correctly 
            // work with namespaces. If false then all namespaces will be lost
            // For example:
            // <j2seproject3:javac gensrcdir="${build.generated.sources.dir}"/>
            // will be modified as follows:
            // <javac gensrcdir="${build.generated.sources.dir}"/>
            //---------------------------------------------------------------------
            Document doc = XMLUtil.parse(new InputSource(is), false, true, null, null);
            NodeList nl = doc.getDocumentElement().getElementsByTagName("import");
            if (nl != null) {
                for (int i = 0; i < nl.getLength(); i++) {
                    Element el = (Element) nl.item(i);
                    String fileAttr = el.getAttribute("file");
                    if (fileAttr == null) {
                        continue;
                    }
                    if (SERVER_BUILDXML_NAME.equals(el.getAttributeNode("file").getValue())) {
                        valid = true;
                        break;
                    }
                }
            }

        } catch (IOException | DOMException | SAXException ex) {
            LOG.log(Level.INFO, ex.getMessage());
        }
        return valid;
    }

    /**
     * Removes build script extension.
     */
    public void disableExtender() {
        removeBuildScript();
/*        try {
            FileObject projFo = project.getProjectDirectory();
            FileObject toDelete = projFo.getFileObject(SuiteConstants.INSTANCE_NBDEPLOYMENT_FOLDER);
            if (toDelete != null) {
                toDelete.delete();
                ProjectManager.getDefault().saveProject(project);
            }
        } catch (IOException ex) {
            LOG.log(Level.INFO, ex.getMessage());
        }
*/
    }

    /**
     * Creates or updates an extension build script. The name of the build file
     * is specified by the constant {@link #EMBEDDED_BUILDXML_PATH}. To create
     * or update the build file the method applies {@code .xls} file with a name
     * specified by the constant {@link #BUILD_XSL }.
     */
    protected void refreshScript() {
        String xslPath = BUILD_XSL;
        String xmlPath = EMBEDDED_BUILDXML_PATH;
        if (isEmbeddedServerEnabled()) {
            GeneratedFilesHelper helper = new GeneratedFilesHelper(project.getProjectDirectory());
            URL stylesheet = this.getClass().getClassLoader().getResource(xslPath);

            try {
                int flags = helper.getBuildScriptState(xmlPath, stylesheet);
                if ((GeneratedFilesHelper.FLAG_MODIFIED & flags) != 0
                        && (GeneratedFilesHelper.FLAG_OLD_PROJECT_XML & flags) != 0
                        && (GeneratedFilesHelper.FLAG_OLD_STYLESHEET & flags) != 0) {

                    FileObject buildScript = project.getProjectDirectory().getFileObject(xmlPath);
                    if (buildScript != null) {
                        buildScript.delete();

                        helper.generateBuildScriptFromStylesheet(xmlPath, stylesheet);
                        return;
                    }
                }

                helper.refreshBuildScript(xmlPath, stylesheet, true);
            } catch (IOException | IllegalStateException ex) {
                LOG.log(Level.INFO, ex.getMessage()); //NOI18N
            }
        }
    }

    /**
     * <ul>
     * <li>
     * Create <code>server-build.xml</code> file. This file content represents
     * Ant targets whose <code>name</code> attributes are as follows:
     * <ul>
     * <li>
     * <code>-server-embedded-macrodef-debug</code>
     * </li>
     * <li>
     * <code>-server-embedded-macrodef-profile</code>
     * </li>
     * <li>
     * <code>debug-embedded-server</code>
     * </li>
     * <li>
     * <code>profile-embedded-server</code>
     * </li>
     * <li>
     * <code>package-dist-jar</code>
     * </li>
     * <li>
     * <code>package-dist-wars</code>
     * </li>
     * </ul>
     * </li>
     * <li>
     * Modify ant target named <code>-pre-pre-compile</code>. The target task
     * must depend on <code>"-server-embedded-macrodef-debug"<code> target and
     *   on <code>"-server-embedded-macrodef-profile"<code> target.
     * </li>
     * </ul>
     */
    protected void addBuildScript() {
        refreshScript();
        AntBuildExtender extender = project.getLookup().lookup(AntBuildExtender.class);
        if (extender != null && extender.getExtensibleTargets().contains(PRE_PRE_TARGET_NAME)) {
            AntBuildExtender.Extension extension = extender.getExtension(EMBEDDED_ID);
            if (extension == null) {
                FileObject destDirFO = project.getProjectDirectory().getFileObject("nbproject"); // NOI18N
                try {
                    GeneratedFilesHelper helper = new GeneratedFilesHelper(project.getProjectDirectory());
                    URL stylesheet = this.getClass().getClassLoader().getResource(BUILD_XSL);
                    helper.generateBuildScriptFromStylesheet(EMBEDDED_BUILDXML_PATH, stylesheet);
                    FileObject destFileFO = destDirFO.getFileObject("server-build", "xml"); // NOI18N
                    extension = extender.addExtension(EMBEDDED_ID, destFileFO);
                    extension.addDependency(PRE_PRE_TARGET_NAME, "-server-embedded-macrodef-debug"); // NOI18N
                    extension.addDependency(PRE_PRE_TARGET_NAME, "-server-embedded-macrodef-profile");

                    ProjectManager.getDefault().saveProject(project);
                } catch (IOException ex) {
                    LOG.log(Level.INFO, ex.getMessage()); //NOI18N  
                }
            }
        }
    }

    /**
     * Removes build script {@code server-build.xml}</code>.
     *
     * @return <code>true</code> if operation is successful. <code>false</code>
     * otherwise.
     */
    private boolean removeBuildScript() {
        AntBuildExtender extender = project.getLookup().lookup(AntBuildExtender.class);
        if (extender != null && extender.getExtensibleTargets().contains(PRE_PRE_TARGET_NAME)) {
            AntBuildExtender.Extension extension = extender.getExtension(EMBEDDED_ID);
            if (extension != null) {
                FileObject destDirFO = project.getProjectDirectory().getFileObject("nbproject"); // NOI18N
                try {
                    extension.removeDependency(PRE_PRE_TARGET_NAME, "-server-embedded-macrodef-debug"); // NOI18N
                    extension.removeDependency(PRE_PRE_TARGET_NAME, "-server-embedded-macrodef-profile"); // NOI18N
                    extender.removeExtension(EMBEDDED_ID);
                    if (destDirFO != null) {
                        FileObject fileToRemove = destDirFO.getFileObject("server-build.xml"); // NOI18N
                        if (fileToRemove != null) {
                            fileToRemove.delete();
                        }
                    }
                    //ProjectManager.getDefault().saveProject(project);
                    return true;
                } catch (IOException ex) {
                    LOG.log(Level.INFO, ex.getMessage()); //NOI18N
                }
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines whether the project lookup contains an extender for the
     * extension id specified by the value of the constant {@link #EMBEDDED_ID}
     *
     * @return {@code true} if the extender is enabled, {@code false} otherwise.
     */
    public boolean isEmbeddedServerEnabled() {
        AntBuildExtender extender = project.getLookup().lookup(AntBuildExtender.class);
        //return extender != null;        
        return extender != null && extender.getExtension(EMBEDDED_ID) != null;
    }
}
