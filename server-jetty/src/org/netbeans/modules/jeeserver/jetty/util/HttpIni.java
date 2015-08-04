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
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.netbeans.modules.jeeserver.jetty.project.nodes.JettyBaseRootNode;
import org.netbeans.modules.jeeserver.jetty.project.template.JettyProperties;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author V. Shyshkin
 */
public class HttpIni  extends AbsractJettyConfig { 
    private static final Logger LOG = Logger.getLogger(HttpIni.class.getName());
    private JettyProperties jvs;
    
    public HttpIni(File file) {
        setFile(file);
        Project p = FileOwnerQuery.getOwner(FileUtil.toFileObject(file));
        jvs = JettyProperties.getInstance(p);
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
    
    public String getHttpPort() {
        return getValue(jvs.getHttpPortPropertyName());
    }
    public String getHttpTimeout() {
        return getValue(jvs.getTimeoutPropertyName());
    }
    
    public void setHttpPort(String value) {
        setValue(jvs.getHttpPortPropertyName(), value);    
    }
    public void setHttpTimeout(String value) {
        setValue(jvs.getTimeoutPropertyName(), value);
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
    
    /**
     * A handler of the {@literal FileEvent } that is registered on the
     * {@literal FileObject} that is associated with a
     * {@literal server-instance-config} folder.
     */
    public static class HttpIniFileChangeHandler extends FileChangeAdapter {

        private final Project project;

        public HttpIniFileChangeHandler(Project project) {
            this.project = project;
        }

        /**
         * Called when a file is changed. Does nothing.
         *
         * @param ev the event describing context where action has taken place
         */
        @Override
        public void fileChanged(FileEvent ev) {
            JettyProperties jvs = JettyProperties.getInstance(project);
            String portProp = jvs.getHttpPortPropertyName();

            String port = BaseUtils.getServerProperties(project).getHttpPort();
            Properties props = BaseUtils.loadProperties(ev.getFile());
            if (port.equals(props.getProperty(portProp))) {
                return;
            }
            
            String uri = BaseUtils.getServerProperties(project).getUri();
            InstanceProperties ip = InstanceProperties.getInstanceProperties(uri);
            ip.setProperty(BaseConstants.HTTP_PORT_PROP, props.getProperty(portProp));
        }
    }
    
}//class
