/*
 * MllpThreadServerConn.java
 *
 * Created on March 29, 2006, 9:42 AM
 *
 * Copyright (C) 2004-2008 M Litherland
 */

package org.nule.integrateclient.net;

import java.io.*;
import java.net.*;

import org.nule.integrateclient.common.*;

/**
 *
 * @author litherm
 */
public class MllpThreadServerConn implements Runnable {
    
    public static final char RETURN = 13;
    
    public static final char SOB = 11;
    
    public static final char EOR = 28;
    
    private Socket s;
    
    private MllpThreadServer mts;
    
    private StringBuffer inbound;
    
    boolean connected = true;
    
    private InputStreamReader in;
    
    private OutputStreamWriter out;
    
    private long count = 0;
    
    private String status = "Server Thread starting.";
    
    private String remote;
    
    private boolean run = true;
    
    private Thread t;
    
    private Logger logger;
    
    private String lastError;
    
    protected String name;
    
    public MllpThreadServerConn(String name, Socket s, MllpThreadServer mts, Logger logger) {
        this.name = name;
        this.s = s;
        this.mts = mts;
        this.logger = logger;
        inbound = new StringBuffer();
        try {
            in = new InputStreamReader(s.getInputStream());
            out = new OutputStreamWriter(s.getOutputStream());
            connected = true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            // This just means we were interrupted.
        }
        remote = s.getRemoteSocketAddress().toString();
    }
    
    public long getCount() {
        return count;
    }
    
    /**
     * This is so that the mllpthreadserver that controls this connection can
     * update the status.
     */
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getStatus() {
        return status;
    }
    
    public String getRemote() {
        return remote;
    }
    
    public void stopClient() {
        logger.trace(name + ": ServerConn told to stop.");
        run = false;
        closeConnection();
        if (t != null && t.isAlive())
            t.interrupt();
        t = null;
    }
    
    public void startClient() {
        logger.trace(name + ": ServerConn told to start.");
        t = new Thread(this);
        t.start();
    }
    
    public void run() {
        run = true;
        try {
            while (run) {
                status = "Waiting for data";
                String fromClient = getData();
                if (fromClient == null) {
                    closeConnection();
                    mts.stopClient(this);
                    logger.trace(name + ": Got null data from connection - stopping");
                } else {
                    count++;
                    logger.trace(name + ": Got data from client.");
                    mts.incomingData(fromClient, this);
                }
            }
        } catch (Exception e) {
            status = "MllpThreadServerConn exception - "+e;
            logger.error(name + ": Exception - "+e);
        } finally {
            run = false;
        }
    }
    
    public boolean sendData(String data) {
        try {
            logger.trace(name + " in send data");
            String outbound = SOB+data+EOR+RETURN;
            out.write(outbound);
            out.flush();
            logger.trace(name + " send data complete");
        } catch (IOException e) {
            lastError = "IOException writing to " + s.toString() + ": "
                    + e.getLocalizedMessage();
            return false;
        }
        return true;
    }
    
    public boolean closeConnection() {
        try {
            connected = false;
            if (out != null) out.close();
            if (s != null) s.close();
        } catch (IOException e) {
            lastError = "Could not close socket:" + e.getLocalizedMessage();
            return false;
        }
        return true;
    }
    
    public String getData() {
        try {
            char[] c = new char[1024];
            while (inbound.indexOf(String.valueOf(EOR)) == -1) {
                logger.trace(name + " about to read currentbuffer size: "+inbound.length()+"("+inbound.toString()+")");
                logger.trace(name + " in.ready: "+in.ready());
                int size = in.read(c);
                logger.trace(name + " read "+size+" bytes");
                if (size == -1) {
                    in.close();
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
            logger.trace(name + " returning on "+retVal.length()+" chars in getdata");
            return retVal;
        } catch (IOException e) {
            lastError = "IOException reading from " + s.toString() + ": "
                    + e.getLocalizedMessage();
            return null;
        }
    }
    
    public boolean isConnected() {
        return connected;
    }
    
    public String getLastError() {
        return lastError;
    }
}
