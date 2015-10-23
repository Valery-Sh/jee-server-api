package org.netbeans.modules.jeeserver.base.embedded.webapp.nodes;

import java.awt.Image;
import javax.swing.Action;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.deployment.actions.WebAppCommandActions;
import org.netbeans.modules.jeeserver.base.deployment.actions.WebAppOpenInnerProjectAction;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.ImageUtilities;

/**
 * Instances of the class correspond to the folders which are themselves
 * web project directories. 
 * The class could be used to build nodes of inner (inside {@code server-instance-config}) folder)
 * web projects. But it will expose all file nodes of the web project directory.
 * Instead the subclass {@link WebAppShortChildNode} is used.
 *
 * @author V. Shyshkin
 */
public class WebAppChildNode extends BaseWebAppChildNode {

    @StaticResource
    private static final String IMAGE = "org/netbeans/modules/jeeserver/base/embedded/resources/webInner01.png";

    /**
     * Creates a new instance of the class for the specified 
     * project and  node key an child nodes keys.
     * The node created has child nodes.
     * 
     * @param serverProj
     * @param key
     * @param webAppKey actually the parameter value is a string value
     *   of the folder name.
     *   If a folder then it is an internal web project inside {@code server-instance-config}.
     * @param childrenKeys keys of child nodes
     * 
     * @throws DataObjectNotFoundException  should never occur
     */
    protected WebAppChildNode(Project serverProj, Object key, Children.Keys childrenKeys) throws DataObjectNotFoundException {
        super(serverProj, key, childrenKeys);
    }
    public static final String NEW_FILE = "&New File...";
    public static final String BUILD = "Build Project";
    public static final String CLEAN_AND_BUILD = "Clean and Build Project";
    public static final String CLEAN = "Clean Project";
    public static final String RUN = "Run Project";
    public static final String DEPLOY = "Deploy";
    public static final String DEBUG = "Debug";
    public static final String PROFILE = "Profile";
    public static final String TEST_RESTFULL_WEB_SERVICE = "Test RESTful Web Services";
    public static final String TEST_PROJECT = "&Test Project";
    public static final String RENAME = "Rename...";
    public static final String MOVE = "Move...";
    public static final String COPY = "Copy...";
    public static final String DELETE = "Delete";
    public static final String FIND = "&Find...";
    public static final String PROPERTIES = "Project Proper&ties";

    /**
     * 
     * @param type
     * @return 
     */
    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage(IMAGE);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return ImageUtilities.loadImage(IMAGE);
    }
    /**
     * Returns an array of actions specific to this node. The list of supported
     * actions is as follows:
     * <ul>
     * <li>Run</li>
     * <li>Deploy</li>
     * <li>Debug</li>
     * <li>Profile</li>
     * <li>Clean</li>
     * <li>Build</li>
     * <li>Clean and Build</li>
     * <li>Open in Project View</li>
     * <li>New File</li>
     * <li>Copy</li>
     * <li>Move</li>
     * <li>Rename</li>
     * <li>Delete</li>
     * <li>Properties</li>
     * </ul>
     *
     * @param context
     * @return an array of actions
     */
    @Override
    public Action[] getActions(boolean context) {
        Action openInnerAction = WebAppOpenInnerProjectAction.getOpenInnerProjectAction(getWebAppProject().getProjectDirectory().getLookup());
        Action profileAction = new WebAppCommandActions.ProfileAction(getWebAppProject());
        Action deployAction = ProjectSensitiveActions.projectCommandAction("redeploy", "Deploy", null);

        Action runAction = new WebAppCommandActions.RunAction(getWebAppProject());
        Action debugAction = new WebAppCommandActions.DebugAction(getWebAppProject());
        Action cleanAction = new WebAppCommandActions.CleanAction(getWebAppProject());
        Action buildAction = new WebAppCommandActions.BuildAction(getWebAppProject());
        Action cleanAndBuildAction = new WebAppCommandActions.CleanAndBuildAction(getWebAppProject());


        Action[] actions = CommonProjectActions.forType("org-netbeans-modules-web-project");
        for (Action a : actions) {
            if (a == null || a.getValue(Action.NAME) == null) {
                continue;
            }
            switch ((String) a.getValue(Action.NAME)) {
                case PROFILE:
                    profileAction = a;
                    break;
            }
        }
        return new Action[]{
            openInnerAction,
            runAction,
            debugAction,
            deployAction,
            profileAction,
            null, // separator

            cleanAction,
            buildAction,
            cleanAndBuildAction,
            null, // separator

            CommonProjectActions.newFileAction(),
            CommonProjectActions.copyProjectAction(),
            CommonProjectActions.moveProjectAction(),
            CommonProjectActions.renameProjectAction(),
            CommonProjectActions.deleteProjectAction(), // Put here more Actions .... 
            null,
            CommonProjectActions.customizeProjectAction(), // Properties
        };
    }
}
