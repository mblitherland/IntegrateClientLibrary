/*
 * ConfigurationMap.java
 * 
 * Copyright (C) 2006-2012 M. Litherland
 */

package org.nule.integrateclient.common;

import java.util.*;

/**
 *
 * @author litherm
 */
public class ConfigurationMap<K, V> extends HashMap<K, V> {
    
    private Properties config;
    
    public void setConfig(Properties config) {
        this.config = config;
    }
    
    public void addConfig(String key, String value) {
        if (config == null) {
            config = new Properties();
        }
        config.setProperty(key, value);
    }
    
    public Properties getConfig() {
        if (config == null) {
            config = new Properties();
        }
        return config;
    }
    
}
