/*
 * Hl7FileIn.java
 *
 * Created on March 29, 2006, 4:17 PM
 *
 * Copyright (C) 2004-2006 M Litherland
 */

package org.nule.integrateclient.file;

import java.io.*;
import java.util.*;

import org.nule.integrateclient.common.*;
import org.nule.lighthl7lib.hl7.*;
import org.nule.lighthl7lib.util.*;

/**
 *
 * @author litherm
 *
 * This class operates on files to read them in line by line, optionally
 * filtering them, then make them available for another class to pick up
 * and handle.
 *
 * All data is passed through the HL7 filter, and is discarded if it doesn't
 * match the regex, leaving this class unsuitable for other kinds of data.
 */
public class Hl7FileIn extends FileIn {
    
    public static final String DESCRIPTION = "This simple file in client reads in " +
            "from the file specified, then exits when complete.  It attempts to " +
            "clean any data it reads in by passing it through a function designed " +
            "to identify HL7 data exclusively.  It assumes records are delimited " +
            "by at least a new line.";
    
    public Hl7FileIn(String name, Logger logger, ProcessorAgent pa) {
        super(name, logger, pa);
    }
    
    public Hl7FileIn(String name, Logger logger, ProcessorAgent pa, Properties p) {
        super(name, logger, pa, p);
    }
    
    
    public void run() {
        running = true;
        status = "Hl7FileIn running.";
        File f = new File(fileName);
        try {
            logger.info(name + ": Client starting.");
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
                logger.debug(name + ": Read record");
                logger.info(name + ": "+data);
                logger.trace(name + ": File in to dataTransfer");
                pa.dataTransfer(data);
                logger.trace(name + ": File in completed dataTransfer");
            }
            fr.close();
        } catch (FileNotFoundException e) {
            status = "Hl7FileIn file not found exception.";
            logger.error(name + ": File "+f+" not found.");
        } catch (IOException e) {
            status = "Hl7FileIn IO exception.";
            logger.error(name + ": IOException reading "+f);
        } catch (Exception e) {
            status = "Hl7FileIn exception - "+e;
            logger.error(name + ": exception - "+e);
        } finally {
            running = false;
        }
    }
}
