package org.netbeans.modules.jeeserver.jetty.embedded;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.modules.jeeserver.base.embedded.apisupport.SupportedApi;
import org.netbeans.modules.jeeserver.base.embedded.apisupport.SupportedApiProvider;

/**
 *
 * @author V. Shyshkin
 */
public class JettySupportedApiProvider implements SupportedApiProvider{
    
    @StaticResource
    private static final String DOWNLOAD_POM = "org/netbeans/modules/jeeserver/jetty/embedded/resources/download-pom.xml";
//    @StaticResource
//    private static final String DOWNLOAD_BASE_POM = "org/netbeans/modules/jeeserver/jetty/embedded/resources/download-base-pom.xml";
    
    
    public JettySupportedApiProvider() {
    }

    
    @Override
    public List<SupportedApi> getApiList() {
        List<SupportedApi> list = new ArrayList<>();
        String[] names = getSupportedApiNames();
        for (String name : names) {
            list.add(getSupportedAPI(name));
        }
        return list;
    }
    

    protected String[] getSupportedApiNames() {
        List<String> list = new ArrayList<>();
        for ( String line : apiMaster) {
            String[] s = line.split("/");
            list.add(s[0]);
        }
        String[] a = new String[list.size()];
        return list.toArray(a);
    }


    protected String[] getMasterLine() {
        return apiMaster;
    }

    public SupportedApi getSupportedAPI(String apiName) {
        List<String> apiLines = new ArrayList<>();
        String[] data = getSource();
        for (String line : data) {
            String[] splited = line.split(":");
            if (apiName.toUpperCase().equals(splited[1].toUpperCase())) {
                apiLines.add(line);
            }
        }
        data = getMasterLine();
        String masterLine = null; 
        for (String line : data) {
            String[] splited = line.split("/");
            if (apiName.toUpperCase().equals(splited[0].toUpperCase())) {
                masterLine = line;
                break;
            }
        }
        return new JettySupportedApi(masterLine,apiLines);
        
    }

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
    public InputStream getDownloadPom(Object... options) {
/*        if ( options.length > 0 && ( options[0] instanceof SupportedApi) ) {
            SupportedApi api = (SupportedApi) options[0];
            if ( "BASE".equals(api.getName().toUpperCase()) ) {
                return getClass().getClassLoader().getResourceAsStream(DOWNLOAD_BASE_POM);
            }
        }
*/        
        return getClass().getClassLoader().getResourceAsStream(DOWNLOAD_POM);
    }

    @Override
    public Map<String, String> getServerVersionProperties(String version) {

        Map<String,String> map = new HashMap<>();
        map.put("nb.server.version",version);
/*        map.put("command.manager.groupId", "org.netbeans.plugin.support.embedded");    
        map.put("command.manager.artifactId", "jetty-9-embedded-command-manager");    
*/        
        map.put("command.manager.version", "[1.3.1-SNAPSHOT,)");            

        return map;
    }
    
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

    public static class JettyDefaultAPIProvider {

    }//
}//class JettySupportedApiProvider
