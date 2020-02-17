/*
 * FtpClient
 *
 * Copyright (C) 2009 M Litherland
 */
package org.nule.integrateclient.net;

import java.util.Properties;
import org.nule.integrateclient.common.*;

/**
 *
 * @author mike
 */
public class FtpClient extends IntegrateClient {

    public static final String DESCRIPTION = "FtpClient is a base class that is " +
            "meant to be extended by other classes to perform FTP functions";

    protected String name;
    protected Logger logger;
    protected ProcessorAgent pa;
    protected String status = "FtpClient not initialized.";
    protected boolean run = false;
    private boolean available = true;
    protected long count = 0;
    protected boolean connected = false;
    private Thread t;
    protected String remoteHost;
    protected int remotePort;
    protected String remoteUser;
    protected String remotePass;
    protected boolean usePassive = true;
    protected boolean binaryMode = false;
    protected boolean stopOnFailure = true;

    ;
    protected String remoteDir;
    protected String remoteRenameExt;
    protected String localWorkingDir;
    protected String localDoneDir;

    public FtpClient(String name, Logger logger, ProcessorAgent pa) throws
            IllegalArgumentException {
        super(name, logger, pa);
        this.name = name;
        this.logger = logger;
        this.pa = pa;
    }

    public FtpClient(String name, Logger logger, ProcessorAgent pa, Properties p) throws
            IllegalArgumentException {
        super(name, logger, pa, p);
        this.name = name;
        this.logger = logger;
        this.pa = pa;
        loadProperties(p);
    }

    public static Properties getDefaults() {
        Properties p = new Properties();
        p.setProperty("remoteHost", "localhost");
        p.setProperty("remotePort", "21");
        p.setProperty("remoteUser", "username");
        p.setProperty("remotePass", "password");
        p.setProperty("usePassive", "true");
        p.setProperty("binaryMode", "false");
        p.setProperty("remoteDir", "remoteDir");
        p.setProperty("remoteRenameExt", "");
        p.setProperty("localWorkingDir", "work");
        p.setProperty("localDoneDir", "done");
        p.setProperty("stopOnFailure", "true");
        return p;
    }

    public static Properties getMandatory() {
        Properties p = new Properties();
        p.setProperty("remoteHost", "");
        p.setProperty("remotePort", "");
        p.setProperty("remoteUser", "");
        p.setProperty("remotePass", "");
        p.setProperty("usePassive", "");
        p.setProperty("binaryMode", "");
        p.setProperty("remoteDir", "");
        p.setProperty("remoteRenameExt", "");
        p.setProperty("localWorkingDir", "");
        p.setProperty("localDoneDir", "");
        p.setProperty("stopOnFailure", "");
        return p;
    }

    public static Properties getOptional() {
        return null;
    }

    public static Properties getTypes() {
        Properties p = new Properties();
        p.setProperty("remoteHost", FieldTypes.STRING);
        p.setProperty("remotePort", FieldTypes.INTEGER);
        p.setProperty("remoteUser", FieldTypes.STRING);
        p.setProperty("remotePass", FieldTypes.STRING);
        p.setProperty("usePassive", FieldTypes.BOOLEAN);
        p.setProperty("binaryMode", FieldTypes.BOOLEAN);
        p.setProperty("remoteDir", FieldTypes.STRING);
        p.setProperty("remoteRenameExt", FieldTypes.STRING);
        p.setProperty("localWorkingDir", FieldTypes.DIR);
        p.setProperty("localDoneDir", FieldTypes.DIR);
        p.setProperty("stopOnFailure", FieldTypes.BOOLEAN);
        return p;
    }

