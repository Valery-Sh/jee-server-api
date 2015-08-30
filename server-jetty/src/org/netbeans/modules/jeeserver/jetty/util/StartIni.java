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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.base.deployment.config.ServerInstanceAvailableModules;
import org.netbeans.modules.jeeserver.base.deployment.config.WebModuleConfig;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.netbeans.modules.jeeserver.base.deployment.utils.Info;
import org.netbeans.modules.jeeserver.jetty.project.JettyLibBuilder;
import org.netbeans.modules.jeeserver.jetty.deploy.JettyServerPlatformImpl;
import org.netbeans.modules.jeeserver.jetty.project.JettyConfig;
import org.netbeans.modules.jeeserver.jetty.project.nodes.actions.AbstractHotDeployedContextAction;
import org.netbeans.modules.jeeserver.jetty.project.nodes.libs.LibUtil;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOColors;

public class StartIni extends AbsractJettyConfig {

    private static final Logger LOG = Logger.getLogger(StartIni.class.getName());

    public StartIni(File file) {
        setFile(file);
    }

    public StartIni(FileObject fileObject) {
        this(FileUtil.toFile(fileObject));

    }

    public StartIni(Project server) {
        this(FileUtil.toFile(server.getProjectDirectory()
                .getFileObject(JettyConstants.JETTY_START_INI)));
    }

