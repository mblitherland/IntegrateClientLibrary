/*
 * LoggerSingleton.java
 *
 * Created on March 28, 2006, 11:50 AM
 *
 * Copyright (C) 2006 M Litherland
 */

package org.nule.integrateclient.common;

/**
 *
 * @author litherm
 */
public class LoggerSingleton {
    
    private static volatile Logger me;
    
    /** Creates a new instance of LoggerSingleton */
    private LoggerSingleton() {
    }
    
    /**
     * Return the instance of the logger.
     */
    public static Logger getLogger() {
        if (me == null) {
            me = new Logger();
        }
        return me;
    }
    
}
