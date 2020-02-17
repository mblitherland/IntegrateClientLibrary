/*
 * Logger.java
 *
 * Copyright (C) 2004-2008 M Litherland
 */

package org.nule.integrateclient.common;

import java.text.*;
import java.util.*;

/**
 *
 * @author litherm
 */
public class Logger {
    
    public static final int ERROR = 0;
    public static final int WARN = 1;
    public static final int INFO = 2;
    public static final int DEBUG = 3;
    public static final int TRACE = 4;
    
    private int level = INFO;
    
    private List clients = new ArrayList();
    private List advancedClients = new ArrayList();
    
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    
    /**
     * Create a new Logger and initialize some instance variables.  This
     * class in some instances should be wrapped in a singleton pattern.
     */
    public Logger() {
    }
    
    /**
     * Set the debugging level.
     */
    public void setLevel(int level) {
        if (level < ERROR)
            this.level = ERROR;
        else if (level > TRACE)
            this.level = TRACE;
        else
            this.level = level;
    }
    
    /**
     * Allow the user to set the level by it's string name instead of
     * the number.  This isn't a particularly good idea, but helpful
     * for when the config loads from string data.
     */
    public void setLevel(String level) {
        debug("Logger received level change to "+level);
        if ("ERROR".equals(level)) this.level = ERROR;
        else if ("WARN".equals(level)) this.level = WARN;
        else if ("INFO".equals(level)) this.level = INFO;
        else if ("DEBUG".equals(level)) this.level = DEBUG;
        else if ("TRACE".equals(level)) this.level = TRACE;
        else this.level = INFO;
    }
    
    /**
     * Return the current debug level.
     */
    public int getLevel() {
        return level;
    }
    
    /**
     * Add a client for the logger.
     */
    public void addClient(OutboundClient oc) {
        debug("Logger adding client " + oc);
        clients.add(oc);
    }
    public void addClient(DetailedLoggerClient oc) {
        debug("Logger adding advanced client "+oc);
        advancedClients.add(oc);
    }
    
    /**
     * Remove the specified client from the logger.
     */
    public void removeClient(OutboundClient oc) {
        debug("Logger removing client " + oc);
        if (oc instanceof IntegrateClient) {
            IntegrateClient ic = (IntegrateClient) oc;
            ic.stopClient();
        }
        clients.remove(oc);
    }
    public void removeClient(DetailedLoggerClient oc) {
        debug("Logger removing client " + oc);
        if (oc instanceof IntegrateClient) {
            IntegrateClient ic = (IntegrateClient) oc;
            ic.stopClient();
        }
        advancedClients.remove(oc);
    }
    
    /**
     * Some processor agents need to get a list of all logging clients so they
     * can selective remove them.
     */
    public List getClients() {
        return clients;
    }
    
    public List getAdvancedClients() {
        return advancedClients;
    }
    
    /**
     * Remove all clients from the logger.
     */
    public void removeAllClients() {
        debug("Logger removing all clients...");
        clients.clear();
        advancedClients.clear();
    }
    
    /**
     * Log an error.
     */
    public void error(String msg, Exception e) {
        log(ERROR, msg);
        log(ERROR, e);
    }

    public void error(String msg) {
        log(ERROR, msg);
    }
    
    /**
     * Log some info.
     */
    public void info(String msg, Exception e) {
        log(INFO, msg);
        log(INFO, e);
    }

    public void info(String msg) {
        log(INFO, msg);
    }
    
    /**
     * Log some debug.
     */
    public void debug(String msg, Exception e) {
        log(DEBUG, msg);
        log(DEBUG, e);
    }

    public void debug(String msg) {
        log(DEBUG, msg);
    }
    
    /**
     * Log some warn.
     */
    public void warn(String msg, Exception e) {
        log(WARN, msg);
        log(WARN, e);
    }
    
    public void warn(String msg) {
        log(WARN, msg);
    }
    
    /**
     * Log some trace.
     */
    public void trace(String msg, Exception e) {
        log(TRACE, msg);
        log(TRACE, e);
    }

    public void trace(String msg) {
        log(TRACE, msg);
    }
    
    private void log(int l, Exception e) {
        StackTraceElement[] ste = e.getStackTrace();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ste.length; i++) {
            sb.append("\t");
            sb.append(ste[i].toString());
            sb.append("\n");
        }
        log(l, "Trace:\n"+sb.toString());
    }

    private void log(int l, String msg) {
        if (level < l) {
            return;
        }
        String levelText = null;
        switch (l) {
            case 0:
                levelText = "ERROR";
                break;
            case 1:
                levelText = "WARN";
                break;
            case 2:
                levelText = "INFO";
                break;
            case 3:
                levelText = "DEBUG";
                break;
            case 4:
                levelText = "TRACE";
                break;
            default:
                levelText = "NULL";
        }
        for (int i = 0; i < clients.size(); i++) {
            OutboundClient oc = (OutboundClient) clients.get(i);
            oc.putMessage(getTimeStamp()+" "+levelText+" "+msg);
        }
        for (int i = 0; i < advancedClients.size(); i++) {
            DetailedLoggerClient dlc = (DetailedLoggerClient) advancedClients.get(i);
            dlc.putMessage(new Date(), levelText, msg);
        }
    }
    
    /**
     * Return a standardized timestamp - "yyyy/MM/dd HH:mm:ss"
     */
    private String getTimeStamp() {
        return sdf.format(new Date());
    }
}
