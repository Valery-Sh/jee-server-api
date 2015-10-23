package org.embedded.ide.tomcat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author V. Shyshkin
 */
public class PathResolver {

    private final String unpackedTempDir;

    public PathResolver(String unpackedTempDir) {
        this.unpackedTempDir = unpackedTempDir;
    }

    public String pathFor(String buildPath, boolean isDevelopmentMode) {

        String result;
        if (isDevelopmentMode) {
            result = dtPathFor(buildPath);
        } else {
            result = rtPathFor(buildPath);
        }

        return result;
    }

    protected void warning(String msg) {
        System.err.println("______________________________________________________");
        System.err.println("   WARNING: CommandManager (PathResolver): " + msg);
        System.err.println("______________________________________________________");
    }

    /**
     * The method calculates the full path to {@literal build/web} or to 
     * {@literal target/<buid-dir> }.
     *
     * @param warPath the path as it found in the {@literal WebAppContext.getWar()
     * }
     * @return an actual path 
     */
    protected String dtPathFor(String warPath) {

        String p = warPath.trim().replace("\\", "/");

        String projDir;
        String buildDir;

        String ref;

        if (p.startsWith("${")) {

            int idx = p.lastIndexOf("}");
            if (idx < 0) {
                warning("The right curly brace is omitted '" + p + "'");
                return warPath;
            }
            // Calc the project dir or .war file
            ref = p.substring(2, idx);
        } else {

            int idx = p.lastIndexOf("/");
            if (idx != p.indexOf("/")) {
                warning("Too many path elements in '" + p + "'");
                return warPath; // original value
            }
            ref = idx > 0 ? p.substring(0, idx) : p;
        }
        // tries to find a file or a folder in the Web Applications
        // folder. It may be an internal web project or a properties file
        // with .webfef or .warref or .htmref extenssion 
        File refFile = getRef(ref);

        if (refFile == null || !refFile.exists()) {
            warning("No project dir found. '" + ref + "' is not registered");
            return warPath; // original value
        }

        projDir = getProjectDirByRef(refFile);
        if (projDir == null) {
            warning("No project dir found. '" + ref + "' is not registered");
            return warPath;
        }

        File f = new File(projDir);
        if (f.isFile() && f.getName().endsWith(".war")) {
            // war file => this is a result
            return projDir;
        } else if (f.isFile()) {
            return warPath;
        }

        if (Utils.isMavenProject(projDir)) {
            buildDir = Utils.getMavenBuildDir(projDir);
        } else if (refFile.getPath().endsWith(".htmref")) {
            Properties props = Utils.loadHtml5ProjectProperties(projDir);
            String v = Utils.resolve("f1", props);
            System.out.println("resolved: = " + v);

            String siteRoot = Utils.resolve(Utils.HTML5_SITE_ROOT_PROP, props);
            if (siteRoot == null) {
                siteRoot = Utils.HTML5_DEFAULT_SITE_ROOT_PROP;
            }
            buildDir = projDir + "/" + siteRoot;
        } else {
            buildDir = projDir + "/build/web";
        }

        return buildDir != null ? buildDir : warPath;

    }

