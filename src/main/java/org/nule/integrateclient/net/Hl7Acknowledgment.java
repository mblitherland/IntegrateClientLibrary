/*
 * Hl7Acknowledgment.java
 *
 * Created on March 30, 2006, 12:45 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.nule.integrateclient.net;

import org.nule.integrateclient.common.*;
import org.nule.lighthl7lib.util.*;

/**
 *
 * @author litherm
 */
public class Hl7Acknowledgment implements AcknowledgmentAgent {
    
    public static final String DESCRIPTION = "This acknowledgment tries to " +
            "format a simple MSA message from the content of the data passed to " +
            "it.";
    
    /** Creates a new instance of Hl7Acknowledgment */
    public Hl7Acknowledgment() {
    }

    public String generateAck(String data, boolean pass) {
        FormatAck fa = new FormatAck(data);
        if (pass)
            return fa.getAck();
        return fa.getNack();
    }
    
}
