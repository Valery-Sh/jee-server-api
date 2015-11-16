package org.netbeans.modules.jeeserver.base.embedded.apisupport;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;

/**
 *
 * @author V. Shyshkin
 */
public abstract class AbstractSupportedApiProvider implements SupportedApiProvider{
    
    @StaticResource
    private static final String DOWNLOAD_POM = "org/netbeans/modules/jeeserver/base/embedded/resources/download-pom.xml";

    protected abstract String[] getSource();
    protected abstract String getCommandManagerVersion();
    protected abstract String[] getMasterLine();
    protected abstract SupportedApi newApiInstance(String masterLine,List<String> apiLines);
    
    @Override
    public List<SupportedApi> getApiList() {
        List<SupportedApi> list = new ArrayList<>();
        String[] names = getSupportedApiNames();
        for (String name : names) {
            list.add(getSupportedAPI(name));
        }
        return list;
    }
    

    protected String[] getSupportedApiNames() {
        List<String> list = new ArrayList<>();
        for ( String line : getMasterLine()) {
            String[] s = line.split("/");
            list.add(s[0]);
        }
        String[] a = new String[list.size()];
        return list.toArray(a);
    }

    @Override
    public SupportedApi getSupportedAPI(String apiName) {
        List<String> apiLines = new ArrayList<>();
        String[] data = getSource();
        for (String line : data) {
            String[] splited = line.split(":");
            if (apiName.toUpperCase().equals(splited[1].toUpperCase())) {
                apiLines.add(line);
            }
        }
        data = getMasterLine();
        String masterLine = null; 
        for (String line : data) {
            String[] splited = line.split("/");
            if (apiName.toUpperCase().equals(splited[0].toUpperCase())) {
                masterLine = line;
                break;
            }
        }
        return newApiInstance(masterLine,apiLines);
    }

    @Override
    public InputStream getDownloadPom(Object... options) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(DOWNLOAD_POM);
        //return getClass().getClassLoader().getResourceAsStream(DOWNLOAD_POM);
    }

    @Override
    public Map<String, String> getServerVersionProperties(String version) {

        Map<String,String> map = new HashMap<>();
        map.put(BaseConstants.NB_SERVER_VERSION,version);
        map.put("command.manager.version", getCommandManagerVersion());            

        return map;
    }
    
}//class JettySupportedApiProvider
