/*
 * NullLogger.java
 *
 * Created on March 30, 2006, 9:02 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.nule.integrateclient.common;

/**
 *
 * @author litherm
 *
 * NullLogger is a logger that does nothing.  It exists because even the clients
 * that output the logs generate log messages and if they are set too verbosely
 * it could lead to a nasty bit of recursion.
 */
public class NullLogger extends Logger {
    
    /** Creates a new instance of NullLogger */
    public NullLogger() {
    }
    
    /**
     * Add a client for the logger.
     */
    public void addClient(OutboundClient oc) {}
    
    /**
     * Remove the specified client from the logger.
     */
    public void removeClient(OutboundClient oc) {}
    
    /**
     * Log an error.
     */
    public void error(String msg) {}
    
    /**
     * Log some info.
     */
    public void info(String msg) {}
    
    /**
     * Log some debug.
     */
    public void debug(String msg) {}
    
    /**
     * Log some warn.
     */
    public void warn(String msg) {}
    
    /**
     * Log some trace.
     */
    public void trace(String msg) {}
}
