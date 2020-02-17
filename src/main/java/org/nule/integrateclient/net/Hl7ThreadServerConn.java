/*
 * Hl7ThreadServerConn.java
 *
 * Created on March 31, 2006, 9:40 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.nule.integrateclient.net;

import java.net.*;

import org.nule.integrateclient.common.*;
import org.nule.lighthl7lib.hl7.*;

/**
 *
 * @author litherm
 */
public class Hl7ThreadServerConn extends MllpThreadServerConn {
    
    /** Creates a new instance of Hl7ThreadServerConn */
    public Hl7ThreadServerConn(String name, Socket s, MllpThreadServer mts, Logger logger) {
        super(name, s, mts, logger);
    }
    
    public String getData() {
        return Hl7Record.cleanString(super.getData());
    }
    
}
