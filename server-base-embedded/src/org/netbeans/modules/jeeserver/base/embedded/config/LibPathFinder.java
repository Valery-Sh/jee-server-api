package org.netbeans.modules.jeeserver.base.embedded.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemLoopException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Valery
 */
public class LibPathFinder extends SimpleFileVisitor<Path> {

    private static final Logger LOG = Logger.getLogger(LibPathFinder.class.getName());

    private static final char GLOB_CHARS[] = "*?".toCharArray();
    private static final char SYNTAXED_GLOB_CHARS[] = "{}[]|:".toCharArray();
    private static final Path EMPTY_PATH = new File(".").toPath();

    private final static EnumSet<FileVisitOption> SEARCH_VISIT_OPTIONS = EnumSet.of(FileVisitOption.FOLLOW_LINKS);

    // internal tracking of prior notified paths (to avoid repeated notification of same ignored path)
    private static final Set<Path> NOTIFIED_PATHS = new HashSet<>();

    private boolean includeDirsInResults = false;
    private final Map<String, Path> pathMap = new HashMap<>();
    private Path basePath = null;

    private PathMatcher dirMatcher;// = new NonHiddenMatcher();
    private PathMatcher fileMatcher;// = new NonHiddenMatcher();

    public LibPathFinder() {
        super();
        init();
    }

    private void init() {
        dirMatcher = new PathMatcher() {
            @Override
            public boolean matches(Path path) {
                try {
                    return !Files.isHidden(path);
                } catch (IOException ex) {
                    LOG.log(Level.WARNING, ex.getMessage());
                    return false;
                }
            }
        };
        fileMatcher = new PathMatcher() {
            @Override
            public boolean matches(Path path) {
                try {
                    return !Files.isHidden(path);
                } catch (IOException ex) {
                    LOG.log(Level.WARNING, ex.getMessage());
                    return false;
                }
            }
        };
        
    }

    private void putPath(Path path) {
        String relPath = basePath.relativize(path).toString();
        pathMap.put(relPath, path);
    }

    public List<File> getFileList() {
        List<File> list = new ArrayList<>();
        for (Path path : pathMap.values()) {
            list.add(path.toFile());
        }
        return list;
    }

    public Collection<Path> getPathMap() {
        return pathMap.values();
    }

    public boolean isIncludeDirsInResults() {
        return includeDirsInResults;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (dirMatcher.matches(dir)) {
            LOG.log(Level.INFO, "Following dir: {0}", dir);
            if (includeDirsInResults && fileMatcher.matches(dir)) {
                putPath(dir);
            }
            return FileVisitResult.CONTINUE;
        } else {
            LOG.log(Level.INFO, "Skipping dir: {0}", dir);
            return FileVisitResult.SKIP_SUBTREE;
        }
    }

