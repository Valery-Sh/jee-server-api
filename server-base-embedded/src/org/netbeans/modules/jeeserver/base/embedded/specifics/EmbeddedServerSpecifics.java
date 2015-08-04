package org.netbeans.modules.jeeserver.base.embedded.specifics;

import org.netbeans.modules.jeeserver.base.deployment.specifics.ServerSpecifics;
import org.netbeans.modules.jeeserver.base.embedded.utils.EmbConstants;

/**
 *
 * @author V. Shyshkin
 */
public interface EmbeddedServerSpecifics extends ServerSpecifics {
    boolean isEmbedded();
    boolean supportsDistributeAs(EmbConstants.DistributeAs distributeAs);
}
