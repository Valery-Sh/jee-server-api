/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jeeserver.jetty.embedded;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.specifics.InstanceBuilder;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.embedded.EmbeddedInstanceBuilder;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Valery
 */
public class JettyCustomizeInstanceBuilder extends EmbeddedInstanceBuilder {

    private static final Logger LOG = Logger.getLogger(InstanceBuilder.class.getName());

    public JettyCustomizeInstanceBuilder(Properties configProps, Options opt) {
        super(configProps, opt);
    }

    @Override
    public Set instantiate() {
        Set result = new HashSet();
        instantiateServerProperties(result);
        return result;
    }

    @Override
    protected void instantiateServerProperties(Set result) {
        Map<String, String> ipmap = getPropertyMap();
        String url = ipmap.get(BaseConstants.URL_PROP);
        String displayName = ipmap.get(BaseConstants.DISPLAY_NAME_PROP);
        InstanceProperties ip = InstanceProperties.getInstanceProperties(url);
        ip.setProperty(BaseConstants.DISPLAY_NAME_PROP,displayName);
        ip.setProperty(BaseConstants.HTTP_PORT_PROP, ipmap.get(BaseConstants.HTTP_PORT_PROP));
        ip.setProperty(BaseConstants.DEBUG_PORT_PROP, ipmap.get(BaseConstants.DEBUG_PORT_PROP));
        ip.setProperty(BaseConstants.SHUTDOWN_PORT_PROP, ipmap.get(BaseConstants.SHUTDOWN_PORT_PROP));
        ip.setProperty(BaseConstants.SERVER_VERSION_PROP, ipmap.get(BaseConstants.SERVER_VERSION_PROP));
        
        result.add(ip);
        //wiz.putProperty(ip.getProperty(BaseConstants.URL_PROP), url);
//            LOG.log(Level.SEVERE, ex.getMessage()); //NOI18N
    }

    @Override
    public InputStream getZipTemplateInputStream() {
        throw new UnsupportedOperationException("Not supported."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateWithTemplates(Set result) {
        throw new UnsupportedOperationException("Not supported."); //To change body of generated methods, choose Tools | Templates.
    }


    @Override
    protected FileObject getLibDir(Project project) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
