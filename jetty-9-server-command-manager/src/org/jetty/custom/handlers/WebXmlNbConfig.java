/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jetty.custom.handlers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.EnumSet;
import java.util.Enumeration;
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
public class WebXmlNbConfig extends AbstractConfiguration
{
    
    /* ------------------------------------------------------------------------------- */
    /**
     * 
     * @param context
     * @throws java.lang.Exception
     */
    @Override
    public void preConfigure (WebAppContext context) throws Exception
    {
        Map<String, ? extends FilterRegistration> srf = (Map<String, FilterRegistration>) context.getServletContext().getFilterRegistrations();
        int n = 0;
        if ( srf != null ) {
            n = srf.size();
        }
//        System.out.println(" ------------ PRECONFIGURE JSF FILTER: filter count=" + n);
        
        EnumSet<DispatcherType> es = EnumSet.of(DispatcherType.REQUEST);
        context.addFilter(JsfFilter.class, "/", es); 

        context.setParentLoaderPriority(true);
        context.addSystemClass("com.sun.faces.");
        context.addSystemClass("javax.faces.");
        
    }

    /* ------------------------------------------------------------------------------- */
    /**
     * Process web-default.xml, web.xml, override-web.xml
     * 
     */
    @Override
    public void configure (WebAppContext context) throws Exception
    {
        System.out.println("CONFIGURE: MY");        
//        EnumSet<DispatcherType> es = EnumSet.of(DispatcherType.FORWARD);
        
//        context.addFilter(JsfFilter.class, "/", es);        
    }
    
    /* ------------------------------------------------------------------------------- */
    protected Resource findWebXml(WebAppContext context) throws IOException, MalformedURLException
    {
        return null;
    }


    /* ------------------------------------------------------------------------------- */
    @Override
    public void deconfigure (WebAppContext context) throws Exception
    {      
    }
}

