package org.netbeans.plugin.support.embedded.jetty;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.eclipse.jetty.webapp.WebAppContext;

import org.netbeans.plugin.support.embedded.jetty.WebAppInfo.Info;

/**
 *
 * @author V. Shyshkin
 */
public class PathResolver {

    protected static void warning(String msg) {
        System.err.println("______________________________________________________");
        System.err.println("   WARNING: CommandManager (PathResolver): " + msg);
        System.err.println("______________________________________________________");
    }

    /**
     *
     * @param warPath a getWar() value of a WebAppContext may be a simple name
     * like "WebApplication1" or a file system path.
     * @return
     */
    public static String[] dtPathFor(String warPath) {
        String[] result = new String[2];
        String p = warPath.trim().replace("\\", "/");

        String projDir;
        String buildDir;

        String ref;

        if (p.startsWith("${")) {

            int idx = p.lastIndexOf("}");
            if (idx < 0) {
                warning("The right curly brace is omitted '" + p + "'");
                result[1] = warPath;
                return result;
            }
            // Calc the project dir or .war file
            ref = p.substring(2, idx);
        } else {

            int idx = p.lastIndexOf("/");
            if (idx != p.indexOf("/")) {
                warning("Too many path elements in '" + p + "'");
                result[1] = warPath;
                return result; // original value
            }
            ref = idx > 0 ? p.substring(0, idx) : p;
        }
        // tries to find a file or a folder in the Web Applications
        // folder. It may be an internal web project or a properties file
        // with .webfef or .warref or .htmref extenssion 
        File refFile = getRef(ref);

        if (refFile == null || !refFile.exists()) {
            warning("No project dir found. '" + ref + "' is not registered");
            result[1] = warPath;
            return result; // original value
        }

        projDir = getProjectDirByRef(refFile);
        if (projDir == null) {
            warning("No project dir found. '" + ref + "' is not registered");
            result[1] = warPath;
            return result;
        }

        File f = new File(projDir);
        if (f.isFile() && f.getName().endsWith(".war")) {
            // war file => this is a result
            result[1] = projDir;
            // try to find out an actual contextPath
            result[0] = getContextPathFromWar(f);
            return result;

        } else if (f.isFile()) {
            result[1] = projDir;
            return result;
        }

        if (Utils.isMavenProject(projDir)) {
            buildDir = Utils.getMavenBuildDir(projDir);
        } else if (refFile.getPath().endsWith(".htmref")) {
            Properties props = Utils.loadHtml5ProjectProperties(projDir);

            String siteRoot = Utils.resolve(Utils.HTML5_SITE_ROOT_PROP, props);
            if (siteRoot == null) {
                siteRoot = Utils.HTML5_DEFAULT_SITE_ROOT_PROP;
            }
            buildDir = projDir + "/" + siteRoot;
            // Actual contextPath
            //String cp = Utils.resolve(Utils.HTML5_WEB_CONTEXT_ROOT_PROP, props);
            //result[0] = cp;
        } else {
            buildDir = projDir + "/build/web";
        }

        if (buildDir != null) {
            result[1] = buildDir;
            //try extract an actual contextpath
            result[0] = Utils.getContextPropertiesByBuildDir(buildDir).getProperty(Utils.CONTEXTPATH_PROP);

        } else {
            result[1] = warPath;
        }
        return result;

    }

