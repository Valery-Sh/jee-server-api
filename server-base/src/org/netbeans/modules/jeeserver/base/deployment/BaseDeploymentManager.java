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
package org.netbeans.modules.jeeserver.base.deployment;

import org.netbeans.modules.jeeserver.base.deployment.utils.BaseServerIconAnnotator;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.DConfigBeanVersionType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.DConfigBeanVersionUnsupportedException;
import javax.enterprise.deploy.spi.exceptions.InvalidModuleException;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.deployment.specifics.ServerSpecifics;
import org.netbeans.modules.jeeserver.base.deployment.progress.BaseDeployProgressObject;
import org.netbeans.modules.jeeserver.base.deployment.progress.BaseIncrementalProgressObject;
import org.netbeans.modules.jeeserver.base.deployment.progress.BaseRunProgressObject;
import org.netbeans.modules.jeeserver.base.deployment.progress.BaseStopProgressObject;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.spi.DeploymentContext;
import org.netbeans.modules.j2ee.deployment.plugins.spi.DeploymentManager2;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl;
import org.netbeans.modules.jeeserver.base.deployment.specifics.LogicalViewNotifier;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/**
 * The {@literal javax.enterprise.deploy.spi.DeploymentManager} implementation.
 * Provides server related information, such as, a list of deployment targets,
 * and runtime configuration information.
 *
 * @author V. Shyshkin
 */
public class BaseDeploymentManager implements DeploymentManager2 {

    private static final Logger LOG = Logger.getLogger(BaseDeploymentManager.class.getName());
    

    protected static final RequestProcessor RP = new RequestProcessor(BaseDeploymentManager.class);
    
    private Lookup lookup;

    /**
     * The deployment mode the server started.
     */
    protected Deployment.Mode currentDeploymentMode;

    private boolean waiting;

    private boolean actuallyRunning;

    /**
     * The value that uniquely identifies the instance of the class
     */
    protected final String uri;
    
    protected final String serverId;
    
    protected final BaseTarget defaultTarget;

    private FileObject serverProjectDirectory;
    
    private ServerInstanceProperties serverProperties;

    private LogicalViewNotifier logicalViewNotifier;
    

    /**
     * ExecutorTask instance of the started server
     */
    private ExecutorTask serverTask;
    /**
     * The object provides the specific server functionality.
     */
    private final ServerSpecifics specifics;

    private List<Pair<BaseTargetModuleID, BaseTargetModuleID>> initialDeployedModulesOld = new CopyOnWriteArrayList<>();

    private List<Pair<String, String>> availableModules = new CopyOnWriteArrayList<>();

    private J2eePlatformImpl platform;

    /**
     * Create a new instance of the class for a given {@literal uri}.
     *
     * @param serverId
     * @param uri the value that uniquely identifies the instance to be created
     * @param specifics
     */
    public BaseDeploymentManager(String serverId, String uri, ServerSpecifics specifics) {
        LOG.log(Level.FINE, "Creating  DeploymentManager uri={0}", uri); //NOI18N
        this.serverId = serverId;
        this.uri = uri;
        currentDeploymentMode = null; //Default
        defaultTarget = createDefaultTarget(uri);
        this.specifics = specifics;
        init();
    }

    private void init() {
        //String s = getInstanceProperties().getProperty(BaseConstants.SERVER_LOCATION_PROP);
        //serverProjectDirectory =  FileUtil.toFileObject(new File(s));
        //specifics.register(this);
        serverProperties = new ServerInstanceProperties(); 
        logicalViewNotifier = new DeploymentManagerLogicalViewNotifier(this);
        //getLookup();
    }

    public Lookup getLookup() {
        
        if (lookup == null) {
            serverProperties.setServerId(serverId);
            serverProperties.setUri(uri);
            serverProjectDirectory = getServerProjectDirectory();
//            serverProperties.setLayerProjectFolderPath(this.getLayerProjectFolderPath());

            lookup = Lookups.fixed(new Object[]{
                this,
                serverProperties,
                serverProjectDirectory,
                logicalViewNotifier,
                getSpecifics().getStartServerPropertiesProvider(this)
            });
        }
        return lookup;
    }

