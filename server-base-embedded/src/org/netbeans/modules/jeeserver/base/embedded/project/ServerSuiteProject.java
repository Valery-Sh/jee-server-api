package org.netbeans.modules.jeeserver.base.embedded.project;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.jeeserver.base.deployment.ide.BaseStartServer;
import org.netbeans.modules.jeeserver.base.embedded.project.nodes.SuiteNotifier;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteConstants;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author V. Shyshkin
 */
public class ServerSuiteProject implements Project {

    private static final Logger LOG = Logger.getLogger(BaseStartServer.class.getName());
    public static final String TYPE = "org-netbeans-modules-jeeserver-base-embedded-project";
    private final FileObject projectDir;
    private final ProjectState state;
    //private final NodeModel instanceContexts;

    private Lookup lookup;

    public ServerSuiteProject(FileObject projectDir, ProjectState state) {
        this.projectDir = projectDir;
        this.state = state;
        //instanceContexts = new NodeModel();
    }

    @Override
    public FileObject getProjectDirectory() {
        return projectDir;
    }

    protected Project getProject() {
        return FileOwnerQuery.getOwner(projectDir);
    }

    //protected abstract String getServerId();
    //protected abstract String getLayerProjectFolderPath();
    @Override
    public Lookup getLookup() {

        if (lookup == null) {
            //final ServerInstanceProperties serverProperties = new ServerInstanceProperties();
            //final String id = getServerId();
            //final String uri = id + ":" + BaseConstants.URIPREFIX_NO_ID + ":" + projectDir.getPath();
            //serverProperties.setServerId(id);
            //serverProperties.setUri(uri);
            //serverProperties.setLayerProjectFolderPath(this.getLayerProjectFolderPath());

            ProjectOpenedHook openHook = new ServerSuiteProjectOpenHook(projectDir);
            SuiteNotifier suiteModel = new SuiteNotifier();
            lookup = Lookups.fixed(new Object[]{
                this,
                getProjectInformation(),
                getLogicalViewProvider(),
                getProjectActionProvider(),
                new ProjectOperations(this),
                openHook,
                suiteModel
                
                //serverProperties,
            //getStartServerPropertiesProvider(),
//                new ServerInstanceAvailableModules<>(this)
            });
        }
        return lookup;
    }


    protected LogicalViewProvider getLogicalViewProvider() {

        return new ServerSuiteProjectLogicalView(this);
    }

    protected ProjectInformation getProjectInformation() {
        return new Info();
    }

    protected ProjectActionProvider getProjectActionProvider() {
        return new ProjectActionProvider();
    }

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
                DefaultProjectOperations.performDefaultDeleteOperation(ServerSuiteProject.this);
            } else if (string.equals(ActionProvider.COMMAND_COPY)) {
                DefaultProjectOperations.performDefaultCopyOperation(ServerSuiteProject.this);
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

    public final class Info implements ProjectInformation {

        @Override
        public Icon getIcon() {
            return new ImageIcon(ImageUtilities.loadImage(SuiteConstants.SERVER_PROJECT_ICON));
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
            return ServerSuiteProject.this;
        }

    }//class Info

}
