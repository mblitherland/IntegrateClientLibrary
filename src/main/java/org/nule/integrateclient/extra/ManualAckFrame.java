/*
 * ManualAckFrame.java
 *
 * Created on September 28, 2007, 6:26 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.nule.integrateclient.extra;

import javax.swing.*;

import org.nule.integrateclient.net.*;

/**
 *
 * @author mike
 */
public class ManualAckFrame extends JFrame {
    
    private ManualAckForm form;
    
    /** Creates a new instance of ManualAckFrame */
    public ManualAckFrame() {
        setTitle("HL7 Manual Acknowledgment");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        add(ManualAckForm.getInstance());
        pack();
        setLocation(Positioner.getX(), Positioner.getY());
        setVisible(true);
    }
    
    
    
}
