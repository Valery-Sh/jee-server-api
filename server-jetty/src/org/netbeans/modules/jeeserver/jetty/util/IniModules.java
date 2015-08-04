/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jeeserver.jetty.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Valery
 */
public class IniModules {

    private static final Logger LOG = Logger.getLogger(IniModules.class.getName());

    protected IniModules() {

    }

    public static Map<String, List<String>> getEnabledModulesByIniName(String baseDir, String homeDir) {
        Map<String, List<String>> byIniName = new HashMap<>();
        getEnabledModules(baseDir, homeDir, byIniName);
        return byIniName;
    }

    private static List<String> getEnabledModules(String baseDir, String homeDir, Map<String, List<String>> byIniName) {
        //
        // First get  all modules from start.ini in the baseDir
        //
        StartIni startIni = new StartIni(Paths.get(baseDir, "start.ini").toFile());
        final List<String> modules = startIni.getEnabledModules();

        List<String> byIniList = new ArrayList<>();
        if (byIniName != null) {
            modules.forEach(modName -> {
                byIniList.add(modName);
            });
            byIniName.put("start.ini", byIniList);    
        }
        

        Path startDPath = Paths.get(baseDir, "start.d");

        try {
            Stream<Path> stream = Files.list(startDPath);
            stream.forEach((p) -> {
                if (p.getFileName().toString().endsWith(".ini")) {
                    StartdIni dini = new StartdIni(p.toFile());
                    List<String> list = dini.getEnabledModules();

                    list.forEach(mod -> {
                        if (!modules.contains(mod)) {
                            modules.add(mod);
                        }
                    });
                    if (byIniName != null) {
                        byIniName.put(p.getFileName().toString(), list);
                    }

                }
            });
        } catch (IOException ex) {
            LOG.log(Level.INFO, ex.getMessage());
        }
        return modules;

    }

    public static List<String> getEnabledModules(String baseDir, String homeDir) {
        return getEnabledModules(baseDir, homeDir, null);
    }

    public static class StartdIni extends AbsractJettyConfig {

        protected StartdIni(File file) {
            setFile(file);
        }

        public StartdIni(FileObject fileObject) {
            this(FileUtil.toFile(fileObject));
        }

        public boolean isEnabled(String moduleName) {
            return moduleLine(moduleName) >= 0;
        }

        public int moduleLine(String moduleName) {
            int idx = -1;
            for (int i = 0; i < lines().size(); i++) {
                if (lines().get(i).startsWith("--module=" + moduleName)) {
                    idx = i;
                    break;
                }
            }
            return idx;
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

        @Override
        public void commentLine(int idx) {
            if (lines().isEmpty() || idx >= lines().size()) {
                return;
            }
            lines().set(idx, "#" + lines().get(idx));
        }

        public void removeModule(String moduleName) {
            int idx = moduleLine(moduleName);
            if (idx >= 0) {
                lines().remove(idx);
            }
        }

        public void commentModule(String moduleName) {
            int idx = moduleLine(moduleName);
            if (idx >= 0) {
                commentLine(idx);
            }
        }

        public void addModule(String moduleName) {
            int idx = moduleLine(moduleName);
            if (idx >= 0) {
                return;
            }
            lines().add("--module=" + moduleName);

        }

        public List<JsfConfig> getSupportedJsfConfigs() {
            List<JsfConfig> l = new ArrayList<>();
            l.add(new JsfConfig("jsf-myfaces", "org.apache.myfaces.webapp.StartupServletContextListener"));
            l.add(new JsfConfig("jsf-mojarra", "com.sun.faces.config.ConfigureListener"));
            return l;
        }

        public List<String> getSupportedJsfListenerClasses() {
            List<JsfConfig> l = getSupportedJsfConfigs();
            List<String> r = new ArrayList<>();
            for (JsfConfig c : l) {
                r.add(c.getListenerClass());
            }
            return r;
        }

        public String getListenerClassForEnabledJsf() {
            List<JsfConfig> l = getSupportedJsfConfigs();
            for (JsfConfig c : l) {
                if (isEnabled(c.getModuleName())) {
                    return c.getListenerClass();
                }
            }
            return null;
        }

        public String getEnabledJsfModuleName() {
            List<JsfConfig> l = getSupportedJsfConfigs();
            for (JsfConfig c : l) {
                if (isEnabled(c.getModuleName())) {
                    return c.getModuleName();
                }
            }
            return null;
        }

        public static class JsfConfig {

            private String moduleName;
            private String listenerClass;

            public JsfConfig(String moduleName, String listenerClass) {
                this.moduleName = moduleName;
                this.listenerClass = listenerClass;
            }

            public String getModuleName() {
                return moduleName;
            }

            public void setModuleName(String moduleName) {
                this.moduleName = moduleName;
            }

            public String getListenerClass() {
                return listenerClass;
            }

            public void setListenerClass(String listenerClass) {
                this.listenerClass = listenerClass;
            }
        }

    }//class
}//class
