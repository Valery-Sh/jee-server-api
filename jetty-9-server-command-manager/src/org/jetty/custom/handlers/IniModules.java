package org.jetty.custom.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 *
 * @author V. Shyshkin
 */
public class IniModules {

    private static final Logger LOG = Logger.getLogger(IniModules.class.getName());

    protected IniModules() {

    }

    public static boolean isCDIEnabled() {
        boolean yes = false;
        for ( String m : getEnabledModules()) {
            if ( m.toLowerCase().equals("cdi")) {
                yes = true;
                break;
            }
        }
        return yes;

    }
    public static boolean isJSFEnabled() {
        boolean yes = false;
        for ( String m : getEnabledModules()) {
            if ( m.toLowerCase().startsWith("jsf-")) {
                yes = true;
                break;
            }
        }
        return yes;
        
    }
            
    public static List<String> getEnabledModules() {
        //
        // First get  all modules from start.ini in the baseDir
        //
        File iniFile = Paths.get(System.getProperty(Utils.JETTY_BASE), "start.ini").toFile();
        IniHelper ini = new IniHelper(iniFile);

        final List<String> modules = ini.getEnabledModules();

        Path startDPath = Paths.get(System.getProperty(Utils.JETTY_BASE), "start.d");

        try {
            Stream<Path> stream = Files.list(startDPath);
            stream.forEach((p) -> {
                if (p.getFileName().toString().endsWith(".ini")) {
                    IniHelper dini = new IniHelper(p.toFile());
                    List<String> list = dini.getEnabledModules();

                    list.forEach(mod -> {
                        if (!modules.contains(mod)) {
                            modules.add(mod);
                        }
                    });
                }
            });
        } catch (IOException ex) {
            LOG.log(Level.INFO, ex.getMessage());
        }
        return modules;
    }

    public static String getJsfListenerClassName() {
        List<String> enabled = getEnabledModules();
        Map<String, String> jsfs = getSupportedJsfModules();
        String config = null;
        for (String s : enabled) {
            if (jsfs.get(s) != null) {
                config = jsfs.get(s);
                break;
            }
        }
        return config;
    }

    public static  Map<String, String> getSupportedJsfModules() {
        Map<String, String> m = new HashMap<>();
        m.put("jsf-myfaces", "org.apache.myfaces.webapp.StartupServletContextListener");
        m.put("jsf-mojarra", "com.sun.faces.config.ConfigureListener");
        m.put("jsf-netbeans", "com.sun.faces.config.ConfigureListener");
        return m;
    }

    public static class IniHelper {


        private File file;
        private List<String> lines = new ArrayList<>();
        private Path baseDir;

        protected IniHelper() {

        }

        protected IniHelper(File file) {
            this.file = file;
            init();
        }

        private void init() {
            baseDir = Paths.get(System.getProperty(Utils.JETTY_BASE));

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
                s = s.replace("${start.basedir}", baseDir.toString());
                if (!lines.contains(s)) {
                    lines.add(s);
                }
            }
        }
    }

}//class
