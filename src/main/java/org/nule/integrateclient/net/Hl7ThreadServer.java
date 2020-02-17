/*
 * Hl7ThreadServer.java
 *
 * Created on March 31, 2006, 9:39 AM
 *
 * Copyright (C) 2006 M Litherland
 */

package org.nule.integrateclient.net;

import java.net.*;
import java.util.*;

import org.nule.integrateclient.common.*;

/**
 *
 * @author litherm
 */
public class Hl7ThreadServer extends MllpThreadServer {
    
    public static final String DESCRIPTION = "This inbound client (server, " +
            "actually) extends MllpThreadServer by ensuring that data " +
            "received is clean HL7 data.";
    
    public Hl7ThreadServer(String name, Logger logger, ProcessorAgent pa) {
        super(name, logger, pa);
    }
    
    public Hl7ThreadServer(String name, Logger logger, ProcessorAgent pa, Properties p) {
        super(name, logger, pa, p);
    }
    
    public static Properties getDefaults() {
        Properties p = new Properties();
        p.setProperty("listenPort", "12345");
        p.setProperty("acknowledgmentAgent", "org.nule.integrateclient.net.Hl7Acknowledgment");
        return p;
    }
    
        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
    public void run() {
        run = true;
        status = "Hl7ThreadServer waiting for a connection.";
        try {
            while (!connected) {
                logger.info(name + ": Trying to open port " + listenPort + " for listening.");
                Thread.sleep(5000);
                establishConnection(listenPort);
            }
            logger.info(name + ": Listening on "+listenPort);
            while (run) {
                Socket s = acceptConnection();
                if (s == null) {
                    logger.warn(name + ": Error accepting connection: "+lastError);
                    continue;
                }
                logger.debug(name + ": Remote connection from: "+s.getRemoteSocketAddress());
                MllpThreadServerConn mtsc = new Hl7ThreadServerConn(name, s, this, logger);
                logger.info(name + ": Starting client: "+mtsc.toString());
                mtsc.startClient();
                connections.add(mtsc);
            }
            closeConnection();
        } catch (InterruptedException e) {
            status = "Hl7ThreadServer interrupted.";
            logger.warn(name + ": Thread interrupted.");
        } catch (Exception e) {
            status = "Hl7ThreadServer Exception - "+e;
            logger.error(name + ": Exception - "+e);
        } finally {
            run = false;
        }
    }
    
}
