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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.openide.filesystems.FileObject;

/**
 *
 * @author V. Shyshkin
 */
public class JettyAddInstanceIterator extends AbstractJettyInstanceIterator {

    private static final Logger LOG = Logger.getLogger(JettyAddInstanceIterator.class.getName());

    public JettyAddInstanceIterator() {
        super();
    }

    public static JettyAddInstanceIterator createIterator() {
        return new JettyAddInstanceIterator();
    }

    @Override
    public Set<InstanceProperties> instantiate() throws IOException {
        final Set<FileObject> fileObjectSet = new LinkedHashSet<>();
        instantiateProjectDir(fileObjectSet);

        Iterator<FileObject> it = fileObjectSet.iterator();
        FileObject fo = null;
        while (it.hasNext()) {
            fo = it.next();
            break;
        }
        Project p = FileOwnerQuery.getOwner(fo);

        final Set<InstanceProperties> ipSet = instantiateServerProperties();
        return ipSet;
    }
}
