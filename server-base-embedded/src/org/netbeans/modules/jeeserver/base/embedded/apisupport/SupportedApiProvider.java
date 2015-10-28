package org.netbeans.modules.jeeserver.base.embedded.apisupport;

import java.io.InputStream;
import java.util.List;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.base.embedded.specifics.EmbeddedServerSpecifics;

/**
 *
 * @author V. Shyshkin
 */
public interface SupportedApiProvider {
    
    public SupportedApi getSupportedAPI(String apiName);
    public static SupportedApiProvider getInstance(BaseDeploymentManager dm) {
        return ((EmbeddedServerSpecifics)dm.getSpecifics()).getSupportedApiProvider();
    }
    List<SupportedApi> getApiList();
    InputStream getDownloadPom();

}
