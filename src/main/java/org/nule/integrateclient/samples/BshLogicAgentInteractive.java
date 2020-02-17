/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nule.integrateclient.samples;

import java.util.*;
import javax.swing.SwingUtilities;

import org.nule.integrateclient.common.*;
import org.nule.integrateclient.extra.*;

/**
 *
 * @author mike
 */
public class BshLogicAgentInteractive extends BshLogicAgent {
    
    public static final String DESCRIPTION = "This agent utilizes beanshell " +
            "to run a script you create against the messages as they are " +
            "processed.  This is the interactive form of the logic class, so " +
            "you will be provided a window to edit your scripts in, and a number " +
            "of base scripts from which you can begin.  The other Beanshell logic " +
            "class is for non-interactive use and can run a script created using " +
            "this agent.";
    
    private BshLogicFrame logicFrame;
    
    public BshLogicAgentInteractive(String name, Logger logger) {
        super(name, logger);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                logicFrame = BshLogicFrame.getInstance();
            }
        });
    }

    protected String getEval() {
        return logicFrame.getScript();
    }

    protected void showErrors(List e) {
        logicFrame.showDialog(e);
    }

    protected boolean runInit() {
        return logicFrame.runInit();
    }
}
