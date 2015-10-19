package org.netbeans.modules.jeeserver.base.embedded.project.nodes;

import org.netbeans.modules.jeeserver.base.deployment.specifics.LogicalViewNotifier;

/**
 * 
 * @author V. Shyshkin
 */
public interface ChildrenNotifier extends LogicalViewNotifier{
    /**
     * The implementation of the method should call an {@literal addNotify() } 
     * of the {@literal FilterNode.Children.Keys } instance.
     */
    void childrenChanged();

    void childrenChanged(Object source, Object... params);
    
    @Override
    void iconChange(String uri,boolean newValue);
    @Override
    void displayNameChange(String uri,String newValue);
    
}
