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
package org.netbeans.modules.jeeserver.base.deployment.progress;

import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.api.progress.ProgressHandle;

/**
 *
 * @author V. Shyshkin
 */
public class BaseProgressListener  implements ProgressListener {

        private final ProgressObject progressObject;
        private final ProgressHandle handle;
        private final String message;
        
        public BaseProgressListener(ProgressObject progressObject,ProgressHandle handle, String message) {
            this.handle = handle;
            this.progressObject = progressObject;
            this.message = message;
        }

        @Override
        public void handleProgressEvent(ProgressEvent progressEvent) {
           if (progressEvent.getDeploymentStatus().isRunning()) {
               handle.progress(message);
           } else if (progressEvent.getDeploymentStatus().isCompleted()) {
                handle.finish();
                progressObject.removeProgressListener(this);
                
           } else if (progressEvent.getDeploymentStatus().isFailed()) {
                handle.finish();
                progressObject.removeProgressListener(this);
           }
        }

}
