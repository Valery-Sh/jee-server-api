package org.netbeans.modules.jeeserver.base.embedded.nodes;

import org.netbeans.modules.j2ee.deployment.plugins.spi.RegistryNodeFactory;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

public class EmbRegistryNodeFactory implements RegistryNodeFactory {

    @Override
    public Node getManagerNode(Lookup lookup) {
        return new EmbManagerNode(lookup);
    }

    @Override
    public Node getTargetNode(Lookup lookup) {
        return new EmbTargetNode();
    }
}