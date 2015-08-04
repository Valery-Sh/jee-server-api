/**
 * This file is part of Base JEE Server support in NetBeans IDE.
 *
 * Base JEE Server support in NetBeans IDE is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * Base JEE Server support in NetBeans IDE is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should see the GNU General Public License here:
 * <http://www.gnu.org/licenses/>.
 */
package org.netbeans.modules.jeeserver.base.deployment.config;

import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeApplication;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleListener;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.ConfigurationFilesListener;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.InstanceListener;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ContextRootConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

public abstract class AbstractModuleConfiguration implements ModuleConfiguration, ContextRootConfiguration { //, PropertyChangeListener {

    private Lookup lookup;
    private static final String CONTEXTPATH = "contextPath";
    private final J2eeModule module;
    private File contextConfigFile;
    protected Project serverProject;
    protected String serverInstanceId;

    protected Project webProject;
    private final String[] contextFilePaths;

    //Project serverProject;
    private static final Logger LOG = Logger.getLogger(AbstractModuleConfiguration.class.getName());

    /**
     * Creates a new instance of the class for the specified module.
     *
     * @param module an object of type {@literal J2eeModule)
     * }
     * @
     * param contextFilePaths
     * @
     * param contextFilePaths
     */
    protected AbstractModuleConfiguration(J2eeModule module, String[] contextFilePaths) {
        this.module = module;
        this.contextFilePaths = contextFilePaths;
        init();
    }

    /**
     *
     * @param module
     * @param contextFilePaths
     * @param serverInstanceId
     */
    protected AbstractModuleConfiguration(J2eeModule module, String[] contextFilePaths, String serverInstanceId) {
        this.module = module;
        this.contextFilePaths = contextFilePaths;
        this.serverInstanceId = serverInstanceId;
        
        init();
    }

    public String[] getContextFilePaths() {
        return contextFilePaths;
    }

    protected Project findServerProject2() {
        if (serverInstanceId == null) {
            return findServerProject();
        }
        Project server = null;
        try {
            BaseDeploymentManager dm = (BaseDeploymentManager) DeploymentFactoryManager.getInstance().getDisconnectedDeploymentManager(serverInstanceId);
            if (dm != null) {
                server = dm.getServerProject();
            }
        } catch (DeploymentManagerCreationException ex) {
            LOG.log(Level.INFO, "AbstractModuleConfiguration.getProjectPropertiesFileObject. {0}", ex.getMessage()); //NOI18N                        
        }

        return server;
    }

    protected Project findServerProject() {
        Project server = null;
        if (!getContextConfigFile().exists()) {
            return null;
        }
        Project web = FileOwnerQuery.getOwner(FileUtil.toFileObject(getContextConfigFile()));
        J2eeModuleProvider p = web.getLookup().lookup(J2eeModuleProvider.class);
        try {
            BaseDeploymentManager dm = (BaseDeploymentManager) DeploymentFactoryManager.getInstance().getDisconnectedDeploymentManager(p.getServerInstanceID());
            if (dm != null) {
                server = dm.getServerProject();
            }
        } catch (DeploymentManagerCreationException ex) {
            LOG.log(Level.INFO, "AbstractModuleConfiguration.getProjectPropertiesFileObject. {0}", ex.getMessage()); //NOI18N                        
        }
        return server;
    }

    protected void notifyDispose() {
        BaseUtils.out("AbstractModuleConfiguration 1 notifyDispose=");

        if (serverProject == null) {
            return;
        }

        AvailableWebModules<AbstractModuleConfiguration> avm = serverProject.getLookup().lookup(AvailableWebModules.class);
        avm.moduleDispose(this);

    }

    protected void notifyCreate() {
        if (serverProject == null) {
            serverProject = findServerProject2();
        }

        if (serverProject == null) {
            return;
        }
        
/*        webProject.getLookup().lookupAll(Object.class).forEach(o -> {
            BaseUtils.out("**************** NOTIFY CREATE = " + o.getClass());
        });
*/        
        AvailableWebModules<AbstractModuleConfiguration> avm = serverProject.getLookup().lookup(AvailableWebModules.class);
        avm.moduleCreate(this);

    }

    protected void notifyServerChange(String newServerInstanceId) {
        if (serverProject == null) {
            return;
        }
        
        AvailableWebModules<AbstractModuleConfiguration> avm = serverProject.getLookup().lookup(AvailableWebModules.class);
        BaseUtils.out("AbstractModuleConfiguration NotifyServerChange 4 " );

        avm.moduleDispose(this);
        
        try {
            DeploymentManager dm = DeploymentFactoryManager.getInstance().getDisconnectedDeploymentManager(newServerInstanceId);
            if (dm != null && (dm instanceof BaseDeploymentManager)) {
                serverProject = ((BaseDeploymentManager) dm).getServerProject();
                serverInstanceId = newServerInstanceId;
                notifyCreate();
            }

        } catch (DeploymentManagerCreationException ex) {
            LOG.log(Level.INFO, "AbstractModuleConfiguration.notifyServerChange. {0}", ex.getMessage()); //NOI18N                        
        }
    }

