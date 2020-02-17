/*
 * FileOutByTime.java
 *
 * Created on March 28, 2006, 4:02 PM
 *
 * Copyright (C) 2004-2006 M Litherland
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
 * This class outputs by files that contain embedded timestamps in the name
 * (using strftime like functionality).  It is handy for separating files
 * based on an hourly or daily basis, for logging or other applications.
 */
public class FileOutByTime extends FileOut {
    
    public static final String DESCRIPTION = "This client outputs files with a " +
            "timestamp in the name of the file.  This is primarily intended for " +
            "log files, as the file will not be closed until the next file has " +
            "been opened or the process agent using the file has been shut down.";
    
    private String logDir;
    private String logTime;
    private String logFile;
    
    public FileOutByTime(String name, Logger logger, ProcessorAgent pa) {
        super(name, logger, pa);
    }
    
    public FileOutByTime(String name, Logger logger, ProcessorAgent pa, Properties p) {
        super(name, logger, pa, p);
    }
    
    public static Properties getMandatory() {
        Properties p = new Properties();
        p.setProperty("logDir", "");
        p.setProperty("logTime", "");
        p.setProperty("logFile", "");
        return p;
    }
    
    public static Properties getOptional() {
        return null;
    }
    
    public static Properties getDefaults() {
        Properties p = new Properties();
        p.setProperty("logDir", "logs");
        p.setProperty("logTime", "yyyyMMddHHmmss");
        p.setProperty("logFile", ".log");
        return p;
    }
    
    public static Properties getTypes() {
        Properties p = new Properties();
        p.setProperty("logDir", FieldTypes.DIR);
        p.setProperty("logTime", FieldTypes.STRING);
        p.setProperty("logFile", FieldTypes.STRING);
        return p;
    }
    
    public static Properties getDescriptions() {
        Properties p = new Properties();
        p.setProperty("logDir", "Directory to where files should be put.");
        p.setProperty("logTime", "A timestamp (in SimpleDateFormat style) to form the file name.");
        p.setProperty("logFile", "An extension to append to the file name.");
        return p;
    }
    
    public void loadProperties(Properties p) {
        logDir = p.getProperty("logDir");
        logTime = p.getProperty("logTime");
        logFile = p.getProperty("logFile");
        if (logDir == null || logTime == null || logFile == null) {
            logger.error(name + ": fileName must be provided.");
            throw new IllegalArgumentException("Invalid argument");
        }
        status = "FileOutByTime configured.";
    }
    
    public boolean startClient() {
        // Ensure the logDir property has been set.
        if (logDir == null)
            return false;
        // Ensure the logTime property has been set.
        if (logTime == null)
            return false;
        // Ensure the logFile property has been set.
        if (logFile == null)
            return false;
        // Ensure the logger has been set.
        if (logger == null)
            return false;
        status = "FileOutByTime starting.";
        if (isRunning())
            stopClient();
        t = new Thread(this);
        t.start();
        return true;
    }
    
    public void run() {
        running = true;
        status = "FileOutByTime running.";
        try {
            fileName = getFileName();
            FileWriter fw = getFileWriter();
            while (running) {
                // This will blocking wait()ing for message.
                String m = getMessage();
                if (m == null)
                    continue;
                count++;
                if (!fileName.equals(getFileName())) {
                    fw.close();
                    fileName = getFileName();
                    fw = getFileWriter();
                }
                fw.write(m+"\n");
                fw.flush(); // This might be a bit much...
            }
            fw.close();
        } catch (IOException e) {
            status = "FileOutByTime IO exception.";
            logger.error(name + ": IOException writing to "+fileName);
        } catch (Exception e) {
            status = "FileOutByTime exception - "+e;
            logger.error(name + ": Exception - "+e);
        } finally {
            running = false;
        }
    }
    
    private String getFileName() {
        SimpleDateFormat sdf = new SimpleDateFormat(logTime);
        String ret = logDir+File.separator+sdf.format(new Date())+logFile;
        return ret;
    }
    
    private FileWriter getFileWriter() throws IOException {
        File f = new File(fileName);
        FileWriter fw = new FileWriter(f);
        return fw;
    }
    
}
