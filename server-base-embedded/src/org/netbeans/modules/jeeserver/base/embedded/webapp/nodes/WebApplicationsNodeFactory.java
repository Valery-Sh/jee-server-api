package org.netbeans.modules.jeeserver.base.embedded.webapp.nodes;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteConstants;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteUtil;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.loaders.DataObjectNotFoundException;

/**
 * Factory class for distributed creation of 
 * project node's children. The instance of the class
 * are assumed to be registered in layer at a location 
 * specific for {@literal j2se } project type.
 * 
 * @author V. Shyshkin
 */
//@NodeFactory.Registration(projectType = "org-netbeans-modules-java-j2seproject")
public class WebApplicationsNodeFactory implements NodeFactory {
    
    private static final Logger LOG = Logger.getLogger(WebApplicationsNodeFactory.class.getName());    
    /**
     * Creates a new instance of {@literal WebApplicationsNode} 
     * for the specified project and returns it
     * an a @{code NodeList) item.
     * The project must be recognized as an embedded server.
     * 
     * @return {@literal NodeFactorySupport.fixedNodeList() ) if the given project
  is not an embedded server. {@code NodeFactorySupport.fixedNodeList(none) )
 where the {@code node } is of type {@code WebApplicationsNode}
 }@see WebApplicationsNode
     */
    @Override
    public NodeList createNodes(Project project) {
            if ( ! SuiteUtil.isServerProject(project)) {
                return NodeFactorySupport.fixedNodeList();
            }
            if (project.getProjectDirectory().getFileObject(SuiteConstants.REG_WEB_APPS_FOLDER) != null) {
                try {
                    WebApplicationsNode node = new WebApplicationsNode(project);
                    node.init(project);
                    return NodeFactorySupport.fixedNodeList(node);
                } catch (DataObjectNotFoundException ex) {
                    LOG.log(Level.INFO, ex.getMessage());
                }
            }
            //If the above try/catch fails, e.g.,
            //our item isn't in the lookup,
            //then return an empty list of nodes:
            return NodeFactorySupport.fixedNodeList();

    }

    
}