/*
 * SlowdownLogicAgent.java
 *
 * Created on June 22, 2006, 2:40 PM
 *
 * Copyright 2006-8 M. Litherland
 */

package org.nule.integrateclient.samples;

import java.util.*;

import org.nule.integrateclient.common.*;

/**
 *
 * @author mike
 */
public class SlowdownLogicAgent extends LogicAgent {
    
    public static final String DESCRIPTION = "This simple agent does no processing " +
            "to the data and always returns true on the load statement.  The one " +
            "catch is that it sleeps a specified number of milliseconds before " +
            "allowing processing to continue.  It therefore guarantees messages " +
            "will be processed no faster than a specified rate.";
    
    private int timeToSleep;
    
    public SlowdownLogicAgent(String name, Logger logger) {
        super(name, logger);
    }
    
    private String message;

    public boolean loadMessage(String message) {
        this.message = message;
        try {
            Thread.sleep(timeToSleep);
        } catch (InterruptedException e) {
            // Oh well, we're interrupted.
        }
        return true;
    }

    public String getProcessed() {
        return message;
    }

    public static Properties getDefaults() {
        Properties p = new Properties();
        p.setProperty("timeToSleep", "1000");
        return p;
    }

    public static Properties getMandatory() {
        Properties p = new Properties();
        p.setProperty("timeToSleep", "");
        return p;
    }

    public static Properties getOptional() {
        return null;
    }

    public void loadProperties(Properties p) throws IllegalArgumentException {
        try {
            timeToSleep = Integer.parseInt(p.getProperty("timeToSleep"));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid argument");
        }
    }

    public static Properties getTypes() {
        Properties p = new Properties();
        p.setProperty("timeToSleep", FieldTypes.INTEGER);
        return p;
    }
    
    public static Properties getDescriptions() {
        Properties p = new Properties();
        p.setProperty("timeToSleep", "The time in milliseconds to sleep between messages.");
        return p;
    }
    
}
