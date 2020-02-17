/*
 * ProcessorAgent.java
 *
 * Created on March 28, 2006, 11:59 AM
 *
 * Copyright (C) 2006 M Litherland
 */

package org.nule.integrateclient.common;

import java.util.*;

/**
 *
 * @author litherm
 */
public abstract class ProcessorAgent {
    
    /**
     * dataTransfer is called by inbound components to hand control of
     * a message over to the outbound components.
     */
    public abstract boolean dataTransfer(String msg);
    
    /**
     * Pass the client properties to load during startup.
     */
    public abstract void loadProperties(Properties p) throws IllegalArgumentException;
    
    /**
     * Return a list of mandatory properties.
     */
    public static Properties getMandatory() {
        return null;
    }
    
    /**
     * Return a list of optional properties
     */
    public static Properties getOptional() {
        return null;
    }
    
    /**
     * May return a list of default or recommended values.
     */
    public static Properties getDefaults() {
        return null;
    }
    
    /**
     * Return a description of the type requested of each) property.  For
     * example string, integer, char, class, etc.
     */
    public static Properties getTypes() {
        return null;
    }
    
    /**
     * Return a description of the property for assistance in filling out
     * the configuration.
     */
    public static Properties getDescriptions() {
        return null;
    }
    
    /**
     * Returns the current config of the agent.
     */
    public abstract Properties getConfig();
    
    /**
     * This is the last error stored by the agent.  Also clears the error.
     */
    public abstract String getLastError();
    
    /**
     * Returns stats for each minute of the last 24 hours.
     */
    public abstract int[] getStats();
    
    /**
     * Returns the total transactions processed count.
     */
    public abstract long getTotal();
    
    /**
     * Returns whether or not the argent has been configured.
     */
    public abstract boolean isConfigured();
    
    /**
     * Returns whether or not the agent is running.
     */
    public abstract boolean isRunning();
    
    /**
     * Causes the agent to be reconfigured with the specified properties.
     */
    public abstract boolean reconfigure(Properties p);
    
    /**
     * Causes the agent to be reconfigured with the specified properties.
     */
    public abstract boolean reconfigure(ConfigurationMap m);
    
    /**
     * Causes the agent to trigger all clients to start.
     */
    public abstract boolean start();
    
    /**
     * Causes the agent to trigger all clients to stop.
     */
    public abstract boolean stop();
    
}
