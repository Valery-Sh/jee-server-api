/**
 * This file is part of Jetty Server suppport in NetBeans IDE.
 *
 * Jetty Server suppport in NetBeans IDE is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * Jetty Server suppport in NetBeans IDE is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should see the GNU General Public License here:
 * <http://www.gnu.org/licenses/>.
 */
package org.jetty.custom.handlers;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ShutdownHandler;

/**
 * For test purpose only.
 * @author V. Shyshkin
 */
public class CustomShutdownHandler extends ShutdownHandler {

    public CustomShutdownHandler(String shutdownToken) {
        super(shutdownToken);
    }
    private boolean isCorrectSecurityToken(HttpServletRequest request)
    {
        String tok = request.getParameter("token");
        return getShutdownToken().equals(tok);
    }
    private void doShutdownServer(Server server) throws Exception
    {
        server.stop();

        if (true )
        {
            System.exit(0);
        }
    }
    private boolean doRequestFromLocalhost(HttpServletRequest request)
    {
//        return "127.0.0.1".equals(getRemoteAddr(request));
        return false;
    }
    
        @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
        System.out.println("CustomShutdownHandler1: target=" + target );
        if (!target.equals("/shutdown"))
        {
            super.handle(target,baseRequest,request,response);
            return;
        }

        if (!request.getMethod().equals("POST"))
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        if (!isCorrectSecurityToken(request))
        {
//            System.out.println("Unauthorized shutdown attempt from " + getRemoteAddr(request));
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        if (!doRequestFromLocalhost(request))
        {
//            System.out.println("Unauthorized shutdown attempt from " + getRemoteAddr(request));
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

//        System.out.println("Shutting down by request from " + getRemoteAddr(request));

        final Server server=getServer();
        new Thread()
        {
            @Override
            public void run ()
            {
                try
                {
                    doShutdownServer(server);
                }
                catch (InterruptedException e)
                {
                    System.out.println("CustomShutdownHandler EXCEPTION1" + e.getMessage());
                }
                catch (Exception e)
                {
                    System.out.println("CustomShutdownHandler EXCEPTION2" + e.getMessage());
                    
                    throw new RuntimeException("Shutting down server",e);
                }
            }
        }.start();
        
    }
}
