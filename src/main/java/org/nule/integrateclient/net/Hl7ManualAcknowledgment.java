/*
 * Hl7ManualAcknowldgment.java
 *
 * Created on September 28, 2007, 6:30 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.nule.integrateclient.net;

import javax.swing.SwingUtilities;

import org.nule.integrateclient.extra.*;

/**
 *
 * @author mike
 */
public class Hl7ManualAcknowledgment extends Hl7Acknowledgment {
    
    private String lastReply = null;
    
    /**
     * Creates a new instance of Hl7ManualAcknowledgment
     */
    public Hl7ManualAcknowledgment() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ManualAckFrame();
            }
        });
    }
    
    public void setReply(String reply) {
        synchronized (this) {
            lastReply = reply;
        }
    }
    
    public String generateAck(String data, boolean pass) {
        ManualAckForm.getInstance().getReply(data, pass, this);
        String reply = null;
        while (reply == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {}
            synchronized (this) {
                if (lastReply != null) {
                    reply = lastReply;
                    lastReply = null;
                }
            }
        }
        return reply;
    }
    
}
