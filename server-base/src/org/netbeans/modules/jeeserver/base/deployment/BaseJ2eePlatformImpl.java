/**
 * This file is part of Base JEE Server suppport in NetBeans IDE.
 *
 * Base JEE Server support in NetBeans IDE is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * Base JEE Server support in NetBeans IDE is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should see the GNU General Public License here:
 * <http://www.gnu.org/licenses/>.
 */
package org.netbeans.modules.jeeserver.base.deployment;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.deployment.common.api.J2eeLibraryTypeProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;

/**
 *
 * @author V. Shyshkin
 */
public class BaseJ2eePlatformImpl extends J2eePlatformImpl implements PropertyChangeListener {

    private static final Logger LOG = Logger.getLogger(BaseJ2eePlatformImpl.class.getName());

    private LibraryImplementation[] libraries;

    private final BaseDeploymentManager manager;

    @StaticResource
    private static final String IMAGE = "org/netbeans/modules/jeeserver/base/deployment/resources/server.png";

    /**
     * Creates a class instance for the given deployment manager. Invokes {@link #loadLibraries()
     * }
     *
     * @param manager
     */
    public BaseJ2eePlatformImpl(DeploymentManager manager) {
        this.manager = (BaseDeploymentManager) manager;
        init();
    }
    ClassPath rootClassPath = null;

    private void init() {
        loadLibraries();
        FileObject f = getSourceRoot();
        rootClassPath = ClassPath.getClassPath(f, ClassPath.COMPILE);
        if (rootClassPath == null) {
            return;
        }
        rootClassPath.addPropertyChangeListener(this);
        /*        rootClassPath.addPropertyChangeListener((PropertyChangeEvent evt) -> {
         if (ClassPath.PROP_ROOTS.equals(evt.getPropertyName())) {
         BaseUtils.out("BaseJ2eePlatformImpl rootClassPath listener " + rootClassPath);
                
         notifyLibrariesChanged();// Update your stuff, because classpath roots have changed.
         }
         });
         */
    }

    /**
     * We must realize that NetBeans uses this method to create java class path
     * for web projects and not for server.
     *
     * @return platform libraries
     */
    @Override
    public LibraryImplementation[] getLibraries() {
        return libraries.clone();
    }

    /**
     * Returns platform's display name.
     *
     * @return platform display name
     */
    @Override
    public String getDisplayName() {

        return "JEE Server";
    }

    /**
     * Return an icon describing the platform. This will be mostly the icon used
     * for server instance nodes
     *
     * @return icon describing the platform
     */
    @Override
    public Image getIcon() {
        return ImageUtilities.loadImage(IMAGE);
    }

    /**
     *
     * @return always returns {@literal null}
     */
    @Override
    public File[] getPlatformRoots() {
        return null;
    }

    /**
     * @param arg0
     * @return an empty list of objects of type {@literal File}
     */
    @Override
    public File[] getToolClasspathEntries(String arg0) {
        return new File[]{};
    }

    /**
     * @param arg0
     * @return false
     */
    @Override
    public boolean isToolSupported(String arg0) {
        return false;
    }

    /**
     * For now the plugin supports Jdk 6 and Jdk 7.
     *
     * @return a set of two elements. The first element has a value of "1,6".
     * The second - "1.7"
     *
     */
    @Override
    public Set<String> getSupportedSpecVersions() {

        Set<String> result = new HashSet<>();
        result.add("1.6");
        result.add("1.7");
        result.add("1.8");
        return result;
    }

    /**
     * Returns a set of supported profiles. The set contains two elements:
     * <ul>
     * <li>Profile.JAVA_EE_6_WEB</li>
     * <li>Profile.JAVA_EE_7_WEB</li>
     * </ul>
     *
     * @return a set of supported profiles
     */
    @Override
    public Set<Profile> getSupportedProfiles() {
        Set<Profile> result = new HashSet<>();

        result.add(Profile.JAVA_EE_6_WEB);
        result.add(Profile.JAVA_EE_7_WEB);
        return result;
    }

    /**
     * Returns a list of supported J2EE module types. Since the method is
     * deprecated it delegates to {@link #getSupportedTypes() }
     *
     * @return a list of supported J2EE module types.
     */
    @Override
    public Set getSupportedModuleTypes() {
        return getSupportedTypes();
    }

    /**
     * For now returns a set that contains a single element with a value
     * {@literal J2eeModule.Type.WAR}.
     *
     * @return a set of supported types
     */
    @Override
    public Set getSupportedTypes() {
        Set<Object> result = new HashSet<>();
        result.add(J2eeModule.Type.WAR);
        return result;
    }

