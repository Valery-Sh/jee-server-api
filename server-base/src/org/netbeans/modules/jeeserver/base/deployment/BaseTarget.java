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

/**
 *
 * @author V. Shyshkin
 */
public class BaseTarget implements Target {

    private final String name;
    private final String uri;
    /**
     * Creates a new instance of the class for a given target name and uri.
     * @param name the value of the {@code name} property
     * @param uri the server instance id
     */
    public BaseTarget (String name, String uri) {
        this.name = name;
        this.uri = uri;
    }
    /**
     * Retrieve the name of the target server.
     * @return the name of the server
     */
    @Override
    public String getName () {
        return name;
    }
    /**
     * Retrieve the description of the target server.
     * Just returns the value of the {@code name} property.
     * @return the description of the server
     */
    @Override
    public String getDescription () {
        return name;
    }
    /**
     * Retrieve the server instance id.
     * @return the server instance id
     */ 
    public String getUri () {
        return uri;
    }
    /**
     * @return the value of the {@code name} property.
     */
    @Override
    public String toString () {
        return name;
    }
    
}
