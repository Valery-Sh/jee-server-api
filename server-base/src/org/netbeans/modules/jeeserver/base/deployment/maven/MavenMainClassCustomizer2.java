/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jeeserver.base.deployment.maven;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;
import javax.swing.JButton;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

/**
 *
 * @author Valery
 */
public class MavenMainClassCustomizer2 {

    public static MavenAuxConfig customize(Project instanceProject ) {
       return customize(instanceProject, null);
    }    
    public static MavenAuxConfig customize(Project instanceProject,String specifiedClass) {
        final String NO_MAIN_CLASS_FOUND = "No Main Class Found";
        JButton customizeButton = createCustomizeButton();

        JButton cancelButton = createCancelButton();
        JButton acceptButton = createAcceptButton();

        acceptButton.setVisible(false);
        // MainClassChooserPanelVisual panel = new MainClassChooserPanelVisual(customizeButton,cancelButton);
        MainClassChooserPanelVisual2 panel = new MainClassChooserPanelVisual2(customizeButton, cancelButton,acceptButton);
        
        panel.getErrorLabel().setVisible(false);
        
        customizeButton.addActionListener(new CustomizerActionListener(instanceProject, panel));

        List<String> classes = Arrays.asList(BaseUtil.getMavenMainClasses(instanceProject));
        
        if (classes.isEmpty()) {
            customizeButton.setEnabled(false);
        }
        
        if ( specifiedClass != null && ! classes.contains(specifiedClass) ) {
            acceptButton.setVisible(false);
            cancelButton.setVisible(true);
            panel.getErrorLabel().setText("The class specified is not a Main Class");
            panel.getErrorLabel().setVisible(true);
            panel.getInfoLabel().setText("Class: " + specifiedClass);
        }
        
        String msg = "Select Main Class for Server Execution";
        MavenAuxConfig config = null;
        DialogDescriptor dd = new DialogDescriptor(panel, msg,
                true, new Object[]{customizeButton, cancelButton, acceptButton}, cancelButton, DialogDescriptor.DEFAULT_ALIGN, null, null);
//                                true, new Object[]{"Select Main Class", "Cancel"}, "Cancel", DialogDescriptor.DEFAULT_ALIGN, null, null);

        DialogDisplayer.getDefault().notify(dd);

        if (dd.getValue() == acceptButton) {
            config = MavenAuxConfig.getInstance(instanceProject);
        }
        return config;

        /*        
         final RequestProcessor.Task task = new RequestProcessor("AddBody").create(new Runnable() {
         @Override
         public void run() {
         JButton customizeButton = createCustomizeButton();

         JButton cancelButton = createCancelButton();
         JButton acceptButton = createAcceptButton();
         customizeButton.addActionListener(new CustomizerActionListener(instanceProject, acceptButton, cancelButton));
                
         acceptButton.setVisible(false);
         // MainClassChooserPanelVisual panel = new MainClassChooserPanelVisual(customizeButton,cancelButton);
         MainClassChooserPanelVisual2 panel = new MainClassChooserPanelVisual2(customizeButton, cancelButton);

         String[] classes = BaseUtil.getMavenMainClasses(instanceProject);
                
                        
         if (classes.length == 0) {
         customizeButton.setEnabled(false);
         } 

         String msg = "Select Main Class for Server Execution";
         DialogDescriptor dd = new DialogDescriptor(panel, msg,
         true, new Object[]{customizeButton, cancelButton, acceptButton}, cancelButton, DialogDescriptor.DEFAULT_ALIGN, null, null);
         //                                true, new Object[]{"Select Main Class", "Cancel"}, "Cancel", DialogDescriptor.DEFAULT_ALIGN, null, null);
                
         DialogDisplayer.getDefault().notify(dd);

         if (dd.getValue() == acceptButton) {
         BaseUtil.out("MavenMainClassCustomizer2 ACCEPTED mainClass=" + BaseUtil.getMavenMainClass(instanceProject));
         config = MavenAuxConfig_OLD.getInstance(instanceProject);
                    
         }
         }
         });
        
         task.schedule(0);
         task.waitFinished();
         return config;
         */
    }

    protected static JButton createCustomizeButton() {
        JButton button = new javax.swing.JButton() {
            @Override
            public void addActionListener(ActionListener al) {
                if (al instanceof CustomizerActionListener) {
                    super.addActionListener(al);
                }
            }
        };

        button.setName("CUSTOMIZE");

        org.openide.awt.Mnemonics.setLocalizedText(button, "Customize");
        button.setEnabled(true);
        return button;

    }

    protected static JButton createCancelButton() {
        JButton button = new javax.swing.JButton();
        button.setName("CANCEL");
        org.openide.awt.Mnemonics.setLocalizedText(button, "Cancel");
        return button;
    }

    protected static JButton createAcceptButton() {
        JButton button = new javax.swing.JButton();
        button.setName("ACCEPT");
        org.openide.awt.Mnemonics.setLocalizedText(button, "Accept");
        return button;
    }

    protected static class CustomizerActionListener implements ActionListener {

        private final Project project;
        private final  MainClassChooserPanelVisual2 panel;
        
        public CustomizerActionListener(Project project, MainClassChooserPanelVisual2 panel) {
            this.project = project;
            this.panel = panel;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String mainClass = null;
            CustomizerProvider cp = project.getLookup().lookup(CustomizerProvider.class);
            if (cp != null) {
                cp.showCustomizer();
                BaseUtil.out("!! ****** AFTER showCustomizer");
                mainClass = MavenAuxConfig.getInstance(project).getMainClass();
                
                List<String> classes = Arrays.asList(BaseUtil.getMavenMainClasses(project));                
                boolean exists = classes.contains(mainClass);
                if ( mainClass != null ) {
                    panel.getInfoLabel().setText(" Selected Class: " + mainClass);
                } else {
                    panel.getInfoLabel().setText(" Selected Class: <not specified>");
                }
                if (mainClass != null && exists) {
                    panel.getErrorLabel().setVisible(false);
                    panel.getAcceptButton().setVisible(true);
                    panel.getCancelButton().setVisible(false);
                    
                }  else if ( mainClass != null ) {
                    panel.getErrorLabel().setText("  The class that you specified doesn't exist");
                    panel.getErrorLabel().setVisible(true);
                    panel.getAcceptButton().setVisible(false);
                    panel.getCancelButton().setVisible(true);
                } else {
                    panel.getErrorLabel().setText(" Main Class is not specified. Try again or Click \"Cancel\"");
                    panel.getErrorLabel().setVisible(true);
                    panel.getAcceptButton().setVisible(false);
                    panel.getCancelButton().setVisible(true);
                }

                BaseUtil.out("!! ****** AFTER showCustomizer main class=" + mainClass);
            }

        }
    }
}
