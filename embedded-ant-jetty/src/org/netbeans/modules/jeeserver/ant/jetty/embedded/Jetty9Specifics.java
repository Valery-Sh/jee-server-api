/**
 * This file is part of Jetty Server Embedded support in NetBeans IDE.
 *
 * Jetty Server Embedded support in NetBeans IDE is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * Jetty Server Embedded support in NetBeans IDE is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should see the GNU General Public License here:
 * <http://www.gnu.org/licenses/>.
 */
package org.netbeans.modules.jeeserver.ant.jetty.embedded;

import java.awt.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.jeeserver.base.deployment.specifics.WizardDescriptorPanel;
import org.netbeans.modules.jeeserver.base.deployment.ServerInstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.spi.FindJSPServlet;
import org.netbeans.modules.jeeserver.ant.base.embedded.specifics.EmbeddedServerSpecifics;
import org.netbeans.modules.jeeserver.ant.base.embedded.utils.EmbConstants;
import org.netbeans.modules.jeeserver.ant.base.embedded.utils.EmbConstants.DistributeAs;
//import static org.netbeans.modules.jeeserver.ant.base.embedded.utils.EmbConstants.DistributeAs.SINGLE_JAR_WARS;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
//import org.netbeans.modules.jeeserver.ant.base.embedded.utils.EmbConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.openide.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.EditableProperties;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;

/**
 *
 * @author V. Shyshkin
 */
public class Jetty9Specifics implements EmbeddedServerSpecifics {

    private static final Logger LOG = Logger.getLogger(Jetty9Specifics.class.getName());

    @StaticResource
    public static final String IMAGE = "org/netbeans/modules/jeeserver/ant/jetty/embedded/resources/server.png";

    //private static final String HELPER_JAR = "nb-jetty-helper.jar";
    public static final String JETTY_SHUTDOWN_KEY = "netbeans";

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

        boolean result = false;

        ServerInstanceProperties sp = BaseUtils.getServerProperties(serverProject);

        String key = JETTY_SHUTDOWN_KEY;
        //for future String pkey = sp.getServerConfigProperties().getProperty("jetty-shutdown-key");
        String pkey = null;
        if (pkey != null) {
            key = pkey;
        }

        HttpURLConnection connection = null;

        try {
            URL url = new URL(sp.getManager().buildUrl() + "/shutdown?token=" + key);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            int code = connection.getResponseCode();
            LOG.log(Level.FINE, "Server Internal Shutdown response code is " + code); //NOI18N
            if (code == 404) {
                result = true;
            }
        } catch (SocketException e) {
            LOG.log(Level.FINE, "The server is not running (SocketException)"); //NOI18N
        } catch (IOException e) {
            LOG.log(Level.FINE, "The server is not running (IOException)"); //NOI18N
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        //
        // We don't know for sure whether the embedded server app supports
        // shutdown handler. So, let try ping.
        //
        long pingtimeout = System.currentTimeMillis() + EmbConstants.SERVER_TIMEOUT_DELAY;
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
            //String urlstr = "/jeeserver/manager?deploy=" + l + "&cp=" + c;
            String urlstr = "/jeeserver/manager?" + cmd;
            BaseUtils.out("Jetty9Specifics: deployCommand urlStr=" + urlstr);
            URL url = new URL(buildUrl(serverProject) + urlstr);

            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            BaseUtils.out("JETTY: RESPONCE CODE = " + connection.getResponseCode());
            if (connection.getResponseCode() == 200) {
                result = getResponseData(connection, cmd);
            }

        } catch (SocketException e) {
            System.out.println("Exception " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Exception " + e.getMessage());
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
        return sb.toString().trim();
    }

    private String buildUrl(Project p) {

        return BaseUtils.managerOf(p).buildUrl();
        //return "http://" + sp.getHost() + ":" + sp.getHttpPort();
    }

    @Override
    public FindJSPServlet getFindJSPServlet(DeploymentManager dm) {
        return new JettyFindJspServlet((BaseDeploymentManager) dm);
    }

    @Override
    public Image getProjectImage(Project serverProject) {
        return ImageUtilities.loadImage(IMAGE);
    }

    @Override
    public void projectCreated(FileObject projectDir, Map<String, Object> props) {
        //
        // Check whether helper lib contains jar file with a name 
        // "nb-jetty-helper.jar". If true then we assume that it is a 
        // jar provided by the plugin.
        //
/*        Library nblib = LibraryManager.getDefault().getLibrary("jetty9-" + EmbConstants.SERVER_HELPER_LIBRARY_POSTFIX);
         if (nblib == null) {
         return;
         }
         List<URL> urls = nblib.getContent("classpath");
         if (urls.size() > 1) {
         // My lib must contain a single jar
         return;
         }

         URL u = urls.get(0);

         String nm = FileUtil.archiveOrDirForURL(urls.get(0)).getName();

         if (! HELPER_JAR.equals(nm)) {
         return;
         }
         */

        //
        // Add command-manager.jar to the classpath of the project
        //
/*        FileObject libExt = projectDir.getFileObject("lib/ext");
        FileObject cmFo;// = null;
        try {
            cmFo = libExt.createData("command-manager", "jar");
            try (OutputStream os = cmFo.getOutputStream(); InputStream is = getClass().getClassLoader().getResourceAsStream("/org/netbeans/modules/jeeserver/jetty/embedded/resources/command-manager.jar")) {
                FileUtil.copy(is, os);
            }
            this.addJarToServerClassPath(FileUtil.toFile(cmFo), projectDir);
        } catch (IOException ex) {
            LOG.log(Level.INFO, ex.getMessage()); //NOI18N
        }
*/
        String actualServerId = (String)props.get(EmbConstants.SERVER_ACTUAL_ID_PROP);
        String cmOut = actualServerId + "-command-manager";
        String cmIn = "/org/netbeans/modules/jeeserver/jetty/embedded/resources/" + actualServerId + "-command-manager.jar";
        
                
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
                    FileUtil.getConfigFile("Templates/jetty9/JettyEmbeddedServer"));
            templateParams.put("port", props.get(EmbConstants.HTTP_PORT_PROP));
            templateParams.put("comStart", "");
            templateParams.put("comEnd", "");

            template.createFromTemplate(
                    outputFolder,
                    "JettyEmbeddedServer.java",
                    templateParams);
            setMainClass(projectDir);
        } catch (IOException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
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
        props.setProperty("main.class", "org.embedded.server.JettyEmbeddedServer");
        BaseUtils.storeEditableProperties(props, fo);
    }

    @Override
    public boolean needsShutdownPort() {
        return false;
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
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isEmbedded() {
        return true;
    }
    @Override
    public Properties getContextPoperties(FileObject config) {
        return JettyModuleConfiguration.getContextProperties(config);
    }            

    @Override
    public boolean supportsDistributeAs(org.netbeans.modules.jeeserver.ant.base.embedded.utils.EmbConstants.DistributeAs distributeAs) {
        boolean result = true;
        switch (distributeAs) {
            case SINGLE_JAR_WARS:
                result = false;

        }
        return result;

    }

}
