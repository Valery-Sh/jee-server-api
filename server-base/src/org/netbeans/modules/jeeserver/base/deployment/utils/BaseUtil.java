/**
 * This file is part of Base JEE Server support in NetBeans IDE.
 *
 * Base JEE Server support in NetBeans IDE is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * Base JEE Server support in NetBeans IDE is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should see the GNU General Public License here:
 * <http://www.gnu.org/licenses/>.
 */
package org.netbeans.modules.jeeserver.base.deployment.utils;

import java.awt.Image;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.extexecution.startup.StartupExtender;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.CommonServerBridge;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.base.deployment.BaseTargetModuleID;
import org.netbeans.modules.jeeserver.base.deployment.ServerInstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.specifics.ServerSpecifics;
import org.netbeans.modules.jeeserver.base.deployment.specifics.ServerSpecificsProvider;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.EditableProperties;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.xml.XMLUtil;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author V. Shyshkin
 */
public class BaseUtil {

    private static final Logger LOG = Logger.getLogger(BaseUtil.class.getName());

    public static String getClassPath(BaseDeploymentManager manager) {
        StringBuilder sb = new StringBuilder();

        Sources sources = ProjectUtils.getSources(manager.getServerProject());
        SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        try {
            for (SourceGroup sourceGroup : sourceGroups) {

                FileObject fos = sourceGroup.getRootFolder();

                ClassPath classPath = ClassPath.getClassPath(fos, ClassPath.COMPILE);
                if (classPath == null) {
                    continue;
                }
                List<ClassPath.Entry> l = classPath.entries();
                int i = 0;
                for (ClassPath.Entry e : l) {
                    File file = FileUtil.archiveOrDirForURL(e.getURL());
                    if (file == null || file.isDirectory() || !file.exists()) {
                        continue;
                    }
                    if (!file.getName().endsWith(".jar")) {
                        continue;
                    }
                    if (sb.length() != 0) {
                        sb.append(":");
                    }
                    sb.append(file.getPath());
                }
            }

        } catch (UnsupportedOperationException ex) {
            LOG.log(Level.INFO, ex.getMessage());

        }
        return sb.toString();
    }

    public static String getMavenClassPath(BaseDeploymentManager manager) {
        StringBuilder sb = new StringBuilder();
        FileObject[] roots = getMavenSourceRoots(manager.getServerProject());
        try {
            for (FileObject root : roots) {

                ClassPath classPath = ClassPath.getClassPath(root, ClassPath.COMPILE);
                if (classPath == null) {
                    continue;
                }
                List<ClassPath.Entry> l = classPath.entries();
                int i = 0;
                for (ClassPath.Entry e : l) {
                    File file = FileUtil.archiveOrDirForURL(e.getURL());
                    if (file == null || file.isDirectory() || !file.exists()) {
                        continue;
                    }
                    if (!file.getName().endsWith(".jar")) {
                        continue;
                    }
                    if (sb.length() != 0) {
                        sb.append(":");
                    }
                    sb.append(file.getPath());
                }
            }

        } catch (UnsupportedOperationException ex) {
            LOG.log(Level.INFO, ex.getMessage());

        }
        return sb.toString();
    }

    public static FileObject getSourceRoot(BaseDeploymentManager dm) {
        Sources sources = ProjectUtils.getSources(dm.getServerProject());
        SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        FileObject result = null;
        try {
            for (SourceGroup sourceGroup : sourceGroups) {
                result = sourceGroup.getRootFolder();
                break;

            }
        } catch (UnsupportedOperationException ex) {
            LOG.log(Level.INFO, ex.getMessage());
        }
        return result;
    }

    public static final String NAME_SOURCE = "1SourceRoot";
    public static final String TYPE_GEN_SOURCES = "GeneratedSources";