    public synchronized J2eePlatformImpl getPlatform() {
        return platform;
    }

    public synchronized void setPlatform(J2eePlatformImpl platform) {
        this.platform = platform;
    }

    public List<Pair<String, String>> getAvailableModules() {
        return availableModules;
    }

    public void setAvailableModules(List<Pair<String, String>> availableModules) {
        this.availableModules = availableModules;
    }

    public List<Pair<BaseTargetModuleID, BaseTargetModuleID>> getInitialDeployedModulesOld() {
        return initialDeployedModulesOld;
    }

    public void setInitialDeployedModulesOld(List<Pair<BaseTargetModuleID, BaseTargetModuleID>> initialDeployedModulesOld) {
        this.initialDeployedModulesOld = initialDeployedModulesOld;
    }

    private BaseTarget createDefaultTarget(String uri) {
        return new BaseTarget(getInstanceProperties().getProperty(BaseConstants.DISPLAY_NAME_PROP), uri);
    }

    public boolean isWaiting() {
        return waiting;
    }

    public void setWaiting(boolean waiting) {
        this.waiting = waiting;
    }

    /**
     * A convenient method to get an instance of type
     * {@literal org.netbeans.api.project.Project} that represents the server
     * instance.
     *
     * @return an object of type {@literal org.netbeans.api.project.Project}
     */
    public Project getServerProject() {
        return FileOwnerQuery.getOwner(getServerProjectDirectory());
    }
    
    public FileObject getServerProjectDirectory() {
//        return serverProjectDirectory;
        String s = getInstanceProperties().getProperty(BaseConstants.SERVER_LOCATION_PROP);
        return FileUtil.toFileObject(new File(s));
        
    }
    
    /**
     * Returns the object that represents the specific server functionality. For
     * example, Jetty module provides it's own implementation of
     * {@link ServerSpecifics}.
     *
     * @return an object of type {@literal ServerSpecifics}
     */
    public ServerSpecifics getSpecifics() {
        return specifics;
    }

    /**
     * Set the reference to the object that represents the specific server
     * functionality. For example, Jetty module provides it's own implementation
     * of {@link ServerSpecifics}.
     *
     * @param specifics object to be set
     */
    /*    public void setSpecifics(ServerSpecifics specifics) {
     this.specifics = specifics;
     }
     */
    /**
     * Returns an object of type BaseTarget, which is used to invoke various
     * methods requiring parameter of type
     * {@literal javax.enterprise.deploy.spi.Target}.
     *
     * @return the default instance of type {@literal Target}
     */
    public BaseTarget getDefaultTarget() {
        return defaultTarget;
    }

    /**
     * Returns the deployment mode in which the server is started. A server can
     * be started in one of the following deployment modes:
     * <ul>
     * <li>Deployment.Mode.RUN</li>
     * <li>Deployment.Mode.DEBUG</li>
     * <li>Deployment.Mode.PROFILE</li>
     * </ul>
     * The {@literal null} value means that the server is not started.
     *
     * @return a current mode of the server state. May return {@literal null}.
     *
     * @see
     * #setCurrentDeploymentMode(org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment.Mode)
     * @see org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment
     */
    public Deployment.Mode getCurrentDeploymentMode() {
        return currentDeploymentMode;
    }

    /**
     * Set the deployment mode in which the server is started. A server can be
     * started in one of the following deployment modes:
     * <ul>
     * <li>Deployment.Mode.RUN</li>
     * <li>Deployment.Mode.DEBUG</li>
     * <li>Deployment.Mode.PROFILE</li>
     * </ul>
     * The {@literal null} value means that the server is not started.
     *
     * @param currentDeploymentMode
     *
     * @see #getCurrentDeploymentMode()
     * @see org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment
     */
    public void setCurrentDeploymentMode(Deployment.Mode currentDeploymentMode) {
        Deployment.Mode old = this.currentDeploymentMode;
        this.currentDeploymentMode = currentDeploymentMode;
        actuallyRunning = isServerRunning();
    }

