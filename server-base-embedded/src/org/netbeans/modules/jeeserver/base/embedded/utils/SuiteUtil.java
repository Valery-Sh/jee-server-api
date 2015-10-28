package org.netbeans.modules.jeeserver.base.embedded.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;
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
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.netbeans.modules.jeeserver.base.embedded.project.ServerSuiteProjectFactory;
import org.netbeans.modules.jeeserver.base.embedded.project.SuiteManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 *
 * @author V. Shyshkin
 */
public class SuiteUtil extends BaseUtil {

    private static final Logger LOG = Logger.getLogger(SuiteUtil.class.getName());


    public static FileObject getCommandManagerJar(Project server) {
        FileObject lib = null;
        if (BaseUtil.isAntProject(server)) {
            lib = server.getProjectDirectory().getFileObject(SuiteConstants.ANT_LIB_PATH + "/ext");
        }

        if (lib == null) {
            return null;
        }
        FileObject[] childs = lib.getChildren();
        FileObject result = null;
        String aid = SuiteManager.getManager(server).getInstanceProperties()
                .getProperty(BaseConstants.SERVER_ACTUAL_ID_PROP);

        String jarPrefix = aid + SuiteConstants.COMMAND_MANAGER_JAR_POSTFIX;
        for (FileObject fo : childs) {
            if (!fo.isFolder() && "jar".equals(fo.getExt())) {
                if (fo.getName().startsWith(jarPrefix)) {
                    result = fo;
                    break;
                }
            }
        }

        return result;
    }

    public static boolean isEmbeddedServer(Project proj) {
        return new ServerSuiteProjectFactory().isProject(proj.getProjectDirectory());
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
        Deployment d = Deployment.getDefault();
        boolean b = false;
        for (String uri : d.getServerInstanceIDs()) {
            InstanceProperties ip = InstanceProperties.getInstanceProperties(uri);
            if (ip == null) {
                continue;
            }
            String l = BaseUtil.getServerLocation(ip);
            if (l == null) {
                continue;
            }
            if (p == null) {
                continue;
            }

            if (Paths.get(l).equals(FileUtil.toFile(p.getProjectDirectory()).toPath())) {
                b = true;
                break;
            }
        }
        return b;

        //return getServerProperties(p) != null;
    }

