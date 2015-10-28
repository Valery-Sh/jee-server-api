/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jeeserver.base.embedded.apisupport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Valery
 */
public abstract class AbstractSupportedApi implements SupportedApi{
    
    private String name;
    private String displayName;
    private String description;
    private boolean alwaysRequired;
    /**
     * key - is a jar name;
     * value - is an array of the dependency lines
     */
    private Map<String, String[]> jarMap;

    public Map<String, String[]> getJarMap() {
        if ( jarMap == null ) {
            jarMap = new HashMap<>();
        }
        return jarMap;
    }
    

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean isAlwaysRequired() {
        return alwaysRequired;
    }

    public void setAlwaysRequired(boolean alwaysRequired) {
        this.alwaysRequired = alwaysRequired;
    }
    
    
}
