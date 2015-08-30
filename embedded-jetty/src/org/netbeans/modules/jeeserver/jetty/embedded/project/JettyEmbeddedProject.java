/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jeeserver.jetty.embedded.project;

import java.beans.PropertyChangeListener;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.jeeserver.base.deployment.specifics.StartServerPropertiesProvider;
import org.netbeans.modules.jeeserver.base.embedded.project.EmbeddedProject;
import org.netbeans.modules.jeeserver.base.embedded.project.EmbeddedProjectLogicalView;
import org.netbeans.spi.project.*;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Valery
 */
public class JettyEmbeddedProject extends EmbeddedProject{
    
    public static final String TYPE= "org-jetty-embedded-instance-project";
    
    public JettyEmbeddedProject(FileObject projectDir, ProjectState state) {
        super(projectDir, state);
    }

    @Override
    protected LogicalViewProvider getLogicalViewProvider() {
      
        return new EmbeddedProjectLogicalView(this);
    }

    @Override
    protected ProjectInformation getProjectInformation() {
        return new Info();
    }


    @Override
    protected String getIconImagePath() {
        return Info.JETTY_ICON;
    }

    @Override
    protected String getServerId() {
        return "jetty-embedded";
    }

    @Override
    protected String getLayerProjectFolderPath() {
        return "Projects/org-jetty-embedded-instance-project/Nodes";
    }
    
    
    private final class Info implements ProjectInformation {

        @StaticResource()
        public static final String JETTY_ICON = "org/netbeans/modules/jeeserver/jetty/embedded/resources/jetty-server-01-16x16.png";

        @Override
        public Icon getIcon() {
            return new ImageIcon(ImageUtilities.loadImage(JETTY_ICON));
        }

        @Override
        public String getName() {
            return getProjectDirectory().getName();
        }

        @Override
        public String getDisplayName() {
            return getName();
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener pcl) {
            //do nothing, won't change
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener pcl) {
            //do nothing, won't change
        }

        @Override
        public Project getProject() {
            return JettyEmbeddedProject.this;
        }

    }//class Info

    
}//class
