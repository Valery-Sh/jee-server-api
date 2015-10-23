/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jeeserver.base.embedded.project.wizard;

import java.awt.event.ActionEvent;
import javax.swing.JButton;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.netbeans.modules.jeeserver.base.embedded.project.SuiteManager;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteConstants;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Valery
 */
public class MavenMainClassCustomizer {

    public static void customize(Project instanceProject) {
        final String NO_MAIN_CLASS_FOUND = "No Main Class Found";
        final RequestProcessor.Task task = new RequestProcessor("AddBody").create(new Runnable() {
            @Override
            public void run() {
                JButton sb = createSelectButton();
                JButton cb = createCancelButton();

                // MainClassChooserPanelVisual panel = new MainClassChooserPanelVisual(sb,cb);
                MainClassChooserPanelVisual panel = new MainClassChooserPanelVisual(sb, cb);

                String[] classes = BaseUtil.getMavenMainClasses(instanceProject);

                if (classes.length == 0) {
                    classes = new String[]{NO_MAIN_CLASS_FOUND};
                    sb.setEnabled(false);
                }

                panel.getMainClassesList().setListData(classes);
                String msg = "Select Main Class for Server Execution";
                DialogDescriptor dd = new DialogDescriptor(panel, msg,
                        true, new Object[]{sb, cb}, cb, DialogDescriptor.DEFAULT_ALIGN, null, null);
//                                true, new Object[]{"Select Main Class", "Cancel"}, "Cancel", DialogDescriptor.DEFAULT_ALIGN, null, null);

                DialogDisplayer.getDefault().notify(dd);

                if (dd.getValue() == sb) {
                    int idx = panel.getMainClassesList().getSelectedIndex();
                    if (idx < 0) {
                        return;
                    }
                    String mainClass = (String) panel.getMainClassesList().getSelectedValue();
                    String uri = SuiteManager.getManager(instanceProject).getUri();
                    InstanceProperties.getInstanceProperties(uri)
                            .setProperty(SuiteConstants.MAVEN_MAIN_CLASS_PROP, mainClass);

                }

            }
        });
        
        task.schedule(0);
        task.waitFinished();
    }

    protected static JButton createSelectButton() {
        JButton button = new javax.swing.JButton();
        button.setName("SELECT");
        org.openide.awt.Mnemonics.setLocalizedText(button, "Select Main Class");
        button.setEnabled(false);
        return button;

    }

    protected static JButton createCancelButton() {
        JButton button = new javax.swing.JButton();
        button.setName("CANCEL");
        org.openide.awt.Mnemonics.setLocalizedText(button, "Cancel");
        return button;
    }
}
