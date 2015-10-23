/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jeeserver.base.deployment.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOColorLines;
import org.openide.windows.IOColors;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author Valery
 */
public class Info {

    protected static final RequestProcessor RP = new RequestProcessor(Info.class);
    
    private static final Logger LOG = Logger.getLogger(Info.class.getName());
    private final List<Store> storeList;

    public Info() {
        storeList = new ArrayList<>();
    }

    public static void out(String ioName, String... messages) {
        IOColors.OutputType ot = IOColors.OutputType.ERROR;
        out(ioName, ot, messages);
    }

    public static void out(String ioName, IOColors.OutputType ot, String... messages) {
        final InputOutput io = IOProvider.getDefault().getIO(ioName, false);
        RP.post(() -> {
            io.getOut().println("-----------------------------------------------------");
            for (String msg : messages) {
                if (IOColorLines.isSupported(io)) {
                    try {
                        IOColorLines.println(io, msg, IOColors.getColor(io, ot));
                    } catch (IOException ex) {
                        LOG.log(Level.INFO, ex.getMessage());
                    }
                } else {
                    io.getOut().println(msg);
                }
            }
            io.getOut().println("-----------------------------------------------------");
            io.select();
        });
    }

    public void out(String ioName) {
        //final IOColors.OutputType ot = IOColors.OutputType.LOG_SUCCESS;
        final InputOutput io = IOProvider.getDefault().getIO(ioName, false);
        //
        // The next line is nessasary . Otherwise no printing occurs
        //
        io.getOut().println("");        

        RP.post(() -> {
            storeList.stream().forEach((s) -> {
                IOColors.OutputType ot = s.getColorType();
                for (String msg : s.getMessages()) {
                    if (IOColorLines.isSupported(io)) {
                        try {
                            IOColorLines.println(io, msg, IOColors.getColor(io, ot));
                        } catch (IOException ex) {
                            LOG.log(Level.INFO, ex.getMessage());
                        }
                    } else {
                        io.getOut().println(msg);
                    }
                }
            });
            io.select();
            storeList.clear();
        });

    }

    public void add(String[] msgs) {
        IOColors.OutputType ot;
        if (storeList.isEmpty()) {
            ot = IOColors.OutputType.LOG_SUCCESS;
        } else {
            ot = storeList.get(storeList.size() - 1).getColorType();
        }
        storeList.add(new Store(ot, msgs));
    }

    public void add(IOColors.OutputType ot, String[] msgs) {
        storeList.add(new Store(ot, msgs));
    }

    public void line() {
        char c = '-';
        line(c);
    }

    public void line(char c) {
        IOColors.OutputType ot;
        if (storeList.isEmpty()) {
            ot = IOColors.OutputType.LOG_SUCCESS;

        } else {
            ot = storeList.get(storeList.size() - 1).getColorType();
        }
        line(ot, c);
    }

    public void line(IOColors.OutputType ot, char c) {
        String s = Character.toString(c);
        StringBuilder sb = new StringBuilder(55);
        for (int i = 0; i < 55; i++) {
            sb.append(s);
        }
        String msgs = sb.toString();
        storeList.add(new Store(ot, msgs));
    }

    protected static class Store {

        private final String[] messages;
        private final IOColors.OutputType colorType;

        public Store(IOColors.OutputType ot, String... msgs) {
            this.colorType = ot;
            this.messages = msgs;
        }

        public IOColors.OutputType getColorType() {
            return colorType;
        }

        public String[] getMessages() {
            return messages;
        }

    }

}
