package org.netbeans.modules.jeeserver.base.embedded.webapp.actions;

import org.netbeans.modules.jeeserver.base.deployment.actions.CommandActionProgress;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.embedded.utils.EmbPackageUtils;

/**
 *
 * @author V. Shyshkin
 */
public class BatchActionCommand {

    public static CommandActionProgress[] invokeAction(final Project serverProject, final String command) {
        final List<Project> webProjects = EmbPackageUtils.getWebProjects(serverProject);
        final CommandActionProgress[] caps = new CommandActionProgress[webProjects.size() + 1];

        for (int i = 0; i < caps.length -  1; i++) {
            Project p = webProjects.get(i);
            caps[i] = CommandActionProgress.invokeAction(p, command);
        }
        
        // Add Server Project to rebuild
        caps[caps.length - 1] = CommandActionProgress.invokeAction(serverProject, command);
        return caps;

    }
}
