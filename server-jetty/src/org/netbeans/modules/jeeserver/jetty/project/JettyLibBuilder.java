package org.netbeans.modules.jeeserver.jetty.project;

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
import org.netbeans.modules.jeeserver.jetty.deploy.LibPathFinder;
import org.netbeans.modules.jeeserver.jetty.project.JettyConfig.IniHelper;
import org.netbeans.modules.jeeserver.jetty.util.JettyConstants;
import org.netbeans.modules.jeeserver.jetty.util.StartIni;
import org.netbeans.modules.jeeserver.jetty.util.Utils;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Valery
 */
public class JettyLibBuilder {

    public List<String> errorMessages = new ArrayList<>();

    // 30.08 private final List<String> moduleNames = new ArrayList<>();
    //30.08 private final List<String> jarNames = new ArrayList<>();
    private static final Logger LOG = Logger.getLogger(StartIni.class.getName());

    private final BaseDeploymentManager manager;

    private String jettyHome;
    private String jettyBase;

    private String jettyVersion;

    private String baseModules;
    private String homeModules;
    //
    // Incldes also modules defined in startd directory
    //
    private List<String> startIniModuleNames;

    private final Map<String, Module> modulesMap = new HashMap<>();
    //private final Map<String, String> jarsMap = new HashMap<>();

    protected JettyLibBuilder(BaseDeploymentManager manager) {

        this.manager = manager;
        this.jettyVersion = null;
        this.homeModules = null;
        this.baseModules = null;
        init();
    }

    private void init() {
        jettyHome = manager.getInstanceProperties().getProperty(BaseConstants.HOME_DIR_PROP);
        if ( jettyHome != null ) {
            jettyHome = manager.getInstanceProperties().getProperty(BaseConstants.HOME_DIR_PROP).replace("\\", "/");
        }
        jettyBase = Paths.get(manager.getServerProject().getProjectDirectory().getPath(), JettyConstants.JETTYBASE_FOLDER)
                .toString().replace("\\", "/");

        jettyVersion = Utils.getFullJettyVersion(jettyHome);

        homeModules = Paths.get(jettyHome, "modules").toString().replace("\\", "/");
        baseModules = Paths.get(jettyBase, "modules").toString().replace("\\", "/");

        //startIniModuleNames = IniModules.getEnabledModules(jettyBase);
        startIniModuleNames = getJettyBaseDefinedModules();

        //createModules();
    }

