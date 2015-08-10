/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jeeserver.jetty.project;

import java.io.IOException;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.modules.jeeserver.jetty.util.HttpIni;
import org.netbeans.modules.jeeserver.jetty.util.StartIni;
import org.netbeans.modules.jeeserver.jetty.util.Utils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.text.DataEditorSupport;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

@Messages({
    "LBL_IniFile_LOADER=Ini Files of JettyServer"
})
@MIMEResolver.ExtensionRegistration(
        displayName = "#LBL_IniFile_LOADER",
        mimeType = "text/x-fileini",
        extension = {"ini"}
)
@DataObject.Registration(
        mimeType = "text/x-fileini",
        iconBase = "org/netbeans/modules/jeeserver/jetty/resources/properties-16x16.gif",
        displayName = "#LBL_IniFile_LOADER",
        position = 300
)
@ActionReferences({
    @ActionReference(
            path = "Loaders/text/x-fileini/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
            position = 100,
            separatorAfter = 200
    ),
    @ActionReference(
            path = "Loaders/text/x-fileini/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
            position = 300
    ),
    @ActionReference(
            path = "Loaders/text/x-fileini/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
            position = 400,
            separatorAfter = 500
    ),
    @ActionReference(
            path = "Loaders/text/x-fileini/Actions",
            id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
            position = 600
    ),
    @ActionReference(
            path = "Loaders/text/x-fileini/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
            position = 700,
            separatorAfter = 800
    ),
    @ActionReference(
            path = "Loaders/text/x-fileini/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
            position = 900,
            separatorAfter = 1000
    ),
    @ActionReference(
            path = "Loaders/text/x-fileini/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
            position = 1100,
            separatorAfter = 1200
    ),
    @ActionReference(
            path = "Loaders/text/x-fileini/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
            position = 1300
    ),
    @ActionReference(
            path = "Loaders/text/x-fileini/Actions",
            id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
            position = 1400
    )
})
public class IniFileDataObject extends MultiDataObject {

    public IniFileDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        Project server = FileOwnerQuery.getOwner(pf);
        registerEditor("text/x-fileini", true);
        if ( isJettyIniFile(pf) ) {
            
            if ( null != pf.getNameExt() )
                /*            if ("http.ini".equals(pf.getNameExt())) {
            pf.addFileChangeListener(new HttpIni.HttpIniFileChangeHandler(server));
            }
             */
                switch (pf.getNameExt()) {
                case "start.ini":
                    pf.addFileChangeListener(new StartIni.StartIniFileChangeHandler(server));
                    break;
                case "http.ini":
                    pf.addFileChangeListener(new HttpIni.HttpIniFileChangeHandler(server));
                    break;
                default:
                    pf.addFileChangeListener(new StartIni.StartIniFileChangeHandler(server));
                    break;
            }
            
            getLookup().lookup(DataEditorSupport.class).setMIMEType("text/x-properties");
        }
    }

    @Override
    protected Node createNodeDelegate() {
        Node node;// = null;
//        BaseUtils.out("&&&&&&& IniFileDataObject.createNodeDelegate primaryfile==" + getPrimaryFile());        
        if ( isJettyIniFile(getPrimaryFile())) {
            node = new IniDataNode(this, Children.LEAF);
        } else {
            node = super.createNodeDelegate();
        }
        return node;
    }

    protected static boolean isJettyIniFile(FileObject pf) {
        if ("IniFileTemplate.ini".equals(pf.getNameExt())) {
            return true;
        }
        FileObject p = pf.getParent();
        if (p == null) {
            return false;
        }
        if (!"jettybase".equals(p.getNameExt()) && !"start.d".equals(p.getNameExt())) {
            return false;
        }
        Project proj = FileOwnerQuery.getOwner(p);
        if (proj == null) {
            return false;
        }

        return Utils.isJettyServer(proj);
    }

    @Override
    protected int associateLookup() {
        return 0;
    }

    @MultiViewElement.Registration(
            displayName = "#LBL_IniFile_EDITOR",
            iconBase = "org/netbeans/modules/jeeserver/jetty/resources/properties-16x16.gif",
            mimeType = "text/x-fileini",
            persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
            preferredID = "IniFile",
            position = 1000
    )
    @Messages("LBL_IniFile_EDITOR=Source")
    public static MultiViewEditorElement createEditor(Lookup lkp
    ) {
        return new MultiViewEditorElement(lkp);

    }

    public static class IniDataNode extends DataNode {
        
        
        public IniDataNode(DataObject obj, Children ch, Lookup lookup) {
            super(obj, ch, lookup);
        }

        public IniDataNode(DataObject obj, Children ch) {
            super(obj, ch);
        }
        @Override
        public String getDisplayName() {
            if ( getDataObject() == null ) {
                return "dataObjNull";
            }
            FileObject fo = getDataObject().getPrimaryFile();
            if ( fo == null) {
                return "primaryNull";
            }
            String  h = fo.getNameExt();
            return h;
        }

        @Override
        public String getHtmlDisplayName() {
            FileObject fo = getDataObject().getPrimaryFile();
            
            String h = fo.getNameExt();
            h = "<font color='!textText'>" + h + "</font>";
            return h;
        }
    }
}//class
