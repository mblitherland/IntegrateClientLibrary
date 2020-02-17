/*
 * Copyright (C) 2010 M. Litherland
 */

package org.nule.integrateclient.util;

import java.io.File;

/**
 * ConfirmDirectory has the humble goal of confirming a directory exists, or in
 * the event that the directory does not, to attempt to make the directory.
 *
 * @author nule
 */
public class ConfirmDirectory {

    public static boolean confirm(File d) {
        if (d.isDirectory()) {
            if (d.canRead() && d.canWrite()) {
                return true;
            } else {
                return false;
            }
        }
        // Assuming that the ability to make the directory implies the fact that
        // we can read from and write to it.
        return d.mkdir();
    }
}
