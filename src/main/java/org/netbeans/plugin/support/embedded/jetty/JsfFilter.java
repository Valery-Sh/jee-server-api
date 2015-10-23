/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.plugin.support.embedded.jetty;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 *
 * @author Valery
 */
public class JsfFilter implements Filter{

    @Override
    public void init(FilterConfig fc) throws ServletException {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain fc) throws IOException, ServletException {
        System.out.println("JSF FILTER");
        WebAppContext ctx = WebAppContext.getCurrentWebAppContext();
        String[] welcomes= ctx.getWelcomeFiles();
        if ( welcomes == null ) {
            fc.doFilter(req, res);
            return;
        }
        String faces = null;
        for ( String w : welcomes) {
            if ( w.startsWith("faces/")) {
                faces = w;
                break;
            }
        }
        if ( faces == null ) {
            fc.doFilter(req, res);
            return;
        }
        
        System.out.println("welcomes="+welcomes);
        
        System.out.println("JSF FILTER current ctx cp=" + ctx.getContextPath());
        
        HttpServletRequest hreq = (HttpServletRequest) req;
        System.out.println("JSF FILTER getContexpPath=" + hreq.getContextPath());
 
        System.out.println("JSF FILTER getRequestURI=" + hreq.getRequestURI());
        String uri = hreq.getRequestURI(); 
        String cp = hreq.getContextPath();
        if ( ! (cp+"/").equals(uri)) {
            fc.doFilter(req, res);
            return;
        }
        //((HttpServletResponse)res).sendRedirect(((HttpServletRequest)req).getContextPath() + "/faces/index.xhtml");
        fc.doFilter(req, res); 
        hreq.getRequestDispatcher(faces).forward(req, res);
        //fc.doFilter(req, res);
    }

    @Override
    public void destroy() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
