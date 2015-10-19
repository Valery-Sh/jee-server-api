
package org.netbeans.plugin.support.embedded.jetty;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 *
 * @author V. Shyshkin
 */
public class DevModePathResolver {
    
    public static String TMP_DIST_WEB_APPS = "embedded_suite_server_instance";
    public static String PROPS_FILE_NAME = "server-instance-web-apps.properties";    
    
    private final WebAppContext ctx;
    
    
    public DevModePathResolver(WebAppContext ctx) {
        this.ctx = ctx;
    }
    public static DevModePathResolver getInstance(WebAppContext ctx) {
        DevModePathResolver r = new DevModePathResolver(ctx);
        return r;
    }
    public String getPath() {
        if ( isResolved() ) {
            return ctx.getWar();
        }
        String result = ctx.getWar();
        Properties props = getWebAppsProperties();
        String cp = ctx.getContextPath();
        String webDir = props.getProperty(cp);
        if ( webDir != null ) {
            result = Utils.getWarPath(webDir);
        }
        return result;
    }
    
    protected boolean isResolved() {
        String path = ctx.getWar();
        if ( ctx.getContextPath() == null) {
            return true;
        }
        
        return path != null && new File(path).exists();
    }

    protected Properties getWebAppsProperties() {
        Properties props = new Properties();
        Path target = getTmpWebAppsDir();
        
        if (target == null || ! Files.exists(target) ) {
            return props;
        }
        Path propsPath = Paths.get(target.toString(), PROPS_FILE_NAME);
        if ( ! Files.exists(propsPath) ){
            return props;
        }
        props = loadProperties(propsPath);
        
        return props;
    }
    
    private Path getTmpWebAppsDir() {
        Path serverDir = Paths.get(System.getProperty("user.dir"));
        String root = serverDir.getRoot().toString().replaceAll(":", "_");
        if (root.startsWith("/")) {
            root = root.substring(1);
        }
        Path targetPath = serverDir.getRoot().relativize(serverDir);
        String tmp = System.getProperty("java.io.tmpdir");

        Path target = Paths.get(tmp, TMP_DIST_WEB_APPS, root, targetPath.toString());
        return target;
    }
    
    public static Properties loadProperties(Path path) {
        File f = path.toFile();
        
        final Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream(f)) {
            props.load(fis);
        } catch (IOException ioe) {
            System.out.println("EXCEPTION");
        }
        return props;

    }
    
}
