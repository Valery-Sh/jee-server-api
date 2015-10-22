package org.netbeans.modules.jeeserver.base.embedded.webapp.actions;

import org.netbeans.modules.jeeserver.base.deployment.actions.CommandActionProgress;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static javax.swing.Action.NAME;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.jeeserver.base.embedded.specifics.EmbeddedServerSpecifics;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.Copier;
import org.netbeans.modules.jeeserver.base.embedded.utils.EmbPackageUtils;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteUtil;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.netbeans.spi.project.ActionProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.Presenter;
import org.openide.windows.IOColorLines;
import org.openide.windows.IOColors;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 * The class provides actions that allow to package web applications in a
 * variety of formats.
 *
 * @author V. Shyshkin
 */
@ActionID(
        category = "Project",
        id = "org.netbeans.modules.embedded.actions.PackageMainAction")
@ActionRegistration(
        asynchronous = true,
        displayName = "#CTL_PackageMainAction",lazy=false)
@ActionReference(path = "Projects/Actions", position = 0)
@NbBundle.Messages("CTL_PackageMainAction=Package Embedded Server as")
public class PackageMainAction extends AbstractAction implements ContextAwareAction {

    private static final Logger LOG = Logger.getLogger(PackageMainAction.class.getName());

    public static final String PACKAGE_SINGLE_JAR = "Into server jar";
    public static final String PACKAGE_WARS = "War archives to folder";
    public static final String PACKAGE_UNPACKED_WARS = "Unpacked war archives to folder";

    public static final String PACKAGE_WARS_TEMP = "package-wars-temp";

    /**
     * Never called.
     *
     * @param e
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        assert false;
    }

    /**
     * Creates an action for the given context. The created action implements
     * the {@literal Presenter.Popup} interface.
     *
     * @param context a lookup that contains the server project instance of type
     * {@literal Project}.
     * @return a new instance of type {@link #ContextAction}
     */
    @Override
    public Action createContextAwareInstance(Lookup context) {
        return new PackageMainAction.ContextAction(context);
    }

    private static final class ContextAction extends AbstractAction implements Presenter.Popup {

        private final Lookup context;

        public ContextAction(Lookup context) {

            this.context = context;
            Project project = context.lookup(Project.class);
            boolean isEmbedded = SuiteUtil.isEmbedded(project);
            setEnabled(isEmbedded);
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, !isEmbedded);
            putValue(NAME, "&Package...");

        }

        public @Override
        void actionPerformed(ActionEvent e) {
        }

