/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jeeserver.jetty.project.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static javax.swing.Action.NAME;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import static org.netbeans.modules.jeeserver.base.deployment.progress.BaseRunProgressObject.LOG;
import org.netbeans.modules.jeeserver.base.deployment.specifics.StartServerPropertiesProvider;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.netbeans.modules.jeeserver.jetty.util.Utils;
import org.openide.awt.DynamicMenuContent;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 * The class provides implementations of the context aware action to be
 * performed to download libraries as specified by Jetty modules. The menu
 * action is accessible from the server project's pop up menu.
 *
 * @author V. Shyshkin
 */
public final class JettyStartCommandActions extends AbstractAction implements ContextAwareAction {
    
    private static final RequestProcessor RP = new RequestProcessor(JettyStartCommandActions.class);

    public JettyStartCommandActions() {
    }

    /**
     * Never called.
     *
     * @param e
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        assert false;
    }

    /**
     * Creates an action for the given context.
     *
     * @param context a lookup that contains the server project instance of type
     * {@literal Project}.
     * @return a new instance of type {@link #ContextAction}
     */
    @Override
    public Action createContextAwareInstance(Lookup context) {
        return new ContextAction(context);
    }

    public Action createContextAwareInstance(Lookup context, String jettyStartCommand) {
        return new ContextAction(context);
    }

    protected static class ContextAction extends AbstractAction {

        private final Project project;
        private BaseDeploymentManager manager;

        public ContextAction(Lookup context) {

            project = context.lookup(Project.class);

            boolean isServer = Utils.isJettyServer(project);
            if (isServer) {
                loadManager();
            }
            setEnabled(isServer && manager != null);
            // we need to hide when disabled putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);            
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, !isServer);

            //putValue(NAME, "&List Classpath (--list-classpath jetty opt)");
            putValue(NAME, "&" + menuItemText());
        }

        private void loadManager() {
            manager = BaseUtil.managerOf(project.getLookup() );
        }

        public @Override
        void actionPerformed(ActionEvent e) {
            perform();
        }

        public void perform() {
//            FileUtil.runAtomicAction((Runnable) () -> {
//                IniModules.CDISupport.showLicenseDialog(project);
//            });
            
//            RequestProcessor rp = new RequestProcessor("Server processor", 1);
            RP.post(new RunnableImpl(), 0, Thread.NORM_PRIORITY);
        }

        protected String getStartJar() {
            InstanceProperties ip = manager.getInstanceProperties();
            return ip.getProperty(BaseConstants.HOME_DIR_PROP) + "/start.jar";
        }

        private class RunnableImpl implements Runnable {

            @Override
            public void run() {
                String serverDir = BaseUtil.getServerLocation(manager.getInstanceProperties());
                File f = new File(serverDir);
                Project project = manager.getServerProject();

                ExecutorTask task;

                StartServerPropertiesProvider pp = project.getLookup().lookup(StartServerPropertiesProvider.class);

                FileObject buildXml = FileUtil.toFileObject(f).getFileObject("build.xml");

                Properties props;

                if (pp != null) {
                    buildXml = pp.getBuildXml(project);
                }

                props = new Properties();
                props.setProperty("start.jar", getStartJar());

                //String[] targets = new String[]{"list-classpath"};
                String[] targets = antTargets();

                try {
                    task = ActionUtils.runTarget(buildXml, targets, props);
                    task.waitFinished();
                } catch (IOException | IllegalArgumentException ex) {
                    LOG.log(Level.INFO, ex.getMessage());
                }
            }

        }//class

        public String menuItemText() {
            return "List Modules (--list-modules jetty opt)";
        }

        public String[] antTargets() {
            return new String[]{"list-modules"};
        }

    }//class

    public static class ListModulesAction extends ContextAction {

        public ListModulesAction(Lookup context) {
            super(context);
        }

        @Override
        public String menuItemText() {
            return "List Modules (--list-modules jetty opt)";
        }

        @Override
        public String[] antTargets() {
            return new String[]{"list-modules"};
        }
    }

    public static class ListClasspathAction extends ContextAction {

        public ListClasspathAction(Lookup context) {
            super(context);
        }
        @Override
        public String menuItemText() {
            return "List Classpath (--list-classpath jetty opt)";
        }

        @Override
        public String[] antTargets() {
            return new String[]{"list-classpath"};
        }
    }

    public static class ListConfigAction extends ContextAction {

        public ListConfigAction(Lookup context) {
            super(context);
        }
        @Override
        public String menuItemText() {
            return "List Config (--list-config jetty opt)";
        }

        @Override
        public String[] antTargets() {
            return new String[]{"list-config"};
        }
    }

}//class
