package org.netbeans.modules.jeeserver.base.embedded.project;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;

import org.netbeans.modules.jeeserver.base.embedded.utils.EmbConstants;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Embedded server customizer implementation.
 *
 * @author V. Shyshkin
 */
public class EmbServerCustomizer
        implements ProjectCustomizer.CompositeCategoryProvider {
    private static final Logger LOG = Logger.getLogger(EmbServerCustomizer.class.getName());

    private final String name;
    protected EmbServerCustomizerPanelVisual jPanel;
    protected Project project;

    private EmbServerCustomizer(String name) {
        this.name = name;
    }

    /**
     * Creates a new instance of the {@code Category} for the specified lookup.
     * @param lookup
     * @return new instance of type {
     * @Category} if the specified lookup contains a project which represents an
     * embedded server. {@code null} if the project doesn't represent an
     * embedded server.
     */
    @Override
    public Category createCategory(Lookup lookup) {
        project = lookup.lookup(Project.class);
        if (BaseUtils.isServerProject(project)) {
            ProjectCustomizer.Category category = ProjectCustomizer.Category.create(name, name, null);
            category.setStoreListener(new StoreHandler(this, category));
            return category;
        } else {
            return null;
        }
    }

    /**
     * Creates a swing panel to customize embedded server.
     *
     * @param category
     * @param lkp
     * @return a component of type {{@link EmbServerCustomizerPanelVisual}
     */
    @Override
    public JComponent createComponent(Category category, Lookup lkp) {
        jPanel = new EmbServerCustomizerPanelVisual(lkp.lookup(Project.class), category);
        return jPanel;
    }

    @NbBundle.Messages({"LBL_Config=Embedded Server"})
    @ProjectCustomizer.CompositeCategoryProvider.Registration(
            projectType = "org-netbeans-modules-java-j2seproject",
            position = 10)
    public static EmbServerCustomizer createEmbeddedServerConfigurationTab() {
        return new EmbServerCustomizer(Bundle.LBL_Config());
    }

    /**
     * The implementation of the {@code ActionListener} interface. 
     * Used when {@code Ok} button is pressed.
     */
    protected static class StoreHandler implements ActionListener {

        private final EmbServerCustomizer customizer;
        private final Project project;
        private EmbServerCustomizerPanelVisual panel;
        private final Category category;

        public StoreHandler(EmbServerCustomizer customizer, Category category) {
            this.customizer = customizer;
            project = customizer.project;
            this.category = category;

        }

        @Override
        public void actionPerformed(ActionEvent e) {
            panel = customizer.jPanel;
            BaseDeploymentManager manager = BaseUtils.managerOf(project);
            panel.store();

            Properties p = panel.getSettings();
            setServerProperties(panel.getSettings());
            InstanceProperties ip = ((BaseDeploymentManager) manager).getInstanceProperties();

            ip.setProperty(EmbConstants.HTTP_PORT_PROP, p.getProperty(EmbConstants.HTTP_PORT_PROP));
            ip.setProperty(EmbConstants.DEBUG_PORT_PROP, p.getProperty(EmbConstants.DEBUG_PORT_PROP));
            ip.setProperty(EmbConstants.SHUTDOWN_PORT_PROP, p.getProperty(EmbConstants.SHUTDOWN_PORT_PROP));
            ip.setProperty(EmbConstants.INCREMENTAL_DEPLOYMENT, p.getProperty(EmbConstants.INCREMENTAL_DEPLOYMENT));

            ip.refreshServerInstance();
        }

        private void setServerProperties(final Properties settings) {
            final FileObject projectDir = project.getProjectDirectory();
            try {
                FileUtil.runAtomicAction(new FileSystem.AtomicAction() {
                    @Override
                    public void run() throws IOException {
                        FileObject fo = projectDir.getFileObject(EmbConstants.SERVER_INSTANCE_PROPERTIES_PATH);
                        FileLock lock = fo.lock();
                        FileOutputStream fos = new FileOutputStream(fo.getPath());

                        try {
                            settings.store(fos, "");
                            fos.close();

                        } catch (IOException ex) {
                            LOG.log(Level.INFO, ex.getMessage());
                        } finally {
                            lock.releaseLock();
                        }
                    }
                });
            } catch (IOException ex) {
                LOG.log(Level.INFO, ex.getMessage());
            }

        }
    }
}
