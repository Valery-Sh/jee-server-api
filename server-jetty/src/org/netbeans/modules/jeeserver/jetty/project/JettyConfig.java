/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jeeserver.jetty.project;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.deployment.lc.LicenseWizard;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.netbeans.modules.jeeserver.jetty.util.JettyConstants;
import org.netbeans.modules.jeeserver.jetty.util.StartIni;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author V.Shyshkin
 */
public class JettyConfig {

    private static final Logger LOG = Logger.getLogger(JettyConfig.class.getName());

    private final static Map<Project, JettyConfig> configs = new ConcurrentHashMap<>();

    private final Project server;

    private JettyLibBuilder libBuilder;

    private Path jettyBase;
    private Path startd;

    protected JettyConfig(Project server) {
        this.server = server;
        init();
    }

    private void init() {
        jettyBase = Paths.get(server.getProjectDirectory().getPath(), JettyConstants.JETTYBASE_FOLDER);
        startd = Paths.get(jettyBase.toString(), "start.d");
    }

    public static synchronized JettyConfig getInstance(Project server) {
        JettyConfig config = configs.get(server);

        if (config == null) {
            config = new JettyConfig(server);
            config.libBuilder = new JettyLibBuilder(BaseUtils.managerOf(server.getLookup()));
            configs.put(server, config);
        }

        return config;
    }

    public JettyLibBuilder getLibBuilder() {
        return libBuilder;
    }

