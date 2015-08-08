/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jeeserver.jetty.deploy.config;

/**
 *
 * @author Valery
 */
public interface DeploymentConfigPaths {
    static String[] getPaths() {
        return new String[] {"WEB-INF/jetty-web.xml","WEB-INF/web-jetty.xml"};
    }
}
