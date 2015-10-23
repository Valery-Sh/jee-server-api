/**
 * This file is part of Jetty Server suppport in NetBeans IDE.
 *
 * Jetty Server suppport in NetBeans IDE is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the License,
 * or (at your option) any later version.
 *
 * Jetty Server suppport in NetBeans IDE is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should see the GNU General Public License here:
 * <http://www.gnu.org/licenses/>.
 */
package org.netbeans.jetty.server.support;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 *
 * @author V.Shyshkin
 */
public class JsfFilter implements Filter {

    @Override
    public void init(FilterConfig fc) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain fc) throws IOException, ServletException {
        System.err.println(" NB-DEPLOYER: JSF REDIRECT FILTER STARTING ");

        HttpServletRequest hreq = (HttpServletRequest) req;
        String uri = hreq.getRequestURI();
        String cp = hreq.getContextPath();
        if (!(cp + "/").equals(uri)) {
            fc.doFilter(req, res);
            return;
        }

        WebAppContext ctx = WebAppContext.getCurrentWebAppContext();
        String[] welcomes = ctx.getWelcomeFiles();
        if (welcomes == null || welcomes.length == 0) {
            fc.doFilter(req, res);
            return;
        }
        //
        // Search a FacesServlet
        //
        Map<String, ? extends ServletRegistration> srs = (Map<String, ServletRegistration>) ctx.getServletContext().getServletRegistrations();

        if (srs == null || srs.isEmpty()) {
            fc.doFilter(req, res);
            return;
        }
        Collection<String> regs = null; // Collection of mapping urls "/faces/* for example
        for (Map.Entry<String, ? extends ServletRegistration> en : srs.entrySet()) {
            if ("javax.faces.webapp.FacesServlet".equals(en.getValue().getClassName())) {
                regs = en.getValue().getMappings();
                break;
            }
        }
        if (regs == null || regs.isEmpty()) {
            fc.doFilter(req, res);
            return;
        }
        //
        // Search a FacesServlet
        //
        Map<String, ? extends FilterRegistration> srf = (Map<String, FilterRegistration>) ctx.getServletContext().getFilterRegistrations();
        //
        // Now we must take into account only patterns like /A/*
        //
        List<String> patterns = new ArrayList<>(regs.size());
        for (String s : regs) {
            if (s.length() < 4) {
                continue;
            }
            if (!(s.startsWith("/") || s.endsWith("/*"))) {
                continue;
            }
            patterns.add(s);
        }
        if (patterns.isEmpty()) {
            fc.doFilter(req, res);
            return;
        }

        //
        // Now we must scan all welcome files and try to match them with regs
        // The first one must be used for forwarding
        //
        String faces = null;

        for (String w : welcomes) {
            for (String m : patterns) {
                String p = m.substring(1, m.lastIndexOf("/*"));
                if (w.startsWith(p)) {
                    faces = w;
                    break;
                }
            }
            if (w != null) {
                break;
            }
        }

        if (faces == null) {
            fc.doFilter(req, res);
            return;
        }
        ((HttpServletResponse) res).sendRedirect(faces);

        //hreq.getRequestDispatcher(faces).forward(req, res);
    }

    @Override
    public void destroy() {
    }

}
