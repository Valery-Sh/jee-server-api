/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jeeserver.base.embedded.server.project.nodes;

import java.beans.PropertyChangeEvent;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author V. Shyshkin
 */
public class SuiteNodeModel implements ChildrenKeysModel {

    private ChildrenKeysModel model;
    /**
     * Notifies {@link ServerInstancesRootNode} instance that 
     * child nodes keys changed. 
     */
    @Override
    public void modelChanged() {
        if (model != null) {
            model.modelChanged();
        }
    }


    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        BaseUtils.out("1 SuiteModel propertyChange new = " + evt.getNewValue());
        
        if (model == null) {
            return;
        }
        BaseUtils.out("2 SuiteModel propertyChange new = " + evt.getNewValue());
        
        model.propertyChange(evt);

    }
    
    private ChildrenKeysModel getServerInstancesModel() {
        if ( model == null ) {
            return null;
        }
        if ( model instanceof ServerInstancesRootNode ) {
            return model;
        } else if ( model instanceof InstanceNode ) {
            BaseUtils.out("NodeModel. InstanceNode.Parent=" + 
                    ((InstanceNode)model).getParentNode());
        }
        return null;
    }
    
    
    final void setModel(ChildrenKeysModel childrenKeysModel) {
        this.model = childrenKeysModel;
    }
    
/*    public Lookup getServerInstancesLookup() {
        ChildrenKeysModel km = getServerInstancesModel();
        if ( km == null ) {
            return null;
        }
        return ((ServerInstancesRootNode)km).getLookup();
    }
*/
    public Lookup getServerInstanceLookup(String uri) {
        ChildrenKeysModel km = getServerInstancesModel();
        if ( km == null ) {
            return null;
        }
        
        Node[] nodes = ((ServerInstancesRootNode)km).getChildKeys().getNodes();
        if ( nodes == null || nodes.length == 0 ) {
            return null;
        }
        Lookup result = null;
        for ( Node node : nodes ) {
            InstanceNode inode = null;
            if ( node instanceof InstanceNode) {
                inode = (InstanceNode) node;
            }
            if ( inode != null && uri.equals(inode.getKey()) ) {
                result = inode.getLookup();
                break;
            }
        }
        return result;
    }
    
    
}
