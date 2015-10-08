package org.netbeans.modules.jeeserver.base.embedded.project.wizard;

import org.netbeans.modules.jeeserver.base.embedded.EmbeddedInstanceBuilder;
import java.io.File;
import java.util.Properties;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.ServerInstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.specifics.InstanceBuilder;
import org.netbeans.modules.jeeserver.base.deployment.specifics.LogicalViewNotifier;
import org.netbeans.modules.jeeserver.base.deployment.specifics.ServerSpecifics;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.embedded.project.SuiteManager;
import org.netbeans.modules.jeeserver.base.embedded.project.nodes.SuiteNotifier;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteConstants;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author Valery
 */
public class ServerInstanceCustomizerWizardAction extends ServerInstanceAddExistingWizardAction {

    public ServerInstanceCustomizerWizardAction(Lookup context, File instanceProjectDir) {
        super(context, instanceProjectDir);
    }

    @Override
    protected void notifySettingChange(Lookup context) {
        LogicalViewNotifier lvn = context.lookup(LogicalViewNotifier.class);

        String uri = (String) wiz.getProperty(BaseConstants.URL_PROP);

        if (lvn != null) {

            lvn.displayNameChange(uri, (String) wiz.getProperty(BaseConstants.DISPLAY_NAME_PROP));
        }
        //AuxiliaryConfiguration aa;
        
        //Project p = SuiteManager.getManager(uri).getServerProject();

        SuiteNotifier suiteNotifier = SuiteManager.getServerSuiteProject(uri)
                .getLookup().lookup(SuiteNotifier.class);
        suiteNotifier.settingsChanged(uri);

    }
    /*        LogicalViewNotifier lvn = context.lookup(LogicalViewNotifier.class);
     if( lvn != null ) {
     lvn.displayNameChange(uri, (String) wiz.getProperty(BaseConstants.DISPLAY_NAME_PROP)); 
     }
     */

    @Override
    protected InstanceBuilder getBuilder(ServerSpecifics specifics, Properties props) {
        return (EmbeddedInstanceBuilder) specifics.getInstanceBuilder(props, InstanceBuilder.Options.CUSTOMIZER);
    }

    @Override
    protected FileObject gerServerInstancesDir(Lookup context) {
        String uri = context.lookup(ServerInstanceProperties.class).getUri();
        return SuiteManager.getServerInstancesDir(uri);
    }

    @Override
    protected void fillWizardDescriptor(WizardDescriptor wiz) {

//        wiz.setButtonListener(new ButtonListener(this));
        String uri = context.lookup(ServerInstanceProperties.class).getUri();
        InstanceProperties ip = InstanceProperties.getInstanceProperties(uri);
        wiz.putProperty(BaseConstants.URL_PROP, uri);
        wiz.putProperty(BaseConstants.DISPLAY_NAME_PROP, ip.getProperty(BaseConstants.DISPLAY_NAME_PROP));
        wiz.putProperty(BaseConstants.HOME_DIR_PROP, ip.getProperty(BaseConstants.HOME_DIR_PROP));
        wiz.putProperty(BaseConstants.HOST_PROP, ip.getProperty(BaseConstants.HOST_PROP));
        wiz.putProperty(BaseConstants.HTTP_PORT_PROP, ip.getProperty(BaseConstants.HTTP_PORT_PROP));
        wiz.putProperty(BaseConstants.DEBUG_PORT_PROP, ip.getProperty(BaseConstants.DEBUG_PORT_PROP));
        wiz.putProperty(BaseConstants.SHUTDOWN_PORT_PROP, ip.getProperty(BaseConstants.SHUTDOWN_PORT_PROP));
        wiz.putProperty(BaseConstants.SERVER_ID_PROP, ip.getProperty(BaseConstants.SERVER_ID_PROP));
        wiz.putProperty("projdir", new File(ip.getProperty(BaseConstants.SERVER_LOCATION_PROP)));
        wiz.putProperty(SuiteConstants.SUITE_PROJECT_LOCATION, new File(ip.getProperty(SuiteConstants.SUITE_PROJECT_LOCATION)));

    }

}
