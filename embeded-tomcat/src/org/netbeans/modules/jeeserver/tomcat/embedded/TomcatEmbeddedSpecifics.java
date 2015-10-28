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

import java.awt.Image;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.plugins.spi.FindJSPServlet;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.base.deployment.specifics.InstanceBuilder;
import org.netbeans.modules.jeeserver.base.deployment.specifics.StartServerPropertiesProvider;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.netbeans.modules.jeeserver.base.embedded.EmbeddedInstanceBuilder;
import org.netbeans.modules.jeeserver.base.embedded.specifics.EmbeddedServerSpecifics;
import org.netbeans.modules.jeeserver.base.embedded.apisupport.SupportedApiProvider;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteConstants;
import org.openide.filesystems.FileObject;

import org.openide.util.ImageUtilities;

/**
 *
 * @author V. Shyshkin
 */
public class TomcatEmbeddedSpecifics implements EmbeddedServerSpecifics {
    
    
    private static final Logger LOG = Logger.getLogger(TomcatEmbeddedSpecifics.class.getName());

    @StaticResource
    private static final String SERVER_IMAGE = "org/netbeans/modules/jeeserver/tomcat/embedded/resources/tomcat.png";

//    private static final String HELPER_JAR = "nb-tomcat-helper.jar";

    @Override
    public boolean pingServer(BaseDeploymentManager dm) {
        
//        ServerInstanceProperties sp = SuiteUtil.getServerProperties(serverProject);

        Socket socket = new Socket();
        int port = Integer.parseInt(dm.getInstanceProperties().getProperty(BaseConstants.HTTP_PORT_PROP));
        int timeout = 50;
        try {
            try {
                socket.connect(new InetSocketAddress("localhost", port), timeout); // NOI18N
                socket.setSoTimeout(timeout);
                try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true); BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                    out.println("HEAD /netbeans-tomcat-status-test HTTP/1.1\r\nHost: localhost:" + port + "\r\n");
                    out.flush();
                    String text = in.readLine();
                    if (text == null || !text.startsWith("HTTP/")) { // NOI18N
                        return false; // not an http response
                    }
                    Map headerFileds = new HashMap();
                    while ((text = in.readLine()) != null && text.length() > 0) {
                        int colon = text.indexOf(':');
                        if (colon <= 0) {
                            return false; // not an http header
                        }
                        String name = text.substring(0, colon).trim();
                        String value = text.substring(colon + 1).trim();
                        List list = (List) headerFileds.get(name);
                        if (list == null) {
                            list = new ArrayList();
                            headerFileds.put(name, list);
                        }
                        list.add(value);
                    }
                    List/*<String>*/ server = (List/*<String>*/) headerFileds.get("Server");
                    if (server != null) {
                        if (server.contains("Apache-Coyote/1.1")) { // NOI18N
                            if (headerFileds.get("X-Powered-By") == null) { // NIO18N
                                // it is probably Tomcat with JWSDP installed
                                return true;
                            }
                        } else if (server.contains("Sun-Java-System/Web-Services-Pack-1.4")) {  // NOI18N
                            // it is probably Tomcat with JWSDP installed
                            return true;
                        }
                    }
                    return false;
                }
            } finally {
                socket.close();
            }
        } catch (IOException ioe) {
            return false;
        }
    }

    @Override
    public boolean shutdownCommand(BaseDeploymentManager dm) {

        int port = Integer.parseInt(dm.getInstanceProperties().getProperty(BaseConstants.SHUTDOWN_PORT_PROP));
        // checking whether a socket can be created is not reliable enough, see #47048
BaseUtil.out("TomcatEmbeddedSpecifics port=" + port);        
        Socket socket = new Socket();
        

        int timeout = 2000;
        try {
            try {
                socket.connect(new InetSocketAddress("localhost", port), timeout); // NOI18N
                socket.setSoTimeout(timeout);
                try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                        // request
                        out.println("netbeans"); // NOI18N
                        out.flush();
                        // response
                        in.readLine();
                    }
                }
            } finally {
                socket.close();
            }
        } catch (IOException ioe) {
            return false;
        }

        long pingtimeout = System.currentTimeMillis() + BaseConstants.SERVER_TIMEOUT_DELAY;
        boolean result = true;
        while (pingServer(dm)) {
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
    public String execCommand(BaseDeploymentManager dm, String cmd) {
        HttpURLConnection connection = null;
        String result = null;
        try {
            String urlstr = "/jeeserver/manager?" + cmd;
            
            BaseUtil.out("TomcatSpecifics: deployCommand urlStr=" + urlstr);

            URL url = new URL(buildUrl(dm) + urlstr);
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.getResponseCode();
            result = getResponseData(connection, cmd);
        } catch (SocketException e) {
        } catch (IOException e) {
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return result;
    }

    protected String getResponseData(HttpURLConnection connection, String cmd) {
        StringBuilder sb = new StringBuilder();
        BufferedReader in;// = null;
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

    protected boolean saveLineSeparator(String cmd) {
        String command = cmd.trim();
        int i = command.indexOf('&');
        if (i < 0) {
            i = command.length();
        }
        return command.substring(4, i).equals("printinfo");
    }

    private String buildUrl(BaseDeploymentManager dm) {
        return "http://localhost:" + dm.getInstanceProperties().getProperty(BaseConstants.HTTP_PORT_PROP);
    }

    @Override
    public FindJSPServlet getFindJSPServlet(DeploymentManager dm) {
        return new TomcatFindJspServlet((BaseDeploymentManager) dm);
    }
    @Override
    public Image getServerImage(Project serverProject) {
        return ImageUtilities.loadImage(SERVER_IMAGE);
    }

/*    @Override
    public Image getProjectImage(Project serverProject) {
        return ImageUtilities.loadImage(IMAGE);
    }
*/    
    @Override
    public InstanceBuilder getInstanceBuilder(Properties props, InstanceBuilder.Options options) {
        InstanceBuilder ib = null;

        if ("ant".equals(props.getProperty("project.based.type"))) {
BaseUtil.out("TomcatSpecifics ANT.BASED");
            if (options.equals(InstanceBuilder.Options.CUSTOMIZER)) {
                ib = new TomcatCustomizeInstanceBuilder(props, options);
            } else {
                ib = new TomcatInstanceBuilder(props, options);
            }
            ((EmbeddedInstanceBuilder) ib).setMavenbased(false);

        } else if ("maven".equals(props.getProperty("project.based.type"))) {
BaseUtil.out("TomcatSpecifics MAVEB.BASED");            
            if (options.equals(InstanceBuilder.Options.CUSTOMIZER)) {
                ib = new TomcatCustomizeInstanceBuilder(props, options);
            } else {
                ib = new TomcatMavenInstanceBuilder(props, options);
            }
            ((EmbeddedInstanceBuilder) ib).setMavenbased(true);
        }

        return ib;
    }
    
/*
    @Override
    public void projectCreated(FileObject projectDir, Map<String, Object> props) {
        //
        // Check whether helper lib contains jar file with a name 
        // "nb-tomcat-helper.jar". If true then we assume that it is a 
        // jar provided by the plugin.
        //
        //
        // Add command-manager.jar to the classpath of the project
        //
        String actualServerId = (String)props.get(SuiteConstants.SERVER_ACTUAL_ID_PROP);
        String cmOut = actualServerId + "-command-manager";
        String cmIn = "/org/netbeans/modules/jeeserver/tomcat/embedded/resources/" + actualServerId + "-command-manager.jar";
        
                
        FileObject libExt = projectDir.getFileObject("lib/ext");
        FileObject cmFo;// = null;
        try {
            cmFo = libExt.createData(cmOut, "jar");
            try (OutputStream os = cmFo.getOutputStream(); InputStream is = getClass().getClassLoader().getResourceAsStream(cmIn)) {
                FileUtil.copy(is, os);
            }
            this.addJarToServerClassPath(FileUtil.toFile(cmFo),projectDir);
        } catch (IOException ex) {
            LOG.log(Level.INFO, ex.getMessage()); //NOI18N
        }

        //
        // Plugin jar => we can create a class from template
        //
        DataObject template;
        DataFolder outputFolder;

        Map<String, Object> templateParams = new HashMap<>(1);
        try {
            FileObject srcFo = projectDir.getFileObject("src");
            FileObject toDelete = projectDir.getFileObject("src/javaapplication0");
            toDelete.delete();
            FileObject targetFo = srcFo.createFolder("org")
                    .createFolder("embedded")
                    .createFolder("server");
            outputFolder = DataFolder.findFolder(targetFo);
            template = DataObject.find(
                    FileUtil.getConfigFile("Templates/tomcat7/TomcatEmbeddedServer"));
            templateParams.put("port", props.get(BaseConstants.HTTP_PORT_PROP));
            templateParams.put("shutdownPort", props.get(BaseConstants.SHUTDOWN_PORT_PROP));

            templateParams.put("comStart", "");
            templateParams.put("comEnd", "");

            template.createFromTemplate(
                    outputFolder,
                    "TomcatEmbeddedServer.java",
                    templateParams);
            setMainClass(projectDir);
        } catch (IOException ex) {
            LOG.log(Level.INFO, ex.getMessage()); //NOI18N
        }
    }

    protected void addJarToServerClassPath(File jar, FileObject projectDir) throws IOException {
        
        if (projectDir == null || jar == null || !jar.exists()) {
            return;
        }
        URI[] uri = new URI[]{Utilities.toURI(jar)};
        ProjectClassPathModifier.addRoots(uri, getSourceRoot(projectDir), ClassPath.COMPILE);
    }

    protected FileObject getSourceRoot(FileObject projectDir) {
        Project p = FileOwnerQuery.getOwner(projectDir);
        Sources sources = ProjectUtils.getSources(p);
        SourceGroup[] sourceGroups
                = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        FileObject result = null;
        try {
            for (SourceGroup sourceGroup : sourceGroups) {
                result = sourceGroup.getRootFolder();
                break;

            }
        } catch (UnsupportedOperationException ex) {
            LOG.log(Level.FINE, ex.getMessage()); //NOI18N
        }
        return result;
    }

    protected void setMainClass(FileObject projDir) {
        FileObject fo = projDir.getFileObject("nbproject/project.properties");
        EditableProperties props = BaseUtils.loadEditableProperties(fo);
        props.setProperty("main.class", "org.embedded.server.TomcatEmbeddedServer");
        BaseUtils.storeEditableProperties(props, fo);
    }
    @Override
    public WizardDescriptorPanel getAddonCreateProjectPanel(org.openide.WizardDescriptor wiz) {
        return null;
    }

*/
    @Override
    public boolean needsShutdownPort() {
        return true;
    }


    @Override
    public int getDefaultPort() {
        return 9190;
    }

    @Override
    public int getDefaultDebugPort() {
        return 9195;
    }

    @Override
    public int getDefaultShutdownPort() {
        return 9191;
    }

    public void redeployCommand(BaseDeploymentManager dm, String oldContextPath, String oldWebApplication, String newContextPath, String newWebApplication) {
        HttpURLConnection connection = null;

        try {
            BaseUtil.out("DEPLOY COMMAND contetPath = " + oldContextPath + "; newContetPath = " + newContextPath);

            String encContextPath = oldContextPath;
            if (oldContextPath == null || oldContextPath.trim().isEmpty()) {
                encContextPath = "/";
            }
            String l = URLEncoder.encode(oldWebApplication, "UTF-8");
            String c = URLEncoder.encode(encContextPath, "UTF-8");

            encContextPath = newContextPath;
            if (newContextPath == null || newContextPath.trim().isEmpty()) {
                encContextPath = "/";
            }
            String ln = URLEncoder.encode(newWebApplication, "UTF-8");
            String cn = URLEncoder.encode(encContextPath, "UTF-8");
            //String urlstr = "/jeeserver/manager?deploy=" + l + "&cp=" + c;
            String urlstr = "/jeeserver/manager?cmd=redeploy&olddir=" + l + "&oldcp=" + c
                    + "&dir=" + ln + "&cp=" + cn;

            URL url = new URL(buildUrl(dm) + urlstr);

            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.getResponseCode();
        } catch (SocketException e) {
        } catch (IOException e) {
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

    }

    @Override
    public boolean supportsDistributeAs(SuiteConstants.DistributeAs distributeAs) {
        boolean result = true;
        switch (distributeAs) {
            case SINGLE_JAR_UNPACKED_WARS:
                result = false;
        }
        return result;
    }
    
    @Override
    public String[] getSupportedContextPaths() {
        return new String[] {"META-INF/context.xml"};
    }
    @Override
    public Properties getContextPoperties(FileObject config) {
        return TomcatModuleConfiguration.getContextProperties(config);
    }            

    @Override
    public SupportedApiProvider getSupportedApiProvider() {
        return null;
    }

}
