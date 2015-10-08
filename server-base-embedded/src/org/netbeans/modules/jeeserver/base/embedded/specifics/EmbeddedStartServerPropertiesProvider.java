package org.netbeans.modules.jeeserver.base.embedded.specifics;

import java.util.Properties;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.base.deployment.specifics.StartServerPropertiesProvider;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteConstants;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.openide.filesystems.FileObject;
import org.w3c.dom.Element;

/**
 *
 * @author Valery
 */
public class EmbeddedStartServerPropertiesProvider implements StartServerPropertiesProvider {

    private Properties startProperties;
    private Properties stopProperties;
    private Properties debugProperties;
    private Properties profileProperties;
    private final BaseDeploymentManager manager;

    public EmbeddedStartServerPropertiesProvider(BaseDeploymentManager dm) {
        this.manager = dm;
    }

    @Override
    public FileObject getBuildXml(Project serverProject) {
        //return serverProject.getProjectDirectory().getFileObject("nbdeploymant/build.xml");
        FileObject fo = serverProject.getProjectDirectory().getFileObject("build.xml");
        if ( ! BaseUtil.isAntProject(serverProject)) {
            if ( fo == null ) {
                fo = serverProject.getProjectDirectory().getFileObject("nbdeployment/build.xml");
            }
        }
        return fo;
    }

    protected Properties getProps(Project serverProject) {
        Properties props = new Properties();
        props.setProperty("target", "run");
        props.setProperty("runtime.encoding", "UTF-8");

        props.setProperty("stop.port", manager.getInstanceProperties().getProperty(BaseConstants.SHUTDOWN_PORT_PROP));
        //props.setProperty("stop.key", JettyServerSpecifics.JETTY_SHUTDOWN_KEY);        
        return props;
    }

    @Override
    public Properties getStartProperties(Project serverProject) {
        AuxiliaryConfiguration ac = serverProject.getLookup().lookup(AuxiliaryConfiguration.class);
//        Element el = ac.getConfigurationFragment("config-data","http://www.netbeans.org/ns/maven-config-data/1", true);
//                 config.getConfigurationFragment("config-data","http://www.netbeans.org/ns/maven-config-data/1", false);
        //BaseUtil.out("Shared Element = " + el);
        Element el = ac.getConfigurationFragment("config-data","http://www.netbeans.org/ns/maven-config-data/1", false);
        BaseUtil.out("2 NOT Shared Element = " + el);
        

        if (startProperties == null) {
            startProperties = getProps(serverProject);
            startProperties.setProperty("target", "run");
        }
        if (!BaseUtil.isAntProject(serverProject)) {
            String cp = BaseUtil.getMavenClassPath(manager);

            FileObject fo = serverProject.getProjectDirectory().getFileObject("target");
            if (fo != null) {
                fo = fo.getFileObject("classes");
            }

            if (fo != null) {
                cp += ":" + fo.getPath();
            }
            startProperties.setProperty("target", "run-embedded-server");

            startProperties.setProperty(SuiteConstants.MAVEN_RUN_CLASSPATH_PROP, cp);
            startProperties.setProperty(SuiteConstants.MAVEN_WORK_DIR_PROP, serverProject.getProjectDirectory().getPath());
            startProperties.setProperty(SuiteConstants.MAVEN_MAIN_CLASS_PROP, InstanceProperties.getInstanceProperties(manager.getUri()).getProperty(SuiteConstants.MAVEN_MAIN_CLASS_PROP));
        }
        return startProperties;
    }

    @Override
    public Properties getStopProperties(Project serverProject) {
        if (stopProperties == null) {
            stopProperties = new Properties();
            stopProperties.setProperty("target", "stop");
            stopProperties.setProperty("stop.port", manager.getInstanceProperties().getProperty(BaseConstants.SHUTDOWN_PORT_PROP));
//            stopProperties.setProperty("stop.key", JettyServerSpecifics.JETTY_SHUTDOWN_KEY);        
        }
        return stopProperties;
    }

    @Override
    public Properties getDebugProperties(Project serverProject) {
        if (debugProperties == null) {
            debugProperties = getProps(serverProject);
            debugProperties.setProperty("target", "debug-embedded-server");
        }

        debugProperties.setProperty("server.debug.port", manager.getInstanceProperties().getProperty(BaseConstants.DEBUG_PORT_PROP));
        debugProperties.setProperty("server.debug.transport", "dt_socket");
        debugProperties.setProperty("debug.args.line", "-Xdebug");

        if (!BaseUtil.isAntProject(serverProject)) {
            String cp = BaseUtil.getMavenClassPath(manager);

            FileObject fo = serverProject.getProjectDirectory().getFileObject("target");
            if (fo != null) {
                fo = fo.getFileObject("classes");
            }

            if (fo != null) {
                cp += ":" + fo.getPath();
            }
            // debugProperties.setProperty("target", "debug-embedded-server");

            debugProperties.setProperty(SuiteConstants.MAVEN_DEBUG_CLASSPATH_PROP, cp);
            debugProperties.setProperty(SuiteConstants.MAVEN_WORK_DIR_PROP, serverProject.getProjectDirectory().getPath());
            debugProperties.setProperty(SuiteConstants.MAVEN_MAIN_CLASS_PROP, InstanceProperties.getInstanceProperties(manager.getUri()).getProperty(SuiteConstants.MAVEN_MAIN_CLASS_PROP));

        }
        return debugProperties;
    }

    @Override
    public Properties getProfileProperties(Project serverProject) {
        profileProperties = new Properties();
        profileProperties.setProperty("target", "profile");
        String profile_args = BaseUtil.getProfileArgs(manager);
        profileProperties.setProperty("profiler.args", profile_args);
        profileProperties.setProperty("stop.port", manager.getInstanceProperties().getProperty(BaseConstants.SHUTDOWN_PORT_PROP));
//        profileProperties.setProperty("stop.key", JettyServerSpecifics.JETTY_SHUTDOWN_KEY);        

        return profileProperties;
    }

}
