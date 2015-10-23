/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jeeserver.base.deployment.progress;

import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.netbeans.spi.project.ActionProvider;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Valery
 */
public class BaseActionProviderExecutor implements Runnable{
    
    protected static final RequestProcessor RP = new RequestProcessor(AbstractProgressObject.class);
    private String command;
    private Project project;
    
    public void execute(String command, Project project) {
        this.command = command;
        this.project = project;
        RP.execute(this);
    }

    @Override
    public void run() {
        Lookup context = project.getLookup();
        ActionProvider ap = project.getLookup().lookup(ActionProvider.class);
/*        for ( String a : ap.getSupportedActions() ) {
            BaseUtil.out("**** action = " + a);
        }
*/        
        ap.invokeAction(command, context);
    }
}
