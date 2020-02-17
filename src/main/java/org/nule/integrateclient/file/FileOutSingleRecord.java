/*
 * FileOutByDir.java
 *
 * Created on March 28, 2006, 4:21 PM
 *
 * Copyright (C) 2004-2008 M Litherland
 */

package org.nule.integrateclient.file;

import java.io.*;
import java.text.*;
import java.util.*;

import org.nule.integrateclient.common.*;

/**
 *
 * @author litherm
 *
 * FileOutByDir outputs files into a given directory at a specified interval,
 * guaranteeing a certain number of files per hour.  As soon as one file is
 * complete it is moved off to the done directory.
 *
 * At the moment, files are created even if they contain no data.  This should
 * not be the case, and will be addressed soon.
 */
public class FileOutSingleRecord extends FileOut {
    
    public static final String DESCRIPTION = "This client outputs files into a " +
            "completed or done directory.  This process will only create files that contain " +
            "a single record.";
    
    private static final int HOUR = 3600;
    
    private String doneDir;
    
    public FileOutSingleRecord(String name, Logger logger, ProcessorAgent pa) {
        super(name, logger, pa);
    }
    
    public FileOutSingleRecord(String name, Logger logger, ProcessorAgent pa, Properties p) {
        super(name, logger, pa, p);
    }
    
    public static Properties getMandatory() {
        Properties p = new Properties();
        p.setProperty("doneDir", "");
        return p;
    }
    
    public static Properties getOptional() {
        return null;
    }
    
    public static Properties getDefaults() {
        Properties p = new Properties();
        p.setProperty("doneDir", "done");
        return p;
    }
    
    public static Properties getTypes() {
        Properties p = new Properties();
        p.setProperty("doneDir", FieldTypes.DIR);
        return p;
    }
    
    public static Properties getDescriptions() {
        Properties p = new Properties();
        p.setProperty("doneDir", "A directory where completed files will be moved to.");
        return p;
    }
    
    public void loadProperties(Properties p) {
        doneDir = p.getProperty("doneDir");
        if (doneDir == null) {
            logger.error(name + ": doneDir must be provided.");
            throw new IllegalArgumentException("Invalid argument");
        }
        status = "FileOutByDir configured.";
    }
    
    public boolean startClient() {
        // Ensure the doneDir property has been set.
        if (doneDir == null)
            return false;
        status = "FileOutByDir starting.";
        if (isRunning())
            stopClient();
        t = new Thread(this);
        t.start();
        return true;
    }
    
    public void run() {
        running = true;
        status = "FileOutByDir running.";
        try {
            while (running) {
                // This will blocking wait()ing for message.
                String m = null;
                try {
                    logger.trace(name + " blocking on get message");
                    m = getMessage();
                    logger.trace(name + " completed get message");
                } catch (RuntimeException e) {
                    logger.error(name + ": Couldn't move file in get message.");
                    running = false;
                    break;
                }
                if (m == null)
                    continue;
                count++;
                logger.info(name + ": " + m);
                FileWriter fw = getFileWriter();
                fw.write(m+"\n");
                fw.flush();
                fw.close();
                logger.trace(name+" wrote and closed file "+fw);
            }
        } catch (IOException e) {
            status = "FileOutByDir IO exception.";
            logger.error(name + ": IOException writing to file");
        } catch (Exception e) {
            status = "FileOutByDir exception - "+e;
            logger.error(name + ": Exception - "+e);
        } finally {
            running = false;
        }
    }
    
    public synchronized String getMessage() {
        while (available == false) {
            try {
                logger.trace(name + " waiting on get...");
                wait();
            } catch (InterruptedException e) {
                return null;
            }
        }
        available = false;
        notifyAll();
        return message;
    }
    
    public boolean stopClient() {
        status = "FileOutByDir shutting down.";
        logger.info(name + ": Client has been told to shutdown.");
        running = false;
        if (t != null && t.isAlive())
            t.interrupt();
        t = null;
        status = "FileOutByDir shut down.";
        return true;
    }
    
    public String getFileName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String ret = sdf.format(new Date());
        return ret;
    }
    
    private FileWriter getFileWriter() throws IOException {
        File f = null;
        String writeFile = getFileName();
        int i = 1;
        while (f == null) {
            f = new File(doneDir+File.separator+writeFile+"."+i);
            if (f.exists()) {
                i++;
                f = null;
            }
        }
        logger.debug(name + ": Setting next file " + f.getName());
        FileWriter writer = new FileWriter(f);
        logger.trace(name + ": returning FileWriter "+writer);
        return writer;
    }
    
}
