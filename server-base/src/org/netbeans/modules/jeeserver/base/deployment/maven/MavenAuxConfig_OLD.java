package org.netbeans.modules.jeeserver.base.deployment.maven;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.SourceVersion;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Valery
 */
public class MavenAuxConfig_OLD {

    private static final Logger LOG = Logger.getLogger(MavenAuxConfig_OLD.class.getName());

    /*    public static MavenAuxConfig_OLD customizedMavenProject(Project serverProject) {
     throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
     */
    private String activatedProfile;
    private String auxAttributeValue;
    private String nbactionsActivatedPath;
    private final List<String> nbactionsPaths = new ArrayList<>();
    private List<String> execArgs = new ArrayList<>();

    protected MavenAuxConfig_OLD() {

    }

    public String getActivatedProfile() {
        return activatedProfile;
    }

    public String getAuxAttributeValue() {
        return auxAttributeValue;
    }

    public String getNbactionsActivatedPath() {
        return nbactionsActivatedPath;
    }

    public List<String> getNbactionsPaths() {
        return nbactionsPaths;
    }

    public static final String AUX_ATTR = "AuxilaryConfiguration";
    public static final String CONFIG_DATA = "config-data";
    public static final String ACTIVATED = "activated";
    public static final String OPEN_FILES = "open-files";
    public static final String DEFAULT_PROFILE = "%%DEFAULT%%";
    public static final String NBACTIONS = "nbactions";

    public static MavenAuxConfig_OLD getInstance(Project p) {
        MavenAuxConfig_OLD instance = new MavenAuxConfig_OLD();
        Object o = p.getProjectDirectory().getAttribute(AUX_ATTR);
        if (o == null || !o.getClass().equals(String.class)) {
            return instance;
        }

        instance.auxAttributeValue = (String) o;

        try (InputStream is = new ByteArrayInputStream(((String) o).getBytes())) {
            instance.parse(is);
            instance.addNbActionPaths(p, instance);
            instance.execArgs = instance.createExecArgs();
        } catch (IOException | SAXException ex) {
            LOG.log(Level.INFO, ex.getMessage());
        }

        return instance;
    }

    public List<String> getAllExecArgs() {
        return execArgs;
    }

    public String getMainClass() {
        String mainClass = null;

//        int len = execArgs.size();
        for (String arg : execArgs) {
            if (SourceVersion.isName(arg)) {
                mainClass = arg;
                break;
            }
        }

        /*        if (len > 0 && SourceVersion.isName(execArgs.get(0))) {
         mainClass = execArgs.get(0);
         } else if (len > 1 && SourceVersion.isName(execArgs.get(1))) {
         mainClass = execArgs.get(1);
         } else if (len > 2 && SourceVersion.isName(execArgs.get(2))) {
         mainClass = execArgs.get(2);
         }
         */
        return mainClass;
    }

    public List<String> getProgramArgs() {
        List<String> list = new ArrayList<>();
        int start = 0;
        for (int i = 0; i < execArgs.size(); i++) {
            if (SourceVersion.isName(execArgs.get(i))) {
                start = i + 1;
                break;
            }
        }
        if (execArgs.size() != start) {
            for (int i = start; i < execArgs.size(); i++) {
                list.add(execArgs.get(i));
            }
        }

        return list;
    }

    public String getProgramArgsLine() {
        List<String> list = getProgramArgs();
        StringBuilder line = new StringBuilder();
        list.forEach(arg -> {
            line.append(arg);
            line.append(" ");
        });

        return line.toString().trim();
    }

    public String getJvmArgsLine() {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < execArgs.size(); i++) {
            String arg = execArgs.get(i);
            if (arg.toUpperCase().startsWith("-X") || arg.toUpperCase().startsWith("-D")) {
                line.append(arg);
                line.append(" ");
            }
            if (arg.equals("-classpath") || arg.startsWith("%") || SourceVersion.isName(arg)) {
                break;
            }
        }

