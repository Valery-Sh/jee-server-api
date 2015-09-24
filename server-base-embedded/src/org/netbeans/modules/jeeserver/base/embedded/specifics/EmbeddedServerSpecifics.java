package org.netbeans.modules.jeeserver.base.embedded.specifics;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.base.deployment.specifics.ServerSpecifics;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.embedded.server.project.InstanceContexts;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteConstants;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 *
 * @author V. Shyshkin
 */
public interface EmbeddedServerSpecifics extends ServerSpecifics {

    static final Logger LOG = Logger.getLogger(SuiteUtil.class.getName());

    boolean supportsDistributeAs(SuiteConstants.DistributeAs distributeAs);

    default InputStream getPomFileTemplate() {
        return null;
    }

    @Override
    default Lookup getServerContext(BaseDeploymentManager dm) {
        InstanceProperties ip = dm.getInstanceProperties();

        FileObject fo = FileUtil.toFileObject(new File(ip.getProperty(SuiteConstants.SUITE_PROJECT_LOCATION)));
        if (fo == null) {
            return null;
        }
        Project suite = FileOwnerQuery.getOwner(fo);
        InstanceContexts contexts = suite.getLookup().lookup(InstanceContexts.class);
        return contexts.get(dm.getUri());
    }

}
