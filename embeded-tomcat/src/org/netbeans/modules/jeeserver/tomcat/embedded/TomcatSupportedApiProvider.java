package org.netbeans.modules.jeeserver.tomcat.embedded;

import java.util.List;
import org.netbeans.modules.jeeserver.base.embedded.apisupport.AbstractSupportedApiProvider;
import org.netbeans.modules.jeeserver.base.embedded.apisupport.SupportedApi;

/**
 *
 * @author V. Shyshkin
 */
public class TomcatSupportedApiProvider extends AbstractSupportedApiProvider {

    private final String actualServerId;

    public TomcatSupportedApiProvider(String actualServerId) {
        this.actualServerId = actualServerId;
    }

    @Override
    protected String[] getMasterLine() {
        return apiMaster;
    }

    protected String[] getSource() {
        return source;
    }

    protected String[] apiMaster = new String[]{
        "base/Base API/The API is required for normal work af any Embedded Server",
        "jsf-mojarra/Java ServerFaces/The API enables the use of jsf facelets or (and) jsp pages in Web applications",};

    protected String[] source = new String[]{
        "maven:base://org.netbeans.plugin.support.embedded/tomcat-7-embedded-command-manager/${command.manager.version}/tomcat-7-embedded-command-manager-${command.manager.version}.jar",
        "maven:base://org.apache.tomcat.embed/tomcat-embed-core/${nb.server.version}/tomcat-embed-core-${nb.server.version}.jar",
        "maven:base://org.apache.tomcat.embed/tomcat-embed-logging-juli/${nb.server.version}/tomcat-embed-logging-juli-${nb.server.version}.jar",
        "maven:base://org.apache.tomcat.embed/tomcat-embed-jasper/${nb.server.version}/tomcat-embed-jasper-${nb.server.version}.jar",
        "maven:base://org.apache.tomcat.embed/tomcat-embed-logging-log4j/${nb.server.version}/tomcat-embed-logging-log4j-${nb.server.version}.jar",
        "maven:base://org.apache.tomcat.embed/tomcat-embed-websocket/${nb.server.version}/tomcat-embed-websocket-${nb.server.version}.jar",
        "maven:base://org.apache.tomcat.embed/tomcat-embed-el/${nb.server.version}/tomcat-embed-el-${nb.server.version}.jar",
        "maven:base://org.apache.tomcat/tomcat-dbcp/${nb.server.version}/tomcat-dbcp-${nb.server.version}.jar",
        "maven:base://org.eclipse.jdt.core.compiler/ecj/4.4.2/ecj-4.4.2.jar",
        //-- ================== JSF-MOJARRA Support =================== -->

        "maven:jsf-mojarra://org.glassfish/javax.faces/2.2.11/javax.faces-2.2.11.jar",};

    @Override
    public String[] getServerVertions() {
        String[] versions;
        if (actualServerId.startsWith("tomcat-7")) {
            versions = new String[]{
                "7.0.65",
                "7.0.64",
                "7.0.59",};
        } else {
            versions = new String[]{
                "8.0.28",
                "8.0.27",
                "8.0.26",
                "8.0.24",};
        }
        return versions;
    }

    @Override
    protected String getCommandManagerVersion() {
        return "[1.0.64-SNAPSHOT,)";
    }

    @Override
    protected SupportedApi newApiInstance(String masterLine, List<String> apiLines) {
        return new TomcatSupportedApi(masterLine, apiLines);
    }

}//class TomcatSupportedApiProvider
