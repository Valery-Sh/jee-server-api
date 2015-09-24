package org.netbeans.modules.jeeserver.base.embedded.project;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteConstants;
import org.openide.filesystems.FileObject;

/**
 * The class provides a set of static methods to be used when a
 * new embedded server project is about to be created.
 * The typical use case in {@link ESServerInstanceLookupProvider}.
 * 
 * @author V. Shyshkin
 */
class RegUtils {
    private static final Logger LOG = Logger.getLogger(RegUtils.class.getName());    
    /**
     * Determines whether the specified project is 
     * an embedded server project.
     * 
     * The method should be used when a directory for the project has 
     * already been created, but the project is not yet registered in the
     * Project View by NetBeans.
     * 
     * @param p the project to be checked
     * @return 
     */
/*    public static boolean isEmbeddedServer(Project project) {
        FileObject fo = project.getProjectDirectory().getFileObject(SuiteConstants.SERVER_INSTANCE_PROPERTIES_PATH);
        return fo != null;
    }    
*/    
    /**
     * Returns embedded server configuration properties as an instance of 
     * {@literal java.util.Map } class.
     * 
     * @param serverProject 
     * @return an instance of {@literal HashMap<String,String>}. If the project 
     * doesn't contain a configuration file the returns an empty {@literal Map}.
     */
    public static Map<String, String> getPropertyMap(Project serverProject) {
        FileObject fo = serverProject.getProjectDirectory().getFileObject(SuiteConstants.INSTANCE_PROPERTIES_PATH);
        if ( fo == null ) {
            return new HashMap<>();
        }
        Properties props = new Properties();
        try(FileInputStream fos = new FileInputStream(fo.getPath());) {
            props.load(fos);
        } catch (IOException e) {
           LOG.log(Level.INFO, e.getMessage());
        }
        Map<String, String> map = new HashMap<>();

        for (Map.Entry e : props.entrySet()) {
            map.put((String) e.getKey(), (String) e.getValue());
        }
        map.put(SuiteConstants.SERVER_LOCATION_PROP, serverProject.getProjectDirectory().getPath());
        return map;
    }
    
}
