/*
 * FileOutByDir.java
 *
 * Created on March 28, 2006, 4:21 PM
 *
 * Copyright (C) 2004-2012 M Litherland
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
public class FileOutByDir extends FileOut {
    
    public static final String DESCRIPTION = "This client outputs files into a " +
            "working directory then moves them on a regular basis to a completed " +
            "or done directory.  It guarantees a certain reliable number of files " +
            "are created per hour, but unfortunately may create empty files.  This " +
            "is the primary file out mechanism in the integrate client library.";
    
    private static final long HOUR = 3600;
    
    private FileWriter fw = null;
    
    private long rollInterval, currentLogTime, nextLogTime;
    
    private String workDir, doneDir;
    
    public FileOutByDir(String name, Logger logger, ProcessorAgent pa) {
        super(name, logger, pa);
    }
    
    public FileOutByDir(String name, Logger logger, ProcessorAgent pa, Properties p) {
        super(name, logger, pa, p);
    }
    
    public static Properties getMandatory() {
        Properties p = new Properties();
        p.setProperty("workDir", "");
        p.setProperty("doneDir", "");
        p.setProperty("files", "");
        return p;
    }
    
    public static Properties getOptional() {
        return null;
    }
    
    public static Properties getDefaults() {
        Properties p = new Properties();
        p.setProperty("workDir", "work");
        p.setProperty("doneDir", "done");
        p.setProperty("files", "12");
        return p;
    }
    
    public static Properties getTypes() {
        Properties p = new Properties();
        p.setProperty("workDir", FieldTypes.DIR);
        p.setProperty("doneDir", FieldTypes.DIR);
        p.setProperty("files", FieldTypes.INTEGER);
        return p;
    }
    
    public static Properties getDescriptions() {
        Properties p = new Properties();
        p.setProperty("workDir", "A directory where files in processs will be written to.");
        p.setProperty("doneDir", "A directory where completed files will be moved to.");
        p.setProperty("files", "The number of output files you wish to create per hour.");
        return p;
    }
    
    @Override
    public void loadProperties(Properties p) {
        workDir = p.getProperty("workDir");
        doneDir = p.getProperty("doneDir");
        if (workDir == null || doneDir == null) {
            logger.error(name + ": workDir and doneDir must be provided.");
            throw new IllegalArgumentException("Invalid argument");
        }
        try {
            int i = Integer.parseInt(p.getProperty("files"));
            rollInterval = HOUR / i * 1000;
            currentLogTime = System.currentTimeMillis();
            nextLogTime = currentLogTime + rollInterval;
        } catch (NumberFormatException e) {
            logger.error("Must provide integer for files per hour.");
            throw new IllegalArgumentException("Invalid argument");
        }
        status = "FileOutByDir configured.";
    }
    
    @Override
    public boolean startClient() {
        // Ensure the workDir property has been set.
        if (workDir == null) {
            return false;
        }
        // Ensure the doneDir property has been set.
        if (doneDir == null) {
            return false;
        }
        status = "FileOutByDir starting.";
        if (isRunning()) {
            stopClient();
        }
        t = new Thread(this);
        t.start();
        return true;
    }
    
    @Override
    public void run() {
        running = true;
        status = "FileOutByDir running.";
        try {
            fileName = getFileName();
            while (running) {
                // This will blocking wait()ing for message.
                String m = null;
                try {
                    m = getMessage();
                } catch (RuntimeException e) {
                    logger.error(name + ": Couldn't move file in get message.");
                    running = false;
                    break;
                }
                if (m == null)
                    continue;
                count++;
                if (fw == null) {
                    fw = getFileWriter();
                }
                logger.info(name + ": " + m);
                fw.write(m+"\n");
                fw.flush(); // This might be a bit much...
                if (!fileName.equals(getFileName())) {
                    logger.debug(name + ": Client switching output files.");
                    if (fw != null) {
                        fw.close();
                        fw = null;
                    }
                    try {
                        clearWorkingDir();
                    } catch (RuntimeException e) {
                        logger.error(name + ": Couldn't move file, exiting runloop.");
                        running = false;
                    }
                    fileName = getFileName();
                }
            }
            if (fw != null) {
                fw.close();
                fw = null;
            }
            clearWorkingDir();
        } catch (IOException e) {
            status = "FileOutByDir IO exception.";
            logger.error(name + ": IOException writing to "+fileName);
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
                wait(1000);
                try {
                    if (!fileName.equals(getFileName())) {
                        logger.debug(name + ": Client switching output files.");
                        if (fw != null) {
                            fw.flush();
                            fw.close();
                            fw = null;
                        }
                        try {
                            clearWorkingDir();
                        } catch (RuntimeException e) {
                            logger.error(name + ": Couldn't move file, exiting runloop.");
                            running = false;
                        }
                        fileName = getFileName();
                    }
                } catch (IOException e) {
                    logger.error(name + ": IOException cycling to "+fileName);
                }
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
        if (fw != null) {
            try {
                fw.close();
            } catch (IOException e) {
                logger.warn(name + ": Exception trying to close filehandle.");
            }
        }
        clearWorkingDir();
        return true;
    }
    
    public String getFileName() {
        long current = System.currentTimeMillis();
        if (current >= nextLogTime) {
            currentLogTime = nextLogTime;
            nextLogTime = currentLogTime + rollInterval;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String ret = sdf.format(new Date(currentLogTime));
        return ret;
    }
    
    public FileWriter getFileWriter() throws IOException {
        File f = new File(workDir+File.separator+fileName);
        logger.trace(name + ": Creating new file " + f.getName());
        FileWriter writer = new FileWriter(f);
        return writer;
    }
    
    public synchronized void clearWorkingDir() throws RuntimeException {
        File f = new File(workDir);
        String[] files = f.list();
        logger.debug(name + ": Client cleaning work dir.");
        for (int i = 0; i < files.length; i++) {
            File m = new File(workDir+File.separator+files[i]);
            if (!m.isDirectory()) {
                boolean moved = false;
                for (int j = 0; j < 100; j++) {
                    File temp = new File(doneDir+File.separator+m.getName()+"."+j);
                    logger.trace(name + ": Renaming file " + m.getName() + " to " +
                            temp.getName());
                    if (temp.exists())
                        continue;
                    if (m.renameTo(temp)) {
                        moved = true;
                        break;
                    } else {
                        logger.warn(name + ": Move failed for " + m.getName()
                        + " to " + t.getName());
                    }
                }
                if (!moved) {
                    logger.error(name + ": All attempts to move file failed.");
                    throw new RuntimeException("Couldn't move file.");
                }
            }
        }
    }
    
}
