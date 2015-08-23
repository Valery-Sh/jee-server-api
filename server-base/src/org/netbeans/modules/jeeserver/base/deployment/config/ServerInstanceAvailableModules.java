/**
 * This file is part of Base JEE Server support in NetBeans IDE.
 *
 * Base JEE Server support in NetBeans IDE is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * Base JEE Server support in NetBeans IDE is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should see the GNU General Public License here:
 * <http://www.gnu.org/licenses/>.
 */
package org.netbeans.modules.jeeserver.base.deployment.config;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author V. Shyshkin
 * @param <T>
 */
public class ServerInstanceAvailableModules<T extends AbstractModuleConfiguration> implements AvailableWebModules<T> {

    private final Project project;

    //private final Map<String, WebModuleConfig> configMap;
    private final Map<Path, WebModuleConfig> configMap;

    private final List<ModulesChangeListener> listeners;

    public ServerInstanceAvailableModules(Project project) {
        this.configMap = Collections.synchronizedMap(new HashMap());
        this.project = project;
        listeners = new ArrayList<>();
    }

    public ServerInstanceAvailableModules addModulesChangeListener(Project project, ModulesChangeListener l) {
        listeners.add(l);
        return this;
    }

    @Override
    public void moduleDispose(T module) {
        if (configMap.isEmpty()) {
            return;
        }
        Properties props = module.getContextProperties();
        String contextPath = props.getProperty("contextPath");
        String war = module.getWebProject().getProjectDirectory().getPath();

        if (war == null) {
            return;
        }
        Path warPath = Paths.get(war);
        WebModuleConfig wmk = configMap.get(warPath);
        if (wmk != null) {
            configMap.remove(warPath);
            fireModulesChange(wmk, ModulesChangeEvent.DISPOSE);
        }
    }

    public Map<Path, WebModuleConfig> getModules() {
        return configMap;
    }

    public WebModuleConfig getModuleConfig(Project webProject) {
        WebModuleConfig c = null;
        Path webProjPath = Paths.get(webProject.getProjectDirectory().getPath());
        for (WebModuleConfig mc : getModuleList()) {
            Path p = Paths.get(mc.getWebProjectPath());
            if (webProjPath.equals(p)) {
                c = mc;
                break;
            }
        }
        return c;
    }

    public WebModuleConfig[] getModuleList() {
        WebModuleConfig[] list = new WebModuleConfig[configMap.size()];
        int i = 0;

        for (WebModuleConfig c : configMap.values()) {
            list[i++] = c;
        }
        return list;

    }

    @Override
    public void moduleCreate(T module) {
        Properties props = module.getContextProperties();
        String contextPath = props.getProperty("contextPath");

        String war = module.getWebProject().getProjectDirectory().getPath();
        if (war == null) {
            return;
        }

        File warFile = new File(war);
        Path warPath = Paths.get(module.getWebProject().getProjectDirectory().getPath());
        String key = null;
        WebModuleConfig wmKey = configMap.get(warPath);

        if (wmKey == null) {
            File webFolder = module.getWebRoot();
            WebModuleConfig wmk = new WebModuleConfig(contextPath, war);
            if (webFolder != null) {
                wmk.setWebFolderPath(webFolder.getPath());
            }
            configMap.put(warPath, wmk);
            FileObject dirFo = FileUtil.toFileObject(new File(wmk.getWebProjectPath()));
            dirFo.addFileChangeListener(new DeleteProjectHandler(module.getWebProject(), wmk));
            fireModulesChange(wmk, ModulesChangeEvent.CREATE);

        }
    }

    public Project getProject() {
        return project;
    }

    @Override
    public synchronized void addModulesChangeListener(ModulesChangeListener l) {
        listeners.add(l);
    }

    @Override
    public synchronized void removeModulesChangeListener(ModulesChangeListener l) {
        listeners.remove(l);
    }

    protected void fireModulesChange(WebModuleConfig cfg, int eventType) {
        WebModuleConfig[] list = new WebModuleConfig[configMap.size()];
        int i = 0;

        for (WebModuleConfig c : configMap.values()) {
            list[i++] = c;
        }

        ModulesChangeEvent e = new ModulesChangeEvent(this, eventType, cfg, list);
        for (ModulesChangeListener l : listeners) {
            l.availableModulesChanged(e);
        }
    }

    public class DeleteProjectHandler implements FileChangeListener {

        private final Project webProject;
        private final WebModuleConfig moduleConfig;

        public DeleteProjectHandler(Project webProject, WebModuleConfig moduleConfig) {
            this.webProject = webProject;
            this.moduleConfig = moduleConfig;
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
        }

        @Override
        public void fileChanged(FileEvent fe) {
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            if (configMap.isEmpty()) {
                return;
            }
            Path p1 = Paths.get(fe.getFile().getPath());
            Path p2 = Paths.get(moduleConfig.getWebProjectPath());
            Project proj = null;
            if (Files.exists(p2)) {
                FileObject fo = FileUtil.toFileObject(p2.toFile());
                proj = FileOwnerQuery.getOwner(fo);
            }
            boolean remove = p1.equals(p2);
            if (proj != null && !remove) {
                if (BaseUtils.isAntProject(proj)) {
                    p2 = Paths.get(proj.getProjectDirectory().getPath(), "nbproject");
                } else if (BaseUtils.isMavenProject(proj)) {
                    p2 = Paths.get(proj.getProjectDirectory().getPath(), "nb-configuration.xml");
                } else {
                    p2 = null;
                }

                remove = p1.equals(p2);
            }
            if (remove) {
                configMap.remove(Paths.get(moduleConfig.getWebProjectPath()));
                fireModulesChange(moduleConfig, ModulesChangeEvent.DELETED);
            }

        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe
        ) {
        }

    }
}//class
