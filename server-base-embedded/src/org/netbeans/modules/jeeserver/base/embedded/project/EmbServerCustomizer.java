package org.netbeans.modules.jeeserver.base.embedded.project;

import java.util.logging.Logger;
import javax.swing.JComponent;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Embedded server customizer implementation.
 *
 * @author V. Shyshkin
 */
public class EmbServerCustomizer
{//        implements ProjectCustomizer.CompositeCategoryProvider {
    private static final Logger LOG = Logger.getLogger(EmbServerCustomizer.class.getName());

    private final String name;
    protected EmbServerCustomizerPanelVisual jPanel;
    //protected Project project;

    private EmbServerCustomizer(String name) {
        this.name = name;
    }

    /**
     * Creates a swing panel to customize embedded server.
     *
     * @param category
     * @param lkp
     * @return a component of type {{@link EmbServerCustomizerPanelVisual}
     */
    public JComponent createComponent(Category category, Lookup lkp) {
        jPanel = new EmbServerCustomizerPanelVisual(lkp, category);
        return jPanel;
    }

    @NbBundle.Messages({"LBL_Config=Embedded Server"})
//    @ProjectCustomizer.CompositeCategoryProvider.Registration(
//            projectType = "org-netbeans-modules-java-j2seproject",
//            position = 10)
    public static EmbServerCustomizer createEmbeddedServerConfigurationTab() {
        return new EmbServerCustomizer(Bundle.LBL_Config());
    }

    /**
     * The implementation of the {@code ActionListener} interface. 
     * Used when {@code Ok} button is pressed.
     */
/*    protected static class StoreHandler implements ActionListener {

        private final EmbServerCustomizer customizer;
        //private final Project project;
        private EmbServerCustomizerPanelVisual panel;
        private final Category category;

        public StoreHandler(EmbServerCustomizer customizer, Category category) {
            this.customizer = customizer;
            //project = customizer.project;
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

            ip.setProperty(SuiteConstants.HTTP_PORT_PROP, p.getProperty(SuiteConstants.HTTP_PORT_PROP));
            ip.setProperty(SuiteConstants.DEBUG_PORT_PROP, p.getProperty(SuiteConstants.DEBUG_PORT_PROP));
            ip.setProperty(SuiteConstants.SHUTDOWN_PORT_PROP, p.getProperty(SuiteConstants.SHUTDOWN_PORT_PROP));
            ip.setProperty(SuiteConstants.INCREMENTAL_DEPLOYMENT, p.getProperty(SuiteConstants.INCREMENTAL_DEPLOYMENT));

            ip.refreshServerInstance();
        }

        private void setServerProperties(final Properties settings) {
            final FileObject projectDir = project.getProjectDirectory();
            try {
                FileUtil.runAtomicAction(new FileSystem.AtomicAction() {
                    @Override
                    public void run() throws IOException {
                        FileObject fo = projectDir.getFileObject(SuiteConstants.INSTANCE_PROPERTIES_PATH);
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
*/    
}