    /**
     * Here is an example for Jetty Server:
     *  <code>
     * <pre>
     *   File jw = module.getDeploymentConfigurationFile("WEB-INF/jetty-web.xml");
     *   File wj = module.getDeploymentConfigurationFile("WEB-INF/web-jetty.xml");
     *   if (jw.exists()) {
     *       return jw;
     *   }
     *   if (!wj.exists()) {
     *       return jw;
     *   }
     *   return wj;
     * </pre>
     * </code>
     *
     * @return
     */
    protected abstract File findContextConfigFile();

    public abstract Properties getContextProperties();

    protected abstract String changeContext(String cp);

    /**
     *
     */
    protected abstract void initContextConfigFile();

    public Project getWebProject() {
        return webProject;
    }

    private void init() {

        if (!getContextConfigFile().exists()) {
            initContextConfigFile();
        } else {
            Properties props = getContextProperties();
            if (props.getProperty(CONTEXTPATH) == null || props.getProperty(CONTEXTPATH).trim().isEmpty()) {
                if (webProject == null) {
                    webProject = FileOwnerQuery.getOwner(Utilities.toURI(getContextConfigFile()));
                }
                changeContext("/" + webProject.getProjectDirectory().getNameExt());
            }
        }
        if (webProject == null) {
            webProject = FileOwnerQuery.getOwner(Utilities.toURI(getContextConfigFile()));
        }
        if (serverInstanceId == null) {
            //
            // May occur when the server doesn't use the ModuleConfigurationFactory2 and
            // instead the old ModuleConfigurationFactory is used.
            //
            serverInstanceId = webProject.getLookup().lookup(J2eeModuleProvider.class).getServerInstanceID();
        }
        BaseUtils.out(" AbstractModeleConfig.webProject = " + webProject);
        BaseUtils.out(" AbstractModeleConfig.serverInstanceID = " + serverInstanceId);

    }

    /**
     * 
     * Returns lookup associated with the object. This lookup should contain implementations of all the supported configurations.
     *  The configuration are: ContextRootConfiguration, DatasourceConfiguration, MappingConfiguration, EjbResourceConfiguration, DeploymentPlanConfiguration, MessageDestinationConfiguration
     *  Implementators are advised to use org.openide.util.lookup.Lookups.fixed(java.lang.Object[]) to implement this method.
     * @return (@Lookups.fixed(this)}
     */
    @Override
    public synchronized Lookup getLookup() {
        if (null == lookup) {
            lookup = Lookups.fixed(this);
        }
        return lookup;
    }

    public File getContextConfigFile() {
        if (contextConfigFile == null) {
            contextConfigFile = findContextConfigFile();
        }
        return contextConfigFile;
    }

    /**
     * @return an object of type File which specifies a folder where web pages,
     * WEB-INF? META-INF reside.
     */
    public File getWebRoot() {
        File c = getContextConfigFile();
        if (c == null || !c.exists()) {
            return null;
        }
        c = c.getParentFile();
        if (!c.isFile() && ("WEB-INF".equals(c.getName()) || "META-INF".equals(c.getName()))) {
            c = c.getParentFile();
        } else {
            c = null;
        }
        return c;
    }

    /**
     * The server calls this method when it is done using this
     * ModuleConfiguration instance.
     */
    @Override
    public void dispose() {
        String s = null;
        if (serverProject != null) {
            s = serverProject.getProjectDirectory().getNameExt();
        }
        notifyDispose();
    }

/*    private void checkServerChange() {

        if (webProject != null) {
            BaseUtils.out("AbstractModuleConfiguration NOT  1  J2eeModule= " + getJ2eeModule());
                    
            J2eeModuleProvider p = webProject.getLookup().lookup(J2eeModuleProvider.class);
            final String newUri = p.getServerInstanceID();
            BaseUtils.out("AbstractModuleConfiguration checkServerChange newUri = " + newUri + "; serverInstanceId = " + serverInstanceId);

            if (serverInstanceId != null && newUri != null && !serverInstanceId.equals(newUri)) {
            //if (serverInstanceId != null && newUri != null) {                
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
        BaseUtils.out("AbstractModuleConfiguration NotifyServerChange  = " + webProject);
                        
                        notifyServerChange(newUri);
                    }
                });
                
            }
        }
    }
*/
    public boolean supportsCreateDatasource() {
        return true;
    }

    /**
     *
     *
     * @return @throws ConfigurationException
     */
    @Override
    public String getContextRoot() throws ConfigurationException {

        Properties props = getContextProperties();
        String cp = props.getProperty(CONTEXTPATH);
        if (cp == null || cp.trim().isEmpty()) {
            //Project wp = FileOwnerQuery.getOwner(Utilities.toURI(contextConfigFile));
            cp = "/" + webProject.getProjectDirectory().getNameExt();
            changeContext(cp);
        }
        return cp;
    }

    /**
     * @return a J2EE module associated with this ModuleConfiguration instance.
     */
    @Override
    public J2eeModule getJ2eeModule() {
        return module;
    }

    /**
     * Set the web context root.
     *
     * @param contextRoot context root to be set.
     * @throws ConfigurationException reports errors in setting the web context
     * root.
     */
    @Override
    public void setContextRoot(final String contextRoot) throws ConfigurationException {
        changeContext(contextRoot);
    }

}//class
