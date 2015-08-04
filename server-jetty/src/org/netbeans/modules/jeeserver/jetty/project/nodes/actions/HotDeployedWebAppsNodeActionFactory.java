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

    protected static final String WAR = "war";
    protected static final String XML = "xml";
    protected static final String WEB = "web";
    protected static final String HTML5 = "html5";

    public Action getAddWarAction(Lookup context) {
        return new WebAppsAction(context, WAR);
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

        private final RequestProcessor.Task task;
        private final Project project;
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
                project = FileOwnerQuery.getOwner(fo);
            } else {
                project = null;
            }

            if (project != null) {
                isJettyServer = Utils.isJettyServer(project);
            } else {
                isJettyServer = false;
            }

            setEnabled(isJettyServer);
            // we need to hide when disabled putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);            
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, !isJettyServer);
            // TODO menu item label with optional mnemonics
            putValue(NAME, "&" + getMenuItemDisplayName());

            task = new RequestProcessor("AddProjectBody").create(new Runnable() { // NOI18N

                @Override
                public void run() {
                    File baseDir = FileUtil.toFile(project.getProjectDirectory().getParent());
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

                    /**
                     * Try if the selectedFo is not a folder and has "war"
                     * extension and is not in a project
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
                    if (WAR.equals(actionType)) {
                        tryCopy(targetFolder, warFo, webappName, "war");
                    } else if (WEB.equals(actionType)) {
                        tryCopyUnpackedWar(targetFolder, warFo, webappName);
                    }
                }

            });

        }

        public void createJettyXmlForHtml5(FileObject projectDir, FileObject targetFolder) {

            String fileName = projectDir.getNameExt();

            if (!undeployIfExists(targetFolder, fileName, "xml")) {
                return;
            }

            Properties html5props = BaseUtils.loadHtml5ProjectProperties(projectDir.getPath());
            String cp = BaseUtils.resolve(BaseConstants.HTML5_WEB_CONTEXT_ROOT_PROP, html5props);
            String war = BaseUtils.resolve(BaseConstants.HTML5_SITE_ROOT_PROP, html5props);

            war = projectDir.getPath() + "/" + war;

            final String jettyxml = Utils.stringOf(HotDeployedWebAppsNodeActionFactory.class.getResourceAsStream("/org/netbeans/modules/jeeserver/jetty/resources/JettyWeb.template"))
                    .replace("${jetty-web-xml-contextpath}", cp)
                    .replace("${jetty-web-xml-war}", war);

            final Path toPath = Paths.get(targetFolder.getPath(), fileName + ".xml");
            FileUtil.runAtomicAction(new Runnable() {

                @Override
                public void run() {
                    try (InputStream is = new ByteArrayInputStream(jettyxml.getBytes());) {
                        Files.copy(is, toPath, StandardCopyOption.REPLACE_EXISTING);
                        targetFolder.refresh();
                    } catch (IOException ex) {
                        BaseUtils.out("createJettyXmlForHtml5: EXCEPTION " + ex.getMessage());
                        LOG.log(Level.INFO, ex.getMessage());
                    }

                }
            });

        }

        protected void tryCopy(FileObject target, FileObject source, String fileName, String ext) {
            if (!undeployIfExists(target, fileName, ext)) {
                return;
            }
            try {
                FileUtil.runAtomicAction(new Runnable() {
                    @Override
                    public void run() {
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
