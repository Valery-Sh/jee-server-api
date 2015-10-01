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
package org.netbeans.modules.jeeserver.base.deployment.specifics;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.plugins.spi.FindJSPServlet;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.openide.filesystems.FileObject;

/**
 *
 * @author V. Shyshkin
 */
public interface ServerSpecifics extends LicensesAcceptor {

    //Lookup getServerLookup(BaseDeploymentManager dm);

    default void register(BaseDeploymentManager dm) {

    }
    default void iconChange(String uri,boolean newValue) {
        
    }
    default void displayNameChange(String uri,String newValue) {
        
    }
    
    default boolean pingServer(BaseDeploymentManager dm) {
        return pingServer(dm, 50);
    }

    default public boolean pingServer(BaseDeploymentManager dm, int timeout) {

        boolean result = false;
        long t1 = 0;
        String urlString = dm.buildUrl();

        if (urlString == null) {
            return false;
        }
        try {
            URL url = new URL(urlString);
            final URLConnection connection = url.openConnection();
            if ( timeout != 0) {
                connection.setConnectTimeout(timeout);
            }

            final Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        connection.connect();
                    } catch (IOException ex) {
                        Thread.interrupted();
                    }
                }
            });

            t.start();
            t.join(timeout);

            t1 = System.currentTimeMillis();
            Map<String, List<String>> headerFields = connection.getHeaderFields();
            result = isConnected(headerFields);
        } catch (InterruptedException | IOException ex) {
            result = false;
        }
        return result;
    }

    default boolean isConnected(Map<String, List<String>> headerFields) {
        long t1 = 0;
        if (headerFields == null) {
            return false;
        }
        boolean result = false;
        
        for (Map.Entry<String, List<String>> e : headerFields.entrySet()) {
            if (e.getKey() == null || !e.getKey().trim().toLowerCase().equals("server")) {
                continue;
            }
            for (String v : e.getValue()) {
                if (v != null && v.trim().toLowerCase().startsWith("jetty")) {
                    result = true;
                    break;
                }
            }
            if (result) {
                break;
            }
        }
        return result;
    }
    boolean shutdownCommand(BaseDeploymentManager dm);

    String execCommand(BaseDeploymentManager dm, String cmd);

    /**
     *
     * @param dm
     * @return May return null.
     */
    FindJSPServlet getFindJSPServlet(DeploymentManager dm);

    Image getProjectImage(Project serverProject);

    /**
     *
     * @param projDir
     * @param props
     */
    void projectCreated(FileObject projDir, Map<String, Object> props);

    boolean needsShutdownPort();

    int getDefaultPort();

    int getDefaultDebugPort();

    int getDefaultShutdownPort();

    default String[] getSupportedContextPaths() {
        return new String[]{"WEB-INF/jetty-web.xml", "WEB-INF/web-jetty.xml"};
    }

    Properties getContextPoperties(FileObject config);

    /**
     *
     * @param manager
     */
    default void serverStarted(DeploymentManager manager) {

    }

    default void serverStarting(DeploymentManager manager) {

    }

    default ProjectWizardBuilder getWizardBuilder() {
        return null;
    }

    default InstanceBuilder getInstanceBuilder(Properties config, InstanceBuilder.Options options) {
        return null;
    }

    /**
     * Returns a java code of the main class of a server instance project as a
     * template
     *
     * @return default value as {@literal null}
     */
    /*    default InputStream getServerInstanceMainClass() {
     return null;
     }
     */
    //WizardDescriptorPanel getAddonCreateProjectPanel(WizardDescriptor wiz);
}
