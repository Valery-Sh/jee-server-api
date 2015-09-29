/**
 * This file is part of Jetty Server support in NetBeans IDE.
 *
 * Jetty Server support in NetBeans IDE is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the License,
 * or (at your option) any later version.
 *
 * Jetty Server support in NetBeans IDE is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should see the GNU General Public License here:
 * <http://www.gnu.org/licenses/>.
 */
package org.netbeans.modules.jeeserver.base.embedded.server.project.wizards;

import java.io.File;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.specifics.InstanceBuilder;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteConstants;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author V. Shyshkin
 */
public abstract class EmbeddedInstanceBuilder extends InstanceBuilder {

    private static final Logger LOG = Logger.getLogger(EmbeddedInstanceBuilder.class.getName());
    private boolean mavenbased;
    
    public EmbeddedInstanceBuilder(Properties props, InstanceBuilder.Options opt) {
        super(props,opt);
    }

    public boolean isMavenbased() {
        return mavenbased;
    }

    public void setMavenbased(boolean mavenbased) {
        this.mavenbased = mavenbased;
    }

    protected void instantiateServerInstanceDir(Set result) {

        InstanceProperties ip = null;
        for (Object o : result) {
            if (o instanceof InstanceProperties) {
                ip = (InstanceProperties) o;
                break;
            }
        }

        FileObject instanciesDir = FileUtil.toFileObject(new File(configProps.getProperty(SuiteConstants.SERVER_INSTANCES_DIR_PROP)));
        String projName = (String) getWizardDescriptor().getProperty("name");

        String instDirName = FileUtil.findFreeFolderName(instanciesDir, projName);

        Project suite = FileOwnerQuery.getOwner(instanciesDir);
        ip.setProperty(SuiteConstants.SERVER_INSTANCE_NAME_PROP, instDirName);
        ip.setProperty(SuiteConstants.SUITE_PROJECT_LOCATION, suite.getProjectDirectory().getPath());
        
//        suite.getLookup().lookup(InstanceLookups.class)
//                .put(ip.getProperty(BaseConstants.URL_PROP));
    }
    
    @Override
    protected String buildURL(String serverId, FileObject projectDir) {
        String serverInstanciesFolder = configProps.getProperty(SuiteConstants.SERVER_INSTANCES_DIR_PROP);
        String suite = new File(serverInstanciesFolder).getParent();
        return serverId + ":" + BaseConstants.URIPREFIX_NO_ID + ":" + projectDir.getPath()
                + ":server:suite:project:" +  suite;
    }
}
