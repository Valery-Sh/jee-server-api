package org.netbeans.modules.jeeserver.jetty.embedded;

import java.util.List;
import org.netbeans.modules.jeeserver.base.embedded.apisupport.AbstractSupportedApiProvider;
import org.netbeans.modules.jeeserver.base.embedded.apisupport.SupportedApi;

/**
 *
 * @author V. Shyshkin
 */
public class JettySupportedApiProvider extends AbstractSupportedApiProvider{
    
    private final String actualServerId;
    
    public JettySupportedApiProvider(String actualServerId) {
        this.actualServerId = actualServerId;
    }

    @Override
    protected String[] getMasterLine() {
        return apiMaster;
    }


    @Override
    protected String[] getSource() {
        return source;
    }

    protected String[] apiMaster = new String[]{
        "base/Base API/The API is required for normal work af any Embedded Server",
        "jsp/Java Server Pages/The API enables the use of jsp pages in Web applications",
        "jsf-mojarra/Java ServerFaces/The API enables the use of jsf facelets or (and) jsp pages in Web applications",
        "cdi-weld/Context and Dependency Injection/The API enables the use of the Weld CDI for servlets",
        "jstl/Apache JSTL/Apache JSTL API",};

    protected String[] source = new String[]{
     
        "maven:base://org.eclipse.jetty.aggregate/jetty-all/${nb.server.version}/type=jar,classifier=uber,scope=provided/jetty-all-${nb.server.version}.jar",
        "maven:base://org.netbeans.plugin.support.embedded/jetty-9-embedded-command-manager/${command.manager.version}/jetty-9-embedded-command-manager-${command.manager.version}.jar",
        "maven:jsp://org.ow2.asm/asm-commons/5.0.1/asm-5.0.1.jar",
        "maven:jsp://javax.annotation/javax.annotation-api/1.2/javax.annotation-api-1.2.jar",
        "maven:jsp://org.eclipse.jetty.orbit/javax.mail.glassfish/1.4.1.v201005082020/javax.mail.glassfish-1.4.1.v201005082020.jar",
        "maven:jsp://javax.transaction/javax.transaction-api/1.2/javax.transaction-api-1.2.jar",
        "maven:jsp://org.eclipse.jetty/apache-jsp/${nb.server.version}/apache-jsp-${nb.server.version}",
        "maven:jsp://org.eclipse.jetty.orbit/org.eclipse.jdt.core/3.8.2.v20130121/org.eclipse.jdt.core-3.8.2.v20130121.jar",
        "maven:jsp://org.mortbay.jasper/apache-el/8.0.27/apache-el-8.0.27.jar",
        "maven:jsp://org.mortbay.jasper/apache-jsp/8.0.27/apache-jsp-8.0.27.jar",
        "maven:jsp://org.glassfish.web/el-impl/2.2/el-impl-2.2.jar",
        // -- ============== Apache JSTL =============== -->

        "maven:jstl://org.apache.taglibs/taglibs-standard-impl/1.2.5/taglibs-standard-impl-1.2.5.jar",
        "maven:jstl://org.apache.taglibs/taglibs-standard-spec/1.2.5/taglibs-standard-spec-1.2.5.jar",
        //-- ================== JSF-MOJARRA Support =================== -->

        "maven:jsf-mojarra://org.glassfish/javax.faces/2.2.11/javax.faces-2.2.11.jar",
        // -- ============== CDI-WELD Support =============== -->

        "maven:cdi-weld://com.google.guava/guava/13.0.1/guava-13.0.1.jar",
        "maven:cdi-weld://javax.enterprise/cdi-api/1.2/cdi-api-1.2.jar",
        "maven:cdi-weld://javax.inject/javax.inject/1/javax.inject-1.jar",
        "maven:cdi-weld://javax.interceptor/javax.interceptor-api/1.2/javax.interceptor-api-1.2.jar",
        "maven:cdi-weld://org.jboss.classfilewriter/jboss-classfilewriter/1.0.5.Final/jboss-classfilewriter-1.0.5.Final.jar",
        "maven:cdi-weld://org.jboss.logging/jboss-logging/3.1.3.GA/jboss-logging-3.1.3.GA.jar",
        "maven:cdi-weld://org.jboss.weld/weld-api/2.2.SP3/weld-api-2.2.SP3.jar",
        "maven:cdi-weld://org.jboss.weld/weld-core-impl/2.2.9.Final/weld-core-impl-2.2.9.Final.jar",
        "maven:cdi-weld://org.jboss.weld/weld-core-jsf/2.2.14.Final/weld-core-jsf-2.2.14.Final.jar",
        "maven:cdi-weld://org.jboss.weld.environment/weld-environment-common/2.2.9.Final/weld-environment-common-2.2.9.Final.jar",
        "maven:cdi-weld://org.jboss.weld.servlet/weld-servlet-core/2.2.9.Final/weld-servlet-core-2.2.9.Final.jar",
        "maven:cdi-weld://org.jboss.weld/weld-spi/2.2.SP3/weld-spi-2.2.SP3.jar",};

    
    @Override
    public String[] getServerVertions() {
        return new String[] {
            "9.3.5.v20151012",
            "9.3.3.v20150827",
            "9.3.2.v20150730",
            "9.3.1.v20150827",
            "9.3.0.v20150612",
        };
    }

    @Override
    protected String getCommandManagerVersion() {
        return "[1.3.1-SNAPSHOT,)";
    }

    @Override
    protected SupportedApi newApiInstance(String masterLine, List<String> apiLines) {
        return new JettySupportedApi(masterLine,apiLines);
    }
}//class JettySupportedApiProvider
