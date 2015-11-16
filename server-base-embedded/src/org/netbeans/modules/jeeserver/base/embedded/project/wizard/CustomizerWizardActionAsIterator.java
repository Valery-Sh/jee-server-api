package org.netbeans.modules.jeeserver.base.embedded.project.wizard;

import org.netbeans.modules.jeeserver.base.embedded.EmbeddedInstanceBuilder;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.ServerInstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.specifics.InstanceBuilder;
import org.netbeans.modules.jeeserver.base.deployment.specifics.LogicalViewNotifier;
import org.netbeans.modules.jeeserver.base.deployment.specifics.ServerSpecifics;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.netbeans.modules.jeeserver.base.deployment.utils.PomXmlUtil;
import org.netbeans.modules.jeeserver.base.deployment.utils.PomXmlUtil.Property;
import org.netbeans.modules.jeeserver.base.embedded.project.SuiteManager;
import org.netbeans.modules.jeeserver.base.embedded.project.nodes.SuiteNotifier;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteConstants;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteUtil;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author Valery
 */
public class CustomizerWizardActionAsIterator extends AddExistingProjectWizardActionAsIterator {
    private static final Logger LOG = Logger.getLogger(CustomizerWizardActionAsIterator.class.getName());

    public CustomizerWizardActionAsIterator(Lookup context, File instanceProjectDir) {
        super(context, instanceProjectDir);
    }

    protected void serverVersionChanged(Lookup context) {
        String uri = (String) wiz.getProperty(BaseConstants.URL_PROP);
        Project instanceProject = SuiteManager.getManager(uri).getServerProject();
        if (BaseUtil.isAntProject(instanceProject)) {
            return;
        }
        try (InputStream is = instanceProject.getProjectDirectory()
                .getFileObject("pom.xml")
                .getInputStream();) {

            PomXmlUtil pomSupport = new PomXmlUtil(is);
            PomXmlUtil.PomProperties props = pomSupport.getProperties();
            String serverVersion = (String) wiz.getProperty(BaseConstants.SERVER_VERSION_PROP);
            Property p = props.getPropertyByName(BaseConstants.NB_SERVER_VERSION);
            if ( serverVersion == null ) {
                return;
            }
            if ( p != null && serverVersion.equals(p.getValue())) {
                return;
            }
            props.replace(BaseConstants.NB_SERVER_VERSION, serverVersion);
            Path target = Paths.get(instanceProject.getProjectDirectory().getPath());
            pomSupport.save(target, "pom.xml");
        } catch (IOException ex) {
            LOG.log(Level.INFO, ex.getMessage());
        }

    }

    @Override
    protected void notifySettingChange(Lookup context) {
        LogicalViewNotifier lvn = context.lookup(LogicalViewNotifier.class);

        String uri = (String) wiz.getProperty(BaseConstants.URL_PROP);

        if (lvn != null) {

            lvn.displayNameChange(uri, (String) wiz.getProperty(BaseConstants.DISPLAY_NAME_PROP));
        }
        //AuxiliaryConfiguration aa;

        serverVersionChanged(context); // May be a server version changed

        SuiteNotifier suiteNotifier = SuiteManager.getServerSuiteProject(uri)
                .getLookup().lookup(SuiteNotifier.class);
        suiteNotifier.settingsChanged(uri);

    }

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
        wiz.putProperty(BaseConstants.SERVER_ACTUAL_ID_PROP, ip.getProperty(BaseConstants.SERVER_ACTUAL_ID_PROP));

        wiz.putProperty(SuiteConstants.CUSTOMIZE_MODE_PROP, Boolean.TRUE);

        String version = ip.getProperty(BaseConstants.SERVER_VERSION_PROP);
        if (version == null) {
            version = SuiteConstants.UNKNOWN_VERSION;
        }
        wiz.putProperty(BaseConstants.SERVER_VERSION_PROP, version);

        wiz.putProperty("projdir", new File(BaseUtil.getServerLocation(ip)));
        wiz.putProperty(SuiteConstants.SUITE_PROJECT_LOCATION, new File(SuiteUtil.getSuiteProjectLocation(ip)));

    }

}
