package org.netbeans.plugin.support.embedded.jetty;

import java.io.File;
import java.util.Properties;
import org.eclipse.jetty.deploy.DeploymentManager;
import org.eclipse.jetty.deploy.PropertiesConfigurationManager;
import org.eclipse.jetty.deploy.providers.WebAppProvider;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

/**
 *
 * @author V. Shyshkin
 */
public class HotDeployer {
    
    private static HotDeployer hotDeployer = null;
    
    protected static HotDeployer create() {
        if (hotDeployer != null) {
            return hotDeployer;
        }
        hotDeployer = new HotDeployer();
        hotDeployer.init();
        return hotDeployer;
    }
    public static HotDeployer getInstance(){
        return hotDeployer;
    }
    private QueuedThreadPool threadPool;
    private Server server;    
    private HandlerCollection handlers;
    private ContextHandlerCollection contextHandlers;
    private ServerConnector serverConnector;
    private DeploymentManager deployer;    
    private WebAppProvider webappProvider;

    public HotDeployer() {
    }
    
    
    private void init() { //throws Exception {
        threadPool = new QueuedThreadPool();
        System.out.println("maxThreads: " + threadPool.getMaxThreads()); // Дает 200
        threadPool.setMaxThreads(500);
        server = new Server(threadPool);
    
        
        handlers = new HandlerCollection();
        contextHandlers = new ContextHandlerCollection();
        
        handlers.addHandler(contextHandlers);
        
        server.setHandler(handlers);

        serverConnector = new ServerConnector(server, new HttpConnectionFactory());        
        serverConnector.setPort(getHttpPort());
        server.addConnector(serverConnector);  
        
        deployer = new DeploymentManager();
        deployer.setContexts(contextHandlers); //выделил, потому, что здесь context handlers
        deployer.setContextAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
                ".*/[^/]*servlet-api-[^/]*\\.jar$|.*/javax.servlet.jsp.jstl-.*\\.jar$|.*/org.apache.taglibs.taglibs-standard-impl-.*\\.jar$");
                                                                                         
        webappProvider = new WebAppProvider();
        webappProvider.setMonitoredDirName("./webapps");

	 // webdefault.xml не обязателен. 	
//        webappProvider.setDefaultsDescriptor("d:/webappsxml/webdefault.xml");
        webappProvider.setScanInterval(1);
        webappProvider.setExtractWars(true);
        webappProvider.setConfigurationManager(new PropertiesConfigurationManager());
        deployer.addAppProvider(webappProvider);
        server.addBean(deployer);  
    }
    
    public int getHttpPort() {
        Properties props = Utils.loadServerProperties(isDevelopmentMode());
        return Integer.parseInt(props.getProperty(Utils.HTTP_PORT_PROP));
    }
    public static boolean isDevelopmentMode() {
        return new File("./" + Utils.DEVELOPMENT_MODE_XML_FILE).exists();
    }

    public QueuedThreadPool getThreadPool() {
        return threadPool;
    }

    public void setThreadPool(QueuedThreadPool threadPool) {
        this.threadPool = threadPool;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public HandlerCollection getHandlers() {
        return handlers;
    }

    public void setHandlers(HandlerCollection handlers) {
        this.handlers = handlers;
    }

    public ContextHandlerCollection getContextHandlers() {
        return contextHandlers;
    }

    public void setContextHandlers(ContextHandlerCollection contextHandlers) {
        this.contextHandlers = contextHandlers;
    }

    public ServerConnector getServerConnector() {
        return serverConnector;
    }

    public void setServerConnector(ServerConnector serverConnector) {
        this.serverConnector = serverConnector;
    }

    public DeploymentManager getDeployer() {
        return deployer;
    }

    public void setDeployer(DeploymentManager deployer) {
        this.deployer = deployer;
    }

    public WebAppProvider getWebappProvider() {
        return webappProvider;
    }

    public void setWebappProvider(WebAppProvider webappProvider) {
        this.webappProvider = webappProvider;
    }
    
}
