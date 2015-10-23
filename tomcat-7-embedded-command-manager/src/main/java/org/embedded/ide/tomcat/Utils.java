package org.embedded.ide.tomcat;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import org.apache.catalina.startup.Tomcat;

import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Utils {

    public static final String UNPACKED_WEB_APPS_PACK_PREFIX = "tomcat7_embedded_unpacked_web_apps_pack_";
    public static final Logger LOG = Logger.getLogger(Utils.class.getName());
    public static final String ANTI_LOCK_PROP_NAME = "antiJARLocking";
    public static final String WEBAPPS_DIR_PROP = "deployWebapps";
    public static final String WEBAPPS_DEFAULT_DIR_NAME = "web-apps";
    public static final String SERVER_PROJECT_XML_FILE = "nbproject/project.xml";
    public static final String DEPLOY_WEB_PROJECTTYPE = "web.project";
    public static final String DEPLOY_HTML5_PROJECTTYPE = "web.clientproject";
    public static final String HTML5_SITE_ROOT_PROP = "site.root.folder";
    public static final String HTML5_DEFAULT_SITE_ROOT_PROP = "public_html";
    public static final String HTML5_WEB_CONTEXT_ROOT_PROP = "web.context.root";

    public static final String WEB_REF = ".webref";
    public static final String WAR_REF = ".warref";
    public static final String HTM_REF = ".htmref";

    /**
     * Keys for Server Properties
     */
    public static String SERVER_ID_PROP = "server-id";
    public static final String DISPLAY_NAME_PROP = "displayName";
    public static final String HTTP_PORT_PROP = "httpportnumber";
    public static final String SHUTDOWN_PORT_PROP = "shutdownPortNumber";
    public static final String SHUTDOWN_KEY = "netbeans";

    public static final String HOST_PROP = "host";
    public static final String DEBUG_PORT_PROP = "debug_port";
    public static final String INCREMENTAL_DEPLOYMENT = "incrementalDeployment";
    public static final String WEBAPPLICATIONS_FOLDER = "server-instance-config";

    public static final String CONTEXTPATH_PROP = "contextPath";
    public static final String WEBAPP_CONFIG_FILE = "context.xml";
    public static final String WEB_APPS_PACK = "web-apps-pack";

    public static final String EMBEDDED_INSTANCE_PROPERTIES_FILE = "server-instance.properties";
    public static final String EMBEDDED_INSTANCE_PROPERTIES_PATH = WEBAPPLICATIONS_FOLDER + "/" + EMBEDDED_INSTANCE_PROPERTIES_FILE;

    public static int bufferSize = 64 * 1024;

    /*    public static void DOMTest() {
     DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();        
     }
     */
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

    public static String copyBaseDir(Tomcat tomcat, String from) {
        String targetDir = getCopyBaseDir(tomcat, from);
        System.out.println("Utils.copyBaseDir targetDir=" + targetDir);
        targetDir = targetDir.replace("\\", "/");
        File fromFile = new File(from);
        System.out.println("copyBaseDir fromFile=" + fromFile.getAbsolutePath());

        File toFile = new File(targetDir);
        try {
            copyDir(fromFile, toFile);
        } catch (IOException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return targetDir;
    }
    public static final String COPY_BASEDIR_NAME = "tomcat_emb__copybasedir___";

    public static void clearInstanceCopyBaseDir(Tomcat tomcat) {
        File rootDir = Paths.get(System.getProperty("java.io.tmpdir"),
                COPY_BASEDIR_NAME, getCopyBaseDirPrefix(tomcat)).toFile();
        delete(rootDir);
    }

    public static String getCopyBaseDir(Tomcat tomcat, String webappDir) {
        //String port = Integer.toString(tomcat.getConnector().getPort());
        File rootDir = Paths.get(System.getProperty("java.io.tmpdir"),
                COPY_BASEDIR_NAME, getCopyBaseDirPrefix(tomcat)).toFile();
        StringBuilder sb = new StringBuilder();
        //sb.append(new File(System.getProperty("java.io.tmpdir")).getAbsolutePath());
        sb.append(rootDir.getAbsolutePath())
                .append("/")
                .append(getWebAppName(webappDir));

        return sb.toString();
    }

    public static String getCopyBaseDirPrefix(Tomcat tomcat) {
        String port = Integer.toString(tomcat.getConnector().getPort());

        StringBuilder sb = new StringBuilder();
        sb.append("tomcat7_emb_")
                .append(port).append("_");

        return sb.toString();
    }

    public static String getUnpackedWebAppsDir(Tomcat tomcat) {
        String port = Integer.toString(tomcat.getConnector().getPort());
        //return "D:/VnsTestApps/Nb74/TomcatEmbeddedServer/package-dist/web-apps3";
        return new File(System.getProperty("java.io.tmpdir")).getAbsolutePath() + "/" + UNPACKED_WEB_APPS_PACK_PREFIX + port;
    }

    public static String getWebAppName(String webappDir) {
        File f = new File(webappDir);
        return f.getParentFile().getParentFile().getName();
    }

    /**
     * Copy files or directories
     *
     * @param from a source file (or folder) to be copied
     * @param to a target folder to copy to
     * @throws IOException any {@literal IO} error during copy
     */
    public static void copy(File from, File to) throws IOException {
        if (from.isDirectory()) {
            copyDir(from, to);
        } else {
            copyFile(from, to);
        }
    }

    /* ------------------------------------------------------------ */
    public static void copyDir(File from, File to) throws IOException {
        if (to.exists()) {
            if (!to.isDirectory()) {
                throw new IllegalArgumentException(to.toString());
            }
        } else {
            to.mkdirs();
        }

        File[] files = from.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                String name = files[i].getName();
                if (".".equals(name) || "..".equals(name)) {
                    continue;
                }
                copy(files[i], new File(to, name));
            }
        }
    }

    /* ------------------------------------------------------------ */
    public static void copyFile(File from, File to) throws IOException {
        FileInputStream in = new FileInputStream(from);
        FileOutputStream out = new FileOutputStream(to);
        copy(in, out);
        in.close();
        out.close();
    }

    /* ------------------------------------------------------------------- */
    /**
     * Copy data as InputStream to an OutputStream.
     *
     * @param in an instance of {@literal InputStream} to be copied
     * @param out an instance of {@literal OutputStream} to copy to
     * @throws IOException any {@literal IO} error during copy
     */
    public static void copy(InputStream in, OutputStream out)
            throws IOException {
        byte buffer[] = new byte[bufferSize];
        int len = bufferSize;

        while (true) {
            len = in.read(buffer, 0, bufferSize);
            if (len < 0) {
                break;
            }
            out.write(buffer, 0, len);
        }
    }

    /**
     * Recursively delete directories.
     *
     * @param file The file to be deleted.
     */
    public static boolean delete(File file) {
        if (!file.exists()) {
            return false;
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; files != null && i < files.length; i++) {
                delete(files[i]);
            }
        }
        return file.delete();
    }

    /* ------------------------------------------------------------ */
    public static boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows ");
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
     * @param projDir
     * @return
     */
    public static boolean isMavenProject(String projDir) {
        return new File(projDir + "/pom.xml").exists();
    }

    public static boolean isBuildOfMavenProject(String buildPath) {
        boolean b = false;

        File buldPathFile = new File(buildPath);
        if (buldPathFile.exists() && buldPathFile.isDirectory()) {
            File p = buldPathFile.getParentFile();
            if (p.exists()) {
                p = p.getParentFile();
                if (new File(p.getPath() + "/pom.xml").exists()) {
                    b = true;
                }
            }
        }
        return b;
    }

    public static Properties getContextProperties(String warPath) {

        File f = new File(warPath + "/META-INF/" + WEBAPP_CONFIG_FILE);
        System.out.println("getContextProperties warPath=" + warPath);
        if (!f.exists()) {
            System.out.println("getContextProperties NOT EXISTS ???? ");
            return loadWebAppProperties(warPath, true); // Html5
        }
        return getContextProperties(f);
    }

    public static Properties getContextProperties(File xmlFile) {
        Properties result = new Properties();
        try {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setValidating(false);
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            builder.setEntityResolver(new ParserEntityResolver());
            Document doc = builder.parse(xmlFile);

            // OLD NodeList nl = doc.getDocumentElement().getElementsByTagName("Context");
            NodeList nl = doc.getElementsByTagName("Context");                        

            if (nl != null) {
                for (int i = 0; i < nl.getLength(); i++) {
                    Element el = (Element) nl.item(i);

                    if (el.getAttribute("path") != null) {
                        result.setProperty("contextPath", el.getAttribute("path"));
                    }
                    if (el.getAttribute("antiJARLocking") != null) {
                        result.setProperty("antiJARLocking", el.getAttribute("antiJARLocking"));
                    }
                    if (el.getAttribute("antiResourceLocking") != null) {
                        result.setProperty("antiResourceLocking", el.getAttribute("antiResourceLocking"));
                        result.setProperty("antiJARLocking", el.getAttribute("antiResourceLocking"));                        
                    }

                }
            }

        } catch (IOException | DOMException | ParserConfigurationException | SAXException ex) {
            //out("Utils: getContextProperties EXCEPTION " + ex.getMessage());
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public static Properties getContextProperties(InputStream xmlFile) {
        Properties result = new Properties();
        try {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setValidating(false);
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            builder.setEntityResolver(new ParserEntityResolver());
            Document doc = builder.parse(xmlFile);

            // OLD NodeList nl = doc.getDocumentElement().getElementsByTagName("Context");
            NodeList nl = doc.getElementsByTagName("Context");            
            if (nl != null) {
                for (int i = 0; i < nl.getLength(); i++) {
                    Element el = (Element) nl.item(i);
                    if (el.getAttribute("path") != null) {
                        result.setProperty("contextPath", el.getAttribute("path"));
                    }
                    if (el.getAttribute("antiJARLocking") != null) {
                        result.setProperty("antiJARLocking", el.getAttribute("antiJARLocking"));
                    }
                    if (el.getAttribute("antiResourceLocking") != null) {
                        result.setProperty("antiResourceLocking", el.getAttribute("antiResourceLocking"));
                        result.setProperty("antiJARLocking", el.getAttribute("antiResourceLocking"));                        
                    }
                }
            }

        } catch (IOException | DOMException | ParserConfigurationException | SAXException ex) {
            //out("Utils: getContextProperties EXCEPTION " + ex.getMessage());
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    private static Properties loadWebAppProperties(String warPath, boolean html5) {
        File f = new File(warPath + "/nbproject/project.properties");
        if (!f.exists()) {
            return loadWebAppProperties(warPath, true);
        }
        Properties projProps = loadProperties(f);
        if (projProps.getProperty(HTML5_SITE_ROOT_PROP) == null
                || projProps.getProperty(HTML5_WEB_CONTEXT_ROOT_PROP) == null) {
            return null;
        }
        Properties props = new Properties();
        props.setProperty(Utils.CONTEXTPATH_PROP, projProps.getProperty(HTML5_WEB_CONTEXT_ROOT_PROP));
        return props;

    }

    /*    public static Properties loadWebAppProperties(String warPath) {

     File f = new File(warPath + "/META-INF/" + WEBAPP_CONFIG_FILE);
     if (!f.exists()) {
     return loadWebAppProperties(warPath, true);
     }
     final Properties props = new Properties();
     try (FileInputStream fis = new FileInputStream(f)) {
     props.load(fis);
     fis.close();
     return props;
     } catch (IOException ioe) {
     return null;
     }
     }
     */
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
        for (File f : new File("./" + WEBAPPLICATIONS_FOLDER).listFiles(filter)) {
            String projName = f.getName();
            if (projName.endsWith(WAR_REF) || projName.endsWith(WEB_REF) || projName.endsWith(HTM_REF)) {
                projName = projName.substring(0, projName.length() - 7);
            }
            System.out.println("REGISTERED: " + projName);
            map.put(projName, f);
        }
        return map;
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
        File f = new File("./" + EMBEDDED_INSTANCE_PROPERTIES_PATH);
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
            InputStream is = Utils.class.getClassLoader().getResourceAsStream(Utils.EMBEDDED_INSTANCE_PROPERTIES_FILE);
            try {
                props.load(is);
                is.close();
            } catch (IOException ex) {
                Logger.getLogger(CommandManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return props;
    }

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
            if ("Context".equals(qName) && attributes != null && attributes.getLength() > 0) {
                for (int i = 0; i < attributes.getLength(); i++) {
                    String value = attributes.getValue(i);
                    String name = attributes.getQName(i);

                    switch (name) {
                        case "path":
                            props.setProperty("contextPath", value);
                            break;
                        case "antiJARLocking":
                            props.setProperty("antiJARLocking", value);
                            break;
                        case "antiResourceLocking":
                            props.setProperty("antiJARLocking", value);
                            props.setProperty("antiResourceLocking", value);
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

}
