package org.netbeans.modules.jeeserver.base.embedded.nodes;

import javax.swing.Action;
import org.netbeans.api.annotations.common.StaticResource;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;


public class EmbManagerNode extends AbstractNode {
    
    @StaticResource
    private final static String ICON_BASE = "org/netbeans/modules/jeeserver/base/embedded/resources/server.png";
    private final Lookup lookup;

    public EmbManagerNode(Lookup lookup) {
        super(Children.LEAF);
        // Set default lookup
        this.lookup = lookup;
        // Set icon
        setIconBaseWithExtension(ICON_BASE);
    }

    @Override
    public String getDisplayName() {
        return "My Test Embedded";
    }

    @Override
    public SystemAction[] getActions() {
        return new SystemAction[]{};
    }

    @Override
    public Action[] getActions(boolean context) {
        //return null;
        return new Action[]{};
    }

    @Override
    public boolean hasCustomizer() {
        return false;
    }

/*    @Override
    public Component getCustomizer() {
        return new AppEngineCustomizer(new AppEngineCustomizerDataSupport(getDeploymentManager()));
    }
*/
/*    public JettyDeploymentManager getDeploymentManager() {
        return lookup.lookup(JettyDeploymentManager.class);
    }
*/ 
}