/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nule.integrateclient.extra;

import java.util.Date;

import org.nule.integrateclient.net.Hl7ManualAcknowledgment;

/**
 *
 * @author mike
 */
public class ManualAckBean {
    
    private String data;
    private Date time;
    private boolean ok;
    private Hl7ManualAcknowledgment ack;
    
    public ManualAckBean(String data, boolean ok, Hl7ManualAcknowledgment ack) {
        this.data = data;
        this.ok = ok;
        this.ack = ack;
        time = new Date();
    }
    
    public String getData() {
        return data;
    }
    
    public Date getTime() {
        return time;
    }
    
    public boolean getOk() {
        return ok;
    }
    
    public Hl7ManualAcknowledgment getAck() {
        return ack;
    }
}
