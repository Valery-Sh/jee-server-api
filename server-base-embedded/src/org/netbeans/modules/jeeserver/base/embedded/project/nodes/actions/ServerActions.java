package org.netbeans.modules.jeeserver.base.embedded.project.nodes.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import javax.lang.model.SourceVersion;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static javax.swing.Action.NAME;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.base.deployment.ServerInstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.actions.StartServerAction;
import org.netbeans.modules.jeeserver.base.deployment.actions.StopServerAction;
import org.netbeans.modules.jeeserver.base.deployment.maven.MavenAuxConfig;
import org.netbeans.modules.jeeserver.base.deployment.progress.BaseActionProviderExecutor;
import org.netbeans.modules.jeeserver.base.deployment.progress.BaseAntTaskProgressObject;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import static org.netbeans.modules.jeeserver.base.deployment.maven.MavenAuxConfig.AUX_ATTR;
import org.netbeans.modules.jeeserver.base.embedded.project.SuiteManager;
import org.netbeans.modules.jeeserver.base.embedded.project.nodes.SuiteNotifier;
import org.netbeans.modules.jeeserver.base.embedded.project.wizard.CustomizerWizardActionAsIterator;
import org.netbeans.modules.jeeserver.base.embedded.project.wizard.AddExistingProjectWizardActionAsIterator;
import org.netbeans.modules.jeeserver.base.embedded.project.wizard.ServerInstanceAntBuildExtender;
import org.netbeans.modules.jeeserver.base.embedded.project.wizard.InstanceWizardActionAsIterator;
import org.netbeans.modules.jeeserver.base.embedded.project.wizard.MainClassChooserPanelVisual;
import org.netbeans.modules.jeeserver.base.embedded.project.wizard.ServerInstanceBuildExtender;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteConstants;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteUtil;
import org.netbeans.modules.jeeserver.base.embedded.webapp.DistributedWebAppManager;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.w3c.dom.Element;

/**
 *
 * @author V. Shyshkin
 */
public class ServerActions {

    public static class StartStopAction {

        public static Action getAction(String type, Lookup context) {
            FileObject fo = context.lookup(FileObject.class);
            Project serverProject = FileOwnerQuery.getOwner(fo);
            Properties props = null;
            if (!BaseUtil.isAntProject(serverProject) && (needsBuildProject(serverProject) || needsBuildRepo(serverProject))) {
                props = new Properties();
                props.setProperty(StartServerAction.ACTION_ENABLED_PROP, "true");
            }
            if ("start".equals(type)) {
                return new StartServerAction().createContextAwareInstance(context, props);
            } else {
                return new StopServerAction().createContextAwareInstance(context);
            }
        }

        protected static boolean needsBuildProject(Project serverProject) {
            boolean result = false;
            FileObject fo = serverProject.getProjectDirectory().getFileObject("target");
            if (fo == null || fo.getFileObject("classes") == null) {
                result = true;
            }
            return result;
        }

        protected static boolean needsBuildRepo(Project serverProject) {

            FileObject cmJar = SuiteUtil.getCommandManagerJar(serverProject);

            Properties pomProperties = BaseUtil.getPomProperties(cmJar);
            boolean result = false;
            if (pomProperties != null) {

                String str = pomProperties.getProperty("groupId");
                str = str.replace(".", "/");
                str += "/"
                        + pomProperties.getProperty("artifactId")
                        + "/"
                        + pomProperties.getProperty("version")
                        + "/"
                        + cmJar.getNameExt();

                result = cmJar.getParent().getFileObject(str) == null;
            }
            return result;
        }
    }

