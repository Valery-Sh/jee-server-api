/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jeeserver.base.embedded.project.nodes;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jeeserver.base.embedded.project.SuiteManager;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;

/**
 *
 * @author Valery
 */
public class InstanceNodeFactory implements NodeFactory {

    private static final Logger LOG = Logger.getLogger(InstanceNodeFactory.class.getName());

    @Override
    public NodeList createNodes(Project project) {
        try {
            ServerInstancesRootNode node = new ServerInstancesRootNode(project);
            //node.init(project);
            return NodeFactorySupport.fixedNodeList(node);
        } catch (DataObjectNotFoundException ex) {
            LOG.log(Level.INFO, ex.getMessage());
        }
//        }
        //If the above try/catch fails, e.g.,
        //our item isn't in the lookup,
        //then return an empty list of nodes:
        return NodeFactorySupport.fixedNodeList();
    }

    public static Node getNode(String key, Project suiteProj) {
        Node node = null;
        try {
            //FileObject fo = suiteProj.getProjectDirectory().getFileObject(SuiteConstants.SERVER_INSTANCES_FOLDER);
            FileObject fo = SuiteManager.getManager(key).getServerProject().getProjectDirectory();
            node = new InstanceNode(DataObject.find(fo).getNodeDelegate(), key);
//                    ,suiteProj.getLookup().lookup(NodeModel.class));
            
        } catch (Exception ex) {
            LOG.log(Level.INFO, ex.getMessage());
        }
        return node;
    }

}
