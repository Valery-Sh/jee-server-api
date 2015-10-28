/**
 * This file is part of Jetty Server support in NetBeans IDE.
 *
 * Jetty Server support in NetBeans IDE is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the License,
 * or (at your option) any later version.
 *
 * Jetty Server support in NetBeans IDE is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should see the GNU General Public License here:
 * <http://www.gnu.org/licenses/>.
 */
package org.netbeans.modules.jeeserver.base.embedded;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.specifics.InstanceBuilder;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.netbeans.modules.jeeserver.base.deployment.utils.LibrariesFileLocator;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteConstants;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteUtil;
import org.netbeans.modules.jeeserver.base.embedded.webapp.DistributedWebAppManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 *
 * @author V. Shyshkin
 */
public abstract class EmbeddedInstanceBuilder extends InstanceBuilder {

    private static final Logger LOG = Logger.getLogger(EmbeddedInstanceBuilder.class.getName());
    private boolean mavenbased;

    protected abstract FileObject getLibDir(Project project);

    public EmbeddedInstanceBuilder(Properties props, InstanceBuilder.Options opt) {
        super(props, opt);
    }

    public boolean isMavenbased() {
        return mavenbased;
    }
/*
    public void copyBuildXml(FileObject targetFolder) throws IOException {
        if (BaseUtil.isAntProject(FileOwnerQuery.getOwner(targetFolder))) {
            return;
        }
        String xmlTmpl = "/org/netbeans/modules/jeeserver/base/embedded/resources/maven-build.xml";
        FileObject buildXml = targetFolder.getParent().getFileObject("build.xml");

        if (buildXml != null) {
            return;
        }

        FileObject xml = targetFolder.getParent().createData("build", "xml");

        try (OutputStream os = xml.getOutputStream(); InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(xmlTmpl)) {
            FileUtil.copy(is, os);
        } catch (IOException ex) {
            LOG.log(Level.INFO, "EmbeddedInstanceBuilder. {0}", ex.getMessage());
        }

    }
*/
    public void setMavenbased(boolean mavenbased) {
        this.mavenbased = mavenbased;
    }

    protected void instantiateServerInstanceDir(Set result) {

        InstanceProperties ip = null;
        for (Object o : result) {
            if (o instanceof InstanceProperties) {
                ip = (InstanceProperties) o;
                break;
            }
        }

        FileObject instancesDir = FileUtil.toFileObject(new File(configProps.getProperty(SuiteConstants.SERVER_INSTANCES_DIR_PROP)));

        Project suite = FileOwnerQuery.getOwner(instancesDir);

        SuiteUtil.setSuiteProjectLocation(ip, suite.getProjectDirectory().getPath());
    }

    @Override
    protected String buildURL(String serverId, FileObject projectDir) {
        String serverInstancesFolder = configProps.getProperty(SuiteConstants.SERVER_INSTANCES_DIR_PROP);
        String suite = new File(serverInstancesFolder).getParent();
        String uid = getSuiteUID();
        
        
        String uri = serverId + ":" + BaseConstants.URIPREFIX_NO_ID + ":" + projectDir.getPath()
                + ":server:suite:project:" + suite + "/uid" + uid;
        return uri;
    }

    protected String getSuiteUID() {
        String uid = null;
        String serverInstancesFolder = configProps.getProperty(SuiteConstants.SERVER_INSTANCES_DIR_PROP);
        FileObject suite = FileUtil.toFileObject(new File(serverInstancesFolder)).getParent();
        FileObject suitePropsFo = suite.getFileObject(SuiteConstants.SUITE_PROPERTIES_LOCATION);
        Properties suiteProps = new Properties();
        try {
            if (suitePropsFo != null) {
                suiteProps = BaseUtil.loadProperties(suite.getFileObject(SuiteConstants.SUITE_PROPERTIES_LOCATION));
                uid = suiteProps.getProperty(SuiteConstants.UID_PROPERTY_NAME);
                if (uid != null) {
                    return uid;
                }
                suitePropsFo.delete();
                uid = UUID.randomUUID().toString();
            } else {
                uid = UUID.randomUUID().toString();
            }
            suiteProps.setProperty(SuiteConstants.UID_PROPERTY_NAME, uid);
            BaseUtil.storeProperties(suiteProps, suite.getFileObject(SuiteConstants.SUITE_CONFIG_FOLDER),
                    SuiteConstants.UID_PROPERTY_NAME);
        } catch (IOException ex) {
            LOG.log(Level.INFO, "EmbeddedInstanceBuilder. {0}", ex.getMessage());
        }
        return uid;
    }

    @Override
    protected void modifyPropertymap(Map<String, String> ip) {
        ip.put(SuiteConstants.MAVEN_MAIN_CLASS_PROP, (String) getWizardDescriptor()
                .getProperty(SuiteConstants.MAVEN_MAIN_CLASS_PROP));
    }

    public void modifyClasspath(Set result) {
        Project p = findProject(result);

        FileObject libExt = getLibDir(p);
        if (libExt == null) {
            return;
        }

        FileObject jarFo = SuiteUtil.getCommandManagerJar(p);

        if (jarFo == null) {
            return;
        }
        final File jar = FileUtil.toFile(jarFo);
        if (!jar.exists()) {
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

        if (cp == null) {
            return;
        }

        for (ClassPath.Entry e : cp.entries()) {
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

    protected void updateServerInstanceProperties(Project project) {
        DistributedWebAppManager distManager = DistributedWebAppManager.getInstance(project);
        String port = (String) getWizardDescriptor().getProperty(BaseConstants.HTTP_PORT_PROP);
        if (port == null) { // Cannot be
            port = "8080";
        }
        String shutdownPort = (String) getWizardDescriptor().getProperty(BaseConstants.SHUTDOWN_PORT_PROP);
        if (shutdownPort == null) { // Cannot be
            shutdownPort = String.valueOf(Integer.MAX_VALUE);
        }
        distManager.setServerInstanceProperty(BaseConstants.HTTP_PORT_PROP, port);
        distManager.setServerInstanceProperty(BaseConstants.SHUTDOWN_PORT_PROP, shutdownPort);        
        
        
        
    }

}
