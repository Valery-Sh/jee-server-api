/**
 * This file is part of Jetty Server support in NetBeans IDE.
 *
 * Jetty Server support in NetBeans IDE is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the License,
 * or (at your option) any later version.
 *
 * Jetty Server support in NetBeans IDE is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should see the GNU General Public License here:
 * <http://www.gnu.org/licenses/>.
 */
package org.netbeans.modules.jeeserver.jetty.project.nodes;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.deployment.config.ModulesChangeEvent;
import static org.netbeans.modules.jeeserver.base.deployment.config.ModulesChangeEvent.DELETED;
import static org.netbeans.modules.jeeserver.base.deployment.config.ModulesChangeEvent.DISPOSE;
import org.netbeans.modules.jeeserver.base.deployment.config.ModulesChangeListener;
import org.netbeans.modules.jeeserver.base.deployment.config.ServerInstanceAvailableModules;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import static org.netbeans.modules.jeeserver.jetty.project.nodes.Bundle.WebModulesRootNode_availableWebApps;
import static org.netbeans.modules.jeeserver.jetty.project.nodes.Bundle.WebModulesRootNode_shortDescription;
import org.netbeans.modules.jeeserver.jetty.project.nodes.WebAppNode.RootChildrenKeys;
import org.netbeans.modules.jeeserver.jetty.util.JettyConstants;
import org.openide.actions.PropertiesAction;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 * Represents the root node of the logical view of the serverProject's folder
 * named {@literal webapps}. Every jetty server serverProject may contain a
 * child folder named {@literal webapps}. Its logical name is
 * {@literal Web Applications}.
 *
 * @author V. Shyshkin
 */
@Messages({
    "WebModulesRootNode.shortDescription=Registered applications for this server",
    "WebModulesRootNode.availableWebApps=Available Web Applications"    
})
public class WebModulesRootNode extends FilterNode {

    private ModulesChangeListener modulesChangeListener;

    @StaticResource
    private static final String IMAGE = "org/netbeans/modules/jeeserver/jetty/resources/web-pages-badge.png";

    /**
     * Creates a new instance of the class for a specified serverProject.
     *
     * @param serverProj a serverProject which is used to create an instance of
     * the class.
     * @throws DataObjectNotFoundException
     */
    public WebModulesRootNode(Project serverProj) throws DataObjectNotFoundException {
        super(DataObject.find(serverProj.getProjectDirectory().
                getFileObject(JettyConstants.WEBAPPS_FOLDER)).getNodeDelegate(),
                new RootChildrenKeys(serverProj));

    }

    public WebModulesRootNode(Project serverProj, Children children) throws DataObjectNotFoundException {
        super(DataObject.find(serverProj.getProjectDirectory().
                getFileObject(JettyConstants.WEBAPPS_FOLDER))
                .getNodeDelegate(), children);

    }

    /**
     * Creates an instance of class {@link FileChangeHandler} and adds it as a
     * listener of the {@literal FileEvent } to the {@literal FileObject}
     * associated with a {@literal server-instance-config} folder.
     *
     * @param serverProj
     */
    protected final void init(Project serverProj) {
        modulesChangeListener = new ModulesChangeHandler(serverProj, this);
        serverProj.getLookup().lookup(ServerInstanceAvailableModules.class)
                .addModulesChangeListener(modulesChangeListener);

        setShortDescription(WebModulesRootNode_shortDescription());
    }

    /**
     * Returns the logical name of the node.
     *
     * @return the value "Web Application"
     */
    @Override
    public String getDisplayName() {
        return WebModulesRootNode_availableWebApps();
    }

    /**
     * Returns an array of actions associated with the node.
     *
     * @param ctx
     * @return an array of the following actions:
     * <ul>
     * <li>
     * Add Existing Web Application
     * </li>
     * <li>
     * Add Archive .war File
     * </li>
     * <li>
     * New Web Application
     * </li>
     * <li>
     * Properties
     * </li>
     * </ul>
     */
    @Override
    public Action[] getActions(boolean ctx) {
        List<Action> actions = new ArrayList<>(2);

        Action propAction = null;
        
        for (Action a : super.getActions(ctx)) {
            if (a instanceof PropertiesAction) {
                propAction = a;
                break;
            }
        }

        Project server = ((RootChildrenKeys) getChildren()).getServerProj();
        if (propAction != null) {
            actions.add(propAction);
        }

        return actions.toArray(new Action[actions.size()]);

    }

    //Next, we add icons, for the default state, which is
    //closed, and the opened state; we will make them the same. 
    //
    //Icons in serverProj logical views are
    //based on combinations--you can combine the node's own icon
    //with a distinguishing badge that is merged with it. Here we
    //first obtain the icon from a data folder, then we add our
    //badge to it by merging it via a NetBeans API utility method:
    @Override
    public Image getIcon(int type) {
        DataFolder root = DataFolder.findFolder(FileUtil.getConfigRoot());
        Image original = root.getNodeDelegate().getIcon(type);
        return ImageUtilities.mergeImages(original,
                ImageUtilities.loadImage(IMAGE), 7, 7);
    }

    @Override
    public Image getOpenedIcon(int type) {
        DataFolder root = DataFolder.findFolder(FileUtil.getConfigRoot());
        Image original = root.getNodeDelegate().getIcon(type);
        return ImageUtilities.mergeImages(original,
                ImageUtilities.loadImage(IMAGE), 7, 7);
    }

    public static class ModulesChangeHandler implements ModulesChangeListener {

        private final Project server;
        private final WebModulesRootNode node;
        private ModulesChangeEvent event;

        public ModulesChangeHandler(Project server, WebModulesRootNode node) {
            this.server = server;
            this.node = node;
        }

        @Override
        public void availableModulesChanged(ModulesChangeEvent event) {
            this.event = event;
            if (event.getEventType() == DISPOSE || event.getEventType() == DELETED) {
                ((RootChildrenKeys) node.getChildren()).removeNotify();
            }
            ((RootChildrenKeys) node.getChildren()).addNotify();

        }

    }

}//class
