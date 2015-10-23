/**
 * This file is part of Jetty Server support in NetBeans IDE.
 *
 * Jetty Server support in NetBeans IDE is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the License,
 * or (at your option) any later version.
 *
 * Jetty Server support in NetBeans IDE is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should see the GNU General Public License here:
 * <http://www.gnu.org/licenses/>.
 */
package org.netbeans.modules.jeeserver.jetty.project.template;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.ParseException;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.deployment.specifics.ServerSpecifics;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.netbeans.modules.jeeserver.jetty.util.JettyConstants;
import org.netbeans.modules.jeeserver.jetty.util.Utils;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

public class JettyServerInstancePanelVisual extends JPanel implements DocumentListener {

    public static final String PROP_PROJECT_NAME = "projectName";

    public static final String PROP_PORT = BaseConstants.HTTP_PORT_PROP;
    public static final String PROP_DEBUG_PORT = BaseConstants.DEBUG_PORT_PROP;
    public static final String PROP_STOP_PORT = BaseConstants.SHUTDOWN_PORT_PROP;
    public static final String PROP_HTTPS_PORT = JettyConstants.JETTY_HTTPS_PORT;
    public static final String PROP_SPDY_PORT = JettyConstants.JETTY_SPDY_PORT;

    public static final String PROP_JETTY_HOME = BaseConstants.HOME_DIR_PROP;
    public static final String PROP_HTTP_TIMEOUT = JettyConstants.JETTY_HTTP_TIMEOUT;
    public static final String PROP_HTTPS_TIMEOUT = JettyConstants.JETTY_HTTPS_TIMEOUT;
    public static final String PROP_SPDY_TIMEOUT = JettyConstants.JETTY_SPDY_TIMEOUT;

    public static final String PROP_ENABLE_NPN = JettyConstants.ENABLE_NPN;
    public static final String PROP_ENABLE_JSF = JettyConstants.ENABLE_JSF;
    public static final String PROP_ENABLE_CDI = JettyConstants.ENABLE_CDI;
    public static final String PROP_ENABLE_SPDY = JettyConstants.ENABLE_SPDY;
    public static final String PROP_ENABLE_HTTPS = JettyConstants.ENABLE_HTTPS;

    protected JettyServerInstanceWizardPanel panel;

    protected boolean customizing = false;

    JettyServerInstancePanelVisual.PortHandler portHandler;
    JettyServerInstancePanelVisual.PortHandler debugPortHandler;
    JettyServerInstancePanelVisual.PortHandler stopPortHandler;
    JettyServerInstancePanelVisual.PortHandler httpsPortHandler;
    JettyServerInstancePanelVisual.PortHandler spdyPortHandler;

    public JettyServerInstancePanelVisual(JettyServerInstanceWizardPanel panel) {
        initComponents();
        this.panel = panel;
        if (panel == null) {
            customizing = true;
            projectNameTextField.setEditable(false);
            projectLocationTextField.setEditable(false);
        }
        noteLabel.setVisible(customizing);
        saveButton.setVisible(false);
        messageLabel.setVisible(false);
        // Register listener on the textFields to make the automatic updates
        addListeners();
        httpTimeoutFormattedTextField.setText("30000");
        httpsTimeoutFormattedTextField.setText("30000");
        spdyTimeoutFormattedTextField.setText("30000");
        confTabbedPane.remove(1);
        confTabbedPane.remove(1);
        confTabbedPane.remove(1);
        confTabbedPane.remove(1);        
        confTabbedPane.remove(1);        
        
        jsfPanel.setVisible(false);
        cdiPanel.setVisible(false);
        sslPanel.setVisible(false);
        httpsPanel.setVisible(false);
        spdyPanel.setVisible(false);

    }

    protected void enableHttpsFields(boolean enabled) {
        httpsPort_Spinner.setEnabled(enabled);
        httpsTimeoutFormattedTextField.setEditable(enabled);
    }

    protected void enableSPDYFields(boolean enabled) {
        spdyPort_Spinner.setEnabled(enabled);
        spdyTimeoutFormattedTextField.setEditable(enabled);
    }

    public void setSaveButtonVisible(boolean vis) {
        saveButton.setVisible(vis);
    }

    final void addListeners() {
        projectNameTextField.getDocument().addDocumentListener(this);
        httpTimeoutFormattedTextField.getDocument().addDocumentListener(this);
        httpsTimeoutFormattedTextField.getDocument().addDocumentListener(this);
        spdyTimeoutFormattedTextField.getDocument().addDocumentListener(this);

        projectLocationTextField.getDocument().addDocumentListener(this);
        jettyHomeTextField.getDocument().addDocumentListener(this);
        enableJsfCheckBox.addActionListener(new CheckBoxActionHandler(enableJsfCheckBox));
        enableCDICheckBox.addActionListener(new CheckBoxActionHandler(enableCDICheckBox));
        enableSPDYCheckBox.addActionListener(new CheckBoxActionHandler(enableSPDYCheckBox));
        enableHttpsCheckBox.addActionListener(new CheckBoxActionHandler(enableHttpsCheckBox));

    }

