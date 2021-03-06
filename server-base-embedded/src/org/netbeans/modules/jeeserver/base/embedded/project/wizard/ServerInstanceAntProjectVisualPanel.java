
package org.netbeans.modules.jeeserver.base.embedded.project.wizard;

import java.io.File;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteConstants;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author V. Shyshkin
 */
public class ServerInstanceAntProjectVisualPanel extends InstancePanelVisual implements DocumentListener {
    
    private static final Logger LOG = Logger.getLogger(ServerInstanceAntProjectVisualPanel.class.getName());
    
    public static final String PROP_PROJECT_NAME = "projectName";

    
    private WizardDescriptor wiz;
    private final InstanceWizardPanel panel;

    /**
     * Creates new form NewJPanel
     */
    public ServerInstanceAntProjectVisualPanel(InstanceWizardPanel panel) {
        initComponents();
        this.messageLabel.setVisible(false);
        this.panel = panel;
        addListeners();
    }

    private void addListeners() {
    //    this.serverPort_Spinner.addChangeListener(this);
    //    this.serverDebugPort_Spinner.addChangeListener(this);
    //    this.shutdownPort_Spinner.addChangeListener(this);

        projectNameTextField.getDocument().addDocumentListener(this);
        projectLocationTextField.getDocument().addDocumentListener(this);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        projectNameLabel = new javax.swing.JLabel();
        projectNameTextField = new javax.swing.JTextField();
        projectLocationLabel = new javax.swing.JLabel();
        projectLocationTextField = new javax.swing.JTextField();
        createdFolderLabel = new javax.swing.JLabel();
        createdFolderTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        messageLabel = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(projectNameLabel, org.openide.util.NbBundle.getMessage(ServerInstanceAntProjectVisualPanel.class, "ServerInstanceAntProjectVisualPanel.projectNameLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(projectLocationLabel, org.openide.util.NbBundle.getMessage(ServerInstanceAntProjectVisualPanel.class, "ServerInstanceAntProjectVisualPanel.projectLocationLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(createdFolderLabel, org.openide.util.NbBundle.getMessage(ServerInstanceAntProjectVisualPanel.class, "ServerInstanceAntProjectVisualPanel.createdFolderLabel.text")); // NOI18N

        createdFolderTextField.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(ServerInstanceAntProjectVisualPanel.class, "ServerInstanceAntProjectVisualPanel.browseButton.text")); // NOI18N
        browseButton.setActionCommand(org.openide.util.NbBundle.getMessage(ServerInstanceAntProjectVisualPanel.class, "ServerInstanceAntProjectVisualPanel.browseButton.actionCommand")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        messageLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(messageLabel, org.openide.util.NbBundle.getMessage(ServerInstanceAntProjectVisualPanel.class, "ServerInstanceAntProjectVisualPanel.messageLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(messageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(projectNameLabel)
                            .addComponent(projectLocationLabel)
                            .addComponent(createdFolderLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(projectNameTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 298, Short.MAX_VALUE)
                            .addComponent(projectLocationTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 298, Short.MAX_VALUE)
                            .addComponent(createdFolderTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 298, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(browseButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(projectNameLabel)
                    .addComponent(projectNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(projectLocationLabel)
                    .addComponent(projectLocationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(createdFolderLabel)
                    .addComponent(createdFolderTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 48, Short.MAX_VALUE)
                .addComponent(messageLabel)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        String command = evt.getActionCommand();
        if ("BROWSE".equals(command)) {
            JFileChooser chooser = new JFileChooser();
            FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
            chooser.setDialogTitle("Select Project Location");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            String path = this.projectLocationTextField.getText();
            if (path.length() > 0) {
                File f = new File(path);
                if (f.exists()) {
                    chooser.setSelectedFile(f);
                }
            }
            if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
                File projectDir = chooser.getSelectedFile();
                projectLocationTextField.setText(FileUtil.normalizeFile(projectDir).getAbsolutePath());
            }
            //panel.fireChangeEvent();
        }
    }//GEN-LAST:event_browseButtonActionPerformed
    @Override
    public void addNotify() {
        super.addNotify();
        //same problem as in 31086, initial focus on Cancel button
        projectNameTextField.requestFocus();
    }

    @Override
    public boolean valid(WizardDescriptor wizardDescriptor) {

        if (projectNameTextField.getText().length() == 0) {
            // TODO if using org.openide.dialogs >= 7.8, can use WizardDescriptor.PROP_ERROR_MESSAGE:
            wizardDescriptor.putProperty("WizardPanel_errorMessage",
                    "Project Name is not a valid folder name.");
            return false; // Display name not specified
        }
        File f = FileUtil.normalizeFile(new File(projectLocationTextField.getText()).getAbsoluteFile());
        if (!f.isDirectory()) {
            String message = "Project Folder is not a valid path.";
            wizardDescriptor.putProperty("WizardPanel_errorMessage", message);
            return false;
        }
        final File destFolder = FileUtil.normalizeFile(new File(createdFolderTextField.getText()).getAbsoluteFile());

        File projLoc = destFolder;
        while (projLoc != null && !projLoc.exists()) {
            projLoc = projLoc.getParentFile();
        }
        if (projLoc == null || !projLoc.canWrite()) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage",
                    "Project Folder cannot be created.");
            return false;
        }

        if (FileUtil.toFileObject(projLoc) == null) {
            String message = "Project Folder is not a valid path.";
            wizardDescriptor.putProperty("WizardPanel_errorMessage", message);
            return false;
        }

        File[] kids = destFolder.listFiles();
        if (destFolder.exists() && kids != null && kids.length > 0) {
            // Folder exists and is not empty
            wizardDescriptor.putProperty("WizardPanel_errorMessage",
                    "Project Folder already exists and is not empty.");
            return false;
        }
        wizardDescriptor.putProperty("WizardPanel_errorMessage", "");
        return true;
    }

    @Override
    public void store(WizardDescriptor d) {
        
//        ServerInstanceWizardAction.panelVisited[0] = true;
        boolean[] v =  (boolean[])d.getProperty(SuiteConstants.PANEL_VISITED_PROP); 
        v[0] = true;
        
        String name = projectNameTextField.getText().trim();
        String folder = createdFolderTextField.getText().trim();

        d.putProperty("projdir", new File(folder));
        d.putProperty("name", name);
    }

    @Override
    public void read(WizardDescriptor settings) {
        File projectLocation = (File) settings.getProperty("projdir");
        if (projectLocation == null || projectLocation.getParentFile() == null || !projectLocation.getParentFile().isDirectory()) {
            projectLocation = ProjectChooser.getProjectsFolder();
        } else {
            projectLocation = projectLocation.getParentFile();
        }
        this.projectLocationTextField.setText(projectLocation.getAbsolutePath());

        String projectName = (String) settings.getProperty("name");
        if (projectName == null) {
            projectName = "EmbeddedServerProject";
        }
        this.projectNameTextField.setText(projectName);
        this.projectNameTextField.selectAll();
        
        
    }

    // Implementation of DocumentListener --------------------------------------
    /**
     * Handles changes in the Project name and project directory,
     */
    private void updateTexts(DocumentEvent e) {

        Document doc = e.getDocument();

        if (doc == projectNameTextField.getDocument() || doc == projectLocationTextField.getDocument()) {
            // Change in the project name
            String projectName = projectNameTextField.getText();
            String projectFolder = projectLocationTextField.getText();
            createdFolderTextField.setText(projectFolder + File.separatorChar + projectName);
        }
        panel.fireChangeEvent(); // Notify that the panel changed
    }
    
    @Override
    public void changedUpdate(DocumentEvent e) {
        updateTexts(e);
        if (this.projectNameTextField.getDocument() == e.getDocument()) {
            firePropertyChange(PROP_PROJECT_NAME, null, this.projectNameTextField.getText());
        }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        updateTexts(e);
        if (this.projectNameTextField.getDocument() == e.getDocument()) {
            firePropertyChange(PROP_PROJECT_NAME, null, this.projectNameTextField.getText());
        }
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        updateTexts(e);
        if (this.projectNameTextField.getDocument() == e.getDocument()) {
            firePropertyChange(PROP_PROJECT_NAME, null, this.projectNameTextField.getText());
        }
    }
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JLabel createdFolderLabel;
    private javax.swing.JTextField createdFolderTextField;
    private javax.swing.JLabel messageLabel;
    private javax.swing.JLabel projectLocationLabel;
    private javax.swing.JTextField projectLocationTextField;
    private javax.swing.JLabel projectNameLabel;
    private javax.swing.JTextField projectNameTextField;
    // End of variables declaration//GEN-END:variables

    @Override
    public JLabel getMessageLabel() {
        return messageLabel;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JButton getSaveButton() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
