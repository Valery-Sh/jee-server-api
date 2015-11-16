package org.netbeans.modules.jeeserver.base.embedded.project.nodes.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static javax.swing.Action.NAME;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.base.deployment.ServerInstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.actions.StartServerAction;
import org.netbeans.modules.jeeserver.base.deployment.actions.StopServerAction;
import org.netbeans.modules.jeeserver.base.deployment.progress.BaseActionProviderExecutor;
import org.netbeans.modules.jeeserver.base.deployment.progress.BaseAntTaskProgressObject;

import org.netbeans.modules.jeeserver.base.deployment.specifics.StartServerPropertiesProvider;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.netbeans.modules.jeeserver.base.embedded.apisupport.ApiDependency;
import org.netbeans.modules.jeeserver.base.embedded.apisupport.SupportedApi;
import org.netbeans.modules.jeeserver.base.embedded.apisupport.SupportedApiProvider;
import org.netbeans.modules.jeeserver.base.deployment.utils.PomXmlUtil;
import org.netbeans.modules.jeeserver.base.deployment.utils.PomXmlUtil.Dependencies;
import org.netbeans.modules.jeeserver.base.deployment.utils.PomXmlUtil.Dependency;
import org.netbeans.modules.jeeserver.base.deployment.utils.PomXmlUtil.PomProperties;
import org.netbeans.modules.jeeserver.base.embedded.project.SuiteManager;
import org.netbeans.modules.jeeserver.base.embedded.project.nodes.SuiteNotifier;
import org.netbeans.modules.jeeserver.base.embedded.project.wizard.AddDependenciesPanelVisual;
import org.netbeans.modules.jeeserver.base.embedded.project.wizard.CustomizerWizardActionAsIterator;
import org.netbeans.modules.jeeserver.base.embedded.project.wizard.AddExistingProjectWizardActionAsIterator;
import org.netbeans.modules.jeeserver.base.embedded.project.wizard.DownloadJarsPanelVisual;
import org.netbeans.modules.jeeserver.base.embedded.project.wizard.ServerInstanceAntBuildExtender;
import org.netbeans.modules.jeeserver.base.embedded.project.wizard.InstanceWizardActionAsIterator;
import org.netbeans.modules.jeeserver.base.embedded.project.wizard.MainClassChooserPanelVisual;
import org.netbeans.modules.jeeserver.base.embedded.project.wizard.ServerInstanceBuildExtender;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteConstants;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteUtil;
import org.netbeans.modules.jeeserver.base.embedded.webapp.DistributedWebAppManager;
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

/**
 *
 * @author V. Shyshkin
 */
public class ServerActions {

    private static final Logger LOG = Logger.getLogger(ServerActions.class.getName());

    @StaticResource
    private static final String COPY_BUILD_XML = "org/netbeans/modules/jeeserver/base/embedded/resources/maven-copy-api-build.xml";

    public static class StartStopAction {

