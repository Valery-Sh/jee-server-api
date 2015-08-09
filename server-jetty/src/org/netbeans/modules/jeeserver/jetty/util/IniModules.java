package org.netbeans.modules.jeeserver.jetty.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.deployment.lc.LicenseWizard;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Valery
 */
public class IniModules {

    private static final Logger LOG = Logger.getLogger(IniModules.class.getName());

    protected IniModules() {

    }

    public static List<String> getEnabledModules(String baseDir) {
        //
        // First get  all modules from start.ini in the baseDir
        //
        StartIni startIni = new StartIni(Paths.get(baseDir, "start.ini").toFile());
        final List<String> modules = startIni.getEnabledModules();

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
                }
            });
        } catch (IOException ex) {
            LOG.log(Level.INFO, ex.getMessage());
        }
        return modules;

    }
    
    public static boolean isModuleEnabled(File jettyBase, String moduleName) {
        return getEnabledModules(jettyBase.getPath()).contains(moduleName);
    }
    
    public static class CDISupport {
        private File jettyBase; 
        private Project server;
        
        public CDISupport(Project server) {
            this.server = server;
            init();
        }
        public CDISupport(File jettyBase) {
            init(jettyBase);
        }
        
        private void init() {
            Path p = Paths.get(server.getProjectDirectory().getPath(), JettyConstants.JETTYBASE_FOLDER);
            this.jettyBase = p.toFile();
        }
        private void init(File jettyBase) {
            FileObject fo = FileUtil.toFileObject(jettyBase);
            server =  FileOwnerQuery.getOwner(fo);
            this.jettyBase = jettyBase;
        }
        
        protected void disableCDIModule() {
            //Path p = Paths.get(jettyBase.getPath(),"start.ini");
            StartIni ini = new StartIni(server, true);
            ini.commentModule("cdi");
            ini.save();
        }
        
        public boolean isCDIEnabled() {
            return getEnabledModules(jettyBase.getPath()).contains("cdi");
        }
        
        public boolean isCdiLibExists() {
            return Paths.get(jettyBase.getPath(), "lib/cdi").toFile().exists();
        }
        public boolean isLicenseAccepted() {
            boolean enabled = isCDIEnabled();
            boolean libExists = isCdiLibExists();
            return enabled && libExists || ! enabled;
        }
        public static boolean isLicenseAccepted(Project server) {
            return new CDISupport(server).isLicenseAccepted();
        }
        
        public static void showLicenseDialog(Project server) {
            CDISupport cdi = new CDISupport(server);
            cdi.showLicenseDialog();
        }
        
        public void showLicenseDialog() {
            
            if ( isLicenseAccepted() ) {
                return;
            }
            LicenseWizard d = new LicenseWizard();
            if ( ! d.showLicenceDialog() ) {
                disableCDIModule();
            }
        }
        
    }
    
    public static class JsfSupport {
        private final File jettyBase; 
        public JsfSupport(File jettyBase) {
            this.jettyBase = jettyBase;
        }
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
            l.forEach(c -> {
                r.add(c.getListenerClass());
            });

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

        public boolean isEnabled(String moduleName) {
            return getEnabledModules(jettyBase.getPath()).contains(moduleName);
        }

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
