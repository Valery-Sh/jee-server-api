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
package org.netbeans.modules.jeeserver.base.deployment.config;

/**
 *
 * @author V. Shyshkin
 */
public class WebModuleConfig {
    //private String key;
    private String contextPath;
    private String webProjectPath;
    private String webFolderPath;
    
    
    public WebModuleConfig(String contextPath, String webProjectPath) {
        this.contextPath = contextPath;
        this.webProjectPath = webProjectPath;
    }
/*    @Override
    public String toString() {
        return key;
    }
*/    
    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
        
    }

    public String getWebProjectPath() {
        return webProjectPath;
    }

    public void setWebProjectPath(String webProjectPath) {
        this.webProjectPath = webProjectPath;
    }

    public String getWebFolderPath() {
        return webFolderPath;
    }

    public void setWebFolderPath(String webFolderPath) {
        this.webFolderPath = webFolderPath;
    }
    
}
