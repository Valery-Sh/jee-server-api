/**
 * This file is part of Jetty Server support in NetBeans IDE.
 *
 * Jetty Server support in NetBeans IDE is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * Jetty Server support in NetBeans IDE is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should see the GNU General Public License here:
 * <http://www.gnu.org/licenses/>.
 */
package org.netbeans.modules.jeeserver.jetty.deploy.config;

import java.util.Properties;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.deployment.specifics.StartServerPropertiesProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.netbeans.modules.jeeserver.jetty.deploy.JettyServerSpecifics;
import org.netbeans.modules.jeeserver.jetty.util.JettyConstants;
//import org.netbeans.modules.jeeserver.jetty.util.NpnConfig;
import org.openide.filesystems.FileObject;

/**
 *
 * @author V. Shyshkin
 */
public class JettyStartServerPropertiesProvider implements StartServerPropertiesProvider {

    private Properties startProperties;
    private Properties stopProperties;
    private Properties debugProperties;
    private Properties profileProperties;

    public JettyStartServerPropertiesProvider() {
    }

    @Override
    public FileObject getBuildXml(Project serverProject) {
        return serverProject.getProjectDirectory().getFileObject(JettyConstants.JETTYBASE_FOLDER + "/build.xml");
    }

    protected String getStartJar(Project serverProject) {
        InstanceProperties ip = BaseUtils.managerOf(serverProject.getLookup()).getInstanceProperties();
        return ip.getProperty(BaseConstants.HOME_DIR_PROP) + "/start.jar";

    }

    protected Properties getProps(Project serverProject) {
        BaseDeploymentManager manager = BaseUtils.managerOf(serverProject.getLookup());
        Properties props = new Properties();
        props.setProperty("target", "run");
        props.setProperty("runtime.encoding", "UTF-8");

        props.setProperty("start.jar", getStartJar(serverProject));

        props.setProperty("server.debug.port", manager.getInstanceProperties().getProperty(BaseConstants.DEBUG_PORT_PROP));
        props.setProperty("server.debug.transport", "dt_socket");
        props.setProperty("debug.args.line", "-Xdebug");
        props.setProperty("stop.port", manager.getInstanceProperties().getProperty(BaseConstants.SHUTDOWN_PORT_PROP));
        props.setProperty("stop.key", JettyServerSpecifics.JETTY_SHUTDOWN_KEY);        
        return props;
    }

    @Override
    public Properties getStartProperties(Project serverProject) {
        if (startProperties == null) {
            startProperties = getProps(serverProject);
            startProperties.setProperty("target", "run");
        }
//        String npnBoot = getNpnBootClassPath(serverProject);
//        startProperties.setProperty("npn.boot", npnBoot);
        return startProperties;
    }
/*    protected String getNpnBootClassPath(Project serverProject) {
        String path = "";
        FileObject fo = serverProject.getProjectDirectory()
                .getFileObject(JettyConstants.JETTY_HTTP_INI);
        
        if ( fo != null ) {
            path = new NpnConfig(serverProject).getBootClassPathLine();
        }
        return path;
    }
*/    
    @Override
    public Properties getStopProperties(Project serverProject) {
        if (stopProperties == null) {
            BaseDeploymentManager manager = BaseUtils.managerOf(serverProject.getLookup());
            stopProperties = new Properties();
            stopProperties.setProperty("target", "stop");
            stopProperties.setProperty("start.jar", getStartJar(serverProject));
            stopProperties.setProperty("stop.port", manager.getInstanceProperties().getProperty(BaseConstants.SHUTDOWN_PORT_PROP));
            stopProperties.setProperty("stop.key", JettyServerSpecifics.JETTY_SHUTDOWN_KEY);        
        }
        return stopProperties;
    }

    @Override
    public Properties getDebugProperties(Project serverProject) {
        if (debugProperties == null) {
            debugProperties = getProps(serverProject);
            debugProperties.setProperty("target", "debug");
        }
        return debugProperties;
    }

    @Override
    public Properties getProfileProperties(Project serverProject) {
        BaseDeploymentManager manager = BaseUtils.managerOf(serverProject.getLookup());        
        profileProperties = new Properties();
        profileProperties.setProperty("target", "profile");
        String profile_args = BaseUtils.getProfileArgs(BaseUtils.managerOf(serverProject.getLookup()));
        profileProperties.setProperty("profiler.args", profile_args);
        profileProperties.setProperty("start.jar", getStartJar(serverProject));
        profileProperties.setProperty("stop.port", manager.getInstanceProperties().getProperty(BaseConstants.SHUTDOWN_PORT_PROP));
        profileProperties.setProperty("stop.key", JettyServerSpecifics.JETTY_SHUTDOWN_KEY);        
        
        return profileProperties;
    }

}