    public static class BuildProjectActions extends AbstractAction implements ContextAwareAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            assert false;
        }

        @Override
        public Action createContextAwareInstance(Lookup context) {
            return getContextAwareInstance("build", context);
        }

        public static Action getContextAwareInstance(String type, Lookup context) {
            FileObject fo = context.lookup(FileObject.class);
            if (BaseUtil.isAntProject(FileOwnerQuery.getOwner(fo))) {
                return getAntContextAwareInstance(type, context);
            } else {
                return getMavenContextAwareInstance(type, context);
            }
        }

        public static Action getAntContextAwareInstance(String command, Lookup context) {
            if (!AntContextAction.isCommandSupported(command)) {
                return null;
            }
            return new AntContextAction(command, context);
        }

        public static Action getMavenContextAwareInstance(String command, Lookup context) {
            if (!MavenContextAction.isCommandSupported(command)) {
                return null;
            }
            return new MavenContextAction(command, context);
        }

        /*        protected static final class BuildContextAction extends ContextAction {

         public BuildContextAction(Lookup context) {
         super(context);
         }

         @Override
         protected String getName() {
         return "Build";
         }

         protected String getAntTarget() {
         return "maven-build-goals";
         }

         @Override
         protected String getMavenGoals() {
         return "package";
         }
         }

         protected static final class CleanContextAction extends ContextAction {

         public CleanContextAction(Lookup context) {
         super(context);
         }

         @Override
         protected String getName() {
         return "Clean";
         }

         protected String getAntTarget() {
         return "maven-build-goals";
         }

         @Override
         protected String getMavenGoals() {
         return "clean";
         }
         }

         protected static final class CleanAndBuildContextAction extends ContextAction {

         public CleanAndBuildContextAction(Lookup context) {
         super(context);
         }

         @Override
         protected String getName() {
         return "Clean and Build";
         }

         protected String getAntTarget() {
         return "maven-build-goals";
         }

         @Override
         protected String getMavenGoals() {
         return "clean package";
         }

         }

         protected static class ContextAction extends AbstractAction { //implements ProgressListener {

         final private Lookup context;
         final Project serverProject;

         public ContextAction(Lookup context) {
         this.context = context;
         FileObject fo = context.lookup(FileObject.class);
         serverProject = FileOwnerQuery.getOwner(fo);

         if (BaseUtil.isAntProject(serverProject)) {
         setEnabled(false);
         } else {
         setEnabled(true);
         }

         putValue(NAME, getName());
         }

         protected String getName() {
         return "Rebuild All ( project and it's repo)";
         }

         protected String getAntTarget() {
         return "maven-rebuild-all";
         }

         protected String getMavenGoals() {
         return "clean deploy:deploy-file install:install-file package";
         }

         protected void setCommonProperties(Properties props) {
         props.setProperty(BaseAntTaskProgressObject.ANT_TARGET, getAntTarget());
         props.setProperty(BaseAntTaskProgressObject.WAIT_TIMEOUT, "0");
         props.setProperty("goals", getMavenGoals());
         }

         @Override
         public void actionPerformed(ActionEvent e) {

         Properties props = setStartProperties(serverProject);
         setCommonProperties(props);

         new BaseAntTaskProgressObject(null, props).execute();

         }

         public Properties setStartProperties(Project serverProject) {

         if (!BaseUtil.isAntProject(serverProject)) {
         return setMavenProperies(serverProject);
         } else {
         return null;
         }

         }

         protected Properties setMavenProperies(Project serverProject) {
         //String cp = BaseUtil.getMavenClassPath(manager);
         Properties startProperties = new Properties();

         FileObject fo = serverProject.getProjectDirectory().getFileObject("nbdeployment/build.xml");
         startProperties.setProperty(BaseAntTaskProgressObject.BUILD_XML, fo.getPath());

         FileObject cmJar = SuiteUtil.getCommandManagerJar(serverProject);

         Properties pomProperties = BaseUtil.getPomProperties(cmJar);
         if (pomProperties != null) {

         String str = pomProperties.getProperty("groupId");
         str = str.replace(".", "/");
         str += "/"
         + pomProperties.getProperty("artifactId")
         + "/"
         + pomProperties.getProperty("version")
         + "/"
         + cmJar.getNameExt();

         if (cmJar.getParent().getFileObject(str) == null) {
         startProperties.setProperty("do.deploy-file", "yes");
         }

         startProperties.setProperty(SuiteConstants.COMMAND_MANAGER_GROUPID,
         pomProperties.getProperty("groupId"));

         startProperties.setProperty(SuiteConstants.COMMAND_MANAGER_ARTIFACTID,
         pomProperties.getProperty("artifactId"));
         startProperties.setProperty(SuiteConstants.COMMAND_MANAGER_VERSION,
         pomProperties.getProperty("version"));
         startProperties.setProperty(BaseConstants.COMMAND_MANAGER_JAR_NAME_PROP,
         pomProperties.getProperty("artifactId") + "-"
         + pomProperties.getProperty("version")
         + ".jar"
         );
         }
         //properties.setProperty("target.project.classes",
         //            "target/classes");

         startProperties.setProperty(SuiteConstants.MAVEN_REPO_LIB_PATH_PROP,
         SuiteConstants.MAVEN_REPO_LIB_PATH);

         //                startProperties.setProperty(SuiteConstants.MAVEN_RUN_CLASSPATH_PROP, cp);
         //
         // We set MAVEN_DEBUG_CLASSPATH_PROP. In future this approach may change
         //
         //                properties.setProperty(SuiteConstants.MAVEN_DEBUG_CLASSPATH_PROP, cp);
         startProperties.setProperty(SuiteConstants.MAVEN_WORK_DIR_PROP, serverProject.getProjectDirectory().getPath());
         String mainClass = getMavenMainClass(serverProject);
         if (mainClass != null) {
         startProperties.setProperty(SuiteConstants.MAVEN_MAIN_CLASS_PROP, mainClass);
         }
         return startProperties;
         }

         protected String getMavenMainClass(Project project) {
         BaseDeploymentManager dm = SuiteManager.getManager(project);
         String mainClass = dm.getInstanceProperties().getProperty(SuiteConstants.MAVEN_MAIN_CLASS_PROP);
         if (mainClass != null) {
         return mainClass;
         }

         String[] classes = BaseUtil.getMavenMainClasses(project);
         if (classes.length == 0) {
         return null;
         }
         if (classes.length == 0 || classes.length > 1) {
         MavenMainClassCustomizer.customize(project);
         mainClass = dm.getInstanceProperties().getProperty(SuiteConstants.MAVEN_MAIN_CLASS_PROP);
         } else {
         mainClass = classes[0];
         }
         return mainClass;
         }

         }//class
         */
        protected static final class MavenContextAction extends AbstractAction { //implements ProgressListener {

            final String command;
            final private Lookup context;
            final Project serverProject;
            final Properties startProperties = new Properties();

            public MavenContextAction(String command, Lookup context) {
                this.context = context;
                this.command = command;
                FileObject fo = context.lookup(FileObject.class);
                serverProject = FileOwnerQuery.getOwner(fo);

                if (BaseUtil.isAntProject(serverProject)) {
                    setEnabled(false);
                } else {
                    setEnabled(true);
                }

                putValue(NAME, getName());

            }

            public static boolean isCommandSupported(String command) {
                boolean result = true;
                switch (command) {
                    case "developer":
                    case "rebuild-all":
                    case "clean":
                    case "build":
                    case "rebuild":
                        break;
                    default:
                        result = false;

                }
                return result;
            }

            private String getName() {
                String name = null;
                switch (command) {
                    case "developer":
                        name = "DEVELOPER_ACTION";
                        break;

                    case "rebuild-all":
                        name = "Rebuild All ( project and it's repo)";
                        break;

                    case "clean":
                        name = "Clean";
                        break;
                    case "build":
                        name = "Build";
                        break;
                    case "rebuild":
                        name = "Clean and Build";
                        break;
                }
                return name;
            }

            protected boolean isDummyAction() {
                return command.equals("developer");
            }

            protected void setCommonProperties() {
                if (isDummyAction()) {
                    return;
                }
                String target = "maven-build-goals";
                String goals = "unknown";

                switch (command) {
                    case "rebuild-all":
                        target = "maven-rebuild-all";
                        goals = "clean deploy:deploy-file install:install-file package";
                        break;

                    case "clean":
                        goals = "clean";
                        break;
                    case "build":
                        goals = "package";
                        break;
                    case "rebuild":
                        goals = "clean package";
                        break;
                }

                startProperties.setProperty(BaseAntTaskProgressObject.ANT_TARGET, target);
                startProperties.setProperty(BaseAntTaskProgressObject.WAIT_TIMEOUT, "0");
                startProperties.setProperty("goals", goals);
                setStartProperies();
            }

            protected void xml(Project p) {


                /*                FileObject buildimplFo = p.getProjectDirectory().getFileObject(BUILD_IMPL_XML);

                 try (InputStream is = buildimplFo.getInputStream();) {
                 //---------------------------------------------------------------------
                 // Pay attension tht third parameter. It's true to correctly 
                 // work with namespaces. If false then all namespaces will be lost
                 // For example:
                 // <j2seproject3:javac gensrcdir="${build.generated.sources.dir}"/>
                 // will be modified as follows:
                 // <javac gensrcdir="${build.generated.sources.dir}"/>
                 //---------------------------------------------------------------------
                 Document doc = XMLUtil.parse(new InputSource(is), false, true, null, null);
                 NodeList nl = doc.getDocumentElement().getElementsByTagName("import");
                 if (nl != null) {
                 for (int i = 0; i < nl.getLength(); i++) {
                 Element el = (Element) nl.item(i);
                 String fileAttr = el.getAttribute("file");
                 if (fileAttr == null) {
                 continue;
                 }
                 if (SERVER_BUILDXML_NAME.equals(el.getAttributeNode("file").getValue())) {
                 valid = true;
                 break;
                 }
                 }
                 }

                 } catch (IOException | DOMException | SAXException ex) {
                 LOG.log(Level.INFO, ex.getMessage());
                 }
                 return valid;
                 */
            }

            public void attr(Project p) {
                
                Project suite = SuiteManager.getServerSuiteProject(SuiteManager.getManager(p).getUri());
                SuiteNotifier notif = suite.getLookup().lookup(SuiteNotifier.class);
                DistributedWebAppManager man = DistributedWebAppManager.getInstance(p);
                notif.childrenChanged(man, null);
                
/*                FileObject pfo = p.getProjectDirectory();

                try {
                    Enumeration<String> en = p.getProjectDirectory().getAttributes();
                    while ( en.hasMoreElements()) {
                        String s = en.nextElement();
                        BaseUtil.out("ServerActions.attr attributeName=" + s);                        
                    }
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                    BaseUtil.out("ServerActions.attr EXCEPTION " + ex.getMessage());
                }
                Enumeration<String> en = pfo.getAttributes();
                while (en.hasMoreElements()) {
                    String nm = en.nextElement();
                    BaseUtil.out("attrname = " + nm + "; attr value)=" + pfo.getAttribute(nm));
                }

                MavenAuxConfig auxConfig = MavenAuxConfig.getInstance(p);
                BaseUtil.out("auxConfig.getAuxAttributeValue()=" + auxConfig.getAuxAttributeValue());
                BaseUtil.out("auxConfig.getActivatedProfile()=" + auxConfig.getActivatedProfile());
                BaseUtil.out("auxConfig.getNbactionCurrentPath()=" + auxConfig.getNbactionsActivatedPath());

                List<String> paths = auxConfig.getNbactionsPaths();
                paths.forEach(path -> {
                    BaseUtil.out("auxConfig.path=" + path);
                });
                List<String> args = auxConfig.getAllExecArgs();
                args.forEach(arg -> {
                    BaseUtil.out("SeverActions: arg=" + arg);
                });
                auxConfig.getJvmArgs().forEach(arg -> {
                    BaseUtil.out("SeverActions: jvm arg=" + arg);
                });
                auxConfig.getProgramArgs().forEach(arg -> {
                    BaseUtil.out("SeverActions: -D  arg =" + arg);
                });
                BaseUtil.out("MAIN CLASS " + auxConfig.getMainClass());

                BaseUtil.out("SourceVersion.isName(a.b.c.MyClass).isName=" + SourceVersion.isName("a.b.c.MyClass"));
                BaseUtil.out("SourceVersion.isName(a).isName=" + SourceVersion.isName("a"));
                BaseUtil.out("SourceVersion.isName(a-b).isName=" + SourceVersion.isName("a-b"));
                BaseUtil.out("SourceVersion.isName(${a}).isName=" + SourceVersion.isName("${a}"));
                BaseUtil.out("SourceVersion.isName(A$B).isName=" + SourceVersion.isName("A$B"));
                SourceVersion.isName("a.b.c.MyClass");
                //JavaPlatform.getDefault().
                //AuxiliaryConfiguration ac = (AuxiliaryConfiguration) pfo.getAttribute("AuxilaryConfiguration");
                AuxiliaryConfiguration ac = null;
                if (ac == null) {
                    return;
                }
                BaseUtil.out("AuxiliaryConfiguration toString= " + toString());

                Element el = ac.getConfigurationFragment(
                        "config-data", "http://www.netbeans.org/ns/maven-config-data/1", false);

                BaseUtil.out("attr NOT Shared Element = " + el);
                el = ac.getConfigurationFragment(
                        "config-data", "http://www.netbeans.org/ns/maven-config-data/1", true);

                BaseUtil.out("attr Shared Element = " + el);

                el = ac.getConfigurationFragment(
                        "config-data", "http://www.netbeans.org/ns/maven-config-data/1", false);
                BaseUtil.out("1) NOT Shared Element = " + el);
*/                        
            }
                        
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isDummyAction()) {
                    FileObject fo = serverProject.getProjectDirectory();
                    Project p = FileOwnerQuery.getOwner(fo);
                    ProjectUtils.getAuxiliaryConfiguration(p);
                    try {
                        p = ProjectManager.getDefault().findProject(fo);
                        BaseUtil.out("DUMMY ACION isModified()=" + ProjectManager.getDefault().isModified(p));
                        BaseUtil.out("DUMMY ACION = isValid()" + ProjectManager.getDefault().isValid(p));
                        ProjectManager.getDefault().saveProject(p);
                        attr(p);
                    } catch (IOException ex) {
                        BaseUtil.out("DUMMY ACION EXCEPTION ex=" + ex.getMessage());
                    } catch (IllegalArgumentException ex) {
                        BaseUtil.out("1 DUMMY ACION EXCEPTION ex=" + ex.getMessage());
                        Exceptions.printStackTrace(ex);
                    }

                    BaseUtil.out("DUMMY ACION project=" + p);

                    return;
                }
                setCommonProperties();
                setStartProperies();

                new BaseAntTaskProgressObject(null, startProperties).execute();

            }

            protected void setStartProperies() {
                if (isDummyAction()) {
                    return;
                }
                FileObject fo = serverProject.getProjectDirectory().getFileObject(SuiteConstants.INSTANCE_NBDEPLOYMENT_FOLDER + "/build.xml");
                startProperties.setProperty(BaseAntTaskProgressObject.BUILD_XML, fo.getPath());

                FileObject cmJar = SuiteUtil.getCommandManagerJar(serverProject);

                Properties pomProperties = BaseUtil.getPomProperties(cmJar);
                if (pomProperties != null) {

                    String str = pomProperties.getProperty("groupId");
                    str = str.replace(".", "/");
                    str += "/"
                            + pomProperties.getProperty("artifactId")
                            + "/"
                            + pomProperties.getProperty("version")
                            + "/"
                            + cmJar.getNameExt();

                    if (cmJar.getParent().getFileObject(str) == null) {
                        startProperties.setProperty("do.deploy-file", "yes");
                    }

                    startProperties.setProperty(SuiteConstants.COMMAND_MANAGER_GROUPID,
                            pomProperties.getProperty("groupId"));

                    startProperties.setProperty(SuiteConstants.COMMAND_MANAGER_ARTIFACTID,
                            pomProperties.getProperty("artifactId"));
                    startProperties.setProperty(SuiteConstants.COMMAND_MANAGER_VERSION,
                            pomProperties.getProperty("version"));
                    startProperties.setProperty(BaseConstants.COMMAND_MANAGER_JAR_NAME_PROP,
                            pomProperties.getProperty("artifactId") + "-"
                            + pomProperties.getProperty("version")
                            + ".jar"
                    );
                }

                startProperties.setProperty(SuiteConstants.MAVEN_REPO_LIB_PATH_PROP,
                        SuiteConstants.MAVEN_REPO_LIB_PATH);

                //
                // We set MAVEN_DEBUG_CLASSPATH_PROP. In future this approach may change
                //
//                properties.setProperty(SuiteConstants.MAVEN_DEBUG_CLASSPATH_PROP, cp);
                startProperties.setProperty(SuiteConstants.MAVEN_WORK_DIR_PROP, serverProject.getProjectDirectory().getPath());
                /*                String mainClass = BaseUtil.getMavenMainClass(serverProject);
                 if ( mainClass == null ) {
                 MavenAuxConfig_OLD mac = SuiteUtil.customizedMavenProject(serverProject);
                 mainClass = mac.getMainClass();
                 }
                 BaseUtil.out("ServerActions mainClass=" + mainClass);
                 if (mainClass != null) {
                 startProperties.setProperty(SuiteConstants.MAVEN_MAIN_CLASS_PROP, mainClass);
                 }
                 */
            }

            /*            protected String getMavenMainClass() {
             BaseDeploymentManager dm = SuiteManager.getManager(serverProject);
             String mainClass = dm.getInstanceProperties().getProperty(SuiteConstants.MAVEN_MAIN_CLASS_PROP);
             if (mainClass != null) {
             return mainClass;
             }

             String[] classes = BaseUtil.getMavenMainClasses(serverProject);
             if (classes.length == 0) {
             return null;
             }
             if (classes.length == 0 || classes.length > 1) {
             //                BaseDeploymentManager dm = SuiteManager.getManager(serverProject);
                    
             MavenMainClassCustomizer.customize(serverProject);
             mainClass = dm.getInstanceProperties().getProperty(SuiteConstants.MAVEN_MAIN_CLASS_PROP);
             } else {
             mainClass = classes[0];
             }
             return mainClass;
             }
             */
        }//class

        protected static final class AntContextAction extends AbstractAction { //implements ProgressListener {

            final private Lookup context;
            final Project serverProject;
            final String command;

            public AntContextAction(String command, Lookup context) {
                this.context = context;
                FileObject fo = context.lookup(FileObject.class);
                this.serverProject = FileOwnerQuery.getOwner(fo);
                this.command = command;
                if (BaseUtil.isAntProject(serverProject)) {
                    setEnabled(true);
                } else {
                    setEnabled(false);
                }

                putValue(NAME, getName());
            }

            @Override
            public void actionPerformed(ActionEvent e) {

                new BaseActionProviderExecutor().execute(command, serverProject);
            }

            public static boolean isCommandSupported(String command) {
                if (null == command) {
                    return false;
                }
                boolean result = true;

                switch (command) {
                    case "clean":
                    case "build":
                    case "rebuild":
                    default:
                        result = false;
                }
                return result;
            }

            private String getName() {
                String result = null;
                if (null != command) {
                    switch (command) {
                        case "clean":
                            result = "Clean";
                            break;
                        case "build":
                            result = "Build";
                            break;
                        case "rebuild":
                            result = "Clean and Build";
                            break;
                    }
                }
                return result;
            }

        }//class

    }//class

    public static class NewMavenProjectAction extends AbstractAction implements ContextAwareAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            assert false;
        }

        @Override
        public Action createContextAwareInstance(Lookup context) {
            return new NewMavenProjectAction.ContextAction(context);
        }

        public static Action getContextAwareInstance(Lookup context) {
            return new NewMavenProjectAction.ContextAction(context);
        }

        private static final class ContextAction extends InstanceWizardActionAsIterator { //implements ProgressListener {

            public ContextAction(Lookup context) {
                super(context);
                putValue(NAME, "&New Server Instance  as Maven Project");
            }

            @Override
            protected boolean isMavenBased() {
                return true;
            }
        }//class
    }//class

    public static class NewAntProjectAction extends AbstractAction implements ContextAwareAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            assert false;
        }

        @Override
        public Action createContextAwareInstance(Lookup context) {
            return new NewAntProjectAction.ContextAction(context);
        }

        public static Action getContextAwareInstance(Lookup context) {
            return new NewAntProjectAction.ContextAction(context);
        }

        private static final class ContextAction extends InstanceWizardActionAsIterator { //implements ProgressListener {

            public ContextAction(Lookup context) {
                super(context);
                putValue(NAME, "&New Server Instance  as Ant Project");
            }

            @Override
            protected boolean isMavenBased() {
                return false;
            }
        }//class
    }//class

    public static class AddExistingProjectAction extends AbstractAction implements ContextAwareAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            assert false;
        }

        /**
         *
         * @param context a Lookup of the ServerInstancesRootNode object
         * @return
         */
        @Override
        public Action createContextAwareInstance(Lookup context) {
            return new AddExistingProjectAction.ContextAction(context);
        }

        /**
         *
         * @param context a Lookup of the ServerInstancesRootNode object
         * @return
         */
        public static Action getContextAwareInstance(Lookup context) {
            return new AddExistingProjectAction.ContextAction(context);
        }

        private static final class ContextAction extends AbstractAction { //implements ProgressListener {

            private final RequestProcessor.Task task;
            private final Lookup context;

            public ContextAction(Lookup context) {
                this.context = context;
                putValue(NAME, "&Add  Existing Project");
                task = new RequestProcessor("AddBody").create(new Runnable() { // NOI18N
                    @Override
                    public void run() {
                        JFileChooser fc = ProjectChooser.projectChooser();
                        int choosed = fc.showOpenDialog(null);
                        if (choosed == JFileChooser.APPROVE_OPTION) {
                            File selectedFile = fc.getSelectedFile();
                            FileObject appFo = FileUtil.toFileObject(selectedFile);
                            String msg = ProjectFilter.check(appFo);
                            if (msg != null) {
                                NotifyDescriptor d
                                        = new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE);
                                DialogDisplayer.getDefault().notify(d);
                                return;
                            }

                            AddExistingProjectWizardActionAsIterator action
                                    = new AddExistingProjectWizardActionAsIterator(context, selectedFile);
                            action.doAction();

                        } else {
                            System.out.println("File access cancelled by user.");
                        }
                    }
                });

            }

            public @Override
            void actionPerformed(ActionEvent e) {
                task.schedule(0);

                if ("waitFinished".equals(e.getActionCommand())) {
                    task.waitFinished();
                }

            }
        }//class
    }//class

    public static class RemoveInstanceAction extends AbstractAction implements ContextAwareAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            assert false;
        }

        @Override
        public Action createContextAwareInstance(Lookup context) {
            return new RemoveInstanceAction.ContextAction(context);
        }

        public static Action getContextAwareInstance(Lookup context) {
            return new RemoveInstanceAction.ContextAction(context);
        }

        private static final class ContextAction extends AbstractAction { //implements ProgressListener {

            private Lookup context;

            public ContextAction(Lookup context) {
                this.context = context;
                putValue(NAME, "&Remove Server Instance");
                //dm = BaseUtils.managerOf(context);

            }

            @Override
            public void actionPerformed(ActionEvent e) {
                BaseDeploymentManager dm = context.lookup(ServerInstanceProperties.class).getManager();
                Project instanceProject = dm.getServerProject();

                if (instanceProject != null) {
                    ServerInstanceBuildExtender extender;
                    if (BaseUtil.isAntProject(instanceProject)) {
                        extender = new ServerInstanceAntBuildExtender(instanceProject);
                    } else {
                        extender = new ServerInstanceBuildExtender(instanceProject);
                    }

                    extender.disableExtender();
                }
                SuiteManager.removeInstance(context.lookup(ServerInstanceProperties.class).getUri());
            }
        }
    }