    private void addPortListeners() {
        if (portHandler != null) {
            return;
        }
        JSpinner.NumberEditor me = (JSpinner.NumberEditor) serverPort_Spinner.getEditor();
        JFormattedTextField tf = me.getTextField();
        portHandler = new JettyServerInstancePanelVisual.PortHandler(serverPort_Spinner);
        tf.getDocument().addDocumentListener(portHandler);

        me = (JSpinner.NumberEditor) serverDebugPort_Spinner.getEditor();
        tf = me.getTextField();
        debugPortHandler = new JettyServerInstancePanelVisual.PortHandler(serverDebugPort_Spinner);
        tf.getDocument().addDocumentListener(debugPortHandler);

        me = (JSpinner.NumberEditor) serverStopPort_Spinner.getEditor();
        tf = me.getTextField();
        stopPortHandler = new JettyServerInstancePanelVisual.PortHandler(serverStopPort_Spinner);
        tf.getDocument().addDocumentListener(stopPortHandler);

        me = (JSpinner.NumberEditor) httpsPort_Spinner.getEditor();
        tf = me.getTextField();
        httpsPortHandler = new JettyServerInstancePanelVisual.PortHandler(httpsPort_Spinner);
        tf.getDocument().addDocumentListener(httpsPortHandler);

        me = (JSpinner.NumberEditor) spdyPort_Spinner.getEditor();
        tf = me.getTextField();
        spdyPortHandler = new JettyServerInstancePanelVisual.PortHandler(spdyPort_Spinner);
        tf.getDocument().addDocumentListener(spdyPortHandler);

    }

    public JLabel getMessageLabel() {
        return messageLabel;
    }

    public JButton getSaveButton() {
        return saveButton;
    }

    public String getProjectName() {
        return this.projectNameTextField.getText();
    }

    public String getJettyHome() {
        return this.jettyHomeTextField.getText();
    }

    String getServerId() {
        return "jettystandalone";
    }

    String getPort() {
        return String.valueOf(serverPort_Spinner.getValue());
    }

    String getPort(JSpinner spinner) {
        JSpinner.NumberEditor ne = (JSpinner.NumberEditor) spinner.getEditor();
        return ne.getTextField().getText();
    }

    String getHttpsPort() {
        return String.valueOf(httpsPort_Spinner.getValue());
    }

    String getSPDYPort() {
        return String.valueOf(spdyPort_Spinner.getValue());
    }

    String getSslPort() {
        return String.valueOf(sslPort_Spinner.getValue());
    }

    String getDebugPort() {
        return String.valueOf(serverDebugPort_Spinner.getValue());
    }

    String getStopPort() {
        return String.valueOf(serverStopPort_Spinner.getValue());
    }

    String getHost() {
        return this.hostTextField.getText().trim();
    }

    boolean isJSFEnabled() {
        return this.enableJsfCheckBox.isSelected();
    }

    boolean isCDIEnabled() {
        return this.enableCDICheckBox.isSelected();
    }

    boolean isSPDYEnabled() {
        return this.enableSPDYCheckBox.isSelected();
    }

