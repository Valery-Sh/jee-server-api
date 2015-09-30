package org.netbeans.modules.jeeserver.base.embedded.specifics;

import java.beans.PropertyChangeEvent;
import java.io.InputStream;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.base.deployment.specifics.ServerSpecifics;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.netbeans.modules.jeeserver.base.embedded.server.project.ServerSuiteManager;
import org.netbeans.modules.jeeserver.base.embedded.server.project.nodes.ChildrenKeysModel;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteConstants;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteUtil;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.util.Lookup;

/**
 *
 * @author V. Shyshkin
 */
public interface EmbeddedServerSpecifics extends ServerSpecifics {

    static final Logger LOG = Logger.getLogger(SuiteUtil.class.getName());

    boolean supportsDistributeAs(SuiteConstants.DistributeAs distributeAs);

    default InputStream getPomFileTemplate() {
        return null;
    }

    @Override
    default void propertyChange(PropertyChangeEvent evt) {
        Object o = evt.getSource();
        BaseDeploymentManager dm = null;
        if (o instanceof BaseDeploymentManager) {
            dm = (BaseDeploymentManager) o;
            switch (evt.getPropertyName()) {
                case "server-running":
                    ChildrenKeysModel model = ServerSuiteManager.getServerSuiteProject(dm.getUri()).getLookup().lookup(ChildrenKeysModel.class);
                    model.propertyChange(evt);

                    break;
            }
        }

    }

/*    @Override
    default Lookup getServerLookup(BaseDeploymentManager dm) {
        return ServerSuiteManager.getServerInstanceLookup(dm.getUri());
    }
*/
    @Override
    default void register(BaseDeploymentManager dm) {
        FileObject fo = dm.getServerProjectDirectory();
        fo.addFileChangeListener(new FileChangeListener() {

            @Override
            public void fileFolderCreated(FileEvent fe) {
            }

            @Override
            public void fileDataCreated(FileEvent fe) {
            }

            @Override
            public void fileChanged(FileEvent fe) {
            }

            @Override
            public void fileDeleted(FileEvent fe) {
                FileObject fo = fe.getFile();
                FileObject source = (FileObject) fe.getSource();
                if (!ProjectManager.getDefault().isProject(source)) {
                    Project suite = ServerSuiteManager.getServerSuiteProject(dm.getUri());

                    ChildrenKeysModel model = suite.getLookup().lookup(ChildrenKeysModel.class);
                    model.modelChanged();

                    source.removeFileChangeListener(this);
                    InstanceProperties.removeInstance(dm.getUri());
                }

            }

            @Override
            public void fileRenamed(FileRenameEvent fe) {
            }

            @Override
            public void fileAttributeChanged(FileAttributeEvent fe) {

            }
        });
    }
}