    /**
     * Returns a set of J2SE platform versions this J2EE platform can run with.
     * The {@code plugin} supports {@code jdk7}. So the result set contains a
     * single string value "1.7".
     *
     * @return a set of J2SE platform versions
     */
    @Override
    public Set getSupportedJavaPlatformVersions() {
        Set<String> versions = new HashSet<>();
        versions.add("1.7"); // NOI18N
        versions.add("1.8"); // NOI18N
        return versions;

    }

    /**
     * The server j2se platform is unknown. Therefore the method return
     * {@literal null}
     *
     * @return {@literal null}
     */
    @Override
    public JavaPlatform getJavaPlatform() {
        return null;
    }

    /**
     *
     */
    protected void notifyLibrariesChanged() {
        synchronized (this) {
            libraries = null;
        }
        //if (fireEvents) {
        LibraryImplementation[] libs = loadLibraries();
        firePropertyChange(PROP_LIBRARIES, null, libs);
        //}        
        // Reload libraries
//BaseUtils.out("PLATFORM: notifyLibrariesChanged" );        
//        loadLibraries();
        // Fire changes
//        firePropertyChange(PROP_LIBRARIES, null, libraries.clone());
    }

    /**
     * Returns the library name.
     *
     * When creates an instance of the server, it is assigned the string
     * identifier. For example "jetty9" or "tomcat7". This identifier is a
     * result of the method invocation.
     *
     * @return a name of the library
     */
    protected String getLibraryName() {
        return manager.getInstanceProperties().getProperty(BaseConstants.SERVER_ID_PROP);
    }

    private synchronized LibraryImplementation[] loadLibraries() {
        LibraryImplementation lib = createLibraryByServerProject();
        if (lib == null) {
            libraries = new LibraryImplementation[]{};
        } else {
            libraries = new LibraryImplementation[]{lib};
        }
        return libraries;
    }

    private LibraryImplementation createLibraryByServerProject() {

        if (manager == null || manager.getInstanceProperties() == null) {
            return null;
        }

        List<URL> urls = getServerClassPathUrls();
        if (urls.isEmpty()) {
            return null;
        }
        // Create library
        LibraryImplementation library = new J2eeLibraryTypeProvider().createLibrary();
        // Set name
        library.setName("Created library");
        // Set content
        library.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH, urls);

        // Store it
        return library;
    }



    protected List<URL> getServerClassPathUrls() {
        List<URL> urlList = new ArrayList<>();
        Sources sources = ProjectUtils.getSources(manager.getServerProject());
        SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        try {
            for (SourceGroup sourceGroup : sourceGroups) {

                FileObject fos = sourceGroup.getRootFolder();

                ClassPath classPath = ClassPath.getClassPath(fos, ClassPath.COMPILE);
                if (classPath != null) {
                    //BaseUtil.out("BaseJ2eePlatformimpl ClassPath.toString==" + classPath.toString());
                }

                /*              classPath.addPropertyChangeListener((PropertyChangeEvent evt) -> {
                 if (ClassPath.PROP_ROOTS.equals(evt.getPropertyName())) {
                 notifyLibrariesChanged();// Update your stuff, because classpath roots have changed.
                 }
                 });
                 */
//                BaseUtils.out("PLATFORM newClasspath=" + classPath);
                List<ClassPath.Entry> l = classPath.entries();
                int i = 0;
                for (ClassPath.Entry e : l) {
                    File file = FileUtil.archiveOrDirForURL(e.getURL());
//                    BaseUtil.out("BaseJ2eePlatformimpl gclass entry file.getPath=" + file.getPath());
                    if (file == null || file.isDirectory() || !file.exists()) {
                        continue;
                    }
                    if (!file.getName().endsWith(".jar")) {
                        continue;
                    }
                    URI uri = FileUtil.toFileObject(file).toURI();

                    try {
                        URL url = uri.toURL();
                        if (urlList.contains(url)) {
                            continue;
                        }
                        urlList.add(uri.toURL());
                    } catch (MalformedURLException ex) {
                        LOG.log(Level.INFO, ex.getMessage());
                    }
                }
            }
        } catch (UnsupportedOperationException ex) {
            LOG.log(Level.INFO, ex.getMessage());

        }
        return urlList;

    }

    protected FileObject getSourceRoot() {
        Sources sources = ProjectUtils.getSources(manager.getServerProject());
        SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);

        FileObject result = null;
        try {
            for (SourceGroup sourceGroup : sourceGroups) {
                result = sourceGroup.getRootFolder();
                break;

            }
        } catch (UnsupportedOperationException ex) {
            LOG.log(Level.INFO, ex.getMessage());
        }
        return result;
    }


    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (ClassPath.PROP_ROOTS.equals(evt.getPropertyName())) {
//                BaseUtils.out("BaseJ2eePlatformImpl rootClassPath listener " + rootClassPath);
            notifyLibrariesChanged();// Update your stuff, because classpath roots have changed.
        }

    }
}
