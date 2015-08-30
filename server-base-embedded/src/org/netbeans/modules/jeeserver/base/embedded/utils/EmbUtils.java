package org.netbeans.modules.jeeserver.base.embedded.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.embedded.specifics.EmbeddedServerSpecifics;
import org.netbeans.modules.jeeserver.base.deployment.specifics.ServerSpecifics;

import org.netbeans.modules.jeeserver.base.embedded.EmbJ2eePlatformFactory;
import org.netbeans.modules.jeeserver.base.deployment.BaseJ2eePlatformImpl;
import org.netbeans.modules.jeeserver.base.deployment.ServerInstanceProperties;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author V. Shyshkin
 */
public class EmbUtils extends BaseUtils{


/*    public static String buildUri(FileObject projectDir) {
        return getServerId() + ":" + BaseConstants.URIPREFIX_NO_ID + ":" + projectDir.getPath();
    }
*/    
    private static final Logger LOG = Logger.getLogger(EmbUtils.class.getName());

/*    
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
*/    
/*    
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
*/
    public static StringBuilder getServerInfo(Project server) {
        Properties props = EmbUtils.loadServerProperties(server);
        StringBuilder sb = new StringBuilder();
        int len = 50;
        String sep = System.lineSeparator();
        File file = FileUtil.toFile(server.getProjectDirectory());

        //String s = new String();
        sb.append(replicate('=', len))
                .append(sep)
                .append("SERVER: ")
                .append("\t\t")
                .append(props.getProperty(BaseConstants.SERVER_ID_PROP))
                .append(sep)
                .append("--- Name:\t\t")
                .append(file.getName())
                .append(sep)
                .append("--- Location:\t\t")
                .append(file.getAbsolutePath())
                .append(sep)
                .append("--- Host:\t\t")
                .append(props.getProperty(BaseConstants.HOST_PROP))
                .append(sep)
                .append("--- Http Port:\t\t")
                .append(props.getProperty(BaseConstants.HTTP_PORT_PROP))
                .append(sep)
                .append("--- Debug Port:\t\t")
                .append(props.getProperty(BaseConstants.DEBUG_PORT_PROP))
                .append(sep)
                .append("--- Incr Deploy:\t")
                .append(props.getProperty(BaseConstants.INCREMENTAL_DEPLOYMENT))
                .append(sep)
                .append(replicate('=', len))
                .append(sep);

        return sb;
    }

    static StringBuilder replicate(char c, int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb;
    }

   
    /**
     * Checks whether the specified port is already in use. The method doesn't
     * make something like "ping". It simply looks at all the registered Server
     * Instances to determine the {@literal http port} they use.
     *
     * @param port the {@literal http port} to check
     * @param exclude the embedded project, whose port is not taken into account
     * @return {@literal true } if the given port is already in use.
     * {@literal false} otherwise
     */
    public static boolean isHttpPortBusy(int port, Project exclude) {
        return isPortBusy(port, BaseConstants.HTTP_PORT_PROP, exclude);
    }
    /**
     * Checks whether the specified port is already in use. The method doesn't
     * make something like "ping". It simply looks at all the registered Server
     * Instances to determine the {@literal http port} they use.
     *
     * @param port the {@literal http port} to check
     * @param exclude the embedded project, whose port is not taken into account
     * @return {@literal true } if the given port is already in use.
     * {@literal false} otherwise
     */
    public static boolean isDebugPortBusy(int port, Project exclude) {
        return isPortBusy(port, BaseConstants.DEBUG_PORT_PROP, exclude);
    }

    public static boolean isShutdownPortBusy(int port, Project exclude) {
        return isPortBusy(port, BaseConstants.SHUTDOWN_PORT_PROP, exclude);
    }