    /**
     * Updates visual representation of the project in the
     * {@literal NetBeans Project View}. Just change the icon that indicates the
     * server actuallyRunning state. The method is invoked when the {@link #currentDeploymentMode) changes.
     *
     * @param oldValue
     * @param newValue @see
     * #setCurrentDeploymentMode(org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment.Mode)
     */
    public void updateServerIconAnnotator(boolean oldValue, boolean newValue) {
        if (oldValue == newValue) {
            return;
        }
        RP.post(() -> {
            BaseServerIconAnnotator sia = Lookup.getDefault().lookup(BaseServerIconAnnotator.class);
            if (sia != null) {
                sia.serverStateChanged();
            }
            BaseUtil.out("updateServerIconAnnotator propertyChange  old=" + oldValue + "; new=" + newValue);
            getSpecifics().iconChange(getUri(),newValue);
        }, 0, Thread.NORM_PRIORITY);

    }

    /**
     * Return <code>ExecutorTask</code> of the started server.
     *
     * @return <code>ExecutorTask</code> of the started server,
     * <code>null</code> if server wasn't started by IDE.
     */
    public ExecutorTask getServerTask() {
        return serverTask;
    }

    /**
     * Set the <code>ExecutorTask</code> of the started server.
     *
     * @param serverTask the task of the started Tomcat. May be {@literal null}.
     */
    public void setServerTask(ExecutorTask serverTask) {
        this.serverTask = serverTask;
    }

    /**
     * Returns {@literal InstanceProperties) object of the registerted
     * server managed by this {@code DeploymentManager}.
     *
     * @return {@literal InstanceProperties) object of the registerted
     * server.
     */
    public InstanceProperties getInstanceProperties() {
        return InstanceProperties.getInstanceProperties(uri);
    }

    /**
     * Starts the server. Used by {@link BaseStartServer}.
     *
     * @return ProgressObject object used to monitor start server progress.
     */
    public synchronized ProgressObject startServer() {
        getSpecifics().serverStarting(this);
        getSpecifics().licensesAccepted(this);
        initialDeployedModulesOld.clear();
        BaseRunProgressObject starter = new BaseRunProgressObject(this);
        starter.fireRunning(CommandType.START, getDefaultTarget().getName());
        LOG.log(Level.INFO, "DEPLOYMENT MANAGER START SERVER");
        return starter.execute(Deployment.Mode.RUN);
    }

    /**
     * Starts the server in debug mode. Used by {@link BaseStartServer}.
     *
     * @return ProgressObject object used to monitor start server progress.
     */
    public ProgressObject startServerDebug() {
        getSpecifics().serverStarting(this);
        LOG.log(Level.INFO, "DEPLOYMENT MANAGER START SERVER (debug)");
        initialDeployedModulesOld.clear();
        BaseRunProgressObject starter = new BaseRunProgressObject(this);
        starter.fireRunning(CommandType.START, getDefaultTarget().getName());
        return starter.execute(Deployment.Mode.DEBUG);
    }

    /**
     * Starts the server in profile mode. Used by {@link BaseStartServer}.
     *
     * @return ProgressObject object used to monitor start server progress.
     */
    public ProgressObject startServerProfile() {
        getSpecifics().serverStarting(this);
        LOG.log(Level.INFO, "DEPLOYMENT MANAGER START SERVER (profile)");
        initialDeployedModulesOld.clear();
        currentDeploymentMode = null;
        BaseRunProgressObject starter = new BaseRunProgressObject(this);
        return starter.execute(Deployment.Mode.PROFILE);
    }

    /**
     * Stops the server in profile mode. Used by {@link BaseStartServer}.
     *
     * @return ProgressObject object used to monitor start server progress.
     */
    public ProgressObject stopServer() {
        LOG.log(Level.INFO, "DEPLOYMENT MANAGER STOP SERVER");
        initialDeployedModulesOld.clear();
        BaseStopProgressObject stopper = new BaseStopProgressObject(this);
        return stopper.execute();
    }

    /**
     * Returns identifier of ProjectDeploymentManager. The value is the same as
     * in {@literal URL} property of the {@literal InstanceProperties}.
     *
     * @return identifier including project directory
     */
    public String getUri() {
        return uri;
    }

