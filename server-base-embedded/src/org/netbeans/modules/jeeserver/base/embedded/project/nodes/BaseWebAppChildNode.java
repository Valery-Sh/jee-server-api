package org.netbeans.modules.jeeserver.base.embedded.project.nodes;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.embedded.utils.EmbConstants;
import org.netbeans.modules.jeeserver.base.embedded.project.WebApplicationsNode;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;

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
public class BaseWebAppChildNode extends FilterNode {

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
    public BaseWebAppChildNode(Project serverProj, Object webAppKey) throws DataObjectNotFoundException {
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
    protected BaseWebAppChildNode(Project serverProj, Object webAppKey, Children.Keys childrenKeys) throws DataObjectNotFoundException {
        super(getNodeByKey(serverProj, webAppKey), childrenKeys);
        this.serverProj = serverProj;
        this.webAppKey = webAppKey;
    }

    static Node getNodeByKey(Project serverProj, Object webAppKey) throws DataObjectNotFoundException {
        return DataObject.find(serverProj.getProjectDirectory().getFileObject(getPath(webAppKey))).getNodeDelegate();
    }

    static String getPath(Object webAppKey) {
        return EmbConstants.REG_WEB_APPS_FOLDER + "/" + webAppKey;
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
    /**
     * Returns a web project which this node represents. 
     * @return a web project which this node represents. 
     */
    public Project getWebAppProject() {
        FileObject webappFo = serverProj.getProjectDirectory().getFileObject(getPath(getWebAppKey()));
        return FileOwnerQuery.getOwner(webappFo);
    }

/*    public class ProfileActionPerformer implements ProjectActionPerformer {

        @Override
        public boolean enable(Project project) {
            return true;
        }

        @Override
        public void perform(Project project) {
        }
    }
*/ 
}
