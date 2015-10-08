package org.netbeans.modules.jeeserver.base.embedded.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Valery
 */
public class ServerLibBuilder {

    public List<String> errorMessages = new ArrayList<>();

    private final List<String> moduleNames = new ArrayList<>();
    private final List<String> jarNames = new ArrayList<>();
    public Long jobtime = 0l;

    private static final Logger LOG = Logger.getLogger(ServerLibBuilder.class.getName());

    private final BaseDeploymentManager manager;

    private String jettyHome;
    private String jettyBase;

    private String jettyVersion;

    private String baseModules;
    private String homeModules;
    //
    // Incleds also modules defined in startd directory
    //
    private List<String> startIniModuleNames;

    private final Map<String, Module> modulesMap = new HashMap<>();
    private final Map<String, String> libPathMap = new HashMap<>();

    protected ServerLibBuilder(BaseDeploymentManager manager) {

        this.manager = manager;
        this.jettyVersion = null;
        this.homeModules = null;
        this.baseModules = null;
        init();
    }

    private void init() {
        BaseUtil.out("HOME = " + System.getProperty("netbeans.HOME"));
        BaseUtil.out("netbeans.home = " + System.getProperty("netbeans.home")); 
        
        jettyHome = manager.getInstanceProperties().getProperty(BaseConstants.HOME_DIR_PROP).replace("\\", "/");
//        jettyBase = Paths.get(manager.getServerProject().getProjectDirectory().getPath(), JettyConstants.JETTYBASE_FOLDER)
//                .toString().replace("\\", "/");

//        jettyVersion = Utils.getFullJettyVersion(jettyHome);

        homeModules = Paths.get(jettyHome, "modules").toString().replace("\\", "/");
        baseModules = Paths.get(jettyBase, "modules").toString().replace("\\", "/");

        //startIniModuleNames = IniModules.getEnabledModules(jettyBase);
        startIniModuleNames = getJettyBaseDefinedModules();
        //createModules();
    }
    /**
     * Returns  modules that are explicitly defined in ini files of the {@literal  ${jetty.base}
     * directory. 
     * Dependent modules are not taken into account.
     * 
     * @return all modules that are explicitly defined in ini files of the {@literal  ${jetty.base}
     * directory.
     */
    public List<String> getJettyBaseDefinedModules() {
        //
        // First get  all modules from start.ini in the baseDir
        //
        //Path iniFile = Paths.get(jettyBase, "start.ini");
        File iniFile = Paths.get(jettyBase, "start.ini").toFile();
        IniHelper ini = new IniHelper(iniFile,Paths.get(jettyBase));

        final List<String> modules = ini.getEnabledModules();

        Path startDPath = Paths.get(jettyBase, "start.d");

        try {
            Stream<Path> stream = Files.list(startDPath);
            stream.forEach((p) -> {
                if (p.getFileName().toString().endsWith(".ini")) {
                    IniHelper dini = new IniHelper(p.toFile(),Paths.get(jettyBase));
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
    
    public ServerLibBuilder build() {
        createModules();
        return this;
    }
    public Map<String, Module> getModulesMap() {
        return modulesMap;
    }

    public Map<String, String> getLibPathMap() {
        return libPathMap;
    }

    public List<String> getJarFileNames() {
        return jarNames;
    }

    public List<String> getModuleNames() {
        return moduleNames;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    protected BaseDeploymentManager getManager() {
        return manager;
    }

    protected Map<String, Module> createModules() {
        //
        // May be for example  "annotations" or alpn-impl/alpn-${java.version} 
        //
        for (String nameRef : this.startIniModuleNames) {
            if (this.modulesMap.containsKey(Paths.get(nameRef).getFileName().toString())) {
                continue;
            }
            Path p = findModule(nameRef);

            if (p == null) {
                continue;
            }
                //
            // Module not found. An error registered in tne errorMessages list
            //
            String filePath = p.toString().replace("\\", "/");
            new Module(this, filePath);

        }

        return modulesMap;
    }

    protected Path findModule(String nameRef) {
        Path p = Paths.get(baseModules, nameRef + ".mod");
        if (!Files.exists(p)) {
            p = Paths.get(homeModules, nameRef + ".mod");
            if (!Files.exists(p)) {
                String srv = getManager().getServerProject().getProjectDirectory().getNameExt();
                String msg = "Server: " + srv + ". The module doesn't exists: " + nameRef;
                if ( ! errorMessages.contains(msg)) {
                    errorMessages.add(msg);
                }
                p = null;
                //throw new RuntimeException("The module doesn't exists: " + nameRef);
            }
        }
        return p;

    }

    /**
     *
     */
    public static class Module {

        private final ServerLibBuilder libBuilder;
        private final String modulePath;

        private String name;
        private String logicalName;

//        private boolean jettyBaseModule;
        private List<Module> depend;
        private List<Path> lib;

        List<String> rawDepend;
        List<String> rawLib;

        public Module(ServerLibBuilder libBuilder, String modulePath) {
            this.libBuilder = libBuilder;
            this.modulePath = modulePath;
            rawDepend = new ArrayList<>();
            rawLib = new ArrayList<>();

            name = FileUtil.toFileObject(new File(modulePath))
                    .getName()
                    .toLowerCase();            
            
            if ( ! libBuilder.moduleNames.contains(name)) {
                libBuilder.moduleNames.add(name);
            }
            logicalName = name;

            init();
        }

        private void init() {
            parseModule();
            libBuilder.modulesMap.put(getName(), this);

            addJars();

            for (String line : rawDepend) {
                String l = line.replace("${jetty.version}", libBuilder.jettyVersion);
                l = line.replace("${java.version}", BaseUtil.getJavaVersion());

                if (libBuilder.modulesMap.containsKey(Paths.get(l).getFileName().toString())) {
                    continue;
                }

                Path p = libBuilder.findModule(l);
                if ( p == null ) {
                    continue;
                }
                String filePath = p.toString().replace("\\", "/");

                new Module(libBuilder, filePath);
            }

        }

        protected void parseModule() {
            Pattern section = Pattern.compile("\\s*\\[([^]]*)\\]\\s*");

            try (BufferedReader buf = Files.newBufferedReader(Paths.get(modulePath))) {
                String sectionType = "";
                String line;
                while ((line = buf.readLine()) != null) {
                    line = line.trim();
                    Matcher sectionMatcher = section.matcher(line);

                    if (sectionMatcher.matches()) {
                        sectionType = sectionMatcher.group(1).trim().toUpperCase(Locale.ENGLISH);
                    } else {
                        // blank lines and comments are valid for ini-template section
                        if ((line.length() == 0) || line.startsWith("#")) {
                        } else {
                            switch (sectionType) {
                                case "":
                                    break;
                                case "DEPEND":
                                    rawDepend.add(line);
                                    break;
                                case "LIB":
                                    rawLib.add(line);
                                    break;
                                case "NAME":
                                    if (line != null && !line.trim().isEmpty()) {
                                        logicalName = name;
                                    }
                                    break;
                                case "FILES":
                                    break;
                                case "DEFAULTS": // old nameRef introduced in 9.2.x
                                case "INI": // new nameRef for 9.3+
                                    break;
                                case "INI-TEMPLATE":
                                    break;
                                case "LICENSE":
                                case "LICENCE":
                                    break;
                                case "OPTIONAL":
                                    break;
                                case "EXEC":
                                    break;
                                case "VERSION":
                                    break;
                                case "XML":
                                    break;

                                default:
                                    throw new RuntimeException("Unrecognized Module section: [" + sectionType + "]");
                            }//switch
                        }//if
                    }//if
                }//while
            } catch (Exception ex) {
                throw new RuntimeException("Cannot pars module fole " + modulePath + "; " + ex.getMessage());
            }//try
        }

        protected void addJars() {
            for (String line : rawLib) {

                String l = line
                        .replace("${jetty.version}", libBuilder.jettyVersion)
                        .replace("${java.version}", BaseUtil.getJavaVersion())
                        .replace("\\", "/");
                Long l1 = System.currentTimeMillis();
                addJars(libBuilder.jettyHome, l);
                addJars(libBuilder.jettyBase, l);
                Long l2 = System.currentTimeMillis();
                libBuilder.jobtime += (l2 - l1);
            }

        }

        /**
         *
         * @param root represents a ${jetty.home} or ${jetty.base} directory
         * @param pattern represents a [lib] entry of a module definition.
         */
        protected void addJars(String root, String pattern) {
            LibPathFinder pf = new LibPathFinder();
            pf.setBase(Paths.get(root));
            String path = root + "/" + pattern;
            path = path.replace("//", "/").replace("\\", "/");

            try {
                List<Path> list = pf.createPaths(path);
                for (Path p : list) {
                    libBuilder.jarNames.add(p.getFileName().toString());
                    libBuilder.libPathMap.put(p.getFileName().toString(), p.toString());
                }
            } catch (IOException ex) {
                System.err.println(" getPaths.error " + ex.getMessage());
                LOG.log(Level.INFO, ex.getMessage());
            }

        }

        public String getModulePath() {
            return modulePath;
        }

        public String getLogicalName() {
            return logicalName;
        }

        public List<String> getRawDepend() {
            return rawDepend;
        }

        public List<String> getRawLib() {
            return rawLib;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Module> getDepend() {
            return depend;
        }

        public void setDepend(List<Module> depend) {
            this.depend = depend;
        }

        public List<Path> getLib() {
            return lib;
        }

        public void setLib(List<Path> lib) {
            this.lib = lib;
        }

    }//class

    public static class Error {

        private String message;
        private String serverProjectName;

    }
}//class
