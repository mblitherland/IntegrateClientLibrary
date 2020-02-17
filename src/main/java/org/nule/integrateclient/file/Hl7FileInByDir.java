/*
 * Hl7FileInByDir.java
 *
 * Created on March 29, 2006, 4:17 PM
 *
 * Copyright (C) 2005-2006 M Litherland
 */

package org.nule.integrateclient.file;

import java.io.*;
import java.util.Properties;

import org.nule.integrateclient.common.*;
import org.nule.lighthl7lib.hl7.*;
import org.nule.lighthl7lib.util.*;

/**
 *
 * @author litherm
 */
public class Hl7FileInByDir extends FileInByDir {
    
    public static final String DESCRIPTION = "This client reads any file that " +
            "shows up in a specified directory attempting to strip any HL7 " +
            "information and pass it to the processor agent.  It assumes that " +
            "records are seperated by at least a new line.";
    
    public Hl7FileInByDir(String name, Logger logger, ProcessorAgent pa) {
        super(name, logger, pa);
    }
    
    public Hl7FileInByDir(String name, Logger logger, ProcessorAgent pa, Properties p) {
        super(name, logger, pa, p);
    }
    
    public void processFile(File f) {
        logger.debug(name + ": Found file "+f);
        try {
            FileReader fr = new FileReader(f);
            LineReader lr = new LineReader(fr);
            while (running){
                String s = lr.readLine();
                if (s == null)
                    break;
                String data = Hl7Record.cleanString(s);
                if (data == null)
                    continue;
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
