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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.netbeans.modules.jeeserver.jetty.project.nodes.actions.ShowInBrowserWebAppAction;
import org.netbeans.modules.jeeserver.jetty.project.nodes.actions.StartHotDeployedWebAppAction;
import org.netbeans.modules.jeeserver.jetty.project.nodes.actions.StopHotDeployedWebAppAction;
import org.netbeans.modules.jeeserver.jetty.project.nodes.actions.UndeployHotDeployedWebAppAction;
import org.netbeans.modules.jeeserver.jetty.util.JettyConstants;
import org.netbeans.modules.jeeserver.jetty.util.Utils;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 * The base class for all child nodes of the {@literal WebApplicationsNode}
 * instance which represents a folder {@literal server-instance-config).
 *
 * Every embedded server projects must contain a child folder named
 * {@code server-instance-config}. This folder is a root of the following  files:
 * <ul>
 * <li>A Web project folder. the folder of the web project that was created
 * inside {@code server-instance-config} directory.
 * </li>
 * <li>A file with extention {@code .webref}. It is a properties file which
 * has a property that references an existing web project in
 * the Project View of the IDE.
 * </li>
 * <li>A file with extention {@code .warref}. It is a properties file which
 * has a property that references an existing {@code war} archive file.
 * </li>
 * <li>
 * An Embedded server configuration file named 
      {@code server-instance.properties}.
 * </li>
 * </ul>
 *
 * }
 * @
 *
 * see WebApplicationsNode
 *
 * @author V. Shyshkin
 */
public class HotBaseWebAppChildNode extends FilterNode {

    private final Project serverProj;
    private final Object webAppKey;
    private Properties contextProperties;

    /**
     * Creates a new instance of the class for the specified project and node
     * key. This constructor should be used when the node to be created has no
     * child nodes.
     *
     * @param serverProj
     * @param webAppKey actually the parameter value is a string value of the
     * folder name or file name including extention. If a folder then it is an
     * internal web project inside {@literal server-instance-config}.
     *
     * @throws DataObjectNotFoundException should never occur
     */
    public HotBaseWebAppChildNode(Project serverProj, Object webAppKey) throws DataObjectNotFoundException {
        super(getNodeByKey(serverProj, webAppKey));
        this.serverProj = serverProj;
        this.webAppKey = webAppKey;
        contextProperties = null;
        
        initContextProperies();
        //this.setChildren(Children.LEAF);
        
    }
    

    @Override
    public boolean canRename() {
        return true;
    }
    @Override
    public boolean canDestroy() {
        return true;
    }    
    private void initContextProperies() {
        FileObject webappFo = serverProj.getProjectDirectory().getFileObject(getPath(webAppKey));
        if (webappFo == null) {
//            BaseUtils.out("BaseWebAppChildNode initContextProperies webappFo == NULL");
            return;
        }
//        BaseUtils.out("BaseWebAppChildNode initContextProperies webappFo.ext =" + webappFo.getExt());

        switch (webappFo.getExt()) {
            case "xml": {
                contextProperties = Utils.getContextProperties(webappFo);
                if (contextProperties.getProperty("contextPath") == null) {
                    contextProperties = null;
                }
                break;
            }
        }

    }

    /**
     * Creates a new instance of the class for the specified project and node
     * key. This constructor should be used when the node to be created has
     * child nodes.
     *
     * @param serverProj
     * @param webAppKey actually the parameter value is a string value of the
     * folder name or file name including extention. If a folder then it is an
     * internal web project inside {@literal server-instance-config}.
     * @param childrenKeys keys of child nodes
     *
     * @throws DataObjectNotFoundException should never occur
     */
    protected HotBaseWebAppChildNode(Project serverProj, Object webAppKey, Children.Keys childrenKeys) throws DataObjectNotFoundException {
        super(getNodeByKey(serverProj, webAppKey), childrenKeys);
        this.serverProj = serverProj;
        this.webAppKey = webAppKey;
        contextProperties = null;
        initContextProperies();

    }

    public Properties getContextProperties() {
        if (contextProperties == null) {
            initContextProperies();
        }
        return contextProperties;
    }

    /**
     * Returns an array of actions specific to this node. The list of supported
     * actions is as follows:
     * <ul>
     * <li>Run</li>
     * <li>Deploy</li>
     * <li>Debug</li>
     * <li>Profile</li>
     * <li>Clean</li>
     * <li>Build</li>
     * <li>Clean and Build</li>
     * <li>Open in Project View</li>
     * <li>New File</li>
     * <li>Copy</li>
     * <li>Move</li>
     * <li>Rename</li>
     * <li>Delete</li>
     * <li>Properties</li>
     * </ul>
     *
     * @param context
     * @return an array of actions
     */
    @Override
    public Action[] getActions(boolean context) {
        try {
            Node node = DataObject.find(getServerProject().getProjectDirectory().getFileObject(getPath(getWebAppKey()))).getNodeDelegate();
            List<Action> list2 = Arrays.asList(node.getActions(true));
            Action showbrowserAction = new ShowInBrowserWebAppAction().createContextAwareInstance(getWebAppProject().getLookup());
            Action startAction = new StartHotDeployedWebAppAction().createContextAwareInstance(getWebAppProject().getLookup());
            Action stopAction = new StopHotDeployedWebAppAction().createContextAwareInstance(getWebAppProject().getLookup());
            Action undeployAction = new UndeployHotDeployedWebAppAction().createContextAwareInstance(getWebAppProject().getLookup());

            List<Action> list1 = Arrays.asList(
                    new Action[]{
                        showbrowserAction,
                        null,
                        startAction,
                        stopAction,
                        null,
                        undeployAction,
                        null    
                    //CommonProjectActions.customizeProjectAction(), // Properties
                    });
            
            list1 = new ArrayList(list1);
            FileObject webappFo = serverProj.getProjectDirectory().getFileObject(getPath(webAppKey));
            
            if (webappFo == null || ! webappFo.isFolder()) {
                list1.addAll(new ArrayList(list2));
            }
            
            return list1.toArray(new Action[list1.size()]);
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
            BaseUtils.out("GET ACTION EXCEPTION " + ex.getMessage());
        }
        
        return null;
    }

    public static Node getNodeByKey(Project serverProj, Object webAppKey) throws DataObjectNotFoundException {
        Node n = DataObject.find(serverProj.getProjectDirectory().getFileObject(getPath(webAppKey))).getNodeDelegate();
        return DataObject.find(serverProj.getProjectDirectory().getFileObject(getPath(webAppKey))).getNodeDelegate();
    }

    static String getPath(Object webAppKey) {
        return JettyConstants.WEBAPPS_FOLDER + "/" + webAppKey;
    }

    /**
     * Returns ab object that represents a key of the node. Actually it is a
     * String whose value represents a folder or a file name.
     *
     * @return a folder or file name as a key of the node
     */
    public Object getWebAppKey() {
        return webAppKey;
    }

    /**
     * @return an object of type {@literal Project} for which nodes are being
     * built.
     */
    public Project getServerProject() {
        return serverProj;
    }

    /**
     * Returns a web project which this node represents.
     *
     * @return a web project which this node represents.
     */
    public FileObject getWebAppProject() {
        FileObject webappFo = serverProj.getProjectDirectory().getFileObject(getPath(getWebAppKey()));
        return webappFo;
    }
}
