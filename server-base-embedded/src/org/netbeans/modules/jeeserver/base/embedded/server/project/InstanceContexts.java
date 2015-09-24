/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jeeserver.base.embedded.server.project;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.ServerInstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Valery
 */
public class InstanceContexts {
    
    private Lookup serverInstancesContext;
    
    private final Map<String, AbstractLookup> map = new ConcurrentHashMap<>();
    
    public InstanceContexts() {
        
    }
    
    public void put(String uri, AbstractLookup context) {
        map.put(uri, context);
    }
    public AbstractLookup get(String uri) {
        return map.get(uri);
    }
    public void put(String uri) {
        InstanceContent c = new InstanceContent();
        ServerInstanceProperties sip = new ServerInstanceProperties();
        c.add(sip);
        InstanceProperties props = InstanceProperties.getInstanceProperties(uri);
        sip.setServerId(props.getProperty(BaseConstants.SERVER_ID_PROP));
        sip.setUri(props.getProperty(BaseConstants.URL_PROP));        
        
        map.put(uri, new AbstractLookup(c));
    }
    
    public void remove(String uri) {
        map.remove(uri);
    }
    
    public Lookup getServerInstancesContext() {
        return serverInstancesContext;
    }

    public void setServerInstancesContext(Lookup serverInstancesContext) {
        this.serverInstancesContext = serverInstancesContext;
    }
    
    public Map<String,AbstractLookup> getURIs() {
        return map;
    }
    
    
}//class
