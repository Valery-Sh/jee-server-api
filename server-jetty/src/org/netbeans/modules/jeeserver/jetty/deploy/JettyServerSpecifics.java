/**
 * This file is part of Jetty Server support in NetBeans IDE.
 *
 * Jetty Server support in NetBeans IDE is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the License,
 * or (at your option) any later version.
 *
 * Jetty Server support in NetBeans IDE is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should see the GNU General Public License here:
 * <http://www.gnu.org/licenses/>.
 */
package org.netbeans.modules.jeeserver.jetty.deploy;

import java.awt.Image;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.deployment.specifics.ServerSpecifics;
import org.netbeans.modules.jeeserver.base.deployment.specifics.WizardDescriptorPanel;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.base.deployment.ServerInstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.spi.FindJSPServlet;
import org.netbeans.modules.jeeserver.base.deployment.specifics.StartServerPropertiesProvider;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.netbeans.modules.jeeserver.jetty.deploy.config.JettyServerModuleConfiguration;
import org.netbeans.modules.jeeserver.jetty.project.JettyProjectLogicalView;
import org.netbeans.modules.jeeserver.jetty.project.nodes.libs.LibUtil;
import org.netbeans.modules.jeeserver.jetty.project.nodes.libs.LibrariesFileNode;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.windows.InputOutput;

/**
 *
 * @author V. Shyshkin
 */
public class JettyServerSpecifics implements ServerSpecifics {

    private static final Logger LOG = Logger.getLogger(JettyServerSpecifics.class.getName());

    public static final String JETTY_SHUTDOWN_KEY = "netbeans";
    public static final String IMAGE = "org/netbeans/modules/jeeserver/jetty/resources/jetty01-16x16.jpg";

