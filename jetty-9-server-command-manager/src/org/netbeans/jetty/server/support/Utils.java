/**
 * This file is part of Jetty Server suppport in NetBeans IDE.
 *
 * Jetty Server suppport in NetBeans IDE is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the License,
 * or (at your option) any later version.
 *
 * Jetty Server suppport in NetBeans IDE is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should see the GNU General Public License here:
 * <http://www.gnu.org/licenses/>.
 */
package org.netbeans.jetty.server.support;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.SourceVersion;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.eclipse.jetty.server.Handler;
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

//    public static final String JETTY_BASE = "jetty.base";

    public static final String WEBAPPS_DEFAULT_DIR_NAME = "web-apps";
    public static final String WEBAPPS_DIR_PROP = "deployWebapps";

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
     * Keys xmlFileor Server Properties
     */
    public static String SERVER_ID_PROP = "server-id";
    public static final String DISPLAY_NAME_PROP = "displayName";
    public static final String HTTP_PORT_PROP = "httpportnumber";

    public static final String HOST_PROP = "host";
    public static final String DEBUG_PORT_PROP = "debug_port";
    public static final String INCREMENTAL_DEPLOYMENT = "incrementalDeployment";
    public static final String WEBAPPLICATIONS_FOLDER = "web-apps";

    public static final String CONTEXTPATH_PROP = "contextPath";
    public static final String WEBAPP_CONFIG_FILE = "context.properties";
    public static final String WEB_APPS_PACK = "web-apps-pack";

    public static final String EMBEDDED_INSTANCE_PROPERTIES_FILE = "server-instance.properties";
    public static final String EMBEDDED_INSTANCE_PROPERTIES_PATH = WEBAPPLICATIONS_FOLDER + "/" + EMBEDDED_INSTANCE_PROPERTIES_FILE;
    public static final String ANTI_LOCK_PROP_NAME = "antiResourceLocking";

    protected static final String WEBAPPS_FOLDER_NAME = "webapps";

    public static final String JETTYBASE_FOLDER = "jettybase";
    public static final String WEBAPPS_FOLDER = JETTYBASE_FOLDER + "/" + WEBAPPS_FOLDER_NAME;
    public static final String JETTY_HTTP_INI = JETTYBASE_FOLDER + "/start.d/http.ini";
    public static final String JETTY_START_INI = JETTYBASE_FOLDER + "/start.ini";

    public static boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows ");
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
     * Test whether a given string is a valid Java identixmlFileier.
     *
     * @param id string which should be checked
     * @return <code>true</code> ixmlFile a valid identixmlFileier
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

    private static Properties loadWebAppProperties(String warPath, boolean html5) {
        File p = new File(warPath).getParentFile();
        File f = new File(p.getAbsolutePath() + "/nbproject/project.properties");

        if (!f.exists()) {
            return null;
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

    public static boolean containsBeansXml(String warPath) {
        return new File(warPath + "/WEB-INF/beans.xml").exists();
    }

    public static CommandManager getCommandManager(Handler h) {
        CommandManager cm = null;
        
        Handler[] hs = h.getServer().getChildHandlersByClass(CommandManager.class);
        if (hs.length > 0) {
            cm =  (CommandManager) hs[0];
        }
        return cm;
    }

    public static Properties getContextProperties(String warPath) {
        final Properties props = new Properties();

        File f = new File(warPath + "/WEB-INF/jetty-web.xml");
        if (!f.exists()) {
            f = new File(warPath + "/WEB-INF/web-jetty.xml");
            if (!f.exists()) {
                return props;
            }
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

            NodeList nl = doc.getDocumentElement().getElementsByTagName("Set");

            if (nl != null) {
                int found = 0;
                for (int i = 0; i < nl.getLength(); i++) {
                    Element el = (Element) nl.item(i);

                    if ("contextPath".equals(el.getAttribute("name"))) {
                        result.setProperty("contextPath", el.getTextContent());
                        found++;
                    } else if ("war".equals(el.getAttribute("name"))) {
                        result.setProperty("war", el.getTextContent());
                        found++;
                    }
                    if (found >= 2) {
                        break;
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
        final Properties props = new Properties();
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

                    if ("contextPath".equals(el.getAttribute("name"))) {
                        result.setProperty("contextPath", el.getTextContent());
                        found++;
                    } else if ("war".equals(el.getAttribute("name"))) {
                        result.setProperty("war", el.getTextContent());
                        found++;
                    }
                    if (found >= 2) {
                        break;
                    }
                }
            }

        } catch (IOException | DOMException | ParserConfigurationException | SAXException ex) {
            //out("Utils: getContextProperties EXCEPTION " + ex.getMessage());
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

}
