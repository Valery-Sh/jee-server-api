/**
 * This file is part of Base JEE Server support in NetBeans IDE.
 *
 * Base JEE Server support in NetBeans IDE is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * Base JEE Server support in NetBeans IDE is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should see the GNU General Public License here:
 * <http://www.gnu.org/licenses/>.
 */
package org.netbeans.modules.jeeserver.base.deployment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.specifics.ServerSpecifics;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Factory to create {@literal DeploymentManager } that can deploy to
 * {@literal Server}.
 *
 * The Server URI has the following format:
 * <PRE><CODE><i>server-id</i>:deploy:server:<i>project-dir-path</i></CODE></PRE>
 * for example
 *
 * @author V.Shyshkin
 * @see ProjectDeploymentManager
 */
public class FactoryDelegate {

    private final Map<String, BaseDeploymentManager> managers = new ConcurrentHashMap<>();

    private final ServerSpecifics specifics;
    private final String serverId;
    protected String uriPrefix;
    protected List<String> toDelete;

    public FactoryDelegate(String serverId, ServerSpecifics specifics) {
        this.specifics = specifics;
        this.serverId = serverId;
        uriPrefix = serverId + ":" + BaseConstants.URIPREFIX_NO_ID + ":";
        toDelete = new ArrayList<>();
        registerUnusedInstances();
    }

    public ServerSpecifics getSpecifics() {
        return specifics;
    }

    public void deleteUnusedInstances() {

        if (toDelete.isEmpty()) {
            return;
        }
        String[] ar = new String[toDelete.size()];
        ar = toDelete.toArray(ar);

        for (String uri : ar) {
            InstanceProperties ip = InstanceProperties.getInstanceProperties(uri);
            if (ip != null) {
                InstanceProperties.removeInstance(uri);
//        BaseUtils.out("FactoryDelegate deleteUnusedInstances uri=" + uri);

                toDelete.remove(uri);
            }
            InstanceProperties.getInstanceProperties(uri);
        }

    }

    /**
     * Determine whether a server exists under the specified location.
     *
     * @param serverLocation an absolute path of the server project directory
     * @return {@literal true } if the server project exists. {@literal false}
     * otherwise
     */
    /*    protected boolean existsServer(String serverLocation, String serverInstanceDir) {

     if (serverLocation == null) {
     return false;
     }

     File f = new File(serverLocation);
     if (!f.exists()) {
     return false;
     }

     FileObject fo = FileUtil.toFileObject(f);

     Project p = FileOwnerQuery.getOwner(fo);
     if (p == null) {
     return false;
     }

     //        return BaseUtils.isServerProject(p);
     if (p.getLookup().lookup(ServerInstanceProperties.class) != null) {
     return true;
     }

     if (serverInstanceDir == null) {
     return false;
     }

     fo = FileUtil.toFileObject(new File(serverInstanceDir));
     if (fo == null) {
     return false;
     }

     if (fo.getFileObject("instance.properties") == null) {
     return false;
     }

     return true;
     }
     */
    /**
     * Determine whether a server exists under the specified location.
     *
     * @param instanceFO an absolute path of the server project directory
     * @return {@literal true } if the server project exists. {@literal false}
     * otherwise
     */
    protected boolean existsServer(FileObject instanceFO) {

        String serverLocation = (String) instanceFO.getAttribute(BaseConstants.SERVER_LOCATION_PROP);
//        String serverInstanceDir = (String) instanceFO.getAttribute(BaseConstants.SERVER_INSTANCE_DIR_PROP);

        if (serverLocation == null) {
            return false;
        }

        File f = new File(serverLocation);
        if (!f.exists()) {
            return false;
        }

        FileObject fo = FileUtil.toFileObject(f);

        Project p = FileOwnerQuery.getOwner(fo);
        if (p == null) {
            return false;
        }

//        return BaseUtils.isServerProject(p);
        if (p.getLookup().lookup(ServerInstanceProperties.class) == null) {
            return false;
        }

        /*        if (serverInstanceDir == null) {
         return false;
         }

         fo = FileUtil.toFileObject(new File(serverInstanceDir));
         if (fo == null) {
         return false;
         }

         if (fo.getFileObject("instance.properties") == null) {
         return false;
         }
         */
        return true;
    }

    public final synchronized void registerUnusedInstances() {

        FileObject dir = FileUtil.getConfigFile("/J2EE/InstalledServers");
        FileObject instanceFOs[] = dir.getChildren();
        for (FileObject instanceFO : instanceFOs) {
            String url = (String) instanceFO.getAttribute(InstanceProperties.URL_ATTR);
            if (!url.startsWith(uriPrefix)) {
                continue;
            }
            if (existsServer(instanceFO)) {
                continue;
            }
            toDelete.add(url);
        }
    }

    /**
     * Tests whether the factory can create a manager for the URI.
     *
     * @param uri the uri
     * @return true when uri is not null and starts with characters as defined
     * by {@link #URI_PREFIX} , false otherwise
     */
    public boolean handlesURI(String uri) {
        if (uri == null) {
            return false;
        }
        return uri.startsWith(uriPrefix);
    }

    public synchronized void removeManager(String uri) {
        managers.remove(uri);
    }

    /**
     * Gets a connected deployment manager for the given uri, username and
     * password
     *
     * @param uri the uri of the deployment manager
     * @param username the user name
     * @param password the password
     * @return the deployment manager
     * @throws DeploymentManagerCreationException
     */
    public synchronized BaseDeploymentManager getDeploymentManager(String uri, String username, String password) throws DeploymentManagerCreationException {
//        BaseUtils.out("FactoryDelegate getDeploymentManager uri=" + uri);

        deleteUnusedInstances();

        if (InstanceProperties.getInstanceProperties(uri) == null) {
            throw new DeploymentManagerCreationException("Invalid URI:" + uri);
        }
        if (!handlesURI(uri)) {
            throw new DeploymentManagerCreationException("Invalid URI:" + uri);
        }
        BaseDeploymentManager manager = managers.get(uri);

        if (null == manager) {
            manager = new BaseDeploymentManager(uri,specifics);
            //manager.setSpecifics(specifics);

            // put into cache
            managers.put(uri, manager);
            
            //initManger(uri);
            
            specifics.register( manager);
        }

        return manager;
    }


    /**
     *
     * Gets a disconnected version of the deployment manager
     *
     * Delegates the method call to {@link #getDeploymentManager(java.lang.String, java.lang.String, java.lang.String)
     * }
     * with null values of username and password parameters.
     *
     * @param uri the uri of the deployment manager
     * @return the deployment manager
     * @throws DeploymentManagerCreationException
     */
    public DeploymentManager getDisconnectedDeploymentManager(String uri) throws DeploymentManagerCreationException {
        return getDeploymentManager(uri, null, null);
    }

    /**
     * @return a display name. The displayName is a concatenation of the {@link #serverId ) and " Server" string constant.
     * {@literal Server}
     */
    public String getDisplayName() {
        return serverId + " Server";
    }

    /**
     * The version of the deployment manager.
     *
     * @return the version.
     */
    public String getProductVersion() {
        return "Server 1.0";
    }
}
