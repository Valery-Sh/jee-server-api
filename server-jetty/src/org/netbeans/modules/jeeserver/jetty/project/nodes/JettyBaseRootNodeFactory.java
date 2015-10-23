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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.jetty.util.Utils;
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
@NodeFactory.Registration(projectType = "org-jetty-server-instance-project")

public class JettyBaseRootNodeFactory implements NodeFactory {

    private static final Logger LOG = Logger.getLogger(JettyBaseRootNodeFactory.class.getName());

    /**
     * Creates a new instance of {@literal WebApplicationsNode} for the
     * specified project and returns it an a @{code NodeList) item. The project
     * must be recognized as an embedded server.
     *
     * @param project
     * @return {@literal NodeFactorySupport.fixedNodeList() ) if the given project
     * is not an embedded server. {@code NodeFactorySupport.fixedNodeList(none) )
     * where the {@code node } is of type {@code WebApplicationsNode}
     * }@see WebApplicationsNode
     */
    @Override
    public NodeList createNodes(Project project) {

        if (!Utils.isJettyServer(project)) {
            return NodeFactorySupport.fixedNodeList();
        }
        //if (project.getProjectDirectory().getFileObject(JettyConstants.WEBAPPLICATIONS_FOLDER) != null) {
        try {
            JettyBaseRootNode node = new JettyBaseRootNode(project);
            node.init(project);
            return NodeFactorySupport.fixedNodeList(node);
        } catch (DataObjectNotFoundException ex) {
            LOG.log(Level.INFO, ex.getMessage());
        }
        return NodeFactorySupport.fixedNodeList();

    }

}
