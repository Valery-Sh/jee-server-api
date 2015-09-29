/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jeeserver.base.embedded.server.project.nodes;

import java.beans.PropertyChangeEvent;
import java.util.List;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;

/**
 *
 * @author Valery
 */
public class NodeModel implements ChildrenKeysModel {

    //private Lookup serverInstancesLookup;
    private ChildrenKeysModel childrenKeysModel;

    protected NodeModel(ChildrenKeysModel childrenKeysModel) {
        this.childrenKeysModel = childrenKeysModel;
    }
    public NodeModel() {
        this(null);
    }
    void init(ChildrenKeysModel childrenKeysModel) {
        this.childrenKeysModel = childrenKeysModel;
    }
    
    @Override
    public void modelChanged() {
        if (childrenKeysModel != null) {
            childrenKeysModel.modelChanged();
        }
    }

    /*    @Override
     public List<InstanceNode> getInstanceNodes() {
     if (rootChildrenModel == null) {
     return null;
     }
     return rootChildrenModel.getInstanceNodes();
     }
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        BaseUtils.out("ServerInstanceRootNode: propertyChange new = " + evt.getNewValue());
        if (childrenKeysModel == null) {
            return;
        }
        childrenKeysModel.propertyChange(evt);

    }

}//class
