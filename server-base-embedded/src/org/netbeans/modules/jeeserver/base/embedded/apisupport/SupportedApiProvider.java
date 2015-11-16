package org.netbeans.modules.jeeserver.base.embedded.apisupport;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.netbeans.modules.jeeserver.base.embedded.specifics.EmbeddedServerSpecifics;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteUtil;

/**
 *
 * @author V. Shyshkin
 */
public interface SupportedApiProvider {
    
    public SupportedApi getSupportedAPI(String apiName);
    public static SupportedApiProvider getInstance(String actualServerId) {
        //return ((EmbeddedServerSpecifics)dm.getSpecifics()).getSupportedApiProvider(dm);
        String serverId = SuiteUtil.getServerIdByAcualId(actualServerId);
        return ((EmbeddedServerSpecifics)BaseUtil.getServerSpecifics(serverId)).getSupportedApiProvider(actualServerId);
    }
    List<SupportedApi> getApiList();
    InputStream getDownloadPom(Object... options);
    Map<String,String> getServerVersionProperties(String version);
    String[] getServerVertions();    
    

}
