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
import java.util.Properties;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.ImageUtilities;

/**
 *
 * @author V. Shyshkin
 */
public class HotXmlChildNode extends HotBaseWebAppChildNode {

    @StaticResource
    private static final String IMAGE = "org/netbeans/modules/jeeserver/jetty/resources/web-xml-16x16.jpg";

    @StaticResource
    private static final String HTML5_IMAGE = "org/netbeans/modules/jeeserver/jetty/resources/HTML5_icon.png";
    
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
    protected HotXmlChildNode(Project serverProj, Object key, Children.Keys childrenKeys) throws DataObjectNotFoundException {
        super(serverProj, key, childrenKeys);
        
    }
    
    protected HotXmlChildNode(Project serverProj, Object key) throws DataObjectNotFoundException {
        super(serverProj, key);
    }
    
    /**
     * 
     * @param type
     * @return 
     */
    @Override
    public Image getIcon(int type) {
        Properties p = getContextProperties();
        if ( p != null && p.getProperty("war") != null) {
            if ( BaseUtil.isHtml5Project(p.getProperty("war")) ) {
                return ImageUtilities.loadImage(HTML5_IMAGE);          
            }
        }
        return ImageUtilities.loadImage(IMAGE);
    }

    @Override
    public Image getOpenedIcon(int type) {
        String name = this.getWebAppKey().toString();
        if ( name.endsWith(".html5.xml")) {
            return ImageUtilities.loadImage(HTML5_IMAGE);
        } 
        return ImageUtilities.loadImage(IMAGE);
    }
    @Override
    public String getDisplayName() {
        return super.getDisplayName();
    }
}