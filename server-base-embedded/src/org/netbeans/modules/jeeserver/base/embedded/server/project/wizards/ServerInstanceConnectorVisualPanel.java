package org.netbeans.modules.jeeserver.base.embedded.server.project.wizards;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.jeeserver.base.deployment.specifics.ServerSpecifics;
import org.netbeans.modules.jeeserver.base.deployment.specifics.ServerSpecificsProvider;
import static org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants.*;

import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.netbeans.modules.jeeserver.base.embedded.specifics.EmbeddedServerSpecifics;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteUtil;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

public class ServerInstanceConnectorVisualPanel extends InstancePanelVisual implements DocumentListener, ActionListener {

    public static final String PROP_PORT = "portNumber";
    public static final String PROP_DEBUG_PORT = "debugPortNumber";
    public static final String PROP_SHUTDOWN_PORT = "shutdownPortNumber";
    private WizardDescriptor wiz;
    private final InstanceWizardPanel panel;
    
    public ServerInstanceConnectorVisualPanel(InstanceWizardPanel panel) {
        initComponents();
        saveButton.setVisible(false);
        nextTimeMsgLabel.setVisible(false);
        this.messageLabel.setVisible(false);
        this.panel = panel;
        addListeners();
    }

    @Override
    public String getName() {
        return "Configure Server Instance";
    }
    private void addListeners() {
        serverId_ComboBox.addActionListener(this);
        //incremental_Deployment_CheckBox.addActionListener(this);
        projectDisplayNameTextField.getDocument().addDocumentListener(this);

    }
    PortHandler portHandler;
    PortHandler debugPortHandler;
    PortHandler shutdownPortHandler;

    private void addPortListeners() {
        if (portHandler != null) {
            return;
        }
        JSpinner.NumberEditor me = (JSpinner.NumberEditor) serverPort_Spinner.getEditor();
        JFormattedTextField tf = me.getTextField();
        portHandler = new PortHandler(serverPort_Spinner);
        tf.getDocument().addDocumentListener(portHandler);

        me = (JSpinner.NumberEditor) serverDebugPort_Spinner.getEditor();
        tf = me.getTextField();
        debugPortHandler = new PortHandler(serverDebugPort_Spinner);
        tf.getDocument().addDocumentListener(debugPortHandler);

        //if (needsShutdownPort()) {
        me = (JSpinner.NumberEditor) shutdownPort_Spinner.getEditor();
        tf = me.getTextField();
        shutdownPortHandler = new PortHandler(serverDebugPort_Spinner);
        tf.getDocument().addDocumentListener(shutdownPortHandler);
        //}


    }

