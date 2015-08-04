/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jeeserver.jetty.project.nodes.actions;

import org.netbeans.modules.jeeserver.jetty.project.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.common.CreateCapability;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.Listener;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.jeeserver.base.deployment.config.ModulesChangeEvent;
import org.netbeans.modules.jeeserver.base.deployment.config.ModulesChangeListener;
import org.netbeans.modules.jeeserver.base.deployment.config.ServerInstanceAvailableModules;
import org.netbeans.modules.jeeserver.base.deployment.config.WebModuleConfig;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.netbeans.modules.jeeserver.jetty.deploy.*;
import org.netbeans.modules.jeeserver.jetty.project.nodes.JettyBaseRootNode;
import org.netbeans.modules.jeeserver.jetty.util.JettyConstants;
import org.netbeans.modules.jeeserver.jetty.util.StartIni;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Valery
 */
public class DDHelper  {

    private static final Logger LOG = Logger.getLogger(JettyBaseRootNode.class.getName());

    public static boolean needsBeansXml(Project serverProj, Project webProj) {
        FileObject webFo = webProj.getProjectDirectory();

        WebModule wm = WebModule.getWebModule(webFo);

        File f = Paths.get(serverProj.getProjectDirectory().getPath(), JettyConstants.JETTY_START_INI).toFile();

        StartIni startIni = new StartIni(f);
        if (wm.getWebInf() == null) {
            return false;
        }
        FileObject beansXml = wm.getWebInf().getFileObject("beans.xml");
        boolean cdiEnabled = startIni.isEnabled("cdi");

        if (cdiEnabled && beansXml == null) {
            return true;
        } else {
            return false;
        }
    }

    public static void addBeansXml(Project serverProj, Project webProj) {

        FileObject webFo = webProj.getProjectDirectory();

        if (webFo == null) {
            return;
        }
        WebModule wm = WebModule.getWebModule(webFo);

        if (wm == null) {
            return;
        }

        File f = Paths.get(serverProj.getProjectDirectory().getPath(), JettyConstants.JETTY_START_INI).toFile();

        StartIni startIni = new StartIni(f);
        if (wm.getWebInf() != null) {
            FileObject beansXml = wm.getWebInf().getFileObject("beans.xml");
            Path beansPath = Paths.get(wm.getWebInf().getPath(), "beans.xml");

            boolean cdiEnabled = startIni.isEnabled("cdi");
            if (cdiEnabled && beansXml == null) {
                //
                // Add beans.xml
                //
                try (InputStream is = JettyBaseRootNode.class.getResourceAsStream("/org/netbeans/modules/jeeserver/jetty/resources/beans.xml");
                        OutputStream os = wm.getWebInf().createAndOpen("beans.xml")) {
                    FileUtil.copy(is, os);
                } catch (IOException ex) {
                    LOG.log(Level.INFO, ex.getMessage());
                }

            } else if (!cdiEnabled && beansXml != null) {
                //
                // Delete beans.xml
                //
                try {
                    Files.delete(beansPath);
                } catch (IOException ex) {
                    LOG.log(Level.INFO, ex.getMessage());
                }
            }
        }

    }

    public static void addJsfListener(Project serverProj, Project webProj) {
        DDProvider p = DDProvider.getDefault();

        FileObject webFo = webProj.getProjectDirectory();

        if (webFo == null) {
            return;
        }

        WebModule wm = WebModule.getWebModule(webFo);

        if (wm == null) {
            return;
        }
        webFo = wm.getDeploymentDescriptor();

        if (webFo == null) {
            return;
        }

        File f = Paths.get(serverProj.getProjectDirectory().getPath(), JettyConstants.JETTY_START_INI).toFile();

        StartIni startIni = new StartIni(f);

        String listenerClass = startIni.getListenerClassForEnabledJsf();

        if (listenerClass == null) {
            return;
        }

        try {
            WebApp webapp = p.getDDRoot(webFo);
            Listener[] listeners = webapp.getListener();
            for (Listener l : listeners) {
                if (listenerClass.equals(l.getListenerClass())) {
                    return;
                }
            }
            List<String> supported = startIni.getSupportedJsfListenerClasses();

            for (Listener l : listeners) {

                if (supported.contains(l.getListenerClass())) {
                    webapp.removeListener(l);
                }
            }

            addListener(webapp, listenerClass);
            webapp.write(webFo);
        } catch (Exception ex) {
            LOG.log(Level.INFO, ex.getMessage());
        }

    }

    public static boolean hasJsfListener(Project serverProj, Project webProj) {
        DDProvider p = DDProvider.getDefault();

        FileObject webFo = webProj.getProjectDirectory();

        if (webFo == null) {
            return true;
        }

        WebModule wm = WebModule.getWebModule(webFo);

        if (wm == null) {
            return true;
        }
        webFo = wm.getDeploymentDescriptor();

        if (webFo == null) {
            return true;
        }

        File f = Paths.get(serverProj.getProjectDirectory().getPath(), JettyConstants.JETTY_START_INI).toFile();

        StartIni startIni = new StartIni(f);

        String listenerClass = startIni.getListenerClassForEnabledJsf();

        if (listenerClass == null) {
            return true;
        }

        try {
            WebApp webapp = p.getDDRoot(webFo);
            Listener[] listeners = webapp.getListener();
            for (Listener l : listeners) {
                if (listenerClass.equals(l.getListenerClass())) {
                    return true;
                }
            }

        } catch (Exception ex) {
            LOG.log(Level.INFO, ex.getMessage());
            return true;
        }
        return false;

    }
    protected static Listener addListener(WebApp webApp, String classname) throws IOException {
        Listener listener = (Listener) createBean(webApp, "Listener"); // NOI18N

        listener.setListenerClass(classname);
        webApp.addListener(listener);
        return listener;
    }

    protected static CommonDDBean createBean(CreateCapability creator, String beanName) throws IOException {
        CommonDDBean bean = null;
        try {
            bean = creator.createBean(beanName);
        } catch (ClassNotFoundException ex) {
            throw new IOException("Error creating bean with name:" + beanName); // NOI18N
        }
        return bean;
    }

}
