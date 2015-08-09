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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import org.netbeans.api.extexecution.startup.StartupExtender;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.jeeserver.base.deployment.ServerInstanceProperties;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.CommonServerBridge;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.base.deployment.BaseTargetModuleID;
import org.netbeans.modules.jeeserver.base.deployment.specifics.ServerSpecifics;
import org.netbeans.modules.jeeserver.base.deployment.specifics.ServerSpecificsProvider;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.EditableProperties;
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
public class BaseUtils {

    private static final Logger LOG = Logger.getLogger(BaseUtils.class.getName());
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
        String msg = NbBundle.getMessage(obj.getClass(),bname);
        if ( msg == null ) {
            msg = bname;
        }
        return NbBundle.getMessage(obj.getClass(),msg);
    }
    public static String getJavaVersion() {
        String java_version = System.getProperty("java.version");
        if (java_version!=null)
        {
            String[] parts = java_version.split("\\.");
            if (parts!=null && parts.length>0)
                System.setProperty("java.version.major",parts[0]);
            if (parts!=null && parts.length>1)
                System.setProperty("java.version.minor",parts[1]);
        }        
        return java_version;
        
    }
    public static String getJavaMajorVersion() {
        String java_version = System.getProperty("java.version");
        if (java_version!=null)
        {
            String[] parts = java_version.split("\\.");
            if (parts!=null && parts.length>0)
                System.setProperty("java.version.major",parts[0]);
            if (parts!=null && parts.length>1)
                System.setProperty("java.version.minor",parts[1]);
        }        
        return System.getProperty("java.version.major");

        
    }    
    public static String getJavaMinorVersion() {
        String java_version = System.getProperty("java.version");
        if (java_version!=null)
        {
            String[] parts = java_version.split("\\.");
            if (parts!=null && parts.length>0)
                System.setProperty("java.version.major",parts[0]);
            if (parts!=null && parts.length>1)
                System.setProperty("java.version.minor",parts[1]);
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
    public static boolean isPortBusy(int port, Project exclude) {
        if (port == Integer.MAX_VALUE) {
            return false;
        }
        boolean result = false;
        String[] uris = Deployment.getDefault().getServerInstanceIDs();
        String excludeUri = null;
        if (exclude != null) {
            excludeUri = managerOf(exclude).getUri();
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
                    try(OutputStream out = toDir.createAndOpen(toFileName);) {
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

    /**
     * Checks whether the specified project is actually a server project. Every
     * server project has an object of type {@link ServerInstanceProperties} in
     * its lookup
     *
     * @param p a project to be checked
     * @return {@literal true } if the specified project is a server project.
     */
    public static boolean isServerProject(Project p) {
        return getServerProperties(p) != null;
    }
    
    public static boolean isMavenProject(String projDir) {
        Project proj = FileOwnerQuery.getOwner(FileUtil.toFileObject(new File(projDir)));
        //return new File(projDir + "/pom.xml").exists();
        return isMavenProject(proj);
    }
    
    public static boolean isMavenProject(Project proj) {
        for ( Object o : proj.getLookup().lookupAll(Object.class)) {
            if ( "org.netbeans.modules.maven.api.NbMavenProject".equals(o.getClass().getName()) ) {
                return true;
            }
        }
        return false;
        //return proj.getProjectDirectory().getFileObject("pom.xml") != null;  
    }

    public static boolean isAntProject(Project proj) {
        
        return proj.getLookup().lookup( org.netbeans.api.project.ant.AntBuildExtender.class) != null; 
        //return proj.getProjectDirectory().getFileObject("pom.xml") != null;  
    }
    
    /**
     * Return a deployment manager object for a given server project.
     *
     * @param serverProject a server project
     * @return an object of type {@link BaseDeploymentManager} if exists.
     * {@literal null} otherwise
     */
    public static BaseDeploymentManager managerOf(Project serverProject) {
        ServerInstanceProperties sp = getServerProperties(serverProject);
        if (sp == null) {
            return null;
        }
        return getServerProperties(serverProject).getManager();
    }

    /**
     * Returns an instance of {@literal ServerInstanceProperties} for a given
     * server project.
     *
     * @param serverProject a server project
     * @return
     */
    public static ServerInstanceProperties getServerProperties(Project serverProject) {
        return serverProject.getLookup().lookup(ServerInstanceProperties.class);
    }

    /**
     * Returns a string representation of the profiler arguments for the given
     * deployment manager.
     *
     * @param manager an instance of {@link BaseDeploymentManager}
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
     * @param project a server project
     * @return an {@literal uri} as it was used to create an instance of the
     * deployment manager
     */
    public static String getUri(Project project) {
        return getServerProperties(project).getUri();
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
        if ( v == null ) {
            return null;
        }
        while ( ! resolved(v) ) {
            v = getValue(v, p);
        }
        return v;
    }
    private static boolean resolved(String value) {
        if ( value == null ||  ! value.trim().contains("${")) {
            return true;
        }
        return false;
    }
    
    private static String getValue(String v, Properties p) {
        while ( ! resolved(v)) {
            String s = v;
            int i1 = s.indexOf("${");
            if ( i1 < 0 ) {
                return v;
            }
            int i2 = s.indexOf("}");
            s = s.substring(i1+2, i2);
            s = resolve(s,p); 
            StringBuilder sb = new StringBuilder(v);
           
            sb.replace(i1, i2+1, s);
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
        if ( fo == null ) {
            return false;
        }
        Project proj = FileOwnerQuery.getOwner(fo);
        if ( proj == null ) {
            return false;
        }
        String type = getAntBasedProjectType(proj);
        if ( BaseConstants.HTML5_PROJECTTYPE.equals(type) ) {
            return true;
        }
        return false;
    }
    public static String getAntBasedProjectType(Project proj) {
        FileObject fo = proj.getProjectDirectory().getFileObject("nbproject/project.xml");
        if ( fo == null ) {
            return null;
        }
        return projectTypeByProjectXml(fo);
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