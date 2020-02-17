/*
 * StandardOut.java
 *
 * Created on March 31, 2006, 12:07 PM
 *
 * Copyright (C) 2005-2008 M Litherland
 */

package org.nule.integrateclient.file;

import org.nule.integrateclient.common.*;

import java.text.*;
import java.util.*;

/**
 *
 * @author litherm
 */
public class StandardOut extends FileOut {
    
    public static final String DESCRIPTION = "This simple client writes any " +
            "information to the JVM's standard out pipe.  It is primarily useful " +
            "for debugging or internal use.";
    
    private boolean showTimeStamp = false;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    
    public StandardOut(String name, Logger logger, ProcessorAgent pa) {
        super(name, logger, pa);
    }
    
    public StandardOut(String name, Logger logger, ProcessorAgent pa, Properties p) {
        super(name, logger, pa, p);
    }
    
    public static Properties getMandatory() {
        return null;
    }
    
    public static Properties getOptional() {
        Properties p = new Properties();
        p.setProperty("showTimeStamp", "");
        return p;
    }
    
    public static Properties getDefaults() {
        Properties p = new Properties();
        p.setProperty("showTimeStamp", "F");
        return p;
    }
    
    public static Properties getTypes() {
        Properties p = new Properties();
        p.setProperty("showTimeStamp", FieldTypes.BOOLEAN);
        return p;
    }
    
    public static Properties getDescriptions() {
        Properties p = new Properties();
        p.setProperty("showTimeStamp", "Display a time-stamp before each message.");
        return p;
    }
    
    public void loadProperties(Properties p) {
        status = "StandardOut configured.";
        if (p.getProperty("showTimeStamp") != null && 
                "T".equalsIgnoreCase(p.getProperty("showTimeStamp"))) {
            showTimeStamp = true;
        }
    }
    
    public Properties getStatus() {
        Properties p = new Properties();
        p.put("status", status);
        p.put("count", Long.toString(count));
        return p;
    }
    
    public boolean startClient() {
        // Ensure the logger has been set.
        if (logger == null)
            return false;
        status = "StandardOut starting.";
        if (isRunning())
            stopClient();
        t = new Thread(this);
        t.start();
        return true;
    }
    
    public void run() {
        running = true;
        status = "StandardOut running.";
        try {
            while (running) {
                status = "Waiting for message.";
                // This will blocking wait()ing for message.
                String m = getMessage();
                if (m == null)
                    continue;
                count++;
                if (showTimeStamp) {
                    System.out.print(sdf.format(new Date())+": ");
                }
                System.out.println(m);
            }
        } catch (Exception e) {
            status = "StandardOut exception - "+e;
            logger.error(name+": Exception - "+e);
        } finally {
            running = false;
        }
    }
}
