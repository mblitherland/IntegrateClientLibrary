/*
 * IntegrateClient.java
 *
 * Created on March 28, 2006, 11:22 AM
 *
 * Copyright (C) 2004-2006 M Litherland
 */

package org.nule.integrateclient.common;

import java.util.Properties;

/**
 *
 * @author litherm
 */
public abstract class IntegrateClient implements Runnable {
    
    public static final String DESCRIPTION = "Please supply a description for this client.";
    
    public IntegrateClient(String name, Logger logger, ProcessorAgent pa) {
    }
    
    public IntegrateClient(String name, Logger logger, ProcessorAgent pa, Properties p) {
    }
    
    /**
     * Return all the associated status information.
     */
    public abstract Properties getStatus();

    /**
     * Returns, where applicable, a connected status of true or false.  If
     * not applicable it will return none.
     */
    public abstract Boolean isConnected();
    
    /**
     * Return a one line status for the client.
     */
    public abstract String getState();
    
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
     * Tell the client to stop processing.
     */
    public abstract boolean stopClient();
    
    /**
     * Tell the client to start processing.
     */
    public abstract boolean startClient();
    
    /**
     * Return whether or not the client is running.
     */
    public abstract boolean isRunning();
    
    /**
     * Return the message count.
     */
    public abstract long getCount();
    
}
