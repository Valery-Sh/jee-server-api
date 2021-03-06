package org.netbeans.modules.jeeserver.base.embedded.project.wizard;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.ListModel;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.netbeans.modules.jeeserver.base.embedded.apisupport.SupportedApi;
import org.netbeans.modules.jeeserver.base.embedded.apisupport.SupportedApiProvider;
import org.netbeans.modules.jeeserver.base.embedded.project.SuiteManager;
import org.netbeans.modules.jeeserver.base.embedded.utils.SuiteUtil;

/**
 *
 * @author Valery
 */
public class AddDependenciesPanelVisual extends javax.swing.JPanel {

    private final Project serverProject;
    private final JButton downloadButton;
    private final JButton cancelButton;
    private String[] apiNames = null;
    private List<SupportedApi> apiList;

    /**
     * Creates new form DownloadJarsPanelVisual
     */
    public AddDependenciesPanelVisual(Project serverProject, JButton downloadButton, JButton cancelButton) {

        initComponents();
        this.serverProject = serverProject;
        this.downloadButton = downloadButton;
        this.cancelButton = cancelButton;
        init();
    }

    private void init() {
//DeploymentFactoryManager.getInstance().        
        this.selectAPIComboBox.setModel(createComboBoxModel());
        this.selectAPIComboBox.addActionListener(new ComboBoxActionListener());
        this.selectAPIComboBox.setSelectedIndex(0);
        this.jarList.setModel(createListModel(null));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        selectAPIComboBox = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jarList = new javax.swing.JList();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        descTextArea = new javax.swing.JTextArea();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, "Select API to Create Dependencies for: "); // NOI18N

        selectAPIComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, "Jar achives to create dependencies for:"); // NOI18N

        jarList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(jarList);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, "Description:"); // NOI18N

        descTextArea.setEditable(false);
        descTextArea.setColumns(20);
        descTextArea.setLineWrap(true);
        descTextArea.setRows(5);
        descTextArea.setWrapStyleWord(true);
        descTextArea.setOpaque(false);
        jScrollPane2.setViewportView(descTextArea);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addComponent(selectAPIComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 493, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(selectAPIComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(32, 32, 32)
                .addComponent(jLabel3)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(32, 32, 32)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(55, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    protected DefaultComboBoxModel createComboBoxModel() {
        apiList = SupportedApiProvider.getInstance(SuiteUtil.getActualServerId(serverProject)).getApiList();
        final List<String> names = new ArrayList<>();
        names.add("<not selected>");
        apiList.forEach(api -> {
            names.add(api.getDisplayName());
            BaseUtil.out("createComboBoxModel apiList displayName = " + api.getDisplayName());
        });

        return new DefaultComboBoxModel(apiNames = names.toArray(new String[names.size()]));
    }

    public List<SupportedApi> getApiList() {
        return apiList;
    }

    public void setApiList(List<SupportedApi> apiList) {
        this.apiList = apiList;
    }

    protected ListModel createListModel(SupportedApi api) {
        final DefaultListModel model = new DefaultListModel();
        if (api == null) {
            return model;
        }
        String serverVersion = SuiteManager
                .getManager(getServerProject())
                .getInstanceProperties()
                .getProperty(BaseConstants.SERVER_VERSION_PROP);
        String version = serverVersion == null ? null : serverVersion;
        api.getJarNames().forEach(jar -> {
            jar = jar.replace("${nb.server.version}", version);
            model.addElement(jar);
        });
        return model;
    }

    public JComboBox getSelectedApiComboBox() {
        return selectAPIComboBox;
    }

    protected class ComboBoxActionListener implements ActionListener {

        public ComboBoxActionListener() {

        }

        @Override
        public void actionPerformed(ActionEvent e) {
            descTextArea.setText("");
            int idx = selectAPIComboBox.getSelectedIndex();
            if (idx == 0) {
                jarList.setModel(createListModel(null));
                downloadButton.setEnabled(false);
            } else {
                jarList.setModel(createListModel(apiList.get(idx - 1)));
                descTextArea.setText(apiList.get(idx - 1).getDescription());
                downloadButton.setEnabled(true);
            }

        }

    }

    public Project getServerProject() {
        return serverProject;
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea descTextArea;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JList jarList;
    private javax.swing.JComboBox selectAPIComboBox;
    // End of variables declaration//GEN-END:variables
}
