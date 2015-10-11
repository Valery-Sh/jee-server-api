package org.netbeans.modules.jeeserver.jetty.embedded;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteConstants;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
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
public class JettyMavenInstanceBuilder extends JettyInstanceBuilder {

    private static final Logger LOG = Logger.getLogger(JettyInstanceBuilder.class.getName());

    @StaticResource
    public static final String zipMavenTemplatePath = "org/netbeans/modules/jeeserver/jetty/embedded/resources/JettyEmbeddedMavenTemplate.zip";//JettyServerInstanceProject.zip";    

    public JettyMavenInstanceBuilder(Properties configProps, Options options) {
        super(configProps, options);
    }

    @Override
    protected FileObject getSrcDir(Project p) {
        return p.getProjectDirectory().getFileObject("src/main/java");
    }

    @Override
    protected FileObject getLibDir(Project p) {
        return p.getProjectDirectory().getFileObject(SuiteConstants.MAVEN_REPO_LIB_PATH);
    }

    @Override
    public FileObject createLib(Project project) {
        FileObject libFo = null;

        File libFolder;
        FileObject fo;
        libFolder = new File(project.getProjectDirectory().getPath() + "/nbdeployment/lib");
        fo = project.getProjectDirectory().getFileObject("nbdeployment/lib");

        if (fo == null) {
            try {
                libFo = FileUtil.createFolder(libFolder);
            } catch (IOException ex) {
                LOG.log(Level.INFO, ex.getMessage()); //NOI18N
            }
        }
        return libFo;
    }

    /**
     *
     * @param project
     */
    @Override
    protected void modifyPomXml(Project project) {
        FileObject projDir = project.getProjectDirectory();

        String jarPath = SuiteConstants.MAVEN_REPO_LIB_PATH +"/" + getCommandManagerJarTemplateName() + ".jar";
        
        FileObject jarFo = projDir.getFileObject(jarPath);
        if (jarFo == null) {
            return;
        }

        Properties pomProps = BaseUtil.getPomProperties(projDir.getFileObject(jarPath));
        if (pomProps == null) {
            return;
        }
        Document doc;
        try (InputStream is = projDir.getFileObject("pom.xml").getInputStream()) {
            //---------------------------------------------------------------------
            // Pay attension tht third parameter. It's true to correctly 
            // work with namespaces. If false then all namespaces will be lost
            // For example:
            // <j2seproject3:javac gensrcdir="${build.generated.sources.dir}"/>
            // will be modified as follows:
            // <javac gensrcdir="${build.generated.sources.dir}"/>
            //---------------------------------------------------------------------

            doc = XMLUtil.parse(new InputSource(is), false, true, null, null);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return;
        } catch (SAXException ex) {
            Exceptions.printStackTrace(ex);
             return;
        }

        String cmArtifactId = pomProps.getProperty("artifactId");
        if (cmArtifactId == null) {
            // we cannot extract pom.properties from command manager jar
            return;
        }

        updateMavenElValue(doc, "command.manager.artifactId", cmArtifactId);
        
        String cmGroupId = pomProps.getProperty("groupId");
        if (cmGroupId != null) {
            updateMavenElValue(doc, "command.manager.groupId", cmGroupId);
        }
        String cmVersion = pomProps.getProperty("version");
        if (cmVersion != null) {
            updateMavenElValue(doc, "command.manager.version", cmVersion);
        }

        
        try (OutputStream out = projDir.getFileObject("pom.xml").getOutputStream()) {
            XMLUtil.write(doc,out, "UTF-8");
        } catch (IOException | DOMException ex) {
            LOG.log(Level.INFO, ex.getMessage());
        }

    }
    protected void updateMavenElValue(Document doc, String elName, String elValue) {

        NodeList nl = doc.getDocumentElement().getElementsByTagName(elName);
        if (nl != null) {
            for (int i = 0; i < nl.getLength(); i++) {
                Element el = (Element) nl.item(i);
                if (el.getParentNode() != null && "properties".equals(el.getParentNode().getNodeName())) {
                    NodeList nl2 = el.getChildNodes();
                    if (nl2.getLength() > 0) {
                        nl2.item(0).setNodeValue(elValue);
                    }
                    break;
                }
            }
        }

    }

    @Override
    public void modifyClasspath(Set result) {

    }

    @Override
    public InputStream getZipTemplateInputStream() {
        return getClass().getClassLoader().getResourceAsStream("/"
                + zipMavenTemplatePath);
    }


}
