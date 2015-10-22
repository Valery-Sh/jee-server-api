package org.netbeans.modules.jeeserver.base.embedded.utils;

import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;

/**
 *
 * @author V. Shyshkin
 */
public class SuiteConstants extends BaseConstants {
    
    public enum DistributeAs {
        SINGLE_JAR_UNPACKED_WARS,
        SINGLE_JAR_WARS,
        UNPACKED_WARS,
        WARS
    }
    // ----------- CommandManager properties names ---------------------
    public static String BASE_DIR_PROP = "base.dir";
    public static String COMMAND_MANAGER_PROPERIES_PROP = "command.manager.pom.properties";    
    public static final String COMMAND_MANAGER_GROUPID = "command.manager.groupId";
    public static final String COMMAND_MANAGER_ARTIFACTID = "command.manager.artifactId";    
    public static final String COMMAND_MANAGER_VERSION = "command.manager.version";    
//    public static final String INSTANCE_NBDEPLOYMENT_FOLDER = "nbdeployment"; 
    
    //public static final String MAVEN_REPO_LIB_PATH = INSTANCE_NBDEPLOYMENT_FOLDER + "/lib";        
    //public static final String MAVEN_REPO_LIB_PATH_PROP = "maven.repo.lib.path";            
    public static final String ANT_LIB_PATH = "lib";            
    
    // ----------- END CommandManager properties names -------------------------------------------------
    public static final String PANEL_VISITED_PROP = "panel.visited";
    
    public static final String MAVEN_MAIN_CLASS_PROP = "maven.main.class";
    public static final String MAVEN_DEBUG_CLASSPATH_PROP = "maven.debug.classpath";
    public static final String MAVEN_RUN_CLASSPATH_PROP = "maven.run.classpath";
    
    
    
    public static final String MAVEN_WORK_DIR_PROP = "maven.work.dir";
    
    public static String TMP_DIST_WEB_APPS = "embedded_suite_server_instance";
    public static String SERVER_INSTANCE_WEB_APPS_PROPS = "server-instance-web-apps.properties";    
    
    public static final String SERVER_INSTANCE_NAME_PROP = "server-instance-display-name";     
    public static final String SERVER_INSTANCES_DIR_PROP = "server-instances-dir"; 
    public static final String SUITE_PROJECT_LOCATION = "embedded-suite-project-location"; 
    public static final String SUITE_URL_ID = ":server:suite:project:";
    public static final String SUITE_CONFIG_FOLDER = "nbconfig"; 
    public static final String SUITE_PROPERTIES = "suite.properties"; 
    public static final String UID_PROPERTY_NAME = "uid";    
    
    public static final String SUITE_PROPERTIES_LOCATION = SUITE_CONFIG_FOLDER + "/" + SUITE_PROPERTIES; 
    
    public static final String SERVER_INSTANCES_FOLDER = "server-instances";
    public static final String SERVER_NBCONFIG_FOLDER = "nbconfig";

    public static final String  DIST_WEB_APPS = "dist-web-apps";

//    public static final String  DIST_WEB_APPS_LOCATION = INSTANCE_NBDEPLOYMENT_FOLDER + "/" + DIST_WEB_APPS;
    
    @StaticResource
    public static final String SERVER_INSTANCES_ICON = "org/netbeans/modules/jeeserver/base/embedded/resources/nbservers-16x16.png";
    @StaticResource
    public static final String WEB_APP_ICON = "org/netbeans/modules/jeeserver/base/embedded/resources/webProjectIcon.gif";
    @StaticResource
    public static final String DIST_WEB_APPS_BADGE_ICON = "org/netbeans/modules/jeeserver/base/embedded/resources/web-pages-badge.png";
    
    @StaticResource
    public static final String SERVER_PROJECT_ICON = "org/netbeans/modules/jeeserver/base/embedded/resources/servers-16x16.png";
    
    public static final String SERVER_INSTANCES_DISPLAYNAME = "Server Instancies";
    
    
    public static final String SERVER_CONFIG_FOLDER = "server-config";
    public static final String SERVER_ACTIVE_MODULES_FOLDER = SERVER_CONFIG_FOLDER + "/active-modules";    
    public static final String SERVER_MODULES_FOLDER = SERVER_CONFIG_FOLDER + "/modules";        
    public static final String SERVER_PROJECT_FOLDER = "server-project";
    
    public static final String START_INI_FILE = SERVER_CONFIG_FOLDER + "/start.ini";
    public static final String MODULES_FOLDER = SERVER_CONFIG_FOLDER + "/modules";

    
    public static final String SERVER_ALL_LIBRARY_POSTFIX = "all";    
    
    public static final String WEBAPP_CONFIG_FILE = "context.properties";
    
    public static final String PACKAGE_DIST = "package-dist";
    
    public static final String WEB_REF = "webref";
    public static final String WAR_REF = "warref";            
    public static final String HTML_REF = "htmref";            
    
    public static final String WEB_APPS_PACK = "web-apps-pack";        
    
    public static final String EAR_REF = "earref";        
    public static final String JEE_REF = "jeeref";        
    
    
    public static final String WEBAPPS_DIR_PROP = "deployWebapps";
    public static final String HTML5_SERVER_URI_PROP = "embedded.server.instance.uri";
    
    
    /**
     * Keys for InstanceProperties
     */
    
    
    public static final String REG_WEB_APPS_FOLDER = SERVER_CONFIG_FOLDER + "/reg-web-apps";  
    
    public static final String SERVER_INSTANCE_PROPERTIES_FILE = "server-instance.properties";        
    //public static final String INSTANCE_PROPERTIES_PATH = REG_WEB_APPS_FOLDER + "/" + INSTANCE_PROPERTIES_FILE;    
    
    public static final String WEBAPPS_DEFAULT_DIR_NAME = "web-apps";

    //public static final String INSTANCE_PROPERTIES_PATH = SERVER_PROJECT_FOLDER + "/src/main/resources/" + INSTANCE_PROPERTIES_FILE;
    
}
