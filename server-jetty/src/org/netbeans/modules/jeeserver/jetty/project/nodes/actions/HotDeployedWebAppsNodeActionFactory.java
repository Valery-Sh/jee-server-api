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
package org.netbeans.modules.jeeserver.jetty.project.nodes.actions;

import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static javax.swing.Action.NAME;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.netbeans.modules.jeeserver.base.deployment.utils.Copier;
import org.netbeans.modules.jeeserver.jetty.util.Utils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 * @author V. Shyshkin
 */
public class HotDeployedWebAppsNodeActionFactory {

    private static final RequestProcessor RP = new RequestProcessor(HotDeployedWebAppsNodeActionFactory.class);

    public static final String HTML5_WAR_TEMP = "html5_convert_to_war_";

    protected static final String WAR = "war";
    protected static final String XML = "xml";
    protected static final String WEB = "web";
    protected static final String HTML5 = "html5";
    protected static final String HTML5_WAR = "html5_war";

    public Action getAddWarAction(Lookup context) {
        return new WebAppsAction(context, WAR);
    }

    public Action getAddHtml5WarAction(Lookup context) {
        return new WebAppsAction(context, HTML5_WAR);
    }

    public Action getAddXmlAction(Lookup context) {
        return new WebAppsAction(context, XML);
    }

    public Action getAddWebFolderAction(Lookup context) {
        return new WebAppsAction(context, WEB);
    }

    public Action getAddHtml5Action(Lookup context) {
        return new WebAppsAction(context, HTML5);
    }

    public static class WebAppsAction extends AbstractAction {

        private static final Logger LOG = Logger.getLogger(HotDeployedWebAppsNodeActionFactory.class.getName());

        private RequestProcessor.Task task;
        private final Project server;
        private final String actionType;

        protected final String getMenuItemDisplayName() {
            String result = "Add Web Application or war archive";
            switch (actionType) {
                case XML:
                    result = "Add Jetty config xml file";
                    break;
                case WEB:
                    result = "Add unpacked war of a Web App";
                    break;
                case HTML5:
                    result = "Add HTML5 Application (as .xml)";
                    break;
                case HTML5_WAR:
                    result = "Add HTML5 Application as WAR Archive";
                    break;

            }
            return result;

        }

        protected String getDialogTitle() {
            String result = "Choose war-archive file";
            switch (actionType) {
                case XML:
                    result = "Choose xml file";
                    break;
                case WEB:
                    result = "Choose Web Project";
                    break;
                case HTML5_WAR:
                case HTML5:
                    result = "Choose HTML5 project";
                    break;
            }
            return result;

        }

        protected String[] getFileFilter() {
            String[] result = new String[]{};
            switch (actionType) {
                case XML:
                    result = new String[]{"Xml files(*.xml)", "xml"};
                    break;
            }
            return result;
        }

        /**
         * If the specified action is a "war" action, it is assumed that we deal
         * with Web Application project type.
         *
         * @param context
         * @param actionType
         */
        public WebAppsAction(final Lookup context, final String actionType) {
            this.actionType = actionType;

            boolean isJettyServer;// = false;
            setEnabled(false);
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);

            FileObject fo = context.lookup(FileObject.class);

            if (fo != null && FileOwnerQuery.getOwner(fo) != null) {
                server = FileOwnerQuery.getOwner(fo);
            } else {
                server = null;
            }

            if (server != null) {
                isJettyServer = Utils.isJettyServer(server);
            } else {
                isJettyServer = false;
            }