    public static StringBuilder getServerInfo(Project server) {

        //Properties props = SuiteUtil.loadServerProperties(server);
        Properties props = null;
        StringBuilder sb = new StringBuilder();
        int len = 50;
        String sep = System.lineSeparator();
        File file = FileUtil.toFile(server.getProjectDirectory());

        //String s = new String();
        sb.append(replicate('=', len))
                .append(sep)
                .append("SERVER: ")
                .append("\t\t")
                //.append(props.getProperty(BaseConstants.SERVER_ID_PROP))
                .append(sep)
                .append("--- Name:\t\t")
                .append(file.getName())
                .append(sep)
                .append("--- Location:\t\t")
                .append(file.getAbsolutePath())
                .append(sep)
                .append("--- Host:\t\t")
                //.append(props.getProperty(BaseConstants.HOST_PROP))
                .append(sep)
                .append("--- Http Port:\t\t")
                //.append(props.getProperty(BaseConstants.HTTP_PORT_PROP))
                .append(sep)
                .append("--- Debug Port:\t\t")
                //.append(props.getProperty(BaseConstants.DEBUG_PORT_PROP))
                .append(sep)
                //.append("--- Incr Deploy:\t")
                //.append(props.getProperty(BaseConstants.INCREMENTAL_DEPLOYMENT))
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
    public static boolean isHttpPortBusy_OLD(int port, Lookup exclude) {
        return isPortBusy_OLD(port, BaseConstants.HTTP_PORT_PROP, exclude);
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
    public static boolean isHttpPortBusy(int port, String exclude) {
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
    public static boolean isDebugPortBusy_OLD(int port, Lookup exclude) {
        return isPortBusy_OLD(port, BaseConstants.DEBUG_PORT_PROP, exclude);
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
    public static boolean isDebugPortBusy(int port, String uri) {
        return isPortBusy(port, BaseConstants.DEBUG_PORT_PROP, uri);
    }

    public static boolean isShutdownPortBusy_OLD(int port, Lookup exclude) {
        return isPortBusy_OLD(port, BaseConstants.SHUTDOWN_PORT_PROP, exclude);
    }

    public static boolean isShutdownPortBusy(int port, String uri) {
        return isPortBusy(port, BaseConstants.SHUTDOWN_PORT_PROP, uri);
    }

    /**
     * Checks whether the specified port is already in use. The method doesn't
     * make something like "ping". It simply looks at all the registered Server
     * Instances to determine the {@literal port} they use.
     *
     * @param port the {@literal port} to check
     * @param portPropName
     * @param exclude the embedded project, whose port is not taken into account
     * @return {@literal true } if the given port is already in use.
     * {@literal false} otherwise
     */
    public static boolean isPortBusy(int port, String portPropName, String exclude) {
        boolean result = false;
        String[] uris = Deployment.getDefault().getServerInstanceIDs();

        for (String uri : uris) {
            if (uri.equals(exclude)) {
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
     * Checks whether the specified port is already in use. The method doesn't
     * make something like "ping". It simply looks at all the registered Server
     * Instances to determine the {@literal port} they use.
     *
     * @param port the {@literal port} to check
     * @param portPropName
     * @param exclude the embedded project, whose port is not taken into account
     * @return {@literal true } if the given port is already in use.
     * {@literal false} otherwise
     */
    public static boolean isPortBusy_OLD(int port, String portPropName, Lookup exclude) {
        boolean result = false;
        String[] uris = Deployment.getDefault().getServerInstanceIDs();
        String excludeUri = null;
        if (exclude != null) {
            excludeUri = managerOf(exclude).getUri();
        }
        for (String uri : uris) {
            if (uri.equals(exclude)) {
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

    public static String getSuiteProjectLocation(InstanceProperties ip) {
        return ip.getProperty(SuiteConstants.SUITE_PROJECT_LOCATION);
    }

    public static void setSuiteProjectLocation(Map<String, String> ip, String location) {
        ip.put(SuiteConstants.SUITE_PROJECT_LOCATION, location);
    }

    public static void setSuiteProjectLocation(InstanceProperties ip, String location) {
        ip.setProperty(SuiteConstants.SUITE_PROJECT_LOCATION, location);        
        //ip.setProperty(SuiteConstants.SUITE_PROJECT_LOCATION, location.replaceAll("\\", "/"));
    }

    /**
     * Checks whether the specified project is actually an embedded server
     * project. Every embedded server project has an object of type
     * {@link ServerInstanceProperties} in its lookup and the method {@code isEmbedded()
     * }
     * of the object of type {@link ServerSpecifics } return {@code true }
     *
     * @param p a project to be checked
     * @return {@literal true } if the specified project is an embedded server
     * project.
     */
    public static boolean isEmbedded(Project p) {
        Deployment d = Deployment.getDefault();
        boolean b = false;
        for (String uri : d.getServerInstanceIDs()) {
            InstanceProperties ip = InstanceProperties.getInstanceProperties(uri);
            if (ip == null) {
                continue;
            }
            String l = BaseUtil.getServerLocation(ip);;
            if (l == null) {
                continue;
            }
            if (p == null) {
                continue;
            }
            if (!Paths.get(l).equals(FileUtil.toFile(p.getProjectDirectory()).toPath())) {
                continue;
            }

            String serverId = ip.getProperty(BaseConstants.SERVER_ID_PROP);
            if (getServerSpecifics(serverId) instanceof EmbeddedServerSpecifics) {
                b = true;
                break;
            }
        }
        return b;
    }

    /*    public static boolean isEmbedded(Project p) {
     ServerInstanceProperties sip = getServerProperties(p.getLookup());
        
     if ( sip == null ) {
     return false;
     }
     String serverId = sip.getServerId();
     return ((EmbeddedServerSpecifics)getServerSpecifics(serverId)).isEmbedded();
     }
     */
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
     * @param context
     * @return a set of supported profiles
     */
    public static Set<Profile> getJavaEEProfiles(Lookup context) {
        DeploymentManager manager = managerOf(context);
        return getJ2eePlatform(manager).getSupportedProfiles();
    }

    public static String getSuiteUID(FileObject suite) {
        String uid = null;
        FileObject suitePropsFo = suite.getFileObject(SuiteConstants.SUITE_PROPERTIES_LOCATION);
        Properties suiteProps = new Properties();
        if (suitePropsFo != null) {
            suiteProps = BaseUtil.loadProperties(suite.getFileObject(SuiteConstants.SUITE_PROPERTIES_LOCATION));
            uid = suiteProps.getProperty(SuiteConstants.UID_PROPERTY_NAME);
        }
        return uid;
    }

    /**
     * Loads a properties file which represents embedded server configurations
     * and returns the corresponding {@literal Properties} instance.
     *
     * @param serverProject en embedded server project
     * @return an instance of properties that corresponds the server
     * configuration file
     */
/*    public static Properties loadServerProperties(Project serverProject) {
        FileObject fo = serverProject.getProjectDirectory().getFileObject(SuiteConstants.REG_WEB_APPS_FOLDER);
        return loadProperties(fo, SuiteConstants.INSTANCE_PROPERTIES_FILE);
    }
*/
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
     * Loads the content of the {@literal context.properties} for the given
     * {@literal FileObject}.
     *
     * @param list
     * @param serverProject
     * @param fo file object which represents {@literal context config} file
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
