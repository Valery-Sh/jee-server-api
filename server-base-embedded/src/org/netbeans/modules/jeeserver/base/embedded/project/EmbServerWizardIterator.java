package org.netbeans.modules.jeeserver.base.embedded.project;

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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.jeeserver.base.embedded.utils.EmbConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.EditableProperties;
//import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.xml.XMLUtil;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

// TODO define position attribute

/**
 *
 * @author Valery
 */
@TemplateRegistration(folder = "Project/EmbeddedServer",
        displayName = "#EmbEmbeddedServer_displayName",
        description = "EmbServerDescription.html",
        iconBase = "org/netbeans/modules/jeeserver/base/embedded/resources/server.png",
        content = "EmbServerProject.zip")
@Messages("EmbEmbeddedServer_displayName=Embedded Server")
public class EmbServerWizardIterator implements WizardDescriptor./*Progress*/InstantiatingIterator {

    private static final Logger LOG = Logger.getLogger(EmbServerWizardIterator.class.getName());

    private int index;
    private WizardDescriptor.Panel[] panels;
    private WizardDescriptor wiz;

    public EmbServerWizardIterator() {
    }

    public static EmbServerWizardIterator createIterator() {
        return new EmbServerWizardIterator();
    }

    private WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[]{
            new EmbServerWizardPanel(),};
    }

    private String[] createSteps() {
        return new String[]{
            NbBundle.getMessage(EmbServerWizardIterator.class, "LBL_CreateProjectStep")
        };
    }

    @Override
    public Set<FileObject> instantiate(/*ProgressHandle handle*/) throws IOException {
        final Set<FileObject> resultSet = new LinkedHashSet<FileObject>();
        FileUtil.runAtomicAction(new Runnable() {
            @Override
            public void run() {
                try {
                    doInstantiate(resultSet);
                } catch (IOException ex) {
                    LOG.log(Level.FINE, ex.getMessage()); //NOI18N
                }
            }

        });
        return resultSet;
    }

    public Set<FileObject> doInstantiate(Set<FileObject> resultSet) throws IOException {
        //Set<FileObject> resultSet = new LinkedHashSet<FileObject>();
        File dirF = FileUtil.normalizeFile((File) wiz.getProperty("projdir"));
        dirF.mkdirs();

        FileObject template = Templates.getTemplate(wiz);
        FileObject dir = FileUtil.toFileObject(dirF);
        
        unZipFile(template.getInputStream(), dir);
        // Always open top dir as a project:
        resultSet.add(dir);
        // Look for nested projects to open as well:
        Enumeration<? extends FileObject> e = dir.getFolders(true);
        while (e.hasMoreElements()) {
            FileObject subfolder = e.nextElement();
            if (ProjectManager.getDefault().isProject(subfolder)) {
                resultSet.add(subfolder);
            }
        }
        File parent = dirF.getParentFile();
        if (parent != null && parent.exists()) {
            ProjectChooser.setProjectsFolder(parent);
        }
        setServerProperties(dir);
        Project p = FileOwnerQuery.getOwner(dir);
        String serverId = (String) wiz.getProperty(EmbConstants.SERVER_ID_PROP);
        String actualServerId = (String) wiz.getProperty(EmbConstants.SERVER_ACTUAL_ID_PROP);

        if (p != null) {
            //setServerClassPath(p, serverId,actualServerId);
        }
        HashMap<String, Object> map = new HashMap<>();
        map.putAll(wiz.getProperties());
        BaseUtils.getServerSpecifics(serverId).projectCreated(dir, map);
        return resultSet;
    }

    private void setServerProperties(final FileObject projectDir) {
        try {
            FileObject fo = projectDir.getFileObject(EmbConstants.INSTANCE_PROPERTIES_PATH);
            EditableProperties props = new EditableProperties(false);
            try {
                FileInputStream fos = new FileInputStream(fo.getPath());
                props.load(fos);
            } catch (IOException ex) {
                LOG.log(Level.INFO, ex.getMessage());
            }
            String serverId = (String) wiz.getProperty(EmbConstants.SERVER_ID_PROP);
            String actualServerId = (String) wiz.getProperty(EmbConstants.SERVER_ACTUAL_ID_PROP);
            
            String url = serverId + ":" + EmbConstants.URIPREFIX_NO_ID + ":" + projectDir.getPath();

            FileLock lock = fo.lock();
            FileOutputStream fos = new FileOutputStream(fo.getPath());

            try {
                props.put(EmbConstants.SERVER_ID_PROP, serverId);
                props.put(EmbConstants.HOST_PROP, (String) wiz.getProperty(EmbConstants.HOST_PROP));
                props.put(EmbConstants.HTTP_PORT_PROP, (String) wiz.getProperty(EmbConstants.HTTP_PORT_PROP));
                props.put(EmbConstants.DEBUG_PORT_PROP, (String) wiz.getProperty(EmbConstants.DEBUG_PORT_PROP));
                props.put(EmbConstants.SHUTDOWN_PORT_PROP, (String) wiz.getProperty(EmbConstants.SHUTDOWN_PORT_PROP));
                props.put(EmbConstants.INCREMENTAL_DEPLOYMENT, (String) wiz.getProperty(EmbConstants.INCREMENTAL_DEPLOYMENT));

                props.put(EmbConstants.URL_PROP, url);
                props.put(EmbConstants.SERVER_ACTUAL_ID_PROP, actualServerId);

                props.store(fos);
                fos.close();

            } catch (IOException ex) {
                LOG.log(Level.WARNING, ex.getMessage()); //NOI18N    
            } finally {
                lock.releaseLock();
            }

            //  }
            //});
        } catch (IOException ex) {
            LOG.log(Level.WARNING, ex.getMessage()); //NOI18N
        }

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

    private void unZipFile(final InputStream source, final FileObject projectRoot) throws IOException {

        try {
            ZipInputStream str = new ZipInputStream(source);
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
                        if ("nbproject/project.properties".equals(entry.getName())) {
                            EditableProperties props = new EditableProperties(false);
                            FileInputStream fis = new FileInputStream(fo.getPath());
                            props.load(fis);
                            fis.close();
                            props.setProperty("dist.jar", "${dist.dir}/" + projectRoot.getName() + ".jar");
                            // javac.classpath must be modified
                            //setJavacClasspath(props);
                            FileOutputStream fos = new FileOutputStream(fo.getPath());
                            props.store(fos);
                            fos.close();
                        }

                    }
                }
            }
        } catch (IOException ex) {
            LOG.log(Level.FINE, ex.getMessage()); //NOI18N

        } finally {
            //try {
            source.close();
            //} catch (IOException ex) {
            //}
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
            //XMLUtil.p
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
            LOG.log(Level.FINE, ex.getMessage()); //NOI18N
            writeFile(str, fo);
        }

    }

    /**
     * TODO (may be) Returns a list of libraries to be included in the class
     * path of a web project. The result list contains all libraries from the
     * server project class path excluding the ones whose name starts with
     * <i>&lt.server-id.gt.-&lt.server-helper&gt.</i>. For example
     * <code>jetty9-server-helper</code> will be excluded for jetty9 embedded
     * server.
     *
     * @param project
     * @param serverId
     * @param actualServerId
     */
    protected void setServerClassPath_NOT_NEEDED(Project project, String serverId,String actualServerId) {
        if (project == null) {
            return;
        }
        Library[] libs = LibraryManager.getDefault().getLibraries();
        List<Library> libList = new ArrayList<>();

        for (Library lib : libs) {
            if (lib.getName().toLowerCase().equals(serverId + "-" + EmbConstants.SERVER_HELPER_LIBRARY_POSTFIX)
                    || lib.getName().toLowerCase().equals(serverId + "-" + EmbConstants.SERVER_ALL_LIBRARY_POSTFIX )
                    || lib.getName().toLowerCase().equals(actualServerId + "-" + EmbConstants.SERVER_HELPER_LIBRARY_POSTFIX )
                    || lib.getName().toLowerCase().equals(actualServerId + "-" + EmbConstants.SERVER_ALL_LIBRARY_POSTFIX)) 
            {
                libList.add(lib);
            }
        }
        libs = new Library[libList.size()];
        //Library[] resultLibs = libList.toArray(libs);
        libList.toArray(libs);
        try {
            ProjectClassPathModifier.addLibraries(libs, getSourceRoot(project), ClassPath.COMPILE);
        } catch (IOException | UnsupportedOperationException ex) {
            LOG.log(Level.FINE, ex.getMessage()); //NOI18N

        }

    }

    protected FileObject getSourceRoot(Project p) {
        Sources sources = ProjectUtils.getSources(p);
        SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        FileObject result = null;
        try {
            for (SourceGroup sourceGroup : sourceGroups) {
                result = sourceGroup.getRootFolder();
                break;

            }
        } catch (UnsupportedOperationException ex) {
            LOG.log(Level.FINE, ex.getMessage()); //NOI18N
        }
        return result;
    }
    
/*    protected void addJarToServerClassPath(File jar, Project p) {
        if (p == null || jar == null || !jar.exists() ) {
            return;
        }
        try {
            ProjectClassPathModifier.addRoots(new URI[] {Utilities.toURI(jar)}, getSourceRoot(p), ClassPath.COMPILE);
        } catch (IOException ex) {
            LOG.log(Level.FINE, ex.getMessage()); //NOI18N
        } 

    }
*/
}//class
