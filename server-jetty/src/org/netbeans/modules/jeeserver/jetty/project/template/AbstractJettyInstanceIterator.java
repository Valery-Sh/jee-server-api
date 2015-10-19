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
package org.netbeans.modules.jeeserver.jetty.project.template;

import java.awt.Component;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceCreationException;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.netbeans.modules.jeeserver.jetty.project.JettyProject;
import org.netbeans.modules.jeeserver.jetty.util.JettyConstants;
import org.netbeans.modules.jeeserver.jetty.util.StartdIniHelper;
import org.netbeans.modules.jeeserver.jetty.util.Utils;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.EditableProperties;
import org.openide.util.NbBundle;
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
public abstract class AbstractJettyInstanceIterator implements WizardDescriptor.InstantiatingIterator {

    private static final Logger LOG = Logger.getLogger(AbstractJettyInstanceIterator.class.getName());

    private int index;
    private WizardDescriptor.Panel[] panels;
    private WizardDescriptor wiz;

    public AbstractJettyInstanceIterator() {
    }

    private WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[]{
            new JettyServerInstanceWizardPanel(),};
    }

    private String[] createSteps() {
        return new String[]{
            NbBundle.getMessage(JettyAddInstanceIterator.class, "LBL_CreateProjectStep")
        };
    }

    @Override
    public abstract Set/*<FileObject>*/ instantiate() throws IOException;

    protected InputStream getTemplateInputStream() {

        String s = (String) wiz.getProperty(BaseConstants.HOME_DIR_PROP);
        BaseUtil.out("****** ZIP TEMPLATE=" + JettyProperties.getProjectZipPath(s));
        return getClass().getClassLoader().getResourceAsStream("/"
                + JettyProperties.getProjectZipPath(s));
    }

    protected Set<FileObject> runInstantiateProjectDir(Set<FileObject> resultSet) throws IOException {
        File dirF = FileUtil.normalizeFile((File) wiz.getProperty("projdir"));
        dirF.mkdirs();

        FileObject dir = FileUtil.toFileObject(dirF);

        unZipFile(getTemplateInputStream(), dir);
        createBuildXml(dir);
        // Always open top dir as a project:
        resultSet.add(dir);
        // Look for nested projects to open as well:
        Enumeration<? extends FileObject> e = dir.getFolders(true);
        while (e.hasMoreElements()) {
            FileObject subfolder = e.nextElement();
            if (subfolder.getNameExt().equals(JettyConstants.JETTYBASE_FOLDER)) {
                continue;
            }
            if (ProjectManager.getDefault().isProject(subfolder)) {
                resultSet.add(subfolder);
            }
        }
        File parent = dirF.getParentFile();
        if (parent != null && parent.exists()) {
            ProjectChooser.setProjectsFolder(parent); // Last used folder with a new project
        }
        instantiateStartDIniFiles(dir);
        JettyProject.enableJSFLibrary(dir);
        return resultSet;
    }

    public void createBuildXml(FileObject projectDir) throws IOException {

        String buildxml = Utils.stringOf(AbstractJettyInstanceIterator.class.getResourceAsStream("/org/netbeans/modules/jeeserver/jetty/resources/build.template"));
        buildxml = buildxml.replace("${jetty_server_instance_name}", "\"" + projectDir.getNameExt() + "\"");

        FileObject fo = projectDir.getFileObject(JettyConstants.JETTYBASE_FOLDER + "/build.xml");
        InputStream is = new ByteArrayInputStream(buildxml.getBytes());
        OutputStream os = fo.getOutputStream();

        try {
            FileUtil.copy(is, os);
        } finally {
            try {
                is.close();
                os.close();
            } catch (IOException ex) {
                LOG.log(Level.INFO, ex.getMessage()); //NOI18N
            }
        }
    }

    /**
     * !!! NOT USED
     *
     * @param projectDir
     * @throws IOException
     */
