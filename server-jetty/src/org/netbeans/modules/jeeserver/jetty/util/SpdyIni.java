/**
 * This file is part of Jetty Server support in NetBeans IDE.
 *
 * Jetty Server support in NetBeans IDE is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * Jetty Server support in NetBeans IDE is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should see the GNU General Public License here:
 * <http://www.gnu.org/licenses/>.
 */
package org.netbeans.modules.jeeserver.jetty.util;

import java.io.File;
import java.util.logging.Logger;

/**
 *
 * @author Valery
 */
public class SpdyIni  extends AbsractJettyConfig { 
    private static final Logger LOG = Logger.getLogger(SpdyIni.class.getName());
    
    public SpdyIni(File file) {
        setFile(file);
        
    }
    public int propertyLine(String propName) {
        int idx = -1;
        for (int i = 0; i < lines().size(); i++) {
            if (lines().get(i).startsWith(propName + "=")) {
                idx = i;
                break;
            }
        }
        return idx;
    }
    
    public String getSpdyPort() {
        return getValue(JettyConstants.JETTY_SPDY_PORT);
    }
    public String getSpdyTimeout() {
        return getValue("spdy.timeout");
    }
    
    public void setSpdyPort(String value) {
        setValue("spdy.port", value);    
    }
    public void setSpdyTimeout(String value) {
        setValue("spdy.timeout", value);
    }
    protected void setValue(String prop,String value) {
        int idx = propertyLine(prop);
        if ( idx < 0 && value == null ) {        
            return;
        }
        if ( idx < 0 ) {
            lines().add(prop + "=" + value);
        } else if ( value != null ) {
            lines().set(idx, prop + "=" + value);
        } else {
            lines().remove(idx);
        }
        
    }
    public String getValue(String key) {
        int idx = propertyLine(key);
        if ( idx == -1 ) {
            return null;
        }
        String line = lines().get(idx);
        String[] pair = line.split("=");
        if ( pair.length < 2 ) {
            return null;
        }
        return pair[1].trim();
    }
}
