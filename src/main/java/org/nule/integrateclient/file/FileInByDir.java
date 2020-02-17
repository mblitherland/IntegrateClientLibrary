/*
 * FileInByDir.java
 *
 * Created on April 4, 2006, 2:53 PM
 *
 * Copyright (C) 2004-2008 M Litherland
 */

package org.nule.integrateclient.file;

import java.io.*;
import java.util.Properties;

import org.nule.integrateclient.common.*;
import org.nule.lighthl7lib.util.LineReader;

/**
 *
 * @author litherm
 */
public class FileInByDir extends FileIn {
    
    public static final String DESCRIPTION = "This client reads any file that " +
            "shows up in a specified directory and passes it to the processor " +
            "agent.  It assumes that records are seperated by at least a new line.";
    
    private String workDir, doneDir;
    private boolean ignoreMoveFail = false;
    
    public FileInByDir(String name, Logger logger, ProcessorAgent pa) {
        super(name, logger, pa);
    }
    
    public FileInByDir(String name, Logger logger, ProcessorAgent pa, Properties p) {
        super(name, logger, pa, p);
    }
    
    public static Properties getMandatory() {
        Properties p = new Properties();
        p.setProperty("workDir", "");
        p.setProperty("doneDir", "");
        return p;
    }
    
    public static Properties getOptional() {
        Properties p = new Properties();
        p.setProperty("ignoreMoveFail", "");
        return p;
    }
    
    public static Properties getDefaults() {
        Properties p = new Properties();
        p.setProperty("workDir", "work");
        p.setProperty("doneDir", "done");
        p.setProperty("ignoreMoveFail", "false");
        return p;
    }
    
    public static Properties getTypes() {
        Properties p = new Properties();
        p.setProperty("workDir", FieldTypes.DIR);
        p.setProperty("doneDir", FieldTypes.DIR);
        p.setProperty("ignoreMoveFail", FieldTypes.BOOLEAN);
        return p;
    }
    
    public static Properties getDescriptions() {
        Properties p = new Properties();
        p.setProperty("workDir", "This directory will be monitored for new files to read in.");
        p.setProperty("doneDir", "Processed files will be moved to this directory.");
        p.setProperty("ignoreMoveFail", "Log the exception if moving a file fails, but don't exit.");
        return p;
    }
    
    public void loadProperties(Properties p) {
        workDir = p.getProperty("workDir");
        doneDir = p.getProperty("doneDir");
        if (workDir == null || doneDir == null) {
            logger.error(name + ": workDir and doneDir must be provided.");
            throw new IllegalArgumentException("Invalid argument");
        }
        if (p.contains("ignoreMoveFail")) {
            if (p.get("ignoreMoveFail").toString().toLowerCase().startsWith("true")) {
                ignoreMoveFail = true;
            }
        }
        logger.info("Setting ignoreMoveFail to "+ignoreMoveFail);
        status = "FileInByDir initialized.";
    }
    
        
    public Properties getStatus() {
        logger.info(name + ": Handling request for status.");
        Properties p = new Properties();
        p.put("workDir", workDir);
        p.put("doneDir", doneDir);
        p.put("ignoreMoveFail", Boolean.toString(ignoreMoveFail));
        p.put("status", status);
        p.put("count", Long.toString(count));
        return p;
    }
    
    public boolean startClient() {
        // Confirm that workDir as been configured
        if (workDir == null)
            return false;
        // Confirm that doneDir as been configured
        if (doneDir == null)
            return false;
        status = "FileInByDir told to start.";
        logger.info(name + ": Client has been told to start.");
        if (isRunning())
            stopClient();
        t = new Thread(this);
        t.start();
        return true;
    }
    
    public void run() {
        running = true;
        status = "FileInByDir running.";
        try {
            while (running) {
                File scanning = new File(workDir);
                String[] fl = scanning.list();
                for (int i = 0; i < fl.length; i++) {
                    File m = new File(workDir+File.separator+fl[i]);
                    if (!m.isDirectory()) {
                        processFile(m);
                        File t = new File(doneDir+File.separator+m.getName());
                        if (!m.renameTo(t)) {
                            logger.warn(name + ": Simple rename failed for "+m.getName());
                            boolean moved = false;
                            for (int j = 0; j < 100; j++) {
                                t = new File(doneDir+File.separator+m.getName()+"."+j);
                                logger.trace(name + ": Renaming file " + m.getName() + " to " +
                                        t.getName());
                                if (t.exists())
                                    continue;
                                if (m.renameTo(t)) {
                                    moved = true;
                                    break;
                                } else {
                                    logger.warn(name + ": Move failed for " + m.getName()
                                    + " to " + t.getName());
                                }
                            }
                            if (!moved) {
                                logger.error(name + ": All attempts to move file failed.");
                                if (!ignoreMoveFail) {
                                    logger.warn(name + ": ingnoreMoveFail is false, stopping the client.");
                                    throw new RuntimeException("Couldn't move file.");
                                }
                            }
                        }
                    }
                }
                Thread.sleep(5000);
            }
        } catch (InterruptedException e) {
            // That's OK, it's just the thread.sleep above
        } catch (Exception e) {
            logger.error(name + ": Exception in run loop", e);
        } finally {
            status = "FileInByDir not running";
            running = false;
        }
    }
    
    public void processFile(File f) {
        logger.debug(name + ": Found file "+f);
        try {
            FileReader fr = new FileReader(f);
            LineReader lr = new LineReader(fr);
            while (running){
                String data = lr.readLine();
                if (data == null)
                    break;
                count++;
                logger.info(name + ": "+data);
                pa.dataTransfer(data);
            }
            fr.close();
        } catch (FileNotFoundException e) {
            logger.error(name + ": File "+f+" not found.");
        } catch (IOException e) {
            logger.error(name + ": IOException reading "+f);
        }
    }
}