    boolean isHttpsEnabled() {
        return this.enableHttpsCheckBox.isSelected();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jSplitPane2 = new javax.swing.JSplitPane();
        projectNameLabel = new javax.swing.JLabel();
        projectNameTextField = new javax.swing.JTextField();
        projectLocationLabel = new javax.swing.JLabel();
        projectLocationTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        createdFolderLabel = new javax.swing.JLabel();
        createdFolderTextField = new javax.swing.JTextField();
        confTabbedPane = new javax.swing.JTabbedPane();
        connectionPanel = new javax.swing.JPanel();
        hostLabel = new javax.swing.JLabel();
        hostTextField = new javax.swing.JTextField();
        serverPortLabel = new javax.swing.JLabel();
        serverPort_Spinner = new javax.swing.JSpinner();
        serverDebugPortLabel = new javax.swing.JLabel();
        serverDebugPort_Spinner = new javax.swing.JSpinner();
        httpTimeoutLabel = new javax.swing.JLabel();
        httpTimeoutFormattedTextField = new javax.swing.JFormattedTextField();
        noteLabel = new javax.swing.JLabel();
        serverStopPortLabel = new javax.swing.JLabel();
        serverStopPort_Spinner = new javax.swing.JSpinner();
        jsfPanel = new javax.swing.JPanel();
        enableJsfCheckBox = new javax.swing.JCheckBox();
        cdiPanel = new javax.swing.JPanel();
        enableCDICheckBox = new javax.swing.JCheckBox();
        httpsPanel = new javax.swing.JPanel();
        enableHttpsCheckBox = new javax.swing.JCheckBox();
        httpsPortLabel = new javax.swing.JLabel();
        httpsTimeoutLabel = new javax.swing.JLabel();
        httpsPort_Spinner = new javax.swing.JSpinner();
        httpsTimeoutFormattedTextField = new javax.swing.JFormattedTextField();
        sslPanel = new javax.swing.JPanel();
        sslPortLabel = new javax.swing.JLabel();
        sslPort_Spinner = new javax.swing.JSpinner();
        keystoreLabel = new javax.swing.JLabel();
        truststoreLabel = new javax.swing.JLabel();
        keystoreTextField = new javax.swing.JTextField();
        truststoreTextField = new javax.swing.JTextField();
        spdyPanel = new javax.swing.JPanel();
        enableSPDYCheckBox = new javax.swing.JCheckBox();
        spdyPortLabel = new javax.swing.JLabel();
        spdyPort_Spinner = new javax.swing.JSpinner();
        spdyTimeoutLabel = new javax.swing.JLabel();
        spdyTimeoutFormattedTextField = new javax.swing.JFormattedTextField();
        jettyHomeLabel = new javax.swing.JLabel();
        jettyHomeTextField = new javax.swing.JTextField();
        jettyHomeBrowseButton = new javax.swing.JButton();
        horSeparator = new javax.swing.JSeparator();
        jPanel2 = new javax.swing.JPanel();
        messageLabel = new javax.swing.JLabel();
        saveButton = new javax.swing.JButton();

        projectNameLabel.setLabelFor(projectNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectNameLabel, "Jetty Base Folder:"); // NOI18N

        projectLocationLabel.setLabelFor(projectLocationTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectLocationLabel, "Jetty Base Location:"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, "Browse..."); // NOI18N
        browseButton.setActionCommand(org.openide.util.NbBundle.getMessage(JettyServerInstancePanelVisual.class, "JettyServerInstancePanelVisual.browseButton.actionCommand")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        createdFolderLabel.setLabelFor(createdFolderTextField);
        org.openide.awt.Mnemonics.setLocalizedText(createdFolderLabel, "Created Folder:"); // NOI18N

        createdFolderTextField.setEditable(false);

        confTabbedPane.setName(""); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(hostLabel, "Host:"); // NOI18N

        hostTextField.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(serverPortLabel, "Http Port:"); // NOI18N

        serverPort_Spinner.setEditor(new javax.swing.JSpinner.NumberEditor(serverPort_Spinner, "#"));

        org.openide.awt.Mnemonics.setLocalizedText(serverDebugPortLabel, "Debug Port:"); // NOI18N

        serverDebugPort_Spinner.setEditor(new javax.swing.JSpinner.NumberEditor(serverDebugPort_Spinner, "#"));

        org.openide.awt.Mnemonics.setLocalizedText(httpTimeoutLabel, "Http Timeout:"); // NOI18N

        httpTimeoutFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("######"))));
        httpTimeoutFormattedTextField.setFocusLostBehavior(javax.swing.JFormattedTextField.PERSIST);

        org.openide.awt.Mnemonics.setLocalizedText(noteLabel, "Note: Changes will take affect the next time you start the server"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(serverStopPortLabel, "Stop Port:"); // NOI18N

        serverStopPort_Spinner.setEditor(new javax.swing.JSpinner.NumberEditor(serverStopPort_Spinner, "#"));

        javax.swing.GroupLayout connectionPanelLayout = new javax.swing.GroupLayout(connectionPanel);
        connectionPanel.setLayout(connectionPanelLayout);
        connectionPanelLayout.setHorizontalGroup(
            connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(connectionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(noteLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 595, Short.MAX_VALUE)
                    .addGroup(connectionPanelLayout.createSequentialGroup()
                        .addGroup(connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(connectionPanelLayout.createSequentialGroup()
                                .addGroup(connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(hostLabel)
                                    .addComponent(serverPortLabel)
                                    .addComponent(serverDebugPortLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(78, 78, 78)
                                .addGroup(connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(serverPort_Spinner, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(serverDebugPort_Spinner, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(hostTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 242, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(connectionPanelLayout.createSequentialGroup()
                                .addComponent(serverStopPortLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(78, 78, 78)
                                .addComponent(serverStopPort_Spinner, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(connectionPanelLayout.createSequentialGroup()
                                .addComponent(httpTimeoutLabel)
                                .addGap(73, 73, 73)
                                .addComponent(httpTimeoutFormattedTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 242, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        connectionPanelLayout.setVerticalGroup(
            connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(connectionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(hostTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(hostLabel))
                .addGap(18, 18, 18)
                .addGroup(connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(serverPort_Spinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(serverPortLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(serverDebugPort_Spinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(serverDebugPortLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(serverStopPort_Spinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(serverStopPortLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 21, Short.MAX_VALUE)
                .addGroup(connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(httpTimeoutLabel)
                    .addComponent(httpTimeoutFormattedTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(noteLabel)
                .addContainerGap())
        );

        confTabbedPane.addTab("Connection", connectionPanel);

        org.openide.awt.Mnemonics.setLocalizedText(enableJsfCheckBox, "Support Java Server Faces (JSF)"); // NOI18N

        javax.swing.GroupLayout jsfPanelLayout = new javax.swing.GroupLayout(jsfPanel);
        jsfPanel.setLayout(jsfPanelLayout);
        jsfPanelLayout.setHorizontalGroup(
            jsfPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jsfPanelLayout.createSequentialGroup()
                .addGap(155, 155, 155)
                .addComponent(enableJsfCheckBox)
                .addContainerGap(211, Short.MAX_VALUE))
        );
        jsfPanelLayout.setVerticalGroup(
            jsfPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jsfPanelLayout.createSequentialGroup()
                .addGap(89, 89, 89)
                .addComponent(enableJsfCheckBox)
                .addContainerGap(153, Short.MAX_VALUE))
        );

        confTabbedPane.addTab(" JSF ", jsfPanel);

        org.openide.awt.Mnemonics.setLocalizedText(enableCDICheckBox, "Support Dependency Injection (CDI)"); // NOI18N

        javax.swing.GroupLayout cdiPanelLayout = new javax.swing.GroupLayout(cdiPanel);
        cdiPanel.setLayout(cdiPanelLayout);
        cdiPanelLayout.setHorizontalGroup(
            cdiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cdiPanelLayout.createSequentialGroup()
                .addGap(155, 155, 155)
                .addComponent(enableCDICheckBox)
                .addContainerGap(179, Short.MAX_VALUE))
        );
        cdiPanelLayout.setVerticalGroup(
            cdiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cdiPanelLayout.createSequentialGroup()
                .addGap(89, 89, 89)
                .addComponent(enableCDICheckBox)
                .addContainerGap(153, Short.MAX_VALUE))
        );

        confTabbedPane.addTab(" CDI ", cdiPanel);

        org.openide.awt.Mnemonics.setLocalizedText(enableHttpsCheckBox, "Enable HTTPS"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(httpsPortLabel, "Https Port"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(httpsTimeoutLabel, "Https Timeout"); // NOI18N

        httpsPort_Spinner.setEditor(new javax.swing.JSpinner.NumberEditor(httpsPort_Spinner, "#"));

        httpsTimeoutFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("######"))));
        httpsTimeoutFormattedTextField.setFocusLostBehavior(javax.swing.JFormattedTextField.PERSIST);

        javax.swing.GroupLayout httpsPanelLayout = new javax.swing.GroupLayout(httpsPanel);
        httpsPanel.setLayout(httpsPanelLayout);
        httpsPanelLayout.setHorizontalGroup(
            httpsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(httpsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(httpsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(enableHttpsCheckBox)
                    .addGroup(httpsPanelLayout.createSequentialGroup()
                        .addGroup(httpsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(httpsPortLabel)
                            .addComponent(httpsTimeoutLabel))
                        .addGap(40, 40, 40)
                        .addGroup(httpsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(httpsPort_Spinner, javax.swing.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
                            .addComponent(httpsTimeoutFormattedTextField))))
                .addContainerGap(358, Short.MAX_VALUE))
        );
        httpsPanelLayout.setVerticalGroup(
            httpsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(httpsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(enableHttpsCheckBox)
                .addGap(45, 45, 45)
                .addGroup(httpsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(httpsPortLabel)
                    .addComponent(httpsPort_Spinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(24, 24, 24)
                .addGroup(httpsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(httpsTimeoutLabel)
                    .addComponent(httpsTimeoutFormattedTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(109, Short.MAX_VALUE))
        );

        confTabbedPane.addTab("HTTPS", httpsPanel);

        org.openide.awt.Mnemonics.setLocalizedText(sslPortLabel, "Jetty Secure Port"); // NOI18N

        sslPort_Spinner.setEditor(new javax.swing.JSpinner.NumberEditor(sslPort_Spinner, "#"));
        sslPort_Spinner.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(keystoreLabel, "Jetty Keystore\n"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(truststoreLabel, "Jetty Truststore"); // NOI18N

        keystoreTextField.setEnabled(false);

        truststoreTextField.setEnabled(false);

        javax.swing.GroupLayout sslPanelLayout = new javax.swing.GroupLayout(sslPanel);
        sslPanel.setLayout(sslPanelLayout);
        sslPanelLayout.setHorizontalGroup(
            sslPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sslPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(sslPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sslPortLabel)
                    .addComponent(keystoreLabel)
                    .addComponent(truststoreLabel))
                .addGap(43, 43, 43)
                .addGroup(sslPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(truststoreTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 341, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sslPort_Spinner, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(keystoreTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 341, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(107, Short.MAX_VALUE))
        );
        sslPanelLayout.setVerticalGroup(
            sslPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sslPanelLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(sslPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sslPortLabel)
                    .addComponent(sslPort_Spinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(sslPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(keystoreLabel)
                    .addComponent(keystoreTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(16, 16, 16)
                .addGroup(sslPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(truststoreLabel)
                    .addComponent(truststoreTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(139, Short.MAX_VALUE))
        );

        confTabbedPane.addTab("SSL", sslPanel);

        org.openide.awt.Mnemonics.setLocalizedText(enableSPDYCheckBox, "Enable SPDY"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(spdyPortLabel, "Spdy Port"); // NOI18N

        spdyPort_Spinner.setEditor(new javax.swing.JSpinner.NumberEditor(spdyPort_Spinner, "#"));

        org.openide.awt.Mnemonics.setLocalizedText(spdyTimeoutLabel, "Spdy Timeout"); // NOI18N

        spdyTimeoutFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("######"))));
        spdyTimeoutFormattedTextField.setFocusLostBehavior(javax.swing.JFormattedTextField.PERSIST);

        javax.swing.GroupLayout spdyPanelLayout = new javax.swing.GroupLayout(spdyPanel);
        spdyPanel.setLayout(spdyPanelLayout);
        spdyPanelLayout.setHorizontalGroup(
            spdyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(spdyPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(spdyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(enableSPDYCheckBox)
                    .addGroup(spdyPanelLayout.createSequentialGroup()
                        .addComponent(spdyPortLabel)
                        .addGap(49, 49, 49)
                        .addComponent(spdyPort_Spinner, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(spdyPanelLayout.createSequentialGroup()
                        .addComponent(spdyTimeoutLabel)
                        .addGap(18, 18, 18)
                        .addComponent(spdyTimeoutFormattedTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(381, Short.MAX_VALUE))
        );
        spdyPanelLayout.setVerticalGroup(
            spdyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(spdyPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(enableSPDYCheckBox)
                .addGap(29, 29, 29)
                .addGroup(spdyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(spdyPortLabel)
                    .addComponent(spdyPort_Spinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(23, 23, 23)
                .addGroup(spdyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(spdyTimeoutLabel)
                    .addComponent(spdyTimeoutFormattedTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(126, Short.MAX_VALUE))
        );

        confTabbedPane.addTab("SPDY", spdyPanel);

        org.openide.awt.Mnemonics.setLocalizedText(jettyHomeLabel, "Jetty Home:"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jettyHomeBrowseButton, "Browse..."); // NOI18N
        jettyHomeBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jettyHomeBrowseButtonActionPerformed(evt);
            }
        });

        horSeparator.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        messageLabel.setForeground(javax.swing.UIManager.getDefaults().getColor("nb.errorForeground"));
        messageLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/jeeserver/jetty/resources/error_16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(messageLabel, "message"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(saveButton, "Save changes"); // NOI18N
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addComponent(saveButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(messageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 488, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(messageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(saveButton))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(horSeparator)
                    .addComponent(confTabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(projectLocationLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(createdFolderLabel)
                                .addComponent(projectNameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(jettyHomeLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(projectNameTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(projectLocationTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(createdFolderTextField, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jettyHomeTextField, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(browseButton)
                            .addComponent(jettyHomeBrowseButton)))
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(projectNameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(projectNameTextField))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(projectLocationLabel)
                    .addComponent(projectLocationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(createdFolderLabel)
                    .addComponent(createdFolderTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(21, 21, 21)
                .addComponent(horSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(21, 21, 21)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jettyHomeLabel)
                    .addComponent(jettyHomeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jettyHomeBrowseButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(confTabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 305, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        JFileChooser chooser = new JFileChooser();
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
        panel.fireChangeEvent();

    }//GEN-LAST:event_browseButtonActionPerformed

    private void jettyHomeBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jettyHomeBrowseButtonActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Jetty Home Location");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        String path = this.jettyHomeTextField.getText();
        if (path.length() > 0) {
            File f = new File(path);
            if (f.exists()) {
                chooser.setSelectedFile(f);
            }
        }
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File homeDir = chooser.getSelectedFile();
            jettyHomeTextField.setText(FileUtil.normalizeFile(homeDir).getAbsolutePath());
        }
        panel.fireChangeEvent();
    }//GEN-LAST:event_jettyHomeBrowseButtonActionPerformed

    protected void saveChanges() {
    }

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed

        saveChanges();
    }//GEN-LAST:event_saveButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JPanel cdiPanel;
    private javax.swing.JTabbedPane confTabbedPane;
    private javax.swing.JPanel connectionPanel;
    private javax.swing.JLabel createdFolderLabel;
    private javax.swing.JTextField createdFolderTextField;
    private javax.swing.JCheckBox enableCDICheckBox;
    private javax.swing.JCheckBox enableHttpsCheckBox;
    private javax.swing.JCheckBox enableJsfCheckBox;
    private javax.swing.JCheckBox enableSPDYCheckBox;
    private javax.swing.JSeparator horSeparator;
    private javax.swing.JLabel hostLabel;
    private javax.swing.JTextField hostTextField;
    private javax.swing.JFormattedTextField httpTimeoutFormattedTextField;
    private javax.swing.JLabel httpTimeoutLabel;
    private javax.swing.JPanel httpsPanel;
    private javax.swing.JLabel httpsPortLabel;
    private javax.swing.JSpinner httpsPort_Spinner;
    private javax.swing.JFormattedTextField httpsTimeoutFormattedTextField;
    private javax.swing.JLabel httpsTimeoutLabel;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JButton jettyHomeBrowseButton;
    private javax.swing.JLabel jettyHomeLabel;
    private javax.swing.JTextField jettyHomeTextField;
    private javax.swing.JPanel jsfPanel;
    private javax.swing.JLabel keystoreLabel;
    private javax.swing.JTextField keystoreTextField;
    private javax.swing.JLabel messageLabel;
    private javax.swing.JLabel noteLabel;
    private javax.swing.JLabel projectLocationLabel;
    private javax.swing.JTextField projectLocationTextField;
    private javax.swing.JLabel projectNameLabel;
    private javax.swing.JTextField projectNameTextField;
    private javax.swing.JButton saveButton;
    private javax.swing.JLabel serverDebugPortLabel;
    private javax.swing.JSpinner serverDebugPort_Spinner;
    private javax.swing.JLabel serverPortLabel;
    private javax.swing.JSpinner serverPort_Spinner;
    private javax.swing.JLabel serverStopPortLabel;
    private javax.swing.JSpinner serverStopPort_Spinner;
    private javax.swing.JPanel spdyPanel;
    private javax.swing.JLabel spdyPortLabel;
    private javax.swing.JSpinner spdyPort_Spinner;
    private javax.swing.JFormattedTextField spdyTimeoutFormattedTextField;
    private javax.swing.JLabel spdyTimeoutLabel;
    private javax.swing.JPanel sslPanel;
    private javax.swing.JLabel sslPortLabel;
    private javax.swing.JSpinner sslPort_Spinner;
    private javax.swing.JLabel truststoreLabel;
    private javax.swing.JTextField truststoreTextField;
    // End of variables declaration//GEN-END:variables

    @Override
    public void addNotify() {
        super.addNotify();
        //same problem as in 31086, initial focus on Cancel button
        projectNameTextField.requestFocus();
    }

    boolean valid(WizardDescriptor wizardDescriptor) {
        if (!customizing) {
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
        }

        if (jettyHomeTextField.getText().length() == 0) {
            // TODO if using org.openide.dialogs >= 7.8, can use WizardDescriptor.PROP_ERROR_MESSAGE:
            wizardDescriptor.putProperty("WizardPanel_errorMessage",
                    "Jetty Home cannot be empty.");
            wizardDescriptor.getNotificationLineSupport().setErrorMessage("!!! Jetty Home cannot be empty.");
            return false; // Display name not specified
        }

        File f = FileUtil.normalizeFile(new File(jettyHomeTextField.getText()).getAbsoluteFile());
        FileObject fo = FileUtil.toFileObject(f);

        if (fo == null || !fo.isFolder() || fo.getFileObject("bin") == null || fo.getFileObject("lib") == null) {
            String message = "Invalid jetty.home path.";
            wizardDescriptor.putProperty("WizardPanel_errorMessage", message);
            return false;
        }

        try {
            httpTimeoutFormattedTextField.commitEdit();
        } catch (ParseException ex) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage",
                    getMessage("MSG_INVALID_HTTP_TIMEOUT"));
            // "Http Timeout must be an integer number.");
            return false; // Display name not specified
        }
        int timeout = Integer.parseInt(httpTimeoutFormattedTextField.getText());
        if (timeout < 0) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage",
                    getMessage("MSG_INVALID_HTTP_TIMEOUT"));
            // "Http Timeout must be an integer number.");
            return false; // Display name not specified
        }

        try {
            httpsTimeoutFormattedTextField.commitEdit();
        } catch (ParseException ex) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage",
                    getMessage("MSG_INVALID_HTTPS_TIMEOUT"));
            // "Http Timeout must be an integer number.");
            return false; // Display name not specified
        }
        timeout = Integer.parseInt(httpsTimeoutFormattedTextField.getText());
        if (timeout < 0) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage",
                    getMessage("MSG_INVALID_HTTPS_TIMEOUT"));
            // "Http Timeout must be an integer number.");
            return false; // Display name not specified
        }

        try {
            spdyTimeoutFormattedTextField.commitEdit();
        } catch (ParseException ex) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage",
                    getMessage("MSG_INVALID_SPDY_TIMEOUT"));
            // "Http Timeout must be an integer number.");
            return false; // Display name not specified
        }
        timeout = Integer.parseInt(spdyTimeoutFormattedTextField.getText());
        if (timeout < 0) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage",
                    getMessage("MSG_INVALID_SPDY_TIMEOUT"));
            return false; // Display name not specified
        }

        int port;
        int debugPort;
        int stopPort;
        int httpsPort;
        int spdyPort;

        try {
            port = Integer.parseInt(getPort(serverPort_Spinner));
        } catch (NumberFormatException ex) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage",
                    getMessage("MSG_INVALID_PORT"));
            return false;
        }
        if (port <= 0) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage",
                    getMessage("MSG_INVALID_PORT"));
            return false;
        }

        try {
            debugPort = Integer.parseInt(getPort(serverDebugPort_Spinner));
        } catch (NumberFormatException ex) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage",
                    getMessage("MSG_INVALID_DEBUG_PORT"));
            return false;
        }
        if (debugPort <= 0) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage",
                    getMessage("MSG_INVALID_DEBUG_PORT"));
            return false;
        }

        try {
            stopPort = Integer.parseInt(getPort(serverStopPort_Spinner));
        } catch (NumberFormatException ex) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage",
                    getMessage("MSG_INVALID_STOP_PORT"));
            return false;
        }
        if (stopPort <= 0) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage",
                    getMessage("MSG_INVALID_STOP_PORT"));
            return false;
        }

        try {
            httpsPort = Integer.parseInt(getPort(httpsPort_Spinner));
        } catch (NumberFormatException ex) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage",
                    getMessage("MSG_INVALID_HTTPS_PORT"));
            return false;
        }
        if (httpsPort <= 0) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage",
                    getMessage("MSG_INVALID_HTTPS_PORT"));
            return false;
        }
        try {
            spdyPort = Integer.parseInt(getPort(spdyPort_Spinner));
        } catch (NumberFormatException ex) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage",
                    getMessage("MSG_INVALID_SPDY_PORT"));
            return false;
        }
        if (spdyPort <= 0) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage",
                    getMessage("MSG_INVALID_SPDY_PORT"));
            return false;
        }

        if (port == debugPort) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage",
                    getMessage("MSG_SAME_HTTP_AND_DEBUG_PORT", String.valueOf(port)));
            return false;
        }
        if (port == stopPort) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage",
                    getMessage("MSG_SAME_HTTP_AND_STOP_PORT", String.valueOf(port)));
            return false;
        }
        if (debugPort == stopPort) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage",
                    getMessage("MSG_SAME_DEBUG_AND_STOP_PORT", String.valueOf(debugPort)));
            return false;
        }

        if (BaseUtil.isPortBusy(port, getManager())) {
            wizardDescriptor.putProperty("WizardPanel_warningMessage",
                    getMessage("MSG_HTTP_PORT_IN_USE", String.valueOf(port)));
            return true;
        }

        if (BaseUtil.isPortBusy(debugPort, getManager())) {
            wizardDescriptor.putProperty("WizardPanel_warningMessage",
                    getMessage("MSG_DEBUG_PORT_IN_USE", String.valueOf(debugPort)));
            return true;
        }
        if (BaseUtil.isPortBusy(stopPort, getManager())) {
            wizardDescriptor.putProperty("WizardPanel_warningMessage",
                    getMessage("MSG_STOP_PORT_IN_USE", String.valueOf(stopPort)));
            return true;
        }

        return true;
    }

    boolean isHomeDirValid() {
        if (jettyHomeTextField.getText().length() == 0) {
            return false; // Display name not specified
        }

        File f = FileUtil.normalizeFile(new File(jettyHomeTextField.getText()).getAbsoluteFile());
        FileObject fo = FileUtil.toFileObject(f);
        return fo != null && fo.isFolder() && fo.getFileObject("bin") != null && fo.getFileObject("lib") != null;
    }

    protected BaseDeploymentManager getManager() {
        return null;
    }

    String getMessage(String bundleKey, String value) {
        return NbBundle.getMessage(JettyServerInstancePanelVisual.class, bundleKey, value);
    }

    String getMessage(String bundleKey) {
        return NbBundle.getMessage(JettyServerInstancePanelVisual.class, bundleKey);
    }

    public void store(WizardDescriptor d) {
        String projectName = projectNameTextField.getText().trim();
        String folder = createdFolderTextField.getText().trim();

        String timeout = httpTimeoutFormattedTextField.getValue().toString();
        d.putProperty(PROP_HTTP_TIMEOUT, timeout);
        timeout = httpsTimeoutFormattedTextField.getValue().toString();
        d.putProperty(PROP_HTTPS_TIMEOUT, timeout);

        timeout = spdyTimeoutFormattedTextField.getValue().toString();
        d.putProperty(PROP_SPDY_TIMEOUT, timeout);

        d.putProperty("projdir", new File(folder));
        d.putProperty("projectName", projectName);

        d.putProperty(BaseConstants.HTTP_PORT_PROP, getPort());
        d.putProperty(BaseConstants.DEBUG_PORT_PROP, getDebugPort());
        d.putProperty(BaseConstants.SHUTDOWN_PORT_PROP, getStopPort());
        d.putProperty(PROP_HTTPS_PORT, getHttpsPort());
        d.putProperty(PROP_SPDY_PORT, getSPDYPort());

        String port = getSslPort();
        if (isHttpsEnabled()) {
            port = getHttpsPort();
        }
        if (isSPDYEnabled()) {
            port = getSPDYPort();
        }

        d.putProperty(JettyConstants.JETTY_SECURE_PORT_PROP, port);

        d.putProperty(BaseConstants.HOST_PROP, getHost());

        d.putProperty(BaseConstants.HOME_DIR_PROP, getJettyHome());

        d.putProperty(BaseConstants.SERVER_ID_PROP, getServerId());

        d.putProperty(PROP_ENABLE_JSF, String.valueOf(isJSFEnabled()));
        d.putProperty(PROP_ENABLE_CDI, String.valueOf(isCDIEnabled()));

        d.putProperty(PROP_ENABLE_SPDY, String.valueOf(isSPDYEnabled()));
        d.putProperty(PROP_ENABLE_HTTPS, String.valueOf(isHttpsEnabled()));

    }

    ServerSpecifics getSpecifics() {
        return Utils.getServerSpecifics(getServerId());
    }

    public void read(WizardDescriptor settings) {
        File projectLocation = (File) settings.getProperty("projdir");
        if (projectLocation == null || projectLocation.getParentFile() == null || !projectLocation.getParentFile().isDirectory()) {
            projectLocation = ProjectChooser.getProjectsFolder();
        } else {
            projectLocation = projectLocation.getParentFile();
        }
        this.projectLocationTextField.setText(projectLocation.getAbsolutePath());

        String jettyHome = (String) settings.getProperty(PROP_JETTY_HOME);
        if (jettyHome == null || jettyHome.trim().isEmpty()) {
            String[] uris = Utils.getServerInstanceIDs(getServerId());
            if (uris.length > 0) {
                jettyHome = InstanceProperties.getInstanceProperties(uris[0]).getProperty(BaseConstants.HOME_DIR_PROP);
            }
        }
        this.jettyHomeTextField.setText(jettyHome);

        String projectName = (String) settings.getProperty("projectName");
        if (projectName == null) {
            projectName = "JettyServerInstance";
        }

        this.projectNameTextField.setText(projectName);

        String timeout = (String) settings.getProperty(PROP_HTTP_TIMEOUT);
        if (timeout == null || timeout.trim().isEmpty()) {
            timeout = "30000";
        }
        httpTimeoutFormattedTextField.setText(timeout);

        timeout = (String) settings.getProperty(PROP_HTTPS_TIMEOUT);
        if (timeout == null || timeout.trim().isEmpty()) {
            timeout = "30000";
        }
        httpsTimeoutFormattedTextField.setText(timeout);

        timeout = (String) settings.getProperty(PROP_SPDY_TIMEOUT);
        if (timeout == null || timeout.trim().isEmpty()) {
            timeout = "30000";
        }
        spdyTimeoutFormattedTextField.setText(timeout);

        hostTextField.setText("localhost");
        this.projectNameTextField.selectAll();
        readDefaultPortSettings(settings);

        String prop = (String) settings.getProperty(PROP_ENABLE_JSF);
        if (prop == null || !Boolean.parseBoolean(prop)) {
            this.enableJsfCheckBox.setSelected(false);
        } else {
            this.enableJsfCheckBox.setSelected(true);
        }

        prop = (String) settings.getProperty(PROP_ENABLE_CDI);
        if (prop == null || !Boolean.parseBoolean(prop)) {
            this.enableCDICheckBox.setSelected(false);
        } else {
            this.enableCDICheckBox.setSelected(true);
        }

        prop = (String) settings.getProperty(PROP_ENABLE_SPDY);
        if (prop == null || !Boolean.parseBoolean(prop)) {
            this.enableSPDYCheckBox.setSelected(false);
            enableSPDYFields(false);
        } else {
            this.enableSPDYCheckBox.setSelected(true);
            this.enableHttpsCheckBox.setSelected(false);
            enableSPDYFields(true);
        }

        prop = (String) settings.getProperty(PROP_ENABLE_HTTPS);
        if (prop == null || !Boolean.parseBoolean(prop)) {
            this.enableHttpsCheckBox.setSelected(false);
            enableHttpsFields(false);
        } else {
            this.enableHttpsCheckBox.setSelected(true);
            this.enableSPDYCheckBox.setSelected(false);
            enableHttpsFields(true);
        }

        prop = (String) settings.getProperty(JettyConstants.JETTY_KEYSTORE_PROP);
        keystoreTextField.setText(prop);
        prop = (String) settings.getProperty(JettyConstants.JETTY_TRUSTSTORE_PROP);
        truststoreTextField.setText(prop);
    }

    void readDefaultPortSettings(WizardDescriptor settings) {
        //
        // ----- http port ------
        //
        String port = (String) settings.getProperty(BaseConstants.HTTP_PORT_PROP);
        if (port == null) {
            port = String.valueOf(getSpecifics().getDefaultPort());
        }
        serverPort_Spinner.setValue(Integer.parseInt(port));
        //
        // ----- debug port ------
        //
        port = (String) settings.getProperty(BaseConstants.DEBUG_PORT_PROP);
        if (port == null) {
            port = String.valueOf(getSpecifics().getDefaultDebugPort());
        }
        serverDebugPort_Spinner.setValue(Integer.parseInt(port));
        //
        // ----- stop port ------
        //
        port = (String) settings.getProperty(BaseConstants.SHUTDOWN_PORT_PROP);
        if (port == null) {
            port = String.valueOf(getSpecifics().getDefaultShutdownPort());
        }
        serverStopPort_Spinner.setValue(Integer.parseInt(port));

        //
        // ----- https port ------
        //
        port = (String) settings.getProperty(PROP_HTTPS_PORT);
        if (port == null) {
            port = "8443";
        }
        httpsPort_Spinner.setValue(Integer.parseInt(port));

        port = (String) settings.getProperty(PROP_SPDY_PORT);
        if (port == null) {
            port = "8443";
        }
        spdyPort_Spinner.setValue(Integer.parseInt(port));
        //
        // ----- ssl secure port ------
        //
        port = (String) settings.getProperty(JettyConstants.JETTY_SECURE_PORT_PROP);
        if (port == null) {
            port = "8443";
        }
        sslPort_Spinner.setValue(Integer.parseInt(port));

        addPortListeners();
    }

    void validate(WizardDescriptor d) throws WizardValidationException {
        // nothing to validate
    }

    // Implementation of DocumentListener --------------------------------------
    @Override
    public void changedUpdate(DocumentEvent e) {
        commonUpdate(e);
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        commonUpdate(e);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        commonUpdate(e);
    }

    protected void commonUpdate(DocumentEvent e) {
        updateTexts(e);
        if (this.projectNameTextField.getDocument() == e.getDocument()) {
            firePropertyChange(PROP_PROJECT_NAME, null, this.projectNameTextField.getText());
        } else if (jettyHomeTextField.getDocument() == e.getDocument()) {
            firePropertyChange(PROP_JETTY_HOME, null, this.jettyHomeTextField.getText());
        } else if (httpTimeoutFormattedTextField.getDocument() == e.getDocument()) {
            firePropertyChange(PROP_HTTP_TIMEOUT, null, this.httpTimeoutFormattedTextField.getText());
        } else if (httpsTimeoutFormattedTextField.getDocument() == e.getDocument()) {
            firePropertyChange(PROP_HTTPS_TIMEOUT, null, this.httpsTimeoutFormattedTextField.getText());
        } else if (spdyTimeoutFormattedTextField.getDocument() == e.getDocument()) {
            firePropertyChange(PROP_SPDY_TIMEOUT, null, this.spdyTimeoutFormattedTextField.getText());
        }

    }

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
        if (panel != null) {
            panel.fireChangeEvent(); // Notify that the panel changed
        }
    }

    protected class PortHandler implements DocumentListener {

        private final JSpinner spinner;

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
            final JFormattedTextField tf = me.getTextField();

            //JSpinner.NumberEditor messl = (JSpinner.NumberEditor) sslPort_Spinner.getEditor();
            //JFormattedTextField tfssl = messl.getTextField();
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    panel.fireChangeEvent();
                    try {
                        if (spinner == httpsPort_Spinner || spinner == spdyPort_Spinner) {
                            sslPort_Spinner.setValue(Integer.parseInt(tf.getText()));
                        }
                    } catch (NumberFormatException ex) {

                    }

                }
            });
            if (spinner == serverPort_Spinner) {
                JettyServerInstancePanelVisual.this.firePropertyChange(PROP_PORT, null, tf.getText());
            } else if (spinner == serverDebugPort_Spinner) {
                JettyServerInstancePanelVisual.this.firePropertyChange(PROP_DEBUG_PORT, null, tf.getText());
            } else if (spinner == serverStopPort_Spinner) {
                JettyServerInstancePanelVisual.this.firePropertyChange(PROP_STOP_PORT, null, tf.getText());
            } else if (spinner == httpsPort_Spinner) {
                JettyServerInstancePanelVisual.this.firePropertyChange(PROP_HTTPS_PORT, null, tf.getText());
                //JettyServerInstancePanelVisual.this.firePropertyChange(JettyConstants.JETTY_SECURE_PORT_PROP, null, tfssl.getText());

            } else if (spinner == spdyPort_Spinner) {
                JettyServerInstancePanelVisual.this.firePropertyChange(PROP_SPDY_PORT, null, tf.getText());
                //JettyServerInstancePanelVisual.this.firePropertyChange(JettyConstants.JETTY_SECURE_PORT_PROP, null, tfssl.getText());
            }

        }
    }//class

    protected class CheckBoxActionHandler implements ActionListener {

        private final JCheckBox box;

        public CheckBoxActionHandler(JCheckBox box) {
            this.box = box;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            panel.fireChangeEvent();
            if (box == enableSPDYCheckBox && enableSPDYCheckBox.isSelected()) {
                enableHttpsCheckBox.setSelected(false);
                enableSPDYFields(true);
                sslPort_Spinner.setValue(Integer.parseInt(getSPDYPort()));
            } else {
                enableSPDYFields(false);
            }

            if (box == enableHttpsCheckBox && enableHttpsCheckBox.isSelected()) {
                enableSPDYCheckBox.setSelected(false);
                enableHttpsFields(true);
                sslPort_Spinner.setValue(Integer.parseInt(getHttpsPort()));
            } else {
                enableHttpsFields(false);
            }
        }

    }
}
