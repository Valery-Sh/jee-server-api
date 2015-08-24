package org.netbeans.modules.jeeserver.ant.base.embedded.project;

import org.netbeans.modules.jeeserver.base.deployment.ServerInstanceProperties;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.jeeserver.base.embedded.utils.EmbConstants;
import org.netbeans.modules.jeeserver.base.deployment.config.ServerInstanceAvailableModules;
import org.netbeans.modules.jeeserver.base.embedded.extender.EmbAntBuildExtender;
import org.netbeans.modules.jeeserver.base.embedded.utils.EmbUtils;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceCreationException;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author V. Shyshkin
 */
@LookupProvider.Registration(projectType = {
    "org-netbeans-modules-java-j2seproject",})
public class EmbServerInstanceLookupProvider implements LookupProvider {
    public static final Logger LOG = Logger.getLogger(EmbServerInstanceLookupProvider.class.getName());    
    /**
     * Creates additional lookup for projects that are recognized 
     * as Embedded Server Projects.
     * Additional lookup contains two elements:
     * <ul>
     *    <li>An instance of {@link ServerInstanceProperties} ;</li>
     *    <li>An Instance of {@link EmbeddedProjectOpenHook}</li>
     * </ul>
     * @param lookup
     * @return {@Lookup.EMPTY} if the project has not been recognized 
     *   as a Embedded Server. Otherwise returns {@literal Lookups.fixed} 
     *    with two objects.
     */
    @Override
    public Lookup createAdditionalLookup(final Lookup lookup) {

        final Project project = lookup.lookup(Project.class);
        Lookup lp = Lookups.fixed(new Object());
        if (!RegUtils.isEmbeddedServer(project)) {
            return Lookup.EMPTY;
        }
        if (project.getProjectDirectory().getFileObject("nbproject/project.xml") == null) {
            return  Lookup.EMPTY;
        }

        final ServerInstanceProperties serverProperties = new ServerInstanceProperties();
        final Properties props = EmbUtils.loadServerProperties(project);
        final String id = props.getProperty(EmbConstants.SERVER_ID_PROP);
        final String actualId = props.getProperty(EmbConstants.SERVER_ACTUAL_ID_PROP);
        final String uri = props.getProperty(EmbConstants.URL_PROP);
        //AvailableWebModules awm =
        serverProperties.setServerId(id);
        serverProperties.setActualServerId(actualId);        
        serverProperties.setUri(uri);
        
        props.setProperty(EmbConstants.SERVER_LOCATION_PROP, project.getProjectDirectory().getPath());

        //serverProperties.setServerConfigProperties(props);

        ProjectOpenedHook hook = new EmbeddedProjectOpenHook(project, serverProperties, props);

        project.getProjectDirectory()
                .addFileChangeListener(new DeleteProjectHandler(project, id, uri));

        
        return Lookups.fixed(serverProperties, hook, new ServerInstanceAvailableModules<>(project));
    }

    protected List<URL> getClassPathUrls(Project p, SourceGroup[] sourceGroups) {
        List<URL> urlList = new ArrayList<>();

        try {
            for (SourceGroup sourceGroup : sourceGroups) {

                FileObject fos = sourceGroup.getRootFolder();
                ClassPath cp = ClassPath.getClassPath(fos, ClassPath.COMPILE);

                List<ClassPath.Entry> l = cp.entries();
                if ( l == null ) { // ? 
                    continue;
                }
                for (ClassPath.Entry e : l) {
                    urlList.add(e.getURL());
                }
            }
        } catch (UnsupportedOperationException ex) {
            LOG.log(Level.INFO, ex.getMessage());
        }
        return urlList;

    }
    /**
     * Allows to hook open and close project actions.
     * The instances of the class is created when creating additional lookup. 
     */
    protected static class EmbeddedProjectOpenHook extends ProjectOpenedHook {

        private final Project project;
        private final ServerInstanceProperties serverProperties;
        private final Properties props;

        public EmbeddedProjectOpenHook(Project project, ServerInstanceProperties serverProperties, Properties props) {
            this.project = project;
            this.serverProperties = serverProperties;
            this.props = props;
        }

        @Override
        protected void projectOpened() {
            if (!BaseUtils.isServerProject(project)) {
                return;
            }
            String uri = serverProperties.getUri();
            try {
                InstanceProperties ip = InstanceProperties.getInstanceProperties(uri);
                if (ip == null) {
                    Map<String, String> map = getPropertyMap(project, props);                    
                    ip = InstanceProperties.createInstanceProperties(uri, null, null, "Embedded: " + project.getProjectDirectory().getName(), map);
                }
                ip.setProperty(EmbConstants.SERVER_LOCATION_PROP, project.getProjectDirectory().getPath());
            } catch (InstanceCreationException ex) {
                LOG.log(Level.INFO, ex.getMessage());
            }
            EmbAntBuildExtender ext = new EmbAntBuildExtender(project);
            ext.enableExtender();
        }

        private static Map<String, String> getPropertyMap(Project project, Properties props) {
            Map<String, String> map = new HashMap<>();

            for (Map.Entry e : props.entrySet()) {
                map.put((String) e.getKey(), (String) e.getValue());
            }
            map.put(EmbConstants.SERVER_LOCATION_PROP, project.getProjectDirectory().getPath());
            return map;
        }

        @Override
        protected void projectClosed() {
            if ( project.getProjectDirectory() == null ) {
                return;
            }
            
            FileObject webappsFolder = project.getProjectDirectory().getFileObject(EmbConstants.WEBAPPLICATIONS_FOLDER);
            // when the project deleted from the outside NetBeans IDE
            if ( webappsFolder == null ) {
                return;
            }
            FileObject[] childs = webappsFolder.getChildren();

            List<Project> plist = new ArrayList<>();
            Project[] innerProjects;
            for (FileObject fo : childs) {
                if (!fo.isFolder()) {
                    continue;
                }
                Project p = FileOwnerQuery.getOwner(fo);
                if (p.getProjectDirectory() == fo) {
                    plist.add(p);
                }
            }

            if (plist.size() > 0) {
                innerProjects = plist.toArray(new Project[plist.size()]);
                OpenProjects.getDefault().close(innerProjects);
            }
            ProjectManager.getDefault().clearNonProjectCache();
            try {
                ProjectManager.getDefault().saveProject(project);
            } catch (IOException ex) {
                LOG.log(Level.INFO, ex.getMessage());
            }
        }
    }
    /**
     * Allows to remove the registered {@literal ServerInstance} when the project deleted.
     */
    public static class DeleteProjectHandler extends FileChangeAdapter {

        private FileObject dir;
        private final String uri;
        private final String serverId;

        public DeleteProjectHandler(Project p, String id, String uri) {
            super();
            this.uri = uri;
            this.serverId = id;
            this.dir = p.getProjectDirectory();
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            super.fileDeleted(fe); //To change body of generated methods, choose Tools | Templates.
            FileObject fo = fe.getFile();
            if (fo.isFolder() && "nbproject".equals(fo.getName())) {
                dir.removeFileChangeListener(this);
                dir = null;
                InstanceProperties.removeInstance(uri);
            }
        }
    }
}
