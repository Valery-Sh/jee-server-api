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
package org.netbeans.modules.jeeserver.jetty.customizer;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;

import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.netbeans.modules.jeeserver.jetty.project.template.JettyServerInstancePanelVisual;
import org.netbeans.modules.jeeserver.jetty.project.template.JettyServerInstanceWizardPanel;
import org.netbeans.modules.jeeserver.jetty.project.template.JettyProperties;
import org.netbeans.modules.jeeserver.jetty.util.HttpIni;
import org.netbeans.modules.jeeserver.jetty.util.HttpsIni;
import org.netbeans.modules.jeeserver.jetty.util.JettyConstants;
import org.netbeans.modules.jeeserver.jetty.util.SpdyIni;
import org.netbeans.modules.jeeserver.jetty.util.SslIni;
import org.netbeans.modules.jeeserver.jetty.util.StartdIniHelper;
import org.netbeans.modules.jeeserver.jetty.util.Utils;
import static org.netbeans.modules.jeeserver.jetty.util.Utils.propertiesOf;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.EditableProperties;

/**
 *
 * @author V.Shyshkin
 */
public class JettyServerCustomizer extends JettyServerInstancePanelVisual implements ChangeListener {

    private static final Logger LOG = Logger.getLogger(JettyServerCustomizer.class.getName());

    protected BaseDeploymentManager manager;
    protected WizardDescriptor wiz;
    protected String title;

    public JettyServerCustomizer(BaseDeploymentManager manager) {
        this(null, manager);
    }

    protected JettyServerCustomizer(JettyServerInstanceWizardPanel wizardPanel, BaseDeploymentManager manager) {
        super(wizardPanel);
        this.manager = manager;
        panel = new JettyServerInstanceWizardPanel(this);
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>();
        panels.add(panel);
        wiz = new WizardDescriptor(new WizardDescriptor.ArrayIterator<>(panels));

        wiz.setButtonListener(new ButtonListener(this));
        InstanceProperties ip = InstanceProperties.getInstanceProperties(manager.getUri());

        wiz.putProperty("projectName", manager.getServerProject().getProjectDirectory().getNameExt());
        wiz.putProperty(BaseConstants.HOME_DIR_PROP, ip.getProperty(BaseConstants.HOME_DIR_PROP));
        wiz.putProperty(BaseConstants.HOST_PROP, ip.getProperty(BaseConstants.HOST_PROP));
        wiz.putProperty(BaseConstants.HTTP_PORT_PROP, ip.getProperty(BaseConstants.HTTP_PORT_PROP));
        wiz.putProperty(BaseConstants.DEBUG_PORT_PROP, ip.getProperty(BaseConstants.DEBUG_PORT_PROP));
        wiz.putProperty(BaseConstants.SHUTDOWN_PORT_PROP, ip.getProperty(BaseConstants.SHUTDOWN_PORT_PROP));
        wiz.putProperty(BaseConstants.SERVER_ID_PROP, ip.getProperty(BaseConstants.SERVER_ID_PROP));
        wiz.putProperty("projdir", new File(BaseUtil.getServerLocation(ip)));

        FileObject fo = manager.getServerProject().getProjectDirectory().getFileObject(JettyConstants.JETTY_HTTP_INI);

        if (fo != null) {
            HttpIni httpIni = new HttpIni(FileUtil.toFile(fo));
            //wiz.putProperty(JettyConstants.JETTY_HTTP_TIMEOUT, httpIni.getHttpTimeout());
            wiz.putProperty(PROP_HTTP_TIMEOUT, httpIni.getHttpTimeout());
        }
        fo = manager.getServerProject().getProjectDirectory().getFileObject(JettyConstants.JETTY_START_D);
        String enabled;
        if (fo.getFileObject("jsf.ini") != null) {
            enabled = "true";
        } else {
            enabled = "false";
        }
        wiz.putProperty(JettyConstants.ENABLE_JSF, enabled);

        if (fo.getFileObject("cdi.ini") != null) {
            enabled = "true";
        } else {
            enabled = "false";
        }

        wiz.putProperty(JettyConstants.ENABLE_CDI, enabled);

        if (fo.getFileObject("spdy.ini") != null) {
            enabled = "true";
        } else {
            enabled = "false";
        }
        wiz.putProperty(JettyConstants.ENABLE_SPDY, enabled);

        if (fo.getFileObject("https.ini") != null) {
            enabled = "true";
        } else {
            enabled = "false";
        }
        wiz.putProperty(JettyConstants.ENABLE_HTTPS, enabled);
        // if https.ini doesn't exist then default values will be set
        storeHttpsIniProperties(fo.getFileObject("https.ini"));
        storeSPDYIniProperties(fo.getFileObject("spdy.ini"));
        storeSslIniProperties(fo.getFileObject("ssl.ini"));
        init();
    }

