
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
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static javax.swing.Action.NAME;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
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
 * The class provides implementations of the  context aware action 
 * to be performed to start the server in debug mode from the server 
 * project's pop up menu.
 * 
 * @author V. Shyshkin
 */
@ActionID(
        category = "Project",
        id = "org.netbeans.modules.jeeserver.jetty.project.nodes.actions.AddBeanXmlAction")
@ActionRegistration(
        displayName = "#CTL_AddBeanXmlAction",
        lazy=false)
@ActionReference(path = "Projects/Actions",  position = 20)
@NbBundle.Messages("CTL_AddBeanXmlAction=Add bean.xml file to WEB-INF")
public final class AddBeanXmlAction extends AbstractAction implements ContextAwareAction {

    private static final Logger LOG = Logger.getLogger(AddListenerAction.class.getName());

    private static final RequestProcessor RP = new RequestProcessor(AddBeanXmlAction.class);
    
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
        

        public ContextAction(final Lookup webapplookup) {
            
            
            webProject = webapplookup.lookup(Project.class);
            J2eeModuleProvider p = BaseUtil.getJ2eeModuleProvider(webProject);
            String id = "";
            if ( p != null) {
                id = p.getServerInstanceID();
            }
            if ( p != null && id.startsWith("jettystandalone:deploy:server") && p.getInstanceProperties() != null ) {
                File file = new File( BaseUtil.getServerLocation(p.getInstanceProperties()));
                FileObject fo = FileUtil.toFileObject(file);
                serverProject = FileOwnerQuery.getOwner(fo);            
            } else {
                serverProject = null;
            }
            
            enabled = serverProject == null ? false : true;
            
            if ( enabled ) {
                setEnabled(DDHelper.needsBeansXml(serverProject, webProject));
            }
            
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
            
            putValue(NAME, "Add beans.xml to WEB-INF folder");

        }

        public @Override
        void actionPerformed(ActionEvent e) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    DDHelper.addBeansXml(serverProject, webProject);
                }
            });
        }//class
    }//class
}//class