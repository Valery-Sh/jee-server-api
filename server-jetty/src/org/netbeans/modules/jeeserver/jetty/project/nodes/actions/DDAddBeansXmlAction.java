/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jeeserver.jetty.project.nodes.actions;

import java.awt.event.ActionEvent;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static javax.swing.Action.NAME;
import org.netbeans.api.project.Project;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Valery
 */
public class DDAddBeansXmlAction extends AbstractAction implements ContextAwareAction {
    
    private static final Logger LOG = Logger.getLogger(DDAddListenerAction.class.getName());

    private static final RequestProcessor RP = new RequestProcessor(DDAddListenerAction.class);

    @Override
    public void actionPerformed(ActionEvent e) {
        assert false;
    }

    @Override
    public Action createContextAwareInstance(Lookup context) {
        assert false;
        return null;
    }

    /**
     *
     * @param serverProj
     * @param webProject
     * @return
     */
    public static Action getDDAddBeansXmlAction(Project serverProj,Project webProject) {
        return new DDAddBeansXmlAction.ContextAction(serverProj,webProject);
    }

    private static final class ContextAction extends AbstractAction {

        private final Project serverProject;
        private final Project webProject;
        

        public ContextAction(final Project serverProject,final Project webProject) {
            this.serverProject = serverProject;
            this.webProject = webProject;
            setEnabled(DDHelper.needsBeansXml(serverProject, webProject));
            
            //putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
            
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
}

