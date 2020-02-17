/*
 * LogicAgent.java
 *
 * Created on March 31, 2006, 2:44 PM
 *
 * Copyright (C) 2006 M Litherland
 */

package org.nule.integrateclient.common;

import java.util.*;

/**
 *
 * @author litherm
 */
public abstract class LogicAgent {
    
    public static final String DESCRIPTION = "Please supply a description for this logic.";
    
    public LogicAgent(String name, Logger logger) {
    }
    
    /**
     * Load a message into the agent for processing.  It returns true or false
     * based upon whether or not the processing within the processor agent
     * should continue.
     */
    public abstract boolean loadMessage(String message);
    
    /**
     * Return the processed message for handing downstream.
     */
    public abstract String getProcessed();
    
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
     * Return a description of the type requested of each property.  For
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
    
}