    public StartIni(Project server, boolean withComments) {
        this.withComments = withComments;

        setFile(FileUtil.toFile(server.getProjectDirectory()
                .getFileObject(JettyConstants.JETTY_START_INI)));

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
    /*
     public List<JsfConfig> getSupportedJsfConfigs() {
     List<JsfConfig> l = new ArrayList<>();
     l.add(new JsfConfig("jsf-myfaces", "org.apache.myfaces.webapp.StartupServletContextListener"));
     l.add(new JsfConfig("jsf-mojarra", "com.sun.faces.config.ConfigureListener"));
     l.add(new JsfConfig("jsf-netbeans", "com.sun.faces.config.ConfigureListener"));
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
     */

    /**
     * A handler of the {@literal FileEvent } that is registered on the
     * {@literal FileObject} that is associated with a
     * {@literal server-instance-config} folder.
     */
    public static class StartIniFileChangeHandler extends FileChangeAdapter {
        private static final RequestProcessor RP = new RequestProcessor(StartIniFileChangeHandler.class);
            
        private final Project project;

        public StartIniFileChangeHandler(Project project) {
            this.project = project;
        }

        /**
         * Called when a file is changed. Does nothing. For now does nothing.
         *
         * @param ev the event describing context where action has taken place
         */
        @Override
        public void fileChanged(FileEvent ev) {
            BaseDeploymentManager manager = BaseUtils.managerOf(project);
            //RequestProcessor rp = new RequestProcessor("Server processor", 1);
            RP.post(new RunnableImpl(manager), 0, Thread.NORM_PRIORITY);
        }

        protected static class RunnableImpl implements Runnable {

            private final BaseDeploymentManager manager;

            public RunnableImpl(BaseDeploymentManager manager) {
                this.manager = manager;
            }

            @Override
            public void run() {
                ((JettyServerPlatformImpl) manager.getPlatform()).notifyLibrariesChanged();
                LibUtil.updateLibraries(manager.getServerProject());
                
            }

        }

        public void fileChanged_1(FileEvent ev) {
            ServerInstanceAvailableModules availableModules = project.getLookup().lookup(ServerInstanceAvailableModules.class);

            //JettyLibBuilder jmh = new JettyLibBuilder((BaseDeploymentManager) BaseUtils.managerOf(project));
            JettyLibBuilder jmh = JettyConfig.getInstance(project).getLibBuilder();            
            jmh.build();
            
            Map<String, String> map1 = jmh.getLibPathMap();
            StartIni ini = null;
            try {
                ini = new StartIni(project);
            } catch (Exception ex) {
                LOG.log(Level.INFO, ex.getMessage());
            }
            String baseDir = Paths.get(
                    project.getProjectDirectory().getPath(), JettyConstants.JETTYBASE_FOLDER)
                    .toString();
            String homeDir = BaseUtils.getServerProperties(project).getHomeDir();

            List<String> modules = ini.getEnabledModules();
            //Map<String, List<String>> byIniName = IniModules.getEnabledModulesByIniName(baseDir, homeDir);

            Info f = new Info();
            f.line('=');
            f.add(new String[]{
                "              FILE start.ini CHANGED"
            });

            f.line();
            f.add(new String[]{
                "  ACTIVE MODULES: "
            });

            String[] ar = new String[modules.size()];
            for (int i = 0; i < modules.size(); i++) {
                ar[i] = "         " + modules.get(i) + "\t\t\t" + "[start.ini]";
            }

            IOColors.OutputType ot = IOColors.OutputType.HYPERLINK;

            f.add(ot, ar);

            f.line(IOColors.OutputType.LOG_SUCCESS, '=');

            f.out("start.ini changed info");

            WebModuleConfig[] configs = availableModules.getModuleList();
        }

        /*        protected void modifyWebPoject(WebModuleConfig cfg) throws IOException {

         DDProvider p = DDProvider.getDefault();

         FileObject webFo = FileUtil.toFileObject(new File(cfg.getWebProjectPath()));

         if (webFo == null) {
         return;
         }
         final WebModule wm = WebModule.getWebModule(webFo);

         if (wm == null) {
         return;
         }
         webFo = wm.getDeploymentDescriptor();

         if (webFo == null) {
         return;
         }

         File f = Paths.get(project.getProjectDirectory().getPath(), JettyConstants.JETTY_START_INI).toFile();

         StartIni startIni = new StartIni(f);

         if (wm.getWebInf() != null) {
         FileObject beansXml = wm.getWebInf().getFileObject("beans.xml");
         Path beansPath = Paths.get(wm.getWebInf().getPath(), "beans.xml");

         boolean cdiEnabled = startIni.isEnabled("cdi");
         if (cdiEnabled && beansXml == null) {
         //
         // Add beans.xml
         //
         try (InputStream is = JettyBaseRootNode.class.getResourceAsStream("/org/netbeans/modules/jeeserver/jetty/resources/beans.xml");) {
         Files.copy(is, beansPath);
         } catch (IOException ex) {
         LOG.log(Level.INFO, ex.getMessage());
         }

         } else if (!cdiEnabled && beansXml != null) {
         //
         // Delete beans.xml
         //
         try {
         Files.delete(beansPath);
         } catch (IOException ex) {
         LOG.log(Level.INFO, ex.getMessage());
         }
         }
         }

         String listenerClass = startIni.getListenerClassForEnabledJsf();

         if (listenerClass == null) {
         return;
         }
         try {
         WebApp webapp = p.getDDRoot(webFo);
         Listener[] listeners = webapp.getListener();
         for (Listener l : listeners) {
         if (listenerClass.equals(l.getListenerClass())) {
         return;
         }
         }
         List<String> supported = startIni.getSupportedJsfListenerClasses();

         for (Listener l : listeners) {
         if (supported.contains(l.getListenerClass())) {
         webapp.removeListener(l);
         }
         }

         addListener(webapp, listenerClass);
         webapp.write(webFo);
         } catch (Exception ex) {
         LOG.log(Level.INFO, ex.getMessage());
         }
         }

         protected Listener addListener(WebApp webApp, String classname) throws IOException {
         Listener listener = (Listener) createBean(webApp, "Listener"); // NOI18N
         listener.setListenerClass(classname);
         webApp.addListener(listener);
         return listener;
         }

         protected CommonDDBean createBean(CreateCapability creator, String beanName) throws IOException {
         CommonDDBean bean = null;
         try {
         bean = creator.createBean(beanName);
         } catch (ClassNotFoundException ex) {
         throw new IOException("Error creating bean with name:" + beanName); // NOI18N
         }
         return bean;
         }
         */
    }//class StartIniChangeHandler

}//class
