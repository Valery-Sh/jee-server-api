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
package org.netbeans.modules.jeeserver.jetty.nodes;

import java.awt.Component;
import javax.swing.Action;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.jetty.customizer.JettyServerCustomizer;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;


public class JettyServerManagerNode extends AbstractNode {
    @StaticResource
    private final static String ICON_BASE = "org/netbeans/modules/jeeserver/jetty/resources/jetty01-16x16.jpg";
    private final Lookup lookup;

    public JettyServerManagerNode(Lookup lookup) {
        super(Children.LEAF);
        // Set default lookup
        this.lookup = lookup;
        // Set icon
        setIconBaseWithExtension(ICON_BASE);
    }

    @Override
    public String getDisplayName() {
        return "jetty Server";
    }

    @Override
    public SystemAction[] getActions() {
        return new SystemAction[]{};
    }

    @Override
    public Action[] getActions(boolean context) {
        //return null;
        return new Action[]{};
    }

    @Override
    public boolean hasCustomizer() {
        return true;
    }

    @Override
    public Component getCustomizer() {
        JettyServerCustomizer c = new JettyServerCustomizer(getDeploymentManager());
        c.setSaveButtonVisible(true);
        return c;
    }

    public BaseDeploymentManager getDeploymentManager() {
        return lookup.lookup(BaseDeploymentManager.class);
    }
 
}