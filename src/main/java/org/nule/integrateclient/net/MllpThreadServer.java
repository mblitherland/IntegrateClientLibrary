/*
 * MllpThreadServer.java
 *
 * Created on March 29, 2006, 9:42 AM
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
 */
public class MllpThreadServer extends IntegrateClient implements InboundClient {
    
    public static final String DESCRIPTION = "This inbound client (server, " +
            "actually) listens on a port, spawns new children to handle incoming " +
            "data and handles any data in MLLP wrappers.";
    
    private ServerSocket ss;
    
    protected String lastError = "";
    
    protected boolean connected = false;
    
    protected int listenPort = -1;
    
    protected String status = "MllpThreadServer not initialized.";
    
    protected Logger logger;
    
    protected boolean run;
    
    private Thread t;
    
    protected List connections = new ArrayList();
    
    private ProcessorAgent pa;
    
    private AcknowledgmentAgent aa;
    
    protected String name;
    
    public MllpThreadServer(String name, Logger logger, ProcessorAgent pa) {
        super(name, logger, pa);
        this.name = name;
        this.logger = logger;
        this.pa = pa;
    }
    
    public MllpThreadServer(String name, Logger logger, ProcessorAgent pa, Properties p) {
        super(name, logger, pa, p);
        this.name = name;
        this.logger = logger;
        this.pa = pa;
        loadProperties(p);
    }
    
    public static Properties getDefaults() {
        Properties p = new Properties();
        p.setProperty("listenPort", "12345");
        p.setProperty("acknowledgmentAgent", "org.nule.integrateclient.net.MllpAcknowledgment");
        return p;
    }

    public static Properties getMandatory() {
        Properties p = new Properties();
        p.setProperty("listenPort", "");
        p.setProperty("acknowledgmentAgent", "");
        return p;
    }
    
    public static Properties getOptional() {
        return null;
    }
    
    public static Properties getTypes() {
        Properties p = new Properties();
        p.setProperty("listenPort", FieldTypes.INTEGER);
        p.setProperty("acknowledgmentAgent", FieldTypes.CLASS);
        return p;
    }
    
    public static Properties getDescriptions() {
        Properties p = new Properties();
        p.setProperty("listenPort", "The port this client will listen on.");
        p.setProperty("acknowledgmentAgent", "Class that produce the acknowledgment.");
        return p;
    }
    
        /* (non-Javadoc)
         * @see org.nule.integrate.client.IntegrateClientMBean#loadProperties(java.util.Properties)
         */
    public void loadProperties(Properties p) throws IllegalArgumentException {
        try {
            listenPort = Integer.parseInt(p.getProperty("listenPort"));
        } catch (NumberFormatException e) {
            logger.error(name + ": Must provide integer for listenPort.");
            throw new IllegalArgumentException("Invalid argument");
        }
        try {
            Class c = IclClassLoader.getInstance().loadClass(p.getProperty("acknowledgmentAgent"));
            aa = (AcknowledgmentAgent) c.newInstance();
        } catch (ClassNotFoundException e) {
            logger.error(name + ": acknowledgmentAgent class not found.");
            throw new IllegalArgumentException("Invalid argument");
        } catch (InstantiationException e) {
            logger.error(name + ": acknowledgmentAgent class could not be instantiated.");
            throw new IllegalArgumentException("Invalid argument");
        } catch (IllegalAccessException e) {
            logger.error(name + ": acknowledgmentAgent class threw illegal argument exception.");
            throw new IllegalArgumentException("Invalid argument");
        }
        status = "MllpThreadServer configured.";
    }
    
        /* (non-Javadoc)
         * @see org.nule.integrate.client.IntegrateClientMBean#getStatus()
         */
    public Properties getStatus() {
        logger.debug(name + ": Handling request for status.");
        Properties p = new Properties();
        p.put("listenPort", Integer.toString(listenPort));
        p.put("status", status);
        long count = 0;
        boolean someConnection = false;
        for (int i = 0; i < connections.size(); i++) {
            MllpThreadServerConn mtsc = (MllpThreadServerConn) connections.get(i);
            count += mtsc.getCount();
            p.put("Thread"+i+"count", Long.toString(mtsc.getCount()));
            p.put("Thread"+i+"status", mtsc.getStatus());
            p.put("Thread"+i+"remote", mtsc.getRemote());
            p.put("Thread"+i+"connected", mtsc.isConnected() ? "True" : "False");
            if (mtsc.isConnected()) {
                someConnection = true;
            }
        }
        p.put("count", Long.toString(count));
        p.put("connected", someConnection ? "True" : "False");
        return p;
    }
    
