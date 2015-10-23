package org.netbeans.modules.jeeserver.jetty.project.nodes;

import java.awt.Image;
import org.netbeans.api.annotations.common.StaticResource;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;

/**
 *
 * @author V. Shyshkin
 */
public class WebFolderNode extends FilterNode {
    
    @StaticResource
    private static final String FOLDER_BAG_IMAGE = "org/netbeans/modules/jeeserver/jetty/resources/web-pages-badge.png";
    

    private final Node original;
    
    public WebFolderNode(Node original) {
        super(original);
        this.original = original;
    }
    public WebFolderNode(Node original, Children c) {
        super(original,c);
        this.original = original;
    }
    
    @Override
    public String getDisplayName() {
        return "Web Pages";
    }
    @Override
    public Image getIcon(int type) {
        return ImageUtilities.mergeImages(original.getIcon(type),
                ImageUtilities.loadImage(FOLDER_BAG_IMAGE), 7, 7);
    }
    
}
