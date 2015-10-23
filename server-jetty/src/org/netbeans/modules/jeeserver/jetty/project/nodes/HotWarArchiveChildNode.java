/**
 * This file is part of Jetty Server support in NetBeans IDE.
 *
 * Jetty Server support in NetBeans IDE is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * Jetty Server support in NetBeans IDE is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should see the GNU General Public License here:
 * <http://www.gnu.org/licenses/>.
 */
package org.netbeans.modules.jeeserver.jetty.project.nodes;

import java.awt.Image;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.ImageUtilities;

/**
 *
 * @author V. Shyshkin
 */
public class HotWarArchiveChildNode extends HotBaseWebAppChildNode {

    @StaticResource
    private static final String IMAGE = "org/netbeans/modules/jeeserver/jetty/resources/war.png";

    /**
     * Creates a new instance of the class for the specified 
     * project and  node key an child nodes keys.
     * The node created has child nodes.
     * 
     * @param serverProj
     * @param key
     * @param childrenKeys keys of child nodes
     * 
     * @throws DataObjectNotFoundException  should never occur
     */
    protected HotWarArchiveChildNode(Project serverProj, Object key, Children.Keys childrenKeys) throws DataObjectNotFoundException {
        super(serverProj, key, childrenKeys);
        
    }
    
    protected HotWarArchiveChildNode(Project serverProj, Object key) throws DataObjectNotFoundException {
        super(serverProj, key);
    }
    
    /**
     * 
     * @param type
     * @return 
     */
    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage(IMAGE);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return ImageUtilities.loadImage(IMAGE);
    }
    @Override
    public String getDisplayName() {
        return super.getDisplayName();
        
    }
}

