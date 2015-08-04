/**
 * This file is part of Jetty Server support in NetBeans IDE.
 *
 * Jetty Server support in NetBeans IDE is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * Jetty Server support in NetBeans IDE is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should see the GNU General Public License here:
 * <http://www.gnu.org/licenses/>.
 */
package org.netbeans.modules.jeeserver.jetty.deploy.config;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.deployment.config.AbstractModuleConfiguration;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.netbeans.modules.jeeserver.jetty.util.ParseEntityResolver;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.xml.XMLUtil;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The instance of the class is created for each Web Project that uses an
 * embedded server. The {@literal ContextRootConfiguration} interface
 * specification states that a class that implements it should be registered in
 * the {@literal ModuleConfiguration} lookup in order in order for the NetBeans
 * to install a context path.
 *
 * @author V.Shyshkin
 */
public class JettyServerModuleConfiguration  extends AbstractModuleConfiguration {

    private static final Logger LOG = Logger.getLogger(JettyServerModuleConfiguration.class.getName());


    /**
     * Creates a new instance of the class for the specified module.
     *
     * @param module an object of type {@literal J2eeModule)
     * }
     * @param contextFilePaths
     */
    protected JettyServerModuleConfiguration(J2eeModule module, String[] contextFilePaths) {
        super(module, contextFilePaths);
    }
    protected JettyServerModuleConfiguration(J2eeModule module, String[] contextFilePaths, String instanceUrl) {
        super(module, contextFilePaths);
    }    
    
    public static JettyServerModuleConfiguration getInstance(J2eeModule module, String[] contextFilePaths) {
        JettyServerModuleConfiguration m = new JettyServerModuleConfiguration(module,contextFilePaths);
        m.notifyCreate();
        return m;
    }
    public static JettyServerModuleConfiguration getInstance(J2eeModule module, String[] contextFilePaths, String instanceUrl) {
        
        JettyServerModuleConfiguration m = new JettyServerModuleConfiguration(module,contextFilePaths,instanceUrl);
        m.notifyCreate();
        return m;
    }

    /**
     * "WEB-INF/jetty-web.xml" or "WEB-INF/web-jetty.xml"
     *
     * @return
     */
    @Override
    protected File findContextConfigFile() {
        File result = null;
        for (String path : getContextFilePaths()) {
            File f = getJ2eeModule().getDeploymentConfigurationFile(path);
            if (f.exists()) {
                result = f;
                break;
            }
        }
        return result != null ? result : getJ2eeModule().getDeploymentConfigurationFile(getContextFilePaths()[0]);
    }

    @Override
    public Properties getContextProperties() {
        FileObject c = FileUtil.toFileObject(getContextConfigFile());
        return getContextProperties(c);
    }

    @Override
    protected String changeContext(String cp) {
        String result = null;
        FileObject jettyXml = FileUtil.toFileObject(getContextConfigFile());
        try {
            InputSource source = new InputSource(jettyXml.getInputStream());
            Document doc = XMLUtil.parse(source, false, false, null, new ParseEntityResolver());
            NodeList nl = doc.getDocumentElement().getElementsByTagName("Set");
            if (nl != null) {
                for (int i = 0; i < nl.getLength(); i++) {
                    Element el = (Element) nl.item(i);
                    result = el.getTextContent();
                    if ("contextPath".equals(el.getAttribute("name"))) {
                        el.setTextContent(cp);
                        break;
                    }
                }
            }
            //doc.normalizeDocument();
            try (OutputStream os = jettyXml.getOutputStream()) {
                XMLUtil.write(doc, os, doc.getXmlEncoding());
            }

        } catch (IOException | DOMException | SAXException ex) {
            LOG.log(Level.INFO,ex.getMessage());
        }
        return result;
    }

    @Override
    protected void initContextConfigFile() {

        if (!getContextConfigFile().exists()) {

            File webinf = getJ2eeModule().getDeploymentConfigurationFile("WEB-INF");
            FileObject webinfDirFo = null;
            try {
                if (!webinf.exists()) {
                    FileUtil.toFileObject(Files.createDirectories(webinf.toPath()).toFile());
                }
                webinfDirFo = FileUtil.toFileObject(webinf);
                FileObject jettyXmlFo = webinfDirFo.createData("jetty-web.xml");
                OutputStream os;
                try (ByteArrayInputStream is = (ByteArrayInputStream) getClass().getResourceAsStream("/org/netbeans/modules/jeeserver/jetty/resources/jetty-web.xml")) {
                    os = jettyXmlFo.getOutputStream();
                    while (true) {
                        int b = is.read();
                        if (b == -1) {
                            break;
                        }
                        os.write(b);
                    }
                }
                os.close();

            } catch (IOException ex) {
                LOG.log(Level.INFO, "JettyModuleConfiguration.getProjectPropertiesFileObject. {0}", ex.getMessage()); //NOI18N                        
            }

            Project wp = FileOwnerQuery.getOwner(webinfDirFo.toURI());
            
            String cp = "/" + wp.getProjectDirectory().getNameExt();
            changeContext("/" + wp.getProjectDirectory().getNameExt());
        }
    }

    
    public static Properties getContextProperties(FileObject jettyXml) {
        Properties result = new Properties();
        try {
            InputSource source = new InputSource(jettyXml.getInputStream());
            Document doc = XMLUtil.parse(source, false, false, null, new ParseEntityResolver());
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
                        case "getCopyDir":
                            result.setProperty("getCopyDir", el.getTextContent());
                            found++;
                            break;
                    }
                    if (found >= 3) {
                        break;
                    }
                }
            }

        } catch (IOException | DOMException | SAXException ex) {
            LOG.log(Level.INFO, ex.getMessage()); //NOI18N                        

        }
        return result;
    }

}