    public Properties getSupportedJSFListeners() {
        Path ini = Paths.get(jettyBase.toString(), "start.d/jsf-listeners.ini");
        File iniFile = ini.toFile();
        List<String> lines = new ArrayList<>();
        final Properties props = new Properties();
        try (FileReader reader = new FileReader(iniFile); BufferedReader buf = new BufferedReader(reader)) {
            String line;

            while ((line = buf.readLine()) != null) {

                if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                    continue;
                }

                line = line.trim().replaceAll(" ", "");
                /* if (line.startsWith("--module=")) {
                 separateModules(line, lines);
                 } else {
                 lines.add(line);
                 }
                 */
            }

            lines.forEach((String ln) -> {
                String[] pair = ln.split("=");
                if (pair.length == 2) {
                    props.setProperty(pair[0], pair[1]);
                }
            });

        } catch (IOException ex) {
            LOG.log(Level.INFO, ex.getMessage());
        }
        return props;
    }

    public String getJSFModuleName() {
        String name = null;
        for (String m : getModuleNames()) {
            if (m.toLowerCase().startsWith("jsf-")) {
                name = m;
                break;
            }
        }
        return name;
    }

    public String getJsfListener() {
        final String[] l = new String[]{null};
        if (Files.exists(startd) && Files.isDirectory(startd)) {
            Path ini = Paths.get(startd.toString(), "jsf-listeners.ini");
            Properties props = getProperties(ini);
            final List<String> modules = getModuleNames();
            props.forEach((name, value) -> {
                if (modules.contains(name)) {
                    l[0] = (String) value;
                }
            });
        }
        return l[0];
    }

    public List<String> getModuleNames() {
        /*        List<String> mns = libBuilder.getModuleNames();
         if (mns != null) {
         mns.forEach((m) -> {
         BaseUtils.out("JettyConfig getModuleNames name=" + m);
         });
         } else {
         BaseUtils.out("JettyConfig getModuleNames = NULL !!!!!!!!!");
         }
         */
        return libBuilder.getModuleNames();
    }

    public Properties getProperties(Path ini) {
        File iniFile = ini.toFile();
        List<String> lines = new ArrayList<>();
        final Properties props = new Properties();
        try (FileReader reader = new FileReader(iniFile); BufferedReader buf = new BufferedReader(reader)) {
            String line;

            while ((line = buf.readLine()) != null) {

                if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                    continue;
                }

                line = line.trim().replaceAll(" ", "");
                if (line.startsWith("--module=")) {
                    separateModules(line, lines);
                } else {
                    lines.add(line);
                }
            }
            lines.forEach((String ln) -> {
                String[] pair = ln.split("=");
                if (pair.length == 2) {
                    props.setProperty(pair[0], pair[1]);
                }
            });

        } catch (IOException ex) {
            LOG.log(Level.INFO, ex.getMessage());
        }
        /*        if (props != null) {
         props.forEach((n, v) -> {
         BaseUtils.out("JettyConfig getProperties name=" + n + "; valu)e=" + v);
         });
         } else {
         BaseUtils.out("JettyConfig getProperties = NULL !!!!!!!!!");
         }
         */
        return props;
    }

    private void separateModules(String line, List<String> lines) {
        int idx = line.indexOf('=');
        String value = line.substring(idx + 1);
//BaseUtils.out("1) %%%%%%%%%%%%%%%%%%%%%% SEPARATE = " + line + "; v=" + value);        
        for (String part : value.split(",")) {
            String s = "--module=" + part;
            s = s.replace("${start.basedir}", jettyBase.toString());
            if (!lines.contains(s)) {
//BaseUtils.out("2) %%%%%%%%%%%%%%%%%%%%%% SEPARATE = " + s);
                lines.add(s);
            }
        }
    }

    public static class IniHelper {

        private File file;
        private List<String> lines = new ArrayList<>();
        private Path jettyBase;


        protected IniHelper(File file, Path jettyBase) {
            this.file = file;
            this.jettyBase = jettyBase;
            init();
        }

        private void init() {

            try (FileReader reader = new FileReader(file)) {
                try (BufferedReader buf = new BufferedReader(reader)) {
                    String line;
                    while ((line = buf.readLine()) != null) {
                        if (line.length() != 0 && line.charAt(0) != '#') {
                            String s = line.trim().replaceAll(" ", "");
                            if (s.startsWith("--module=")) {
                                separateModules(s);
                            }
                        }
                    }
                } catch (IOException ex) {
                    LOG.log(Level.INFO, ex.getMessage());
                }
            } catch (FileNotFoundException ex) {
                LOG.log(Level.INFO, ex.getMessage());
            } catch (IOException ex) {
                LOG.log(Level.INFO, ex.getMessage());
            }

        }

        public void setFile(File file) {
            this.file = file;
            init();
        }

        public void commentLine(int idx) {
            if (lines.isEmpty() || idx >= lines.size()) {
                return;
            }
            lines.set(idx, "#" + lines.get(idx));
        }

        public List<String> lines() {
            return lines;
        }
        /**
         * 
         * @return a list of module names without extensions.
         */
        public List<String> getEnabledModules() {
            List<String> list = new ArrayList<>();
            for (int i = 0; i < lines().size(); i++) {
                String ln = lines().get(i);
                if (ln.startsWith("--module=")) {
                    list.add(ln.substring("--module=".length()));
                }
            }
            return list;
        }

        public File getFile() {
            return file;
        }

        private void separateModules(String line) {
            int idx = line.indexOf('=');
            String value = line.substring(idx + 1);
            for (String part : value.split(",")) {
                String s = "--module=" + part;
                s = s.replace("${start.basedir}", jettyBase.toString());
                if (!lines.contains(s)) {
                    lines.add(s);
                }
            }
        }
    }//class
    public static class CDISupport {
        private File jettyBase; 
        private Project server;
        
        public CDISupport(Project server) {
            this.server = server;
            init();
        }
        
        private void init() {
            Path p = Paths.get(server.getProjectDirectory().getPath(), JettyConstants.JETTYBASE_FOLDER);
            this.jettyBase = p.toFile();
        }
        
        protected void disableCDIModule() {
            //Path p = Paths.get(jettyBase.getPath(),"start.ini");
            StartIni ini = new StartIni(server, true);
            ini.commentModule("cdi");
            ini.save();
        }
        
        public boolean isCDIEnabled() {
            return JettyConfig.getInstance(server).getModuleNames().contains("cdi");
        }
        
        public boolean isCDILibExists() {
            return Paths.get(jettyBase.getPath(), "lib/cdi").toFile().exists();
        }
        public boolean isLicenseAccepted() {
            boolean enabled = isCDIEnabled();
            boolean libExists = isCDILibExists();
            return enabled && libExists || ! enabled;
        }
        public static boolean isLicenseAccepted(Project server) {
            return new CDISupport(server).isLicenseAccepted();
        }
        
        public static boolean showLicenseDialog(Project server) {
            CDISupport cdi = new CDISupport(server);
            return cdi.showLicenseDialog();
        }
        
        public boolean showLicenseDialog() {
            
            if ( isLicenseAccepted() ) {
                return true;
            }
            LicenseWizard d = new LicenseWizard();
            if ( ! d.showLicenceDialog() ) {
                disableCDIModule();
            }
            return false;
        }
        
    }

}//class