    /**
     * Returns modules that are explicitly defined in ini files of the {@literal  ${jetty.base}
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
        IniHelper ini = new IniHelper(iniFile, Paths.get(jettyBase));

        final List<String> modules = ini.getEnabledModules();

        Path startDPath = Paths.get(jettyBase, "start.d");

        try {
            Stream<Path> stream = Files.list(startDPath);
            stream.forEach((p) -> {
                if (p.getFileName().toString().endsWith(".ini")) {
                    IniHelper dini = new IniHelper(p.toFile(), Paths.get(jettyBase));
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

    public JettyLibBuilder build() {
        createModules();
        return this;
    }

    public Map<String, Module> getModulesMap() {
        return modulesMap;
    }

    public Map<String, String> getLibPathMap() {
        Map<String, String> map = new HashMap<>();
        modulesMap.forEach((modName, modObj) -> {
            map.putAll(modObj.jarsMap);
        });

        return map;
    }

    public List<String> getJarNames() {
        //30.08 return jarNames;
        List<String> list = new ArrayList<>();
        modulesMap.forEach((modName, moduleObj) -> {
            list.addAll(moduleObj.getJarNames());
        });
        return list;
    }

    public List<String> getModuleNames() {
//30.05        return moduleNames;
        List<String> list = new ArrayList<>();
        modulesMap.forEach((k, v) -> {
            list.add(k);
        });
        return list;
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

        List<String> oldIniModules = this.startIniModuleNames;
        //
        // get explicitly defined modules in tje jetty.base dir
        //
        this.startIniModuleNames = getJettyBaseDefinedModules();
        for (String nameRef : oldIniModules) {
            if (!startIniModuleNames.contains(nameRef)) {
                Module mod = modulesMap.get(Paths.get(nameRef).getFileName().toString());
                if ( mod != null ) {
                    mod.delete();
                }
            }
        }
            /*        for (String nameRef : oldIniModules) {
             Module mod = modulesMap.get(Paths.get(nameRef).getFileName().toString());
             if (modulesMap.containsKey(Paths.get(nameRef).getFileName().toString())) {
             // We must delete a module
             BaseUtils.out("$$$$$$$$$$ module to delete = " + nameRef);
             // this.jarsMap.clear();
             this.modulesMap.clear();
             errorMessages.clear();
             //moduleNames.clear();
             //jarNames.clear();

             }
             //
             // Module not found. An error registered in tne errorMessages list
             //
             //String filePath = p.toString().replace("\\", "/");
             //new Module(this, filePath);

             }
             */
            //int count = 0;
            for (String nameRef : this.startIniModuleNames) {
                //BaseUtils.out( (++count) + " module = " + nameRef);
                if (this.modulesMap.containsKey(Paths.get(nameRef).getFileName().toString())) {
                    continue;
                }
                Path p = findModule(nameRef);

                Module m = modulesMap.get(nameRef);
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
                if (!errorMessages.contains(msg)) {
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

        private final JettyLibBuilder libBuilder;
        /**
         * A full path to the module file.
         */
        private final String moduleLocation;
        /**
         * A low-cased module name without extension.
         */
        private String name;
        /**
         * A line of the {@literal [name] } section in the module file.
         */
        private String logicalName;

        //private List<Module> depend;
        //private List<Path> lib;
        /**
         * The key is a jar name and a value is an absolute path to a jar file
         */
        private Map<String, String> jarsMap;
        /**
         * A list of the modules that depend on this module
         */
        List<String> dependentModules;

        /**
         * Lines of a {@literal [depend]} section of the module
         */
        List<String> rawDependLines;
        /**
         * Lines of a {@literal [lib]} section of the module
         */
        List<String> rawLibLines;

        public Module(JettyLibBuilder libBuilder, String moduleLocation) {
            this.libBuilder = libBuilder;
            this.moduleLocation = moduleLocation;
            dependentModules = new ArrayList<>();
            rawDependLines = new ArrayList<>();
            rawLibLines = new ArrayList<>();

            name = FileUtil.toFileObject(new File(moduleLocation))
                    .getName()
                    .toLowerCase();

            logicalName = name;

            init();
        }

        private void init() {
            jarsMap = new HashMap<>();
            parseModule();
            libBuilder.modulesMap.put(getName(), this);

            addJars();

            for (String line : rawDependLines) {
                String l = line.replace("${jetty.version}", libBuilder.jettyVersion);
                l = line.replace("${java.version}", BaseUtil.getJavaVersion());
                Module mod = libBuilder.modulesMap.get(Paths.get(l).getFileName().toString());
                if (mod != null) {
                    mod.addDependentModule(name);
                    continue;
                }

                Path p = libBuilder.findModule(l);
                if (p == null) {
                    continue;
                }
                String filePath = p.toString().replace("\\", "/");

                mod = new Module(libBuilder, filePath);
                mod.addDependentModule(name);
            }
        }

        public void delete() {
            if (dependentModules.isEmpty()) {
                libBuilder.modulesMap.remove(name);
                rawDependLines.forEach(line -> {
                    String l = line.replace("${jetty.version}", libBuilder.jettyVersion);
                    l = line.replace("${java.version}", BaseUtil.getJavaVersion());
                    Module mod = libBuilder.modulesMap.get(Paths.get(l).getFileName().toString());
                    if (mod != null) {
                        mod.delete();
                    }
                });
            }
        }

        /**
         * return a list of the modules that depend on this module
         *
         * @return a list of dependent modules
         */
        public List<String> getDependentModules() {
            return dependentModules;
        }

        /**
         * Adds a specified module name to a list of dependent modules.
         *
         * @param moduleName a module name to be added
         */
        public void addDependentModule(String moduleName) {
            if (!dependentModules.contains(moduleName)) {
                dependentModules.add(moduleName);
            }
        }

        protected void parseModule() {
            Pattern section = Pattern.compile("\\s*\\[([^]]*)\\]\\s*");

            try (BufferedReader buf = Files.newBufferedReader(Paths.get(moduleLocation))) {
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
                                    rawDependLines.add(line);
                                    break;
                                case "LIB":
                                    rawLibLines.add(line);
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
                throw new RuntimeException("Cannot pars module fole " + moduleLocation + "; " + ex.getMessage());
            }//try//try
        }

        protected void addJars() {
            rawLibLines.forEach(line -> {
                String l = line
                        .replace("${jetty.version}", libBuilder.jettyVersion)
                        .replace("${java.version}", BaseUtil.getJavaVersion())
                        .replace("\\", "/");
                addJars(libBuilder.jettyHome, l);
                addJars(libBuilder.jettyBase, l);
            });
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
                    jarsMap.put(p.getFileName().toString(), p.toString());
                }
            } catch (IOException ex) {
                LOG.log(Level.INFO, ex.getMessage());
            }
        }

        /**
         * Returns a full path to a module file as a {@literal  String}.
         *
         * @return a full path to a module file
         */
        public String getModuleLocation() {
            return moduleLocation;
        }

        /**
         * Return the line of the {@literal [name] } section in the module file.
         *
         * @return a logical name of the module
         */
        public String getLogicalName() {
            return logicalName;
        }

        /**
         * Returns a character string that matches the string in the
         * {@literal [depend]} section of the module
         *
         * @return a line of a {@literal [depend]} section of the module
         */
        public List<String> getRawDependLines() {
            return rawDependLines;
        }

        /**
         * Returns a character string that matches the string in the
         * {@literal [lib]} section of the module.
         *
         * @return a line of a {@literal [lib]} section of the module.
         */
        public List<String> getRawLibLines() {
            return rawLibLines;
        }

        /**
         * Returns a simple module name without extension. The name is
         * lowercased.
         *
         * @return a simple module name without extension. The name is in lower
         * case.
         */
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        /**
         * Returns a list of {@literal jar} file names which corresponds to a
         * {@literal [lib]} section in the module file.
         *
         * @return a jar file names as a {@literal List}
         */
        public List<String> getJarNames() {
            return new ArrayList<>(jarsMap.keySet());
        }

    }//class

    public static class Error {

        private String message;
        private String serverProjectName;

    }
}//class
