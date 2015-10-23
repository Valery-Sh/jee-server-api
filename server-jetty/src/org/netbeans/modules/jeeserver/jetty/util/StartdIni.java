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
package org.netbeans.modules.jeeserver.jetty.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class StartdIni extends AbsractJettyConfig {

    private static final Logger LOG = Logger.getLogger(StartIni.class.getName());

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

    public Map<String,String> getIniProperties() {
        List<String> lines = lines();
        Map<String,String> map = new HashMap<>();
        lines.forEach(line -> {
            if ( ! line.trim().isEmpty() && ! line.trim().startsWith("#")) {
                String[] pair = line.split("=");
                if ( pair.length == 2 ) {
                    map.put(pair[0],pair[1]);
                }
            }
        });
        return map;
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
