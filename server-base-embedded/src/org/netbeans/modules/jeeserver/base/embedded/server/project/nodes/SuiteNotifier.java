package org.netbeans.modules.jeeserver.base.embedded.server.project.nodes;

import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.netbeans.modules.jeeserver.base.embedded.server.project.SuiteManager;
import org.netbeans.modules.jeeserver.base.embedded.server.project.wizards.ServerInstanceAntBuildExtender;

/**
 *
 * @author V. Shyshkin
 */
public class SuiteNotifier { //implements ChildrenNotifier {

    private ChildrenNotifier rootNodeNotifier;

    public ChildrenNotifier getModel() {
        return rootNodeNotifier;
    }

    public void settingsChanged(String uri) {
        Project p = SuiteManager.getManager(uri).getServerProject();
        
        ServerInstanceAntBuildExtender ext = new ServerInstanceAntBuildExtender(p);
        ext.updateNbDeploymentFile();
        
    }
    
    /**
     * Notifies {@link ServerInstancesRootNode} instance that 
     * child nodes keys changed. 
     */
    public void instancesChanged() {
        if (rootNodeNotifier != null) {
            rootNodeNotifier.childrenChanged();
        }
    }
    
    public void iconChange(String uri,boolean newValue) {
        if (rootNodeNotifier == null) {
            return;
        }
        rootNodeNotifier.iconChange(uri,newValue);
    }
    
    public void displayNameChange(String uri,String newValue) {
        if (rootNodeNotifier == null) {
            return;
        }
        
        rootNodeNotifier.displayNameChange(uri,newValue);
    }

/*    public void propertyChange(PropertyChangeEvent evt) {
        if (rootNodeNotifier == null) {
            return;
        }
        rootNodeNotifier.propertyChange(evt);
    }
*/    
/*    private ChildrenNotifier getRootNodeNotifier() {
        if ( rootNodeNotifier == null ) {
            return null;
        }
        if ( rootNodeNotifier instanceof ServerInstancesRootNode ) {
            return rootNodeNotifier;
        } else if ( rootNodeNotifier instanceof InstanceNode ) {
            BaseUtils.out("NodeModel. InstanceNode.Parent=" + 
                    ((InstanceNode)rootNodeNotifier).getParentNode());
        }
        return null;
    }
*/    
    
    final void setModel(ChildrenNotifier childrenKeysModel) {
        this.rootNodeNotifier = childrenKeysModel;
    }
    
/*    public Lookup getServerInstancesLookup() {
        ChildrenNotifier km = getRootNodeNotifier();
        if ( km == null ) {
            return null;
        }
        return ((ServerInstancesRootNode)km).getLookup();
    }
*/
/*    public Lookup getServerInstanceLookup(String uri) {
        ChildrenNotifier km = getRootNodeNotifier();
        if ( km == null ) {
            return null;
        }
        
        Node[] nodes = ((ServerInstancesRootNode)km).getChildKeys().getNodes();
        if ( nodes == null || nodes.length == 0 ) {
            return null;
        }
        Lookup result = null;
        for ( Node node : nodes ) {
            InstanceNode inode = null;
            if ( node instanceof InstanceNode) {
                inode = (InstanceNode) node;
            }
            if ( inode != null && uri.equals(inode.getKey()) ) {
                result = inode.getLookup();
                break;
            }
        }
        return result;
    }
*/    
    
}