        public static Action getAction(String type, Lookup context) {
            FileObject fo = context.lookup(FileObject.class);
            Project serverProject = FileOwnerQuery.getOwner(fo);
            Properties props = null;
            if (!BaseUtil.isAntProject(serverProject) && (needsBuildProject(serverProject))) {
                //|| needsBuildRepo(serverProject))) {
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

    }
    public static class StartJarAction {

        public static Action getAction(String type, Lookup context) {
            FileObject fo = context.lookup(FileObject.class);
            Project serverProject = FileOwnerQuery.getOwner(fo);
            Properties props = null;
            if (!BaseUtil.isAntProject(serverProject)) {
                //|| needsBuildRepo(serverProject))) {
                props = new Properties();
                props.setProperty(StartServerAction.ACTION_ENABLED_PROP, "true");
            }
            if ("start-jar".equals(type)) {
                return new StartServerAction().createContextAwareInstance(context, props);
            } else {
                return new StopServerAction().createContextAwareInstance(context);
            }
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

                FileObject fo = SuiteManager.getManager(serverProject)
                        .getLookup()
                        .lookup(StartServerPropertiesProvider.class)
                        .getBuildXml(serverProject);

                startProperties.setProperty(BaseAntTaskProgressObject.BUILD_XML, fo.getPath());
                startProperties.setProperty(SuiteConstants.BASE_DIR_PROP, serverProject.getProjectDirectory().getPath());
                //
                // We set MAVEN_DEBUG_CLASSPATH_PROP. In future this approach may change
                //
//                properties.setProperty(SuiteConstants.MAVEN_DEBUG_CLASSPATH_PROP, cp);
                startProperties.setProperty(SuiteConstants.MAVEN_WORK_DIR_PROP, serverProject.getProjectDirectory().getPath());
            }

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

                        // MainClassChooserPanelVisual panel = new MainClassChooserPanelVisual(db,cb);
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

    public static class DownLoadJarsAction extends AbstractAction implements ContextAwareAction {

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
            return new DownLoadJarsAction.ContextAction(context);
        }

        /**
         *
         * @param context a Lookup of the ServerInstancesRootNode object
         * @return
         */
        public static Action getContextAwareInstance(Lookup context) {
            return new DownLoadJarsAction.ContextAction(context);
        }

        private static final class ContextAction extends AbstractAction { //implements ProgressListener {

            private final RequestProcessor.Task task;
            private final Lookup context;
            private final Project instanceProject;

            public ContextAction(Lookup context) {
                this.context = context;
                FileObject fo = context.lookup(FileObject.class);
                instanceProject = FileOwnerQuery.getOwner(fo);

                if (BaseUtil.isAntProject(instanceProject)) {
                    setEnabled(true);
                } else {
                    setEnabled(false);
                }

                putValue(NAME, "&Download jars");
                putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);

                task = new RequestProcessor("AddBody").create(new Runnable() { // NOI18N
                    @Override
                    public void run() {
                        JButton db = createDownloadButton();
                        JButton cb = createCancelButton();

                        // MainClassChooserPanelVisual panel = new MainClassChooserPanelVisual(db,cb);
                        DownloadJarsPanelVisual panel = new DownloadJarsPanelVisual(instanceProject, db, cb);

                        DialogDescriptor dd = new DialogDescriptor(panel, "Select API and download jars",
                                true, new Object[]{db, cb}, cb, DialogDescriptor.DEFAULT_ALIGN, null, null);
//                                true, new Object[]{"Select Main Class", "Cancel"}, "Cancel", DialogDescriptor.DEFAULT_ALIGN, null, null);

                        DialogDisplayer.getDefault().notify(dd);

                        if (dd.getValue() == db) {
                            int idx = panel.getSelectedApiComboBox().getSelectedIndex();
                            if (idx <= 0) {
                                return;
                            }
                            SupportedApi api = panel.getApiList().get(idx - 1);
                            List<ApiDependency> apiDeps = api.getDependencies();
                            if (apiDeps.isEmpty()) {
                                return;
                            }
                            apiDeps.forEach(d -> {
                                BaseUtil.out("DownloadJarAction: dependency: " + d.getJarName());
                                BaseUtil.out("DownloadJarAction: groupId: " + d.getGroupId());
                                BaseUtil.out("DownloadJarAction: artifactId: " + d.getArtifacId());
                                BaseUtil.out("DownloadJarAction: version: " + d.getVersion());

                            });
                            createPom(api, panel.getTargetFolder());
                        }

                    }
                });
            }

            protected void createPom(SupportedApi api, String copyToDir) {
                SupportedApiProvider provider = SupportedApiProvider.getInstance(SuiteUtil.getActualServerId(instanceProject));
                InputStream is = provider.getDownloadPom(api);
                PomXmlUtil pomSupport = new PomXmlUtil(is);
                PomProperties props = pomSupport.getProperties();

                String serverVersion = SuiteManager
                        .getManager(instanceProject)
                        .getInstanceProperties()
                        .getProperty(BaseConstants.SERVER_VERSION_PROP);

                Map<String, String> map = provider.getServerVersionProperties(serverVersion);
                map.put("target.directory", copyToDir);
                props.replaceAll(map);

                Path target = SuiteUtil.createTempDir(instanceProject, "downloads");
                Dependencies deps = pomSupport.getDependencies();
                api.getDependencies().forEach(d -> {
                    Dependency dep = new Dependency(d.getGroupId(), d.getArtifacId(), d.getVersion());
                    dep.setTags(d.getOtherTags());
                    deps.add(dep);
                });
                //Dependency dep = new Dependency();
                pomSupport.save(target, "pom.xml");
                //
                // copy build.xml
                //
                InputStream buildIS = getClass().getClassLoader().getResourceAsStream(COPY_BUILD_XML);
                try {
                    Files.copy(buildIS, Paths.get(target.toString(), "build.xml"), StandardCopyOption.REPLACE_EXISTING);
                    copyJars(target);
                } catch (IOException ex) {
                    LOG.log(Level.INFO, ex.getMessage());

                }

            }

            protected void copyJars(Path basedir) {
                Properties execProps = new Properties();
                basedir.resolve("build.xml").toString();
                BaseUtil.out("****  COPY JARS " + basedir.resolve("build.xml").toString());
                String buildXmlPath = basedir.resolve("build.xml").toString();
                execProps.setProperty("build.xml", buildXmlPath);
                execProps.setProperty(BaseAntTaskProgressObject.WAIT_TIMEOUT, "0");
                execProps.setProperty("goals", "package");

                execProps.setProperty(SuiteConstants.BASE_DIR_PROP, basedir.toString());
                execProps.setProperty(SuiteConstants.MAVEN_WORK_DIR_PROP, basedir.toString());

                execProps.setProperty(BaseAntTaskProgressObject.ANT_TARGET, "maven-build-goals");

                BaseAntTaskProgressObject task = new BaseAntTaskProgressObject(null, execProps);
                task.execute();
            }

            protected JButton createDownloadButton() {
                JButton button = new javax.swing.JButton();
                button.setName("SELECT");
                org.openide.awt.Mnemonics.setLocalizedText(button, "Download jars");
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
        }

    }//class DownloadJaesAction

    public static class AddDependenciesAction extends AbstractAction implements ContextAwareAction {

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
            return new AddDependenciesAction.ContextAction(context);
        }

        /**
         *
         * @param context a Lookup of the ServerInstancesRootNode object
         * @return
         */
        public static Action getContextAwareInstance(Lookup context) {
            return new AddDependenciesAction.ContextAction(context);
        }

        private static final class ContextAction extends AbstractAction { //implements ProgressListener {

            private final RequestProcessor.Task task;
            private final Lookup context;
            private final Project instanceProject;

            public ContextAction(Lookup context) {
                this.context = context;
                FileObject fo = context.lookup(FileObject.class);
                instanceProject = FileOwnerQuery.getOwner(fo);

                if (!BaseUtil.isAntProject(instanceProject)) {
                    setEnabled(true);
                } else {
                    setEnabled(false);
                }

                putValue(NAME, "&Add Specific Dependencies");
                putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);

                task = new RequestProcessor("AddBody").create(new Runnable() { // NOI18N
                    @Override
                    public void run() {
                        JButton db = createAddDependenciesButton();
                        JButton cb = createCancelButton();

                        // MainClassChooserPanelVisual panel = new MainClassChooserPanelVisual(db,cb);
                        AddDependenciesPanelVisual panel = new AddDependenciesPanelVisual(instanceProject, db, cb);

                        DialogDescriptor dd = new DialogDescriptor(panel, "Select API and download jars",
                                true, new Object[]{db, cb}, cb, DialogDescriptor.DEFAULT_ALIGN, null, null);
//                                true, new Object[]{"Select Main Class", "Cancel"}, "Cancel", DialogDescriptor.DEFAULT_ALIGN, null, null);

                        DialogDisplayer.getDefault().notify(dd);

                        if (dd.getValue() == db) {
                            int idx = panel.getSelectedApiComboBox().getSelectedIndex();
                            if (idx <= 0) {
                                return;
                            }
                            SupportedApi api = panel.getApiList().get(idx - 1);
                            List<ApiDependency> apiDeps = api.getDependencies();
                            if (apiDeps.isEmpty()) {
                                return;
                            }
                            createPom(api);
                        }

                    }
                });
            }

            protected void createPom(SupportedApi api) {
                SupportedApiProvider provider = SupportedApiProvider.getInstance(SuiteUtil.getActualServerId(instanceProject));
                try (InputStream is = instanceProject.getProjectDirectory()
                        .getFileObject("pom.xml")
                        .getInputStream();) 
                {

                    PomXmlUtil pomSupport = new PomXmlUtil(is);
                    PomProperties props = pomSupport.getProperties();

                    String serverVersion = SuiteManager
                            .getManager(instanceProject)
                            .getInstanceProperties()
                            .getProperty(BaseConstants.SERVER_VERSION_PROP);

                    Map<String, String> map = provider.getServerVersionProperties(serverVersion);

                    props.replaceAll(map);
                    Path target = Paths.get(instanceProject.getProjectDirectory().getPath());
                    Dependencies deps = pomSupport.getDependencies();
                    api.getDependencies().forEach(d -> {
                        Dependency dep = new Dependency(d.getGroupId(), d.getArtifacId(), d.getVersion());
                        dep.setTags(d.getOtherTags());
                        deps.delete(dep);
                        deps.add(dep);
                    });
                    pomSupport.save(target, "pom.xml");
                } catch (IOException ex) {
                    LOG.log(Level.INFO, ex.getMessage());
                }

            }

            protected JButton createAddDependenciesButton() {
                JButton button = new javax.swing.JButton();
                button.setName("SELECT");
                org.openide.awt.Mnemonics.setLocalizedText(button, "Add dependencies");
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
        }

    }//class AddDependenciesAction

}
