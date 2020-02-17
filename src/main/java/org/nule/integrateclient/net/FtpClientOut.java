/*
 * FtpClientOut.java
 * 
 * Copyright (C) 2008-2012 M. Litherland
 */

package org.nule.integrateclient.net;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.Properties;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.nule.integrateclient.common.*;

/**
 *
 * @author mike
 */
public class FtpClientOut extends FtpClient implements OutboundClient {

    public static final String DESCRIPTION = "This FTP client reads files from " +
            "a local directory and stores them on a remote FTP server.  Once " +
            "the read is complete it renames the remote file, and moves the local. " +
            "If necessary a separate outbound client could prepare the files for " +
            "this client to read.  " +
            "This may seem needlessly complex, but it prevents the need to " +
            "support a number of different data types from the perspective of the " +
            "ftp client, and lets you handle processed files however you like " +
            "locally.  It also prevents a situation where a very large file could " +
            "run ICL out of memory.  It's technically an outbound client so that " +
            "you can use another event (for example InboundTrigger) to trigger " +
            "the client.  Any message it receives will do this.";
    
    private static final long MINUTE = 60000;

    public FtpClientOut(String name, Logger logger, ProcessorAgent pa) throws
            IllegalArgumentException {
        super(name, logger, pa);
        this.name = name;
        this.logger = logger;
        this.pa = pa;
    }

    public FtpClientOut(String name, Logger logger, ProcessorAgent pa, Properties p) throws
            IllegalArgumentException {
        super(name, logger, pa, p);
        this.name = name;
        this.logger = logger;
        this.pa = pa;
        loadProperties(p);
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
        p.setProperty("remoteDir", "The remote dir to upload files");
        p.setProperty("remoteRenameExt", "An extention to add to the remote file when done " +
                "transferring.  If left blank the remote file will not be renamed upon completion.");
        p.setProperty("localWorkingDir", "The local working directory to scan for files " +
                "to be transferred.");
        p.setProperty("localDoneDir", "The local done directory to move files to " +
                "after the transfer is complete.");
        p.setProperty("stopOnFailure", "Should a failure occur with FTP or with " +
                "local file operations, stop the client processing.");
        return p;
    }

    public void run() {
        status = "FtpClientOut running";
        run = true;
        try {
            while (run) {
                if (getMessage()) {
                    logger.info("FtpClientOut received message to run");
                    File work = new File(localWorkingDir);
                    File[] workFiles = work.listFiles();
                    if (workFiles.length < 1) {
                        logger.info("No files to process, waiting for next signal");
                        continue;
                    }
                    FTPClient ftp = new FTPClient();
                    try {
                        ftp.connect(remoteHost, remotePort);
                        ftp.login(remoteUser, remotePass);
                        int reply = ftp.getReplyCode();
                        if (!FTPReply.isPositiveCompletion(reply)) {
                            ftp.disconnect();
                            logger.warn("Failure logging in to FTP "+ftp.getReplyString());
                            Thread.sleep(MINUTE);
                            continue;
                        }
                        connected = true;
                        ftp.changeWorkingDirectory(remoteDir);
                        if (!FTPReply.isPositiveCompletion(reply)) {
                            ftp.disconnect();
                            logger.warn("Failure changing directory "+ftp.getReplyString());
                            Thread.sleep(MINUTE);
                            continue;
                        }
                        if (usePassive) {
                            ftp.enterLocalPassiveMode();
                        } else {
                            ftp.enterLocalActiveMode();
                        }
                        if (binaryMode) {
                            ftp.setFileType(FTP.BINARY_FILE_TYPE);
                        } else {
                            ftp.setFileType(FTP.ASCII_FILE_TYPE);
                        }
                        for (int i = 0; i < workFiles.length; i++) {
                            try {
                                File file = workFiles[i];
                                String fileName = file.getName();
                                FileInputStream fis = new FileInputStream(file);
                                logger.info(name+" : Storing remote file "+fileName+
                                        " of size "+file.length()+" bytes");
                                boolean storeSucceeded = true;
                                if (!ftp.storeFile(fileName, fis)) {
                                    storeSucceeded = false;
                                    logger.error("Couldn't store "+fileName);
                                    if (stopOnFailure) {
                                        run = false;
                                        break;
                                    }
                                }
                                fis.close();
                                if (storeSucceeded) {
                                    File done = new File(localDoneDir+
                                            System.getProperty("file.separator")+fileName);
                                    file.renameTo(done);
                                    if (!remoteRenameExt.equals("")) {
                                        if (!ftp.rename(fileName, fileName+remoteRenameExt)) {
                                            logger.error("Couldn't rename file "+fileName);
                                            if (stopOnFailure) {
                                                run = false;
                                                break;
                                            }
                                        }
                                    }
                                }
                            } catch (IOException e) {
                                logger.error("IOException in FTP: "+e);
                            }
                            count++;
                        }
                        ftp.logout();
                    } catch (SocketException e) {
                        logger.warn("Socket exception trying to connect to (" +
                                remoteHost + ":" + remotePort+") "+e);
                        Thread.sleep(MINUTE);
                    } catch (IOException e) {
                        logger.warn("IOException in FTP: "+e);
                        Thread.sleep(MINUTE);
                    } finally {
                        if (ftp.isConnected()) {
                            try {
                                ftp.disconnect();
                            } catch (IOException e) {
                                // do nothing
                            }
                        }
                        connected = false;
                    }

                }
                // Just prevent tight looping if something goes wrong with the
                // wait/notify.
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
        } finally {
            run = false;
        }
    }
}
