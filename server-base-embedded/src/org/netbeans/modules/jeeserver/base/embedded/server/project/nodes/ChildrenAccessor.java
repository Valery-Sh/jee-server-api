/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jeeserver.base.embedded.server.project.nodes;

import org.openide.nodes.FilterNode;

/**
 *
 * @author Valery
 */
public interface ChildrenAccessor {
    FilterNode.Children.Keys getChildKeys();
    void addNotify();
}