//        Utilities.getBuildExecutionSupportImplementation().    

    public static class InstancePropertiesAction extends AbstractAction implements ContextAwareAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            assert false;
        }

        @Override
        public Action createContextAwareInstance(Lookup context) {
            return new InstancePropertiesAction.ContextAction(context);
        }

        public static Action getContextAwareInstance(Lookup context) {
            return new InstancePropertiesAction.ContextAction(context);
        }

        private static final class ContextAction extends AbstractAction { //implements ProgressListener {

            private final Lookup context;
            private final RequestProcessor.Task task;

            public ContextAction(Lookup context) {
                this.context = context;
                putValue(NAME, "&Properties");
                task = new RequestProcessor("AddBody").create(new Runnable() { // NOI18N
                    @Override
                    public void run() {

                        CustomizerWizardActionAsIterator action
                                = new CustomizerWizardActionAsIterator(context, FileUtil.toFile(context.lookup(FileObject.class)));
                        action.actionPerformed(null);
                    }
                });

            }

            public @Override
            void actionPerformed(ActionEvent e) {
                task.schedule(0);

                if ("waitFinished".equals(e.getActionCommand())) {
                    task.waitFinished();
                }

            }
        }//class
    }

    public static class ProjectFilter {

        public static String check(FileObject appFo) {

            if (appFo == null) {
                return "Cannot be null";
            }
            String msg = "The selected project is not a Project ";
            Project proj = FileOwnerQuery.getOwner(appFo);
            if (proj == null) {
                return msg;
            }

            FileObject fo = proj.getProjectDirectory().getFileObject("nbproject/project.xml");
            if (fo != null && SuiteUtil.projectTypeByProjectXml(fo).equals(SuiteConstants.HTML5_PROJECTTYPE)) {
                return "The selected project is an Html5 Project ";
            }
            /*
             if (fo != null && SuiteUtil.projectTypeByProjectXml(fo).equals(SuiteConstants.WEB_PROJECTTYPE)) {
             return "The selected project is an Web Application";
             }
            
             if (BaseUtil.isMavenWebProject(proj)) {
             return "The selected project is a Maven  Web Application";
             }
             */
            if (SuiteManager.getManager(proj) != null) {
                return "The selected project allready registered as a Server Instance";
            }
            if (BaseUtil.isMavenProject(proj) || BaseUtil.isAntProject(proj)) {
                return null;
            }

            return "The selected project is a Maven  Web Application";
        }
    }

    public static class DefineMainClassAction extends AbstractAction implements ContextAwareAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            assert false;
        }

        /**
         *
         * @param context a Lookup of the ServerInstancesRootNode object
         * @return
         */
        @Override
        public Action createContextAwareInstance(Lookup context) {
            return new DefineMainClassAction.ContextAction(context);
        }

        /**
         *
         * @param context a Lookup of the ServerInstancesRootNode object
         * @return
         */
        public static Action getContextAwareInstance(Lookup context) {
            return new DefineMainClassAction.ContextAction(context);
        }

        private static final class ContextAction extends AbstractAction { //implements ProgressListener {

            private final RequestProcessor.Task task;
            private final Lookup context;

            //private static final String CANCEL = "CANCEL";
            private static final String NO_MAIN_CLASS_FOUND = "No Main Class Found";

            public ContextAction(Lookup context) {
                this.context = context;
                FileObject fo = context.lookup(FileObject.class);
                final Project instanceProject = FileOwnerQuery.getOwner(fo);

                if (BaseUtil.isAntProject(instanceProject)) {
                    setEnabled(false);
                } else {
                    setEnabled(true);
                }

                putValue(NAME, "&Assign Main Class");
                putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, BaseUtil.isAntProject(instanceProject));

                task = new RequestProcessor("AddBody").create(new Runnable() { // NOI18N
                    @Override
                    public void run() {
                        JButton sb = createSelectButton();
                        JButton cb = createCancelButton();

                        // MainClassChooserPanelVisual panel = new MainClassChooserPanelVisual(sb,cb);
                        MainClassChooserPanelVisual panel = new MainClassChooserPanelVisual(sb, cb);
                        panel.setServerProject(instanceProject);
                        String[] classes = BaseUtil.getMavenMainClasses(instanceProject);

                        if (classes.length == 0) {
                            classes = new String[]{NO_MAIN_CLASS_FOUND};
                            sb.setEnabled(false);
                        }

                        panel.getMainClassesList().setListData(classes);
                        String msg = "Select Main Class for Server Execution";
                        DialogDescriptor dd = new DialogDescriptor(panel, msg,
                                true, new Object[]{sb, cb}, cb, DialogDescriptor.DEFAULT_ALIGN, null, null);
//                                true, new Object[]{"Select Main Class", "Cancel"}, "Cancel", DialogDescriptor.DEFAULT_ALIGN, null, null);

                        DialogDisplayer.getDefault().notify(dd);

                        if (dd.getValue() == sb) {
                            int idx = panel.getMainClassesList().getSelectedIndex();
                            if (idx < 0) {
                                return;
                            }
                            String mainClass = (String) panel.getMainClassesList().getSelectedValue();
                            String uri = SuiteManager.getManager(instanceProject).getUri();
                            InstanceProperties.getInstanceProperties(uri)
                                    .setProperty(SuiteConstants.MAVEN_MAIN_CLASS_PROP, mainClass);

                        }

                    }
                });
            }

            protected JButton createSelectButton() {
                JButton button = new javax.swing.JButton();
                button.setName("SELECT");
                org.openide.awt.Mnemonics.setLocalizedText(button, "Select Main Class");
                button.setEnabled(false);
                return button;

            }

            protected JButton createCancelButton() {
                JButton button = new javax.swing.JButton();
                button.setName("CANCEL");
                org.openide.awt.Mnemonics.setLocalizedText(button, "Cancel");
                return button;
            }

            public @Override
            void actionPerformed(ActionEvent e) {
                task.schedule(0);

                if ("waitFinished".equals(e.getActionCommand())) {
                    task.waitFinished();
                }

            }
        }//class
    }//class

}//class
