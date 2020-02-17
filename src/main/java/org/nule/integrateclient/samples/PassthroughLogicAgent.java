/*
 * PassthroughLogicAgent.java
 *
 * Created on March 31, 2006, 4:13 PM
 *
 * Copyright 2006 M. Litherland
 */

package org.nule.integrateclient.samples;

import java.util.*;

import org.nule.integrateclient.common.*;

/**
 *
 * @author litherm
 */
public class PassthroughLogicAgent extends LogicAgent {
    
    public static final String DESCRIPTION = "This simple agent does no processing " +
            "to the data and always returns true on the load statement.";
    
    public PassthroughLogicAgent(String name, Logger logger) {
        super(name, logger);
    }
    
    private String message;

    public boolean loadMessage(String message) {
        this.message = message;
        return true;
    }

    public String getProcessed() {
        return message;
    }

    public static Properties getDefaults() {
        return null;
    }

    public static Properties getMandatory() {
        return null;
    }

    public static Properties getOptional() {
        return null;
    }

    public void loadProperties(Properties p) throws IllegalArgumentException {
    }

    public static Properties getTypes() {
        return null;
    }
    
    public static Properties getDescriptions() {
        return null;
    }
}
