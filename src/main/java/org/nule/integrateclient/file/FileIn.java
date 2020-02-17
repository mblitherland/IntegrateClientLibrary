/*
 * FileIn.java
 *
 * Copyright (C) 2004-2008 M Litherland
 */

package org.nule.integrateclient.file;

import java.io.*;
import java.util.*;

import org.nule.integrateclient.common.*;
import org.nule.lighthl7lib.util.LineReader;

/**
 *
 * @author litherm
 *
 * This class operates on files to read them in line by line, optionally
 * filtering them, then make them available for another class to pick up
 * and handle.
 */
public class FileIn extends IntegrateClient implements InboundClient {
    
    public static final String DESCRIPTION = "This simple file in client reads in " +
            "from the file specified, then exits when complete.  It assumes " +
            "records are delimited by at least a new line.";
    
    protected String name;
    
    protected String fileName;
    
    protected int sendFileCount;
    
    protected Thread t;
    
    protected boolean running;
    
    protected String status = "FileIn not initialized.";
    
    protected long count;
    
    protected Logger logger;
    
    protected ProcessorAgent pa;
    
    public FileIn(String name, Logger logger, ProcessorAgent pa) {
        super(name, logger, pa);
        this.name = name;
        this.logger = logger;
        this.pa = pa;
    }
    
    public FileIn(String name, Logger logger, ProcessorAgent pa, Properties p) {
        super(name, logger, pa, p);
        this.name = name;
        this.logger = logger;
        this.pa = pa;
        loadProperties(p);
    }
    
    public static Properties getMandatory() {
        Properties p = new Properties();
        p.put("fileName", "");
        return p;
    }
    
    public static Properties getOptional() {
        Properties p = new Properties();
        p.put("sendFileCount", "");
        return p;
    }
    
    public static Properties getDefaults() {
        Properties p = new Properties();
        p.put("fileName", "filetoread.txt");
        p.put("sendFileCount", "1");
        return p;
    }
    
    public static Properties getTypes() {
        Properties p = new Properties();
        p.put("fileName", FieldTypes.FILE);
        p.put("sendFileCount", FieldTypes.INTEGER);
        return p;
    }
    
    public static Properties getDescriptions() {
        Properties p = new Properties();
        p.put("fileName", "A file to read in.");
        p.put("sendFileCount", "The number of times to send the specified file.");
        return p;
    }
    
    public void loadProperties(Properties p) {
        fileName = p.getProperty("fileName");
        if (fileName == null) {
            logger.error(name + ": fileName must be provided.");
            throw new IllegalArgumentException("Invalid argument");
        }
        sendFileCount = 1;
        if (p.getProperty("sendFileCount") != null && !"".equals(p.getProperty("sendFileCount"))) {
            try {
                sendFileCount = Integer.parseInt(p.getProperty("sendFileCount"));
            } catch (NumberFormatException e) {
                logger.warn(name + ": Must provide integer for sendFileCount.");
            }
        }
        status = "FileIn configured.";
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
        // Confirm that fileName as been configured
        if (fileName == null)
            return false;
        status = "FileIn starting.";
        logger.info(name + ": Client has been told to start.");
        if (isRunning())
            stopClient();
        t = new Thread(this);
        t.start();
        return true;
    }
    
    public boolean stopClient() {
        status = "FileIn shutting down.";
        logger.info(name + ": Client has been told to shutdown.");
        running = false;
        if (t != null && t.isAlive())
            t.interrupt();
        t = null;
        status = "FileIn shut down.";
        return true;
    }
    
    public boolean isRunning() {
        return running;
    }
    
    public String getState() {
        return status;
    }
    
    public long getCount() {
        return count;
    }
    
    public void run() {
        running = true;
        status = "FileIn running.";
        File f = new File(fileName);
        try {
            logger.info(name + ": Client starting.");
            for (int i = 0; i < sendFileCount; i++) {
                FileReader fr = new FileReader(f);
                LineReader lr = new LineReader(fr);
                while (running){
                    String data = lr.readLine();
                    if (data == null) {
                        status = "FileIn reached end of file";
                        logger.debug(name + ": Reached end of file");
                        break;
                    }
                    count++;
                    logger.debug(name + ": Read record");
                    logger.info(name + ": "+data);
                    logger.trace(name + ": File in to dataTransfer");
                    pa.dataTransfer(data);
                    logger.trace(name + ": File in completed dataTransfer");
                }
                fr.close();
            }
        } catch (FileNotFoundException e) {
            status = "FileIn file not found exception.";
            logger.error(name + ": File "+f+" not found.");
        } catch (IOException e) {
            status = "FileIn IO exception.";
            logger.error(name + ": IOException reading "+f);
        } catch (Exception e) {
            status = "Some other exception in run loop.";
            logger.error(name + ": Some other exception - "+e);
        } finally {
            running = false;
        }
    }
}
