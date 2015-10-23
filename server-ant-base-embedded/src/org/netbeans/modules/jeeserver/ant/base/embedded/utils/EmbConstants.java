package org.netbeans.modules.jeeserver.ant.base.embedded.utils;

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
    
    
    public static final String WEBAPPLICATIONS_FOLDER = "server-instance-config";  
    public static final String SERVER_INSTANCE_CONFIG_FOLDER = WEBAPPLICATIONS_FOLDER;  
    
    public static final String SERVER_INSTANCE_PROPERTIES_FILE = "server-instance.properties";        
    public static final String SERVER_INSTANCE_PROPERTIES_PATH = WEBAPPLICATIONS_FOLDER + "/" + SERVER_INSTANCE_PROPERTIES_FILE;    
    
    public static final String WEBAPPS_DEFAULT_DIR_NAME = "web-apps";
    
}
