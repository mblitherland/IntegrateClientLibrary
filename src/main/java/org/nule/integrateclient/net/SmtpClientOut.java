/*
 * SmtpClientOut
 *
 * Copyright (C) 2009 M Litherland
 */

package org.nule.integrateclient.net;

import java.io.IOException;
import java.net.SocketException;
import java.util.Properties;
import org.apache.commons.net.smtp.SMTPClient;
import org.apache.commons.net.smtp.SMTPReply;
import org.nule.integrateclient.common.*;

/**
 *
 * @author mike
 */
public class SmtpClientOut extends IntegrateClient implements OutboundClient {

    public static String DESCRIPTION = "A simple SMTP client to send emails. " +
            "The String message you pass to the client when you wish to send an " +
            "email requires certain parameters.  A 'From:' field with the sending " +
            "email, a 'To:' field with recipient emails, comma separated, a " +
            "'Subject:' field with subject text and the body.  Each of the fields " +
            "requires a newline after it, anything else is sent as the body.";

    protected String name;
    protected Logger logger;
    protected ProcessorAgent pa;
    protected String status;
    protected long count;
    protected boolean connected = false;
    protected boolean run = false;
    protected boolean available = true;
    private Thread t;

    protected String hostname;
    protected boolean stopOnFailure;

    public SmtpClientOut(String name, Logger logger, ProcessorAgent pa) throws
            IllegalArgumentException {
        super(name, logger, pa);
        this.name = name;
        this.logger = logger;
        this.pa = pa;
    }

    public SmtpClientOut(String name, Logger logger, ProcessorAgent pa, Properties p) throws
            IllegalArgumentException {
        super(name, logger, pa, p);
        this.name = name;
        this.logger = logger;
        this.pa = pa;
        loadProperties(p);
    }
    
    public static Properties getDefaults() {
        Properties p = new Properties();
        p.put("hostname", "");
        p.put("stopOnFailure", "");
        return p;
    }

    public static Properties getMandatory() {
        Properties p = new Properties();
        p.put("hostname", "");
        p.put("stopOnFailure", "");
        return p;
    }

    public static Properties getOptional() {
        return null;
    }

    public static Properties getTypes() {
        Properties p = new Properties();
        p.put("hostname", "");
        p.put("stopOnFailure", "");
        return p;
    }

    public static Properties getDescriptions() {
        Properties p = new Properties();
        p.put("hostname", "");
        p.put("stopOnFailure", "");
        return p;
    }

    public void loadProperties(Properties p) throws IllegalArgumentException {
        hostname = p.getProperty("remoteHost");
        if (hostname == null) {
            logger.error(name + ": hostname must be provided.");
            throw new IllegalArgumentException("Invalid argument");
        }
        if (p.contains("stopOnFailure")) {
            if (p.get("stopOnFailure").toString().equalsIgnoreCase("false")) {
                stopOnFailure = false;
            }
        }
        status = "FtpClient configured.";
    }

    public Properties getStatus() {
        logger.info(name + ": Handling request for status.");
        Properties p = new Properties();
        p.setProperty("hostname", hostname);
        p.setProperty("stopOnFailure", Boolean.toString(stopOnFailure));
        p.put("status", status);
        p.put("count", Long.toString(count));
        p.put("connected", Boolean.toString(connected));
        return p;
    }

    public Boolean isConnected() {
        return new Boolean(connected);
    }

    public String getState() {
        return status;
    }

    public boolean stopClient() {
        status = "Client has been told to shutdown.";
        logger.info(name + ": Client has been told to shutdown.");
        run = false;
        if (t != null && t.isAlive()) {
            t.interrupt();
        }
        t = null;
        status = "SmtpClientOut is shutdown.";
        return true;
    }

    public boolean startClient() {
        status = "SmtpClientOut starting.";
        logger.info(name + ": Client has been told to start.");
        if (isRunning()) {
            stopClient();
        }
        t = new Thread(this);
        t.start();
        return true;
    }

    public boolean isRunning() {
        return run;
    }

    public long getCount() {
        return count;
    }

    public void run() {
        status = "SmtpClientOut running";
        run = true;
        try {
            while (run) {
                if (getMessage()) {
                    logger.info("SmtpClientOut received message to run");
                    SMTPClient smtp = new SMTPClient();
                    try {
                        smtp.connect(hostname);
                        logger.trace(name+" SMTP Reply: "+smtp.getReplyString());
                        int reply = smtp.getReplyCode();
                        if (!SMTPReply.isPositiveCompletion(reply)) {
                            logger.error(name+" SMTP Failure");
                            if (stopOnFailure) {
                                run = false;
                                break;
                            }
                        }
                        

                    } catch (SocketException e) {
                        logger.error("SocketException in SMTP: "+e);
                        if (stopOnFailure) {
                            run = false;
                            break;
                        }
                    } catch (IOException e) {
                        logger.error("IOException in SMTP: "+e);
                        if (stopOnFailure) {
                            run = false;
                            break;
                        }
                    } finally {
                        try {
                            if (smtp.isConnected()) {
                                smtp.disconnect();
                            }
                        } catch (IOException e) {
                            logger.warn(name+" IOExeption disconnecting "+e);
                        }
                    }
                }
                // Just prevent tight looping if something goes wrong with the
                // wait/notify.
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            logger.info(name+" Interrupted.");
        } finally {
            run = false;
        }
    }

    public synchronized void putMessage(String message) {
        while (available == true) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        available = true;
        notifyAll();
    }

    public synchronized boolean getMessage() {
        while (available == false) {
            try {
                wait();
            } catch (InterruptedException e) {
                return false;
            }
        }
        available = false;
        notifyAll();
        return true;
    }
    
}
