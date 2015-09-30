package org.netbeans.modules.jeeserver.base.embedded.nodes;

import java.awt.Component;
import java.io.File;
import javax.swing.Action;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.base.deployment.ServerInstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.embedded.server.project.ServerSuiteManager;
import org.netbeans.modules.jeeserver.base.embedded.server.project.wizards.ServerInstanceCustomizer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;


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
        
        Lookup lk = createInstanceLookup(dm.getUri());
        
        return new ServerInstanceCustomizer(lk);
    }
    public BaseDeploymentManager getDeploymentManager() {
        return lookup.lookup(BaseDeploymentManager.class);
    }

    private Lookup createInstanceLookup(String uri) {

        InstanceContent c = new InstanceContent();
        ServerInstanceProperties sip = createServerInstanceProperties(uri);
        c.add(sip);
        InstanceProperties props = InstanceProperties.getInstanceProperties(uri);
        sip.setServerId(props.getProperty(BaseConstants.SERVER_ID_PROP));
        sip.setUri(props.getProperty(BaseConstants.URL_PROP));
        FileObject fo = FileUtil.toFileObject(new File(props.getProperty(BaseConstants.SERVER_LOCATION_PROP)));
        c.add(fo);
        //sip.setCurrentDeploymentMode(sip.getManager().getCurrentDeploymentMode());        
        return new AbstractLookup(c);
    }
    
    private  ServerInstanceProperties createServerInstanceProperties(String uri) {
        InstanceProperties props = InstanceProperties.getInstanceProperties(uri);
        ServerInstanceProperties sip = new ServerInstanceProperties();
        sip.setServerId(props.getProperty(BaseConstants.SERVER_ID_PROP));
        sip.setUri(props.getProperty(BaseConstants.URL_PROP));
        return sip;
    }
    
/*    
    public JettyDeploymentManager getDeploymentManager() {
        return lookup.lookup(JettyDeploymentManager.class);
    }
 */
}