    /**
     * Checks whether the specified port is already in use. The method
     * doesn't make something like "ping". It simply looks at all the registered
     * Server Instances to determine the {@literal port} they use.
     *
     * @param port the {@literal port} to check
     * @param portPropName
     * @param exclude the embedded project, whose port is not taken
     * into account
     * @return {@literal true } if the given port is already in use.
     * {@literal false} otherwise
     */
    public static boolean isPortBusy(int port, String portPropName, Project exclude) {
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
                String p = InstanceProperties.getInstanceProperties(uri).getProperty(portPropName);
                if (p == null) {
                    continue;
                }
                if (Integer.parseInt(p) == port) {
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
     *
     * @param webProject
     * @return
     */
    public static J2eeModuleProvider getJ2eeModuleProvider(Project webProject) {
        return webProject.getLookup().lookup(J2eeModuleProvider.class);
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
     * Checks whether the specified project is actually an embedded server
     * project. Every embedded server project has an object of type
     * {@link ServerInstanceProperties} in its lookup and the method {@code isEmbedded() }
     * of the object of type {@link ServerSpecifics } return {@code true }
     *
     * @param p a project to be checked
     * @return {@literal true } if the specified project is an embedded server
     * project.
     */
    public static boolean isEmbedded(Project p) {
        ServerInstanceProperties sip = getServerProperties(p);
        
        if ( sip == null ) {
            return false;
        }
        String serverId = sip.getServerId();
        return ((EmbeddedServerSpecifics)getServerSpecifics(serverId)).isEmbedded();
    }
    /**
     *
     * @param manager
     * @return
     */
    private static BaseJ2eePlatformImpl getJ2eePlatform(DeploymentManager manager) {
        EmbJ2eePlatformFactory f = new EmbJ2eePlatformFactory();
        return (BaseJ2eePlatformImpl) f.getJ2eePlatformImpl(manager);
//        return (BaseJ2eePlatformImpl) f.getJ2eePlatformImpl(manager);
    }

    /**
     * Returns a set of supported profiles by the specified embedded server.
     *
     * @param serverProject an embedded server project
     * @return a set of supported profiles
     */
    public static Set<Profile> getJavaEEProfiles(Project serverProject) {
        DeploymentManager manager = managerOf(serverProject);
        return getJ2eePlatform(manager).getSupportedProfiles();
    }

    /**
     * Loads a properties file which represents embedded server configurations
     * and returns the corresponding {@literal Properties} instance.
     *
     * @param serverProject en embedded server project
     * @return an instance of properties that corresponds the server
     * configuration file
     */
    public static Properties loadServerProperties(Project serverProject) {
        FileObject fo = serverProject.getProjectDirectory().getFileObject(EmbConstants.REG_WEB_APPS_FOLDER);
        return loadProperties(fo, EmbConstants.INSTANCE_PROPERTIES_FILE);
    }

    private static Properties loadProperties(FileObject propFolder, String fileName) {
        FileObject fo = propFolder.getFileObject(fileName);
        if (fo == null) {
            return null;
        }

        final Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(fo.getPath());) {
            props.load(fis);
            //fis.close();
            return props;
        } catch (IOException ioe) {
            LOG.log(Level.INFO, "loadProperties() of " + fileName, ioe);
            return null;
        }
    }

    /**
     * Loads the content of the {@literal context.properties} for the
     * given {@literal FileObject}.
     *
     * @param list
     * @param serverProject
     * @param fo file object which represents
     * {@literal context config} file
     * @return an object of type {@literal Properties}
     */
/*    public static Properties loadWebAppConfProperties(Project serverProject,FileObject fo) {
        if (fo == null || fo.isFolder() ) {
            return null;
        }
        if ("xml".equals(fo.getExt() ) ) {
            
        }
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(fo.getPath());) {
            props.load(fis);
            fis.close();
        } catch (IOException ioe) {
            LOG.log(Level.INFO, "loadProperties() of " + fo.getNameExt(), ioe);
            props = null;
        }
        return props;
    }
*/    
    public static List<String> toList(String list) {
        List<String> result = new ArrayList<>();
        if (list == null) {
            return result;
        }

        StringTokenizer tokenizer = new StringTokenizer(list, ",");
        while (tokenizer.hasMoreTokens()) {
            result.add(tokenizer.nextToken());
        }
        return result;
    }

    public static String createCommand(String command, String contextPath, String path) {
        StringBuilder sb = new StringBuilder();
        sb.append("cmd=");
        sb.append(command);
        sb.append("&cp=");
        sb.append(encode(contextPath));
        sb.append("&dir=");
        sb.append(encode(path));
        return sb.toString();
    }

    public static String createCommand(String command, String contextPath, String path, String projType) {
        StringBuilder sb = new StringBuilder();
        sb.append("cmd=");
        sb.append(command);
        sb.append("&cp=");
        sb.append(encode(contextPath));
        sb.append("&dir=");
        sb.append(encode(path));
        sb.append("&projtype=");
        sb.append(projType);

        return sb.toString();
    }
}
