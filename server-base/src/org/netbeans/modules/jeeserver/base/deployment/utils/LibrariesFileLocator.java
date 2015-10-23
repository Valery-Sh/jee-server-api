package org.netbeans.modules.jeeserver.base.deployment.utils;

import java.io.File;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.j2ee.deployment.common.api.J2eeLibraryTypeProvider;

import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;

/**
 * URLMapper for the nbinst URL protocol. The mapper handles only the
 * translation from URL into FileObjects. The opposite conversion is not needed,
 * it is handled by the default URLMapper. The format of the nbinst URL is
 * nbinst://host/relativepath. The host part is optional, if presents it
 * specifies the name of the supplying module. The relativepath is mandatory and
 * specifies the relative relativepath from the ${netbeans.home},
 * ${netbeans.user} or ${netbeans.dirs}.
 *
 * @author Tomas Zezula
 */
public class LibrariesFileLocator {

    private static final Logger LOG = Logger.getLogger(LibrariesFileLocator.class.getName());

    public static final String NBINST_PROTOCOL = "nbinst";     //NOI18N
    public static final String JAR_PROTOCOL = "jar";     //NOI18N
    public static final String FILE_PROTOCOL = "file";     //NOI18N    
    public static final String VOLUME_TYPE = J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH;

    /**
     * Creates a new instance of LibURLDecoder
     */
    public LibrariesFileLocator() {

    }

    /**
     * Returns File for given URL
     *
     * @param url the URL for which the File should be find.
     * @return File returns null in case of unknown protocol or if the file
     * cannot be located.
     */
    public static File findFile(URL url) {
        File file = null;
        String protocol = url.getProtocol();
        if (protocol == null) {
            return null;
        }
        if (FILE_PROTOCOL.equals(protocol)) {
            return new File(url.getPath());
        }
        String path = url.getPath();
        if (NBINST_PROTOCOL.equals(protocol) || JAR_PROTOCOL.equals(protocol)) {
            path = normalizePath(url);
        } else {
            return null;
        }
        BaseUtil.out("+++++++++++ findFile.path=" + path);
        try {
            URL newurl = new URL(path);
            BaseUtil.out("+++++++++++ findFile.newurl.path=" + newurl.getPath());
            file = locateFile(newurl);
        } catch (MalformedURLException ex) {
            LOG.log(Level.INFO, ex.getMessage());
        }
        return file;
    }

    public static File getFile(URL url) {
        File file = null;
        String protocol = url.getProtocol();
        if (protocol == null) {
            return null;
        }
        String path = normalizePath(url);

        try {

            switch (protocol) {
                case FILE_PROTOCOL:
                    file = new File(new URL(path).getPath());
                    break;
                case JAR_PROTOCOL:
                    URL jarUrl = new URL(path);
                    if (FILE_PROTOCOL.equals(jarUrl.getProtocol())) {
                        
                        file = new File(jarUrl.getPath());
                    } else {
                        file = new File(path);
                    }   break;
            }//switch
        } catch (MalformedURLException ex) {
            LOG.log(Level.INFO, ex.getMessage());
            return null;
        }
        return file;
    }

    private static String normalizePath(URL url) {
        String path = url.getPath();
        while (true) {
            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            } else {
                break;
            }
        }
        if (path.endsWith("!")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    public static List<File> findFiles(Library lib) {
        final List<File> files = new ArrayList<>();

        List<URL> urls = lib.getContent(VOLUME_TYPE);
        if (urls == null) {
            return files;
        }
        urls.forEach(url -> {
            File file = findFile(url);
            if (file != null) {
                files.add(file);
            }
        });
        return files;
    }

    private static File locateFile(URL url) {
        File file = null;
        try {
            URI uri = new URI(url.toExternalForm());
            String codebase = uri.getHost();
            String relativepath = uri.getPath();
            relativepath = relativepath.substring(1);
            if (relativepath.length() > 0) {
                file = InstalledFileLocator.getDefault()
                        .locate(relativepath, codebase, false);
            }
        } catch (URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
        }
        return file;

    }

}
