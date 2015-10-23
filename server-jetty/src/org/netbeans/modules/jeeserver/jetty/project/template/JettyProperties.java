package org.netbeans.modules.jeeserver.jetty.project.template;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.jetty.util.JettyConstants;
import org.netbeans.modules.jeeserver.jetty.util.Utils;
import org.openide.WizardDescriptor;

/**
 *
 * @author V. Shyshkin
 */
public final class JettyProperties {

    public static String templatePath = "org/netbeans/modules/jeeserver/jetty/project/template";//JettyServerInstanceProject.zip";    
    private Properties propertyNames;
    protected String nearestVersion = null;
    protected String jettyVersion = null;    
    protected String jettyHome;

    public static final Map<String, String> versions = new HashMap<>();
    /**
     * When a new zipped template will appear add it to versions
     */
    static {
        versions.put("9.2", "JettyServerInstanceProject-9.2.0.zip");
        versions.put("9.3", "JettyServerInstanceProject-9.3.0.zip");
    }

    public static JettyProperties getInstance(String jettyHome) {
        return new JettyProperties(jettyHome);
    }
    public static JettyProperties getInstance(WizardDescriptor wiz ) {
        return new JettyProperties((String)wiz.getProperty(BaseConstants.HOME_DIR_PROP));
    }

    
    public static JettyProperties getInstance(Project project) {
        return getInstance(InstanceProperties.getInstanceProperties(Utils.buildUri(project.getProjectDirectory())));
    }

    public static JettyProperties getInstance(InstanceProperties ip) {
        JettyProperties jvs = new JettyProperties();
        jvs.jettyVersion = ip.getProperty(BaseConstants.SERVER_VERSION_PROP);
        jvs.jettyHome = ip.getProperty(BaseConstants.HOME_DIR_PROP);

        if ( jvs.jettyVersion == null ) {
            jvs.jettyVersion = Utils.getJettyVersion(jvs.jettyHome);
        }
        
        jvs.nearestVersion = convertToNearestVersion(jvs.jettyVersion);
        jvs.propertyNames = names(jvs.nearestVersion);
        return jvs;
    }
    
    
    private JettyProperties(String jettyHome) {
        this.jettyHome = jettyHome;
        jettyVersion = Utils.getJettyVersion(jettyHome);
        nearestVersion = convertToNearestVersion(jettyVersion);        
        propertyNames = names(nearestVersion);
    }
    
    private JettyProperties() {
        propertyNames = null;
    }

    
    public static String getProjectZipPath(String jettyHome) {
        String vm = Utils.getJettyVersion(jettyHome);
        String r;
        r = getMaxLessOrEqualThan(vm);
        if (r != null) {
            return templatePath + "/" + versions.get(r);
        }

        r = getMinGreaterOrEqualThan(vm);

        return templatePath + "/" + versions.get(r);

    }

    protected static String convertToNearestVersion(String vm) {
        String r;
        r = getMaxLessOrEqualThan(vm);
        if (r != null) {
            return r;
        }

        return getMinGreaterOrEqualThan(vm);

    }
    

    protected static String getMaxLessOrEqualThan(String vm) {
        int intvm = numberOf(vm);
        int max = -1;
        String r = null;
        for (String k : versions.keySet()) {
            int i = numberOf(k);
            if (i <= intvm && i > max) {
                max = i;
                r = k;
            }
        }
        return r;
    }

    protected static String getMinGreaterOrEqualThan(String vm) {
        int intvm = numberOf(vm);
        int min = -1;
        String r = null;
        for (String k : versions.keySet()) {
            int i = numberOf(k);
            if (min < 0 && i >= intvm) {
                min = i;
                r = k;
                continue;
            }
            if (i >= intvm && i < min) {
                min = i;
                r = k;
            }
        }
        return r;

    }

    protected static int numberOf(String vm) {

        String p1 = "0";
        String p2 = "0";
        String p3 = "0";

        String s = vm;

        int i = s.indexOf('.');

        if (i < 0) {
            p1 = s;
        } else {
            p1 = s.substring(0, i);
        }

        if (i >= 0) {
            s = s.substring(i + 1);

            i = s.indexOf('.');
            if (i < 0) {
                p2 = s;
            } else {
                p2 = s.substring(0, i);
            }
        }
        if (i >= 0) {
            p3 = s.substring(i + 1);
        }
        return Integer.parseInt(p1) * 100 * 100
                + Integer.parseInt(p2) * 100
                + Integer.parseInt(p3);
    }

    protected static Properties names(String vm) {
        Properties p = new Properties();
        if (vm.equals("9.2")) {
            p.setProperty(JettyConstants.JETTY_HTTP_TIMEOUT, "http.timeout");
            p.setProperty(JettyConstants.JETTY_HTTP_PORT, "jetty.port");
            p.setProperty(JettyConstants.JETTY_HTTPS_TIMEOUT, "https.timeout");
            p.setProperty(JettyConstants.JETTY_HTTPS_PORT, "https.port");
        } else if (vm.equals("9.3")) {
            p.setProperty(JettyConstants.JETTY_HTTP_TIMEOUT, "jetty.http.idleTimeout");
            p.setProperty(JettyConstants.JETTY_HTTP_PORT, "jetty.http.port");
            p.setProperty(JettyConstants.JETTY_HTTPS_TIMEOUT, "https.timeout");
            p.setProperty(JettyConstants.JETTY_HTTPS_PORT, "https.port");
        }
        return p;
    }

    
    public String getHttpPortPropertyName() {
        return propertyNames.getProperty(JettyConstants.JETTY_HTTP_PORT);
    }
    public String getTimeoutPropertyName() {
        return propertyNames.getProperty(JettyConstants.JETTY_HTTP_TIMEOUT);
    }
    
}
