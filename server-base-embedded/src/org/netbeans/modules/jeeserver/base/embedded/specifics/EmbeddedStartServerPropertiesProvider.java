package org.netbeans.modules.jeeserver.base.embedded.specifics;

import java.util.Properties;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.base.deployment.specifics.StartServerPropertiesProvider;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.netbeans.modules.jeeserver.base.embedded.project.SuiteManager;
import org.netbeans.modules.jeeserver.base.embedded.project.wizard.MavenMainClassCustomizer;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteConstants;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteUtil;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Valery
 */
public class EmbeddedStartServerPropertiesProvider implements StartServerPropertiesProvider {

    private static final Logger LOG = Logger.getLogger(BaseDeploymentManager.class.getName());

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

        FileObject fo = serverProject.getProjectDirectory().getFileObject("build.xml");
        if (!BaseUtil.isAntProject(serverProject)) {
            if (fo == null) {
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

        if (startProperties == null) {
            startProperties = getProps(serverProject);
            startProperties.setProperty("target", "run");
        }

        if (!BaseUtil.isAntProject(serverProject)) {
            setMavenProperies(serverProject, startProperties, "run-embedded-server");
        }
        return startProperties;
    }

    protected void setMavenProperies(Project serverProject, Properties properties, String target) {
        String cp = BaseUtil.getMavenClassPath(manager);
        
        properties.remove("do.build");
        properties.remove("do.deploy-file");        
        
        FileObject fo = serverProject.getProjectDirectory().getFileObject("target");
        if (fo != null) {
            fo = fo.getFileObject("classes");
        }

/*        if (fo != null) {
            cp += ":" + fo.getPath();
        } 
*/
        if (fo == null) {
            properties.setProperty("do.build", "yes");
        }   
        
        FileObject cmJar = SuiteUtil.getCommandManagerJar(serverProject);
        
        Properties pomProperties = BaseUtil.getPomProperties(cmJar);
        if (pomProperties != null) {
            
            String str = pomProperties.getProperty("groupId");
            str = str.replace(".", "/");
            str +=  "/"
                    + pomProperties.getProperty("artifactId") 
                    + "/" 
                    + pomProperties.getProperty("version")
                    + "/"
                    + cmJar.getNameExt();
            if ( cmJar.getParent().getFileObject(str) == null ) {
                properties.setProperty("do.deploy-file", "yes");
            }
                    
            properties.setProperty(SuiteConstants.COMMAND_MANAGER_GROUPID,
                    pomProperties.getProperty("groupId"));

            properties.setProperty(SuiteConstants.COMMAND_MANAGER_ARTIFACTID,
                    pomProperties.getProperty("artifactId"));
            properties.setProperty(SuiteConstants.COMMAND_MANAGER_VERSION,
                    pomProperties.getProperty("version"));
            properties.setProperty(BaseConstants.COMMAND_MANAGER_JAR_NAME_PROP,
                    pomProperties.getProperty("artifactId") + "-"
                    + pomProperties.getProperty("version")
                    + ".jar"
            );
        }
        //properties.setProperty("target.project.classes",
        //            "target/classes");
        
        properties.setProperty(SuiteConstants.MAVEN_REPO_LIB_PATH_PROP,
                SuiteConstants.MAVEN_REPO_LIB_PATH);

        properties.setProperty("target", target);

        properties.setProperty(SuiteConstants.MAVEN_RUN_CLASSPATH_PROP, cp);
        //
        // We set MAVEN_DEBUG_CLASSPATH_PROP. In future this approach may change
        //
        properties.setProperty(SuiteConstants.MAVEN_DEBUG_CLASSPATH_PROP, cp);        
        properties.setProperty(SuiteConstants.MAVEN_WORK_DIR_PROP, serverProject.getProjectDirectory().getPath());
        String mainClass = getMainClass(serverProject);
        if (mainClass != null) {
            properties.setProperty(SuiteConstants.MAVEN_MAIN_CLASS_PROP, mainClass);
        }

    }

    protected String getMainClass(Project project) {
        BaseDeploymentManager dm = SuiteManager.getManager(project);
        String mainClass = dm.getInstanceProperties().getProperty(SuiteConstants.MAVEN_MAIN_CLASS_PROP);
        if (mainClass != null) {
            return mainClass;
        }

        String[] classes = BaseUtil.getMavenMainClasses(project);
        if (classes.length == 0) {
            return null;
        }
        if (classes.length == 0 || classes.length > 1) {
            MavenMainClassCustomizer.customize(project);
            mainClass = dm.getInstanceProperties().getProperty(SuiteConstants.MAVEN_MAIN_CLASS_PROP);
        } else {
            mainClass = classes[0];
        }
        return mainClass;
    }

    @Override
    public Properties getStopProperties(Project serverProject
    ) {
        if (stopProperties == null) {
            stopProperties = new Properties();
            stopProperties.setProperty("target", "stop");
            stopProperties.setProperty("stop.port", manager.getInstanceProperties().getProperty(BaseConstants.SHUTDOWN_PORT_PROP));
//            stopProperties.setProperty("stop.key", JettyServerSpecifics.JETTY_SHUTDOWN_KEY);        
        }
        return stopProperties;
    }

    @Override
    public Properties getDebugProperties(Project serverProject
    ) {
        if (debugProperties == null) {
            debugProperties = getProps(serverProject);
            debugProperties.setProperty("target", "debug-embedded-server");
        }

        debugProperties.setProperty("server.debug.port", manager.getInstanceProperties().getProperty(BaseConstants.DEBUG_PORT_PROP));
        debugProperties.setProperty("server.debug.transport", "dt_socket");
        debugProperties.setProperty("debug.args.line", "-Xdebug");

        if (!BaseUtil.isAntProject(serverProject)) {
            setMavenProperies(serverProject, debugProperties, "debug-embedded-server");
            /*            String cp = BaseUtil.getMavenClassPath(manager);

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
             */
        }
        return debugProperties;
    }

    @Override
    public Properties getProfileProperties(Project serverProject
    ) {
        profileProperties = new Properties();
        profileProperties.setProperty("target", "profile");
        String profile_args = BaseUtil.getProfileArgs(manager);
        profileProperties.setProperty("profiler.args", profile_args);
        profileProperties.setProperty("stop.port", manager.getInstanceProperties().getProperty(BaseConstants.SHUTDOWN_PORT_PROP));
//        profileProperties.setProperty("stop.key", JettyServerSpecifics.JETTY_SHUTDOWN_KEY);        

        return profileProperties;
    }

}
