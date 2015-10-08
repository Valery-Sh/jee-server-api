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
package org.netbeans.modules.jeeserver.base.deployment.specifics;

/**
 *
 * @author V. Shyshkin
 */
public interface ServerSpecificsProvider {
    String getServerId();
    String[] getSupportedServerIds();
    /**
     * Must return a string concatenation: {@literal actualId} parameter
     * and the string as postfix.
     * 
     * @param actualId
     * @return a command manager name as a string without extension
     */
    //String getCommandManagerName(String actualId );
    /**
     *  Returns an object 
     *  @return 
     */
    ServerSpecifics getSpecifics();
}