        /**
         * Create an instance of {@literal javax.swing.JMenu } with a list of {@literal javax.swing.JMenuItem
         * }.
         *
         * @return an object of type {@literal javax.swing.JMenu}.
         */
        @Override
        public JMenuItem getPopupPresenter() {
            Project project = context.lookup(Project.class);
            boolean isEmbedded = SuiteUtil.isServerProject(project);
            JMenu result = new JMenu("Package...");  //remember JMenu is a subclass of JMenuItem
            result.add(new JMenuItem(new SingleJarAction(context)));
            result.add(new JMenuItem(new WarsToFolderAction(context)));
            result.add(new JMenuItem(new UnpackedWarsToFolderAction(context)));
            result.setEnabled(isEmbedded);
            result.setVisible(isEmbedded);
            return result;
        }
    }//class

    protected static void doAction() {

    }

    protected static int rebuildWebProjects(final CommandActionProgress[] caps, final InputOutput io) {
        int result = 0;
        int counter = caps == null ? 0 : caps.length;
        while (counter > 0) {
            if (caps != null) {
                for (CommandActionProgress cap : caps) {
                    if (cap == null) {
                        continue;
                    }
                    if (cap.isFinished() && !cap.isChecked()) {
                        counter--;
                        cap.setChecked(true);
                        String msg = "The project " + cap.getProject().getProjectDirectory().getNameExt() + " built successfully. " + (new Date());
                        io.getOut().println(msg);
                        //}
                    }
                }
            }

            if (counter > 0) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                }
            }
        }//while

        counter = 0;
        for (CommandActionProgress cap : caps) {
            if (cap.isFailed()) {
                String msg = "An error found when rebuild the project " + cap.getProject().getProjectDirectory().getNameExt() + ". " + (new Date());
                io.getErr().println(msg);
                counter++;
            }
        }
        if (counter > 0) {
            String msg = counter + " errors found when rebuild web projects. " + (new Date());
            io.getErr().println(msg);
            result = -1;
        }

        return result;
    }

    protected static void outputResult(final InputOutput io, final ActionResult actionResult, final RequestProcessor.Task task) {
        RequestProcessor rp = new RequestProcessor();
        rp.post(new Runnable() {

            @Override
            public void run() {
                if (task != null) {
                    task.waitFinished();
                }
                IOColors.OutputType ot = IOColors.OutputType.LOG_SUCCESS;
                String msg = "DISTRIBUTION PACKAGE BUILD SUCCESS";
                if (actionResult.getResult() != 0) {
                    ot = IOColors.OutputType.LOG_FAILURE;
                    msg = "DISTRIBUTION PACKAGE BUILD FAILED";
                }
                io.getOut().println("-----------------------------------------------------");
                if (IOColorLines.isSupported(io)) {
                    try {
                        IOColorLines.println(io, msg, IOColors.getColor(io, ot));
                    } catch (IOException ex) {
                        LOG.log(Level.INFO, ex.getMessage());
                    }
                } else {
                    io.getOut().println(msg);
                }
                io.getOut().println("-----------------------------------------------------");
                if (actionResult.getResult() == 0) {
                    io.select();
                }
            }
        });
    }

    protected static void outputResult(final InputOutput io, final ExecutorTask task) {
        RequestProcessor rp = new RequestProcessor();
        rp.post(new Runnable() {

            @Override
            public void run() {
                int result = task.result();
                IOColors.OutputType ot = IOColors.OutputType.LOG_SUCCESS;
                String msg = "DISTRIBUTE PACKAGE BUILD SUCCESS";
                if (result != 0) {
                    ot = IOColors.OutputType.LOG_FAILURE;
                    msg = "DISTRIBUTE PACKAGE BUILD FAILED";
                }
                io.getOut().println("-----------------------------------------------------");
                if (IOColorLines.isSupported(io)) {
                    try {
                        IOColorLines.println(io, msg, IOColors.getColor(io, ot));
                    } catch (IOException ex) {
                        LOG.log(Level.INFO, ex.getMessage());
                    }
                } else {
                    io.getOut().println(msg);
                }
                io.getOut().println("-----------------------------------------------------");
                if (result == 0) {
                    io.select();
                }
            }
        });
    }

    protected static void notifyInvalidProjects(List<String> msgs) {
        String notifyMsg = NbBundle.getMessage(PackageMainAction.class,
                "MSG_Invalid_Projects")
                + ":" + System.lineSeparator();
        for (String msg : msgs) {
            notifyMsg += "\t" + msg + System.lineSeparator();
        }
        notifyMsg += NbBundle.getMessage(PackageMainAction.class,
                "MSG_Fix_and_try_again");

        NotifyDescriptor nd = new NotifyDescriptor.Message(
                notifyMsg,
                NotifyDescriptor.ERROR_MESSAGE);
        nd.setTitle(NbBundle.getMessage(PackageMainAction.class,
                "TITLE_Invalid_Projects"));
        DialogDisplayer.getDefault().notify(nd);

    }

    protected static void notifyRebuldProjects(List<String> msgs) {
        String notifyMsg = NbBundle.getMessage(PackageMainAction.class,
                "MSG_Rebuild_Projects")
                + ":" + System.lineSeparator();
        for (String msg : msgs) {
            notifyMsg += "\t" + msg + System.lineSeparator();
        }
        notifyMsg += NbBundle.getMessage(PackageMainAction.class,
                "MSG_Fix_and_try_again");

        NotifyDescriptor nd = new NotifyDescriptor.Message(
                notifyMsg,
                NotifyDescriptor.ERROR_MESSAGE);
        nd.setTitle(NbBundle.getMessage(PackageMainAction.class,
                "TITLE_Rebuild_Projects"));
        DialogDisplayer.getDefault().notify(nd);

    }

    protected static void notifyNotUnzipedWars(List<String> msgs) {
        String notifyMsg = NbBundle.getMessage(PackageMainAction.class,
                "MSG_Not_unzipped_wars")
                + ":" + System.lineSeparator();
        for (String msg : msgs) {
            notifyMsg += "\t" + msg + System.lineSeparator();
        }
        notifyMsg += NbBundle.getMessage(PackageMainAction.class,
                "MSG_Fix_and_try_again");

        NotifyDescriptor nd = new NotifyDescriptor.Message(
                notifyMsg,
                NotifyDescriptor.ERROR_MESSAGE);
        nd.setTitle(NbBundle.getMessage(PackageMainAction.class,
                "TITLE_Not_unzipped_wars"));
        DialogDisplayer.getDefault().notify(nd);
    }

    protected static void notifyError(final String title, final String notifyMsg) {

        NotifyDescriptor nd = new NotifyDescriptor.Message(
                notifyMsg,
                NotifyDescriptor.ERROR_MESSAGE);
        nd.setTitle(title);

        DialogDisplayer.getDefault().notify(nd);
    }

    protected static Object notifyWarsToFolder(String folderName) {
        String msg = "<html>Change,  if necessary,  the name of the folder,<br/> where files are to be placed: </html> ";
        String msg1 = "Press Cancel to stop packaging. ";

        NotifyDescriptor.InputLine nd = new NotifyDescriptor.InputLine(
                // "Web Apps Target Folder Name: ", "Change Folder Name",

                msg, "Change FolderName",
                NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.PLAIN_MESSAGE);

        nd.setInputText(folderName);
        nd.createNotificationLineSupport().setInformationMessage(msg1);
        //nd.setMessage(" Press Cancel to stop packaging. ");
        Object result = DialogDisplayer.getDefault().notify(nd);
        if (result == NotifyDescriptor.CANCEL_OPTION) {
            result = null;
        } else {
            result = nd.getInputText();
        }
        return result;
    }

    protected static class SingleJarAction extends AbstractAction {

        protected final Lookup context;

        public SingleJarAction(Lookup context) {
            this.context = context;
            Project project = context.lookup(Project.class);
            String name = ProjectUtils.getInformation(project).getDisplayName();
            // TODO state for which projects action should be enabled
            boolean isEmbedded = SuiteUtil.isServerProject(project);
            // we need to hide when disabled putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);            
            setEnabled(isEmbedded);
            // we need to hide when disabled putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);            
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, !isEmbedded);
            putValue(NAME, "Into Server Jar");

        }

        @Override
        public void actionPerformed(final ActionEvent e) {

            String serverName = context.lookup(Project.class).getProjectDirectory().getName();

            InputOutput io = IOProvider.getDefault().getIO("Package Apps (package-dist)", false);
            io.getOut().println();
            io.getOut().println("*** Packaging embedded server " + serverName + ". Time: " + new Date() + " ***");
            ActionResult r = new ActionResult();
            doAction(e, io, r);
        }

        private void doAction(ActionEvent e, final InputOutput io, final ActionResult actionResult) {
            final Project serverProject = context.lookup(Project.class);
            final String serverDirPath = serverProject.getProjectDirectory().getPath();
            io.getOut().println("ACTION source=" + e.getSource().getClass().getName());
            String serverName = serverProject.getProjectDirectory().getName();
            io.select();

            io.getOut().println("--------- Server Project " + serverName + " Packaging in a jar ---------- " + (new Date()));

            EmbPackageUtils.deleteWebBuildDirs(serverProject);

            io.getOut().println("Delete web projects build dirs success. " + (new Date()));
            try {
                File f = Paths.get(serverDirPath, SuiteConstants.PACKAGE_DIST).toFile();                
                boolean b = true;
                if ( f.exists() ) {
                    b = Copier.delete(Paths.get(serverDirPath, SuiteConstants.PACKAGE_DIST).toFile(), false);
                }
                if (b) {
                    io.getOut().println("Delete the package-dist directory success. " + (new Date()));
                } else {
                    //io.getErr().println("Delete the package-dist failed but packaging may continue.");
                    io.getErr().println("Delete the package-dist failed. The folder may be in use by another process.");                    
                    actionResult.setResult(-1);
                    outputResult(io, actionResult, null);
                    //notifyError("Delete package-dist","Can't delete the package-dist. May be in use by another process");
                    return;                    
                }

            } catch (Exception ex) {
                io.getErr().println("Delete package-dist failed. Exception " + ex.getMessage());
            }

            //ESPackageUtils.deletePackageDist(serverProject);
            final List<String> msgs = EmbPackageUtils.vaidateWebProjects(serverProject);

            if (!msgs.isEmpty()) {
                for (String msg : msgs) {
                    io.getErr().println("Validate web projects. " + msg + ".  " + (new Date()));
                }
                actionResult.setResult(-1);
                notifyInvalidProjects(msgs);
                return;
            }
            io.getOut().println("Validate web projects success. " + (new Date()));

            final CommandActionProgress[] caps = BatchActionCommand.invokeAction(serverProject, ActionProvider.COMMAND_BUILD);

            /*
             * The task ctarts actions and monitors their processes
             */
            final RequestProcessor.Task task = new RequestProcessor().post(new Runnable() {
                @Override
                public void run() {

                    actionResult.setResult(rebuildWebProjects(caps, io));
                    /*
                     Now the server project and all web projects are built.
                     Next steps: 
                     1. Delete if exists the package-dist directory
                     which is located at the root of the server directory.
                     2. Create a new package-dist directory.   
                     3. Copy a server jar file to the package-dist directory.
                     4. Find all web projects and add them to a specified 
                     List collection.
                     5. Copy a server properties file embedded-server.properties    
                     into the root of the server jar 
                     6. Copy the content of each war file of the 
                     projects in the collection into the server jar entry
                     named web-apps-pack
                     */

                    io.select();

                    Path projPath = Paths.get(serverProject.getProjectDirectory().getPath());
                    Path packageDistPath = projPath.resolve(SuiteConstants.PACKAGE_DIST);
                    String serverJarName = projPath.getFileName() + ".jar";
                    File serverJar = packageDistPath.resolve(serverJarName).toFile();
                    //File serverProps = projPath.resolve(SuiteConstants.INSTANCE_PROPERTIES_PATH).toFile();
                    File serverProps = null;

                    try {

                        Copier copier = new Copier(projPath.resolve("dist").toFile());
                        copier.copyTo(packageDistPath.toFile());

                        //serverJarPath = packageDistPath.resolve(serverJar.getName());
                        io.getOut().println("Copy " + serverJar.getName() + " file to package-dist directory success. " + new Date());

                        Copier.ZipUtil.copy(serverProps, serverJar);

                        io.getOut().println("Copy " + serverProps.getName() + " file to ../package-dist/" + serverJarName + " success. " + new Date());

                    } catch (Exception ex) {
                        LOG.log(Level.INFO, ex.getMessage());
                        io.getOut().println("Exception " + ex.getMessage());
                    }

                    packageApplications(io, actionResult);
                }

            });
            outputResult(io, actionResult, task);
        }

        protected File getWebappsFolder(Project serverProject) {
            //Properties props = SuiteUtil.loadServerProperties(serverProject);
            Properties props = null;            

            String warsFolderName = props.getProperty(SuiteConstants.WEBAPPS_DIR_PROP);
            if (warsFolderName == null) {
                warsFolderName = "web-apps";
                
            }
            return FileUtil.toFile(serverProject.getProjectDirectory()).toPath().resolve(SuiteConstants.PACKAGE_DIST).resolve(warsFolderName).toFile();
        }

        protected boolean updateWebappsFolder(Project serverProject) {
            //Properties props = SuiteUtil.loadServerProperties(serverProject);
            Properties props = null;

            String warsFolderName = props.getProperty(SuiteConstants.WEBAPPS_DIR_PROP);
            String newFolderName = warsFolderName;
            if (warsFolderName == null) {
                newFolderName = SuiteConstants.WEBAPPS_DEFAULT_DIR_NAME;
                Object o = notifyWarsToFolder(newFolderName);
                if (o == null) {
                    return false; // cancel
                }
                newFolderName = o.toString();
            }
            if (newFolderName != null && !newFolderName.equals(warsFolderName)) {
                props.setProperty(SuiteConstants.WEBAPPS_DIR_PROP, newFolderName);
                FileObject target = serverProject.getProjectDirectory().getFileObject(SuiteConstants.REG_WEB_APPS_FOLDER);
//                SuiteUtil.updateProperties(props, target, SuiteConstants.INSTANCE_PROPERTIES_FILE);
            }
            return true;
        }

        protected void packageApplications(final InputOutput io, final ActionResult actionResult) {
            final Project serverProject = context.lookup(Project.class);
            Path projPath = Paths.get(serverProject.getProjectDirectory().getPath());
            String serverJarName = projPath.getFileName() + ".jar";
            File serverJar = projPath.resolve(SuiteConstants.PACKAGE_DIST).resolve(serverJarName).toFile();

            io.select();

            List<FileObject> warFiles = EmbPackageUtils.getWarFiles(serverProject);
            io.getOut().println("Unzip war files. " + (new Date()));

            List<FileObject> notUnzipped = new ArrayList<>();

            for (FileObject fo : warFiles) {
                String warName = fo.getNameExt(); // with ext
                EmbeddedServerSpecifics specifics = (EmbeddedServerSpecifics) BaseUtil.managerOf(context).getSpecifics();
                if (specifics.supportsDistributeAs(SuiteConstants.DistributeAs.SINGLE_JAR_WARS)) {
                    Copier copier = new Copier(FileUtil.toFile(fo), io);
                    if (!copier.copyToZip(serverJar, SuiteConstants.WEB_APPS_PACK)) {
                        notUnzipped.add(fo);
                        break;
                    }
                } else {
                    warName = fo.getName(); // without ext
                    if (!Copier.ZipUtil.copyZipToZip(FileUtil.toFile(fo),
                            serverJar, SuiteConstants.WEB_APPS_PACK + "/" + warName)) {
                        notUnzipped.add(fo);
                        break;
                    }
                }
                io.getOut().println("Copy " + warName + " file to ../package-dist/" + serverJar.getName() + "/web-apps.pack success. " + new Date());                    
            }
            

/*            for (FileObject fo : warFiles) {
                String warName = fo.getName(); // without ext
                if (!Copier.ZipUtil.copyZipToZip(FileUtil.toFile(fo),
                        serverJar, SuiteConstants.WEB_APPS_PACK + "/" + warName)) {
                    notUnzipped.add(fo);
                    break;
                }
                io.getOut().println("Copy " + warName + " file to ../package-dist/" + serverJar.getName() + "/web-apps.pack success. " + new Date());
            }
*/
            final List<String> msgs = new ArrayList<>();
            if (!notUnzipped.isEmpty()) {
                for (FileObject fo : notUnzipped) {
                    msgs.add(fo.getNameExt());
                }
                actionResult.setResult(-1);
                notifyNotUnzipedWars(msgs);
                return;
            }

            io.getOut().println("Package Html5 projects. " + (new Date()));
            List<Project> html5Files = EmbPackageUtils.getHtml5Projects(serverProject);
            for (Project p : html5Files) {
                EmbPackageUtils.html5ProjectToServerJar(p, serverProject.getProjectDirectory(), io);
            }

            io.getOut().println("Package Html5 projects success. " + (new Date()));

        }

    }//class    

    protected static class WarsToFolderAction extends SingleJarAction {

        public WarsToFolderAction(Lookup context) {
            super(context);
            putValue(NAME, "War archives to folder");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {

            //final InputOutput io = IOProvider.getDefault().getIO("Package Apps (package-dist)", false);
            Project server = context.lookup(Project.class);
            if (!updateWebappsFolder(server)) {
                return;
            }
            super.actionPerformed(e);
        }

        @Override
        protected void packageApplications(final InputOutput io, final ActionResult actionResult) {
            final Project serverProject = context.lookup(Project.class);
            File warFolder = getWebappsFolder(serverProject);
            if (warFolder == null) {
                actionResult.setResult(-1);
                return;
            }
            io.select();

            List<FileObject> warFiles = EmbPackageUtils.getWarFiles(serverProject);
            io.getOut().println("Copy war files. " + (new Date()));

            for (FileObject fo : warFiles) {
                String warName = fo.getNameExt(); // without ext
                Copier copier = new Copier(FileUtil.toFile(fo));
                if (copier.copyTo(warFolder) == null) {
                    notifyError("Copy war file", "War file " + fo.getPath() + " cannot be copied. ");
                    actionResult.setResult(-1);
                    return;
                }
                io.getOut().println(" --- Copy " + warName + " file to ../package-dist/" + warFolder.getName() + " success. " + new Date());
            }

            io.getOut().println("Package Html5 projects. " + (new Date()));

            List<Project> html5Files = EmbPackageUtils.getHtml5Projects(serverProject);
            for (Project p : html5Files) {
                EmbPackageUtils.html5ProjectToWar(p, serverProject.getProjectDirectory(), warFolder.getName(), io);
            }

            io.getOut().println("Package Html5 projects success. " + (new Date()));
        }

    }//class    

    protected static class UnpackedWarsToFolderAction extends WarsToFolderAction {

        //protected final Lookup context;
        public UnpackedWarsToFolderAction(Lookup context) {
            super(context);
            putValue(NAME, "Unpacked war archives to folder");
        }

        @Override
        protected void packageApplications(final InputOutput io, final ActionResult actionResult) {
            final Project serverProject = context.lookup(Project.class);
            File warFolder = getWebappsFolder(serverProject);
            if (warFolder == null) {
                actionResult.setResult(-1);
                return;
            }

            io.select();

            List<FileObject> warFiles = EmbPackageUtils.getWarFiles(serverProject);
            io.getOut().println("Unzip war files. " + (new Date()));

            List<FileObject> notUnzipped = new ArrayList<>();

            for (FileObject fo : warFiles) {
                String warName = fo.getName(); // without ext
                File f = new File(warFolder.getPath() + "/" + warName);
                if (!Copier.ZipUtil.unzip(FileUtil.toFile(fo), "/", f)) {
                    notUnzipped.add(fo);
                    break;
                }

                io.getOut().println("Unzip " + warName + " file to ../package-dist/" + warFolder.getName() + " success. " + new Date());
            }
            List<String> msgs = new ArrayList<>();
            if (!notUnzipped.isEmpty()) {
                msgs.clear();
                for (FileObject fo : notUnzipped) {
                    msgs.add(fo.getNameExt());
                }
                actionResult.setResult(-1);
                notifyNotUnzipedWars(msgs);
                return;

            }

            io.getOut().println("Package Html5 projects. " + (new Date()));
            List<Project> html5Files = EmbPackageUtils.getHtml5Projects(serverProject);
            for (Project p : html5Files) {
                EmbPackageUtils.html5ProjectToFolder(p, serverProject.getProjectDirectory(), warFolder.getName(), io);
            }

            io.getOut().println("Package Html5 projects success. " + (new Date()));

        }

    }//class    

    public static class ActionResult {

        private int result = 0;

        public int getResult() {
            return result;
        }

        public void setResult(int result) {
            this.result = result;
        }
    }
}//class