    /**
     * Returns http url as a {@literal String}.
     *
     * The value returned is a string as: <br/>
     * <pre><code>http://&lt..host&gt..:&lt..port&gt..</code></pre>
     *
     * @return Returns http url as a string
     */
    public String buildUrl() {
        if (getInstanceProperties() == null) {
            return null;
        }
        String host = getInstanceProperties().getProperty(BaseConstants.HOST_PROP);
        String port = getInstanceProperties().getProperty(BaseConstants.HTTP_PORT_PROP);
        return "http://" + host + ":" + port;
    }

    public boolean isActuallyRunning() {
        return actuallyRunning;
    }

    public void setActuallyRunning(boolean running) {
        boolean old = this.actuallyRunning;
        this.actuallyRunning = running;

        updateServerIconAnnotator(old, running);
    }

    /**
     * Determines whether the server is actuallyRunning. Delegates the execution
     * of this method to the null null null null null null     {@link ServerSpecifics#pingServer(org.netbeans.api.project.Project) 
     *
     * @return {@literal true} if the server is actuallyRunning. {@literal false} otherwise.
     */
    public boolean isServerRunning() {

        setActuallyRunning(pingServer());
        return actuallyRunning;
        //return getSpecifics().pingServer(this);
    }

    public boolean pingServer() {
        
        return getSpecifics().pingServer(this);
    }

    //============================================================
    // DeploymentManager Implementation
    //============================================================
    /**
     * Retrieve the list of deployment targets supported by this
     * DeploymentManager.
     *
     * @return A list of deployment Target designators the user may select for
     * application deployment. The list contains a single object defined as
     * {@link #defaultTarget}. The method does not throw an exception.
     *
     */
    @Override
    public Target[] getTargets() throws IllegalStateException {
        return new Target[]{defaultTarget};
    }

    /**
     * Return an empty list of objects.
     *
     * @param mt
     * @param targets
     * @return an empty list of objects. The method does not throw an exception.
     * @throws javax.enterprise.deploy.spi.exceptions.TargetException
     */
    @Override
    public TargetModuleID[] getRunningModules(ModuleType mt, Target[] targets) throws TargetException, IllegalStateException {
        return new TargetModuleID[]{};
    }

    /**
     * Return an empty list of objects.
     *
     * @param mt
     * @param targets
     * @return an empty list of objects. The method does not throw an exception.
     * @throws javax.enterprise.deploy.spi.exceptions.TargetException
     */
    @Override
    public TargetModuleID[] getNonRunningModules(ModuleType mt, Target[] targets) throws TargetException, IllegalStateException {
        return new TargetModuleID[]{};
    }

    /**
     * Return an empty list of objects.
     *
     * @param mt
     * @param targets
     * @return an empty list of objects. The method does not throw an exception.
     * @throws javax.enterprise.deploy.spi.exceptions.TargetException
     */
    @Override
    public TargetModuleID[] getAvailableModules(ModuleType mt, Target[] targets) throws TargetException, IllegalStateException {
        return getAvailableModules1(mt, targets);
    }

    protected TargetModuleID[] getAvailableModules1(ModuleType mt, Target[] targets) throws TargetException, IllegalStateException {
        List<TargetModuleID> list = new ArrayList<>();

        for (Pair<BaseTargetModuleID, BaseTargetModuleID> pair : initialDeployedModulesOld) {
            if (pair.second() == null) {
                list.add(BaseTargetModuleID.getInstance(this, (BaseTarget) targets[0], pair.first().getContextPath(), pair.first().getProjectDir()));
            } else {
                list.add(BaseTargetModuleID.getInstance(this, (BaseTarget) targets[0], pair.first().getContextPath(), pair.first().getProjectDir()));
            }
        }

        BaseTargetModuleID[] a = new BaseTargetModuleID[list.size()];
        return list.toArray(a);
    }

    /**
     * When invoked throws an Exception. This method will never be called.
     *
     * @param d
     * @return
     * @throws javax.enterprise.deploy.spi.exceptions.InvalidModuleException
     */
    @Override
    public DeploymentConfiguration createConfiguration(DeployableObject d) throws InvalidModuleException {
        throw new RuntimeException("This should never be called");
    }

