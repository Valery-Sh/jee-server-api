/**
 * This file is part of Tomcat Server Embedded support in NetBeans IDE.
 *
 * Tomcat Server Embedded support in NetBeans IDE is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * Tomcat Server Embedded suppport in NetBeans IDE is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should see the GNU General Public License here:
 * <http://www.gnu.org/licenses/>.
 */
package org.netbeans.modules.jeeserver.tomcat.embedded;

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
 *
 * @author V. Shyshkin
 */
public class TomcatModuleConfiguration extends AbstractModuleConfiguration {

    private static final Logger LOG = Logger.getLogger(TomcatModuleConfiguration.class.getName());

    /**
     * Creates a new instance of the class for the specified module.
     *
     * @param module an object of type {@literal J2eeModule)
     * }
     * @
     * param contextFilePaths
     */
    protected TomcatModuleConfiguration(J2eeModule module, String[] contextFilePaths) {
        super(module, contextFilePaths);
    }

    protected TomcatModuleConfiguration(J2eeModule module, String[] contextFilePaths, String instanceUrl) {
        super(module, contextFilePaths);
    }

    public static TomcatModuleConfiguration getInstance(J2eeModule module, String[] contextFilePaths) {
        TomcatModuleConfiguration m = new TomcatModuleConfiguration(module, contextFilePaths);
        m.notifyCreate();
        return m;
    }

    public static TomcatModuleConfiguration getInstance(J2eeModule module, String[] contextFilePaths, String instanceUrl) {
//Utils.out("JettyServerModuleConfiguration.getInstance() j2eeModule.=" + module.getDeploymentConfigurationFile("WEB-INF"));
        TomcatModuleConfiguration m = new TomcatModuleConfiguration(module, contextFilePaths, instanceUrl);
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
        FileObject contextXml = FileUtil.toFileObject(getContextConfigFile());
        try {
            InputSource source = new InputSource(contextXml.getInputStream());
            Document doc = XMLUtil.parse(source, false, false, null, new org.netbeans.modules.jeeserver.base.embedded.utils.ParseEntityResolver());
            Element el = doc.getDocumentElement();
            result = el.getAttribute("path");
            el.setAttribute("path", cp);
            //doc.normalizeDocument();
            try (OutputStream os = contextXml.getOutputStream()) {
                XMLUtil.write(doc, os, doc.getXmlEncoding());
            }

        } catch (IOException | DOMException | SAXException ex) {
            LOG.log(Level.INFO, "TomcatModuleConfiguration.getProjectPropertiesFileObject. {0}", ex.getMessage()); //NOI18N                        
        }
        return result;
    }

    @Override
    protected void initContextConfigFile() {
        if (!getContextConfigFile().exists()) {

            File metainf = getJ2eeModule().getDeploymentConfigurationFile("META-INF");
            FileObject metainfDirFo = null;
            try {
                if (!metainf.exists()) {
                    FileUtil.toFileObject(Files.createDirectories(metainf.toPath()).toFile());
                }
                metainfDirFo = FileUtil.toFileObject(metainf);
                FileObject contextXmlFo = metainfDirFo.createData("context.xml");
                OutputStream os;
                try (ByteArrayInputStream is = (ByteArrayInputStream) getClass().getResourceAsStream("/org/netbeans/modules/jeeserver/tomcat/embedded/resources/context.xml")) {
                    os = contextXmlFo.getOutputStream();
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
                LOG.log(Level.INFO, "TomcatModuleConfiguration.getProjectPropertiesFileObject. {0}", ex.getMessage()); //NOI18N                        
            }
            Project wp = FileOwnerQuery.getOwner(metainfDirFo.toURI());
            changeContext("/" + wp.getProjectDirectory().getNameExt());
        }
    }

    public static Properties getContextProperties(FileObject contextXml) {
        Properties result = new Properties();
        try {
            InputSource source = new InputSource(contextXml.getInputStream());
            Document doc = XMLUtil.parse(source, false, false, null, new org.netbeans.modules.jeeserver.base.embedded.utils.ParseEntityResolver());
            Element el = doc.getDocumentElement();
            if (el.getAttribute("path") != null) {
                result.setProperty("contextPath", el.getAttribute("path"));
            }
            if (el.getAttribute("antiJARLocking") != null) {
                result.setProperty("antiJARLocking", el.getAttribute("antiJARLocking"));
            }
            if (el.getAttribute("antiResourceLocking") != null) {
                result.setProperty("antiJARLocking", el.getAttribute("antiJARLocking"));
                result.setProperty("antiResourceLocking", el.getAttribute("antiResourceLocking"));
            }
        } catch (IOException | DOMException | SAXException ex) {
            LOG.log(Level.INFO, ex.getMessage()); //NOI18N                        

        }
        return result;
    }

}
