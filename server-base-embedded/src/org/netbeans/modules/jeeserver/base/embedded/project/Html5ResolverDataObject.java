//Html5ResolverDataObject
package org.netbeans.modules.jeeserver.base.embedded.project;
import java.io.IOException;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.text.DataEditorSupport;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

@Messages({
    "LBL_Html5Resolver_LOADER=Files of DeployedResolver"
})
@MIMEResolver.ExtensionRegistration(
        
        displayName = "#LBL_Html5Resolver_LOADER",
        mimeType = "text/x-htmref",
        extension = {"htmref"})
@DataObject.Registration(
        mimeType = "text/x-htmref",
        iconBase = "org/netbeans/modules/jeeserver/base/embedded/resources/HTML5_project_icon.png",
        displayName = "#LBL_Html5Resolver_LOADER",
        position = 300)
@ActionReferences({
    @ActionReference(
            path = "Loaders/text/x-htmref/Actions",
            id =
            @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
            position = 100,
            separatorAfter = 200),
    @ActionReference(
            path = "Loaders/text/x-htmref/Actions",
            id =
            @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
            position = 300),
    @ActionReference(
            path = "Loaders/text/x-htmref/Actions",
            id =
            @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
            position = 400,
            separatorAfter = 500),
    @ActionReference(
            path = "Loaders/text/x-htmref/Actions",
            id =
            @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
            position = 600),
    @ActionReference(
            path = "Loaders/text/x-htmref/Actions",
            id =
            @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
            position = 700,
            separatorAfter = 800),
    @ActionReference(
            path = "Loaders/text/x-htmref/Actions",
            id =
            @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
            position = 900,
            separatorAfter = 1000),
    @ActionReference(
            path = "Loaders/text/x-htmref/Actions",
            id =
            @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
            position = 1100,
            separatorAfter = 1200),
    @ActionReference(
            path = "Loaders/text/x-/Actions",
            id =
            @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
            position = 1300),
    @ActionReference(
            path = "Loaders/text/x-htmref/Actions",
            id =
            @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
            position = 1400)
})
/**
 * Registers new file types with extentions {@code webref,jeeref,warref,earref}.
 * Those file types associated with {@code properties} files.
 */
public class Html5ResolverDataObject extends MultiDataObject {
//extension = {"properties","tmpref","innerref","webref","jeeref","warref","earref","htmref})
    public Html5ResolverDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        registerEditor("text/x-htmref", true);
        //Associate .deployed editor with .properties editor
        getLookup().lookup(DataEditorSupport.class).setMIMEType("text/x-properties");
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

    @MultiViewElement.Registration(
            
            displayName = "#LBL_Html5Resolver_EDITOR",
            iconBase = "org/netbeans/modules/jeeserver/base/embedded/resources/HTML5_project_icon.png",
            mimeType = "text/x-htmref",
            persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
            preferredID = "Html5Resolver",
            position = 1000)
    @Messages("LBL_Html5Resolver_EDITOR=Source")
    public static MultiViewEditorElement createEditor(Lookup lkp) {
        return new MultiViewEditorElement(lkp);
    }
}
