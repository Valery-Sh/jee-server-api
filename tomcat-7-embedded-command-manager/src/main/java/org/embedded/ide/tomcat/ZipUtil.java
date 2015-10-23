package org.embedded.ide.tomcat;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Valery
 */
public class ZipUtil {

    private static final Logger LOG = Logger.getLogger(ZipUtil.class.getName());

    public static Path createEmptyZip(File zipFile) {
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        URI uri = URI.create("jar:file:/" + zipFile.getAbsolutePath().replace("\\", "/"));
        try (FileSystem zipfs = FileSystems.newFileSystem(uri, env)) {
            return zipfs.getPath(zipFile.getPath());
        } catch (IOException ex) {
            Logger.getLogger(ZipUtil.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("EXEPTION: " + ex.getMessage());
        }
        return null;
    }

    public static void createZip(File srcDir, File targetZip, String dirNameInTarget) {
        // Create empty zip
        //Path zip = createEmptyZip(targetZip);
        copyToZip(srcDir, targetZip, dirNameInTarget);

    }

    public static void createZip(File srcDir, File targetZip) {
        // Create empty zip
        //Path zip = createEmptyZip(targetZip);
        copyToZip(srcDir, targetZip);

    }

    public static void delete(File zipFile, String path) {
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        URI uri = URI.create("jar:file:/" + zipFile.getAbsolutePath().replace("\\", "/"));
        try (FileSystem zipfs = FileSystems.newFileSystem(uri, env)) {
            Path pathInZipfile = zipfs.getPath(path);
            deleteDirs(pathInZipfile);

        } catch (IOException ex) {
            Logger.getLogger(ZipUtil.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("EXEPTION: " + ex.getMessage());
        }
    }

    public static void deleteDirs(Path path) throws IOException {
        Path last = path;
        if (!Files.isDirectory(path)) {
            Files.delete(path);
            return;
        }
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(path)) {
            for (Path child : ds) {
                if (Files.isDirectory(child)) {
                    deleteDirs(child);

                } else {
                    Files.delete(child);
                }
            }
        }
        if (!"/".equals(path.toString())) {
            Files.delete(path);
        }
    }

    private static void copyFile(FileSystem srcfs, FileSystem targetfs, String srcFilePath, String srcFileInTarget)
            throws IOException {

        Path srcPath = srcfs.getPath(srcFilePath.replace("\\", "/"));

        Path zipPath = targetfs.getPath(srcFileInTarget);

        if (Files.isDirectory(srcPath)) {
            if (!Files.exists(zipPath)) {
                try {
                    mkdirs(zipPath);
                } catch (FileAlreadyExistsException ex) {
                    LOG.log(Level.INFO, ex.getMessage());
                }
            }
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(srcPath)) {
                for (Path child : ds) {
                    copyFile(srcfs, targetfs,
                            srcFilePath + (srcFilePath.endsWith("/") ? "" : "/") + child.getFileName(),
                            srcFileInTarget + (srcFileInTarget.endsWith("/") ? "" : "/") + child.getFileName());
                }
            }
        } else {
            Files.copy(srcPath, zipPath, StandardCopyOption.REPLACE_EXISTING);
        }

    }

    /**
     * Copies the content of the specified {@literal zip} file to a specified
     * entry of the target {@literal zip} file.
     *
     * @param srcZip the file which content is to be copied
     * @param targetZip target zip file
     * @param targetPathInZip the entry path in the target zip
     * @return 
     */
    public static boolean copyZipToZip(File srcZip, File targetZip, String targetPathInZip) {
        boolean result = true;
        //copyToZip(src, zipFile, src.getName());
        //Path srcPath = getZipPath(srcZip, "/");
        FileSystem srcfs = getZipFileSystem(srcZip);
        FileSystem targetfs = getZipFileSystem(targetZip);
        Path pathInZipfile = targetfs.getPath(targetPathInZip);
        try {
            if (!Files.exists(pathInZipfile)) {
                mkdirs(pathInZipfile);
            }
            String srcPath = srcfs.getPath("/").toString();
            copyFile(srcfs, targetfs, srcPath, targetPathInZip);
            srcfs.close();
            targetfs.close();
        } catch (FileAlreadyExistsException ex) {
            LOG.log(Level.INFO, ex.getMessage());
            result = false;
        } catch (IOException ex) {
            Logger.getLogger(ZipUtil.class.getName()).log(Level.SEVERE, null, ex);
            result = false;
        }
        return result;
        //Files.copy(srcPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
    }



