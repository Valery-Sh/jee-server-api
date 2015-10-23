/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jeeserver.base.deployment;

import org.netbeans.modules.jeeserver.base.deployment.specifics.LogicalViewNotifier;

/**
 *
 * @author Valery
 */
public class DeploymentManagerLogicalViewNotifier implements LogicalViewNotifier{
    
    BaseDeploymentManager dm;
    
    public DeploymentManagerLogicalViewNotifier(BaseDeploymentManager dm) {
        this.dm = dm;
    }
    
    @Override
    public void iconChange(String uri,boolean newValue) {
        dm.getSpecifics().iconChange(uri, newValue);
    }
    @Override
    public void displayNameChange(String uri,String newValue) {
        dm.getSpecifics().displayNameChange(uri, newValue);
    }
    
}
