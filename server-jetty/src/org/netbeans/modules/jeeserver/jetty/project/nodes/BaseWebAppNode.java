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

import java.io.File;
import javax.swing.Action;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.deployment.actions.WebAppCommandActions;
import org.netbeans.modules.jeeserver.base.deployment.actions.WebAppNodeUtils;
import org.netbeans.modules.jeeserver.base.deployment.config.WebModuleConfig;
import org.netbeans.modules.jeeserver.jetty.project.nodes.actions.DDAddBeansXmlAction;
import org.netbeans.modules.jeeserver.jetty.project.nodes.actions.DDAddListenerAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Utilities;

/**
 * The base class for all child nodes of the {@literal WebApplicationsNode} 
 * instance which represents a folder {@literal server-instance-config).
 
 Every embedded server projects must contain a child folder named
 {@code server-instance-config}. This folder is a root of the following  files:
 <ul>
   <li>A Web project folder. the folder of the web project that was
       created inside {@code server-instance-config} directory. 
   </li>
   <li>A file with extention {@code .webref}. It is a properties file which
       has a property that references an existing web project in 
       the Project View of the IDE.
   </li>
   <li>A file with extention {@code .warref}. It is a properties file which
       has a property that references an existing {@code war} archive file.
   </li>
   <li>
      An Embedded server configuration file named 
      {@code server-instance.properties}. 
   </li>
 </ul>
 
 }@see WebApplicationsNode
 * @author V. Shyshkin
 */
public class BaseWebAppNode extends FilterNode {

    private final Project serverProj;
    private final Object webAppKey;
    /**
     * Creates a new instance of the class for the specified 
     * project and  node key.
     * This constructor should be used when the node to be created 
     * has no child nodes.
     * 
     * @param serverProj
     * @param webAppKey actually the parameter value is a string value
     *   of the folder name or file name including extention.
     *   If a folder then it is an internal web project inside {@literal server-instance-config}.
     * 
     * @throws DataObjectNotFoundException  should never occur
     */
    public BaseWebAppNode(Project serverProj, Object webAppKey) throws DataObjectNotFoundException {
        super(getNodeByKey(serverProj, webAppKey));
        this.serverProj = serverProj;
        this.webAppKey = webAppKey;
    }

    /**
     * Creates a new instance of the class for the specified 
     * project and  node key.
     * This constructor should be used when the node to be created 
     * has child nodes.
     * 
     * @param serverProj
     * @param webAppKey actually the parameter value is a string value
     *   of the folder name or file name including extention.
     *   If a folder then it is an internal web project inside {@literal server-instance-config}.
     * @param childrenKeys keys of child nodes
     * 
     * @throws DataObjectNotFoundException  should never occur
     */
    protected BaseWebAppNode(Project serverProj, Object webAppKey, FilterNode.Children.Keys childrenKeys) throws DataObjectNotFoundException {
        super(getNodeByKey(serverProj, webAppKey), childrenKeys);
        this.serverProj = serverProj;
        this.webAppKey = webAppKey;
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
        Object o = getWebAppKey();
        Project webProj = null;
        if ( o != null && (o instanceof WebModuleConfig) ) {
            WebModuleConfig key = (WebModuleConfig) o;
            webProj = FileOwnerQuery.getOwner(Utilities.toURI(new File(key.getWebProjectPath())));
        }
        if ( webProj == null ) {
            return new Action[]{};
        } else {
            
            Action[] actions = WebAppNodeUtils.getActions(webProj);
            return addActions(webProj, actions);
        }
        
//        return new Action[]{};
/*        Action startAction = new StartWebAppAction().createContextAwareInstance(getWebAppProject().getLookup());
        Action stopAction  = new StopWebAppAction().createContextAwareInstance(getWebAppProject().getLookup());        
        Action undeployAction  = new UndeployWebAppAction().createContextAwareInstance(getWebAppProject().getLookup());                
        return new Action[]{
            startAction,
            stopAction,
            null,
            undeployAction
            //CommonProjectActions.customizeProjectAction(), // Properties
        };
*/        
    }
    
    protected Action[] addActions(Project webProj,Action[] actions) {
        Action[] result = new Action[actions.length + 3];
        int j = 0;
        for ( int i=0; i < actions.length; i++ ) {
            result[j] = actions[i];
            if ( actions[i] != null && (actions[i] instanceof WebAppCommandActions.CleanAndBuildAction) ) {
                result[++j] = null;
                result[++j] = DDAddListenerAction.getDDAddListenerAction(serverProj,webProj);
                result[++j] = DDAddBeansXmlAction.getDDAddBeansXmlAction(serverProj,webProj);
            }
            j++;
        }
        return result;
    }
    public static Node getNodeByKey(Project serverProj, Object webAppKey) throws DataObjectNotFoundException {
        WebModuleConfig wmKey =(WebModuleConfig) webAppKey;
        FileObject p = FileUtil.toFileObject(new File(wmKey.getWebProjectPath()));
        Node n = DataObject.find(p).getNodeDelegate();
        return n;
    }

    /**
     * Returns ab object that represents a key of the node.
     * Actually it is a String whose value represents a folder or a file name. 
     * @return a folder or file name as a key of the node
     */
    public Object getWebAppKey() {
        return webAppKey;
    }
    /**
     * @return an object of type {@literal Project} for which nodes are being built.
     */
    public Project getServerProject() {
        return serverProj;
    }
}
