/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jeeserver.base.deployment.specifics;

/**
 *
 * @author Valery
 */
public interface LogicalViewNotifier {
    
    void iconChange(String uri,boolean newValue);
    void displayNameChange(String uri,String newValue);
    
}
