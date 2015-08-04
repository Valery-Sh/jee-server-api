/**
 * This file is part of Jetty Server Embedded support in NetBeans IDE.
 *
 * Jetty Server Embedded support in NetBeans IDE is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * Jetty Server Embedded support in NetBeans IDE is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should see the GNU General Public License here:
 * <http://www.gnu.org/licenses/>.
 */
package org.netbeans.modules.jeeserver.jetty.embedded;

import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfigurationFactory2;


public class JettyConfigurationFactory  implements ModuleConfigurationFactory2 {

    @Override
    public ModuleConfiguration create(J2eeModule module) throws ConfigurationException {
        return create(module, null);
    }

    @Override
    public ModuleConfiguration create(J2eeModule module, String instanceUrl) throws ConfigurationException {
        String[] paths = new String[] {"WEB-INF/jetty-web.xml","WEB-INF/web-jetty.xml"};
        return JettyModuleConfiguration.getInstance(module, paths,instanceUrl);

    }
}