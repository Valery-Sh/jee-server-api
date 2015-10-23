package org.netbeans.modules.jeeserver.base.embedded.nodes;

import java.awt.Component;
import javax.swing.Action;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.netbeans.modules.jeeserver.base.embedded.project.wizard.IDEServerCustomizer;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;


public class EmbManagerNode extends AbstractNode {
    
    @StaticResource
    private final static String ICON_BASE = "org/netbeans/modules/jeeserver/base/embedded/resources/embedded-server-16x16.png";
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
        return true;
    }

    @Override
    public Component getCustomizer() {
        BaseDeploymentManager dm = getDeploymentManager();
        return new IDEServerCustomizer(dm.getLookup());
    }
    public BaseDeploymentManager getDeploymentManager() {
        return lookup.lookup(BaseDeploymentManager.class);
    }

}