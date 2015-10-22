/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jeeserver.base.embedded.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Valery
 */
public class IniHelper {
    
    private static final Logger LOG = Logger.getLogger(LibPathFinder.class.getName());

    private File file;
    private final List<String> lines = new ArrayList<>();
    private final Path jettyBase;

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
