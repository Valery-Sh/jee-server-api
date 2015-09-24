package org.netbeans.modules.jeeserver.base.embedded.project;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.jeeserver.base.deployment.ServerInstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.ide.BaseStartServer;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.filesystems.FileObject;
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
        
/*        Project[] ps = OpenProjects.getDefault().getOpenProjects();
        for ( Project p : ps) {
            if ( p.getProjectDirectory().getName().contains("OOO")) {
                OpenProjects.getDefault().addPropertyChangeListener(new OpenProjectListeners.PropertiesListener());
            }
            
        }
*/        
    }

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