    /**
     *
     * Used by {@literal StartServerAction},{@literal StartServerDebugAction},
     * {@literal StartServerProfileAction} and {@literal StopServerAction}.
     *
     * @return {@literal true} if the server is not actuallyRunning.
     * {@literal false} otherwise
     * @see StartServerAction}
     * @see StartServerDebugAction}
     * @see StartServerProfileAction}
     * @see StopServerAction}
     */
    public boolean isStopped() {
        ExecutorTask t = getServerTask();
        boolean stopped = false;
        if (t == null || t.isFinished()) {
            stopped = true;
        }
        return stopped;
    }

    /**
     * Moves the archive specified by the second parameter to the designated
     * deployment targets.
     *
     * The current method implementation notifies the server of the web
     * application is to be distributed. It uses {@link #specifics} object to
     * complete the distribution.
     *
     * @param targets not used
     * @param moduleArchive an archive to be distributed. It is a war archive in
     * this implementation.
     * @param deploymentPlan not used
     * @return ProgressObject an object that tracks and reports the status of
     * the distribution process.
     * @throws IllegalStateException is thrown when the method is called when
     * the server is not actuallyRunning.
     */
    @Override
    public ProgressObject distribute(Target[] targets, File moduleArchive, File deploymentPlan) throws IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     *
     * @param fo web application <code>war</code> archive
     * @return
     * @throws IllegalStateException
     */
    public BaseTargetModuleID getModule(FileObject fo) {
        String cp = WebModule.getWebModule(fo).getContextPath();
        FileObject projectDir = FileOwnerQuery.getOwner(fo).getProjectDirectory();
        return BaseTargetModuleID.getInstance(this, defaultTarget, cp, projectDir.getPath());
    }

    /**
     * When invoked throws an {@literal UnsupportedOperationException}. This
     * method will never be called.
     *
     * @param targets
     * @param in
     * @param in1
     * @return
     */
    @Override
    public ProgressObject distribute(Target[] targets, InputStream in, InputStream in1) throws IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * When invoked throws an {@literal UnsupportedOperationException}. This
     * method will never be called.
     *
     * @param targets
     * @param mt
     * @param in
     * @param in1
     * @return
     */
    @Override
    public ProgressObject distribute(Target[] targets, ModuleType mt, InputStream in, InputStream in1) throws IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * For now just returns a {@literal ProgressObject} that indicates that the
     * normal started successfully. This is because all of the functionality is
     * in {@link #distribute(javax.enterprise.deploy.spi.Target[], java.io.File, java.io.File)
     * }
     * method. It is subject to revision.
     *
     * @param modules
     * @return
     * @throws IllegalStateException
     */
    @Override
    public ProgressObject start(TargetModuleID[] modules) throws IllegalStateException {
        LOG.log(Level.INFO, "DEPLOYMENT MANAGER: start targetModuleID={0}", modules[0]);
        if (!isServerRunning()) {
            throw new IllegalStateException("ESDeploymentManager.start called on disconnected instance");   // NOI18N
        }
        if (modules.length != 1 || !(modules[0] instanceof BaseTargetModuleID)) {
            throw new IllegalStateException("ESDeploymentManager.start invalid TargetModuleID passed");   // NOI18N
        }

        BaseIncrementalProgressObject deployer = new BaseIncrementalProgressObject(this);
        return deployer.start((BaseTargetModuleID) modules[0]);
    }

    /**
     * When invoked throws an UnsupportedOperationException. This method will
     * never be called. !!! TODO It is subject to revision.
     *
     * @param tmids
     * @return
     */
    @Override
    public ProgressObject stop(TargetModuleID[] tmids) throws IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * The method is not suppoted and always return {@literal null} value.
     *
     * @param tmids
     * @return null
     * @throws IllegalStateException
     */
    @Override
    public ProgressObject undeploy(TargetModuleID[] tmids) throws IllegalStateException {
        return new BaseDeployProgressObject(this).undeploy((BaseTargetModuleID) tmids[0], getServerProject().getProjectDirectory());
    }