    public String getProjectDirByRef(String ref) {
        String result = null;
        File[] list = new File("./" + Utils.WEBAPPLICATIONS_FOLDER).listFiles();
        File resultFile = null;
        // search when exactly equal
        for (File f : list) {
            if (f.getName().equals(ref)) {
                resultFile = f;
                break;
            }
        }
        if (resultFile == null && !ref.endsWith(Utils.WEB_REF) && !ref.endsWith(Utils.WAR_REF)
                && !ref.endsWith(Utils.HTM_REF)) {
            for (File f : list) {
                if (f.getName().equals(ref + Utils.WEB_REF)
                        || f.getName().equals(ref + Utils.WAR_REF)
                        || f.getName().equals(ref + Utils.HTM_REF)) {
                    resultFile = f;
                    break;
                }
            }

        }
        if (resultFile != null) {
            Properties props = new Properties();
            try (FileInputStream in = new FileInputStream(resultFile)) {
                props.load(in);
                result = props.getProperty("webAppLocation");
            } catch (FileNotFoundException ex) {
                Logger.getLogger(PathResolver.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(PathResolver.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result;
    }

    protected String getProjectDirByRef(File ref) {
        String result = null;
        if (ref != null && ref.isDirectory()) {
            return ref.getAbsolutePath();
        }
        if (ref != null) {
            Properties props = new Properties();
            try (FileInputStream in = new FileInputStream(ref)) {
                props.load(in);
                result = props.getProperty("webAppLocation");
            } catch (FileNotFoundException ex) {
                Logger.getLogger(PathResolver.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(PathResolver.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result;
    }

    /**
     * Tries to find a file or a folder in the {@literal Web Applications}
     * folder by it's name.
     *
     * @param ref may be a simple name or a name with extention
     * {@literal .warref} or {@literal .webref} or {@literal htmref}
     * @return the {@literal java.io.File} object or {@literal null}
     */
    protected File getRef(String ref) {
        File result = null;
        File[] list = new File("./" + Utils.WEBAPPLICATIONS_FOLDER).listFiles();
        File resultFile = null;
        // search when exactly equal
        for (File f : list) {
            if (f.getName().equals(ref)) {
                resultFile = f;
                break;
            }
        }
        if (resultFile == null && !ref.endsWith(Utils.WEB_REF) && !ref.endsWith(Utils.WAR_REF)
                && !ref.endsWith(Utils.HTM_REF)) {
            for (File f : list) {
                if (f.getName().equals(ref + Utils.WEB_REF)
                        || f.getName().equals(ref + Utils.WAR_REF)
                        || f.getName().equals(ref + Utils.HTM_REF)) {
                    result = f;
                    break;
                }
            }
        }
        return result;
    }

    public String getOwnerProjectDir(File warFile) {
        String result = null;
        if (warFile.exists() && warFile.getParentFile().exists() && warFile.getParentFile().getParentFile().exists()) {
            result = warFile.getParentFile().getParentFile().getAbsolutePath();
        }
        return result;
    }

    /**
     * In production mode we must return a simple name of the war file without
     * extention.
     *
     * @param warPath the path as it is in the {@literal WebAppContext.getWar()
     * }
     * @return an actual path
     */
    protected String rtPathFor(String warPath) {
        String p = warPath.trim();
        String warName; // = null;
        String remPath = "";
        String ref;

        if (p.startsWith("${")) {
            int idx = p.lastIndexOf("}");
            // We consider that the content in curly braces contains, for example,
            // WebApp or WebApp.webref or WebApp01.warref
            // 
            ref = p.substring(2, idx);
            if (idx < p.length() - 1) {
                remPath = p.substring(idx + 2);
            }
        } else {
            int idx = p.lastIndexOf("/");
            if (idx != p.indexOf("/")) {
                return warPath; // original value
            }
            ref = idx > 0 ? p.substring(0, idx) : p;
            if (idx < p.length() - 1) {
                remPath = p.substring(idx + 1);
            }
        }

        if (!remPath.isEmpty() && !remPath.equals("/")) {

            warName = remPath.substring(0); // may be warning
            if (warName.endsWith(Utils.WEB_REF) || warName.endsWith(Utils.WAR_REF)) {
                warName = warName.substring(0, warName.length() - 7);
            }
        } else {
            warName = ref.endsWith(Utils.WEB_REF) || ref.endsWith(Utils.WAR_REF)
                    ? ref.substring(0, ref.length() - 7) : ref;
        }
        if (warName != null && warName.endsWith(".war")) {
            warName = warName.substring(0, warName.length() - 4);
        }
        System.out.println("PathResolver rtPathFor warName="+warName);
        if (warName != null) {
            warName = rtPathFor2(warName);
        } else {
            warName = warPath; // original value
        }
        return warName;
    }

    protected String rtPathFor2(String warName) {
        File war;
        // May be in .jar ?
        // URL url = this.getClass().getClassLoader().getResource("web-apps-pack/" + webappName + buildName);
        String warNameExt = warName + ".war";
        URL url = PathResolver.class.getClassLoader().getResource("web-apps-pack/" + warNameExt);
        if (url != null && unpackedTempDir != null) {
            //In a jar archive
            //9.12war = new File(unpackedTempDir + "/" + warNameExt);
            war = new File(unpackedTempDir + "/" + warName);
        } else {
            String webappsDir = Utils.WEBAPPS_DEFAULT_DIR_NAME;
            Properties serverProps = Utils.loadServerProperties(CommandManager.isDevelopmentMode());
            if (serverProps != null && serverProps.getProperty(Utils.WEBAPPS_DIR_PROP) != null) {
                webappsDir = serverProps.getProperty(Utils.WEBAPPS_DIR_PROP);
            }

            war = new File("./" + webappsDir + "/" + warName);
        }

        String path = null;

        if (war.exists()) {
            path = war.getAbsolutePath();
        } else {
            File f = new File(war.getAbsolutePath() + ".war");

            if (f.exists()) {
                path = f.getAbsolutePath();
            }
        }
        System.out.println("PathResolver rtPathFor2=" + path);        
        return path;
    }

}
