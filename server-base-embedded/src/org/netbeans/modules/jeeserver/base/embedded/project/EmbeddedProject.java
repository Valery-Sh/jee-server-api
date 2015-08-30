package org.netbeans.modules.jeeserver.base.embedded.project;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.jeeserver.base.deployment.config.ServerInstanceAvailableModules;
import org.netbeans.modules.jeeserver.base.deployment.ServerInstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.ide.BaseStartServer;
import org.netbeans.modules.jeeserver.base.deployment.specifics.StartServerPropertiesProvider;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.netbeans.modules.jeeserver.base.deployment.utils.LibrariesFileLocator;
import org.netbeans.modules.jeeserver.base.embedded.utils.EmbUtils;
//import org.netbeans.modules.jeeserver.jetty.deploy.config.JettyStartServerPropertiesProvider;
//import org.netbeans.modules.jeeserver.jetty.util.JettyConstants;
//import org.netbeans.modules.jeeserver.jetty.util.Utils;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author V. Shyshkin
 */
public abstract class EmbeddedProject implements Project {

    private static final Logger LOG = Logger.getLogger(BaseStartServer.class.getName());

    private final FileObject projectDir;
    private final ProjectState state;

    private Lookup lookup;

    public EmbeddedProject(FileObject projectDir, ProjectState state) {
        this.projectDir = projectDir;
        this.state = state;
    }

/*    public static void enableJSFLibrary(FileObject projDir) {

        Library[] libs = LibraryManager.getDefault().getLibraries();
        //
        // add JSF library to ${jetty.base}/lib/jsf-netbeans folder
        //
        Library jsfLib = LibraryManager.getDefault().getLibrary("jsf20");
        if (jsfLib == null) {
            return;
        }
        List<File> files = LibrariesFileLocator.findFiles(jsfLib);
        final FileObject jsfFolder = FileUtil.toFileObject(Paths.get(projDir.getPath(), JettyConstants.JETTYBASE_FOLDER, "lib/jsf-netbeans").toFile());
        
        files.forEach(file -> {
            FileObject fo = FileUtil.toFileObject(file);
            try {
                FileUtil.copyFile(fo, jsfFolder, fo.getName(), fo.getExt());
            } catch (IOException ex) {
                LOG.log(Level.INFO, ex.getMessage());
            }
        });
    }
*/
    @Override
    public FileObject getProjectDirectory() {
        return projectDir;
    }

    protected Project getProject() {
        return FileOwnerQuery.getOwner(projectDir);
    }
    protected abstract String getServerId();
    protected abstract String getLayerProjectFolderPath();
    
    @Override
    public Lookup getLookup() {

        if (lookup == null) {
            final ServerInstanceProperties serverProperties = new ServerInstanceProperties();
            final String id = getServerId();
            final String uri = id + ":" + BaseConstants.URIPREFIX_NO_ID + ":" + projectDir.getPath();
            serverProperties.setServerId(id);
            serverProperties.setUri(uri);
            serverProperties.setLayerProjectFolderPath(this.getLayerProjectFolderPath());
            
            ProjectOpenedHook openHook = new EmbeddedProjectOpenHook(projectDir, serverProperties);

            lookup = Lookups.fixed(new Object[]{
                this,
                getProjectInformation(),
                getLogicalViewProvider(),
                getProjectActionProvider(),
                new ProjectOperations(this),
                openHook,
                serverProperties,
                //getStartServerPropertiesProvider(),
//                new ServerInstanceAvailableModules<>(this)
            });
        }
        return lookup;
    }
    
    protected abstract LogicalViewProvider getLogicalViewProvider();
    protected abstract ProjectInformation getProjectInformation();
    //protected abstract StartServerPropertiesProvider getStartServerPropertiesProvider();
    protected abstract String getIconImagePath();

    
    protected ProjectActionProvider getProjectActionProvider() {
        return new ProjectActionProvider();
    }
    
    
/*    private final class Info implements ProjectInformation {

        //@StaticResource()
        //public static final String JETTY_ICON = "org/netbeans/modules/jeeserver/jetty/resources/jetty01-16x16.jpg";

        @Override
        public Icon getIcon() {
            return new ImageIcon(ImageUtilities.loadImage(JETTY_ICON));
        }

        @Override
        public String getName() {
            return getProjectDirectory().getName();
        }

        @Override
        public String getDisplayName() {
            return getName();
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener pcl) {
            //do nothing, won't change
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener pcl) {
            //do nothing, won't change
        }

        @Override
        public Project getProject() {
            return EmbeddedProject.this;
        }

    }//class Info
*/
    private final class ProjectActionProvider implements ActionProvider {

        private final String[] supported = new String[]{
            ActionProvider.COMMAND_DELETE,
            ActionProvider.COMMAND_COPY,
            "start"
        };

        @Override
        public String[] getSupportedActions() {
            return supported;
        }

        @Override
        public void invokeAction(String string, Lookup lookup) throws IllegalArgumentException {
            if (string.equals(ActionProvider.COMMAND_DELETE)) {
                DefaultProjectOperations.performDefaultDeleteOperation(EmbeddedProject.this);
            } else if (string.equals(ActionProvider.COMMAND_COPY)) {
                DefaultProjectOperations.performDefaultCopyOperation(EmbeddedProject.this);
            } else if (string.equals("start")) {
            }
        }

        @Override
        public boolean isActionEnabled(String command, Lookup lookup) throws IllegalArgumentException {
            switch (command) {
                case ActionProvider.COMMAND_DELETE:
                    return true;
                case ActionProvider.COMMAND_COPY:
                    return true;
                case "start":
                    return true;
                default:
                    throw new IllegalArgumentException(command);
            }
        }
    }

    private class ProjectOperations implements DeleteOperationImplementation {

        private final Project project;

        public ProjectOperations(Project project) {
            this.project = project;
        }

        @Override
        public void notifyDeleting() throws IOException {
        }

        @Override
        public void notifyDeleted() throws IOException {
        }

        @Override
        public List<FileObject> getMetadataFiles() {
            return new ArrayList<>();
        }

        @Override
        public List<FileObject> getDataFiles() {
            FileObject[] fo = project.getProjectDirectory().getChildren();
            return Arrays.asList(fo);
        }

    }
}
