/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.plugin.support.embedded.jetty;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Valery
 */
public class WebAppInfo {

    private boolean jarFile;
    private File file;

    public WebAppInfo(URL resourceURL) throws IOException {

        final JarURLConnection connection;

        if ("jar".equals(resourceURL.getProtocol())) {
            jarFile = true;
        }
        connection = (JarURLConnection) resourceURL.openConnection();
        URL url = connection.getJarFileURL();
        file = new File(url.getFile());
    }

    public WebAppInfo(File dirOrJar) {
        jarFile = false;
        if (dirOrJar.isFile() && dirOrJar.getName().endsWith(".jar")) {
            jarFile = true;
        }
        file = dirOrJar;

    }

    public boolean isJarFile() {
        return jarFile;
    }

    public File getFile() {
        return file;
    }

    public Set<Info> buildInfoSet(String forPath) {

        Set<Info> set = new HashSet<>();

        if (!file.exists()) {
            return set;
        }

        if (isJarFile()) {
            URI uri = URI.create("jar:file:" + file.toPath().toUri().getPath());
            return buildInfoSet(uri, forPath);
        }

        FileSystem fs = FileSystems.getDefault();
        try {
            set = buildInfoSet(fs, forPath);
        } catch (IOException ex) {
            Logger.getLogger(WebAppInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
        return set;

    }

    public Set<Info> buildInfoSet(URI uri, String forPath) {
        Set<Info> set = new HashSet<>();
        Map<String, String> env = new HashMap<>();
        env.put("create", "false");

        try (FileSystem fs = FileSystems.newFileSystem(uri, env)) {
            set = buildInfoSet(fs, forPath);
        } catch (IOException ex) {
            Logger.getLogger(WebAppInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
        return set;

    }

    protected Set<Info> buildInfoSet(FileSystem fs, String forPath) throws IOException {
        Set<Info> set = new HashSet<>();
        Path dirPath = fs.getPath(forPath);

        if (!Files.exists(dirPath)) {
            return set;
        }
        DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath);
        Info info;// = null;
        String webAppName;// = null;
        for (Path path : stream) {

            String contextPath = null;
            info = getInfo(path);
            if (info != null) {
                webAppName = info.getWebAppName();
                if (info.isWarFile()) {
                    Path warPath = fs.getPath(path.toString());
                    File warFile = warPath.toFile();                    
                    contextPath = getContextPath(warFile, webAppName);
                }

                if (contextPath == null) {
                    contextPath = "/" + webAppName;
                }
                info.setContextPath(contextPath);
                set.add(info);
                /*                System.out.println("--------------- Info buildInfoSet -----------------");
                 System.out.println("--------------- webAppName = " + info.getWebAppName());
                 System.out.println("--------------- webAppName = " + info.getContextPath());
                 System.out.println("--------------- warPath = " + info.getWarPath());
                 System.out.println("=====================================");
                 */
            }

        }//for
        return set;
    }

    protected Info getInfo(Path path) throws IOException {
        String webAppName = path.getFileName().toString().replace("/", "");
        Info info = null;

        if (!Files.isDirectory(path) && webAppName.endsWith(".war")
                && isJarFile()) {
            info = getInfoByWarInZip(path);
        } else if (!Files.isDirectory(path) && webAppName.endsWith(".war")) {
            info = getInfoByWarInFolder(path);
        } else if (Files.isDirectory(path)) {
            info = getInfoByFolder(path);
        }
        return info;
    }

    protected String getContextPath(FileSystem fs, Path webDir) {
        String cp;// = null;
        System.out.println("webDir=" + webDir);
        Path jettyxml = fs.getPath(webDir.toString(), "WEB-INF/jetty-web.xml");
        if (!Files.exists(jettyxml)) {
            System.out.println("no jetty-web.xml");
            jettyxml = fs.getPath(webDir.toString(), "WEB-INF/web-jetty.xml");
        }
        Properties props = new Properties();
        try (InputStream is = Copier.ZipUtil.getZipEntryInputStream(fs, jettyxml.toString())) {
            props = Utils.getContextProperties(is);
        } catch (IOException ex) {
            Logger.getLogger(WebAppInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
        cp = props.getProperty("contextPath");
        /*        System.out.println("-------- GETCONTEXT PATH ---------------");
         System.out.println("webDir=" + webDir);        
         System.out.println("Jetty-web.xml=" + jettyxml);
         System.out.println("Jetty-web.xml content=" + Copier.ZipUtil.getZipEntryAsString(fs, jettyxml.toString()));
         System.out.println("Jetty-web.xml contextPath=" + props.getProperty("contextPath"));        
         System.out.println("----------------------------------------");        
         */
        return cp;
    }

    protected String getContextPath(File warFile, String webappName) {
        String cp = "/" + webappName;// = null;
    
        String jettyweb = Copier.ZipUtil.getZipEntryAsString(warFile, "WEB-INF/jetty-web.xml");
        if (jettyweb == null) {
            jettyweb = Copier.ZipUtil.getZipEntryAsString(warFile, "WEB-INF/web-jetty.xml");
        }
        Properties props = new Properties();
        if (jettyweb != null) {
            try (InputStream is = new ByteArrayInputStream(jettyweb.getBytes())) {
                props = Utils.getContextProperties(is);
            } catch (IOException ex) {
                Logger.getLogger(WebAppInfo.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            jettyweb = Copier.ZipUtil.getZipEntryAsString(warFile, "META-INF/context.properties");
            if (jettyweb != null) {
                try (InputStream is = new ByteArrayInputStream(jettyweb.getBytes())) {
                    props.load(is);
                } catch (IOException ex) {
                    Logger.getLogger(WebAppInfo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        if ( props.getProperty(Utils.CONTEXTPATH_PROP) != null  ) {
           cp = props.getProperty(Utils.CONTEXTPATH_PROP);
        }
        return cp;
    }

    protected Info getInfoByWarInZip(Path warPath) {
        return null;
    }

    protected Info getInfoByWarInFolder(Path war) throws IOException {
        String s = war.getFileName().toString().replace("/", "");
        String webAppName = s
                .substring(0, s.length() - 4);

        String warPath = Paths.get(file.getCanonicalPath(), war.toString()).toString();
        Info info = new Info(webAppName, null, warPath, true);

        return info;

    }

    protected Info getInfoByFolder(Path folderPath) throws MalformedURLException, IOException {

        String webAppName = folderPath.getFileName().toString().replace("/", "");
        String warPath;

        if (isJarFile()) {
            warPath = folderPath.toUri().toURL().toExternalForm();
        } else {
            warPath = Paths.get(file.getCanonicalPath(), folderPath.toString()).toString();
        }
        Info info = new Info(webAppName, null, warPath, false);
        return info;

    }

    public static class Info {

        private String webAppName;
        private String contextPath;
        private String warPath;
        private boolean warFile;

        public Info() {
        }

        public Info(String webAppName, String contextPath, String warPath, boolean warFile) {
            this.webAppName = webAppName;
            this.contextPath = contextPath;
            this.warPath = warPath;
            this.warFile = warFile;
        }

        public String getWebAppName() {
            return webAppName;
        }

        public void setWebAppName(String webAppName) {
            this.webAppName = webAppName;
        }

        public String getContextPath() {
            return contextPath;
        }

        public void setContextPath(String contextPath) {
            this.contextPath = contextPath;
        }

        public String getWarPath() {
            return warPath;
        }

        public void setWarPath(String warPath) {
            this.warPath = warPath;
        }

        public boolean isWarFile() {
            return warFile;
        }

        public void setWarFile(boolean warFile) {
            this.warFile = warFile;
        }

    }
}