    static String getProjectDirByRef(File ref) {
        String result = null;
        if (ref != null && ref.isDirectory()) {
            return ref.getAbsolutePath();
        }
        if (ref != null) {
            Properties props = new Properties();
            try (FileInputStream in = new FileInputStream(ref)) {
                props.load(in);
                result = props.getProperty("webAppLocation");
            } catch (FileNotFoundException ex) {
                Logger.getLogger(PathResolver.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(PathResolver.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result;
    }

    /**
     * Tries to find a file or a folder in the {@literal Web Applications}
     * folder by it's name.
     *
     * @param ref may be a simple name or a name with extention
     * {@literal .warref} or {@literal .webref} or {@literal htmref}
     * @return the {@literal java.io.File} object or {@literal null}
     */
    static File getRef(String ref) {
        File result = null;
        File[] list = new File("./" + Utils.REG_WEB_APPS_FOLDER).listFiles();
        File resultFile = null;
        //
        // search when exactly equal
        //
        for (File f : list) {
            if (f.getName().equals(ref)) {
                resultFile = f;
                break;
            }
        }

        if (resultFile == null && !ref.endsWith(Utils.WEB_REF) && !ref.endsWith(Utils.WAR_REF)
                && !ref.endsWith(Utils.HTM_REF)) {
            for (File f : list) {
                if (f.getName().equals(ref + Utils.WEB_REF)
                        || f.getName().equals(ref + Utils.WAR_REF)
                        || f.getName().equals(ref + Utils.HTM_REF)) {
                    result = f;
                    break;
                }
            }
        }
        return result;
    }

    public static String getAppNameByJarEntry(String entryName) {
        String appProps = "META-INF/context.properties";
        
        if (entryName.startsWith(Utils.WEB_APPS_PACK) && entryName.endsWith("WEB-INF/" + Utils.WEBAPP_CONFIG_FILE)) {
            appProps = "WEB-INF/" + Utils.WEBAPP_CONFIG_FILE;
        }
        return entryName.substring(Utils.WEB_APPS_PACK.length() + 1, entryName.indexOf(appProps) - 1);
    }

    /**
     * Returns an instance of {@literal Map<String,Properties>} where a
     * contextPath of a web application is a {@literal key} , and the
     * {@literal Properties} object is a {@literal  value}.
     *
     * The Properties objects contains a property with a name specified by the
     * constant {@link org.embedded.ide.jetty.Utils#RUNTIME_APP_PATH_PROP}. It's
     * value represents a runtime path of the web application in the
     * {@literal web-apps-pack}.
     *
     *
     * For each web application packaged in the server jar defines it's
     * contextPath.
     * <ul>
     * <li>
     * If there is file named as {@literal jetty-web.xml}, the contextPath
     * extracted from it
     * </li>
     * <li>
     * If a file named as {@literal context.properties} (it's an HTML5
     * application) exists in the folder {@literal META-INF} , the contextPath
     * extracted from it
     * </li>
     * <li>
     * Otherwise the name of the entry (application name or war file name) is
     * considered to be a contextPath.
     * </li>
     * </ul>
     *
     * If {@literal jetty-web.xml} exists then the contextPath
     * {@literal META-INF/context.properties} if exists
     *
     * @param url
     * @return Returns an instance of {@literal Map<String,Properties>}
     */
/*    public static Map<String, Properties> getMapOfPropsByContextPaths(URL url) {

        System.out.println("---- getMapOfPropsByContextPaths--- url=" + url);

        Map<String, Properties> map = new HashMap<>();

        //ContextHandlerCollection chc = getServer().getChildHandlerByClass(ContextHandlerCollection.class);
        String jarPath = url.getPath().substring(5, url.getPath().indexOf("!"));
        System.out.println("---- getMapOfPropsByContextPaths--- jarPath=" + jarPath);

        try {
            JarFile jarFile = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
            Enumeration<JarEntry> entries = jarFile.entries();
            Set<JarEntry> apps = new HashSet<>(); // registered as .webref or .htmref, but not .warref
            Set<JarEntry> wars = new HashSet<>(); // registered as  .warref (may be without context config file) 
            while (entries.hasMoreElements()) {
                JarEntry e = entries.nextElement();
                System.out.println("---- getMapOfPropsByContextPaths--- entry.name=" + e.getName());
                if (e.getName().startsWith(Utils.WEB_APPS_PACK) && e.getName().endsWith("WEB-INF/" + Utils.WEBAPP_CONFIG_FILE)) {
                    System.out.println("---- getMapOfPropsByContextPaths--- ADDED entry.name=" + e.getName());
                    apps.add(e);
                } else if (e.getName().startsWith(Utils.WEB_APPS_PACK) && e.getName().endsWith("META-INF/context.properties")) {
                    //
                    // Html5 project
                    //
                    System.out.println("---- getMapOfPropsByContextPaths--- ADDED entry.name=" + e.getName());
                    apps.add(e);
                } else if (e.getName().startsWith(Utils.WEB_APPS_PACK)) {
                    String warName = e.getName().substring(Utils.WEB_APPS_PACK.length() + 1);
                    //
                    // check whether warName represents a folder or a .war file
                    // We do not consider folders which has no config file
                    //
                    if (!warName.contains("/")) {
                        wars.add(e);
                    }
                }
            }//while

            //
            // Now apps contains Jar Entries every of which corresponds 
            // to s jetty-web.xml (std web project) or context.properties fo
            // Html5 applications
            //
            for (JarEntry e : apps) {
                String appName = getAppNameByJarEntry(e.getName());
                final Properties props;
                try (InputStream is = PathResolver.class.getClassLoader().getResourceAsStream(e.getName())) {
                    if (e.getName().startsWith(Utils.WEB_APPS_PACK) && e.getName().endsWith("WEB-INF/" + Utils.WEBAPP_CONFIG_FILE)) {
                        props = Utils.getContextProperties(is);
                    } else {
                        props = new Properties();
                        props.load(is);
                    }
                }
                String contextPath = props.getProperty(Utils.CONTEXTPATH_PROP);
                String appPath = PathResolver.class.getClassLoader().getResource(Utils.WEB_APPS_PACK + "/" + appName).toExternalForm();
                Properties mapValue = new Properties();
                mapValue.put(Utils.RUNTIME_APP_PATH_PROP, appPath);
                map.put(contextPath, mapValue);

            }//for
            //
            // We must treat .warref separatly as they may not contain 
            // context config file
            //
            for (JarEntry e : wars) {
                String appName = e.getName().substring(Utils.WEB_APPS_PACK.length() + 1);
                String contextPath = appName;

                String appPath = PathResolver.class.getClassLoader().getResource(Utils.WEB_APPS_PACK + "/" + appName).toExternalForm();
                Properties mapValue = new Properties();
                mapValue.put(Utils.RUNTIME_APP_PATH_PROP, appPath);
                map.put(contextPath, mapValue);
            }

        } catch (IOException ex) {
            Logger.getLogger(CommandManager.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return map;

    }

    public static Map<String, Properties> getMapOfPropsByContextPaths() {
        System.out.println("------- getMapOfPropsByContextPaths() -------");
        Map<String, Properties> map = new HashMap<>();

        String webappsDir = Utils.WEBAPPS_DEFAULT_DIR_NAME;
        Properties serverProps = Utils.loadServerProperties(isDevelopmentMode());
        if (serverProps != null && serverProps.getProperty(Utils.WEBAPPS_DIR_PROP) != null) {
            webappsDir = serverProps.getProperty(Utils.WEBAPPS_DIR_PROP);
        }
        File dir = new File("./" + webappsDir);
        System.out.println("DIR: " + dir.getAbsolutePath());
        if (!dir.exists()) {
            return map;
        }
        File[] fileList = dir.listFiles();
        for (File f : fileList) {
            System.out.println("getMapOfPropsByContextPaths: fileList f: " + f.getAbsolutePath());

            String contextPath;
            Properties props = new Properties();
            if (f.isDirectory()) {
//                System.out.println("CM: f.getName=" + f.getName());
                contextPath = "/" + f.getName();
                File propFile = new File(f.getAbsolutePath() + "/META-INF/context.properties");// + Utils.WEBAPP_CONFIG_FILE);
                if (propFile.exists()) {
                    //
                    // It's an Html5 project
                    // 
                    try (FileInputStream fis = new FileInputStream(propFile)) {
                        props.load(fis);
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(CommandManager.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(CommandManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    contextPath = props.getProperty(Utils.CONTEXTPATH_PROP);
                } else {
                    File jettyXmlFile = new File(f.getAbsolutePath() + "/WEB-INF/" + Utils.WEBAPP_CONFIG_FILE);
                    if (jettyXmlFile.exists()) {
                        props = Utils.getContextProperties(jettyXmlFile);
                        contextPath = props.getProperty(Utils.CONTEXTPATH_PROP);
                    }
                }
            } else if (f.getName().endsWith(".war")) {
                contextPath = getContextPathFromWar(f);
//                System.out.println("CM: contextPath=" + contextPath);

            } else {
                continue;
            }

            Properties mapValue = new Properties();
            mapValue.put(Utils.RUNTIME_APP_PATH_PROP, f.getAbsolutePath());
            map.put(contextPath, mapValue);

        }
        return map;
    }
*/
/*    public static String getContextPathFromWar(String war) {
        return getContextPathFromWar(new File(war));

    }
*/
    public static String getContextPathFromWar(File war) {
        String appName = war.getName().substring(0, war.getName().length() - 4);
        String contextPath = "/" + appName;
        Properties props = new Properties();

        try {
            ZipFile zipFile = new ZipFile(war);
            ZipEntry e = zipFile.getEntry("WEB-INF/" + Utils.WEBAPP_CONFIG_FILE);
            if (e == null) {
                e = zipFile.getEntry("WEB-INF/web-jetty.xml");
            }

            if (e != null) {
                try (InputStream in = zipFile.getInputStream(e)) {
                    props = Utils.getContextProperties(in);
                }
            }

            if (e == null) {
                e = zipFile.getEntry("META-INF/context.properties");
                if (e != null) {
                    try (InputStream in = zipFile.getInputStream(e)) {
                        props.load(in);
                    }
                }
            }
            if (e != null && props.getProperty(Utils.CONTEXTPATH_PROP) != null) {
                contextPath = props.getProperty(Utils.CONTEXTPATH_PROP);
            }
        } catch (IOException ex) {
            Logger.getLogger(PathResolver.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("extractContextPath exception " + ex.getMessage());
        }

        return contextPath;
    }


    /**
     * In production mode we must return a simple name of the war file without
     * extention.
     *
     * @return
     */
/*    public static String rtPathFor(String contextPath) {
        Map<String, Properties> map;

        URL url = PathResolver.class.getClassLoader().getResource(Utils.WEB_APPS_PACK);
        
        if (url != null) {
            map = getMapOfPropsByContextPaths(url);
        } else {
            map = getMapOfPropsByContextPaths();
        }
        System.out.println("== rtPathFor  map=" + map + "; cp=" + contextPath);
        if (map.get(contextPath) == null) {
            return null;
        }
        return map.get(contextPath).getProperty(Utils.RUNTIME_APP_PATH_PROP);
    }
*/
    public static String[] rtPathFor(WebAppContext webapp) {
        
        String contextPath = webapp.getContextPath();
        String warPath = webapp.getWar();
        //String webappsDir = null;
        String webappsDir = Utils.WEBAPPS_DEFAULT_DIR_NAME;  
        
        
        
        URL url = PathResolver.class.getClassLoader().getResource(Utils.WEB_APPS_PACK);        
        Set<Info> infoSet = new HashSet<>();        
        if  ( url != null ) {
            WebAppInfo webappInfo;
            try {
                webappInfo = new WebAppInfo(url);
                infoSet = webappInfo.buildInfoSet(Utils.WEB_APPS_PACK);            
            } catch (IOException ex) {
                Logger.getLogger(PathResolver.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            WebAppInfo webappInfo = new WebAppInfo(new File("./"));
            infoSet = webappInfo.buildInfoSet(webappsDir);            
        }
        boolean found = false;
        for ( Info info : infoSet) {
            if ( info.getWebAppName().equals(webapp.getWar())) {
                contextPath = info.getContextPath();
                warPath = info.getWarPath();
                found = true;
                break;
            }
        }
        if ( ! found ) {
            Properties props = Utils.getContextPropertiesByBuildDir(webapp.getWar());
            if ( props != null  ) {
                String s = props.getProperty(Utils.CONTEXTPATH_PROP);
                if ( s != null) {
                    contextPath = s;
                }
            }
                
        }
        return new String[] {contextPath,warPath};
    }

    
}
