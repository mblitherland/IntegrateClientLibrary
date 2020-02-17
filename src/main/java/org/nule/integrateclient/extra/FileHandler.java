/*
 * FileHandler.java
 *
 * Created on March 29, 2006, 3:58 PM
 *
 * Copyright (C) 2004-2012 M Litherland
 */

package org.nule.integrateclient.extra;

import java.util.*;
import javax.swing.*;

/**
 *
 * @author litherm
 */
public class FileHandler {
    
    private volatile static FileHandler fh = null;
    private Map chooserMap = new TreeMap();
    
    /**
     * The constructor needs to be protected.
     *
     */
    private FileHandler() {
    }
    
    /**
     * Return the JFileChooser we have created, or create a new one.
     * 
     * @return - A JFileChooser
     */
    public static FileHandler getFileHandler() {
        if (fh == null) {
            fh = new FileHandler();
        }
        return fh;
    }
    
    /**
     * 
     * 
     */
    public JFileChooser getFileChooser(String name) {
        if (chooserMap.keySet().contains(name)) {
            return (JFileChooser) chooserMap.get(name);
        } else {
            JFileChooser jfc = new JFileChooser(System.getProperty("user.dir"));
            chooserMap.put(name, jfc);
            return jfc;
        }
    }
}
