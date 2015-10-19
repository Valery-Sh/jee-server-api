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
package org.netbeans.modules.jeeserver.base.embedded.project.nodes;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.embedded.project.ServerSuiteProject;
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
//@NodeFactory.Registration(projectType = ServerSuiteProject.TYPE, position = 0)
public class DistributedWebAppRootFactory implements NodeFactory {

    private static final Logger LOG = Logger.getLogger(DistributedWebAppRootFactory.class.getName());

    
    @Override
    public NodeList createNodes(Project suiteProject) {
        try {
            
            //DistributedWebAppRootNode node = new DistributedWebAppRootNode(suiteProject);
            //node.init(suiteProject);
            //return NodeFactorySupport.fixedNodeList(node);
            return null;
        } catch (Exception ex) {
            LOG.log(Level.INFO, ex.getMessage());
        }
        //If the above try/catch fails, e.g.,
        //our item isn't in the lookup,
        //then return an empty list of nodes:
        return NodeFactorySupport.fixedNodeList();

    }
}//class