    public static Properties getDescriptions() {
        Properties p = new Properties();
        p.setProperty("remoteHost", "The hostname or IP to connect to");
        p.setProperty("remotePort", "The port to connect to (default 21)");
        p.setProperty("remoteUser", "The remote user name");
        p.setProperty("remotePass", "The remote password");
        p.setProperty("usePassive", "Set passive mode when we connect (needed for " +
                "some network conditions)");
        p.setProperty("binaryMode", "Set binary mode on all transfers");
        p.setProperty("remoteDir", "The remote dir to scan for files");
        p.setProperty("remoteRenameExt", "An extention to add to the remote file when done " +
                "transferring.  If left blank the remote file will be deleted.");
        p.setProperty("localWorkingDir", "The local working directory to store files " +
                "being transferred.");
        p.setProperty("localDoneDir", "The local done directory to move files to " +
                "after the transfer is complete.");
        p.setProperty("stopOnFailure", "Should a failure occur with FTP or with " +
                "local file operations, stop the client processing.");
        return p;
    }

    public void loadProperties(Properties p) throws IllegalArgumentException {
        remoteHost = p.getProperty("remoteHost");
        if (remoteHost == null) {
            logger.error(name + ": remoteHost must be provided.");
            throw new IllegalArgumentException("Invalid argument");
        }
        remoteUser = p.getProperty("remoteUser");
        if (remoteUser == null) {
            logger.error(name + ": remoteUser must be provided.");
            throw new IllegalArgumentException("Invalid argument");
        }
        remotePass = p.getProperty("remotePass");
        if (remotePass == null) {
            logger.error(name + ": remotePass must be provided.");
            throw new IllegalArgumentException("Invalid argument");
        }
        remoteDir = p.getProperty("remoteDir");
        if (remoteDir == null) {
            logger.error(name + ": remoteDir must be provided.");
            throw new IllegalArgumentException("Invalid argument");
        }
        remoteRenameExt = p.getProperty("remoteRenameExt");
        if (remoteRenameExt == null) {
            logger.error(name + ": remoteRenameExt must be provided.");
            throw new IllegalArgumentException("Invalid argument");
        }
        localWorkingDir = p.getProperty("localWorkingDir");
        if (localWorkingDir == null) {
            logger.error(name + ": localWorkingDir must be provided.");
            throw new IllegalArgumentException("Invalid argument");
        }
        localDoneDir = p.getProperty("localDoneDir");
        if (localDoneDir == null) {
            logger.error(name + ": localDoneDir must be provided.");
            throw new IllegalArgumentException("Invalid argument");
        }
        try {
            remotePort = Integer.parseInt(p.getProperty("remotePort"));
        } catch (NumberFormatException e) {
            logger.error(name + ": Must provide integer for remotePort.");
            throw new IllegalArgumentException("Invalid argument");
        }
        if (p.contains("usePassive")) {
            if (p.get("usePassive").toString().equalsIgnoreCase("false")) {
                usePassive = false;
            }
        }
        if (p.contains("binaryMode")) {
            if (p.get("binaryMode").toString().equalsIgnoreCase("true")) {
                binaryMode = true;
            }
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
        p.setProperty("remoteHost", remoteHost);
        p.setProperty("remotePort", Integer.toString(remotePort));
        p.setProperty("remoteUser", "****");
        p.setProperty("remotePass", "****");
        p.setProperty("usePassive", Boolean.toString(usePassive));
        p.setProperty("binaryMode", Boolean.toString(binaryMode));
        p.setProperty("remoteDir", remoteDir);
        p.setProperty("remoteRenameExt", remoteRenameExt);
        p.setProperty("localWorkingDir", localWorkingDir);
        p.setProperty("localDoneDir", localDoneDir);
        p.setProperty("stopOnFailure", Boolean.toString(stopOnFailure));
        p.put("status", status);
        p.put("count", Long.toString(count));
        p.put("connected", Boolean.toString(connected));
        return p;
    }

    public Boolean isConnected() {
        return connected;
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
        status = "FtpClient is shutdown.";
        return true;
    }

    public boolean startClient() {
        if (remoteHost == null) {
            return false;
        }
        if (remotePort < 1 || remotePort > 65535) {
            logger.warn(name + ": Invalid range for remotePort.");
            return false;
        }
        status = "FtpClient starting.";
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
        throw new RuntimeException("Not implemented in FtpClient base class");
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