    @Override
    public boolean valid(WizardDescriptor wizardDescriptor) {
        wizardDescriptor.putProperty("WizardPanel_errorMessage", null);
        wizardDescriptor.putProperty("WizardPanel_warningMessage", null);


        int port;
        try {
            port = Integer.parseInt(getPort(serverPort_Spinner));
        } catch (NumberFormatException e) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage",
                    NbBundle.getMessage(ServerInstanceConnectorVisualPanel.class, "MSG_NOT_A_NUMBER", getPort(serverPort_Spinner)));
            return false;
        }
        if (port < 0) {
            // TODO if using org.openide.dialogs >= 7.8, can use WizardDescriptor.PROP_ERROR_MESSAGE:
            wizardDescriptor.putProperty("WizardPanel_errorMessage",
                    NbBundle.getMessage(ServerInstanceConnectorVisualPanel.class, "MSG_INVALID_PORT"));
            return false;
        }
        
        try {
            Integer.parseInt(getPort(serverDebugPort_Spinner));
        } catch (NumberFormatException e) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage",
                    NbBundle.getMessage(ServerInstanceConnectorVisualPanel.class, "MSG_NOT_A_NUMBER", getPort(serverDebugPort_Spinner)));
            return false;
        }
        if (Integer.parseInt(getPort(serverDebugPort_Spinner)) < 0) {
            // TODO if using org.openide.dialogs >= 7.8, can use WizardDescriptor.PROP_ERROR_MESSAGE:
            wizardDescriptor.putProperty("WizardPanel_errorMessage",
                    NbBundle.getMessage(ServerInstanceConnectorVisualPanel.class, "MSG_INVALID_DEBUG_PORT"));
            return false;
        }

        if (needsShutdownPort()) {
            try {
                Integer.parseInt(getPort(shutdownPort_Spinner));
            } catch (NumberFormatException e) {
                wizardDescriptor.putProperty("WizardPanel_errorMessage",
                        NbBundle.getMessage(ServerInstanceConnectorVisualPanel.class, "MSG_NOT_A_NUMBER", getPort(shutdownPort_Spinner)));
                return false;
            }
            if (Integer.parseInt(getPort(shutdownPort_Spinner)) < 0) {
                // TODO if using org.openide.dialogs >= 7.8, can use WizardDescriptor.PROP_ERROR_MESSAGE:
                wizardDescriptor.putProperty("WizardPanel_errorMessage",
                        NbBundle.getMessage(ServerInstanceConnectorVisualPanel.class, "MSG_INVALID_SHUTDOWN_PORT"));
                return false;
            }

        }
        port = Integer.parseInt(getPort(serverPort_Spinner));
        String excludeUri = (String)wiz.getProperty(URL_PROP);
        if (SuiteUtil.isHttpPortBusy(port, excludeUri) ) {
            wizardDescriptor.putProperty("WizardPanel_warningMessage",
                    NbBundle.getMessage(ServerInstanceConnectorVisualPanel.class, "MSG_HTTP_PORT_IN_USE", String.valueOf(port)));
            return true;
        }
        if (needsShutdownPort()) {
            port = Integer.parseInt(getPort(shutdownPort_Spinner));
            if (SuiteUtil.isShutdownPortBusy(port, excludeUri)) {
                wizardDescriptor.putProperty("WizardPanel_warningMessage",
                        NbBundle.getMessage(ServerInstanceConnectorVisualPanel.class, "MSG_SHUTDOWN_PORT_IN_USE", String.valueOf(port)));
                return true;
            }
        }

        return true;
    }

    ServerSpecifics getSpecifics() {
        String serverId = BaseUtils.getServerIdByAcualId(getActualServerId());
        return BaseUtils.getServerSpecifics(serverId);
    }

    boolean needsShutdownPort() {
        return getSpecifics().needsShutdownPort();
    }

    @Override
    public void store(WizardDescriptor wiz) {
        
//        ServerInstanceWizardAction.panelVisited[1] = true;
        boolean[] v =  (boolean[])wiz.getProperty(ServerInstanceWizardAction.PANEL_VISITED_PROP); 
        v[0] = true;
        
        wiz.putProperty(HTTP_PORT_PROP, getPort());
        wiz.putProperty(DEBUG_PORT_PROP, getDebugPort());
        wiz.putProperty(HOST_PROP, getHost());
        wiz.putProperty(DISPLAY_NAME_PROP, getDisplayName());
        
//        String p = getShutdownPort();
        wiz.putProperty(SHUTDOWN_PORT_PROP, getShutdownPort());
        DefaultComboBoxModel<String> dcm = (DefaultComboBoxModel<String>) serverId_ComboBox.getModel();
        
        if ( this.serverId_ComboBox.isEnabled() ) {
            //-----------------------------------------------------------
            // Add New Instance or Add Existing. 
            // For customizer we shouldn't store those properties
            //------------------------------------------------------------
            String actualServerId = (String) dcm.getSelectedItem();
            String serverId = BaseUtils.getServerIdByAcualId(actualServerId);
            wiz.putProperty(SERVER_ID_PROP, serverId);
            wiz.putProperty(SERVER_ACTUAL_ID_PROP, actualServerId);
        }
//        wiz.putProperty(INCREMENTAL_DEPLOYMENT, isIncrementalDeployment());
    }
    
