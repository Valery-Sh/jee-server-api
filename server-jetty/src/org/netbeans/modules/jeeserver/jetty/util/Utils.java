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
package org.netbeans.modules.jeeserver.jetty.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.deployment.specifics.ServerSpecifics;
import org.netbeans.modules.jeeserver.base.deployment.specifics.ServerSpecificsProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.netbeans.modules.jeeserver.base.deployment.utils.Copier;
import org.netbeans.modules.jeeserver.jetty.project.JettyProjectFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.xml.XMLUtil;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author V. Shyshkin
 */
public class Utils {

    private static final Logger LOG = Logger.getLogger(Utils.class.getName());

    public static String getJettyVersion(String jettyHome) {
        Path lib = Paths.get(jettyHome, "lib");

        if (!Files.exists(lib)) {
            return "9.2.11";
        }
        String jarName = null;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(lib)) {
            for (Path path : stream) {
                String fileName = path.toFile().getName();
                System.out.println(fileName);
                if (!(fileName.endsWith(".jar") && fileName.startsWith("jetty-server-"))) {
                    continue;
                }
                jarName = fileName;
            }
        } catch (IOException | DirectoryIteratorException x) {
            // IOException can never be thrown by the iteration.
            // In this snippet, it can only be thrown by newDirectoryStream.
            System.err.println(x);
        }
        if (jarName == null) {
            return "9.2.11";
        }
        String r = jarName.substring("jetty-server-".length());
        int i = r.indexOf('.');
        i = r.indexOf('.', i + 1);
        int hyphen = r.indexOf('-', i + 1);
        int point = r.indexOf('.', i + 1);
        i = hyphen >= 0 && hyphen < point ? hyphen : point;
        // i = r.indexOf('.', i + 1 );
        r = r.substring(0, i);
        System.out.println("R=" + r + "; i=" + i);
        return r;
    }
    public static String getFullJettyVersion(String jettyHome) {
        Path lib = Paths.get(jettyHome, "lib");

        if (!Files.exists(lib)) {
            return "9.2.11";
        }
        String jarName = null;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(lib)) {
            for (Path path : stream) {
                //File f = path.toFile();
                String fileName = path.toFile().getName();
                System.out.println(fileName);
                if (!(fileName.endsWith(".jar") && fileName.startsWith("jetty-server-"))) {
                    continue;
                }
                jarName = fileName;
            }
        } catch (IOException | DirectoryIteratorException x) {
            // IOException can never be thrown by the iteration.
            // In this snippet, it can only be thrown by newDirectoryStream.
            System.err.println(x);
        }
        if (jarName == null) {
            return "9.2.11";
        }
        
        String r = jarName.substring("jetty-server-".length(),jarName.length()-4);
        
        return r;
    }

    public static void out(String msg) {
        InputOutput io = IOProvider.getDefault().getIO("ShowMsg", false);
        io.getOut().println(msg);
        io.getOut().close();
    }

    public static Properties propertiesOf(InstanceProperties ip) {
        Properties props = new Properties();
        Enumeration en = ip.propertyNames();
        while (en.hasMoreElements()) {
            String key = (String) en.nextElement();
            String prop = ip.getProperty(key);
            props.setProperty(key, prop);
        }
        return props;
    }

    public static Properties propertiesOf(Project project) {
        InstanceProperties ip = InstanceProperties.getInstanceProperties(buildUri(project));
        Properties props = new Properties();
        Enumeration en = ip.propertyNames();
        while (en.hasMoreElements()) {
            String key = (String) en.nextElement();
            String prop = ip.getProperty(key);
            props.setProperty(key, prop);
        }
        return props;
    }

    public static String getServerId() {
        return "jettystandalone";
    }

    private static String buildUri(Project project) {
        return buildUri(project.getProjectDirectory());
    }

    public static String buildUri(FileObject projectDir) {
        return getServerId() + ":" + BaseConstants.URIPREFIX_NO_ID + ":" + projectDir.getPath();
    }

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

    /**
     *
     * @param serverIdProp the server-id property as specified by 
     * {@link org.netbeans.modules.jeeserver.base.deployment.specofics.ServerSpecificsProvider }
     * @return an array of elements. Each element represents the uri of a server
     * instance.
     */
    public static String[] getServerInstanceIDs(String serverIdProp) {
        List<String> ids = new ArrayList<>();

        String[] uris = InstanceProperties.getInstanceList();
        for (String uri : uris) {
            try {
                String p = InstanceProperties.getInstanceProperties(uri).getProperty(BaseConstants.SERVER_ID_PROP);
                if (p == null) {
                    continue;
                }
                if (p.equals(serverIdProp)) {
                    ids.add(uri);
                }
            } catch (IllegalStateException ex) {
                LOG.log(Level.INFO, ex.getMessage());
            }
        }
        String[] ar = new String[ids.size()];
        return ids.toArray(ar);
    }

    public static boolean isJettyServer(Project proj) {
        return new JettyProjectFactory().isProject(proj.getProjectDirectory());
    }

    public static Map<String, String> getDefaultPropertyMap(FileObject projectDir) {
        Map<String, String> map = new HashMap<>();
        map.put(BaseConstants.SERVER_ID_PROP, Utils.getServerId());
        map.put(BaseConstants.URL_PROP, Utils.buildUri(projectDir));
        map.put(BaseConstants.HOST_PROP, "localhost");
        ServerSpecifics spec = BaseUtils.getServerSpecifics(Utils.getServerId());
        map.put(BaseConstants.DEBUG_PORT_PROP, String.valueOf(spec.getDefaultDebugPort()));
        map.put(BaseConstants.DISPLAY_NAME_PROP, projectDir.getNameExt());
        map.put(BaseConstants.SERVER_LOCATION_PROP, projectDir.getPath());
        return map;
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
            LOG.log(Level.FINE, ex.getMessage()); //NOI18N

        } finally {
            try {
                is.close();
            } catch (IOException ex) {
                LOG.log(Level.FINE, ex.getMessage()); //NOI18N
            }
        }

        return sb.toString();
    }

    public static Properties getContextProperties(String jettyXmlStr) {
        if ( jettyXmlStr == null ) {
            return null;
        }
        InputSource source;
        source = new InputSource(new ByteArrayInputStream(jettyXmlStr.getBytes()));
        return getContextProperties(source);
    }

    public static Properties getContextProperties(FileObject jettyXml) {
        if ( jettyXml == null ) {
            return null;
        }
        
        InputSource source;
        try {
            source = new InputSource(jettyXml.getInputStream());
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
        return getContextProperties(source);
    }

    public static Properties getContextProperties(InputSource source) {
        
        Properties result = new Properties();
        if ( source == null ) {
            return result;
        }
        try {
            Document doc = XMLUtil.parse(source, false, false, null, new ParseEntityResolver());
            NodeList nl = doc.getDocumentElement().getElementsByTagName("Set");
            if (nl != null) {
                int found = 0;
                for (int i = 0; i < nl.getLength(); i++) {
                    Element el = (Element) nl.item(i);
                    switch (el.getAttribute("name")) {
                        case "contextPath":
                            result.setProperty("contextPath", el.getTextContent());
                            //BaseUtils.out("1. Utils.getContextProperties el.getTextContext()=" + el.getTextContent());
                            found++;
                            break;
                        case "war":
                            result.setProperty("war", el.getTextContent());
                            //BaseUtils.out("2. Utils.getContextProperties el.getTextContext()=" + el.getTextContent());

                            found++;
                            break;
                        case "getCopyDir":
                            result.setProperty("getCopyDir", el.getTextContent());
                            found++;
                            break;
                    }
                    if (found >= 3) {
                        break;
                    }
                }//for
            }

        } catch (IOException | DOMException | SAXException ex) {
            Utils.out("Utils: getContextProperties EXCEPTION " + ex.getMessage());
            LOG.log(Level.INFO, ex.getMessage());
        }
        return result;
    }
    
    public static String getState(BaseDeploymentManager manager, String contextPath)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("cmd=");
        sb.append("getstatebycontextpath");
        sb.append("&cp=");
        sb.append(BaseUtils.encode(contextPath));

        
        return manager.getSpecifics().execCommand(manager.getServerProject(), sb.toString()); 
/*        switch(state)
        {
            case __FAILED: return FAILED;
            case __STARTING: return STARTING;
            case __STARTED: return STARTED;
            case __STOPPING: return STOPPING;
            case __STOPPED: return STOPPED;
        }
*/        
    }
    
    public static class SAXHandler extends DefaultHandler {

        String content = null;

        @Override
        public void startElement(String uri, String localName,
                String qName, Attributes attributes)
                throws SAXException {

            switch (qName) {
                case "Set":
                    String s = attributes.getValue(0);
                    break;
            }
        }
    }

}//class