    private void init() {
        panel.readSettings(wiz);
        panel.addChangeListener(this);
        stateChanged(null);
    }

    private void storeHttpsIniProperties(FileObject iniFo) {
        String port = null;
        String timeout = null;

        if (iniFo != null) {
            HttpsIni ini = new HttpsIni(FileUtil.toFile(iniFo));
            port = ini.getHttpsPort();
            timeout = ini.getHttpsTimeout();
        }
        if (port == null) {
            port = "8443";
        }
        if (timeout == null) {
            timeout = "30000";
        }

        wiz.putProperty(JettyConstants.JETTY_HTTPS_PORT, port);
        wiz.putProperty(JettyConstants.JETTY_HTTPS_TIMEOUT, timeout);
        if (isIniEnabled("https")) {
            wiz.putProperty(JettyConstants.JETTY_SECURE_PORT_PROP, port);

        }
    }

    private void storeSslIniProperties(FileObject iniFo) {
        if (iniFo == null) {
            return;
        }

        SslIni ini = new SslIni(FileUtil.toFile(iniFo));
        String keystore = ini.getKeystore();
        String truststore = ini.getTruststore();
        if (keystore != null) {
            wiz.putProperty(JettyConstants.JETTY_KEYSTORE_PROP, keystore);
        }
        if (truststore != null) {
            wiz.putProperty(JettyConstants.JETTY_TRUSTSTORE_PROP, truststore);
        }
    }

    private void storeSPDYIniProperties(FileObject iniFo) {
        String port = null;
        String timeout = null;

        if (iniFo != null) {
            SpdyIni ini = new SpdyIni(FileUtil.toFile(iniFo));
            port = ini.getSpdyPort();
            timeout = ini.getSpdyTimeout();
        }
        if (port == null) {
            port = "8443";
        }
        if (timeout == null) {
            timeout = "30000";
        }

        wiz.putProperty(JettyConstants.JETTY_SPDY_PORT, port);
        wiz.putProperty(JettyConstants.JETTY_SPDY_TIMEOUT, timeout);
        if (isIniEnabled("spdy")) {
            wiz.putProperty(JettyConstants.JETTY_SECURE_PORT_PROP, port);
        }

    }

    public WizardDescriptor getWizardDescriptor() {
        return wiz;
    }

    @Override
    protected BaseDeploymentManager getManager() {
        return manager;
    }

    @Override
    public final void stateChanged(ChangeEvent e) {
        boolean isValid = panel.isValid();
        String msg = "";
        getMessageLabel().setVisible(false);

        if (!isValid) {
            msg = (String) wiz.getProperty("WizardPanel_errorMessage");
            getMessageLabel().setForeground(Color.red);
            getMessageLabel().setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/jeeserver/jetty/resources/error_16.png"))); // NOI18N);
            getMessageLabel().setVisible(true);
        } else if (wiz.getProperty("WizardPanel_warningMessage") != null) {
            msg = (String) wiz.getProperty("WizardPanel_warningMessage");
            getMessageLabel().setForeground(Color.black);
            getMessageLabel().setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/jeeserver/jetty/resources/warning_16.png"))); // NOI18N);
            getMessageLabel().setVisible(true);
        }

        getSaveButton().setEnabled(isValid);

        wiz.putProperty("WizardPanel_errorMessage", null);
        wiz.putProperty("WizardPanel_warningMessage", null);
        getMessageLabel().setText(msg);
    }

