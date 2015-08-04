/**
 * This file is part of Base JEE Server support in NetBeans IDE.
 *
 * Base JEE Server support in NetBeans IDE is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * Base JEE Server support in NetBeans IDE is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should see the GNU General Public License here:
 * <http://www.gnu.org/licenses/>.
 */
package org.netbeans.modules.jeeserver.base.deployment.jsp;

import java.io.File;
import org.netbeans.modules.jeeserver.base.deployment.BaseDeploymentManager;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseConstants;

/**
 * The abstract class allows the plugin to specify the location of 
 * {@code servlets} generated for JSPs.
 * Each specific server can extend this class in order to enable JSP 
 * support.
 * 
 * @author V. Shyshkin
 */
public abstract class BaseFindJSPServlet { 

    protected static final String WEB_INF_TAGS = "WEB-INF/tags/";
    protected static final String META_INF_TAGS = "META-INF/tags/";
    
    protected BaseDeploymentManager manager;
    /**
     * Creates a new instance of the class for the given {@literal DeploymentManager}
     * @param manager
     */
    protected BaseFindJSPServlet(BaseDeploymentManager manager) {
        this.manager = manager;
    }

    /**
     * Returns the temporary directory where the server writes servlets 
     * generated from JSPs. 
     * The servlets placed in this directory must honor the Java directory 
     * naming conventions, i.e. the servlet must be placed in 
     * subdirectories of this directory corresponding to the servlet 
     * package name.
     * 
     * @param moduleContextPath  web module context path
     * @return the root temp directory containing servlets generated from 
     * JSPs for this module.
     */
    //@override
    public File getServletTempDirectory(String moduleContextPath) {
        String port = manager.getInstanceProperties().getProperty(BaseConstants.HTTP_PORT_PROP);

        String convertedContextPath = moduleContextPath.replace('/', '_');
        convertedContextPath = convertedContextPath.replace('\\', '_');
        convertedContextPath += "-";

        File baseDir = getWorkDirByServerHome();
        File jspDir = null;

        if (baseDir != null) {
            jspDir = getJspDir(baseDir, convertedContextPath, port);
        }
        if (jspDir != null) {
            return jspDir;
        }
        
        baseDir = new File(System.getProperty("java.io.tmpdir"));
        if (!baseDir.exists()) {
            return null;
        }
        return getJspDir(baseDir, convertedContextPath, port);
    }
    //TO SPECIFICS
    protected abstract File getJspDir(File baseDir, String convertedContextPath, String port);
    
    //TO SPECIFICS
    protected abstract File getWorkDirByServerHome();
    /**
     * Returns the resource path of the servlet generated for a particular JSP,
     * relatively to the main temporary directory.
     * @param moduleContextPath context path of web module in which the JSP is located.
     * @param jspResourcePath  the path of the JSP for which the servlet 
     * is requested, e.g. "pages/login.jsp". Never starts with a '/'.
     * 
     * @return the resource name of the servlet generated for the JSP in the 
     * module, e.g. "org/apache/jsps/pages/login$jsp.java". 
     * Must never start with a '/'. The servlet file itself does not need to 
     * exist at this point - if this particular page was not compiled yet.
     */
    //@override
    public String getServletResourcePath(String moduleContextPath, String jspResourcePath) {
        
        //we expect .tag file; in other case, we expect .jsp file
        String path = getTagHandlerClassName(jspResourcePath);
        if (path != null) //.tag
            path = path.replace('.', '/') + ".java";
        else //.jsp
            path = getServletPackageName(jspResourcePath).replace('.', '/') + '/' +
                   getServletClassName(jspResourcePath) + ".java";
        return path;
    }

    
    public String getServletEncoding(String moduleContextPath, String jspResourcePath) {
        return "UTF8";
    }
    /**
     * Copied (and slightly modified) from org.apache.jasper.compiler.JspUtil
     *
     * Gets the fully-qualified class name of the tag handler corresponding to
     * the given tag file path.
     *
     * @param path Tag file path
     *
     * @return Fully-qualified class name of the tag handler corresponding to 
     * the given tag file path
     */
    protected String getTagHandlerClassName(String path) {

        String className;
        int begin;
        int index;
        
        index = path.lastIndexOf(".tag");
        if (index == -1) {
            return null;
        }

        index = path.indexOf(WEB_INF_TAGS);
        if (index != -1) {
            className = "org.apache.jsp.tag.web.";
            begin = index + WEB_INF_TAGS.length();
        } else {
	    index = path.indexOf(META_INF_TAGS);
	    if (index != -1) {
		className = "org.apache.jsp.tag.meta.";
		begin = index + META_INF_TAGS.length();
	    } else {
		return null;
	    }
	}

        className += JspNameUtil.makeJavaPackage(path.substring(begin));
  
        return className;
    }
    
    // copied from org.apache.jasper.JspCompilationContext
    protected String getDerivedPackageName(String jspUri) {
        int iSep = jspUri.lastIndexOf('/');
        return (iSep > 0) ? JspNameUtil.makeJavaPackage(jspUri.substring(0,iSep)) : "";
    }
    // copied from org.apache.jasper.JspCompilationContext
    public String getServletPackageName(String jspUri) {
        String dPackageName = getDerivedPackageName(jspUri);
        if (dPackageName.length() == 0) {
            return JspNameUtil.JSP_PACKAGE_NAME;
        }
        return JspNameUtil.JSP_PACKAGE_NAME + '.' + getDerivedPackageName(jspUri);
    }
    // copied from org.apache.jasper.JspCompilationContext
    public String getServletClassName(String jspUri) {
        int iSep = jspUri.lastIndexOf('/') + 1;
        return JspNameUtil.makeJavaIdentifier(jspUri.substring(iSep));
    }
    
}