        return line.toString().trim();
    }

    public List<String> getJvmArgs() {
        List<String> list = new ArrayList<>();

        for (int i = 0; i < execArgs.size(); i++) {
            String arg = execArgs.get(i);
            if (arg.toUpperCase().startsWith("-X") || arg.toUpperCase().startsWith("-D")) {
                list.add(arg);
            }
            if (arg.equals("-classpath") || arg.startsWith("%") || SourceVersion.isName(arg)) {
                break;
            }
        }

        return list;
    }

    protected void parse(InputStream is) throws IOException, SAXException {
        Document doc = XMLUtil.parse(new InputSource(is), false, true, null, null);
        NodeList nl = doc.getDocumentElement().getElementsByTagName(ACTIVATED);
        if (nl != null) {
            for (int i = 0; i < nl.getLength(); i++) {
                Element el = (Element) nl.item(i);
                if (!CONFIG_DATA.equals(el.getParentNode().getNodeName())) {
                    continue;
                }
                this.activatedProfile = el.getTextContent();
                break;
            }
        }
    }

    protected void addNbActionPaths(Project p, MavenAuxConfig_OLD instance) {

        for (FileObject fo : p.getProjectDirectory().getChildren()) {
            if (fo.isFolder() || !("xml".equals(fo.getExt()) && fo.getName().startsWith(NBACTIONS))) {
                continue;
            }
            this.nbactionsPaths.add(fo.getPath());
            if (DEFAULT_PROFILE.equals(fo.getName()) && NBACTIONS.equals(fo.getName())) {
                this.nbactionsActivatedPath = fo.getPath();
            } else if (fo.getName().equals(NBACTIONS + "-" + activatedProfile)) {
                this.nbactionsActivatedPath = fo.getPath();
            }
        }
    }

    protected List<String> createExecArgs() {
        List<String> args = new ArrayList<>();
        if (activatedProfile == null || nbactionsActivatedPath == null
                || !new File(nbactionsActivatedPath).exists()) {
            return args;
        }
        FileObject nbactios = FileUtil.toFileObject(new File(nbactionsActivatedPath));

        try (InputStream is = nbactios.getInputStream()) {
            parseNbActions(is, args);
        } catch (IOException | SAXException ex) {
            LOG.log(Level.INFO, ex.getMessage());
            return args;
        }
        return args;
    }

    protected void parseNbActions(InputStream is, List<String> args) throws IOException, SAXException {
        Document doc = XMLUtil.parse(new InputSource(is), false, true, null, null);
        NodeList nl = doc.getDocumentElement().getElementsByTagName("action");
        if (nl == null && nl.getLength() == 0) {
            return;
        }
        Element actionEl = findActionElement(nl);

        if (actionEl == null) {
            return;
        }

        NodeList propNodeList = actionEl.getElementsByTagName("properties");
        if (propNodeList == null || propNodeList.getLength() == 0) {
            return;
        }

        Element propEl = (Element) propNodeList.item(0);

        NodeList argsNl = propEl.getElementsByTagName("exec.args");
        if (argsNl == null || argsNl.getLength() == 0) {
            return;
        }

        Element argsEl = (Element) argsNl.item(0);

        String c = argsEl.getTextContent();

        if (c == null || c.trim().isEmpty()) {
            return;
        }
        String[] argArray = c.split(" ");
        for (String s : argArray) {
            BaseUtil.out("MavenAuxConfig ARGS " + s);
            args.add(s);
        }
    }

    protected Element findActionElement(NodeList nodeList) {
        Element result = null;
        //
        // Scans all <action> elements
        //
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element actionEl = (Element) nodeList.item(i);

            if ((getRunActionNameElement(actionEl)) != null) {
                result = actionEl;
                break;
            }
        }
        return result;
    }

    protected Element getRunActionNameElement(Element actionEl) {
        Element result = null;

        NodeList nodeList = actionEl.getElementsByTagName("actionName");
        if (nodeList == null || nodeList.getLength() == 0) {
            return result;
        }

        Element actionNameEl = (Element) nodeList.item(0);

        if (actionNameEl.getTextContent() == null) {
            return result;
        }

        if ("RUN".equals(actionNameEl.getTextContent().toUpperCase())) {
            result = actionNameEl;
        }
        return result;
    }

    /**
     *
     * @param project
     * @return
     */
    public static MavenAuxConfig_OLD customizeMainClass(Project project, String specifiedClass) {
        return null;

        /*        MavenAuxConfig_OLD config = MavenMainClassCustomizer2.customize(project, specifiedClass);
         if (config == null) {
         return new MavenAuxConfig_OLD();
         }
         return config;

         }

         public static MavenAuxConfig_OLD customizeMainClass(Project project) {
         return customizeMainClass(project, null);
         }
         */
    }
}
