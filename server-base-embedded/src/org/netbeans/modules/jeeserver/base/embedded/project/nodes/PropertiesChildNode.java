package org.netbeans.modules.jeeserver.base.embedded.project.nodes;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.deployment.actions.WebAppCommandActions;
import org.netbeans.modules.jeeserver.base.deployment.actions.WebAppOpenInnerProjectAction;
import static org.netbeans.modules.jeeserver.base.embedded.project.nodes.BaseWebAppChildNode.getPath;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;

/**
 * Instances of the class correspond to the files with {@literal webref, warref}
 * extentions.
 *
 * @author V. Shyshkin
 */
public class PropertiesChildNode extends BaseWebAppChildNode {
    private static final Logger LOG = Logger.getLogger(PropertiesChildNode.class.getName());

    /**
     * Creates a new instance of the class for the specified project and node
     * key.
     *
     * @param project
     * @param key
     * @throws DataObjectNotFoundException should never occur
     */
    public PropertiesChildNode(Project project, Object key) throws DataObjectNotFoundException {
        super(project, key);
    }
    public List<Action> getAddOnActions() {
        return new ArrayList<>();
    }
    /**
     * Returns a web project which this node represents.
     *
     * @return a web project which this node represents. may be {@literal null}
     * when the node represents {@literal *.webref) or
    {@code *.warref} and they reference to a non-existing project.
}     */
    @Override
    public Project getWebAppProject() {
        FileObject propsFo = getServerProject().getProjectDirectory().getFileObject(getPath(getWebAppKey()));
        if (propsFo == null) {
            return null;
        }
        Properties props = BaseUtils.loadProperties(propsFo);
        String webappPath = props.getProperty("webAppLocation");
        
        if (webappPath == null) {
            return null;
        }
        FileObject webappFo;
        try {
            webappFo = FileUtil.toFileObject(new File(webappPath));
//ESUtils.out("4 PropertiesChidNode getWebAppProject " + webappFo);        
            
            if (webappFo == null || FileOwnerQuery.getOwner(webappFo) == null) {
                return null;
            }
//ESUtils.out("5 PropertiesChidNode getWebAppProject " + webappFo.getNameExt());        

        } catch (Exception x) {
//ESUtils.out("6 PropertiesChidNode getWebAppProject EXCEPTION");        
            
            return null;
        }
//ESUtils.out("7 PropertiesChidNode getWebAppProject " + FileOwnerQuery.getOwner(webappFo));        
        
        return FileOwnerQuery.getOwner(webappFo);
    }

    /**
     * Returns an array of actions specific to this node. The list of supported
     * actions is as follows:
     * <ul>
     * <li>Run</li>
     * <li>Deploy</li>
     * <li>Debug</li>
     * <li>Clean</li>
     * <li>Build</li>
     * <li>Clean and Build</li>
     * <li>Open in Project View</li>
     * </ul>
     *
     * @param context
     * @return
     */
    @Override
    public Action[] getActions(boolean context) {

        try {
            Node node = DataObject.find(getServerProject().getProjectDirectory().getFileObject(getPath(getWebAppKey()))).getNodeDelegate();

            List<Action> list1;
            List<Action> list2 = Arrays.asList(node.getActions(true));
            if (getWebAppProject() != null) {
                Action openInnerAction = WebAppOpenInnerProjectAction.getOpenInnerProjectAction(getWebAppProject().getProjectDirectory().getLookup());
                Action runAction = new WebAppCommandActions.RunAction(getWebAppProject());
                Action debugAction = new WebAppCommandActions.DebugAction(getWebAppProject());
                Action deployAction = new WebAppCommandActions.DeployAction(getWebAppProject());

                Action cleanAction = new WebAppCommandActions.CleanAction(getWebAppProject());
                Action buildAction = new WebAppCommandActions.BuildAction(getWebAppProject());
                Action cleanAndBuildAction = new WebAppCommandActions.CleanAndBuildAction(getWebAppProject());

                list1 = Arrays.asList(new Action[]{
                    openInnerAction,
                    runAction,
                    debugAction,
                    deployAction,
                    //                    profileAction, //TODO may be
                    null, // separator

                    cleanAction,
                    buildAction,
                    cleanAndBuildAction,
                    null, // separator
                });

                list1 = new ArrayList(list1);

            } else {
                list1 = new ArrayList();
                list1.addAll(getAddOnActions());
            }
            list1.addAll(new ArrayList(list2));

            return list1.toArray(new Action[list1.size()]);
        } catch (DataObjectNotFoundException ex) {
           LOG.log(Level.INFO, ex.getMessage());
        }

        return new Action[]{};

    }

