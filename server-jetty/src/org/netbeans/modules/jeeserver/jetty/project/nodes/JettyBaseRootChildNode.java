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

import org.netbeans.api.project.Project;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;

/**
 *
 * @author V. Shyshkin
 */
public class JettyBaseRootChildNode extends FilterNode {

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
    public JettyBaseRootChildNode(Project serverProj, Object webAppKey) throws DataObjectNotFoundException {
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
    protected JettyBaseRootChildNode(Project serverProj, Object webAppKey, FilterNode.Children.Keys childrenKeys) throws DataObjectNotFoundException {
        super(getNodeByKey(serverProj, webAppKey), childrenKeys);
        this.serverProj = serverProj;
        this.webAppKey = webAppKey;
    }
    
    public static Node getNodeByKey(Project serverProj, Object webAppKey) throws DataObjectNotFoundException {
        Node n = DataObject.find(serverProj.getProjectDirectory().getFileObject(getPath(webAppKey))).getNodeDelegate();
        return DataObject.find(serverProj.getProjectDirectory().getFileObject(getPath(webAppKey))).getNodeDelegate();
    }

    static String getPath(Object webAppKey) {
        return "jettybase/" + webAppKey;
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
