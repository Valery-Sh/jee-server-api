
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
package org.netbeans.modules.jeeserver.base.embedded.webapp.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static javax.swing.Action.NAME;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteConstants;
import org.netbeans.modules.jeeserver.base.embedded.webapp.DistributedWebAppManager;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * The class provides implementations of the  context aware action 
 * to be performed to start the server in debug mode from the server 
 * project's pop up menu.
 * 
 * @author V. Shyshkin
 */
@ActionID(
        category = "Project",
        id = "org.netbeans.modules.jeeserver.base.embedded.webapp.actions.RemoveDistWebAppAction" )
@ActionRegistration(
        iconInMenu = true,
        displayName = "#CTL_RemoveDistWebAppAction",
        lazy=false)
@ActionReference(path = "Projects/Actions",  position = 20)
@NbBundle.Messages("CTL_RemoveDistWebAppAction=Remove Web Application from Distributed List")
public final class RemoveDistWebAppAction extends AbstractAction implements ContextAwareAction {

    private static final Logger LOG = Logger.getLogger(RemoveDistWebAppAction.class.getName());

    private static final RequestProcessor RP = new RequestProcessor(RemoveDistWebAppAction.class);
    
    @StaticResource
    private final static String ICON = "org/netbeans/modules/jeeserver/base/embedded/resources/remove.png"; 
    
    @Override
    public void actionPerformed(ActionEvent e) {
        assert false;
    }
    /**
     * Creates an action for the given context.
     * @param context a lookup that contains the server project instance of type
     *  {@literal Project}.
     * @return a new instance of type {@link #ContextAction}
     */
    @Override
    public Action createContextAwareInstance(Lookup context) {
        return new ContextAction(context);
    }

    private static final class ContextAction extends AbstractAction {

        private final Project serverProject;
        private final Project webProject;
        DistributedWebAppManager distManager = null;        

        public ContextAction(final Lookup webapplookup) {
            
            
            webProject = webapplookup.lookup(Project.class);

            J2eeModuleProvider p = BaseUtil.getJ2eeModuleProvider(webProject);
            String id = "";
            if ( p != null) {
                id = p.getServerInstanceID();
            }
            distManager = null;
            if ( p != null && p.getInstanceProperties() != null && p.getInstanceProperties().getProperty(SuiteConstants.SUITE_PROJECT_LOCATION) != null) {
                File file = new File( BaseUtil.getServerLocation(p.getInstanceProperties()));
                FileObject fo = FileUtil.toFileObject(file);
                serverProject = FileOwnerQuery.getOwner(fo);            
                distManager = DistributedWebAppManager.getInstance(serverProject);
                
            } else {
                serverProject = null;
            }
            
            enabled = (serverProject == null || ! distManager.isRegistered(webProject)) ? false : true;

            
            setEnabled(enabled);
            
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
            
            String name = webProject.getProjectDirectory().getNameExt();
            
            putValue(SMALL_ICON, ImageUtilities.loadImage(ICON, false));
            //putValue("iconBase", ICON);            
                    
            putValue(NAME, "Remove " + name + " from Distributed List");

        }

        public @Override
        void actionPerformed(ActionEvent e) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    distManager.unregister(webProject);
                }
            });
        }//class
    }//class
}//class