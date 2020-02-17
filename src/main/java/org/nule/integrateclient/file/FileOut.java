/*
 * FileOut.java
 *
 * Created on March 28, 2006, 3:41 PM
 *
 * Copyright (C) 2004-2012 M Litherland
 */

package org.nule.integrateclient.file;

import java.io.*;
import java.util.*;
import org.nule.integrateclient.common.*;

/**
 *
 * @author litherm
 *
 * A simple file out client that writes to a single file.  Also the base
 * for most other file out clients.
 */
public class FileOut extends IntegrateClient implements OutboundClient {
    
    public static final String DESCRIPTION = "The file out client writes output " +
            "to a single file.  It is primarily used only as a base for other " +
            "file-based output classes and shouldn't be used as an outbound " +
            "client unless you really know what you're doing.";
    
    protected String fileName;
    
    protected boolean running;
    
    protected String status = "FileOut not initialized.";
    
    private ProcessorAgent pa;
    
    protected Logger logger = null;
    
    protected long count;
    
    protected Thread t;
    
    protected String message;
    
    protected boolean available = false;
    
    protected String name;
    
    public FileOut(String name, Logger logger, ProcessorAgent pa) {
        super(name, logger, pa);
        this.name = name;
        this.logger = logger;
        this.pa = pa;
    }
    
    public FileOut(String name, Logger logger, ProcessorAgent pa, Properties p) {
        super(name, logger, pa, p);
        this.name = name;
        this.logger = logger;
        this.pa = pa;
        loadProperties(p);
    }
    
    public static Properties getMandatory() {
        Properties p = new Properties();
        p.setProperty("fileName", "");
        return p;
    }
    
    public static Properties getOptional() {
        return null;
    }
    
    public static Properties getDefaults() {
        Properties p = new Properties();
        p.setProperty("fileName", "filetowrite.txt");
        return p;
    }
    
    public static Properties getTypes() {
        Properties p = new Properties();
        p.setProperty("fileName", FieldTypes.FILE);
        return p;
    }
    
    public static Properties getDescriptions() {
        Properties p = new Properties();
        p.setProperty("fileName", "A file to write to.");
        return p;
    }
    
    public void loadProperties(Properties p) {
        fileName = p.getProperty("fileName");
        if (fileName == null) {
            logger.error(name + ": fileName must be provided.");
            throw new IllegalArgumentException("Invalid argument");
        }
        status = "FileOut configured.";
    }
    
    public Properties getStatus() {
        logger.info(name + ": Handling request for status.");
        Properties p = new Properties();
        p.put("fileName", fileName);
        p.put("status", status);
        p.put("count", Long.toString(count));
        return p;
    }

    public Boolean isConnected() {
        return null;
    }
    
    public boolean startClient() {
        // Ensure the fileName property has been set.
        if (fileName == null) {
            return false;
        }
        if (isRunning()) {
            stopClient();
        }
        t = new Thread(this);
        t.start();
        return true;
    }
    
    public boolean stopClient() {
        status = "FileOut shutting down.";
        logger.info(name + ": Client has been told to shutdown.");
        running = false;
        if (t != null && t.isAlive()) {
            t.interrupt();
        }
        t = null;
        status = "FileOut shut down.";
        return true;
    }
    
    public boolean isRunning() {
        return running;
    }
    
    public String getState() {
        return status;
    }
    
    /**
     * Implement OutboundClient, which allows this to be a logger, among
     * other things.
     */
    public synchronized void putMessage(String msg) {
        while (available == true) {
            try {
                logger.trace(name + " waiting on put...");
                wait();
            } catch (InterruptedException e) {
            }
        }
        message = msg;
        available = true;
        notifyAll();
    }
    
    protected synchronized String getMessage() {
        while (available == false) {
            try {
                wait();
            } catch (InterruptedException e) {
                return null;
            }
        }
        available = false;
        notifyAll();
        return message;
    }
    
    public long getCount() {
        return count;
    }
    
    public void run() {
        running = true;
        status = "FileOut running.";
        File f = new File(fileName);
        FileWriter fw = null;
        try {
            logger.info(name + ": Client starting.");
            fw = new FileWriter(f);
            while (running) {
                status = "Waiting for message.";
                // This will blocking wait()ing for message.
                String m = getMessage();
                if (m == null) {
                    continue;
                }
                count++;
                logger.info(name + ": "+m);
                fw.write(m+"\n");
                fw.flush(); // This might be a bit much...
            }
        } catch (IOException e) {
            status = "FileOut IO exception.";
            logger.error(name + ": IOException writing to "+f);
        } catch (Exception e) {
            status = "FileOut Exception - "+e;
            logger.error(name = ": Exception - "+e);
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e) { /* we gave it our best */ }
            }
            running = false;
        }
    }
    
}