    public static FileObject[] getMavenSourceRoots(Project project) {
        Sources sources = ProjectUtils.getSources(project);

        FileObject[] result = null;
        List<FileObject> list = new ArrayList<>();
        try {

            SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);

            for (SourceGroup sourceGroup : sourceGroups) {
                if (NAME_SOURCE.equals(sourceGroup.getName())) {
                    list.add(sourceGroup.getRootFolder());
                }
            }

            sourceGroups = sources.getSourceGroups(TYPE_GEN_SOURCES);

            for (SourceGroup sourceGroup : sourceGroups) {
                list.add(sourceGroup.getRootFolder());
            }

            result = list.toArray(new FileObject[list.size()]);
        } catch (UnsupportedOperationException ex) {
            LOG.log(Level.INFO, ex.getMessage());
        }

        return result;
    }

    public static String[] getMavenMainClasses(Project project) {
        String[] result = new String[0];
        FileObject[] sourceRoots = getMavenSourceRoots(project);
        if (sourceRoots == null || sourceRoots.length == 0) {
            return result;
        }
        Collection<ElementHandle<TypeElement>> c = SourceUtils.getMainClasses(sourceRoots);
        if (c == null || c.isEmpty()) {
            return result;
        }

        ElementHandle<TypeElement>[] elemArray = c.toArray(new ElementHandle[c.size()]);
        result = new String[c.size()];
        for (int i = 0; i < c.size(); i++) {
            result[i] = elemArray[i].getQualifiedName();
        }

        return result;
    }

    public static String stringOf(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;

        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException ex) {
            LOG.log(Level.INFO, ex.getMessage()); //NOI18N

        } finally {
            try {
                is.close();
            } catch (IOException ex) {
                LOG.log(Level.INFO, ex.getMessage()); //NOI18N
            }
        }

        return sb.toString();
    }

    public static void sleep(long msec) {
        Long time = System.currentTimeMillis();
        while (System.currentTimeMillis() < time + msec) {
        }

    }

    /**
     * Return an instance of {@literal J2eeModule} for the specified web
     * project.
     *
     * @param webProject the web project whose {@literal J2eeModule} is required
     * @return an instance of {@literal J2eeModule} for the specified web
     * project
     */
    public static J2eeModule getJ2eeModule(Project webProject) {
        return getJ2eeModuleProvider(webProject).getJ2eeModule();
    }

    /**
     *
     * @param webProject
     * @return
     */
    public static J2eeModuleProvider getJ2eeModuleProvider(Project webProject) {
        return webProject.getLookup().lookup(J2eeModuleProvider.class);
    }

    public static String getNbBundleMsg(Object obj, String bname) {
        String msg = NbBundle.getMessage(obj.getClass(), bname);
        if (msg == null) {
            msg = bname;
        }
        return NbBundle.getMessage(obj.getClass(), msg);
    }

    public static String getJavaVersion() {
        String java_version = System.getProperty("java.version");
        if (java_version != null) {
            String[] parts = java_version.split("\\.");
            if (parts != null && parts.length > 0) {
                System.setProperty("java.version.major", parts[0]);
            }
            if (parts != null && parts.length > 1) {
                System.setProperty("java.version.minor", parts[1]);
            }
        }
        return java_version;

    }

    public static String getJavaMajorVersion() {
        String java_version = System.getProperty("java.version");
        if (java_version != null) {
            String[] parts = java_version.split("\\.");
            if (parts != null && parts.length > 0) {
                System.setProperty("java.version.major", parts[0]);
            }
            if (parts != null && parts.length > 1) {
                System.setProperty("java.version.minor", parts[1]);
            }
        }
        return System.getProperty("java.version.major");

    }

    public static String getJavaMinorVersion() {
        String java_version = System.getProperty("java.version");
        if (java_version != null) {
            String[] parts = java_version.split("\\.");
            if (parts != null && parts.length > 0) {
                System.setProperty("java.version.major", parts[0]);
            }
            if (parts != null && parts.length > 1) {
                System.setProperty("java.version.minor", parts[1]);
            }
        }
        return System.getProperty("java.version.minor");

    }

    public static String createCommand(BaseTargetModuleID module, String cmd) {

        StringBuilder sb = new StringBuilder();
        sb.append("cmd=");
        sb.append(cmd);
        sb.append("&cp=");
        sb.append(encode(module.getContextPath()));
        sb.append("&dir=");

        sb.append(encode(module.getProjectDir()));
        return sb.toString();
    }

    /**
     * Translates a context path string into
     * <code>application/x-www-form-urlencoded</code> format.
     *
     * @param toEncode
     * @return
     */
    public static String encode(String toEncode) {
        String str = toEncode.replace("\\", "/");
        try {
            StringTokenizer st = new StringTokenizer(str, "/"); // NOI18N
            if (!st.hasMoreTokens()) {
                return str;
            }
            StringBuilder result = new StringBuilder();
            while (st.hasMoreTokens()) {
                result.append("/").append(URLEncoder.encode(st.nextToken(), "UTF-8")); // NOI18N
            }
            return result.toString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e); // this should never happen
        }
    }

    /**
     * Checks whether the specified port is already in use. The method doesn't
     * make something like "ping". It simply looks at all the registered Server
     * Instances to determine the {@literal http port} they use.
     *
     * @param port the {@literal port} to check
     * @param exclude the project, whose port is not taken into account
     * @return {@literal true } if the given port is already in use.
     * {@literal false} otherwise
     */
    public static boolean isPortBusy(int port, BaseDeploymentManager exclude) {
        if (port == Integer.MAX_VALUE) {
            return false;
        }
        boolean result = false;
        String[] uris = Deployment.getDefault().getServerInstanceIDs();
        String excludeUri = null;
        if (exclude != null) {
            excludeUri = exclude.getUri();
        }
        for (String uri : uris) {
            if (uri.equals(excludeUri)) {
                continue;
            }
            try {
                String p = InstanceProperties.getInstanceProperties(uri).getProperty(BaseConstants.HTTP_PORT_PROP);

                if (p != null && Integer.parseInt(p) == port) {
                    result = true;
                    break;
                }
                p = InstanceProperties.getInstanceProperties(uri).getProperty(BaseConstants.DEBUG_PORT_PROP);

                if (p != null && Integer.parseInt(p) == port) {
                    result = true;
                    break;
                }

                p = InstanceProperties.getInstanceProperties(uri).getProperty(BaseConstants.SHUTDOWN_PORT_PROP);

                if (p != null && Integer.parseInt(p) < Integer.MAX_VALUE && Integer.parseInt(p) == port) {
                    result = true;
                    break;
                }

            } catch (IllegalStateException | NumberFormatException ex) {
                LOG.log(Level.INFO, ex.getMessage());
            }
        }
        return result;
    }

    /**
     * Loads a specified file and returns it's content as an {
     *
     * @EditableProperties}
     * @param fo file object to be loaded
     * @return an instance of {@literal EditableProperties}
     */
    public static EditableProperties loadEditableProperties(FileObject fo) {

        final EditableProperties props = new EditableProperties(false);
        try (FileInputStream fis = new FileInputStream(fo.getPath())) {
            props.load(fis);
            fis.close();
            return props;
        } catch (IOException ioe) {
            LOG.log(Level.INFO, "loadEditableProperties() of " + fo.getNameExt(), ioe);
            return null;
        }
    }

    /**
     * Stores {@literal EditableProperties} content to file specified by the
     * second parameter.
     *
     * @param props an object to be stored
     * @param fo a file object which determines the destination file
     */
    public static void storeEditableProperties(EditableProperties props, FileObject fo) {
        try (FileOutputStream fos = new FileOutputStream(fo.getPath())) {
            props.store(fos);
            fos.close();
        } catch (IOException ioe) {
            LOG.log(Level.INFO, "loadEditableProperties() of " + fo.getNameExt(), ioe);
        }
    }

    /**
     * Stores {@literal Properties} content to file specified by the second
     * parameter. If the properties file already exists then the method does
     * nothing and returns {@code false}. To update existing properties file use {@link #updateProperties(java.util.Properties, org.openide.filesystems.FileObject, java.lang.String)
     *
     *
     * @param props an object to be stored
     * @param toDir
     * @param toFileName
     * @return
     */
    public static boolean storeProperties(final Properties props, final FileObject toDir, final String toFileName) {
        final FileObject fo = toDir.getFileObject(toFileName);
        if (fo != null) {
            return false;
        }
        try {
            FileUtil.runAtomicAction(new FileSystem.AtomicAction() {
                @Override
                public void run() throws IOException {

                    //OutputStream out = toDir.createAndOpen(toFileName);
                    try (OutputStream out = toDir.createAndOpen(toFileName);) {
                        props.store(out, "");
                        out.close();

                    } catch (IOException e) {
                        LOG.log(Level.INFO, "ESUtils storeProperties() run()", e);
                    }
                }
            });
            return true;
        } catch (IOException ex) {
            LOG.log(Level.INFO, "ESUtils storeProperties()", ex);
        }
        return false;
    }

    /**
     * Updates a file specified by its directory and name and updates its
     * content with property values specified by {@literal Properties} instance.
     * If the properties file doesn't exist then the method does nothing and
     * returns {@code false}. To store a new properties file use {@link #storeProperties(java.util.Properties, org.openide.filesystems.FileObject, java.lang.String)
     *
     * @param props a source of value used for updating
     * @param dir the directory where the file located
     * @param fileName the name of file to be updated
     * @return {@literal true} if success. {@literal false} if an error occurs
     * for some reason
     */
    public static boolean updateProperties(final Properties props, FileObject dir, final String fileName) {
        final FileObject fo = dir.getFileObject(fileName);
        if (fo == null) {
            return false;
        }
        try {
            FileUtil.runAtomicAction(new FileSystem.AtomicAction() {
                @Override
                public void run() throws IOException {

                    EditableProperties outProps = loadEditableProperties(fo);
                    FileLock lock = fo.lock();
                    FileOutputStream fos = new FileOutputStream(fo.getPath());
                    for (String propName : props.stringPropertyNames()) {
                        outProps.put(propName, props.getProperty(propName));
                    }
                    try {
                        outProps.store(fos);
                        fos.close();

                    } catch (IOException e) {
                        LOG.log(Level.INFO, e.getMessage());
                    } finally {
                        lock.releaseLock();
                    }
                }
            });
            return true;
        } catch (IOException ex) {
            LOG.log(Level.INFO, ex.getMessage());
        }
        return false;
    }

    /**
     * Prints messages to the Output window of the IDE. Useful for debugging.
     *
     * @param msg
     */
    public static void out(String msg) {
        InputOutput io = IOProvider.getDefault().getIO("ShowMessage", false);
        io.getOut().println(msg);
        io.getOut().close();
    }

    public static FileObject getWar(Project webProject) {
        try {
            return webProject.getLookup().lookup(J2eeModuleProvider.class).getJ2eeModule().getArchive();
        } catch (IOException ex) {
            return null;
        }
    }

    /**
     * Returns an instance of {@literal ServerSpecifics} for the specified
     * {@literal serverId}. For example, the serverId for {@literal server} is
     * {@literal "jetty9"}. {@literal serverId} is defined by the appropriate
     * {@link ServerSpecificsProvider} implementation.
     *
     * @param serverId the server identifier of a server.
     * @return an instance of {@literal ServerSpecifics} for the given
     * {@literal serverId}
     * @see ServerSpecificsProvider
     * @see ServerSpecifics
     */
    public static ServerSpecifics getServerSpecifics(String serverId) {
        DeploymentFactory[] fs = DeploymentFactoryManager.getInstance().getDeploymentFactories();
        for (DeploymentFactory f : fs) {
            if (!(f instanceof ServerSpecificsProvider)) {
                continue;
            }

            if (serverId.equals(((ServerSpecificsProvider) f).getServerId())) {
                return ((ServerSpecificsProvider) f).getSpecifics();
            }
        }
        return null;
    }

    public static BaseDeploymentManager managerOf(Project p) {

        BaseDeploymentManager dm = null;

        if (p == null || p.getProjectDirectory() == null) {
            return null;
        }

        if (!ProjectManager.getDefault().isProject(p.getProjectDirectory())) {
            return null;
        }

        Path sourceProjectPath = Paths.get(p.getProjectDirectory().getPath());

        Deployment deployment = Deployment.getDefault();

        if (deployment == null || deployment.getServerInstanceIDs() == null) {
            return null;
        }

        for (String uri : deployment.getServerInstanceIDs()) {
            InstanceProperties ip = InstanceProperties.getInstanceProperties(uri);
            if (ip == null) {
                continue;
            }
            String foundServerLocation = ip.getProperty(BaseConstants.SERVER_LOCATION_PROP);
            if (foundServerLocation == null || !new File(foundServerLocation).exists()) {
                // May be not a native plugin server
                continue;
            }

            FileObject foundServerFo = FileUtil.toFileObject(new File(foundServerLocation));

            if (!ProjectManager.getDefault().isProject(foundServerFo)) {
                return null;
            }

            Project serverProj = FileOwnerQuery.getOwner(foundServerFo);

            if (serverProj == null) {
                continue;
            }

            Path foundServerPath = Paths.get(foundServerLocation);

            if (sourceProjectPath.equals(foundServerPath)) {
                try {
                    dm = (BaseDeploymentManager) DeploymentFactoryManager.getInstance().getDisconnectedDeploymentManager(uri);
                } catch (DeploymentManagerCreationException ex) {
                    LOG.log(Level.INFO, ex.getMessage()); //NOI18N
                }

                break;
            }
        }
        return dm;

        //return getServerProperties(p) != null;
    }

    public static String getServerIdByAcualId(String actualServerId) {
        DeploymentFactory[] fs = DeploymentFactoryManager.getInstance().getDeploymentFactories();
        for (DeploymentFactory f : fs) {
            if (!(f instanceof ServerSpecificsProvider)) {
                continue;
            }
            String[] ids = ((ServerSpecificsProvider) f).getSupportedServerIds();
            for (String id : ids) {
                if (actualServerId.equals(id)) {
                    return ((ServerSpecificsProvider) f).getServerId();
                }
            }
        }
        return null;

    }

    /**
     * Returns an {@literal Image} instance for the specified server id.
     *
     * @param serverId the server identifier as defined by an appropriate 
     *   {@link ServerSpecificsProvider }
     * @return an {@literal Image} instance for the specified server id.
     */
    public static Image getServerImage(String serverId) {
        if (serverId == null) {
            return null;
        }
        Image image = null;
        DeploymentFactory[] factories = DeploymentFactoryManager.getInstance().getDeploymentFactories();
        for (DeploymentFactory df : factories) {
            if (df instanceof ServerSpecificsProvider) {
                ServerSpecificsProvider p = (ServerSpecificsProvider) df;
                if (serverId.equals(p.getServerId())) {
                    image = p.getSpecifics().getProjectImage(null); // we don't bother of the project
                    break;
                }
            }
        }
        return image;
    }

    public static boolean isMavenProject(String projDir) {
        Project proj = FileOwnerQuery.getOwner(FileUtil.toFileObject(new File(projDir)));
        //return new File(projDir + "/pom.xml").exists();
        return isMavenProject(proj);
    }

    public static boolean isMavenProject(Project proj) {
        for (Object o : proj.getLookup().lookupAll(Object.class)) {
            if ("org.netbeans.modules.maven.api.NbMavenProject".equals(o.getClass().getName())) {
                return true;
            }
        }
        return false;
        //return proj.getProjectDirectory().getFileObject("pom.xml") != null;  
    }

    public static boolean isMavenWebProject(Project proj) {
        boolean b = false;
        for (Object o : proj.getLookup().lookupAll(Object.class)) {
            if (!"org.netbeans.modules.maven.api.NbMavenProject".equals(o.getClass().getName())) {
                continue;
            }
            if (proj.getProjectDirectory().getFileObject("src/main/webapp") != null) {
                b = true;
                break;
            }
        }
        return b;
        //return proj.getProjectDirectory().getFileObject("pom.xml") != null;  
    }

    public static boolean isAntProject(Project proj) {
        return proj.getLookup().lookup(org.netbeans.api.project.ant.AntBuildExtender.class) != null;
        //return proj.getProjectDirectory().getFileObject("pom.xml") != null;  
    }

    /**
     * Return a deployment manager object for a given server project.
     *
     * @param serverProject a server project
     * @return an object of type {@link ProjectDeploymentManager} if exists.
     * {@literal null} otherwise
     */
    public static BaseDeploymentManager managerOf(Lookup context) {
        ServerInstanceProperties sp = context.lookup(ServerInstanceProperties.class);
        if (sp == null) {
            return null;
        }
        return sp.getManager();
    }

    /**
     * Returns an instance of {@literal ServerInstanceProperties} for a given
     * server project.
     *
     * @param context
     * @return
     */
    public static ServerInstanceProperties getServerProperties(Lookup context) {
        return context.lookup(ServerInstanceProperties.class);
    }

    /**
     * Returns a string representation of the profiler arguments for the given
     * deployment manager.
     *
     * @param manager an instance of {@link ProjectDeploymentManager}
     * @return a string representation of the {@literal agentpath} etc.
     */
    public static String getProfileArgs(BaseDeploymentManager manager) {
        ServerInstance inst = CommonServerBridge.getCommonInstance(manager.getUri());
        String path = "";
        for (StartupExtender args : StartupExtender.getExtenders(Lookups.singleton(inst), StartupExtender.StartMode.PROFILE)) {
            for (String arg : args.getArguments()) {
                path += " " + arg;
            }
        }
        return path;
    }

    /**
     * Return string {@literal uri} for the given server project
     *
     * @param context
     * @param project a server project
     * @return an {@literal uri} as it was used to create an instance of the
     * deployment manager
     */
    public static String getServerInstanceId(Lookup context) {
        return getServerProperties(context).getUri();
    }

    public static Properties loadProperties(FileObject propFile) {
        final Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(propFile.getPath());) {
            props.load(fis);
            fis.close();
        } catch (IOException ioe) {
            LOG.log(Level.INFO, "loadProperties() of " + propFile.getNameExt(), ioe);
        }
        return props;
    }

    public static Properties loadHtml5ProjectProperties(String projDir) {
        File f = new File(projDir + "/nbproject/project.properties");
        final Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(f)) {
            props.load(fis);
            fis.close();
            return props;
        } catch (IOException ioe) {
            LOG.log(Level.INFO, "loadHtml5ProjectProperties()", ioe);
            return null;
        }
    }

    public static String resolve(String key, Properties p) {
        String v = p.getProperty(key);
        if (v == null) {
            return null;
        }
        while (!resolved(v)) {
            v = getValue(v, p);
        }
        return v;
    }

    private static boolean resolved(String value) {
        if (value == null || !value.trim().contains("${")) {
            return true;
        }
        return false;
    }

    private static String getValue(String v, Properties p) {
        while (!resolved(v)) {
            String s = v;
            int i1 = s.indexOf("${");
            if (i1 < 0) {
                return v;
            }
            int i2 = s.indexOf("}");
            s = s.substring(i1 + 2, i2);
            s = resolve(s, p);
            StringBuilder sb = new StringBuilder(v);

            sb.replace(i1, i2 + 1, s);
            v = sb.toString();
        }
        return v;
    }

    public static String projectTypeByProjectXml(FileObject projXml) {
        String result = null;
        try {
            InputSource source = new InputSource(projXml.getInputStream());
            Document doc = XMLUtil.parse(source, false, false, null, null);
            NodeList nl = doc.getDocumentElement().getElementsByTagName("type");
            if (nl != null) {
                for (int i = 0; i < nl.getLength(); i++) {
                    Element el = (Element) nl.item(i);
                    result = el.getTextContent();
                    break;
                }
            }
        } catch (IOException | DOMException | SAXException ex) {
        }
        return result;
    }

    public static boolean isHtml5Project(String path) {
        FileObject fo = FileUtil.toFileObject(new File(path));
        if (fo == null) {
            return false;
        }
        Project proj = FileOwnerQuery.getOwner(fo);
        if (proj == null) {
            return false;
        }
        String type = getAntBasedProjectType(proj);
        if (BaseConstants.HTML5_PROJECTTYPE.equals(type)) {
            return true;
        }
        return false;
    }

    public static String getAntBasedProjectType(Project proj) {
        FileObject fo = proj.getProjectDirectory().getFileObject("nbproject/project.xml");
        if (fo == null) {
            return null;
        }
        return projectTypeByProjectXml(fo);
    }

    public static int comparePath(Path p1, Path p2) {
        if (p1.equals(p2)) {
            return 0;
        }
        int result = 0;
        if (Files.isDirectory(p1) && Files.isDirectory(p2)) {
            result = p1.compareTo(p2);
        } else if (Files.isDirectory(p1)) {
            Path p = p2.getParent();
            if (p1.equals(p)) {
                result = -1;
            } else {
                result = p1.compareTo(p);
            }
        } else if (Files.isDirectory(p2)) {
            Path p = p1.getParent();
            if (p2.equals(p)) {
                result = 1;
            } else {
                result = p1.compareTo(p);
            }
        } else {
            Path pp1 = p1.getParent();
            Path pp2 = p2.getParent();
            if (pp1.equals(pp2)) {
                result = p1.compareTo(p2);
            } else {
                result = pp1.compareTo(pp2);
            }

        }
        return result;
    }

    public static Properties getPomProperties(FileObject jarFo) {
        Properties props = new Properties();
        String s = Copier.ZipUtil.extractEntry(FileUtil.toFile(jarFo), "pom.properties", "META-INF/maven");

        if (s == null) {
            return null;
        }

        try (InputStream is = new ByteArrayInputStream(s.getBytes())) {
            InputStreamReader isr = new InputStreamReader(is);
            props.load(isr);
        } catch (Exception ex) {
            LOG.log(Level.WARNING, ex.getMessage());
        }
        return props;
    }

    /*    public static String getAntBasedProjectType(Project proj) {
     FileObject fo = proj.getProjectDirectory().getFileObject("nbproject/project.xml");
     if ( fo == null ) {
     return null;
     }
     return getAntBasedProjectType(fo);
     }
    
     public static String getAntBasedProjectType(FileObject xmlFo) {
     String result = null;
     File xmlFile = FileUtil.toFile(xmlFo);
     try {
     DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
     domFactory.setValidating(false);
     DocumentBuilder builder = domFactory.newDocumentBuilder();
     builder.setEntityResolver(new ParserEntityResolver());
     Document doc = builder.parse(xmlFile);

     NodeList nl = doc.getDocumentElement().getElementsByTagName("type");
     if (nl != null) {
     for (int i = 0; i < nl.getLength(); i++) {
     Element el = (Element) nl.item(i);
     result = el.getTextContent();
     break;
     }
     }

     } catch (IOException | DOMException | ParserConfigurationException | SAXException ex) {
     //out("Utils: getContextProperties EXCEPTION " + ex.getMessage());
     Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
     }
     return result;
     }
     */
}
