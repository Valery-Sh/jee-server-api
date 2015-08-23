package org.netbeans.modules.jeeserver.jetty.project.nodes;

import java.awt.Image;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;

/**
 *
 * @author V. Shyshkin
 */
public class WebModuleRegisteredNode extends AbstractNode {
    
    @StaticResource
    private static final String FOLDER_BAG_IMAGE = "org/netbeans/modules/jeeserver/jetty/resources/web-pages-badge.png";
    

    private final Children children;
    private final Project webProject;
    
    public WebModuleRegisteredNode(Project webProject,Children c) {
        super(c);
        this.children = c;
        this.webProject = webProject;
    }
    
    @Override
    public String getDisplayName() {
        return webProject.getProjectDirectory().getNameExt();
    }
    
}