    /**
     * The instances of the class correspond to files with
     * {@literal webref} extentions.
     *
     * @author V. Shyshkin
     */
    public static class WebRefChildNode extends PropertiesChildNode {

        public WebRefChildNode(Project project, Object key) throws DataObjectNotFoundException {
            super(project, key);
        }
    }//class
    /**
     * The instances of the class correspond to files with
     * {@literal warref} extentions.
     *
     * @author V. Shyshkin
     */
    public static class WarRefChildNode extends PropertiesChildNode {

        public WarRefChildNode(Project project, Object key) throws DataObjectNotFoundException {
            super(project, key);
        }

        /**
         * Returns null value in order to skip web project actions.
         *
         * @return null.
         */
        @Override
        public Project getWebAppProject() {
            return null;
        }
        @Override
        public List<Action> getAddOnActions() {
            List<Action> actions = new ArrayList<>();
            
            WarRefActions.WarRefRunAction runAction = new WarRefActions.WarRefRunAction();
            WarRefActions.WarRefDeployAction deployAction = new  WarRefActions.WarRefDeployAction();            
            WarRefActions.WarRefUndeployAction undeployAction = new  WarRefActions.WarRefUndeployAction();            
            
            actions.add(runAction);
            actions.add(deployAction);
            actions.add(undeployAction);            
            actions.add(null);
            
            return actions;
        }
    }//class    

    /**
     * The instances of the class correspond to files with
     * {@literal htmref} extentions.
     *
     * @author V. Shyshkin
     */
    public static class HtmRefChildNode extends PropertiesChildNode {

        public HtmRefChildNode(Project project, Object key) throws DataObjectNotFoundException {
            super(project, key);
        }

        /**
         * Returns null value in order to skip web project actions.
         *
         * @return null.
         */
        @Override
        public Project getWebAppProject() {
            return null;
        }
        @Override
        public List<Action> getAddOnActions() {
            List<Action> actions = new ArrayList<>();
            Html5RefActions.HtmRefRunAction runAction = new Html5RefActions.HtmRefRunAction();
            Html5RefActions.HtmRefDeployAction deployAction = new  Html5RefActions.HtmRefDeployAction();            
            Html5RefActions.HtmRefUndeployAction undeployAction = new  Html5RefActions.HtmRefUndeployAction();            
            Html5RefActions.HtmRefShowBrowserAction showBrowserAction = new  Html5RefActions.HtmRefShowBrowserAction();            
            
            actions.add(runAction);
            actions.add(deployAction);
            actions.add(undeployAction);            
            actions.add(showBrowserAction);            
            
            actions.add(null);
            
            return actions;
        }
    }//class    
    
    /**
     * For future release.
     * The instances of the class correspond to files with
     * {@literal jeeref} extentions.
     *
     * @author V. Shyshkin
     */
    public static class JeeRefChildNode extends PropertiesChildNode {

        public JeeRefChildNode(Project project, Object key) throws DataObjectNotFoundException {
            super(project, key);
        }
    }//class      
    /**
     * For future release.
     * The instances of the class correspond to files with
     * {@literal earref} extentions.
     *
     * @author V. Shyshkin
     */
    public static class EarRefChildNode extends PropertiesChildNode {

        public EarRefChildNode(Project project, Object key) throws DataObjectNotFoundException {
            super(project, key);
        }
    }//class      
}//class
