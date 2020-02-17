/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nule.integrateclient.net;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.Properties;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.nule.integrateclient.common.*;

/**
 *
 * @author mike
 */
public class FtpClientIn extends FtpClient implements OutboundClient {

    public static final String DESCRIPTION = "This FTP client reads files from " +
            "a remote directory and stores them to a local directory.  Once " +
            "the read is complete it removes or renames the remote file.  A separate " +
            "file inbound client should be used to then read the data files in. " +
            "This may seem needlessly complex, but it prevents the need to " +
            "support a number of different data types from the perspective of the " +
            "ftp client, and lets you handle processed files however you like " +
            "locally.  It also prevents a situation where a very large file could " +
            "run ICL out of memory.  It's technically an outbound client so that " +
            "you can use another event (for example InboundTrigger) to trigger " +
            "the client.  Any message it receives will do this.";

    public FtpClientIn(String name, Logger logger, ProcessorAgent pa) throws
            IllegalArgumentException {
        super(name, logger, pa);
        this.name = name;
        this.logger = logger;
        this.pa = pa;
    }

    public FtpClientIn(String name, Logger logger, ProcessorAgent pa, Properties p) throws
            IllegalArgumentException {
        super(name, logger, pa, p);
        this.name = name;
        this.logger = logger;
        this.pa = pa;
        loadProperties(p);
    }

    public void run() {
        status = "FtpClientIn running";
        run = true;
        try {
            while (run) {
                if (getMessage()) {
                    logger.info("FtpClientIn received message to run");
                    FTPClient ftp = new FTPClient();
                    try {
                        ftp.connect(remoteHost, remotePort);
                        ftp.login(remoteUser, remotePass);
                        int reply = ftp.getReplyCode();
                        if (!FTPReply.isPositiveCompletion(reply)) {
                            ftp.disconnect();
                            logger.warn("Failure logging in to FTP "+ftp.getReplyString());
                            Thread.sleep(60000);
                            continue;
                        }
                        connected = true;
                        ftp.changeWorkingDirectory(remoteDir);
                        if (!FTPReply.isPositiveCompletion(reply)) {
                            ftp.disconnect();
                            logger.warn("Failure changing directory "+ftp.getReplyString());
                            Thread.sleep(60000);
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
                        FTPFile[] files = ftp.listFiles();
                        for (int i = 0; i < files.length; i++) {
                            try {
                                FTPFile file = files[i];
                                String fileName = file.getName();
                                if (!remoteRenameExt.equals("") &&
                                        fileName.endsWith(remoteRenameExt)) {
                                    continue;
                                }
                                File out = new File(localWorkingDir+
                                        System.getProperty("file.separator")+fileName);
                                FileOutputStream fos = new FileOutputStream(out);
                                logger.info(name+" : Retrieving remote file "+fileName+
                                        " of size "+file.getSize()+" bytes");
                                boolean retrieveSucceeded = true;
                                if (!ftp.retrieveFile(fileName, fos)) {
                                    retrieveSucceeded = false;
                                    logger.error("Couldn't retrieve "+fileName);
                                    if (stopOnFailure) {
                                        run = false;
                                        break;
                                    }
                                }
                                fos.close();
                                File done = new File(localDoneDir+
                                        System.getProperty("file.separator")+fileName);
                                if (!out.renameTo(done)) {
                                    logger.error("Couldn't move local file to "+done.getName());
                                    if (stopOnFailure) {
                                        run = false;
                                        break;
                                    }
                                }
                                if (retrieveSucceeded) {
                                    if (remoteRenameExt.equals("")) {
                                        if (!ftp.deleteFile(fileName)) {
                                            logger.error("Couldn't delete file "+fileName);
                                            if (stopOnFailure) {
                                                run = false;
                                                break;
                                            }
                                        }
                                    } else {
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
                                if (stopOnFailure) {
                                    run = false;
                                    break;
                                }
                            }
                            count++;
                        }
                        ftp.logout();
                    } catch (SocketException e) {
                        logger.warn("Socket exception trying to connect to (" +
                                remoteHost + ":" + remotePort+") "+e);
                        Thread.sleep(60000);
                    } catch (IOException e) {
                        logger.warn("IOException in FTP: "+e);
                        Thread.sleep(60000);
                    } finally {
                        if (ftp.isConnected()) {
                            try {
                                ftp.disconnect();
                            } catch (IOException e) {
                                logger.warn("IOException disconnecting from FTP");
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
            logger.info(name+" Interrupted.");
        } finally {
            run = false;
        }
    }
}
