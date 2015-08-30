package org.netbeans.modules.jeeserver.base.embedded.utils;

import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;

/**
 *
 * @author V. Shyshkin
 */
public class EmbConstants extends BaseConstants {
    
    public enum DistributeAs {
        SINGLE_JAR_UNPACKED_WARS,
        SINGLE_JAR_WARS,
        UNPACKED_WARS,
        WARS
    }
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
    
    public static final String INSTANCE_PROPERTIES_FILE = "server-instance.properties";        
    //public static final String INSTANCE_PROPERTIES_PATH = REG_WEB_APPS_FOLDER + "/" + INSTANCE_PROPERTIES_FILE;    
    
    public static final String WEBAPPS_DEFAULT_DIR_NAME = "web-apps";

    public static final String INSTANCE_PROPERTIES_PATH = SERVER_PROJECT_FOLDER + "/src/main/resources/" + INSTANCE_PROPERTIES_FILE;
    
}
