package org.embedded.ide.jetty;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.SourceVersion;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 *
 */
public class Utils {

    private static final Logger LOG = Logger.getLogger(Utils.class.getName());

    public static final String WEBAPPS_DEFAULT_DIR_NAME = "web-apps";
    public static final String WEBAPPS_DIR_PROP = "deployWebapps";

    public static final String DEVELOPMENT_MODE_XML_FILE = "development-mode.xml";
    public static final String DEPLOY_WEB_PROJECTTYPE = "web.project";
    public static final String DEPLOY_HTML5_PROJECTTYPE = "web.clientproject";
    public static final String HTML5_PROJECTTYPE = "org.netbeans.modules.web.clientproject";

    public static final String HTML5_SITE_ROOT_PROP = "site.root.folder";
    public static final String HTML5_DEFAULT_SITE_ROOT_PROP = "public_html";
    public static final String HTML5_WEB_CONTEXT_ROOT_PROP = "web.context.root";

    public static final String WEB_REF = ".webref";
    public static final String WAR_REF = ".warref";
    public static final String HTM_REF = ".htmref";
    public static final String RUNTIME_APP_PATH_PROP = "runtime.app.path";

    /**
     * Keys for Server Properties
     */
    public static String SERVER_ID_PROP = "server-id";
    public static final String DISPLAY_NAME_PROP = "displayName";
    public static final String HTTP_PORT_PROP = "httpportnumber";

    public static final String HOST_PROP = "host";
    public static final String DEBUG_PORT_PROP = "debug_port";
    public static final String INCREMENTAL_DEPLOYMENT = "incrementalDeployment";
    //public static final String WEBAPPLICATIONS_FOLDER = "server-instance-config";
    public static final String REG_WEB_APPS_FOLDER = "reg-web-apps";

    public static final String CONTEXTPATH_PROP = "contextPath";
    public static final String WEBAPP_CONFIG_FILE = "jetty-web.xml";
    public static final String WEB_APPS_PACK = "web-apps-pack";

    public static final String INSTANCE_PROPERTIES_FILE = "server-instance.properties";
    public static final String ANTI_LOCK_PROP_NAME = "antiResourceLocking";

    public static final String SERVER_CONFIG_FOLDER = "server-config";
    public static final String SERVER_PROJECT_FOLDER = "server-project";
    public static final String INSTANCE_PROPERTIES_PATH = "src/main/resources/" + INSTANCE_PROPERTIES_FILE;
    
    public static boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows ");
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
    
