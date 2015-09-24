/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jeeserver.base.embedded.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.ProjectGroupChangeEvent;
import org.netbeans.api.project.ui.ProjectGroupChangeListener;
import org.netbeans.modules.jeeserver.base.deployment.utils.BaseUtils;
import org.openide.util.Exceptions;

/**
 *
 * @author Valery
 */
public class OpenProjectListeners {
    
    public static class PropertiesListener implements PropertyChangeListener{
    public Object getProjectDirectory(Object p) {
                Object r = null;
                Method method;
                
                try {
                    Method[] ms = p.getClass().getMethods();
                    for ( Method  m : ms) {
                        BaseUtils.out(" --- method: " + m.getName());
                    }
                    method = p.getClass().getMethod("getProjectDirectory", (Class<?>[]) null);
                    r = method.invoke(p, new Object[] {} );
                    BaseUtils.out("--- !!! dir=" + r);            
                } catch (NoSuchMethodException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (SecurityException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IllegalAccessException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IllegalArgumentException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (InvocationTargetException ex) {
                    Exceptions.printStackTrace(ex);
                }
        return r;
    }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String oldv = "" + evt.getOldValue();
            String newv = "" + evt.getNewValue();
            String oldclassname = null;
            String newclassname = null;
            BaseUtils.out("===================================================================");            
            BaseUtils.out("PropertiesListener : propName=" + evt.getPropertyName());            
            if ( evt.getOldValue() != null  ) {
                oldclassname = evt.getOldValue().getClass().getName() + "; isProject=" + (evt.getOldValue() instanceof Project);
                BaseUtils.out("----old dir =" + getProjectDirectory(evt.getOldValue()));
            }
            if ( evt.getNewValue() != null  ) {
                newclassname = evt.getNewValue().getClass().getName() + "; isProject=" + (evt.getNewValue() instanceof Project);
                BaseUtils.out("----new dir =" + getProjectDirectory(evt.getNewValue()));                
            }
            BaseUtils.out("   ----PropertiesListener : oldClassName=" + oldclassname);            
            BaseUtils.out("   ----PropertiesListener : newClassName=" + newclassname); 
            if ( evt.getOldValue() != null && (evt.getOldValue() instanceof Project) ) {
                oldv = ((Project)evt.getOldValue()).getProjectDirectory().getNameExt();
            }
            if ( evt.getNewValue() != null && (evt.getNewValue() instanceof Project) ) {
                newv = ((Project)evt.getNewValue()).getProjectDirectory().getNameExt();
            }
            
            BaseUtils.out("PropertiesListener : propName" + evt.getPropertyName() +
                    "; oldv=" + oldv 
                    + "; newv=" + newv); 
        }
    }   
    
}