    /**
     * Set the active basePath, used for resolving relative paths.
     * <p>
     * When a hit arrives for a subsequent find that has the same relative path
     * as a prior hit, the new hit overrides the prior path as the active hit.
     *
     * @param basePath the basePath to tag all pathList with
     */
    public void setBase(Path basePath) {
        this.basePath = basePath;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (fileMatcher.matches(file)) {
            putPath(file);
        } else {
            LOG.log(Level.INFO, "Ignoring file: {0}", file);
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException ex) throws IOException {
        if (ex instanceof FileSystemLoopException) {
            if (!NOTIFIED_PATHS.contains(file)) {
                LOG.log(Level.WARNING, "skipping detected filesystem loop: {0}", file);
                NOTIFIED_PATHS.add(file);
            }
            return FileVisitResult.SKIP_SUBTREE;
        } else {
            LOG.log(Level.WARNING, ex.getMessage());
            return super.visitFileFailed(file, ex);
        }
    }

    /**
     * Get a List of {@link Path}s from a provided pattern.
     * <p>
     * Resolution Steps:
     * <ol>
     * <li>If the pattern starts with "regex:" or "glob:" then a standard
     * {@link PathMatcher} is built using
     * {@link java.nio.file.FileSystem#getPathMatcher(String)} as a file
     * search.</li>
     * <li>If pattern starts with a known filesystem root (using information
     * from {@link java.nio.file.FileSystem#getRootDirectories()}) then this is
     * assumed to be a absolute file system pattern.</li>
     * <li>All other patterns are treated as relative to BaseHome information:
     * <ol>
     * <li>Search ${jetty.home} first</li>
     * <li>Search ${jetty.base} for overrides</li>
     * </ol>
     * </li>
     * </ol>
     * <p>
     * Pattern examples:
     * <dl>
     * <dt><code>lib/logging/*.jar</code></dt>
     * <dd>Relative pattern, not recursive, search <code>${jetty.home}</code>
     * then <code>${jetty.base}</code> for lib/logging/*.jar content</dd>
     *
     * <dt><code>lib/**&#47;*-dev.jar</code></dt>
     * <dd>Relative pattern, recursive search <code>${jetty.home}</code> then
     * <code>${jetty.base}</code> for files under <code>lib</code> ending in
     * <code>-dev.jar</code></dd>
     *
     * <dt><code>etc/jetty.xml</code></dt>
     * <dd>Relative pattern, no glob, search for
     * <code>${jetty.home}/etc/jetty.xml</code> then
     * <code>${jetty.base}/etc/jetty.xml</code></dd>
     *
     * <dt><code>glob:/opt/app/common/*-corp.jar</code></dt>
     * <dd>PathMapper pattern, glob, search <code>/opt/app/common/</code> for
     * <code>*-corp.jar</code></dd>
     *
     * </dl>
     *
     * <p>
     * Notes:
     * <ul>
     * <li>FileSystem case sensitivity is implementation specific (eg: linux is
     * case-sensitive, windows is case-insensitive).<br>
     * See {@link java.nio.file.FileSystem#getPathMatcher(String)} for more
     * details</li>
     * <li>Pattern slashes are implementation neutral (use '/' always and you'll
     * be fine)</li>
     * <li>Recursive searching is limited to 30 levels deep (not
     * configurable)</li>
     * <li>File System loops are detected and skipped</li>
     * </ul>
     *
     * @param pattern the pattern to search.
     * @return the collection of paths found
     * @throws IOException if error during search operation
     */
    public List<Path> createPaths(String pattern) throws IOException {
        List<Path> pathList = new ArrayList<>();

        if (isAbsolute(pattern)) {
            // Perform absolute path pattern search
            // The root to start search from
            Path root = getRootByPattern(pattern);
            System.out.println("PATH root= " + root);
            // The matcher for file pathList
            PathMatcher matcher = getMatcher(pattern);

            this.includeDirsInResults = true;
            this.fileMatcher = matcher;

            setBase(root);
            Files.walkFileTree(root, SEARCH_VISIT_OPTIONS, 10, this);
            pathList.addAll(getPathMap());
        }

//!!!!         Collections.sort(pathList,new NaturalSort.Paths());
        return pathList;
    }

    public PathMatcher getMatcher(final String rawpattern) {
        FileSystem fs = FileSystems.getDefault();

        String pattern = rawpattern;

        // Strip trailing slash (if present)
        int lastchar = pattern.charAt(pattern.length() - 1);
        if (lastchar == '/' || lastchar == '\\') {
            pattern = pattern.substring(0, pattern.length() - 1);
        }

        // If using FileSystem.getPathMatcher() with "glob:" or "regex:"
        // use FileSystem default pattern behavior
        if (pattern.startsWith("glob:") || pattern.startsWith("regex:")) {
            LOG.log(Level.INFO, "Using Standard {0} pattern: {1}", new Object[]{fs.getClass().getName(), pattern});
            return fs.getPathMatcher(pattern);
        }

        // If the pattern starts with a root path then its assumed to
        // be a full system path
        if (isAbsolute(pattern)) {
            String pat = "glob:" + pattern;
            LOG.log(Level.INFO, "Using absolute path pattern: {0}", pat);
            return fs.getPathMatcher(pat);
        }

        // Doesn't start with filesystem root, then assume the pattern
        // is a relative file path pattern.
        String pat = "glob:**/" + pattern;
        LOG.log(Level.INFO, "Using relative path pattern: {0}", pat);
        return fs.getPathMatcher(pat);
    }

    /**
     * Provide the non-glob / non-regex prefix on the pattern as a Path
     * reference.
     *
     * @param pattern the pattern to test
     * @return the Path representing the search root for the pattern provided.
     */
    public Path getRootByPattern(final String pattern) {
        StringBuilder root = new StringBuilder();

        int start = 0;
        boolean syntaxed = false;
        if (pattern.startsWith("glob:")) {
            start = "glob:".length();
            syntaxed = true;
        } else if (pattern.startsWith("regex:")) {
            start = "regex:".length();
            syntaxed = true;
        }
        int len = pattern.length();
        int lastSep = 0;
        for (int i = start; i < len; i++) {
            int cp = pattern.codePointAt(i);
            if (cp < 127) {
                char c = (char) cp;

                // unix path case
                if (c == '/') {
                    root.append(c);
                    lastSep = root.length();
                } else if (c == '\\') {
                    root.append("\\");
                    lastSep = root.length();

                    // possible escaped sequence.
                    // only really interested in windows escape sequences "\\"
                    int count = countChars(pattern, i + 1, '\\');
                    if (count > 0) {
                        // skip extra slashes
                        i += count;
                    }
                } else {
                    if (isGlob(c, syntaxed)) {
                        break;
                    }
                    root.append(c);
                }
            } else {
                root.appendCodePoint(cp);
            }
        }

        String rootPath = root.substring(0, lastSep);
        if (rootPath.length() <= 0) {
            return EMPTY_PATH;
        }

        return toPath(rootPath);
    }

    private int countChars(String pattern, int offset, char c) {
        int count = 0;
        int len = pattern.length();
        for (int i = offset; i < len; i++) {
            if (pattern.charAt(i) == c) {
                count++;
            } else {
                break;
            }
        }
        return count;
    }

    /**
     * Determine if part is a glob pattern.
     *
     * @param part the string to check
     * @param syntaxed true if overall pattern is syntaxed with
     * <code>"glob:"</code> or <code>"regex:"</code>
     * @return true if part has glob characters
     */
    private boolean isGlob(char c, boolean syntaxed) {
        for (char g : GLOB_CHARS) {
            if (c == g) {
                return true;
            }
        }
        if (syntaxed) {
            for (char g : SYNTAXED_GLOB_CHARS) {
                if (c == g) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Convert a pattern to a Path object.
     *
     * @param pattern the raw pattern (can contain "glob:" or "regex:" syntax
     * indicator)
     * @return the Path version of the pattern provided.
     */
    private Path toPath(final String pattern) {
        String test = pattern;
        if (test.startsWith("glob:")) {
            test = test.substring("glob:".length());
        } else if (test.startsWith("regex:")) {
            test = test.substring("regex:".length());
        }
        return new File(test).toPath();
    }

    /**
     * Tests if provided pattern is an absolute reference (or not)
     *
     * @param pattern the pattern to test
     * @return true if pattern is an absolute reference.
     */
    public boolean isAbsolute(final String pattern) {
        Path searchRoot = getRootByPattern(pattern);
        if (searchRoot == EMPTY_PATH) {
            return false;
        }
        return searchRoot.isAbsolute();
    }

    private static class NonHiddenMatcher implements PathMatcher {

        @Override
        public boolean matches(Path path) {
            try {
                return !Files.isHidden(path);
            } catch (IOException ex) {
                LOG.log(Level.WARNING, ex.getMessage());
                return false;
            }
        }
    }//class

}
