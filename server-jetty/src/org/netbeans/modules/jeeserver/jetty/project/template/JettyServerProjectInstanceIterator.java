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
package org.netbeans.modules.jeeserver.jetty.project.template;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import org.netbeans.api.templates.TemplateRegistration;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;

// TODO define position attribute
@TemplateRegistration(folder = "Project/JettyServer", 
        displayName = "#JettyServerInstance_displayName", 
        description = "JettyServerInstanceDescription.html", 
        iconBase = "org/netbeans/modules/jeeserver/jetty/resources/JettyServerInstance.png", content = "JettyServerInstanceProject-9.2.0.zip")
@Messages("JettyServerInstance_displayName=Jetty Server Instance")
public class JettyServerProjectInstanceIterator extends AbstractJettyInstanceIterator {


    public JettyServerProjectInstanceIterator() {
        super();
    }

    public static JettyServerProjectInstanceIterator createIterator() {
        return new JettyServerProjectInstanceIterator();
    }

    @Override
    public Set<FileObject> instantiate() throws IOException {
        final Set<FileObject> fileObjectSet = new LinkedHashSet<>();
        instantiateProjectDir(fileObjectSet);
        instantiateServerProperties();
        return fileObjectSet;
    }
}
