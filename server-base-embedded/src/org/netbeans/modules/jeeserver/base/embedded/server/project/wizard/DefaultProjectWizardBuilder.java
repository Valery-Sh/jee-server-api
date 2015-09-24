/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jeeserver.base.embedded.server.project.wizard;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.modules.jeeserver.base.deployment.specifics.ProjectWizardBuilder;
import org.openide.WizardDescriptor;
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
 * @author Valery
 */
public class DefaultProjectWizardBuilder implements ProjectWizardBuilder {

    private static final Logger LOG = Logger.getLogger(DefaultProjectWizardBuilder.class.getName());
    
    @StaticResource
    public static final String ANT_BASED_TEMPLATE = "org/netbeans/modules/jeeserver/base/embedded/server/project/wizard/AntBasedProjectInstance.zip";
    @StaticResource
    public static final String MAVEN_BASED_TEMPLATE = "org/netbeans/modules/jeeserver/base/embedded/server/project/wizard/MavenBasedProjectInstance.zip";
    
    protected WizardDescriptor wiz;
    protected boolean mavenbased;
    
    public DefaultProjectWizardBuilder(WizardDescriptor wiz, boolean mavenbased) {
        this.wiz = wiz;
    }
    protected InputStream getTemplateInputStream() {

        InputStream is;
        
        if ( mavenbased ) {
            is = getClass().getClassLoader().getResourceAsStream("/" + MAVEN_BASED_TEMPLATE);
        } else {
            is = getClass().getClassLoader().getResourceAsStream("/" + ANT_BASED_TEMPLATE);
        }
        return is;
    }
    
    
    protected void unZipAntProjectTemplate(InputStream source, FileObject projectRoot) throws IOException {
        ZipInputStream str = null;
        try {
            str = new ZipInputStream(source);
            ZipEntry entry;
            while ((entry = str.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    FileUtil.createFolder(projectRoot, entry.getName());
                } else {
                    FileObject fo = FileUtil.createData(projectRoot, entry.getName());
                    if ("nbproject/project.xml".equals(entry.getName())) {
                        // Special handling for setting name of Ant-based projects; customize as needed:
                        filterProjectXML(fo, str, projectRoot.getName());
                    } else {
                        writeFile(str, fo);
                    }
                }
            }
        } finally {
            source.close();
            str.close();
        }
    }

    protected void unZipMavenProjectTemplate(InputStream source, FileObject projectRoot) throws IOException {
        ZipInputStream str = null;
        try {
            str = new ZipInputStream(source);
            ZipEntry entry;
            while ((entry = str.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    FileUtil.createFolder(projectRoot, entry.getName());
                } else {
                    FileObject fo = FileUtil.createData(projectRoot, entry.getName());
                    if ("nbproject/project.xml".equals(entry.getName())) {
                        // Special handling for setting name of Ant-based projects; customize as needed:
                        filterProjectXML(fo, str, projectRoot.getName());
                    } else {
                        writeFile(str, fo);
                    }
                }
            }
        } finally {
            source.close();
            str.close();
        }
    }

    protected static void writeFile(ZipInputStream str, FileObject fo) throws IOException {
        try (OutputStream out = fo.getOutputStream()) {
            FileUtil.copy(str, out);
        }
    }

    protected static void filterProjectXML(FileObject fo, ZipInputStream str, String name) throws IOException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            FileUtil.copy(str, baos);
            Document doc = XMLUtil.parse(new InputSource(new ByteArrayInputStream(baos.toByteArray())), false, false, null, null);
            NodeList nl = doc.getDocumentElement().getElementsByTagName("name");
            if (nl != null) {
                for (int i = 0; i < nl.getLength(); i++) {
                    Element el = (Element) nl.item(i);
                    if (el.getParentNode() != null && "data".equals(el.getParentNode().getNodeName())) {
                        NodeList nl2 = el.getChildNodes();
                        if (nl2.getLength() > 0) {
                            nl2.item(0).setNodeValue(name);
                        }
                        break;
                    }
                }
            }
            try (OutputStream out = fo.getOutputStream()) {
                XMLUtil.write(doc, out, "UTF-8");
            }
        } catch (IOException | DOMException | SAXException ex) {
            LOG.log(Level.INFO, ex.getMessage());
            writeFile(str, fo);
        }

    }
    
}
