/**
 * This file is part of Jetty Server support in NetBeans IDE.
 *
 * Jetty Server support in NetBeans IDE is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * Jetty Server support in NetBeans IDE is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should see the GNU General Public License here:
 * <http://www.gnu.org/licenses/>.
 */
package org.netbeans.modules.jeeserver.jetty.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author V. Shyshkin
 */
public class NpnConfig {

    private static final Logger LOG = Logger.getLogger(Utils.class.getName());

    private final Project project;
    private final List<String> lines = new ArrayList<>();
    private final List<String> vertionLines = new ArrayList<>();
    
    public NpnConfig(Project project) {
        this.project = project;
        init();
    }

    private final void init() {
Utils.out("NpnConfig.init() START");
        FileObject npnFo = findNpnMod();
        if (npnFo == null) {
            return;
        }

        createNpnModLines(npnFo);
Utils.out("NpnConfig.init() lines.size()=" + lines.size());                
        if (lines.isEmpty()) {
            return;
        }
        
        String path = getNpnModVersionPath();
        if ( path == null ) {
            return;
        }
Utils.out("NpnConfig.init() path=" + path);                        
        FileObject fo = project.getProjectDirectory();
        FileObject npn = fo.getFileObject(JettyConstants.JETTYBASE_FOLDER + "/modules/" + path + ".mod");
        if (npn == null) {
            String homeDir = BaseUtils.getServerProperties(project).getHomeDir();
            fo = FileUtil.toFileObject(new File(homeDir));
            npn = fo.getFileObject("modules/" + path + ".mod");
        }
        if ( npn == null ) {
            return;
        }
        createNpnVersionLines(npn);
Utils.out("NpnConfig.init() versionLines.size()=" + vertionLines.size());                                
        
    }
    public String getBootClassPathLine() {
        //String start = "-Xbootclasspath/p:lib";
        String boot = "";
        for ( String line : vertionLines ) {
            if ( line.startsWith("-Xbootclasspath/p:lib")) {
                boot = line;
                break;
            }
        }
        return boot;
    }
    public String getNpnNameByJavaVersion() {
        return "npn-" + System.getProperty("java.version");
    }

    protected String getNpnModVersionPath() {
        String path = null;
        int idx = -1;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.length() == 0 || line.startsWith("#")) {
                continue;
            }
            if (line.startsWith("[depend]")) {
                idx = i;
                break;
            }
        }
        String s = getNpnNameByJavaVersion();
Utils.out("NpnConfig.getNpnModVersionPath() [depend] idx=" + idx + "; s=" + s);                                
        if (idx >= 0 && idx < lines.size()) {
            idx++;
            for (int i = idx; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.length() == 0 || line.startsWith("#") 
                        || line.startsWith("[") || line.startsWith("-")) {
                    continue;
                }
                if (line.startsWith("[") || line.startsWith("-")) {
                    break;
                }
                
Utils.out("NpnConfig.getNpnModVersionPath() [depend] line=" + line + "; s=" + s);                                
                if ( line.contains(s)) {
                    path = line;
                    break;
                }
                if ( line.contains("npn-${java.version}") ) {
                    int n = line.indexOf("npn-${java.version}");
                    path = line.substring(0,n) + getNpnNameByJavaVersion();
                    break;
                }
            }
        }
Utils.out("NpnConfig.getNpnModVersionPath() [depend] idx=" + idx + "; s=" + s);                                        
        return path;
    }

    public FileObject findNpnMod() {
        FileObject fo = project.getProjectDirectory();
        FileObject npn = fo.getFileObject(JettyConstants.JETTYBASE_FOLDER + "/modules/npn.mod");
        if (npn == null) {
            String homeDir = BaseUtils.getServerProperties(project).getHomeDir();
            fo = FileUtil.toFileObject(new File(homeDir));
            npn = fo.getFileObject("modules/npn.mod");
        }
        return npn;
    }

    public FileObject findNpnVersionMod() {
        FileObject fo = project.getProjectDirectory();
        FileObject npn = fo.getFileObject(JettyConstants.JETTYBASE_FOLDER + "/modules/npn.mod");
        if (npn == null) {
            String homeDir = BaseUtils.getServerProperties(project).getHomeDir();
            fo = FileUtil.toFileObject(new File(homeDir));
            npn = fo.getFileObject("modules/npn.mod");
        }
        return npn;
    }

    protected void createNpnModLines(FileObject npnFo) {
        lines.addAll(getNpnModLines(npnFo));
    }
    protected void createNpnVersionLines(FileObject npnFo) {
        vertionLines.addAll(getNpnModLines(npnFo));
    }

    protected List<String> getNpnModLines(FileObject npnFo) {
        List<String> list = new ArrayList<>();
        File file = FileUtil.toFile(npnFo);
        try (FileReader reader = new FileReader(file)) {
            try (BufferedReader buf = new BufferedReader(reader)) {
                String line;
                while ((line = buf.readLine()) != null) {
                    list.add(line.trim());
                }
            } catch (IOException ex) {
                LOG.log(Level.INFO, ex.getMessage());
            }
        } catch (FileNotFoundException ex) {
            LOG.log(Level.INFO, ex.getMessage());
        } catch (IOException ex) {
            LOG.log(Level.INFO, ex.getMessage());
        }
        return list;
    }

}
