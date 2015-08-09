package org.netbeans.modules.jeeserver.base.deployment.lc;

import java.util.Properties;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import static org.netbeans.modules.jeeserver.base.deployment.lc.LicenseWizard.PROP_LICENCE_ACCEPTED;
import org.openide.WizardDescriptor;

public final class LicenseVisualPanel extends JPanel implements ChangeListener {

    protected WizardDescriptor wiz;
    Properties settings;
    LicenseWizardPanel panel;

    /**
     * Creates new form LicenseVisualPanel1
     * @param panel
     */
    public LicenseVisualPanel(LicenseWizardPanel panel) {
        initComponents();
        this.panel = panel;
        // Register listener on the textFields to make the automatic updates
        addListeners();
    }
    private void addListeners() {
        this.acceptCheckBox.addChangeListener(this);
    }
    @Override
    public void stateChanged(ChangeEvent e) {
        //panel.fireChangeEvent(); // Notify that the panel changed
        wiz.putProperty(PROP_LICENCE_ACCEPTED, acceptCheckBox.isSelected());
    }

    void read(WizardDescriptor settings) {
        this.wiz = settings;
        acceptCheckBox.setSelected((boolean) wiz.getProperty(PROP_LICENCE_ACCEPTED));
    }

    void store(WizardDescriptor settings) {
        settings.putProperty(PROP_LICENCE_ACCEPTED, getAccept());
    }

    boolean getAccept() {
        return this.acceptCheckBox.isSelected();
    }
    @Override
    public String getName() {
        return "";
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextPane2 = new javax.swing.JTextPane();
        acceptCheckBox = new javax.swing.JCheckBox();

        jTextPane1.setContentType("text/html"); // NOI18N
        jTextPane1.setText("<html>\r\n  <head>\r\n\r\n  </head>\r\n  <body>\r\n    <p style=\"margin-top: 0\">\r\n<b>ALERT:</b> There are enabled module(s) with licenses      \r\n    </p>\r\n  </body>\r\n</html>\r\n"); // NOI18N
        jScrollPane1.setViewportView(jTextPane1);

        jTextPane2.setContentType("text/html"); // NOI18N
        jTextPane2.setText("<html>\r\n  <head>\r\n\r\n  </head>\r\n  <body>\r\n    <p style=\"margin-top: 0\">\r\n<br/>\nThe following 1 module(s):\n<ul>\n <li> contains software not provided by the Eclipse Foundation!</li>\n <li>contains software not covered by the Eclipse Public License!</li>\n <li> has not been audited for compliance with its license</li>\n</ul>\n</p>\nModule: <b>cdi</b>\n<ul>\n  <li>Weld is an open source project hosted on Github and released under the Apache 2.0 license.</li>\n  <li><a href=\"http://weld.cdi-spec.org/\">http://weld.cdi-spec.org/</a> </li>\n  <li><a href=\"http://www.apache.org/licenses/LICENSE-2.0.html\">http://www.apache.org/licenses/LICENSE-2.0.html</a></li>      \r\n</ul>\n    </p>\r\n  </body>\r\n</html>\r\n"); // NOI18N
        jScrollPane2.setViewportView(jTextPane2);

        org.openide.awt.Mnemonics.setLocalizedText(acceptCheckBox, "Accept and proceed"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 588, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(acceptCheckBox)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 277, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 29, Short.MAX_VALUE)
                .addComponent(acceptCheckBox)
                .addGap(23, 23, 23))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox acceptCheckBox;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JTextPane jTextPane2;
    // End of variables declaration//GEN-END:variables
}