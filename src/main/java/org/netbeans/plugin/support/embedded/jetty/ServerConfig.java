package org.netbeans.plugin.support.embedded.jetty;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author V.Shyshkin
 */
public class ServerConfig {

//    private static final String JSF_LISTENERS_PROP = "jsf.listeners";
    
    private static final Logger LOG = Logger.getLogger(ServerConfig.class.getName());
    
    private final Properties serverProperties;
    private final Map<String,List<String>> map = new HashMap<>();
    
    protected ServerConfig(Properties serverProperties) {
        this.serverProperties = serverProperties;
        init();
    }

    private void init() {
        serverProperties.forEach( (k,v)  -> {
            List<String> list = new ArrayList<>();
            if ( v != null &&  ! v.toString().trim().isEmpty() ) {
                String[] a = v.toString().split(":");
                list = new ArrayList<>(Arrays.asList(a));
            }
            map.put(k.toString(), list);
        });
    }
    
    public int getHttpPort() {
        return Integer.parseInt(serverProperties.getProperty(Utils.HTTP_PORT_PROP));
    }
    
    public boolean isEnabled(String propName) {
        List<String> list = map.get(propName + ".mod");
        if ( list == null ) {
            return false;
        }
        return list.contains("active");
    }
 
    public String getProperty(String prop, List<String> list) {
        String result = null;
        for ( String s : list) {
            if ( prop.equals(s) ) {
                result = s;
                break;
            }
            String[] parts = s.split(":");
            for ( String p : parts) {
                String[] kv = p.split("->");
                
                if ( prop.equals(kv[0]) && kv.length == 2 ) {
                    result = kv[1];
                }
            }
            if ( result != null ) {
                break;
            }
        }
        return result;
    }
    public boolean isCDIEnabled() {
        return map.containsKey("cdi.mod");
    }

    public boolean isJSFEnabled() {
        boolean yes = false;
        
        for (String m : map.keySet()) {
            if (m.toLowerCase().startsWith("jsf-")) {
                yes = true;
                break;
            }
        }
        return yes;

    }

    public String getJsfListener() {
        String l = null;
        for ( Map.Entry<String,List<String>> e : map.entrySet()) {
            if ( e.getKey().startsWith("module.jsf-") && isEnabled(e.getKey())) {
                List<String> list = e.getValue();
                for ( String s : list) {
                    String[] sa = s.split("->");
                    if ( sa.length < 2 || ! "jsf-listener".equals(sa[0])) {
                        continue;
                    }
                    l = sa[1];
                    break;
                }
            }
            if ( l != null ) {
                break;
            }
        }
        return l;
    }

}//class
