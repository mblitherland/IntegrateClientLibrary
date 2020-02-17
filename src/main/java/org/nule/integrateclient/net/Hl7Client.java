/*
 * Hl7Client.java
 *
 * Created on March 30, 2006, 9:29 AM
 *
 * Copyright (C) 2004-2008 M Litherland
 */

package org.nule.integrateclient.net;

import java.util.*;

import org.nule.integrateclient.common.*;
import org.nule.lighthl7lib.hl7.*;

/**
 *
 * @author litherm
 *
 * This is a simple TCP/IP client (outbound) that implements the MLLP.  It
 * filters for HL7 data and therefore is not useful for other types of data
 * transfered via MLLP.
 */
public class Hl7Client extends MllpClient {
    
    public static final String DESCRIPTION = "This client extends MllpClient by " +
            "ensuring that data received back from the server after a send is " +
            "clean HL7 data.";
    
    public Hl7Client(String name, Logger logger, ProcessorAgent pa) {
        super(name, logger, pa);
    }
    
    public Hl7Client(String name, Logger logger, ProcessorAgent pa, Properties p) {
        super(name, logger, pa, p);
    }
    
    public static Properties getDefaults() {
        Properties p = new Properties();
        p.setProperty("sendHost", "localhost");
        p.setProperty("sendPort", "12345");
        p.setProperty("haltOnAe", "false");
        p.setProperty("haltOnAr", "false");
        return p;
    }

    public static Properties getMandatory() {
        Properties p = new Properties();
        p.setProperty("sendHost", "");
        p.setProperty("sendPort", "");
        return p;
    }
    
    public static Properties getOptional() {
        Properties p = new Properties();
        p.setProperty("haltOnAe", "");
        p.setProperty("haltOnAr", "");
        return p;
    }
    
    public static Properties getTypes() {
        Properties p = new Properties();
        p.setProperty("sendHost", FieldTypes.STRING);
        p.setProperty("sendPort", FieldTypes.INTEGER);
        p.setProperty("haltOnAe", FieldTypes.BOOLEAN);
        p.setProperty("haltOnAr", FieldTypes.BOOLEAN);
        return p;
    }
    
    public static Properties getDescriptions() {
        Properties p = new Properties();
        p.setProperty("sendHost", "The host that will receive the connection.");
        p.setProperty("sendPort", "The port that the host will listen on.");
        p.setProperty("haltOnAe", "");
        p.setProperty("haltOnAr", "");
        return p;
    }
    
    public void loadProperties(Properties p) throws IllegalArgumentException {
        sendHost = p.getProperty("sendHost");
        if (sendHost == null) {
            logger.error(name + ": sendHost must be provided.");
            throw new IllegalArgumentException("Invalid argument");
        }
        try {
            sendPort = Integer.parseInt(p.getProperty("sendPort"));
        } catch (NumberFormatException e) {
            logger.error(name + ": Must provide integer for sendPort.");
            throw new IllegalArgumentException("Invalid argument");
        }
        String prop = p.getProperty("haltOnAe");
        if (prop != null && prop.equalsIgnoreCase("true")) {
            haltOnAe = true;
        } else {
            haltOnAe = false;
        }
        prop = p.getProperty("haltOnAr");
        if (prop != null && prop.equalsIgnoreCase("true")) {
            logger.debug(name + "setting haltOnAr true");
            haltOnAr = true;
        } else {
            haltOnAr = false;
        }
        status = "Hl7Client configured.";
    }

    protected void processAck(String ack) {
        if (haltOnAe || haltOnAr) {
            logger.debug("Have a halt directive ae="+haltOnAe+" ar="+haltOnAr);
            try {
                Hl7Record hl7Ack = new Hl7Record(Hl7Record.cleanString(ack));
                String msa1 = hl7Ack.get("MSA").field(1).toString();
                if (haltOnAe && msa1.equals("AE")) {
                    logger.info("Halting processing because AE was received.");
                    pa.stop();
                }
                if (haltOnAr && msa1.equals("AR")) {
                    logger.info("Halting processing because AR was received.");
                    pa.stop();
                }
            } catch (Exception e) {
                logger.info("Exception processing ACK and halt flags present.");
                pa.stop();
            }
        }
    }
    
    public String getData() {
        return Hl7Record.cleanString(super.getData());
    }
    
}
