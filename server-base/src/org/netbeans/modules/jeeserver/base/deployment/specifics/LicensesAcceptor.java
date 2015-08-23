/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jeeserver.base.deployment.specifics;

import javax.enterprise.deploy.spi.DeploymentManager;

/**
 *
 * @author V. Shyshkin
 */
public interface LicensesAcceptor {
    public default boolean licensesAccepted(DeploymentManager manager) {
        return true;
    }
}
