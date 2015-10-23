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
package org.netbeans.modules.jeeserver.base.deployment.utils;

import java.awt.Image;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.spi.project.ProjectIconAnnotator;
import org.openide.util.ChangeSupport;
import org.openide.util.ImageUtilities;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 * The NetBeans project that represents a Server may be in running or
 * not-running state.
 *
 * The class annotates the project with an icon that visually indicates the
 * state in the Project View of the IDE.
 *
 * @author V. Shyshkin
 */
@ServiceProvider(service = ProjectIconAnnotator.class)
public class BaseServerIconAnnotator implements ProjectIconAnnotator {

    protected static final RequestProcessor RP = new RequestProcessor(BaseServerIconAnnotator.class);

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private boolean enabled;

//    @StaticResource
//    private static final String IMAGE = "org/netbeans/modules/jeeserver/base/deployment/resources/server.png";
    @StaticResource
    private static final String RUNNING_IMAGE = "org/netbeans/modules/jeeserver/base/deployment/resources/running.png";

    public BaseServerIconAnnotator() {
    }

    @Override
    public Image annotateIcon(Project p, Image orig, boolean openedNode) {
        if ("AServerSuite01".equals(p.getProjectDirectory().getNameExt())) {
        }
        
        BaseDeploymentManager dm = BaseUtil.managerOf(p);
        if (dm == null) {
            return orig;
        }
        Image projImage = dm.getSpecifics().getProjectImage(p);

        if (projImage == null) {
            return orig;
        }
        Image runImage = projImage;
        /**
         * We don't ping a server as we can by calling dm.getServerRunning
         */
        if (dm.isActuallyRunning()) {
            runImage = ImageUtilities.mergeImages(projImage, ImageUtilities.loadImage(RUNNING_IMAGE), 16, 8);
        }
        long t1 = System.currentTimeMillis();
        RP.post(
                new Runnable() {

                    @Override
                    public void run() {
                        dm.isServerRunning();
                    }

                }, 0, Thread.NORM_PRIORITY
        );
        return runImage;
    }

    public @Override
    void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public @Override
    void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    void setEnabled(boolean enabled) {
        this.enabled = enabled;
        changeSupport.fireChange();
    }

    public void serverStateChanged() {
        changeSupport.fireChange();
    }

}
