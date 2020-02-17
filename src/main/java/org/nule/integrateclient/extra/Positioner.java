/*
 * Positioner.java
 *
 * Created on January 29, 2007, 9:28 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.nule.integrateclient.extra;

/**
 *
 * @author mike
 */
public class Positioner {
    
    private static int x = 0;
    private static int y = 0;
    
    /** Creates a new instance of Positioner */
    public Positioner() {
    }
    
    public static int getX() {
        if (x > 200) {
            x = 0;
        } else {
            x += 25;
        }
        return x;
    }
    
    public static int getY() {
        if (y > 200) {
            y = 0;
        } else {
            y += 25;
        }
        return y;
    }
    
}
