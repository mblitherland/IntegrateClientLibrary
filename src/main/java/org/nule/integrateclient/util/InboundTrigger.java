/*
 * InboundTrigger.java
 * 
 * Copyright (C) 2004-2012 M Litherland
 */

package org.nule.integrateclient.util;

import java.text.SimpleDateFormat;
import java.util.*;
import org.nule.integrateclient.common.*;

/**
 *
 * @author mike
 */
public class InboundTrigger extends IntegrateClient implements InboundClient {
    
    public static final String DESCRIPTION = "This inbound client doesn't actually " +
            "accept data.  Instead it generates an empty inbound message at an " +
            "interval you specify.  This can be used to trigger events like a poll " +
            "of a database or the generation of some other sort of message using " +
            "the beanshell logic.  There is one special behavior you should be " +
            "aware of for this script.  Sending it an ACK from the logic will result " +
            "in the loop waiting the specified time for the next event, but a NACK " +
            "will result in another trigger being sent immediately.  That way if " +
            "you perform a poll and have multiple messages to send subsequent ones " +
            "can be sent more quickly.";
        
    protected String name;
    protected Logger logger;
    protected ProcessorAgent pa;
    private int timeToSleep;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss.SSS");
    protected String status = "InboundTrigger not initialized.";
    protected Thread t;
    protected boolean running;
    protected long count = 0;
    
    public InboundTrigger(String name, Logger logger, ProcessorAgent pa) {
        super(name, logger, pa);
        this.name = name;
        this.logger = logger;
        this.pa = pa;
    }
    
    public InboundTrigger(String name, Logger logger, ProcessorAgent pa, Properties p) {
        super(name, logger, pa, p);
        this.name = name;
        this.logger = logger;
        this.pa = pa;
        loadProperties(p);
    }
    
    public static Properties getMandatory() {
        Properties p = new Properties();
        p.put("sleep", "");
        return p;
    }
    
    public static Properties getOptional() {
        return null;
    }
    
    public static Properties getDefaults() {
        Properties p = new Properties();
        p.put("sleep", "1000");
        return p;
    }
    
    public static Properties getTypes() {
        Properties p = new Properties();
        p.put("sleep", FieldTypes.INTEGER);
        return p;
    }
    
    public static Properties getDescriptions() {
        Properties p = new Properties();
        p.put("sleep", "A time in milliseconds to sleep before generating a trigger.");
        return p;
    }
    
    public Properties getStatus() {
        logger.info(name + ": Handling request for status.");
        Properties p = new Properties();
        p.put("sleep", Integer.toString(timeToSleep));
        p.put("status", status);
        return p;
    }

    public Boolean isConnected() {
        return null;
    }

    public String getState() {
        return status;
    }

    public void loadProperties(Properties p) throws IllegalArgumentException {
        timeToSleep = 1000;
        if (p.getProperty("sleep") != null && !"".equals(p.getProperty("sleep"))) {
            try {
                timeToSleep = Integer.parseInt(p.getProperty("sleep"));
            } catch (NumberFormatException e) {
                logger.warn(name + ": Must provide integer for sleep.");
            }
        }
        status = "InboundTrigger configured.";
    }

    public boolean stopClient() {
        status = "InboundTrigger shutting down.";
        logger.info(name + ": Client has been told to shutdown.");
        running = false;
        if (t != null && t.isAlive()) {
            t.interrupt();
        }
        t = null;
        status = "InboundTrigger shut down.";
        return true;
    }

    public boolean startClient() {
        status = "InboundTrigger starting.";
        logger.info(name + ": Client has been told to start.");
        if (isRunning()) {
            stopClient();
        }
        t = new Thread(this);
        t.start();
        return true;
    }

    public boolean isRunning() {
        return running;
    }
    
    public long getCount() {
        return count;
    }

    public void run() {
        running = true;
        status = "InboundTrigger running.";
        try {
            logger.info(name + ": Client starting.");
            while (running) {
                Thread.sleep(timeToSleep);
                count++;
                pa.dataTransfer(sdf.format(new Date()));
            }
        } catch (InterruptedException e) {
            status = "Interrupted exception.";
            logger.error(name + ": InterruptedException - "+e);
        } catch (Exception e) {
            status = "Some other exception in run loop.";
            logger.error(name + ": Some other exception - "+e);
        } finally {
            running = false;
        }
    }

}
