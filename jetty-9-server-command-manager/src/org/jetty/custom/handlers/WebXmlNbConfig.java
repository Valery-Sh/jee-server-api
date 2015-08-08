/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jetty.custom.handlers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.EnumSet;
import java.util.EventListener;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import org.eclipse.jetty.webapp.AbstractConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 *
 * @author Valery
 */
public class WebXmlNbConfig extends AbstractConfiguration {

    /* ------------------------------------------------------------------------------- */
    /**
     *
     * @param context
     * @throws java.lang.Exception
     */
    @Override
    public void preConfigure(WebAppContext context) throws Exception {
        Map<String, ? extends FilterRegistration> srf = (Map<String, FilterRegistration>) context.getServletContext().getFilterRegistrations();
        int n = 0;
        if (srf != null) {
            n = srf.size();
        }
//        System.out.println(" ------------ PRECONFIGURE JSF FILTER: filter count=" + n);

        EnumSet<DispatcherType> es = EnumSet.of(DispatcherType.REQUEST);
        context.addFilter(JsfFilter.class, "/", es);
        //context.getServletContext().setInitParameter("org.eclipse.jetty.servlet.Default.welcomeServlets", "false");

        //context.setParentLoaderPriority(true);
        //
        // webapp cannot change / replace jsf classes        
        //
        context.addSystemClass("com.sun.faces.");
        context.addSystemClass("javax.faces.");
        context.addSystemClass("com.google.common.");
        //
        // don't hide jsf classes from webapps 
        // (allow webapp to use ones from system classloader)        
        //
        context.prependServerClass("-com.sun.faces.");
        context.prependServerClass("-javax.faces.");
        context.prependServerClass("-com.google.common.");

        System.out.println("PRE CONFIGURE isStarting=" + context.isStarting());
        System.out.println("PRE CONFIGURE isStarted=" + context.isStarted());
        
        System.out.println("POST CONFIGURE isStarting=" + context.isStarting());
        System.out.println("POST CONFIGURE isStarted=" + context.isStarted());
        //
        // add config listener for an active jsf module
        //
        String className = IniModules.getJsfListenerClassName();

        if (className != null) {
            addJsfServletContextListener(context, className);
        }
        
        //org.apache.myfaces.webapp.StartupServletContextListener ll;

    }

    protected void addJsfServletContextListener(WebAppContext context, String className) {
        EventListener[] listeners = context.getEventListeners();
        boolean found = false;
        if (listeners != null) {
            for (EventListener l : listeners) {
                System.out.println("addServletContextListener POSTCONFIGURE l=" + l.getClass().getName());
                if (className.equals(l.getClass().getName())) {
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            context.getServletContext().addListener(className);
            System.out.println("Add JSF Config listener class = " + className);

        }
    }

    /* ------------------------------------------------------------------------------- */
    /**
     * Process web-default.xml, web.xml, override-web.xml
     *
     * @param context
     */
    @Override
    public void configure(WebAppContext context) throws Exception {
    }

    /* ------------------------------------------------------------------------------- */
    protected Resource findWebXml(WebAppContext context) throws IOException, MalformedURLException {
        return null;
    }


    /* ------------------------------------------------------------------------------- */
    @Override
    public void deconfigure(WebAppContext context) throws Exception {
    }

    @Override
    public void postConfigure(WebAppContext context) throws Exception {
        System.out.println("POST CONFIGURE isStarting=" + context.isStarting());
        System.out.println("POST CONFIGURE isStarted=" + context.isStarted());
        
        String className = IniModules.getJsfListenerClassName();        
        EventListener[] listeners = context.getEventListeners();
        boolean found = false;
        if (listeners != null) {
            for (EventListener l : listeners) {
                System.out.println("addServletContextListener POSTCONFIGURE class = " + l.getClass().getName());
                if (className.equals(l.getClass().getName())) {
                    found = true;
//                    break;
                }
            }
        }
        if (!found) {
            System.out.println("NOT FOUND Add JSF Config listener class = " + className);
        } else {
            System.out.println("!!! FOUND Add JSF Config listener class = " + className);
        }

    }

    public static void welcome(WebAppContext context, String stage) {
        String[] wf = context.getWelcomeFiles();
        if (wf != null) {
            for (String s : wf) {
                System.out.println(stage + " WebAppLifeCycleListener welcomeFile=" + s);
            }
        }

    }

}
