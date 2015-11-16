package org.netbeans.modules.jeeserver.base.deployment.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author V. Shyshkin
 */
public class ParseEntityResolver implements EntityResolver {

    @Override
    public InputSource resolveEntity(String pubid, String sysid)
            throws SAXException, IOException {
        return new InputSource(new ByteArrayInputStream(new byte[0]));
    }
}
