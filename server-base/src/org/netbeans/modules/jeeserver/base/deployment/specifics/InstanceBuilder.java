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
package org.netbeans.modules.jeeserver.base.deployment.specifics;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceCreationException;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.EditableProperties;
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
public abstract class InstanceBuilder {

    private static final Logger LOG = Logger.getLogger(InstanceBuilder.class.getName());
    private WizardDescriptor wiz;
    
    protected Properties configProps;
    
    public InstanceBuilder(Properties configProps) {
        this.configProps = configProps;
    }

    public abstract Set instantiate();

    public abstract InputStream getZipTemplateInputStream();
    
    public abstract void finishInstantiateProjectDir(Set result);

    
    public WizardDescriptor getWizardDescriptor() {
        return wiz;
    }

    public void setWizardDescriptor(WizardDescriptor wiz) {
        this.wiz = wiz;
    }

    protected void runInstantiateProjectDir(Set result) throws IOException {

        File dirF = FileUtil.normalizeFile((File) wiz.getProperty("projdir"));
        dirF.mkdirs();

        FileObject dir = FileUtil.toFileObject(dirF);

        unZipFile(getZipTemplateInputStream(), dir);
        createBuildXml(dir);
        // Always open top dir as a project:

        // Look for nested projects to open as well:
        File parent = dirF.getParentFile();
        if (parent != null && parent.exists()) {
            ProjectChooser.setProjectsFolder(parent); // Last used folder with a new project
        }
        Project p = ProjectManager.getDefault().findProject(dir);
        OpenProjects.getDefault().open(new Project[]{p}, true);
        result.add(p);
        finishInstantiateProjectDir(result);        
    }

    /**
     * Does nothing.
     */
    public void createBuildXml(FileObject projectDir) throws IOException {
    }

    protected void instantiateProjectDir(Set result) throws IOException {

        FileUtil.runAtomicAction(new Runnable() {
            @Override
            public void run() {
                try {
                    runInstantiateProjectDir(result);
                } catch (IOException ex) {
                    LOG.log(Level.FINE, ex.getMessage()); //NOI18N
                }
            }
        });
    }

    protected void instantiateServerProperties(Set result) {
        Map<String, String> ipmap = getPropertyMap();
        String url = ipmap.get(BaseConstants.URL_PROP);
        String displayName = ipmap.get(BaseConstants.DISPLAY_NAME_PROP);

        try {
            InstanceProperties ip = InstanceProperties.createInstanceProperties(url, null, null, displayName, ipmap);
            result.add(ip);
            //wiz.putProperty(ip.getProperty(BaseConstants.URL_PROP), url);
        } catch (InstanceCreationException ex) {
            LOG.log(Level.SEVERE, ex.getMessage()); //NOI18N
        }
    }


    protected Properties mapToProperties(Map<String, String> map) {
        Properties props = new Properties();
        for (String key : map.keySet()) {
            props.setProperty(key, map.get(key));
        }
        return props;
    }

    protected Map<String, String> getPropertyMap() {

        Map<String, String> ip = new HashMap<>();
        FileObject projectDir = FileUtil.toFileObject(FileUtil.normalizeFile((File) wiz.getProperty("projdir")));
        String serverId = (String) wiz.getProperty(BaseConstants.SERVER_ID_PROP);
        String url = serverId + ":" + BaseConstants.URIPREFIX_NO_ID + ":" + projectDir.getPath();
        String jettyHome = (String) wiz.getProperty(BaseConstants.HOME_DIR_PROP);
//        String jettyVersion = Utils.getJettyVersion(jettyHome);

        String displayName = projectDir.getNameExt();

        ip.put(BaseConstants.SERVER_ID_PROP, serverId);
        ip.put(BaseConstants.DISPLAY_NAME_PROP, displayName);

        ip.put(BaseConstants.HOST_PROP, (String) wiz.getProperty(BaseConstants.HOST_PROP));
        ip.put(BaseConstants.HTTP_PORT_PROP, (String) wiz.getProperty(BaseConstants.HTTP_PORT_PROP));
        ip.put(BaseConstants.DEBUG_PORT_PROP, (String) wiz.getProperty(BaseConstants.DEBUG_PORT_PROP));
        ip.put(BaseConstants.SHUTDOWN_PORT_PROP, (String) wiz.getProperty(BaseConstants.SHUTDOWN_PORT_PROP));
        ip.put(BaseConstants.URL_PROP, url);
        ip.put(BaseConstants.HOME_DIR_PROP, jettyHome);
        ip.put(BaseConstants.SERVER_LOCATION_PROP, projectDir.getPath());
//        ip.put(BaseConstants.SERVER_VERSION_PROP, jettyVersion);
//        modifyPropertymap(ip);

        return ip;
    }

