/*
 * OutboundClient.java
 *
 * Created on March 28, 2006, 11:29 AM
 *
 * Copyright (C) 2006 M Litherland
 */

package org.nule.integrateclient.common;

/**
 *
 * @author litherm
 */
public interface OutboundClient {
    
    /**
     * Give the client a message that it is to process and send.
     */
    public void putMessage(String message);
    
}
