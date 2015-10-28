package org.netbeans.modules.jeeserver.base.embedded.apisupport;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author V. Shyshkin
 */
public interface SupportedApi {
    String getName();
    default List<String> getJarNames() {
        List<ApiDependency> deps = getDependencies();
        List<String> list = new ArrayList<>();
        deps.forEach( d -> {
            list.add(d.getJarName());
        });
        return list;
    }
    String getDisplayName();
    String getDescription();
    boolean isAlwaysRequired();
    List<ApiDependency> getDependencies();
    
}