    @Override
    public boolean pingServer(Project serverProject) {

        ServerInstanceProperties sp = BaseUtils.getServerProperties(serverProject);
        String urlString = sp.getManager().buildUrl();

        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            if (connection == null) {
                return false;
            }
            connection.setRequestMethod("POST");
            Map<String, List<String>> headerFields = connection.getHeaderFields();
            if (headerFields == null) {
                return false;
            }
            for (Map.Entry<String, List<String>> e : headerFields.entrySet()) {
                if (e.getKey() == null || !e.getKey().trim().toLowerCase().equals("server")) {
                    continue;
                }
                for (String v : e.getValue()) {
                    if (v != null && v.trim().toLowerCase().startsWith("jetty")) {
                        return true;
                    }
                }
            }
        } catch (SocketException e) {
        } catch (IOException e) {
        }
        return false;

    }

    @Override
    public boolean shutdownCommand(Project serverProject) {

        boolean result;

        ServerInstanceProperties sp = BaseUtils.getServerProperties(serverProject);

        String key = JETTY_SHUTDOWN_KEY;

        // for future  String pkey = sp.getServerConfigProperties().getProperty("jetty-shutdown-key");
        String pkey = null;
        if (pkey != null) {
            key = pkey;
        }

        ExecutorTask task;

        StartServerPropertiesProvider pp = serverProject.getLookup().lookup(StartServerPropertiesProvider.class);

        String[] targets = new String[]{"stop"};

        FileObject buildXml = pp.getBuildXml(serverProject);
        Properties props = pp.getStopProperties(serverProject);
        ExecutorTask st = BaseUtils.managerOf(serverProject).getServerTask();
        InputOutput stio = null;
        if (st != null) {
            stio = st.getInputOutput();
        }

        try {
            task = ActionUtils.runTarget(buildXml, targets, props);
            InputOutput io = task.getInputOutput();
            task.waitFinished(BaseConstants.SERVER_TIMEOUT_DELAY);
            io.getOut().flush();
            io.closeInputOutput();
            //io.select();
            if (stio != null) {
                stio.select();
            }

        } catch (IOException | IllegalArgumentException | InterruptedException ex) {
            LOG.log(Level.INFO, ex.getMessage());
        }

        long pingtimeout = System.currentTimeMillis() + BaseConstants.SERVER_TIMEOUT_DELAY;
        result = true;
        while (pingServer(serverProject)) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ie) {
            }
            if (System.currentTimeMillis() > pingtimeout) {
                result = false;
                break;
            }
        }
        return result;
    }

    @Override
    public String execCommand(Project serverProject, String cmd) {
        HttpURLConnection connection = null;
        String result = null;
        try {
            String urlstr = "/jeeserver/manager?" + cmd;
            URL url = new URL(buildUrl(serverProject) + urlstr);

            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            if (connection.getResponseCode() == 200) {
                result = getResponseData(connection, cmd);
            }

        } catch (SocketException ex) {
            LOG.log(Level.INFO, ex.getMessage()); //NOI18N
        } catch (IOException ex) {
            LOG.log(Level.INFO, ex.getMessage()); //NOI18N

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return result;
    }

    protected boolean saveLineSeparator(String cmd) {
        String command = cmd.trim();
        int i = command.indexOf('&');
        if (i < 0) {
            i = command.length();
        }
        return command.substring(4, i).equals("printinfo");
    }

    protected String getResponseData(HttpURLConnection connection, String cmd) {
        StringBuilder sb = new StringBuilder();
        BufferedReader in;
        try {
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
        } catch (IOException ex) {
            LOG.log(Level.INFO, ex.getMessage());
            return null;
        }
        String inputLine;
        boolean saveLineSeparator = saveLineSeparator(cmd);
        try {
            while ((inputLine = in.readLine()) != null) {
                sb.append(inputLine);
                if (saveLineSeparator) {
                    sb.append(System.lineSeparator());
                }
            }
        } catch (IOException ex) {
            LOG.log(Level.INFO, ex.getMessage());
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                LOG.log(Level.INFO, ex.getMessage());
            }
        }
        return sb.toString();
    }

    private String buildUrl(Project p) {
        return BaseUtils.managerOf(p).buildUrl();
    }

    @Override
    public FindJSPServlet getFindJSPServlet(DeploymentManager dm) {
        return new JettyServerFindJspServlet((BaseDeploymentManager) dm);
    }

    @Override
    public Image getProjectImage(Project serverProject) {
        return ImageUtilities.loadImage(IMAGE);
    }

    @Override
    public void projectCreated(FileObject projectDir, Map<String, Object> props) {
    }

    @Override
    public boolean needsShutdownPort() {
        return true;
    }

    @Override
    public WizardDescriptorPanel getAddonCreateProjectPanel(org.openide.WizardDescriptor wiz) {
        return null;
    }

    @Override
    public int getDefaultPort() {
        return 8080;
    }

    @Override
    public int getDefaultDebugPort() {
        return 4000;
    }

    /**
     * Returns {@literal Integer.MAX_VALUE} to specify that {@literal jetty}
     * doesn't support shutdown port.
     *
     * @return Integer.MAX_VALUE
     */
    @Override
    public int getDefaultShutdownPort() {
        return 8180;
    }

    @Override
    public void serverStarted(DeploymentManager manager) {
        BaseDeploymentManager dm = (BaseDeploymentManager) manager;
BaseUtils.out("1 Specifics serverStartted time=" + System.currentTimeMillis());
        
        LibUtil.updateLibraries(dm.getServerProject());
/*        LibrariesFileNode ln = (LibrariesFileNode)dm.getServerProject().getLookup()
                .lookup(JettyProjectLogicalView.class)
                .getLibrariesRootNode();
        if( ln != null ) {
            ((LibrariesFileNode.FileKeys) ln.getChildrenKeys()).addNotify();
        }
*/        
BaseUtils.out("5 Specifics serverStartted time=" + System.currentTimeMillis());

    }
    @Override
    public Properties getContextPoperties(FileObject config) {
        return JettyServerModuleConfiguration.getContextProperties(config);
    }            

}
