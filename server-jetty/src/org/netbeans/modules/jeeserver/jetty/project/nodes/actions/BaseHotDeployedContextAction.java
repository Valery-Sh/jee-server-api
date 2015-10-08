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
package org.netbeans.modules.jeeserver.jetty.project.nodes.actions;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtil;
import org.netbeans.modules.jeeserver.base.deployment.utils.Copier;
import org.netbeans.modules.jeeserver.jetty.util.JettyConstants;
import org.netbeans.modules.jeeserver.jetty.util.Utils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

/**
 *
 * @author V. Shyshkin
 */
public class BaseHotDeployedContextAction extends AbstractHotDeployedContextAction {

    private static final Logger LOG = Logger.getLogger(BaseHotDeployedContextAction.class.getName());

    public BaseHotDeployedContextAction(Lookup context, String command) {
        super(context, command);
    }

    /**
     *
     * @param webFo
     * @param props
     * @return a string containing {@code lifecycle state} and web application
     * context path separated by {@code SPACE}
     */
    protected String getWebAppState(FileObject webFo, Properties props) {
        String state = null;
        if (manager != null && manager.pingServer()) {
            if (manager != null && !manager.isStopped()) {//&& manager.pingServer()) {
                state = manager.getSpecifics().execCommand(manager, createCommand("getstatebycontextpath", props));
                //state = manager.getSpecifics().execCommand(project, createCommand("getstate", props));                
            }
        }
        return state;

    }

    @Override
    protected Properties getContextProperties(FileObject webFo) {
        if ( ! isServerRunning() ) {
            return null;
        }
        Properties props = new Properties();

        props.setProperty(CONTEXTPATH, webFo.getNameExt());
        props.setProperty(WAR, webFo.getPath());

        if (webFo.isFolder()) {
            String state = getWebAppState(webFo, props);
            if (state != null) {
                String[] a = state.split(" ");
                props.setProperty("state", a[0]);
                props.setProperty(CONTEXTPATH, a[1]);
            }
        } else {
            switch (webFo.getExt()) {
                case "xml": {
                    //props = Utils.getJettyXmlProperties(project, webFo);
                    props = Utils.getContextProperties(webFo);
                    String state = getWebAppState(webFo, props);
                    if (state != null) {
                        String[] a = state.split(" ");
                        props.setProperty("state", a[0]);
                    }
                    break;
                }
                case "war": {
                    // we must extract jetty-web.xml if exists
                    
                    String s = Copier.ZipUtil.getZipEntryAsString(FileUtil.toFile(webFo), "WEB-INF/jetty-web.xml");
                    props = Utils.getContextProperties(s);
                    if ( props == null ) {
                        props = new Properties();
                        props.setProperty(BaseConstants.CONTEXTPATH_PROP, webFo.getName());
                    }
                    String state = getWebAppState(webFo, props);
                    if (state != null) {
                        String[] a = state.split(" ");
                        props.setProperty("state", a[0]);
                        props.setProperty(CONTEXTPATH, a[1]);
                    }
                    break;
                }
            }
        }
        return props;
    }

    @Override
    protected String getMenuItemName() {
        String result;
        switch (command) {
            case "starthotdeployed":
                result = "Start";
                break;
            case "stophotdeployed":
                result = "Stop";
                break;
            case "undeployhotdeployed":
                result = "Undeploy";
                break;
            default:
                result = "Unknown";
        }
        return result;
    }

    @Override
    protected boolean isActionEnabled() {
        
        boolean serverRunning = isServerRunning();
        
        boolean result;
        
        switch (command) {
            case "starthotdeployed":
                result = serverRunning && ( JettyConstants.STOPPED.equals(contextProps.getProperty("state"))
                            || JettyConstants.SHUTDOWN.equals(contextProps.getProperty("state")));
                break;
            case "stophotdeployed":
                result = serverRunning && JettyConstants.STARTED.equals(contextProps.getProperty("state"));
                break;
            case "stophotdeployedbycontextpath":
                if ( serverRunning ) {
                    result = JettyConstants.STARTED.equals(contextProps.getProperty("state"));
                } else {
                    result = false;
                }
                //result = serverRunning && JettyConstants.STARTED.equals(contextProps.getProperty("state"));
                break;
            case "undeployhotdeployed":
                result = true;
                if (Utilities.isWindows()) {
                    FileObject fo = context.lookup(FileObject.class);
                    result = (!fo.isFolder()) || !serverRunning;
                }
                break;
            default:
                result = false;
        }
        return result;
    }

    @Override
    protected void executeServerCommand(String command, Properties props) {
        if (!manager.pingServer()) {
            return;
        }

        manager.getSpecifics().execCommand(manager, createCommand(command, props));
    }

    /**
     *
     * @param command may be one of
     * {@code "starthotdeployed", "stophotdeployed", "getstate"}
     * @param props
     * @return
     */
    @Override
    protected String createCommand(String command, Properties props) {
        StringBuilder sb = new StringBuilder();
        sb.append("cmd=");
        sb.append(command);
        sb.append("&cp=");
        sb.append(BaseUtil.encode(props.getProperty(CONTEXTPATH)));
        if ( props.getProperty(WAR) != null ) {
            sb.append("&dir=");
            sb.append(BaseUtil.encode(props.getProperty(WAR)));
        }

        return sb.toString();
    }

    @Override
    protected void runActionPerformed() {
        FileObject fo = context.lookup(FileObject.class);
        if (command.equals("undeployhotdeployed")) {

            try {
                fo.delete();
            } catch (IOException ex) {
                BaseUtil.out("BaseHotDeployedContextAction contextFo.delete EXCEPTION " + fo.getNameExt() + "; ex=" + ex.getMessage());
                LOG.log(Level.INFO, ex.getMessage());
            }
        } else {
            executeServerCommand(command, contextProps);
        }

    }

}