/*    String isIncrementalDeployment() {
        return incremental_Deployment_CheckBox.isSelected() ?
                "true" : "false";
    }
*/    
    String getActualServerId() {
        DefaultComboBoxModel<String> dcm = (DefaultComboBoxModel<String>) serverId_ComboBox.getModel();
        return (String) dcm.getSelectedItem();
    }
    String getDisplayName() {
        return this.projectDisplayNameTextField.getText();
    }

    String getPort() {
        return String.valueOf(serverPort_Spinner.getValue());
    }

    String getPort(JSpinner spinner) {
        JSpinner.NumberEditor ne = (JSpinner.NumberEditor) spinner.getEditor();
        return ne.getTextField().getText();
    }

    String getDebugPort() {
        return String.valueOf(serverDebugPort_Spinner.getValue());
    }

    String getShutdownPort() {
        return String.valueOf(shutdownPort_Spinner.getValue());
    }

    String getHost() {
        return this.hostTextField.getText().trim();
    }

    DefaultListModel buildListModel(String[] strings) {
        DefaultListModel<String> m = new DefaultListModel<>();
        for (String s : strings) {
            m.addElement(s);
        }
        return m;
    }

    DefaultComboBoxModel<String> buildComboModel() {
        DefaultComboBoxModel<String> m = new DefaultComboBoxModel<>();
        
        DeploymentFactory[] fs = DeploymentFactoryManager.getInstance().getDeploymentFactories();
        
        for (DeploymentFactory f : fs) {
            if (!(f instanceof ServerSpecificsProvider)) {
                continue;
            }
            ServerSpecificsProvider ssp = (ServerSpecificsProvider) f;
            if ( ! (ssp.getSpecifics() instanceof EmbeddedServerSpecifics) ) {
                continue;
            }
            if (! (ssp.getSpecifics() instanceof EmbeddedServerSpecifics ) ) {
                continue;
            }
            // OLD m.addElement(((ServerSpecificsProvider) f).getServerId());
            for ( String id : ((ServerSpecificsProvider) f).getSupportedServerIds()) {
                m.addElement(id);
            }
        }

        return m;
    }

    @Override
    public void read(WizardDescriptor wiz) {
        this.wiz = wiz;
        //-----  Server ID ---------
        DefaultComboBoxModel<String> dcm = buildComboModel();
        serverId_ComboBox.setModel(dcm);
        String actualServerId = (String) wiz.getProperty(SERVER_ACTUAL_ID_PROP);
        if (actualServerId != null && dcm.getIndexOf(actualServerId) >= 0) {
            dcm.setSelectedItem(actualServerId);
            this.serverId_ComboBox.setFont(new Font((String)dcm.getSelectedItem(),Font.BOLD, serverId_ComboBox.getFont().getSize() ));            
        }
        
        
        File projectLocation = (File) wiz.getProperty("projdir");
        this.projectLocationTextField.setText(projectLocation.getAbsolutePath());
        String projectName = (String) wiz.getProperty("name");
        
        this.projectNameTextField.setText(projectName);
        String displayName = (String) wiz.getProperty(DISPLAY_NAME_PROP);
        if ( displayName == null ) {
            displayName = "";
            if ( projectName != null ) {
                displayName = projectName;
            }
        }
        this.projectDisplayNameTextField.setText(displayName);
        

/*        String incrDepl = (String) wiz.getProperty("incrementalDeployment");
        if ( incrDepl == null ) {
            incrDepl = "true";
        }
        if ( incrDepl.equals("true")) {
            incremental_Deployment_CheckBox.setSelected(true);
        } else {
            incremental_Deployment_CheckBox.setSelected(false);
        }
*/        
        hostTextField.setText("localhost");
        readDefaultPortSettings(wiz);

    }

    void readDefaultPortSettings(WizardDescriptor settings) {
        
        String port = (String) settings.getProperty(HTTP_PORT_PROP);
        
        if (port == null) {
            port = String.valueOf(getSpecifics().getDefaultPort());
        }
        serverPort_Spinner.setValue(Integer.parseInt(port));

        port = (String) settings.getProperty(DEBUG_PORT_PROP);
        if (port == null) {
            port = String.valueOf(getSpecifics().getDefaultDebugPort());
        }
        serverDebugPort_Spinner.setValue(Integer.parseInt(port));

        port = (String) settings.getProperty(SHUTDOWN_PORT_PROP);

        if (port == null) {
            port = String.valueOf(getSpecifics().getDefaultShutdownPort());
        }

        shutdownPort_Spinner.setValue(Integer.parseInt(port));

        if (!needsShutdownPort()) {
            shutdownPort_Spinner.setVisible(false);
            shutdownPort_Label.setVisible(false);
        } else {
            shutdownPort_Spinner.setVisible(true);
            shutdownPort_Label.setVisible(true);
        }
        addPortListeners();
    }

    @Override
    public void validate(WizardDescriptor d) {
        // nothing to validate
    }

    
    @Override
    public void changedUpdate(DocumentEvent e) {
        panel.fireChangeEvent(); // Notify that the panel changed
        if (e.getDocument() == this.projectDisplayNameTextField.getDocument()) {
            firePropertyChange(DISPLAY_NAME_PROP, null, this.projectDisplayNameTextField.getText());
        }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        panel.fireChangeEvent();
        if (this.projectDisplayNameTextField.getDocument() == e.getDocument()) {
            firePropertyChange(DISPLAY_NAME_PROP, null, this.projectDisplayNameTextField.getText());
        }
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        panel.fireChangeEvent();
        if (this.projectNameTextField.getDocument() == e.getDocument()) {
            firePropertyChange(DISPLAY_NAME_PROP, null, this.projectDisplayNameTextField.getText());
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        panel.fireChangeEvent(); // Notify that the panel changed
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == serverId_ComboBox) {
            panel.fireChangeEvent();
            readDefaultPortSettings(wiz);
        } 
    }

    @Override
    public JButton getSaveButton() {
        return saveButton;
    }

    protected class PortHandler implements DocumentListener {

        private JSpinner spinner;

        public PortHandler(JSpinner spinner) {
            this.spinner = spinner;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            firePropertyChange();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            firePropertyChange();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            firePropertyChange();
        }

        private void firePropertyChange() {
            JSpinner.NumberEditor me = (JSpinner.NumberEditor) spinner.getEditor();
            JFormattedTextField tf = me.getTextField();
            SwingUtilities.invokeLater(new Runnable(){

                @Override
                public void run() {
                    panel.fireChangeEvent();      
                }
            });
            //panel.fireChangeEvent();
            if (spinner == serverPort_Spinner) {
                ServerInstanceConnectorVisualPanel.this.firePropertyChange(PROP_PORT, null, tf.getText());
            } else if (spinner == shutdownPort_Spinner) {
                ServerInstanceConnectorVisualPanel.this.firePropertyChange(PROP_SHUTDOWN_PORT, null, tf.getText());
            } else if (spinner == serverDebugPort_Spinner) {
                ServerInstanceConnectorVisualPanel.this.firePropertyChange(PROP_DEBUG_PORT, null, tf.getText());
            }
            
        }
    }//class

    @Override
    public JLabel getMessageLabel() {
        return messageLabel;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        hostLabel = new javax.swing.JLabel();
        hostTextField = new javax.swing.JTextField();
        serverPortLabel = new javax.swing.JLabel();
        serverPort_Spinner = new javax.swing.JSpinner();
        serverDebugPortLabel = new javax.swing.JLabel();
        serverDebugPort_Spinner = new javax.swing.JSpinner();
        shutdownPort_Label = new javax.swing.JLabel();
        shutdownPort_Spinner = new javax.swing.JSpinner();
        nextTimeMsgLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        serverId_ComboBox = new javax.swing.JComboBox();
        messageLabel = new javax.swing.JLabel();
        projectNameTextField = new javax.swing.JTextField();
        projectNameLabel = new javax.swing.JLabel();
        projectLocationLabel = new javax.swing.JLabel();
        projectLocationTextField = new javax.swing.JTextField();
        projectDisplayNameLabel = new javax.swing.JLabel();
        projectDisplayNameTextField = new javax.swing.JTextField();
        horSeparator = new javax.swing.JSeparator();
        saveButton = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(hostLabel, org.openide.util.NbBundle.getMessage(ServerInstanceConnectorVisualPanel.class, "ServerInstanceConnectorVisualPanel.hostLabel.text")); // NOI18N

        hostTextField.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(serverPortLabel, org.openide.util.NbBundle.getMessage(ServerInstanceConnectorVisualPanel.class, "ServerInstanceConnectorVisualPanel.serverPortLabel.text")); // NOI18N

        serverPort_Spinner.setEditor(new javax.swing.JSpinner.NumberEditor(serverPort_Spinner, "#"));

        org.openide.awt.Mnemonics.setLocalizedText(serverDebugPortLabel, org.openide.util.NbBundle.getMessage(ServerInstanceConnectorVisualPanel.class, "ServerInstanceConnectorVisualPanel.serverDebugPortLabel.text")); // NOI18N

        serverDebugPort_Spinner.setEditor(new javax.swing.JSpinner.NumberEditor(serverDebugPort_Spinner, "#"));

        org.openide.awt.Mnemonics.setLocalizedText(shutdownPort_Label, "Shutdown Port:"); // NOI18N

        shutdownPort_Spinner.setEditor(new javax.swing.JSpinner.NumberEditor(shutdownPort_Spinner, "#"));

        org.openide.awt.Mnemonics.setLocalizedText(nextTimeMsgLabel, "Note: Changes will take affect the next time you start the server"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(nextTimeMsgLabel)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(hostLabel)
                            .addComponent(serverPortLabel)
                            .addComponent(serverDebugPortLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(shutdownPort_Label))
                        .addGap(62, 62, 62)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(serverPort_Spinner, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(hostTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 242, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(shutdownPort_Spinner, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(serverDebugPort_Spinner, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)))))
                .addContainerGap(46, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(hostTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(hostLabel))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(serverPort_Spinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(serverPortLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(serverDebugPort_Spinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(serverDebugPortLabel))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(shutdownPort_Label)
                    .addComponent(shutdownPort_Spinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 47, Short.MAX_VALUE)
                .addComponent(nextTimeMsgLabel)
                .addContainerGap())
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(ServerInstanceConnectorVisualPanel.class, "ServerInstanceConnectorVisualPanel.jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, "Server ID"); // NOI18N

        serverId_ComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        org.openide.awt.Mnemonics.setLocalizedText(messageLabel, org.openide.util.NbBundle.getMessage(ServerInstanceConnectorVisualPanel.class, "ServerInstanceConnectorVisualPanel.messageLabel.text")); // NOI18N

        projectNameTextField.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(projectNameLabel, org.openide.util.NbBundle.getMessage(ServerInstanceConnectorVisualPanel.class, "ServerInstanceConnectorVisualPanel.projectNameLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(projectLocationLabel, org.openide.util.NbBundle.getMessage(ServerInstanceConnectorVisualPanel.class, "ServerInstanceConnectorVisualPanel.projectLocationLabel.text")); // NOI18N

        projectLocationTextField.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(projectDisplayNameLabel, "Display Name"); // NOI18N

        projectDisplayNameTextField.setText(org.openide.util.NbBundle.getMessage(ServerInstanceConnectorVisualPanel.class, "ServerInstanceConnectorVisualPanel.projectDisplayNameTextField.text")); // NOI18N

        horSeparator.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        org.openide.awt.Mnemonics.setLocalizedText(saveButton, "Save changes"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(horSeparator)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 516, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(projectNameLabel)
                                    .addComponent(projectLocationLabel)
                                    .addComponent(projectDisplayNameLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(projectLocationTextField, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(projectNameTextField)
                                    .addComponent(projectDisplayNameTextField)))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addGap(18, 18, 18)
                                .addComponent(serverId_ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 291, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(saveButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(messageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addContainerGap())))
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
                    .addComponent(projectLocationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(projectDisplayNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(projectDisplayNameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(horSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(serverId_ComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(26, 26, 26)
                .addComponent(jTabbedPane1)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(saveButton)
                    .addComponent(messageLabel))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSeparator horSeparator;
    private javax.swing.JLabel hostLabel;
    private javax.swing.JTextField hostTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel messageLabel;
    private javax.swing.JLabel nextTimeMsgLabel;
    private javax.swing.JLabel projectDisplayNameLabel;
    private javax.swing.JTextField projectDisplayNameTextField;
    private javax.swing.JLabel projectLocationLabel;
    private javax.swing.JTextField projectLocationTextField;
    private javax.swing.JLabel projectNameLabel;
    private javax.swing.JTextField projectNameTextField;
    private javax.swing.JButton saveButton;
    private javax.swing.JLabel serverDebugPortLabel;
    private javax.swing.JSpinner serverDebugPort_Spinner;
    private javax.swing.JComboBox serverId_ComboBox;
    private javax.swing.JLabel serverPortLabel;
    private javax.swing.JSpinner serverPort_Spinner;
    private javax.swing.JLabel shutdownPort_Label;
    private javax.swing.JSpinner shutdownPort_Spinner;
    // End of variables declaration//GEN-END:variables
}
