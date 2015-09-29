package org.netbeans.modules.jeeserver.base.embedded.server.project.nodes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 *
 * @author V. Shyshkin
 */
public interface ChildrenKeysModel extends PropertyChangeListener{

    void modelChanged();
    @Override
    void propertyChange(PropertyChangeEvent evt);
    //List<InstanceNode> getInstanceNodes();
    
}
