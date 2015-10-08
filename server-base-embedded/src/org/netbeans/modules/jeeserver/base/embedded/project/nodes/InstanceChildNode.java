package org.netbeans.modules.jeeserver.base.embedded.project.nodes;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.nodes.Node;

/**
 *
 * @author V. Shyshkin
 */
public class InstanceChildNode {

    private static final Logger LOG = Logger.getLogger(InstanceChildNode.class.getName());

    public static class InstanceProjectLogicalView {

        public static Node create(Project instanceProject) {
            Node node = null;
            try {
                LogicalViewProvider lvp = instanceProject.getLookup().lookup(LogicalViewProvider.class);
                node = lvp.createLogicalView();
                
            } catch (Exception ex) {
                LOG.log(Level.INFO, ex.getMessage());
            }
            return node;

        }
    }//class InstanceProjectLogicalView

}//class