        /* (non-Javadoc)
         * @see org.nule.integrate.client.IntegrateClientMBean#getState()
         */
    public String getState() {
        return status;
    }
    
        /* (non-Javadoc)
         * @see org.nule.integrate.client.IntegrateClientMBean#stopClient()
         */
    public boolean stopClient() {
        status = "MllpThreadServer shutting down.";
        logger.info(name + ": Stopping all clients.");
        for (int i = 0; i < connections.size(); i++) {
            MllpThreadServerConn mtsc = (MllpThreadServerConn) connections.get(i);
            mtsc.stopClient();
        }
        logger.info(name + ": Stopping server.");
        connections.clear();
        closeConnection();
        if (t != null && t.isAlive())
            t.interrupt();
        t = null;
        run = false;
        status = "MllpThreadServer shut down.";
        return true;
    }
    
    public boolean stopClient(MllpThreadServerConn mtsc) {
        logger.info(name + ": Stopping client: "+mtsc.toString());
        connections.remove(mtsc);
        mtsc.stopClient();
        return true;
    }
    
        /* (non-Javadoc)
         * @see org.nule.integrate.client.IntegrateClientMBean#startClient()
         */
    public boolean startClient() {
        if (listenPort < 1 || listenPort > 65535) {
            logger.warn(name + ": listenPort has not been set.");
            return false;
        }
        if (aa == null) {
            logger.warn(name + ": acknowledgmentAgent has not been set.");
            return false;
        }
        status = "MllpThreadServer starting.";
        logger.info(name + ": Client told to start.");
        if (isRunning())
            stopClient();
        t = new Thread(this);
        t.start();
        return true;
    }
    
        /* (non-Javadoc)
         * @see org.nule.integrate.client.IntegrateClientMBean#isRunning()
         */
    public boolean isRunning() {
        return run;
    }
    
    public long getCount() {
        long count = 0;
        for (int i = 0; i < connections.size(); i++) {
            MllpThreadServerConn mtsc = (MllpThreadServerConn) connections.get(i);
            count += mtsc.getCount();
        }
        return count;
    }
    
        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
    public void run() {
        run = true;
        status = "MllpThreadServer waiting for a connection.";
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
                MllpThreadServerConn mtsc = new MllpThreadServerConn(name, s, this, logger);
                logger.info(name + ": Starting client: "+mtsc.toString());
                mtsc.startClient();
                connections.add(mtsc);
            }
            closeConnection();
        } catch (InterruptedException e) {
            status = "MllpThreadServer interrupted.";
            logger.warn(name + ": Thread interrupted.");
        } catch (Exception e) {
            status = "MllpThreadServer Exception - "+e;
            logger.error(name + ": Exception - "+e);
        } finally {
            run = false;
        }
    }
    
    public synchronized void incomingData(String fromClient,
            MllpThreadServerConn mtsc) {
        logger.debug(name + ": Data from: "+mtsc.toString());
        logger.info(name + ": "+fromClient);
        String ack = aa.generateAck(fromClient, pa.dataTransfer(fromClient));
        logger.info(name + ": Sending acknowledgment: "+ack);
        mtsc.setStatus("Sending acknowledgment.");
        if (mtsc.sendData(ack)) {
            logger.debug(name + ": Acknowledgment sent");
            mtsc.setStatus("Acknowledgment sent.");
        } else {
            logger.warn(name + ": Acknowledgment not sent: "+mtsc.getLastError());
            mtsc.setStatus("Acknowledgment sent.");
        }
    }
    
    public boolean establishConnection(int port) {
        try {
            ss = new ServerSocket(port);
            connected = true;
        } catch (IOException e) {
            lastError = "Could not open server port " + Integer.toString(port)
            + ", is it in use?";
            connected = false;
        }
        return connected;
    }
    
    public Socket acceptConnection() {
        try {
            // This'll block
            Socket s = ss.accept();
            return s;
        } catch (IOException e) {
            lastError = "Failure accepting connection: "+e.getLocalizedMessage();
            return null;
        }
    }
    
    public boolean closeConnection() {
        try {
            connected = false;
            if (ss != null) ss.close();
        } catch (IOException e) {
            lastError = "Could not close socket:" + e.getLocalizedMessage();
            return false;
        }
        return true;
    }
    
    public Boolean isConnected() {
        boolean someConnection = false;
        for (int i = 0; i < connections.size(); i++) {
            MllpThreadServerConn mtsc = (MllpThreadServerConn) connections.get(i);
            if (mtsc.isConnected()) {
                someConnection = true;
            }
        }
        return someConnection;
    }
}
