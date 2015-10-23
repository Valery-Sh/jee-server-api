/**
 * This file is part of Tomcat Server Embedded support in NetBeans IDE.
 *
 * Tomcat Server Embedded support in NetBeans IDE is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * Tomcat Server Embedded support in NetBeans IDE is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should see the GNU General Public License here:
 * <http://www.gnu.org/licenses/>.
 */
package org.netbeans.modules.jeeserver.tomcat.embedded;

import java.io.File;
import org.netbeans.modules.j2ee.deployment.plugins.spi.FindJSPServlet;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.base.deployment.jsp.BaseFindJSPServlet;

/**
 *
 * @author Valery
 */
public class TomcatFindJspServlet extends BaseFindJSPServlet implements FindJSPServlet {
    
    public TomcatFindJspServlet(BaseDeploymentManager manager) {
        super(manager);
    }
    
    @Override
    protected File getJspDir(File baseDir, String convertedContextPath, String port) {
        for (File folder : baseDir.listFiles()) {
            if (folder.getName().startsWith("jetty-")
                    && folder.getName().contains("-" + port)
                    && folder.getName().contains(convertedContextPath)) {
                File r = new File(folder, "jsp");
                return r;
            }
        }
        return null;

    }
    @Override
    protected File getWorkDirByServerHome() {
        File work = new File(System.getProperty("jetty.home"), "work");
        if (work.exists() && work.isDirectory()) {
            return work;
        }
        return null;
    }
    
}