    public static FileSystem getZipFileSystem(File zipFile) {
        FileSystem r = null; //to return

        Map<String, String> env = new HashMap<>();
        env.put("create", "true");

        URI uri = URI.create("jar:file:/" + zipFile.getAbsolutePath().replace("\\", "/"));

        try {
            r = FileSystems.newFileSystem(uri, env);
        } catch (IOException ex) {
            Logger.getLogger(ZipUtil.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("EXEPTION: " + ex.getMessage());
        }
        return r;

    }

    public static boolean copyToZip(File src, File zipFile) {
        if ( src.isFile() ) {
            return copyToZip(src, zipFile, "/");
        }
        return copyToZip(src, zipFile, src.getName());
    }

    public static boolean copyToZip(File src, File zipFile, String srcNameInZip) {
        boolean result = true;
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        URI uri = URI.create("jar:file:/" + zipFile.getAbsolutePath().replace("\\", "/"));
        FileSystem srcfs = FileSystems.getDefault();
        try (FileSystem zipfs = FileSystems.newFileSystem(uri, env)) {
            Path pathInZipfile = zipfs.getPath(srcNameInZip);

            if (!Files.exists(pathInZipfile)) {
                try {
                    mkdirs(pathInZipfile);
                } catch (FileAlreadyExistsException ex) {
                    LOG.log(Level.INFO, ex.getMessage());
                    result = false;
                }
            }
            String srcPath = src.getPath();
            if ( src.isFile() ) {
                Files.copy(src.toPath(),pathInZipfile.resolve(src.getName()));
                //Files.copy(src.toPath(),pathInZipfile);
            } else {
                copyFile(srcfs, zipfs, srcPath, srcNameInZip);
            }

        } catch (IOException ex) {
            Logger.getLogger(ZipUtil.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("EXEPTION: " + ex.getMessage());
            result = false;
        }
        
        return result;
    }

    public static void unzip(File zipFile, String zipEntry, File targetFolder) throws IOException {
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        URI uri = URI.create("jar:file:/" + zipFile.getAbsolutePath().replace("\\", "/"));
        Path entryPath;
        try (FileSystem zipfs = FileSystems.newFileSystem(uri, env)) {
            entryPath = zipfs.getPath(zipEntry);
            copyFile(zipfs, FileSystems.getDefault(), entryPath.toString(), targetFolder.getPath());
        } catch (IOException ex) {
            Logger.getLogger(ZipUtil.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("EXEPTION: " + ex.getMessage());
        }
    }

    public static void mkdirs(Path path) throws IOException {
        path = path.toAbsolutePath();
        Path parent = path.getParent();
        if (parent != null) {
            if (Files.notExists(parent)) {
                mkdirs(parent);
            }
        }
        Files.createDirectory(path);
    }

    public static void mkdirs(File zipFile, String path) throws IOException {
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        URI uri = URI.create("jar:file:/" + zipFile.getAbsolutePath().replace("\\", "/"));

        try {
            try (FileSystem zipfs = FileSystems.newFileSystem(uri, env)) {
                Path pathInZip = zipfs.getPath(path);
                mkdirs(pathInZip);
            }
        } catch (IOException ex) {
            System.out.println("EXCEPTION " + ex.getMessage());
        }
    }

}
