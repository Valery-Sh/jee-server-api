package org.netbeans.modules.jeeserver.base.embedded.project.web;

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
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.JComponent;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.jeeserver.base.embedded.utils.EmbConstants;
import org.netbeans.modules.jeeserver.base.embedded.utils.EmbUtils;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.openide.DialogDisplayer;
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
 * Instances of this class launch and track a wizard iterator that creates a new
 * Web application.
 *
 * @see ESWebAppTemplateWizardIterator
 * @author V. Shyshkin
 */
public final class EmbNewWebAppWizardPerformer {

    private static final Logger LOG = Logger.getLogger(EmbNewWebAppWizardPerformer.class.getName());
    private WizardDescriptor wiz;

    private Project server;

    /**
     * Create a new instance of the class for the given server project.
     *
     * @param server embedded server project instance
     */
    public EmbNewWebAppWizardPerformer(Project server) {
        this.server = server;

    }

    public void perform() {
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>();
        panels.add(new EmbWebAppTemplateWizardPanel());
        String[] steps = new String[panels.size()];
        for (int i = 0; i < panels.size(); i++) {
            Component c = panels.get(i).getComponent();
            // Default step name to component name of panel.
            steps[i] = c.getName();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
            }
        }
        wiz = new WizardDescriptor(new WizardDescriptor.ArrayIterator<WizardDescriptor>(panels));
        FileObject rootDir = server.getProjectDirectory().getFileObject(EmbConstants.WEBAPPLICATIONS_FOLDER);
        wiz.putProperty("rootPath", rootDir.getPath());
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle("...dialog title...");
        wiz.putProperty("name", buildProjectName());

        Set<Profile> jeeProfiles;
        jeeProfiles = EmbUtils.getJavaEEProfiles(server);

        wiz.putProperty("jeeProfiles", jeeProfiles);

        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
            try {
                instantiate();
            } catch (IOException ex) {
                LOG.log(Level.INFO, ex.getMessage());
            }
        }
    }

    protected String buildProjectName() {

        String result = "WebApp";
        String name = "webapp";

        Project[] projects = OpenProjects.getDefault().getOpenProjects();
        Set<Integer> set = new HashSet<>();
        for (Project p : projects) {
            String nm = p.getProjectDirectory().getName().toLowerCase();
            if (nm.equals(name)) {
                set.add(-1);
            } else if (nm.startsWith(name)) {
                String last = nm.substring(name.length());
                try {
                    int i = Integer.parseInt(last);
                    if (i > 0) {
                        set.add(i);
                    }
                } catch (NumberFormatException e) {
                }
            }
        }
        if (set.isEmpty() || !set.contains(-1) || set.size() == 1) {
            return result;
        }

        set.remove(-1);

        Integer[] numbers = new Integer[set.size()];

        numbers = set.toArray(numbers);
        Arrays.sort(numbers);
        if (numbers[0] > 0) {
            return result + "0";
        }
        int i = numbers[0];
        for (int n : numbers) {
            if (n - i > 1) {
                i++;
                break;
            }
            i = n;
        }
        return result + i;
    }

    public Set<FileObject> instantiate(/*ProgressHandle handle*/) throws IOException {

        final Set<FileObject> resultSet = new LinkedHashSet<>();
        FileObject rootDir = server.getProjectDirectory().getFileObject(EmbConstants.WEBAPPLICATIONS_FOLDER);
        File f = new File(rootDir.getPath() + "/" + (String) wiz.getProperty("name"));
        final File dirF = FileUtil.normalizeFile(f);

        FileUtil.runAtomicAction(new Runnable() {
            @Override
            public void run() {
                try {
                    doInstantiate(resultSet, dirF);
                } catch (IOException ex) {
                }
            }
        });

        final FileObject dir = FileUtil.toFileObject(dirF);

        Project p = FileOwnerQuery.getOwner(dir);
        ProjectManager.getDefault().clearNonProjectCache();
        OpenProjects.getDefault().open(new Project[]{p}, false);
        return resultSet;
    }

    public Set<FileObject> doInstantiate(Set<FileObject> resultSet, File dirF) throws IOException {

        dirF.mkdirs();

        FileObject dir = FileUtil.toFileObject(dirF);

        InputStream stream = getClass().getResourceAsStream("EmbWebAppTemplateProject.zip");

        unZipFile(stream, dir);
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

        return resultSet;
    }

    private String getEEPlatformProperty() {
        String value = (String) wiz.getProperty("jeeProfile");
        Set<Profile> ps = (Set<Profile>) wiz.getProperty("jeeProfiles");
        for (Profile p : ps) {
            if (p.getDisplayName().equals(value)) {
                value = p.toPropertiesString();
            }
        }

        return value;
    }

    private void unZipFile(InputStream source, FileObject projectRoot) throws IOException {
        try {
            ZipInputStream str = new ZipInputStream(source);
            ZipEntry entry;
            String uri = BaseUtils.getUri(server);
            while ((entry = str.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    FileUtil.createFolder(projectRoot, entry.getName());
                } else {
                    FileObject fo;
                    switch (entry.getName()) {
                        case "nbproject/private/private.properties":
                            fo = FileUtil.createData(projectRoot, "nbproject/private/private.properties");
                            break;
                        default:
                            fo = FileUtil.createData(projectRoot, entry.getName());
                            break;
                    }
                    if ("nbproject/project.xml".equals(entry.getName())) {
                        // Special handling for setting name of Ant-based projects; customize as needed:
                        filterProjectXML(fo, str, projectRoot.getName());
                    } else {
                        writeFile(str, fo);
                        switch (entry.getName()) {
                            case "nbproject/project.properties": {
                                Properties props = new Properties();
                                FileInputStream fis = new FileInputStream(fo.getPath());
                                props.load(fis);

                                props.setProperty("war.name", projectRoot.getName() + ".war");
                                props.setProperty("j2ee.platform", getEEPlatformProperty());
                                try (FileOutputStream fos = new FileOutputStream(fo.getPath())) {
                                    props.store(fos, "");
                                }
                                break;
                            }
                            case "nbproject/private/private.properties": {
                                Properties props = new Properties();
                                try (FileInputStream fis = new FileInputStream(fo.getPath())) {
                                    props.load(fis);
                                }

                                props.setProperty("j2ee.server.instance", uri);
                                try (FileOutputStream fos = new FileOutputStream(fo.getPath())) {
                                    props.store(fos, "");
                                }
                                break;
                            }
                        }

                    }
                }
            }
        } finally {
            try {
                source.close();
            } catch (Exception ex) {
                BaseUtils.out("SOURCE==" + source + "; EXCEPTION=" + ex.getMessage());
            }
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
        } catch (IOException | SAXException | DOMException ex) {
            LOG.log(Level.INFO, ex.getMessage());
            writeFile(str, fo);
        }

    }
}
