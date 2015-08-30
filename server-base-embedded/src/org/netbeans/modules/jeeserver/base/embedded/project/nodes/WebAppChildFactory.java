package org.netbeans.modules.jeeserver.base.embedded.project.nodes;

import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.embedded.utils.EmbConstants;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;

/**
 *
 * @author V. Shyshkin
 */
public class WebAppChildFactory {

    public static Node getNode(Project project, Object key) {
        String name = key.toString();
        Node node = null;

        FileObject fo = project.getProjectDirectory().getFileObject(EmbConstants.REG_WEB_APPS_FOLDER).getFileObject(name);
        try {
            if (fo.isFolder() ) {
                node = new WebAppShortChildNode(project, key);
            } else if ("embedded-instance.properties".equals(fo.getNameExt())) {
                node = new EmbConfigChildNode(project, key);
            } else if ("properties".equals(fo.getExt())) {
                node = new PropertiesChildNode(project, key);
            } else if (EmbConstants.WEB_REF.equals(fo.getExt())) {
                node = new PropertiesChildNode.WebRefChildNode(project, key);
            } else if (EmbConstants.HTML_REF.equals(fo.getExt())) {
                node = new PropertiesChildNode.HtmRefChildNode(project, key);                
            } else if (EmbConstants.WAR_REF.equals(fo.getExt())) {
                node = new PropertiesChildNode.WarRefChildNode(project, key);
            }  else if (EmbConstants.JEE_REF.equals(fo.getExt())) {
                node = new PropertiesChildNode.JeeRefChildNode(project, key);
            } else if (EmbConstants.EAR_REF.equals(fo.getExt())) {
                node = new PropertiesChildNode.EarRefChildNode(project, key);
            } else {

            }
        } catch (Exception e) {
        }
        return node;
    }
}
