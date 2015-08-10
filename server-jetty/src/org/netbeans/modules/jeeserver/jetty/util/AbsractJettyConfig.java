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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author V. Shyshkin
 */
public class AbsractJettyConfig {
    private static final Logger LOG = Logger.getLogger(AbsractJettyConfig.class.getName());
    protected boolean withComments;

    private File file;
    private List<String> lines = new ArrayList<>();
    private Path baseDir;
    
    protected AbsractJettyConfig() {    
        
    }
    private void init() {
        try {
            Project p = FileOwnerQuery.getOwner(FileUtil.toFileObject(file));
            baseDir = FileUtil.toFile(
                   p.getProjectDirectory().getFileObject(JettyConstants.JETTYBASE_FOLDER))
                    .toPath();
        } catch (Exception ex) {
            throw new RuntimeException("Invalid base dir " + file.toString() + "; " + ex.getMessage() );
        }
        
        
        try (FileReader reader = new FileReader(file)) {
            try (BufferedReader buf = new BufferedReader(reader)) {
                String line;
                while ((line = buf.readLine()) != null) {
                    if (withComments ) {
                        String s = line.trim().replaceAll(" ", "");
                        if (s.startsWith("--module=")) {
                            separateModules(s);
                        } else {
                            lines.add(line);
                        }
                        
                    }
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
    public void setFile(FileObject file) {
        this.file = FileUtil.toFile(file);
        init();
    }
    

    public void commentLine(int idx) {
        if (lines.isEmpty() || idx >= lines.size()) {
            return;
        }
        lines.set(idx, "#" + lines.get(idx));
    }


    public void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file));) {
            for (String line : lines) {
                writer.write(line + System.lineSeparator());
            }
        } catch (IOException ex) {
            LOG.log(Level.INFO, ex.getMessage());
        }
    }

    public List<String> lines() {
        return lines;
    }

    public File getFile() {
        return file;
    }

    private void separateModules(String line) {
        int idx = line.indexOf('=');
        String value = line.substring(idx + 1);
        for (String part : value.split(",")) {
            String s = "--module=" + part;
            s = s.replace("${start.basedir}",baseDir.toString());
            if ( ! lines.contains(s)) {
                lines.add(s);
            }
        }
    }
}
