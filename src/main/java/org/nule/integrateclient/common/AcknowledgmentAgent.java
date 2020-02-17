/*
 * AcknowledgmentAgent.java
 *
 * Created on March 30, 2006, 9:41 AM
 *
 * Copyright (C) 2006 M Litherland
 */

package org.nule.integrateclient.common;

/**
 *
 * @author litherm
 */
public interface AcknowledgmentAgent {
    
    public static final String DESCRIPTION = "Please add a description for this acknowledgment";
    
    public String generateAck(String data, boolean pass);
    
}
