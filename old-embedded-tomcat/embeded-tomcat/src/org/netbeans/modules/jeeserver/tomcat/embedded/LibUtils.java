/**
 * This file is part of Tomcat Server Embedded support in NetBeans IDE.
 *
 * Tomcat Server Embedded support in NetBeans IDE is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * Tomcat Server Embedded support in NetBeans IDE is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should see the GNU General Public License here:
 * <http://www.gnu.org/licenses/>.
 */
package org.netbeans.modules.jeeserver.tomcat.embedded;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.jeeserver.base.embedded.project.EmbServerWizardIterator;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Utilities;

/**
 * The class provides static methods to work with NetBeans libraries.
 *
 * The {@code plugin} has two Library Wrapper Modules. The first provides
 * classes to help a developer to implement an Embedded Jetty9 Server. The
 * second provides classes to help to develop of an Embedded Tomcat7 Server.
 * None of these modules need not be installed with the {
 *
 * @coce plugin}. However, if one or both of the module will be installed then
 * the {@code Ant Libraries Manager} will list one or two new libraries,
 * respectively.
 *
 * @author V. Shyshkin
 */
public class LibUtils {
    
    private static final Logger LOGGER = Logger.getLogger(LibUtils.class.getName());

    /**
     * Create a new library to be presented in the
     * {@code Ant Libraries Manager}.
     *
     * @param jarName the file name of the jar archive to be added to the
     * {@code classpath} of the library
     * @param libName the library name as it appears in the
     * {@code Ant Libraries Manager} dialog.
     */
    public static void createLibrary(String jarName, String libName) {//throws IOException {
        if (LibraryManager.getDefault().getLibrary(libName) != null) {
            return;
        }

        final File jar = findModuleJar(jarName);
        if (jar == null) {
            return;
        }
        HashMap<String, List<URL>> map = new HashMap<>();
        List<URL> list = new ArrayList<>();
        try {
            list.add(Utilities.toURI(jar).toURL());
            map.put("classpath", list);
            LibraryManager.getDefault().createLibrary("j2se", libName, map);
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, ex.getMessage()); //NOI18N
        }
    }

    /**
     * Finds a jar archive by it's name.
     *
     * @param jarName the name of the jar archive file
     * @return an object of type {@code File} for the given file name
     */
    public static File findModuleJar(String jarName) {
        File file = InstalledFileLocator.getDefault().locate("modules/ext/" + jarName, null, false);
        if (file == null || !file.exists()) {
            file = null;
        }
        return file;
    }
}
