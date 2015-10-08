package org.netbeans.modules.jeeserver.base.embedded.webapp.nodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteConstants;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 *
 * @author V. Shyshkin
 */
public class WebAppShortChildNode extends WebAppChildNode {
    private static final Logger LOG = Logger.getLogger(PropertiesChildNode.class.getName());
    
    /**
     * Creates a new instance of the class for the specified 
     * project, node key and child nodes keys.
     * The node created has child nodes.
     * 
     * @param serverProj
     * @param webAppKey actually the parameter value is a string value
     *   of the folder name.
     *   If a folder then it is an internal web project inside {@literal server-instance-config}.
     * 
     * @throws DataObjectNotFoundException  should never occur
     */

    public WebAppShortChildNode(Project serverProj, Object webAppKey) throws DataObjectNotFoundException {
        super(serverProj, webAppKey, new Keys(serverProj,webAppKey));
    }
    /**
     * Instances of the class represent child nodes of the inner
     * web project node.
     */
    public static class Keys extends Children.Keys {

        private final Project serverProj;
        private final Object webAppKey;
        
        public Keys(Project serverProj,Object webAppKey) {
            this.serverProj = serverProj;
            this.webAppKey = webAppKey;
        }
        /**
         * Create a single node which represents a {@literal web} folder
         * of the inner web project.
         * 
         * @param key
         * @return 
         */
        @Override
        protected Node[] createNodes(Object key) {
            Node[] nodeArray = new Node[1];
            try {
                Node node =  DataObject.find(serverProj.getProjectDirectory().
                    getFileObject(SuiteConstants.REG_WEB_APPS_FOLDER)
                        .getFileObject(webAppKey.toString())
                        .getFileObject("web"))
                        .getNodeDelegate();
                nodeArray = new Node[]{node};
            } catch (DataObjectNotFoundException ex) {
                LOG.log(Level.INFO, ex.getMessage());
            }
            return nodeArray;
        }

        @Override
        protected void addNotify() {
            List keyArray = new ArrayList<>(1);
            keyArray.add("web");
            this.setKeys(keyArray);
        }

        @Override
        protected void removeNotify() {
            this.setKeys(Collections.EMPTY_LIST);
        }
        public Project getServerProj() {
            return serverProj;
        }

    }//class
    
    
}
