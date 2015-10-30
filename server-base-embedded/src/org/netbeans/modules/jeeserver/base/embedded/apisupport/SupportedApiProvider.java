package org.netbeans.modules.jeeserver.base.embedded.apisupport;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.netbeans.modules.jeeserver.base.embedded.specifics.EmbeddedServerSpecifics;

/**
 *
 * @author V. Shyshkin
 */
public interface SupportedApiProvider {
    
    public SupportedApi getSupportedAPI(String apiName);
    public static SupportedApiProvider getInstance(String serverId) {
        //return ((EmbeddedServerSpecifics)dm.getSpecifics()).getSupportedApiProvider(dm);
        return ((EmbeddedServerSpecifics)BaseUtil.getServerSpecifics(serverId)).getSupportedApiProvider();
    }
    List<SupportedApi> getApiList();
    InputStream getDownloadPom(Object... options);
    Map<String,String> getServerVersionProperties(String version);
    String[] getServerVertions();    
    

}
