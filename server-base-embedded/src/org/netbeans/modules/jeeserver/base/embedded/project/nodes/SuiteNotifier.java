package org.netbeans.modules.jeeserver.base.embedded.project.nodes;

import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.netbeans.modules.jeeserver.base.embedded.project.SuiteManager;
import org.netbeans.modules.jeeserver.base.embedded.project.wizard.ServerInstanceAntBuildExtender;

/**
 *
 * @author V. Shyshkin
 */
public class SuiteNotifier { //implements ChildrenNotifier {

    private ChildrenNotifier rootNodeNotifier;

    /**
     * Invoked when server instance configurations have been changed during
     * customization.
     *
     * @param uri a server instance id as specified by the class
     * {@literal Deployment}.
     */
    public void settingsChanged(String uri) {
        Project p = SuiteManager.getManager(uri).getServerProject();

        ServerInstanceAntBuildExtender ext = new ServerInstanceAntBuildExtender(p);
        ext.updateNbDeploymentFile();

    }

    /**
     * Notifies {@link ServerInstancesRootNode} instance that child nodes keys
     * changed.
     */
    public void instancesChanged() {
        if (rootNodeNotifier != null) {
            rootNodeNotifier.childrenChanged();
        }
    }

    public void childrenChanged(Object source, Object... params) {
        if (rootNodeNotifier != null) {
BaseUtil.out("SuiteNotifier childrenChanged");
            rootNodeNotifier.childrenChanged(source, params);
        }
    }

    public void iconChange(String uri, boolean newValue) {
        if (rootNodeNotifier == null) {
            return;
        }
        rootNodeNotifier.iconChange(uri, newValue);
    }

    public void displayNameChange(String uri, String newValue) {
        if (rootNodeNotifier == null) {
            return;
        }

        rootNodeNotifier.displayNameChange(uri, newValue);
    }

    final void setNotifier(ChildrenNotifier childrenNotifier) {
        this.rootNodeNotifier = childrenNotifier;
    }

}