/*    public void modifyNPNModule(FileObject projectDir) throws IOException {
        String npn = (String) wiz.getProperty(PROP_ENABLE_NPN);
        boolean enabledNPN = true;

        if (npn == null || !Boolean.parseBoolean(npn)) {
            enabledNPN = false;
        }

        FileObject npnFo = projectDir.getFileObject("jetty.base/modules/npn.mod");
        FileObject npnOldFo = projectDir.getFileObject("jetty.base/modules/npn.old.mod");

        if (enabledNPN && npnOldFo != null) {
            npnFo.delete();
        }

    }
*/
    protected Set<FileObject> instantiateProjectDir(final Set<FileObject> resultSet) throws IOException {

        FileUtil.runAtomicAction(new Runnable() {
            @Override
            public void run() {
                try {
                    runInstantiateProjectDir(resultSet);
                } catch (IOException ex) {
                    LOG.log(Level.FINE, ex.getMessage()); //NOI18N
                }
            }
        });
        return resultSet;
    }

    protected Set<InstanceProperties> instantiateServerProperties() {
        Set<InstanceProperties> result = new HashSet<>();
        Map<String, String> ipmap = getPropertyMap();
        String url = ipmap.get(BaseConstants.URL_PROP);
        String displayName = ipmap.get(BaseConstants.DISPLAY_NAME_PROP);

        try {
            InstanceProperties ip = InstanceProperties.createInstanceProperties(url, null, null, displayName, ipmap);
            result.add(ip);
        } catch (InstanceCreationException ex) {
            LOG.log(Level.SEVERE, ex.getMessage()); //NOI18N
        }

        return result;
    }

    protected void instantiateStartDIniFiles(FileObject projDir) {
        StartdIniHelper helper = new StartdIniHelper(wiz);
        helper.instantiateStartdIni(projDir, "jsf");
        helper.instantiateStartdIni(projDir, "cdi");
        helper.instantiateStartdIni(projDir, "https");
        helper.instantiateStartdIni(projDir, "spdy");
        helper.instantiateStartdIni(projDir, "ssl");// must be after spdy and https
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
        String jettyVersion = Utils.getJettyVersion(jettyHome);

        String displayName = projectDir.getNameExt();

        ip.put(BaseConstants.SERVER_ID_PROP, serverId);
        ip.put(BaseConstants.DISPLAY_NAME_PROP, displayName);

        ip.put(BaseConstants.HOST_PROP, (String) wiz.getProperty(BaseConstants.HOST_PROP));
        ip.put(BaseConstants.HTTP_PORT_PROP, (String) wiz.getProperty(BaseConstants.HTTP_PORT_PROP));
        ip.put(BaseConstants.DEBUG_PORT_PROP, (String) wiz.getProperty(BaseConstants.DEBUG_PORT_PROP));
        ip.put(BaseConstants.SHUTDOWN_PORT_PROP, (String) wiz.getProperty(BaseConstants.SHUTDOWN_PORT_PROP));
        ip.put(BaseConstants.URL_PROP, url);
        ip.put(BaseConstants.HOME_DIR_PROP, jettyHome);
        //ip.put(BaseConstants.SERVER_LOCATION_PROP, projectDir.getPath());
        BaseUtil.setServerLocation(ip, projectDir.getPath());
        ip.put(BaseConstants.SERVER_VERSION_PROP, jettyVersion);

        return ip;
    }

    @Override
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        index = 0;
        panels = createPanels();
        // Make sure list of steps is accurate.
        String[] steps = createSteps();
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                // TODO if using org.openide.dialogs >= 7.8, can use WizardDescriptor.PROP_*:
                jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i));
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps);
            }
        }
    }

    @Override
    public void uninitialize(WizardDescriptor wiz) {
        this.wiz.putProperty("projdir", null);
        this.wiz.putProperty("name", null);
        this.wiz = null;
        panels = null;
    }

    @Override
    public String name() {
        return MessageFormat.format("{0} of {1}",
                new Object[]{new Integer(index + 1), new Integer(panels.length)});
    }

    @Override
    public boolean hasNext() {
        return index < panels.length - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    @Override
    public WizardDescriptor.Panel current() {
        return panels[index];
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    @Override
    public final void addChangeListener(ChangeListener l) {
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
    }

    private void unZipFile(InputStream source, FileObject projectRoot) throws IOException {

        JettyProperties jvs = JettyProperties.getInstance(wiz);

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
                        if (JettyConstants.JETTY_HTTP_INI.equals(entry.getName())) {
                            EditableProperties props = new EditableProperties(false);
                            try (FileInputStream fis = new FileInputStream(fo.getPath())) {
                                props.load(fis);
                            }
//                            props.setProperty(JettyConstants.JETTY_HTTP_PORT, (String) wiz.getProperty(BaseConstants.HTTP_PORT_PROP));
//                            props.setProperty(JettyConstants.JETTY_HTTP_TIMEOUT, (String) wiz.getProperty(JettyConstants.JETTY_HTTP_TIMEOUT));
                            //props.setProperty(jvs.propertyName(JettyConstants.JETTY_HTTP_PORT), (String) wiz.getProperty(BaseConstants.HTTP_PORT_PROP));
                            props.setProperty(jvs.getHttpPortPropertyName(), (String) wiz.getProperty(BaseConstants.HTTP_PORT_PROP));
                            props.setProperty(jvs.getTimeoutPropertyName(), (String) wiz.getProperty(JettyConstants.JETTY_HTTP_TIMEOUT));

                            try (FileOutputStream fos = new FileOutputStream(fo.getPath())) {
                                props.store(fos);
                            }
                        }

                    }
                }
            }
        } finally {
            source.close();
            str.close();
        }
    }

    private static void writeFile(ZipInputStream str, FileObject fo) throws IOException {
        try (OutputStream out = fo.getOutputStream()) {
            FileUtil.copy(str, out);
        }
    }

    private static void filterProjectXML(FileObject fo, ZipInputStream str, String name) throws IOException {
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
