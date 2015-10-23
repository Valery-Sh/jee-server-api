package org.netbeans.modules.jeeserver.jetty;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author V.Shyshkin
 */
public class INFO {
    public static void log(String msg) {
        Logger.getLogger(INFO.class.getName()).log(Level.WARNING, "@@@@@ MYLOG: " + msg );                        
    }
}
