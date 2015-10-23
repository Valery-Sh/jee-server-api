/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jeeserver.base.deployment.maven;

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
import org.netbeans.spi.project.ProjectConfigurationProvider;
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
public class MavenAuxConfig {
    private static final Logger LOG = Logger.getLogger(MavenAuxConfig.class.getName());

/*    public static MavenAuxConfig customizedMavenProject(Project serverProject) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
*/
    private String activatedProfile;
    private String auxAttributeValue;
    private String nbactionsActivatedPath;
    private final List<String> nbactionsPaths = new ArrayList<>();
    private List<String> execArgs = new ArrayList<>();

    protected MavenAuxConfig() {

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
    //public static final String DEFAULT_PROFILE = "%%DEFAULT%%";
    public static final String DEFAULT_PROFILE = "<default config>";
    public static final String NBACTIONS = "nbactions";

    public static MavenAuxConfig getInstance(Project p) {
        MavenAuxConfig instance = new MavenAuxConfig();
        ProjectConfigurationProvider pcp = p.getLookup().lookup(ProjectConfigurationProvider.class);
        instance.activatedProfile = pcp.getActiveConfiguration().getDisplayName();
BaseUtil.out("MavenAuxConfig/ getInstance.activated = " + instance.activatedProfile);
        // Left for compatability
        Object o = p.getProjectDirectory().getAttribute(AUX_ATTR);
BaseUtil.out("MavenAuxConfig/ getInstance getAttribute(AUX_ATTR) " + o);        
/*        if (o == null || !o.getClass().equals(String.class)) {
            return instance;
        }
*/

        instance.auxAttributeValue = (String) o;
        instance.addNbActionPaths(p);
        instance.execArgs = instance.createExecArgs();        
        
/*        try (InputStream is = new ByteArrayInputStream(((String) o).getBytes())) {
            instance.parse(is);
            instance.addNbActionPaths(p);
            instance.execArgs = instance.createExecArgs();
        } catch (IOException | SAXException ex) {
            LOG.log(Level.INFO, ex.getMessage());
        }
*/
        return instance;
    }
/*
    ======== OLD VERSION ==================
    
    public static MavenAuxConfig getInstance(Project p) {
        MavenAuxConfig instance = new MavenAuxConfig();
    
        Object o = p.getProjectDirectory().getAttribute(AUX_ATTR);
        if (o == null || !o.getClass().equals(String.class)) {
            return instance;
        }

        instance.auxAttributeValue = (String) o;
        instance.addNbActionPaths(p);
        instance.execArgs = instance.createExecArgs();        
        
        try (InputStream is = new ByteArrayInputStream(((String) o).getBytes())) {
            instance.parse(is);
            instance.addNbActionPaths(p);
            instance.execArgs = instance.createExecArgs();
        } catch (IOException | SAXException ex) {
            LOG.log(Level.INFO, ex.getMessage());
        }

        return instance;
    }
*/    
    
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
        for ( int i=0; i < execArgs.size(); i++) {
            String arg = execArgs.get(i);
            if (arg.toUpperCase().startsWith("-X") || arg.toUpperCase().startsWith("-D") ) {
                line.append(arg);
                line.append(" ");
            }
            if ( arg.equals("-classpath") || arg.startsWith("%") || SourceVersion.isName(arg) ) {
                break;
            }
        }

        return line.toString().trim();
    }

    public List<String> getJvmArgs() {
        List<String> list = new ArrayList<>();

        for ( int i=0; i < execArgs.size(); i++) {
            String arg = execArgs.get(i);
            if (arg.toUpperCase().startsWith("-X") || arg.toUpperCase().startsWith("-D") ) {
                list.add(arg);
            }
            if ( arg.equals("-classpath") || arg.startsWith("%") || SourceVersion.isName(arg) ) {
                break;
            }
        }

        return list;
    }
    

/*    protected void parse(InputStream is) throws IOException, SAXException {
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
*/
    protected void addNbActionPaths(Project p) {

        for (FileObject fo : p.getProjectDirectory().getChildren()) {
            if (fo.isFolder() || !("xml".equals(fo.getExt()) && fo.getName().startsWith(NBACTIONS))) {
                continue;
            }
            this.nbactionsPaths.add(fo.getPath());
            if (DEFAULT_PROFILE.equals(activatedProfile) && NBACTIONS.equals(fo.getName())) {
                this.nbactionsActivatedPath = fo.getPath();
            } else if (fo.getName().equals(NBACTIONS + "-" + activatedProfile)) {
                this.nbactionsActivatedPath = fo.getPath();
            }
        }
    }

    protected List<String> createExecArgs() {
        List<String> args = new ArrayList<>();
BaseUtil.out("MavenAuxConfig/ createExecArgs nbactionsActivatedPath = " + nbactionsActivatedPath );        
BaseUtil.out("MavenAuxConfig/ createExecArgs activatedProfile = " + activatedProfile );        
        
        if (activatedProfile == null || nbactionsActivatedPath == null
                || !new File(nbactionsActivatedPath).exists()) {
            return args;
        }
        FileObject nbactios = FileUtil.toFileObject(new File(nbactionsActivatedPath));

        try (InputStream is = nbactios.getInputStream()) {
            parseNbActions(is, args);
BaseUtil.out("MavenAuxConfig/ createExecArgs AFTER  parseNbActions args.size=" + args.size());        
            args.forEach(a -> {
                BaseUtil.out("MavenAuxConfig/ createExecArgs AFTER  parseNbActions arg=" + a);        
            });
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
     * @param specifiedClass
     * @return
     */
    public static MavenAuxConfig customizeMainClass(Project project, String specifiedClass) {

        MavenAuxConfig config = MavenMainClassCustomizer2.customize(project, specifiedClass);
        if (config == null) {
            return new MavenAuxConfig();
        }
        return config;

    }

    public static MavenAuxConfig customizeMainClass(Project project) {
        return customizeMainClass(project, null);
    }

    
}
