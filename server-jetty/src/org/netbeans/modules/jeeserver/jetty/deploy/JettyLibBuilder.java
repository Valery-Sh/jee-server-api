package org.netbeans.modules.jeeserver.jetty.deploy;

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
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.netbeans.modules.jeeserver.jetty.util.IniModules;
import org.netbeans.modules.jeeserver.jetty.util.JettyConstants;
import org.netbeans.modules.jeeserver.jetty.util.StartIni;
import org.netbeans.modules.jeeserver.jetty.util.Utils;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Valery
 */
public class JettyLibBuilder {

    public List<String> mods = new ArrayList<>();
    public List<String> nams = new ArrayList<>();
    public Long jobtime = 0l;

    private static final Logger LOG = Logger.getLogger(StartIni.class.getName());

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

    public JettyLibBuilder(BaseDeploymentManager manager) {
//BaseUtils.out(" 1 JettyLibBuilder CONSTRUCTOR" + System.currentTimeMillis());        
        this.manager = manager;
        this.jettyVersion = null;
        this.homeModules = null;
        this.baseModules = null;
        init();
    }

    private void init() {
        jettyHome = manager.getInstanceProperties().getProperty(BaseConstants.HOME_DIR_PROP).replace("\\", "/");
        jettyBase = Paths.get(manager.getServerProject().getProjectDirectory().getPath(), JettyConstants.JETTYBASE_FOLDER)
                .toString().replace("\\", "/");

        jettyVersion = Utils.getFullJettyVersion(jettyHome);

        homeModules = Paths.get(jettyHome, "modules").toString().replace("\\", "/");
        baseModules = Paths.get(jettyBase, "modules").toString().replace("\\", "/");
        StartIni startIni = new StartIni(manager.getServerProject());
        //startIniModuleNames = IniModules.getEnabledModules(jettyBase, jettyHome);
        startIniModuleNames = IniModules.getEnabledModules(jettyBase);        
        createModules();
    }

    public Map<String, Module> getModulesMap() {
        return modulesMap;
    }

    public Map<String, String> getLibPathMap() {
        return libPathMap;
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
            String filePath = p.toString().replace("\\", "/");
            Module m = new Module(this, filePath);
        }

        return modulesMap;
    }

    protected Path findModule(String nameRef) {
        Path p = Paths.get(baseModules, nameRef + ".mod");
        if (!Files.exists(p)) {
            p = Paths.get(homeModules, nameRef + ".mod");
            if (!Files.exists(p)) {
                throw new RuntimeException("The module doesn't exists: " + nameRef);
            }
        }
        return p;

    }

    /**
     *
     */
    public static class Module {

        private final JettyLibBuilder libBuilder;
        private final String modulePath;

        private String name;
        private String logicalName;

//        private boolean jettyBaseModule;
        private List<Module> depend;
        private List<Path> lib;

        List<String> rawDepend;
        List<String> rawLib;

        public Module(JettyLibBuilder libBuilder, String modulePath) {
            this.libBuilder = libBuilder;
            this.modulePath = modulePath;
            rawDepend = new ArrayList<>();
            rawLib = new ArrayList<>();

            name = FileUtil.toFileObject(new File(modulePath)).getName();
            libBuilder.mods.add(name);
            logicalName = name;

            init();
        }

        private void init() {
            parseModule();
            libBuilder.modulesMap.put(getName(), this);

            addJars();

            for (String line : rawDepend) {
                String l = line.replace("${jetty.version}", libBuilder.jettyVersion);
                l = line.replace("${java.version}", BaseUtils.getJavaVersion());

                if (libBuilder.modulesMap.containsKey(Paths.get(l).getFileName().toString())) {
                    continue;
                }

                Path p = libBuilder.findModule(l);
                String filePath = p.toString().replace("\\", "/");

                Module m = new Module(libBuilder, filePath);
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
                        .replace("${java.version}", BaseUtils.getJavaVersion())
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
                    libBuilder.nams.add(p.getFileName().toString());
                    libBuilder.libPathMap.put(p.getFileName().toString(), p.toString());
                }
            } catch (IOException ex) {
                System.err.println(" getPaths.error " + ex.getMessage());
                LOG.log(Level.INFO, ex.getMessage());
            }

        }

        public JettyLibBuilder getHandler() {
            return libBuilder;
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
}//class
