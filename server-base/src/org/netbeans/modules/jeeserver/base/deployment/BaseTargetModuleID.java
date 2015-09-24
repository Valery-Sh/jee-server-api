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

import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;

/**
 *
 * @author V. Shyshkin
 */
public class BaseTargetModuleID implements TargetModuleID {

    private final BaseTarget target;
    private final String hostname;
    private final int port;
    private String contextPath;
    private String projectDir;

    /**
     * Creates a new instance of the class for the given target server, the host
     * name, the port number and the context path.
     *
     * @param target
     * @param hostname
     * @param port
     * @param contextPath
     */
    protected BaseTargetModuleID(BaseTarget target, String hostname, int port, String contextPath) {
        this.target = target;
        this.hostname = hostname;
        this.port = port;
        this.contextPath = contextPath;
    }

    private static BaseTargetModuleID getInstance(BaseDeploymentManager dm, BaseTarget t, String cp) {
        int p = Integer.parseInt(InstanceProperties.getInstanceProperties(dm.getUri())
                .getProperty(BaseConstants.HTTP_PORT_PROP));
        String h = InstanceProperties.getInstanceProperties(dm.getUri())
                .getProperty(BaseConstants.HOST_PROP);

        return new BaseTargetModuleID(t, h, p, cp);
    }

    public static BaseTargetModuleID getInstance(BaseDeploymentManager dm, BaseTarget t, String cp, String projectDir) {
        BaseTargetModuleID module = getInstance(dm, t, cp);
        module.setProjectDir(projectDir);
//        BaseUtils.out("ESTargetModuleID projDir=" + projectDir);
        return module;
    }

    /**
     * Retrieve the target server this module was deployed to.
     *
     * @return an object representing a server target.
     */
    @Override
    public Target getTarget() {
        return target;
    }

    public String getProjectDir() {
        return projectDir;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public void setProjectDir(String projectDir) {
        this.projectDir = projectDir;
    }

    /**
     * Retrieve the context path of this module.
     *
     * @return the context path of this module
     */
    public String getContextPath() {
        return contextPath;
    }

    /**
     * Retrieve the id assigned to represent the deployed module.
     *
     * @return the same value as {@link #getWebURL() }
     */
    @Override
    public String getModuleID() {
        //return ((BaseTarget)getTarget()).getUri();
        return getWebURL();
    }

    /**
     * Retrieve the string representation of the web module URL.
     *
     * @return the string concatenation <br/>
     * <code>http://&lt.hostname&gt.:&lt.port&gt.&lt.context-path&gt.</code>
     *
     */
    @Override
    public String getWebURL() {
        String uri = "http://" + hostname + ":" + port;
        return uri + contextPath.replaceAll(" ", "%20");
    }

    /**
     * @return the value of {@literal moduleId}
     */
    @Override
    public String toString() {
        return getModuleID();
    }

    @Override
    public TargetModuleID getParentTargetModuleID() {
        return null;
    }

    @Override
    public TargetModuleID[] getChildTargetModuleID() {
        return null;
    }

}
