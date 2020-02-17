/*
 * MllpClient.java
 *
 * Created on March 29, 2006, 9:14 AM
 *
 * Copyright (C) 2004-2008 M Litherland
 */

package org.nule.integrateclient.net;

import java.io.*;
import java.net.*;
import java.util.*;

import org.nule.integrateclient.common.*;

/**
 *
 * @author litherm
 *
 * This is a simple TCP/IP client (outbound) that implements the MLLP.  It
 * performs no filtering and is suitable for any data using the MLLP protocol.
 */
public class MllpClient extends IntegrateClient implements OutboundClient {
    
    public static final String DESCRIPTION = "This simple outbound client sends " +
            "messages in MLLP wrappers.  This client will work with any type of " +
            "data that doesn't contain embedded MLLP characters (chars 11, 13, 28).";
    
    public static final char RETURN = 13;
    
    public static final char SOB = 11;
    
    public static final char EOR = 28;
    
    protected String name;
    
    protected String sendHost;
    
    protected int sendPort;
    
    protected String status = "MllpClient not initialized.";
    
    protected long count = 0;
    
    private boolean run = false;
    
    protected Logger logger;
    
    private Thread t;
    
    protected ProcessorAgent pa;
    
    private String message;
    
    private boolean available;
    
    private Socket s;
    
    private InputStreamReader in;
    
    private OutputStream out;
    
    private boolean connected = false;
    
    private StringBuffer inbound;
    
    // For the Hl7Client, seems to be required to keep this in the super class...
    protected boolean haltOnAe = false;
    protected boolean haltOnAr = false;
    
    public MllpClient(String name, Logger logger, ProcessorAgent pa) throws
            IllegalArgumentException {
        super(name, logger, pa);
        this.name = name;
        this.logger = logger;
        this.pa = pa;
        inbound = new StringBuffer();
    }
    
    public MllpClient(String name, Logger logger, ProcessorAgent pa, Properties p) throws
            IllegalArgumentException {
        super(name, logger, pa, p);
        this.name = name;
        this.logger = logger;
        this.pa = pa;
        loadProperties(p);
        inbound = new StringBuffer();
    }
    
    public static Properties getDefaults() {
        Properties p = new Properties();
        p.setProperty("sendHost", "localhost");
        p.setProperty("sendPort", "12345");
        return p;
    }

    public static Properties getMandatory() {
        Properties p = new Properties();
        p.setProperty("sendHost", "");
        p.setProperty("sendPort", "");
        return p;
    }
    
    public static Properties getOptional() {
        return null;
    }
    
    public static Properties getTypes() {
        Properties p = new Properties();
        p.setProperty("sendHost", FieldTypes.STRING);
        p.setProperty("sendPort", FieldTypes.INTEGER);
        return p;
    }
    
    public static Properties getDescriptions() {
        Properties p = new Properties();
        p.setProperty("sendHost", "The host that will receive the connection.");
        p.setProperty("sendPort", "The port that the host will listen on.");
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
        status = "MllpClient configured.";
    }
    
    public Properties getStatus() {
        logger.info(name + ": Handling request for status.");
        Properties p = new Properties();
        p.put("sendHost", sendHost);
        p.put("sendPort", Integer.toString(sendPort));
        p.put("status", status);
        p.put("count", Long.toString(count));
        p.put("connected", Boolean.toString(connected));
        return p;
    }

    public Boolean isConnected() {
        return new Boolean(connected);
    }
    
    public boolean stopClient() {
        status = "Client has been told to shutdown.";
        logger.info(name + ": Client has been told to shutdown.");
        run = false;
        closeConnection();
        if (t != null && t.isAlive())
            t.interrupt();
        t = null;
        status = "Client is shutdown.";
        return true;
    }
    
    public boolean startClient() {
        if (sendHost == null)
            return false;
        if (sendPort < 1 || sendPort > 65535) {
            logger.warn(name + ": Invalid range for sendPort.");
            return false;
        }
        status = "MllpClient starting.";
        logger.info(name + ": Client has been told to start.");
        if (isRunning())
            stopClient();
        t = new Thread(this);
        t.start();
        return true;
    }
    
    public String getState() {
        return status;
    }
    
    public boolean isRunning() {
        return run;
    }
    
    /**
     * This is for when the SimpleController runs us in batch mode.  It allows
     * the controller to wait until the last message is sent before telling us
     * to shutdown.
     *
     */
    public synchronized void waitTillDone() {
        while (available == true) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
    }
    
    public synchronized void putMessage(String msg) {
        while (available == true) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        message = msg;
        available = true;
        notifyAll();
    }
    
