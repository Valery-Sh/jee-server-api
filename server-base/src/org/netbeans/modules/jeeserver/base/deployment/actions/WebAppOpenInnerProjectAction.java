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
package org.netbeans.modules.jeeserver.base.deployment.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static javax.swing.Action.NAME;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.awt.DynamicMenuContent;
import org.openide.loaders.DataFolder;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 * The class defines an action which allows to open an inner web project or a
 * project referenced by {@code webref} to be opened in the Project View of the
 * IDE. Here the term "inner project" means a project located inside a server
 * project directory named {@code server-instance-config}.
 *
 * @author V. Shyshkin
 */
public final class WebAppOpenInnerProjectAction extends AbstractAction implements ContextAwareAction {

    private static final Logger LOG = Logger.getLogger(WebAppOpenInnerProjectAction.class.getName());

    private static final RequestProcessor RP = new RequestProcessor(WebAppOpenInnerProjectAction.class);

    @Override
    public void actionPerformed(ActionEvent e) {
        assert false;
    }

    @Override
    public Action createContextAwareInstance(Lookup context) {
        assert false;
        return null;
    }

    /**
     *
     * @param context a Lookup of the {@code FileObject} which represents the
     * inner web project directory.
     * @return
     */
    public static Action getOpenInnerProjectAction(Lookup context) {
        return new WebAppOpenInnerProjectAction.ContextAction(context);
    }

    private static final class ContextAction extends AbstractAction {

        private final Lookup context;

        public ContextAction(final Lookup context) {
            this.context = context;
            boolean foundProject = false;


            for (DataFolder d : context.lookupAll(DataFolder.class)) {
                if (ProjectManager.getDefault().isProject(d.getPrimaryFile())) {
                    foundProject = true;
                    break;
                }
            }
            if (!foundProject) {
                putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
                setEnabled(false);
            }

            // TODO menu item label with optional mnemonics
            putValue(NAME, "Open in Project View");

        }

        public @Override
        void actionPerformed(ActionEvent e) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    Set<Project> projects = new HashSet<>();
                    // Collect projects corresponding to selected folders.
                    for (DataFolder d : context.lookupAll(DataFolder.class)) {
                        try {
                            Project p = ProjectManager.getDefault().findProject(d.getPrimaryFile());
                            if (p != null) {
                                projects.add(p);
                            }
                            // Ignore folders not corresponding to projects (will not disable action if some correspond to projects).
                            // Similarly, do not worry about projects which are already open - no harm done.
                        } catch (IOException e) {
                            LOG.log(Level.INFO, e.getMessage());

                        }
                    }
                    OpenProjects.getDefault().open(projects.toArray(new Project[projects.size()]), false, true);
                }
            });
        }//class
    }//class
}