            setEnabled(isJettyServer);
            // we need to hide when disabled putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);            
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, !isJettyServer);
            // TODO menu item label with optional mnemonics
            putValue(NAME, "&" + getMenuItemDisplayName());
            init(context);
//            task = new RequestProcessor("AddProjectBody").create(new Runnable() { // NOI18N
        }

        private void init(final Lookup context) {
            task = RP.create(() -> {
                File baseDir = FileUtil.toFile(server.getProjectDirectory().getParent());
                String[] fileFilter = getFileFilter();
                FileChooserBuilder fcb = new FileChooserBuilder("")
                        .setTitle(getDialogTitle())
                        .setFilesOnly(false)
                        .setDefaultWorkingDirectory(baseDir);
                if (XML.equals(actionType)) {
                    fcb.addFileFilter(new FileNameExtensionFilter(fileFilter[0], fileFilter[1]));
                    fcb.setFilesOnly(true);
                }
                JFileChooser fc = fcb.createFileChooser();
                int choosed = fc.showOpenDialog(null);
                if (choosed != JFileChooser.APPROVE_OPTION) {
                    return;
                }
                File selectedFile = fc.getSelectedFile();
                FileObject selectedFo = FileUtil.toFileObject(selectedFile);
                FileObject targetFolder = context.lookup(FileObject.class);

                if (XML.equals(actionType)) {
                    tryCopy(targetFolder, selectedFo, selectedFo.getName(), "xml");
                    return;
                }//if

                Project webapp = FileOwnerQuery.getOwner(selectedFo);
                if (HTML5.equals(actionType)) {
                    if (webapp == null) {
                        return;
                    }

                    createJettyXmlForHtml5(webapp.getProjectDirectory(), targetFolder);
                    return;
                }
                if (HTML5_WAR.equals(actionType)) {
                    if (webapp == null) {
                        return;
                    }

                    createWarForHtml5(webapp.getProjectDirectory(), targetFolder);
                    return;
                }

                /**
                 * Try if the selectedFo is not a folder and has "war" extension
                 * and is not in a project
                 */
                if (webapp == null && WAR.equals(actionType)
                        && !selectedFo.isFolder() && "war".equals(selectedFo.getExt())) {
                    tryCopy(targetFolder, selectedFo, selectedFo.getName(), "war");
                    return;
                }

                if (webapp == null) {
                    return;
                }

                String webappName = webapp.getProjectDirectory().getNameExt();
                /**
                 * .war file inside a project
                 */
                FileObject warFo = BaseUtils.getWar(webapp);
                if (warFo == null) {
                    return;
                }
                if (null != actionType) {
                    switch (actionType) {
                        case WAR:
                            tryCopy(targetFolder, warFo, webappName, "war");
                            break;
                        case WEB:
                            tryCopyUnpackedWar(targetFolder, warFo, webappName);
                            break;
                    }
                }
            } // NOI18N
            );

        }
        @StaticResource
        public static final String JETTY_WEB = "org/netbeans/modules/jeeserver/jetty/resources/JettyWeb.template";

        @StaticResource
        public static final String JETTY_WEB_CONTEXT = "org/netbeans/modules/jeeserver/jetty/resources/JettyWebContext.template";

        public void createWarForHtml5(FileObject html5Dir, FileObject targetFolder) {

            String fileName = html5Dir.getNameExt();

            if (!undeployIfExists(targetFolder, fileName, "WAR")) {
                return;
            }

            Properties html5props = BaseUtils.loadHtml5ProjectProperties(html5Dir.getPath());
            String contextPath = BaseUtils.resolve(BaseConstants.HTML5_WEB_CONTEXT_ROOT_PROP, html5props);
            String siteRootPath = BaseUtils.resolve(BaseConstants.HTML5_SITE_ROOT_PROP, html5props);

            final File siteRoot = new File(html5Dir.getPath() + "/" + siteRootPath);
            InputStream input = HotDeployedWebAppsNodeActionFactory.class.getResourceAsStream("/" + JETTY_WEB_CONTEXT);

            if (null == contextPath) {
                contextPath = "/" + fileName;
            }

            final String jettyxml = Utils.stringOf(input)
                    .replace("${jetty-web-xml-contextpath}", contextPath);

            //File warFile = new File(targetFolder.getPath() + "/" + html5Dir.getNameExt() + ".war");
            String warFileName = html5Dir.getNameExt() + ".war";
            Path tmpPath = Paths.get(System.getProperty("java.io.tmpdir") + "/"
                    + HTML5_WAR_TEMP + html5Dir.getNameExt() + "_" + System.currentTimeMillis());

            File tmpWar = Paths.get(tmpPath.toString(), warFileName).toFile();

            final Path tmpJettyxmlPath = Paths.get(tmpPath.toString(), "jetty-web.xml");

            try (InputStream is = new ByteArrayInputStream(jettyxml.getBytes());) {
                Copier.mkdirs(tmpPath);
                Files.copy(is, tmpJettyxmlPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                BaseUtils.out("createWarForHtml5: create jetty-web.xmlEXCEPTION " + ex.getMessage());
                LOG.log(Level.INFO, ex.getMessage());
            }

            try {
                Copier.ZipUtil.copy(siteRoot, tmpWar);
                Copier.ZipUtil.copy(tmpJettyxmlPath.toFile(), tmpWar, "WEB-INF");

                FileUtil.copyFile(FileUtil.toFileObject(tmpWar), targetFolder, fileName, "war");
                targetFolder.refresh();
            } catch (Exception ex) {
                BaseUtils.out("createWarForHtml5: EXCEPTION " + ex.getMessage());
                LOG.log(Level.INFO, ex.getMessage());
            }

            FileUtil.runAtomicAction(new Runnable() {

                @Override
                public void run() {
                    Copier.delete(tmpPath.toFile());
                }
            });

        }

        public void createJettyXmlForHtml5(FileObject html5Dir, FileObject targetFolder) {

            String fileName = html5Dir.getNameExt();

            if (!undeployIfExists(targetFolder, fileName, "war")) {
                return;
            }

            Properties html5props = BaseUtils.loadHtml5ProjectProperties(html5Dir.getPath());
            String cp = BaseUtils.resolve(BaseConstants.HTML5_WEB_CONTEXT_ROOT_PROP, html5props);
            String war = BaseUtils.resolve(BaseConstants.HTML5_SITE_ROOT_PROP, html5props);

            InputStream input = HotDeployedWebAppsNodeActionFactory.class.getResourceAsStream("/" + JETTY_WEB);

            if (null == cp) {
                cp = "/" + fileName;
            }

            final String jettyxml = Utils.stringOf(input)
                    .replace("${jetty-web-xml-contextpath}", cp)
                    .replace("${jetty-web-xml-war}", war);

            final Path toPath = Paths.get(targetFolder.getPath(), fileName + ".xml");
            FileUtil.runAtomicAction((Runnable) () -> {
                try (InputStream is = new ByteArrayInputStream(jettyxml.getBytes());) {
                    Files.copy(is, toPath, StandardCopyOption.REPLACE_EXISTING);
                    targetFolder.refresh();
                } catch (IOException ex) {
                    BaseUtils.out("createJettyXmlForHtml5: EXCEPTION " + ex.getMessage());
                    LOG.log(Level.INFO, ex.getMessage());
                }
            });

        }

        protected void tryCopy(FileObject target, FileObject source, String fileName, String ext) {
            if (!undeployIfExists(target, fileName, ext)) {
                return;
            }
            try {
                FileUtil.runAtomicAction((Runnable) () -> {
                    try {
                        if (source.isFolder()) {
                            Copier c = new Copier(FileUtil.toFile(source));
                            c.copyTo(FileUtil.toFile(target), fileName + "." + ext);
                            //source.copy(target, fileName, ext);
                        } else {
                            Files.copy(Paths.get(source.getPath()), Paths.get(target.getPath(), fileName + ".war"), StandardCopyOption.REPLACE_EXISTING);
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                        BaseUtils.out("COPY : EXCEPTION " + ex.getMessage());
                    }
                });
                target.refresh();
            } catch (Exception ex) {
                BaseUtils.out("NEW FILE : EXCEPTION " + ex.getMessage());
                LOG.log(Level.INFO, ex.getMessage());
            }
        }

        protected void tryCopyUnpackedWar(FileObject target, FileObject source, String fileName) {
            if (!undeployIfExists(target, fileName, "")) {
                return;
            }
            try {
                FileUtil.runAtomicAction(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            File zipFile = FileUtil.toFile(source);
                            File targetFolder = Paths.get(target.getPath(), fileName).toFile();
                            Copier.ZipUtil.unzip(zipFile, "/", targetFolder);
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                            BaseUtils.out("tryCopyUnpackedWar : EXCEPTION " + ex.getMessage());
                        }
                    }
                });

                target.refresh();

            } catch (Exception ex) {
                BaseUtils.out("NEW FILE : EXCEPTION " + ex.getMessage());
                LOG.log(Level.INFO, ex.getMessage());
            }
        }

        protected boolean undeployIfExists(FileObject targetFolder, String fileName, String ext) {

            if (targetFolder.getFileObject(fileName, ext) == null) {
                return true;
            }

            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                    "File name "
                    + fileName + " already exists. \n"
                    + "Click 'Yes' if you want to replace it.",
                    NotifyDescriptor.YES_NO_OPTION
            );
            Object retValue = DialogDisplayer.getDefault().notify(nd);

            if (retValue == DialogDescriptor.NO_OPTION) {
                return false;
            }

            try {
                targetFolder.getFileObject(fileName, ext).delete();
            } catch (IOException ex) {
                LOG.log(Level.INFO, ex.getMessage());
                return false;
            }
            //          }
            return true;

        }

        public @Override
        void actionPerformed(ActionEvent e) {
            task.schedule(0);
            if ("waitFinished".equals(e.getActionCommand())) {
                task.waitFinished();
            }
        }
    }
}
