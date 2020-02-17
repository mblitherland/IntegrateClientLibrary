/*
 * MllpAcknowledgment.java
 *
 * Created on March 30, 2006, 10:52 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.nule.integrateclient.net;

import org.nule.integrateclient.common.*;

/**
 *
 * @author litherm
 */
public class MllpAcknowledgment implements AcknowledgmentAgent {
    
    public static final String DESCRIPTION = "This acknowledgment class simply " +
            "returns an 'A' for an acknowledgment and a 'N' for a not " +
            "acknowledgment.";
    
    /** Creates a new instance of MllpAcknowledgment */
    public MllpAcknowledgment() {
    }

    public String generateAck(String data, boolean pass) {
        if (pass)
            return "A";
        return "N";
    }
    
}
