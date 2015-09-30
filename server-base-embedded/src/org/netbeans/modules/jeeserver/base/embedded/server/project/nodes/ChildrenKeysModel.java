package org.netbeans.modules.jeeserver.base.embedded.server.project.nodes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * 
 * @author V. Shyshkin
 */
public interface ChildrenKeysModel extends PropertyChangeListener{
    /**
     * The implementation of the method should call an {@literal addNotify() } 
     * of the {@literal FilterNode.Children.Keys } instance.
     */
    void modelChanged();
    @Override
    void propertyChange(PropertyChangeEvent evt);
    
}