    /**
     * This method designates whether the server provides application
     * redeployment functionality.
     *
     * The method always returns {@literal false} indicating that the server
     * does not provide redeployment.
     *
     * @return false
     *
     */
    @Override
    public boolean isRedeploySupported() {
        return false;
    }

    /**
     * When invoked throws an UnsupportedOperationException. This method will
     * never be called.
     *
     * @param tmids
     * @param file
     * @param file1
     * @return
     * @see #isRedeploySupported()
     */
    @Override
    public ProgressObject redeploy(TargetModuleID[] tmids, File file, File file1) throws UnsupportedOperationException, IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * When invoked throws an UnsupportedOperationException. This method will
     * never be called.
     *
     * @param tmids
     * @param in
     * @param in1
     * @return
     * @see #isRedeploySupported()
     */
    @Override
    public ProgressObject redeploy(TargetModuleID[] tmids, InputStream in, InputStream in1) throws UnsupportedOperationException, IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * The method does nothing when invoked.
     */
    @Override
    public void release() {
    }

    /**
     * The method does nothing when invoked.
     *
     * @param locale
     */
    @Override
    public void setLocale(Locale locale) throws UnsupportedOperationException {
    }

    /**
     * Returns default locale.
     *
     * @return the result if invocation {@literal Locale.getDefault()}
     * @see #getDefaultLocale()
     * @see #getSupportedLocales()
     * @see #isLocaleSupported(java.util.Locale)
     */
    @Override
    public Locale getCurrentLocale() {
        return Locale.getDefault();
    }

    /**
     * Returns default locale.
     *
     * @return the result if invocation {@literal Locale.getDefault()}
     * @see #getCurrentLocale()
     * @see #getSupportedLocales()
     * @see #isLocaleSupported(java.util.Locale)
     */
    @Override
    public Locale getDefaultLocale() {
        return Locale.getDefault();
    }

    /**
     * Returns default locale.
     *
     * @return the result if invocation {@literal Locale.getAvailableLocales()}
     * @see #getCurrentLocale()
     * @see #getDefaultLocales()
     * @see #isLocaleSupported(java.util.Locale)
     */
    @Override
    public Locale[] getSupportedLocales() {
        return Locale.getAvailableLocales();
    }

    /**
     * Returns boolean indicating whether the given locale is supported.
     *
     * @param locale
     * @return {@literal true} if the given locale is suppoted. {@literal false}
     * otherwise.
     * @see #getCurrentLocale()
     * @see #getDefaultLocales()
     * @see #getSupportedLocales()
     */
    @Override
    public boolean isLocaleSupported(Locale locale) {
        if (locale == null) {
            return false;
        }

        Locale[] supLocales = getSupportedLocales();
        for (Locale supLocale : supLocales) {
            if (locale.equals(supLocale)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Always returns {@literal null}.
     *
     * @return always {@literal null}
     */
    @Override
    public DConfigBeanVersionType getDConfigBeanVersion() {
        return null;
    }

    /**
     * Always returns {@literal false}.
     *
     * @param dcbvt
     * @return always {@literal false}
     */
    @Override
    public boolean isDConfigBeanVersionSupported(DConfigBeanVersionType dcbvt) {
        return false;
    }

    /**
     * The method does nothing.
     *
     * @param dcbvt
     * @throws DConfigBeanVersionUnsupportedException
     */
    @Override
    public void setDConfigBeanVersion(DConfigBeanVersionType dcbvt) throws DConfigBeanVersionUnsupportedException {
    }

    @Override
    public ProgressObject redeploy(TargetModuleID[] tmids, DeploymentContext dc) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ProgressObject distribute(Target[] targets, DeploymentContext context) {
        INFO.log("----------------------------------------------");
        INFO.log("-               DISTRIBUTE NEW !!!           -");
        INFO.log("----------------------------------------------");

        FileObject war = FileUtil.toFileObject(context.getModuleFile());

        BaseTargetModuleID module = getModule(war);

        BaseDeployProgressObject deployer = new BaseDeployProgressObject(this);
        return deployer.deploy(module);

    }

}
