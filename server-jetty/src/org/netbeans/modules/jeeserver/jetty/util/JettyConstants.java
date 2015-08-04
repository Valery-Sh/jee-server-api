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
package org.netbeans.modules.jeeserver.jetty.util;

/**
 *
 * @author V. Shyshkin
 */
public class JettyConstants {
    
    public static final String JETTY_HTTP_TIMEOUT = "http.timeout";
    public static final String JETTY_HTTP_PORT = "jetty.port";
    public static final String JETTY_HTTPS_PORT = "https.port";
    public static final String JETTY_HTTPS_TIMEOUT = "https.timeout";
    public static final String JETTY_SPDY_PORT = "spdy.port";    
    public static final String JETTY_SPDY_TIMEOUT = "spdy.timeout";
    public static final String JETTY_SECURE_PORT_PROP = "jetty.secure.port";
    public static final String JETTY_KEYSTORE_PROP = "jetty.keystore";
    public static final String JETTY_TRUSTSTORE_PROP = "jetty.truststore";

    
    public static final String ENABLE_JSF = "enableJsf";
    public static final String ENABLE_NPN = "enableNPN";
    public static final String ENABLE_CDI = "enableCDI";
    public static final String ENABLE_SPDY = "enableSPDY";
    public static final String ENABLE_SSL = "enableSSL";
    public static final String ENABLE_HTTPS = "enableHTTPS";
    
    
    
    /**
     * protected to avoid usage instead of WEBAPPS_FOLDER
     */
    protected static final String  WEBAPPS_FOLDER_NAME = "webapps";
    
    public static final String  JETTYBASE_FOLDER = "jettybase";
    public static final String  WEBAPPS_FOLDER = JETTYBASE_FOLDER + "/" + WEBAPPS_FOLDER_NAME;    
    public static final String  JETTY_HTTP_INI = JETTYBASE_FOLDER + "/start.d/http.ini";
    public static final String  JETTY_START_INI = JETTYBASE_FOLDER + "/start.ini";
    
    public static final String  JETTY_START_D = JETTYBASE_FOLDER + "/start.d";
    
    public static final String LIBRARY_FILE = ".nblibrary";
    //========== Jetty Server Lificycle State ===================
    
    public static final String STOPPED="STOPPED";
    public static final String FAILED="FAILED";
    public static final String STARTING="STARTING";
    public static final String STARTED="STARTED";
    public static final String STOPPING="STOPPING";
    public static final String RUNNING="RUNNING";
    
}
