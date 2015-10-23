package org.embedded.ide.jetty;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Valery
 */
public class Copier {

    public static final int NOTIFY_BY_TIME = 0;
    public static final int NOTIFY_BY_COUNTER = 2;
    public static final int DO_NOT_NOTIFY = 4;

    private static final Logger LOG = Logger.getLogger(Copier.class.getName());

    private final File srcFile;
    private int interval;
    private int counter;
    private long startTimeMillis;
    private int notifyOption;
    private int copied;

    private InputOutput io;

    public Copier(File srcFile) {
        this(srcFile, DO_NOT_NOTIFY, 0);
    }

    public Copier(File srcFile, InputOutput io) {
        this(srcFile, io, NOTIFY_BY_TIME, 500); // TODO change to 500

    }

    public Copier(File srcFile, InputOutput io, int notifyOption, int interval) {
        this.srcFile = srcFile;
        this.notifyOption = notifyOption;
        this.interval = interval;
        this.io = io;
    }

    public Copier(File srcFile, int notifyOption, int interval) {
        this.srcFile = srcFile;
        this.notifyOption = notifyOption;
        this.interval = interval;
    }

    /**
     * Copies the source file of the object to a target file. The method doesn't
     * throw an exception. Instead it returns the {@code null} value.<br/>
     * The target file must be a directory. If not then the method returns
     * {@code null}.<br/>
     * If the target file doesn't exist then it will be created. <br>
     * If the source file is a directory then it's content will be copied (all
     * entries are copied).
     *
     * @param targetDir
     * @return
     */
    public File copyTo(File targetDir) {
        File result = null;

        startTimeMillis = System.currentTimeMillis();
        counter = 0;
        copied = 0;
        try {
            Path ps = Paths.get(targetDir.getPath());
            if (!Files.exists(ps)) {
                Files.createDirectories(ps);
            } else if (!Files.isDirectory(ps)) {
                return null;
            }
            if (srcFile.isFile()) {
                File t = Paths.get(targetDir.getPath(), srcFile.getName()).toFile();
                copy(srcFile, t);
                result = t;
            } else {
                copy(srcFile, ps.toFile());
                result = ps.toFile();
            }
        } catch (IOException ex) {
            Logger.getLogger(Copier.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public boolean copyToZip(File zipFile) {
        return Copier.ZipUtil.copy(srcFile, zipFile, "/");
    }

    public boolean copyToZip(File zipFile, String... newEntryNames) {
        return Copier.ZipUtil.copy(null, srcFile, zipFile, newEntryNames);
    }

    public void unzip(File zipFile, String zipEntry, File targetFolder) {
        Copier.ZipUtil.unzip(null, zipFile, zipEntry, targetFolder);
    }

    /**
     * Copies the source file of the object to a target file. The method doesn't
     * throw an exception. Instead it returns the {@code null} value.<br/>
     * The target file must be a directory. If not then the method returns
     * {@code null}.<br/>
     * If the target file doesn't exist then it will be created. <br>
     * If the source file is a directory then it's content will be copied (all
     * entries are copied).<br/>
     * If the source file is a directory then the content of the directory will
     * be copied (all entries are copied) to a subdirectory named
     * {@code newName} of the target directory.<br/>
     * If the source file is not a directory it will be copied to the target
     * directory with a new name that is specified by the parameter
     * {@code newName}. If the new name is the same as the old name then the
     * file will be replaced. <br/>
     *
     * @param targetDir
     * @return
     */
    public File copyTo(File targetDir, String... newName) {

        if (newName == null) {
            return null;
        }

        String newNamePath = "";
        for (String s : newName) {
            newNamePath = newNamePath + "/" + s;
        }
        File result = null;

        startTimeMillis = System.currentTimeMillis();
        counter = 0;
        copied = 0;
        try {
            Path ps = Paths.get(targetDir.getPath());
            if (srcFile.isDirectory()) {
                ps = Paths.get(targetDir.getPath(), newNamePath);
            }
            if (!Files.exists(ps)) {
                Files.createDirectories(ps);
            }
            ps = Paths.get(targetDir.getPath(), newNamePath);
            copy(srcFile, ps.toFile());
            result = ps.toFile();
        } catch (IOException ex) {
            Logger.getLogger(Copier.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public static boolean delete(File file) {
        Copier copier = new Copier(file);
        return copier.delete();
    }

    /**
     * Deletes the source file. If the source file is a directory then all the
     * entries of the source file and the directory itself will be deleted.
     * <br/>
     * The method doesn't throw an exception. Instead it returns the boolean
     * {@code false} value.<br/>
     *
     * @return the deleted file or {@code null}
     */
    public boolean delete() {
        boolean result = true;
        try {
            if (srcFile.isFile()) {
                Files.delete(srcFile.toPath());
            } else {
                deleteDirs(srcFile.toPath());
            }
        } catch (IOException ex) {
            Logger.getLogger(Copier.class.getName()).log(Level.SEVERE, null, ex);
            result = false;
        }
        return result;
    }

    /**
     * Deletes the file or directory specified by path relative to the source
     * file. If the source file is not a directory then the method returns
     * (#code null}.
     * <br/>
     * The parameter {@code subPath} specifies the position of the file to be
     * deleted relative to the source file.<br/>
     * If the file to be deleted doesn't exists then the method returns
     * {@code null}.
     * <br/>
     * The method doesn't throw an exception. Instead it returns the boolean
     * {@code false} value.<br/>
     * If {
     *
     * @coe subPatn == "/"} then the method behaves as the {@link #delete() }.
     *
     * @param subPath the path of the file to be deleted relative to the source
     * file. if subPatn == "/" then the method works as the {@link #delete() }.
     * @return the deleted file or {@code null}
     */
    public boolean delete(String subPath) {
        if (!srcFile.isDirectory()) {
            return false;
        }
        boolean result = true;
        try {
            File toDelete = Paths.get(srcFile.getPath(), subPath).toFile();
            if (!toDelete.exists()) {
                result = false;
            }
            if (toDelete.isFile()) {
                Files.delete(toDelete.toPath());
            } else {
                deleteDirs(toDelete.toPath());
            }
        } catch (IOException ex) {
            Logger.getLogger(Copier.class.getName()).log(Level.SEVERE, null, ex);
            result = false;
        }
        return result;
    }

    /**
     * Clears the content of the source file. The method doesn't throw an
     * exception. Instead it returns the {@code null} value.<br/>
     * If the source file is a directory then all the entries of the source file
     * will be deleted.
     * <br/>
     * If the source file is not a directory then the method does nothing and
     * returns {@code null}.<br/>
     *
     * @return the deleted file or {@code null}
     */
    public boolean clear() {
        boolean result = true;
        try {
            if (srcFile.isDirectory()) {
                clearDir(srcFile.toPath());
            }
        } catch (IOException ex) {
            Logger.getLogger(Copier.class.getName()).log(Level.SEVERE, null, ex);
            result = false;
        }
        return result;
    }

    private void deleteDirs(Path path) throws IOException, FileSystemException {
        clearDir(path);
        Files.delete(path);

    }

    private void clearDir(Path path) throws IOException, FileSystemException {
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(path)) {
            for (Path child : ds) {
                if (child.toFile().isDirectory()) {
                    deleteDirs(child);

                } else {
                    Files.delete(child);
                }
            }
        }
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public int isNotify() {
        return notifyOption;
    }

    public void setNotify(int notifyOption) {
        this.notifyOption = notifyOption;
    }

    protected void fireFileCopied(File f) {
        copied++;

        switch (notifyOption) {
            case DO_NOT_NOTIFY:
                break;
            case NOTIFY_BY_COUNTER:
                ++counter;
                if (counter >= interval) {
                    if (io != null) {
                        io.getOut().println(" --- Copied " + copied + " files. " + new Date());
                    }
                    counter = 0;
                }
                break;
            case NOTIFY_BY_TIME:
                if (System.currentTimeMillis() - startTimeMillis >= interval) {
                    startTimeMillis = System.currentTimeMillis();
                    if (io != null) {
                        io.getOut().println(" --- Copied " + copied + " files. " + new Date());
                    }
                }

                break;
        }

    }

    protected void copy(File srcFolder, File targetFolder) throws IOException {

        Path targetPath = targetFolder.toPath();
        Path srcPath = srcFolder.toPath();

        if (Files.isDirectory(srcPath)) {
            if (!Files.exists(targetPath)) {
                try {
                    mkdirs(targetPath);
                } catch (FileAlreadyExistsException ex) {
                    LOG.log(Level.INFO, ex.getMessage());
                }
            }
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(srcPath)) {
                for (Path child : ds) {
                    if (Files.isDirectory(child)) {
                        fireFileCopied(child.toFile());
                    }
                    copy(child.toFile(),
                            Paths.get(targetFolder.getPath(), child.toFile().getName()).toFile());
                }
            }
        } else {
            Files.copy(srcPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            fireFileCopied(srcPath.toFile());
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

    public static void mkdirs(File file) throws IOException {
        mkdirs(file.toPath());
    }

    public static class ZipUtil {

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
            copy(srcDir, targetZip, dirNameInTarget);

        }

        public static void createZip(File srcDir, File targetZip) {
            // Create empty zip
            //Path zip = createEmptyZip(targetZip);
            copy(srcDir, targetZip);

        }

        public static boolean delete(File zipFile, String path) {
            Map<String, String> env = new HashMap<>();
            env.put("create", "true");
            URI uri = URI.create("jar:file:/" + zipFile.getAbsolutePath().replace("\\", "/"));
            try (FileSystem zipfs = FileSystems.newFileSystem(uri, env)) {
                Path pathInZipfile = zipfs.getPath(path);
                deleteDirs(pathInZipfile);
                return true;

            } catch (IOException ex) {
                Logger.getLogger(ZipUtil.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("EXEPTION: " + ex.getMessage());
                return false;
            }
        }

        public static void deleteDirs(Path path) throws IOException {

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

        /*        private static void copyFile(FileSystem srcfs, FileSystem targetfs, String srcFilePath, String srcFileInTarget)
         throws IOException {

         Path srcPath = srcfs.getPath(srcFilePath.replace("\\", "/"));

         Path zipPath = targetfs.getPath(srcFileInTarget);

         if (Files.isDirectory(srcPath)) {
         if (!Files.exists(zipPath)) {
         try {
         //mkdirs(zipPath);
         Files.createDirectories(zipPath);
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
         */
        private static void copyFile(Copier copier, FileSystem srcfs, FileSystem targetfs, String srcFilePath, String srcFileInTarget)
                throws IOException {

            Path srcPath = srcfs.getPath(srcFilePath.replace("\\", "/"));

            Path zipPath = targetfs.getPath(srcFileInTarget);

            if (Files.isDirectory(srcPath)) {
                if (!Files.exists(zipPath)) {
                    try {
                        //mkdirs(zipPath);
                        Files.createDirectories(zipPath);
                    } catch (FileAlreadyExistsException ex) {
                        LOG.log(Level.INFO, ex.getMessage());
                    }
                }
                try (DirectoryStream<Path> ds = Files.newDirectoryStream(srcPath)) {
                    for (Path child : ds) {
                        if (Files.isDirectory(child)) {
                            if (copier != null) {
                                copier.fireFileCopied(child.toFile());
                            }
                        }

                        copyFile(copier, srcfs, targetfs,
                                srcFilePath + (srcFilePath.endsWith("/") ? "" : "/") + child.getFileName(),
                                srcFileInTarget + (srcFileInTarget.endsWith("/") ? "" : "/") + child.getFileName());
                    }
                }
            } else {
                Files.copy(srcPath, zipPath, StandardCopyOption.REPLACE_EXISTING);
                if (copier != null) {
                    copier.fireFileCopied(srcPath.toFile());
                }

            }

        }

        public static boolean copyZipToZip(File srcZip, File targetZip, String targetPathInZip) {
            return copyZipToZip(null, srcZip, targetZip, targetPathInZip);
        }

        /**
         * Copies the content of the specified {@literal zip} file to a
         * specified entry of the target {@literal zip} file.
         *
         * @param srcZip the file which content is to be copied
         * @param targetZip target zip file
         * @param targetPathInZip the entry path in the target zip
         * @return
         */
        static boolean copyZipToZip(Copier copier, File srcZip, File targetZip, String targetPathInZip) {
            boolean result = true;
            //copyToZip(src, zipFile, src.getName());
            //Path srcPath = getZipPath(srcZip, "/");
            //FileSystem srcfs = getZipFileSystem(srcZip);
            //FileSystem targetfs = getZipFileSystem(targetZip);
            //Path pathInZipfile = targetfs.getPath(targetPathInZip);
            try(FileSystem srcfs = getZipFileSystem(srcZip); FileSystem targetfs = getZipFileSystem(targetZip);) {
                Path pathInZipfile = targetfs.getPath(targetPathInZip);
                if (!Files.exists(pathInZipfile)) {
                    //mkdirs(pathInZipfile);
                    Files.createDirectories(pathInZipfile);
                }
                String srcPath = srcfs.getPath("/").toString();
                copyFile(copier, srcfs, targetfs, srcPath, targetPathInZip);
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
                r = FileSystems.getFileSystem(uri);
                if (r != null) {
                    r.close();
                }
            } catch (Exception ex) {
                //Logger.getLogger(ZipUtil.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Copier.ZipUtil.getZipFileSystem(File) EXEPTION: " + ex.getMessage());
            }

            try {
                r = FileSystems.newFileSystem(uri, env);
            } catch (IOException ex) {
                Logger.getLogger(ZipUtil.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("EXEPTION: " + ex.getMessage());
            }
            return r;

        }

        public static boolean copy(File src, File zipFile) {
            if (src.isFile()) {
                return copy(src, zipFile, "/");
            }
            return copy(src, zipFile, "/");
        }

        public static boolean copy(File src, File zipFile, String... newEntryNames) {
            return copy(null, src, zipFile, newEntryNames);
        }

        static boolean copy(Copier copier, File src, File zipFile, String... newEntryNames) {
            if (newEntryNames == null) {
                return false;
            }
            String srcNameInZip = "";
            for (String s : newEntryNames) {
                srcNameInZip = srcNameInZip + "/" + s;
            }
            boolean result = true;
            Map<String, String> env = new HashMap<>();
            env.put("create", "true");
            URI uri = URI.create("jar:file:/" + zipFile.getAbsolutePath().replace("\\", "/"));
            FileSystem srcfs = FileSystems.getDefault();
            try (FileSystem zipfs = FileSystems.newFileSystem(uri, env)) {
                Path pathInZipfile = zipfs.getPath(srcNameInZip);

                if (!Files.exists(pathInZipfile)) {
                    try {
                        //mkdirs(pathInZipfile);
                        Files.createDirectories(pathInZipfile);
                    } catch (FileAlreadyExistsException ex) {
                        LOG.log(Level.INFO, ex.getMessage());
                        result = false;
                    }
                }
                String srcPath = src.getPath();
                if (src.isFile()) {
                    Files.copy(src.toPath(), pathInZipfile.resolve(src.getName()));
                    //Files.copy(src.toPath(),pathInZipfile);
                } else {
                    copyFile(copier, srcfs, zipfs, srcPath, srcNameInZip);
                }

            } catch (IOException ex) {
                Logger.getLogger(ZipUtil.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("EXEPTION: " + ex.getMessage());
                result = false;
            }

            return result;
        }

        public static boolean unzip(File zipFile, String zipEntry, File targetFolder) {
            return unzip(null, zipFile, zipEntry, targetFolder);
        }

        static boolean unzip(Copier copier, File zipFile, String zipEntry, File targetFolder) {
            boolean result = true;
            Map<String, String> env = new HashMap<>();
            env.put("create", "true");
            URI uri = URI.create("jar:file:/" + zipFile.getAbsolutePath().replace("\\", "/"));
            Path entryPath;
            try (FileSystem zipfs = FileSystems.newFileSystem(uri, env)) {
                entryPath = zipfs.getPath(zipEntry);
                copyFile(copier, zipfs, FileSystems.getDefault(), entryPath.toString(), targetFolder.getPath());
            } catch (IOException ex) {
                Logger.getLogger(ZipUtil.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("EXEPTION: " + ex.getMessage());
                result = false;
            }
            return result;

        }

        public static Set<String> getZipDirectoryFileNames(File zipFile, String forPath) {
            Set<String> set = new HashSet<>();
            DirectoryStream<Path> stream = null;
            Map<String, String> env = new HashMap<>();
            env.put("create", "false");
            Path zipFilePath = zipFile.toPath();
            URI uri = URI.create("jar:file:" + zipFilePath.toUri().getPath());
            System.out.println("uri=" + uri + "; exists=" + zipFile.exists());

            Path dirPath;

            try (FileSystem zipfs = FileSystems.newFileSystem(uri, env)) {
                //entryPath = zipfs.getPath(zipEntry );
                dirPath = zipfs.getPath(forPath);
                stream = Files.newDirectoryStream(dirPath);

                for (Path p : stream) {
                    set.add(p.toRealPath().getFileName().toString());
                }
                System.out.println("---------------");
                //copyFile(copier, zipfs, FileSystems.getDefault(), entryPath.toString(), targetFolder.getPath());
            } catch (IOException ex) {
                Logger.getLogger(ZipUtil.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("EXEPTION: " + ex.getMessage());
            }
            return set;

        }

        public static boolean _try1(File zipFile) {
            boolean result = true;
            Map<String, String> env = new HashMap<>();
            env.put("create", "true");
            URI uri = URI.create("jar:file:/" + zipFile.getAbsolutePath().replace("\\", "/"));
            System.out.println("uri=" + uri + "; exists=" + zipFile.exists());

            Path entryPath;
            String zipEntry = "/";
            try (FileSystem zipfs = FileSystems.newFileSystem(uri, env)) {
                //entryPath = zipfs.getPath(zipEntry );
                entryPath = zipfs.getPath("", "web-apps-pack", "WebApp01");
                Iterable<Path> ite = zipfs.getRootDirectories();
                for (Path p : ite) {
                    System.out.println("Path in ite p=" + p);
                }
                System.out.println("---------------");
                int count = zipfs.getPath("web-apps-pack").getNameCount();
                System.out.println("name count=" + count);
                System.out.println("---------------");
                Path dir = zipfs.getPath("web-apps-pack");
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                    for (Path entry : stream) {
                        System.out.println("Stream path=" + entry.toString());
                    }
                }
                System.out.println("---------------");

                Iterator<Path> it = entryPath.iterator();
                while (it.hasNext()) {
                    Path p = it.next();
                    System.out.println("Path p=" + p);
                }
                //copyFile(copier, zipfs, FileSystems.getDefault(), entryPath.toString(), targetFolder.getPath());
            } catch (IOException ex) {
                Logger.getLogger(ZipUtil.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("EXEPTION: " + ex.getMessage());
                result = false;
            }
            return result;

        }

        /**
         * Extracts a file (not directory) from a specified zip archive.
         *
         * @param zipFile
         * @param zipEntry a string than represents a zip entry relative to the
         * root of {@literal  zipFile}. For example? if a zip file is a
         * {@literal war} archive then {@literal  "WEB-INF/jetty-web.xml"}.
         * @return a string representation of the extracted zip entry
         */
        public static String getZipEntryAsString(File zipFile, String zipEntry) {
            return unzipToString(null, zipFile, zipEntry);
        }

        public static String getZipEntryAsString(FileSystem zipfs, String zipEntry) {

            String result = null;

            Map<String, String> env = new HashMap<>();
            env.put("create", "true");

            Path entryPath = zipfs.getPath(zipEntry);
            if (!Files.exists(entryPath)) {
                result = null;
            } else {
                try (InputStream is = ZipUtil.getZipEntryInputStream(null, zipfs, entryPath.toString());) {
                    result = stringOf(is);
                } catch (IOException ex) {
                    Logger.getLogger(Copier.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
//                copyFile(copier, zipfs, FileSystems.getDefault(), entryPath.toString(), targetFolder.getPath());
            return result;
        }

        public static InputStream getZipEntryInputStream(FileSystem zipFs, String zipEntry)
                throws IOException {

            Path zipEntryPath = zipFs.getPath(zipEntry.replace("\\", "/"));
            return Files.newInputStream(zipEntryPath);
        }

        static String unzipToString(Copier copier, File zipFile, String zipEntry) {

            String result;

            Map<String, String> env = new HashMap<>();
            env.put("create", "true");
            URI uri = URI.create("jar:file:/" + zipFile.getAbsolutePath().replace("\\", "/"));
            Path entryPath;
            try (FileSystem zipfs = FileSystems.newFileSystem(uri, env)) {

                entryPath = zipfs.getPath(zipEntry);
                if (!Files.exists(entryPath)) {
                    result = null;
                } else {
                    InputStream is = ZipUtil.getZipEntryInputStream(copier, zipfs, entryPath.toString());
                    result = stringOf(is);
                }
//                copyFile(copier, zipfs, FileSystems.getDefault(), entryPath.toString(), targetFolder.getPath());
            } catch (IOException ex) {
                Logger.getLogger(ZipUtil.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("EXEPTION: " + ex.getMessage());
                result = null;
            }
            return result;
        }

        static InputStream getZipEntryInputStream(Copier copier, FileSystem zipFs, String zipEntry)
                throws IOException {

            Path zipEntryPath = zipFs.getPath(zipEntry.replace("\\", "/"));
            return Files.newInputStream(zipEntryPath);
        }

        public static String stringOf(InputStream is) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;

            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            } catch (IOException ex) {
                System.out.println("stringOf EXCEPTION" + ex.getMessage()); //NOI18N

            } finally {
                try {
                    is.close();
                } catch (IOException ex) {
                    System.out.println("stringOf close() EXCEPTION" + ex.getMessage()); //NOI18N
                }
            }

            return sb.toString();
        }

        public static void mkdirs(Path path) throws IOException {
            Files.createDirectories(path);
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

    }//class ZipUtil

    public class InputOutput {

        public PrintStream getOut() {
            return System.out;
        }
    }

}//class

