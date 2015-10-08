package org.netbeans.modules.jeeserver.base.embedded.webapp.nodes;

import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.openide.loaders.DataObjectNotFoundException;

/**
 * Instances of the class correspond to the server configuration file.
 * @author V. Shyshkin
 */
public class EmbConfigChildNode extends BaseWebAppChildNode {

    public EmbConfigChildNode(Project project, Object key) throws DataObjectNotFoundException {
        super(project, key);
    }
    /**
     * Returns an empty array of actions.
     * NetBeans IDE will provide standard set of actions for this type of file.
     * 
     * @param ctx
     * @return an empty array
     */
    @Override
    public Action[] getActions(boolean ctx) {
        return new Action[]{};
    }
}
