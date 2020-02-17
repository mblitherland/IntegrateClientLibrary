/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nule.integrateclient.common;

import java.util.Date;

/**
 *
 * @author mike
 */
public interface DetailedLoggerClient {
    public void putMessage(Date timeStamp, String level, String message);
}
