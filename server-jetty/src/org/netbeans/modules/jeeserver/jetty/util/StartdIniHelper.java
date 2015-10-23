/**
 * This file is part of Jetty Server support in NetBeans IDE.
 *
 * Jetty Server support in NetBeans IDE is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * Jetty Server support in NetBeans IDE is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should see the GNU General Public License here:
 * <http://www.gnu.org/licenses/>.
 */
package org.netbeans.modules.jeeserver.jetty.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.jeeserver.jetty.customizer.JettyServerCustomizer;
import org.netbeans.modules.jeeserver.jetty.project.template.AbstractJettyInstanceIterator;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Helps to customize the {@code jettybase/start.d} folder content.
 * 
 * @author V. Shyshkin
 */
public class StartdIniHelper {

    private static final Logger LOG = Logger.getLogger(AbstractJettyInstanceIterator.class.getName());
    
    private final WizardDescriptor wiz;
//    private final FileObject projDir;
    
    
    public StartdIniHelper(WizardDescriptor wiz) {
        this.wiz = wiz;
    }
    
    public void instantiateStartdIni(FileObject projDir, String iniName) {
        String iniFileName = iniName + ".ini";
        File startdFile = new File(projDir.getPath() + "/" + JettyConstants.JETTY_START_D);
        FileObject startdFo = FileUtil.toFileObject(startdFile);
        FileObject iniFo = startdFo.getFileObject(iniFileName);

        boolean iniExists = iniFo != null;

        if (isIniEnabled(iniName)) {
            if (!iniExists) {
                try {
                    copyFile(startdFo, iniName);
                    if ("spdy".equals(iniName) || "https".equals(iniName)) {
                        copyFile(startdFo, "ssl");
                    }
                } catch (IOException ex) {
                    LOG.log(Level.FINE, ex.getMessage()); //NOI18N   
                }
            }
            applyStartDChanges(startdFo, iniName);
        } else {
            if (iniExists) {
                try {
                    iniFo.delete();
                    if ("spdy".equals(iniName) || "https".equals(iniName)) {
                        FileObject sslIniFo = startdFo.getFileObject("ssl.ini");
                        if (sslIniFo != null) {
                            sslIniFo.delete();
                        }
                    }

                } catch (IOException ex) {
                    LOG.log(Level.FINE, ex.getMessage()); //NOI18N
                }
            }
        }
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
    protected void applyStartDChanges(FileObject startd, String iniName) {
        FileObject fo = startd.getFileObject(iniName + ".ini");
        switch (iniName) {
            case "jsf":
                applyStartDJsfChanges(fo);
                break;
            case "cdi":
                applyStartDCdiChanges(fo);
                break;
            case "https":
                applyStartDHttpsChanges(fo);
                break;
            case "spdy":
                applyStartDSPDYChanges(fo);
                break;
            case "ssl":
                applyStartDSslChanges(fo);
                break;
        }

    }

    protected void applyStartDJsfChanges(FileObject iniFo) {

    }

    protected void applyStartDCdiChanges(FileObject cdiFo) {

    }

    protected void applyStartDHttpsChanges(FileObject httpsFo) {
        if (httpsFo == null) {
            return;
        }
        HttpsIni ini = new HttpsIni(FileUtil.toFile(httpsFo));
        String v = (String) wiz.getProperty(JettyConstants.JETTY_HTTPS_PORT);
        ini.setHttpsPort(v);
        v = (String) wiz.getProperty(JettyConstants.JETTY_HTTPS_TIMEOUT);
        ini.setHttpsTimeout(v);
        ini.save();
    }

    protected void applyStartDSPDYChanges(FileObject spdyFo) {
        if (spdyFo == null) {
            return;
        }
        SpdyIni ini = new SpdyIni(FileUtil.toFile(spdyFo));
        String v = (String) wiz.getProperty(JettyConstants.JETTY_SPDY_PORT);
        ini.setSpdyPort(v);
        v = (String) wiz.getProperty(JettyConstants.JETTY_SPDY_TIMEOUT);
        ini.setSpdyTimeout(v);
        ini.save();

    }

    protected void applyStartDSslChanges(FileObject sslFo) {
        if (sslFo == null) {
            return;
        }
        SslIni ini = new SslIni(FileUtil.toFile(sslFo));
        String v = (String) wiz.getProperty(JettyConstants.JETTY_SECURE_PORT_PROP);
        ini.setSecurePort(v);
        ini.save();

    }
    public void copyFile(FileObject startdFo, String iniName) throws IOException {
        String tmpl = Utils.stringOf(JettyServerCustomizer.class.getResourceAsStream("/org/netbeans/modules/jeeserver/jetty/resources/" + iniName + ".ini.template"));
        InputStream is = new ByteArrayInputStream(tmpl.getBytes());
        FileObject fo = startdFo.getFileObject(iniName + ".ini");
        if ( fo == null ) {
            startdFo.createData(iniName + ".ini");
            fo = startdFo.getFileObject(iniName + ".ini");
        }
        OutputStream os = fo.getOutputStream();
        
        try {
            FileUtil.copy(is, os);
        } finally {
            try {
                is.close();
                os.close();
            } catch (IOException ex) {
                LOG.log(Level.FINE, ex.getMessage()); //NOI18N
            }
        }
        
    }
}