    public synchronized String getMessage() {
        while (available == false) {
            try {
                wait();
            } catch (InterruptedException e) {
                return null;
            }
        }
        available = false;
        notifyAll();
        return message;
    }
    
    public long getCount() {
        return count;
    }
    
    public void run() {
        run = true;
        status = "MllpClient running.";
        try {
            logger.info(name + ": Client starting.");
            logger.info(name + ": Connecting to "+sendHost+":"+sendPort);
            if (establishConnection(sendHost, sendPort)) {
                logger.trace(name + ": Successfully established connection: "+s.getLocalSocketAddress());
            } else {
                logger.debug(name + ": Failed to establish connection");
            }
            while (run) {
                // This will blocking wait()ing for message.
                String m = getMessage();
                if (m == null)
                    continue;
                logger.info(name + ": Received message");
                count++;
                boolean inner = true;
                while (inner) {
                    try {
                        logger.info(name + ": "+m);
                        sendMsg(m);
                        logger.info(name + ": Waiting for ack/nak.");
                        String ack = getData();
                        if (ack == null) {
                            logger.warn(name + ": Received a null ack - this probably means we're disconnected.");
                            if (establishConnection(sendHost, sendPort)) {
                                logger.trace(name + ": Successfully established connection: "+s.getLocalSocketAddress());
                            } else {
                                logger.debug(name + ": Failed to establish connection");
                                Thread.sleep(5000);
                            }
                        } else {
                            logger.info(name + ": Got ack/nak :"+ack);
                            inner = false;
                            // processAck needs to be about the last thing in the run loop
                            processAck(ack);
                        }
                    } catch (IOException e) {
                        // Catches either IOException or NullPointerException,
                        // in either case we're likely not connected.
                        logger.warn(name + ": Lost connection, re-establishing: "+e.getLocalizedMessage());
                        if (establishConnection(sendHost, sendPort)) {
                            logger.trace(name + ": Successfully established connection: "+s.getLocalSocketAddress());
                        } else {
                            logger.debug(name + ": Failed to establish connection");
                            Thread.sleep(5000);
                        }
                    } catch (NullPointerException e) {
                        // Catches either IOException or NullPointerException,
                        // in either case we're likely not connected.
                        logger.warn(name + ": Lost connection, re-establishing: "+e.getLocalizedMessage());
                        if (establishConnection(sendHost, sendPort)) {
                            logger.trace(name + ": Successfully established connection: "+s.getLocalSocketAddress());
                        } else {
                            logger.debug(name + ": Failed to establish connection");
                            Thread.sleep(5000);
                        }
                    }
                }
            }
            stopClient();
            closeConnection();
        } catch (InterruptedException e) {
            logger.warn(name + ": Thread interrupted.");
            status = "MllpClient interrupted.";
        } catch (Exception e) {
            status = "MllpClient Exception - "+e;
            logger.error(name + ": Exception - "+e);
        } finally {
            run = false;
        }
    }
    
    /**
     * Allow subclasses to process the ack.
     * @param ack
     */
    protected void processAck(String ack) {
        
    }
    
    public String getData() {
        try {
            char[] c = new char[1024];
            while (inbound.indexOf(String.valueOf(EOR)) == -1) {
                int size = in.read(c);
                if (size == -1) {
                    if (inbound.length() == 0) {
                        return null;
                    } else {
                        String result = inbound.toString();
                        inbound.delete(0, inbound.length());
                        return result;
                    }
                }
                inbound.append(c, 0, size);
            }
            int pos = inbound.indexOf(String.valueOf(EOR));
            String retVal = inbound.substring(0, pos);
            inbound.delete(0, pos + 1);
            return retVal;
        } catch (IOException e) {
            logger.warn("Error reading from connection", e);
            return null;
        }
    }
    
    public void sendMsg(String msg) throws IOException, NullPointerException {
        String outbound = SOB+msg+EOR+RETURN;
        out.write(outbound.getBytes());
        out.flush();
    }
    
    public boolean establishConnection(String host, int port) {
        try {
            s = new Socket(host, port);
            in = new InputStreamReader(s.getInputStream());
            out = s.getOutputStream();
            connected = true;
        } catch (UnknownHostException e) {
            logger.error("Host unknown", e);
            connected = false;
        } catch (IOException e) {
            logger.error("IOException connecting to host "+host, e);
            connected = false;
        }
        return connected;
    }
    
    public boolean closeConnection() {
        try {
            connected = false;
            if (out != null) out.close();
            if (s != null) s.close();
        } catch (IOException e) {
            logger.warn("Couldn't not close connection", e);
            return false;
        }
        return true;
    }
}