    @Override
    public void saveChanges() {
        Utils.out("--------- CUSTOMIZER SAVE CHANGES ---------");
        store(wiz);
        InstanceProperties ip = InstanceProperties.getInstanceProperties(manager.getUri());
        String homeDir = (String) wiz.getProperty(BaseConstants.HOME_DIR_PROP);
        ip.setProperty(BaseConstants.HOME_DIR_PROP, homeDir);
//        JettyProperties jvs = JettyProperties.getInstance(wiz);

        ip.setProperty(BaseConstants.SERVER_VERSION_PROP, Utils.getJettyVersion(homeDir));

        ip.setProperty(BaseConstants.HTTP_PORT_PROP, (String) wiz.getProperty(BaseConstants.HTTP_PORT_PROP));
        ip.setProperty(BaseConstants.DEBUG_PORT_PROP, (String) wiz.getProperty(BaseConstants.DEBUG_PORT_PROP));
        ip.setProperty(BaseConstants.SHUTDOWN_PORT_PROP, (String) wiz.getProperty(BaseConstants.SHUTDOWN_PORT_PROP));

        // 
        // Change ${jetty.base}/start.d/http.ini
        //
        Properties iniProps = new Properties();
        iniProps.setProperty(BaseConstants.HTTP_PORT_PROP, (String) wiz.getProperty(BaseConstants.HTTP_PORT_PROP));
        
        iniProps.setProperty(JettyConstants.JETTY_HTTP_TIMEOUT, (String) wiz.getProperty(JettyConstants.JETTY_HTTP_TIMEOUT));
/*        FileUtil.runAtomicAction(new Runnable() {
            @Override
            public void run() {
//                updateHttpIni(manager.getServerProject().getProjectDirectory(), iniProps);
//                instantiateStartDIniFiles(manager.getServerProject().getProjectDirectory());
            }
        });
*/
        updateHttpIni(manager.getServerProject().getProjectDirectory(), iniProps);
        // 
        // Change ${jetty.base}/star.d
        //
        
        // instantiateStartDIniFiles(manager.getServerProject().getProjectDirectory());
    }

    protected boolean isIniEnabled(String iniName) {
        String p = null;
        switch (iniName) {
            case "jsf":
                p = (String) wiz.getProperty(JettyConstants.ENABLE_JSF);
                break;
            case "cdi":
                p = (String) wiz.getProperty(JettyConstants.ENABLE_CDI);
                break;
            case "spdy":
                p = (String) wiz.getProperty(JettyConstants.ENABLE_SPDY);
                break;
            case "https":
                p = (String) wiz.getProperty(JettyConstants.ENABLE_HTTPS);
                break;
            case "ssl":
                if (isIniEnabled("https") || isIniEnabled("spdy")) {
                    p = "true";
                }
                break;

        }
        return (p != null && p.equals("true"));
    }

    protected void instantiateStartDIniFiles(FileObject projDir) {
        StartdIniHelper helper = new StartdIniHelper(wiz);
        helper.instantiateStartdIni(projDir, "jsf");
        helper.instantiateStartdIni(projDir, "cdi");
        helper.instantiateStartdIni(projDir, "https");
        helper.instantiateStartdIni(projDir, "spdy");
        helper.instantiateStartdIni(projDir, "ssl");// must be after spdy and https
    }

    public static void updateHttpIni(FileObject serverProjDir, Properties iniProps) {
        Project proj = FileOwnerQuery.getOwner(serverProjDir);
        Properties p = propertiesOf(proj);
        //String jettyHome = p.getProperty(BaseConstants.HOME_DIR_PROP );
        JettyProperties jvs = JettyProperties.getInstance(proj);
        //jvs
        FileObject httpIni = serverProjDir.getFileObject(JettyConstants.JETTY_HTTP_INI);
        if (httpIni == null) {
            return;
        }
        EditableProperties props = new EditableProperties(false);

        try (FileInputStream fis = new FileInputStream(httpIni.getPath())) {
            props.load(fis);
        } catch (IOException ex) {
            LOG.log(Level.INFO, ex.getMessage());
        }
        String prop = jvs.getHttpPortPropertyName();
        props.setProperty(prop, iniProps.getProperty(BaseConstants.HTTP_PORT_PROP));

        prop = jvs.getTimeoutPropertyName();
        BaseUtil.out("jvs.getTimeoutPropertyName()=" + prop);
        BaseUtil.out("jvs.getTimeoutPropertyName()=" + prop);
        
        BaseUtil.out("iniProps.getProperty(JettyConstants.JETTY_HTTP_TIMEOUT)=" + iniProps.getProperty(JettyConstants.JETTY_HTTP_TIMEOUT));
        
        props.setProperty(prop, iniProps.getProperty(JettyConstants.JETTY_HTTP_TIMEOUT));
        try (FileOutputStream fos = new FileOutputStream(httpIni.getPath())) {
            props.store(fos);
        } catch (IOException ex) {
            LOG.log(Level.INFO, ex.getMessage());
        }
    }

    protected static class ButtonListener implements ActionListener {

        final JettyServerCustomizer customizer;

        public ButtonListener(JettyServerCustomizer c) {
            this.customizer = c;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if ("Finish".equals(e.getActionCommand())) {
                customizer.saveChanges();
            }
        }
    }
}