    public static String getUserDir() {
        return System.getProperty("user.dir");
    }
    public static String getServerConfigDir() {
        Path p = Paths.get(System.getProperty("user.dir")).getParent();
        return Paths.get(p.toString(), SERVER_CONFIG_FOLDER ).toString();
    }
    public static String getServerProjectDir() {
        Path p = Paths.get(System.getProperty("user.dir")).getParent();
        return Paths.get(p.toString(), SERVER_PROJECT_FOLDER ).toString();
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

    private static boolean resolved(String value) {
        if (value == null || !value.trim().contains("${")) {
            return true;
        }
        return false;
    }

    /**
     * Test whether a given string is a valid Java identifier.
     *
     * @param id string which should be checked
     * @return <code>true</code> if a valid identifier
     * @see SourceVersion#isIdentifier
     * @see SourceVersion#isKeyword
     */
    public static boolean isJavaIdentifier(String id) {
        if (id == null) {
            return false;
        }
        return SourceVersion.isIdentifier(id) && !SourceVersion.isKeyword(id);
    }

    /**
     * (@code Development mode} only.
     *
     * @param buildPath
     * @return
     */
    /*    public static boolean isBuildOfMavenProject(String buildPath) {
     boolean b = false;
        
     File buildPathFile = new File(buildPath);
     if (buildPathFile.exists() && buildPathFile.isDirectory()) {
     File p = buildPathFile.getParentFile();
     if (p.exists()) {
     p = p.getParentFile();
     if (new File(p.getPath() + "/pom.xml").exists()) {
     b = true;
     }
     }
     }
     return b;
     }
     */
    public static boolean isBuildOfMavenProject(String buildPath) {
        Path p = Paths.get(buildPath);
        System.out.println("Utils.isBuildOfMavenProject buildPath=" + buildPath + "; p=" + p);

        String nm = p.getFileName().toString();
        p = p.getParent();
        System.out.println("Utils.isBuildOfMavenProject nm=" + nm + "; p=" + p);
        Path r = p.resolve(nm + ".war");

        if (!Files.exists(r)) {
            return false;
        }
        r = p.getParent().resolve("pom.xml");

        if (!Files.exists(r)) {
            return false;
        }
        return true;
    }

    /**
     * (@code Development mode} only.
     *
     * @param projDir
     * @return
     */
    public static boolean isMavenProject(String projDir) {
        return new File(projDir + "/pom.xml").exists();
    }

    /**
     * {@literal Development mode} only.
     *
     * @param projDir
     * @return
     */
    public static String getMavenBuildDir(String projDir) {
        Path target = Paths.get(projDir + "/target");
        FilenameFilter filter = new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".war");
            }

        };

        File[] list = target.toFile().listFiles(filter);
        if (list == null || list.length == 0) {
            return null;
        }
        String result = null;
        String targetDir = target.toString();
        for (File f : list) {
            if (f.isDirectory()) {
                continue;
            }
            String nm = f.getName();
            String nmNoExt = nm.substring(0, nm.length() - 4);
            File webInf = new File(targetDir + "/" + nmNoExt + "/WEB-INF");
            if (webInf.exists() && webInf.isDirectory()) {
                result = nmNoExt;
                break;
            }
        }//for

        if (result != null) {
            result = targetDir + "/" + result;
        }
        return result;

    }
    public static String getHtml5BuildDir(String webDir) {
        String path = new File(webDir).getAbsolutePath();

        Properties props = Utils.loadHtml5ProjectProperties(webDir);
        if (props == null) {
            return path;
        }
        String siteRoot = Utils.resolve(Utils.HTML5_SITE_ROOT_PROP, props);
        if (siteRoot == null) {
            siteRoot = Utils.HTML5_DEFAULT_SITE_ROOT_PROP;
        }
        return new File(path + "/" + siteRoot).getAbsolutePath();
    }

    private static String findProjectDir(String warPath) {
        File f = null;
        File p = new File(warPath);
        while (true) {
            p = p.getParentFile();
            if (p == null) {
                return null;
            }
            f = new File(p.getAbsolutePath() + "/nbproject/projectxml");
            if (f.exists()) {
                break;
            }
        }
        return p.getAbsolutePath();
    }

    private static Properties loadWebAppProperties(String warPath, boolean html5) {
        System.err.println("loadWebAppProperties warPath=" + warPath);
        Properties result = new Properties();
        File f = null;
        File p = new File(warPath);
        while (true) {
            p = p.getParentFile();
            if (p == null) {
                return result;
            }
            f = new File(p.getAbsolutePath() + "/nbproject/project.properties");
            if (f.exists()) {
                break;
            }
        }

        Properties projProps = loadProperties(f);
        if (projProps.getProperty(HTML5_SITE_ROOT_PROP) == null
                || projProps.getProperty(HTML5_WEB_CONTEXT_ROOT_PROP) == null) {
            return result;
        }

        result.setProperty(Utils.CONTEXTPATH_PROP, projProps.getProperty(HTML5_WEB_CONTEXT_ROOT_PROP));
        return result;

    }

    public static boolean isHtml5Project(String warPath) {
        String projDir = findProjectDir(warPath);
        if (projDir == null) {
            return false;
        }
        String type = getAntBasedProjectType(projDir);
        if (HTML5_PROJECTTYPE.equals(type)) {
            return true;
        }
        return false;
    }

    public static String getAntBasedProjectType(String projDir) {
        File f = new File(projDir + "/nbproject/project.xml");
        if (!f.exists()) {
            return null;
        }
        return getAntBasedProjectType(f);
    }

    public static String getAntBasedProjectType(File xmlFile) {
        String result = null;
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
            //out("Utils: getContextPropertiesByBuildDir EXCEPTION " + ex.getMessage());
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public static Properties getContextPropertiesByBuildDir(String warPath) {
        File f = new File(warPath + "/WEB-INF/jetty-web.xml");
        System.out.println("^^^ getContextProperties 1 " + warPath);
        if (!f.exists()) {
            System.out.println("^^^ getContextProperties 2 " + warPath);
            f = new File(warPath + "/WEB-INF/web-jetty.xml");
            if (!f.exists()) {
                System.out.println("^^^ getContextProperties 3 " + warPath);
                return loadWebAppProperties(warPath, true);
            }
        }
        return getContextProperties(f);
    }

    public static Properties getContextProperties(File xmlFile) {
        Properties result = new Properties();
        if ( ! xmlFile.exists() ) {
            return result;
        }
        try {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setValidating(false);
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            builder.setEntityResolver(new ParserEntityResolver());
            Document doc = builder.parse(xmlFile);

            NodeList nl = doc.getDocumentElement().getElementsByTagName("Set");

            if (nl != null) {
                int found = 0;
                for (int i = 0; i < nl.getLength(); i++) {
                    Element el = (Element) nl.item(i);
                    switch (el.getAttribute("name")) {
                        case "contextPath":
                            result.setProperty("contextPath", el.getTextContent());
                            found++;
                            break;
                        case "war":
                            result.setProperty("war", el.getTextContent());
                            found++;
                            break;
                        case "copyWebDir":
                            result.setProperty("copyWebDir", el.getTextContent());
                            found++;
                            break;
                    }
                    if (found > 2) {
                        break;
                    }
                }
            }

        } catch (IOException | DOMException | ParserConfigurationException | SAXException ex) {
            //out("Utils: getContextPropertiesByBuildDir EXCEPTION " + ex.getMessage());
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public static Properties getContextProperties(InputStream xmlFile) {
        //final Properties props = new Properties();
        Properties result = new Properties();
        try {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setValidating(false);
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            builder.setEntityResolver(new ParserEntityResolver());
            Document doc = builder.parse(xmlFile);

            NodeList nl = doc.getDocumentElement().getElementsByTagName("Set");

            if (nl != null) {
                int found = 0;
                for (int i = 0; i < nl.getLength(); i++) {
                    Element el = (Element) nl.item(i);
                    switch (el.getAttribute("name")) {
                        case "contextPath":
                            result.setProperty("contextPath", el.getTextContent());
                            found++;
                            break;
                        case "war":
                            result.setProperty("war", el.getTextContent());
                            found++;
                            break;
                        case "copyWebDir":
                            result.setProperty("copyWebDir", el.getTextContent());
                            found++;
                            break;
                    }
                    if (found > 2) {
                        break;
                    }
                }
            }

        } catch (IOException | DOMException | ParserConfigurationException | SAXException ex) {
            //out("Utils: getContextPropertiesByBuildDir EXCEPTION " + ex.getMessage());
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }

    public static Properties loadProperties(File f) {

        final Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream(f)) {
            props.load(fis);
            fis.close();
        } catch (IOException ioe) {
            LOG.log(Level.INFO, "loadServerProperties()", ioe);
            return null;
        }
        return props;

    }

    public static Properties loadServerProperties(boolean developmentMode) {
        File f = new File("./" + INSTANCE_PROPERTIES_PATH);
        Properties props = new Properties();
        if (developmentMode) {

            try (FileInputStream fis = new FileInputStream(f)) {
                props.load(fis);
                fis.close();
            } catch (IOException ioe) {
                LOG.log(Level.INFO, "loadServerProperties()", ioe);
                return null;

            }
        } else {
            InputStream is = Utils.class
                    .getClassLoader().getResourceAsStream(Utils.INSTANCE_PROPERTIES_FILE);

            try {
                props.load(is);
                is.close();
            } catch (IOException ex) {
                Logger.getLogger(CommandManager.class.getName()).log(Level.SEVERE, null, ex);
            }
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
            LOG.log(Level.INFO, "loadServerProperties()", ioe);
            return null;
        }
    }

    public static Map<String, File> getRegisteredApps() {
        final Map<String, File> map = new HashMap<>();
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                boolean accept = false;
                File f = new File(dir.getAbsolutePath() + "/" + name);

                if (f.isDirectory() || name.endsWith(WAR_REF) || name.endsWith(WEB_REF) || name.endsWith(HTM_REF)) {
                    accept = true;
                }
                return accept;
            }

        };
        File webappFolder = new File("./" + REG_WEB_APPS_FOLDER);
        if ( ! webappFolder.exists() ) {
            return map;
        }
        
        for (File f : new File("./" + REG_WEB_APPS_FOLDER).listFiles(filter)) {
            String projName = f.getName();
            if (projName.endsWith(WAR_REF) || projName.endsWith(WEB_REF) || projName.endsWith(HTM_REF)) {
                projName = projName.substring(0, projName.length() - 7);
            }
            map.put(projName, f);
            System.out.println("REGISTERED: " + projName);

        }
        return map;

    }

    public static class SAXHandler extends org.xml.sax.helpers.DefaultHandler {

        String content = null;
        private Properties props;

        public SAXHandler(Properties props) {
            this.props = props;
        }

        @Override
        //Triggered when the start of tag is found.
        public void startElement(String uri, String localName,
                String qName, Attributes attributes)
                throws SAXException {
            if ("Set".equals(qName) && attributes != null && attributes.getLength() > 0) {
                int found = 0;
                for (int i = 0; i < attributes.getLength(); i++) {
                    String value = attributes.getValue(i);

                    //String content = attributes.
                    switch (value) {
                        case "contextPath":
                            props.setProperty("contextPath", value);
                            found++;
                            break;
                        case "war":
                            props.setProperty("war", value);
                            found++;
                            break;
                        case "getCopyDir":
                            props.setProperty("getCopyDir", value);
                            found++;
                            break;
                    }
                    if (found >= 3) {
                        break;
                    }
                }
            }
        }

        @Override
        public InputSource resolveEntity(String pubid, String sysid)
                throws SAXException, IOException {
            return new InputSource(new ByteArrayInputStream(new byte[0]));
        }

    }//class SaxHandler

    public static class ParserEntityResolver implements EntityResolver {

        @Override
        public InputSource resolveEntity(String pubid, String sysid)
                throws SAXException, IOException {
            return new InputSource(new ByteArrayInputStream(new byte[0]));
        }
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
            System.out.println("stringOf EXCEPTION" + ex.getMessage()); //NOI18N

        } finally {
            try {
                is.close();
            } catch (IOException ex) {
                System.out.println("stringOf close() EXCEPTION" + ex.getMessage()); //NOI18N
            }
        }

        return sb.toString();
    }

}
