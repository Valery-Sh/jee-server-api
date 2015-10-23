/**
 * This file is part of Base JEE Server support in NetBeans IDE.
 *
 * Base JEE Server support in NetBeans IDE is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * Base JEE Server support in NetBeans IDE is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should see the GNU General Public License here:
 * <http://www.gnu.org/licenses/>.
 */
package org.netbeans.modules.jeeserver.base.deployment.utils;

import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;

/**
 *
 * @author V. Shyshkin
 */
public class BaseConstants {
    
    
    public static final String COMMAND_MANAGER_JAR_POSTFIX = "-command-manager";
    public static final String COMMAND_MANAGER_JAR_NAME_PROP = "command.manager.jar.name";

    public static final String WEB_PROJECTTYPE = "org.netbeans.modules.web.project";    
    public static final String HTML5_PROJECTTYPE = "org.netbeans.modules.web.clientproject";

    public static final String DEPLOY_WEB_PROJECTTYPE = "web.project";    
    public static final String DEPLOY_HTML5_PROJECTTYPE = "web.clientproject";
    
    public static final String HTML5_SITE_ROOT_PROP = "site.root.folder";    
    public static final String HTML5_DEFAULT_SITE_ROOT_PROP = "public_html";
    public static final String HTML5_WEB_CONTEXT_ROOT_PROP = "web.context.root";    
    
    public static final String INCREMENTAL_DEPLOYMENT = "incrementalDeployment";
    public static String SERVER_ID_PROP = "server-id";
    public static String SERVER_ACTUAL_ID_PROP = "server-actual-id";    
    public static String URIPREFIX_NO_ID = "deploy:server";
    
    public static final String SERVER_HELPER_LIBRARY_POSTFIX = "ext";
    
    public static final long   SERVER_TIMEOUT_DELAY = 20000;
    
    public static final String CONTEXTPATH_PROP = "contextPath";
    
    
    /**
     * Keys for InstanceProperties
     */
    public static final String DISPLAY_NAME_PROP = InstanceProperties.DISPLAY_NAME_ATTR;

    public static final String HTTP_PORT_PROP = InstanceProperties.HTTP_PORT_NUMBER;
    public static final String SHUTDOWN_PORT_PROP = "shutdownPortNumber";
    
    public static final String URL_PROP = InstanceProperties.URL_ATTR;
    public static final String SERVER_LOCATION_PROP = "serverLocation"; 
//    public static final String SERVER_INSTANCE_DIR_PROP = "server-instance-dir";     
    
//    public static final String SERVER_INSTANCIES_DIR_PROP = "server-instances-dir"; 
    
    public static final String HOME_DIR_PROP = "home_dir";     
    public static final String SERVER_VERSION_PROP = "server_version";         
    
    
    public static final String HOST_PROP = "host"; 
    public static final String DEBUG_PORT_PROP = "debug_port"; 
    public static final String JAVA_PLATFORM_PROP = "java_platform";
    public static final String JAVADOCS_PROP = "javadocs"; 
    public static final String PLATFORM_ANT_NAME_PROP = "platform.ant.name"; 
    public static final String NAMESPACE_PROP = "persistentNamespace"; 
    
    public static final String WEB_APP_LOCATION_PROP = "webAppLocation";    
    
}
