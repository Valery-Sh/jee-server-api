/**
 * This file is part of Base JEE Server support in NetBeans IDE.
 *
 * Base JEE Server support in NetBeans IDE is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * Base JEE Server support in NetBeans IDE is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should see the GNU General Public License here:
 * <http://www.gnu.org/licenses/>.
 */
package org.netbeans.modules.jeeserver.jetty.project.nodes.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Paths;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static javax.swing.Action.NAME;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.netbeans.modules.jeeserver.jetty.util.IniModules.JsfSupport;
import org.netbeans.modules.jeeserver.jetty.util.JettyConstants;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * The class provides implementations of the context aware action to be
 * performed to start the server in debug mode from the server project's pop up
 * menu.
 *
 * @author V. Shyshkin
 */
@ActionID(
        category = "Project",
        id = "org.netbeans.modules.jeeserver.jetty.project.nodes.actions.AddListenerAction")
@ActionRegistration(
        displayName = "#CTL_AddListenerAction",
        lazy = false)
@ActionReference(path = "Projects/Actions", position = 30, separatorAfter = 31)
@NbBundle.Messages("CTL_AddListenerAction=Add listener  to web.xml")
public final class AddListenerAction extends AbstractAction implements ContextAwareAction {

    private static final Logger LOG = Logger.getLogger(AddListenerAction.class.getName());

    private static final RequestProcessor RP = new RequestProcessor(AddListenerAction.class);

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

    private static final class ContextAction extends AbstractAction {

        private final Project serverProject;
        private final Project webProject;

        public ContextAction(final Lookup webapplookup) {

            webProject = webapplookup.lookup(Project.class);
            J2eeModuleProvider p = BaseUtils.getJ2eeModuleProvider(webProject);
            String id = "";
            if (p != null) {
                id = p.getServerInstanceID();
            }
            if (p != null && id.startsWith("jettystandalone:deploy:server")) {
                File file = new File(p.getInstanceProperties().getProperty(BaseConstants.SERVER_LOCATION_PROP));
                FileObject fo = FileUtil.toFileObject(file);
                serverProject = FileOwnerQuery.getOwner(fo);
            } else {
                serverProject = null;
            }

            boolean enabled = serverProject == null ? false : true;

            if (enabled) {

                //File f = Paths.get(serverProject.getProjectDirectory().getPath(), JettyConstants.JETTY_START_INI).toFile();

                //StartIni startIni = new StartIni(f);

                //String jsfModule = startIni.getEnabledJsfModuleName();
                
                File f = Paths.get(serverProject.getProjectDirectory().getPath(), JettyConstants.JETTYBASE_FOLDER).toFile();                
                JsfSupport jsfSupport = new JsfSupport(f);

                String jsfModule = jsfSupport.getEnabledJsfModuleName();
                
                enabled = jsfModule != null && !DDHelper.hasJsfListener(serverProject, webProject);
                setEnabled(enabled);

                String s = isEnabled() ? "(" + jsfModule + ") " : "";
                putValue(NAME, "Add Listener " + s + " to web.xml ");
            }

            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);

        }

        public @Override
        void actionPerformed(ActionEvent e) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    DDHelper.addJsfListener(serverProject, webProject);
                }
            });
        }//class
    }//class
}//class