    private void unZipFile(InputStream source, FileObject projectRoot) throws IOException {

        //ZipInputStream str = null;
        try( ZipInputStream str = new ZipInputStream(source);) {
            //str = new ZipInputStream(source);
            ZipEntry entry;
            while ((entry = str.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    FileUtil.createFolder(projectRoot, entry.getName());
                } else {
                    FileObject fo = FileUtil.createData(projectRoot, entry.getName());
                    if ("nbproject/project.xml".equals(entry.getName())) {
                        // Special handling for setting name of Ant-based projects; customize as needed:
                        filterProjectXML(fo, str, projectRoot.getName());
                    } 
/*                    else if ("nbproject/build-impl.xml".equals(entry.getName())) {
                        BaseUtils.out("nbproject/build-impl.xml root=" + projectRoot.getName());
                        // Special handling for setting name of Ant-based projects; customize as needed:
                        filterBuildXML(fo, str, projectRoot.getName() + "-impl");
                    }
                    else if ("build.xml".equals(entry.getName())) {
                        // Special handling for setting name of Ant-based projects; customize as needed:
                        filterBuildXML(fo, str, projectRoot.getName());
                        BaseUtils.out("nbproject/build.xml root=" + projectRoot.getName());
                    } 
*/        
                    else {
                        writeFile(str, fo);
                        if ("nbproject/project.properties".equals(entry.getName())) {
                            EditableProperties props = new EditableProperties(false);
                            try (FileInputStream fis = new FileInputStream(fo.getPath())) {
                                props.load(fis);
                            }
                            props.setProperty("dist.jar", "${dist.dir}/" + projectRoot.getName() + ".jar");
                            props.setProperty("main.class", "org.embedded.server.JettyEmbeddedServer");
                            try (FileOutputStream fos = new FileOutputStream(fo.getPath())) {
                                props.store(fos);
                            }
                        }

                    }
                }
            }
        } finally {
            source.close();
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

    protected static void filterBuildXML(FileObject fo, ZipInputStream str, String name) throws IOException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            FileUtil.copy(str, baos);
            //---------------------------------------------------------------------
            // Pay attension tht third parameter. It's true to correctly 
            // work with namespaces. If false then all namespaces will be lost
            // For example:
            // <j2seproject3:javac gensrcdir="${build.generated.sources.dir}"/>
            // will be modified as follows:
            // <javac gensrcdir="${build.generated.sources.dir}"/>
            //---------------------------------------------------------------------
            Document doc = XMLUtil.parse(new InputSource(new ByteArrayInputStream(baos.toByteArray())), false, true, null, null);
            Element pel = doc.getDocumentElement();
            BaseUtils.out("1 Element pel=" + pel.getAttribute(name));
            pel.setAttribute("name", name);
            BaseUtils.out("2 Element pel=" + pel.getAttribute(name));
            try (OutputStream out = fo.getOutputStream()) {
                XMLUtil.write(doc, out, "UTF-8");
            }
        } catch (IOException | DOMException | SAXException ex) {
            BaseUtils.out("EXCEPTION " + ex.getMessage());
            LOG.log(Level.INFO, ex.getMessage());
            writeFile(str, fo);
        }

    }

}
