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
package org.netbeans.modules.jeeserver.base.deployment.config;

import java.util.EventObject;

/**
 *
 * @author V. Shyshkin
 */
public class ModulesChangeEvent extends EventObject {
    public static final int DISPOSE = 1;
    public static final int CREATE = 3;
    public static final int SERVER_CHANGE = 5;
    
    public static final int DELETED = 7;
    
    private int eventType;
    
    private WebModuleConfig target;
    
    private WebModuleConfig[] available;
    
    public ModulesChangeEvent(Object available) {
        super(available);
    }
    
    public ModulesChangeEvent(Object source, int eventType, 
            WebModuleConfig target,
            WebModuleConfig[] available ) {
        super(source);
        this.eventType = eventType;
        this.target = target;
        this.available = available;
    }
    
    public WebModuleConfig getTarget() {
       return target; 
    }

    public WebModuleConfig[] getAvailable() {
        return available;
    }
    public int getEventType() {
        return eventType;
    }
    
}
