/**
 * This file is part of Jetty Server support in NetBeans IDE.
 *
 * Jetty Server support in NetBeans IDE is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the License,
 * or (at your option) any later version.
 *
 * Jetty Server support in NetBeans IDE is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should see the GNU General Public License here:
 * <http://www.gnu.org/licenses/>.
 */
package org.netbeans.modules.jeeserver.jetty.deploy;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.base.deployment.ServerInstanceProperties;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.common.api.J2eeLibraryTypeProvider;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl2;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.jetty.util.JettyConstants;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Jetty's implementation of the {@code J2eePlatformImpl}.
 *
 * @author V. Shyshkin
 */
public class JettyServerPlatformImpl extends J2eePlatformImpl2 {

    private static JettyServerPlatformImpl platform;

    public static final String JETTY_LIB_DIR = "lib";

    private static final String JETTY_SERVER_ICON = "org/netbeans/modules/jeeserver/jetty/resources/jetty01-16x16.jpg";

    private String displayName;
    private ServerInstanceProperties sp;
    private BaseDeploymentManager manager;
    private JettyLibBuilder jettyLibBuilter;
    LibraryImplementation[] libraries;

    /**
     * Creates a new instance of the Jetty Installation.
     *
     * @param manager
     */
    private JettyServerPlatformImpl(BaseDeploymentManager manager) {
//        BaseUtils.out("JettyServerPlatformImpl CONSTRUCTOR " + System.currentTimeMillis());
        this.manager = manager;
        init();
    }

    public static JettyServerPlatformImpl getInstance(BaseDeploymentManager manager) {
        if (manager.getPlatform() == null) {
            JettyServerPlatformImpl p = new JettyServerPlatformImpl(manager);
            p.manager = manager;
            manager.setPlatform(p);
            p.displayName = "Jetty";
            String homePath = manager.getInstanceProperties().getProperty(BaseConstants.HOME_DIR_PROP);

            if (homePath != null) {
                FileObject fo = FileUtil.toFileObject(new File(homePath));
                if (fo != null) {
                    p.displayName += " (" + fo.getNameExt() + ")";
                }
            }
        }
        return (JettyServerPlatformImpl) manager.getPlatform();
    }

    private void init() {
    }
    public void notifyLibrariesChanged() {
        synchronized (this) {
            libraries = null;
        }
        firePropertyChange(PROP_LIBRARIES, null, getLibraries());
    }
    
    public void fireChangeEvents() {
        firePropertyChange(PROP_LIBRARIES, null, getLibraries());
    }
    public void notifyLibrariesChanged(boolean fireEvents) {
        synchronized (this) {
            libraries = null;
        }
        if ( fireEvents ) {
            firePropertyChange(PROP_LIBRARIES, null, getLibraries());
        }
    }

    public void notifyLibrariesChanged_old() {
        if (!needsReloadLibraries()) {
            return;
        }
        LibraryImplementation lib = (LibraryImplementation) libraries[0];
        loadLibraries(lib);
        firePropertyChange(PROP_LIBRARIES, null, libraries);
    }

    public boolean needsReloadLibraries() {
        J2eeLibraryTypeProvider libProvider = new J2eeLibraryTypeProvider();
        LibraryImplementation newlib = libProvider.createLibrary();

        newlib.setName(displayName);
        loadLibraries(newlib);
        List<URL> newcontent = newlib.getContent(J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH);
        //
        // If newcontent == null or is empty that something goes wrong and we cannot rebuild libraryList
        //
        if (newcontent == null || newcontent.isEmpty()) {
            return false;
        }
        if (libraries == null || libraries.length == 0 ) {
            return true;
        }
        LibraryImplementation oldlib = (LibraryImplementation) libraries[0];
        List<URL> oldcontent = oldlib.getContent(J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH);
        if (oldcontent == null || oldcontent.isEmpty()) {
            return true;
        }
        if (newcontent.size() != oldcontent.size()) {
            return true;
        }
        for (int i = 0; i < oldcontent.size(); i++) {
            URL oldurl = oldcontent.get(i);
            URL newurl = newcontent.get(i);
            if (!oldurl.equals(newurl)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public synchronized LibraryImplementation[] getLibraries() {
        if (libraries == null) {
            J2eeLibraryTypeProvider libProvider = new J2eeLibraryTypeProvider();
            LibraryImplementation lib = libProvider.createLibrary();

            lib.setName(displayName);
            loadLibraries(lib);
            libraries = new LibraryImplementation[1];
            libraries[0] = lib;

        }
        return libraries;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public Image getIcon() {
        return ImageUtilities.loadImage(JETTY_SERVER_ICON);
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
     * TODO
     *
     * @return
     */
    @Override
    public File getServerHome() {
        return new File(manager.getInstanceProperties().getProperty(BaseConstants.HOME_DIR_PROP));
    }

    /**
     * TODO
     *
     * @return
     */
    @Override
    public File getDomainHome() {
        return new File(manager.getInstanceProperties().getProperty(BaseConstants.HOME_DIR_PROP));
    }

    @Override
    public File getMiddlewareHome() {
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
     * For now returns a set that contains a single element with a value
     * {@literal J2eeModule.Type.WAR}.
     *
     * @return a set of supported types
     */
    @Override
    public Set<J2eeModule.Type> getSupportedTypes() {
        return Collections.singleton(J2eeModule.Type.WAR);
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
        Set<Profile> result = new HashSet<Profile>();

        result.add(Profile.JAVA_EE_6_WEB);
        result.add(Profile.JAVA_EE_7_WEB);
        return result;
    }

    /**
     * Returns a set of J2SE platform versions this J2EE platform can run with.
     * The plugin supports {@code jdk7}. So the result set contains a single
     * string value "1.7".
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
     * The server project {@code j2se} platform is unknown. Therefore the method
     * return {@literal null}
     *
     * @return {@literal null}
     */
    @Override
    public JavaPlatform getJavaPlatform() {
        return null;
    }

    @Override
    public Lookup getLookup() {
        return Lookups.fixed(this);
    }

    // --------------- Private helper methods -------------------------------------------------
    private void loadLibraries(LibraryImplementation lib) {
//        BaseUtils.out(" 1 JettyServerPlatformImpl loadLibraries" + System.currentTimeMillis());
        lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH, getClasses());
    }

    //public static final String JETTY_DIR = "lib";
    protected List<URL> getClasses() {
        List<URL> list = new ArrayList<>();
        Map<String, URL> target = new HashMap<>();
        jettyLibBuilter = new JettyLibBuilder(manager);

        List<String> nams = jettyLibBuilter.nams;
        nams.sort( (s1,s2)  -> {
            return s1.compareTo(s2);
        });
        List<String> mods = jettyLibBuilter.mods;
        mods.sort( (s1,s2)  -> {
            return s1.compareTo(s2);
        });
        Map<String, String> source = jettyLibBuilter.getLibPathMap();
        addJars(source, target);
        list = new ArrayList(target.values());

        return list;
    }

    protected void addJars(Map<String, String> source, Map<String, URL> target) {
        if (source == null || source.isEmpty()) {
            return;
        }
        source.forEach((k, v) -> {
            target.put(k, FileUtil.urlForArchiveOrDir(new File(v)));
        });
    }
}//class
