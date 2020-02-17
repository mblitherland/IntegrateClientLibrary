/*
 * Copyright (C) 2004-2008 M Litherland
 */

package org.nule.integrateclient.net;

import java.util.Properties;

import org.nule.integrateclient.common.*;

/**
 *
 * @author mike
 */
public class Hl7ManualAckThreadServer extends Hl7ThreadServer {
    
    public static final String DESCRIPTION = "This inbound client (server, " +
            "actually) extends Hl7ThreadServer to render an interface allowing you " +
            "to selectively produce standard ACK or NACK messages (or ignore " +
            "them and wait for a timeout).  If a number of messages queue " +
            "(potentially from multiple senders) you can choose to ACK or NACK " +
            "them all.";
    
    public Hl7ManualAckThreadServer(String name, Logger logger, ProcessorAgent pa) {
        super(name, logger, pa);
    }
    
    public Hl7ManualAckThreadServer(String name, Logger logger, ProcessorAgent pa, Properties p) {
        super(name, logger, pa, p);
    }

    public static Properties getDefaults() {
        Properties p = new Properties();
        p.setProperty("listenPort", "12345");
        p.setProperty("acknowledgmentAgent", "org.nule.integrateclient.net.Hl7ManualAcknowledgment");
        return p;
    }
    
    public static Properties getDescriptions() {
        Properties p = new Properties();
        p.setProperty("listenPort", "The port this client will listen on.");
        p.setProperty("acknowledgmentAgent", "Changing this class will break the manual acknowledgment!");
        return p;
    }
}
