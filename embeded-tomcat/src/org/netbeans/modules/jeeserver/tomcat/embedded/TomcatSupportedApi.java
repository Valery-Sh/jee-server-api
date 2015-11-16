package org.netbeans.modules.jeeserver.tomcat.embedded;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.jeeserver.base.embedded.apisupport.AbstractSupportedApi;
import org.netbeans.modules.jeeserver.base.embedded.apisupport.ApiDependency;

/**
 *
 * @author V. Shyshkin
 */
public class TomcatSupportedApi extends AbstractSupportedApi {
    
    protected String[] source;
    private final List<String> dataLines;
    private final String masterLine;
    
    
    public TomcatSupportedApi(String masterLine,List<String> dataLines) {
        this.masterLine = masterLine;
        this.dataLines = dataLines;
        init();
    }
    
    private void init() {
        String[] lines = masterLine.split("/");
        setName(lines[0]);
        setDisplayName(lines[1]);
        setDescription(lines[2]);
        
    }
    
    @Override
    public List<ApiDependency> getDependencies() {
        List<ApiDependency> list = new ArrayList<>();
        dataLines.stream().forEach((line) -> {
            list.add(ApiDependency.getInstance(line));
        });
        return list;
    }


}//class
