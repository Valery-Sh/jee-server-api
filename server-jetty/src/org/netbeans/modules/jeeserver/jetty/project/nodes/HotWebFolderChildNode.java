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
public class HotWebFolderChildNode extends HotBaseWebAppChildNode {

    @StaticResource
    private static final String IMAGE = "org/netbeans/modules/jeeserver/jetty/resources/web-folder-closed.gif";

    @StaticResource
    private static final String IMAGE_BADGE = "org/netbeans/modules/jeeserver/jetty/resources/web-pages-badge.png";

    /**
     * Creates a new instance of the class for the specified project and node
     * key an child nodes keys. The node created has child nodes.
     *
     * @param serverProj
     * @param key
     * @param childrenKeys keys of child nodes
     *
     * @throws DataObjectNotFoundException should never occur
     */
    protected HotWebFolderChildNode(Project serverProj, Object key, Children.Keys childrenKeys) throws DataObjectNotFoundException {
        super(serverProj, key, childrenKeys);
    }

    protected HotWebFolderChildNode(Project serverProj, Object key) throws DataObjectNotFoundException {
        super(serverProj, key);
    }

    /*    public static final String NEW_FILE = "&New File...";
     public static final String BUILD = "Build Project";
     public static final String CLEAN_AND_BUILD = "Clean and Build Project";
     public static final String CLEAN = "Clean Project";
     public static final String RUN = "Run Project";
     public static final String DEPLOY = "Deploy";
     public static final String DEBUG = "Debug";
     public static final String PROFILE = "Profile";
     public static final String TEST_RESTFULL_WEB_SERVICE = "Test RESTful Web Services";
     public static final String TEST_PROJECT = "&Test Project";
     public static final String RENAME = "Rename...";
     public static final String MOVE = "Move...";
     public static final String COPY = "Copy...";
     public static final String DELETE = "Delete";
     public static final String FIND = "&Find...";
     public static final String PROPERTIES = "Project Proper&ties";
     */
    /**
     *
     * @param type
     * @return
     */
    @Override
    public Image getIcon(int type) {
        //DataFolder root = DataFolder.findFolder(FileUtil.getConfigRoot());
        //Image original = root.getNodeDelegate().getIcon(type);
        Image original = ImageUtilities.loadImage(IMAGE);
        return ImageUtilities.mergeImages(original,
                ImageUtilities.loadImage(IMAGE_BADGE), 7, 7);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public String getDisplayName() {
        return super.getDisplayName();
    }
}
