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
package org.netbeans.modules.jeeserver.base.embedded.server.project.nodes;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.embedded.server.project.ServerSuiteProject;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.loaders.DataObjectNotFoundException;

/**
 * Factory class for distributed creation of project node's children. The
 * instance of the class are assumed to be registered in layer at a location
 * specific for {@literal j2se } project type.
 *
 * @author V. Shyshkin
 */
@NodeFactory.Registration(projectType = ServerSuiteProject.TYPE, position = 0)
public class ServerInstancesNodeFactory implements NodeFactory {

    private static final Logger LOG = Logger.getLogger(ServerInstancesNodeFactory.class.getName());

    /**
     * Creates a new instance of {@literal WebApplicationsNode} for the
 specified suiteProject and returns it an a @{code NodeList) item. The suiteProject
 must be recognized as an embedded server.
     *
     * @param suiteProject
     * @return {@literal NodeFactorySupport.fixedNodeList() ) if the given suiteProject
 is not an embedded server. {@code NodeFactorySupport.fixedNodeList(none) )
 where the {@code node } is of type {@code WebApplicationsNode}
 }@see WebApplicationsNode
     */
    @Override
    public NodeList createNodes(Project suiteProject) {
       // PackageView p;
/*        if (!EmbUtils.isEmbeddedServer(suiteProject)) {
            return NodeFactorySupport.fixedNodeList();
        }
*/        
        try {
            ServerInstancesRootNode node = new ServerInstancesRootNode(suiteProject);
            //node.init(suiteProject);
            return NodeFactorySupport.fixedNodeList(node);
        } catch (DataObjectNotFoundException ex) {
            LOG.log(Level.INFO, ex.getMessage());
        }
//        }
        //If the above try/catch fails, e.g.,
        //our item isn't in the lookup,
        //then return an empty list of nodes:
        return NodeFactorySupport.fixedNodeList();

    }
    /*
     public static Node getNode(Project server, Object key) {
     Node node = null;
     WebModuleRegisteredNode wfn = null;        
     try {
     WebModuleConfig c = (WebModuleConfig) key;

     Project webProject = FileOwnerQuery.getOwner(FileUtil.toFileObject(new File(c.getWebProjectPath())));
     LogicalViewProvider lvp = webProject.getLookup().lookup(LogicalViewProvider.class);
     node = lvp.createLogicalView();
     } catch (Exception ex) {
     LOG.log(Level.INFO, ex.getMessage());
     }
     return node;
     }
     */
}//class
