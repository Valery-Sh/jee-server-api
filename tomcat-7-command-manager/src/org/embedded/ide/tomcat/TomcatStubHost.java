/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.embedded.ide.tomcat;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;
import javax.management.ObjectName;
import javax.naming.directory.DirContext;
import javax.servlet.ServletException;
import org.apache.catalina.AccessLog;
import org.apache.catalina.Cluster;
import org.apache.catalina.Container;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Loader;
import org.apache.catalina.Manager;
import org.apache.catalina.Pipeline;
import org.apache.catalina.Realm;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;

/**
 *
 * @author Valery
 */
public class TomcatStubHost implements Host {
    private static Host host;
    private Host delegateHost;
    
    private TomcatStubHost(Host delegateHost) {
        this.delegateHost = delegateHost;
    }
    public static Host getHost(Host delegateHost) {
        if(host != null ) {
            return host;
        } 
        return new TomcatStubHost(delegateHost);
    }
    @Override
    public String getXmlBase() {
        return delegateHost.getXmlBase();
    }

    @Override
    public void setXmlBase(String string) {
        delegateHost.setXmlBase(string);
        
    }

    @Override
    public String getAppBase() {
        return delegateHost.getAppBase();
    }

    @Override
    public void setAppBase(String string) {
        delegateHost.setAppBase(string);
    }

    @Override
    public boolean getAutoDeploy() {
        return delegateHost.getAutoDeploy();
    }

    @Override
    public void setAutoDeploy(boolean bln) {
        delegateHost.setAutoDeploy(bln);
    }

    @Override
    public String getConfigClass() {
         return delegateHost.getConfigClass();
    }

    @Override
    public void setConfigClass(String string) {
        delegateHost.setConfigClass(string);
    }

    @Override
    public boolean getDeployOnStartup() {
        return delegateHost.getDeployOnStartup();
    }

    @Override
    public void setDeployOnStartup(boolean bln) {
        delegateHost.setDeployOnStartup(bln);
    }

    @Override
    public String getDeployIgnore() {
        return delegateHost.getDeployIgnore();
    }

    @Override
    public Pattern getDeployIgnorePattern() {
        return delegateHost.getDeployIgnorePattern();
    }

    @Override
    public void setDeployIgnore(String string) {
        delegateHost.setDeployIgnore(string);
    }

    @Override
    public ExecutorService getStartStopExecutor() {
        return delegateHost.getStartStopExecutor();
    }

    @Override
    public boolean getUndeployOldVersions() {
        return delegateHost.getUndeployOldVersions();
    }

    @Override
    public void setUndeployOldVersions(boolean bln) {
        delegateHost.setUndeployOldVersions(bln);
    }

    @Override
    public void addAlias(String string) {
        delegateHost.addAlias(string);
    }

    @Override
    public String[] findAliases() {
        return delegateHost.findAliases();
    }

    @Override
    public void removeAlias(String string) {
        delegateHost.removeAlias(string);
    }

    @Override
    public boolean getCreateDirs() {
        return delegateHost.getCreateDirs();
    }

    @Override
    public void setCreateDirs(boolean bln) {
        delegateHost.setCreateDirs(bln);
    }

    @Override
    public String getInfo() {
        return delegateHost.getInfo();
    }

    @Override
    public Loader getLoader() {
        return delegateHost.getLoader();
    }

    @Override
    public void setLoader(Loader loader) {
        delegateHost.setLoader(loader);
    }


    @Override
    public Manager getManager() {
        return delegateHost.getManager();
    }

    @Override
    public void setManager(Manager mngr) {
        delegateHost.setManager(mngr);
    }

    @Override
    public Object getMappingObject() {
        return delegateHost.getMappingObject();
    }

    @Override
    public ObjectName getObjectName() {
        return delegateHost.getObjectName();
    }

    @Override
    public Pipeline getPipeline() {
        return delegateHost.getPipeline();
    }

    @Override
    public Cluster getCluster() {
        return delegateHost.getCluster();
    }

    @Override
    public void setCluster(Cluster clstr) {
        delegateHost.setCluster(clstr);
    }

    @Override
    public int getBackgroundProcessorDelay() {
        return delegateHost.getBackgroundProcessorDelay();
    }

    @Override
    public void setBackgroundProcessorDelay(int i) {
        delegateHost.setBackgroundProcessorDelay(i);
    }

    @Override
    public String getName() {
        return delegateHost.getName();
    }

    @Override
    public void setName(String string) {
        delegateHost.setName(string);
    }

    @Override
    public Container getParent() {
        return delegateHost.getParent();
    }

    @Override
    public void setParent(Container cntnr) {
        delegateHost.setParent(cntnr);
    }

    @Override
    public ClassLoader getParentClassLoader() {
        return delegateHost.getParentClassLoader();
    }

    @Override
    public void setParentClassLoader(ClassLoader cl) {
        delegateHost.setParentClassLoader(cl);
    }

    @Override
    public Realm getRealm() {
        return delegateHost.getRealm();
    }

    @Override
    public void setRealm(Realm realm) {
        delegateHost.setRealm(realm);
    }

    @Override
    public DirContext getResources() {
        return delegateHost.getResources();
    }

    @Override
    public void setResources(DirContext dc) {
        delegateHost.setResources(dc);
    }

    @Override
    public void backgroundProcess() {
        delegateHost.backgroundProcess();
    }

    @Override
    public void addChild(Container cntnr) {
    }

    @Override
    public void addContainerListener(ContainerListener cl) {
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener pl) {
    }

    @Override
    public Container findChild(String string) {
        return delegateHost.findChild(string);
    }

    @Override
    public Container[] findChildren() {
        return delegateHost.findChildren();
    }

    @Override
    public ContainerListener[] findContainerListeners() {
        return delegateHost.findContainerListeners();
    }

    @Override
    public void invoke(Request rqst, Response rspns) throws IOException, ServletException {
        delegateHost.invoke(rqst, rspns);
    }

    @Override
    public void removeChild(Container cntnr) {
    }

    @Override
    public void removeContainerListener(ContainerListener cl) {
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener pl) {
    }

    @Override
    public void fireContainerEvent(String string, Object o) {
    }

    @Override
    public void logAccess(Request rqst, Response rspns, long l, boolean bln) {
        delegateHost.logAccess(rqst, rspns, l, bln);
    }

    @Override
    public AccessLog getAccessLog() {
        return delegateHost.getAccessLog();
    }

    @Override
    public int getStartStopThreads() {
        return delegateHost.getStartStopThreads();
    }

    @Override
    public void setStartStopThreads(int i) {
        delegateHost.setStartStopThreads(i);
    }

    @Override
    public void addLifecycleListener(LifecycleListener ll) {
    }

    @Override
    public LifecycleListener[] findLifecycleListeners() {
        return delegateHost.findLifecycleListeners();
    }

    @Override
    public void removeLifecycleListener(LifecycleListener ll) {
    }

    @Override
    public void init() throws LifecycleException {
        delegateHost.init();
    }

    @Override
    public void start() throws LifecycleException {
        delegateHost.start();
    }

    @Override
    public void stop() throws LifecycleException {
        delegateHost.stop();
    }

    @Override
    public void destroy() throws LifecycleException {
        delegateHost.destroy();
    }

    @Override
    public LifecycleState getState() {
        return delegateHost.getState();
    }

    @Override
    public String getStateName() {
        return delegateHost.getStateName() ;
    }


    @Override
    public org.apache.juli.logging.Log getLogger() {
        return delegateHost.getLogger();
    }
    
}
