/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nule.integrateclient.extra;

import java.awt.*;

import javax.swing.*;

import org.nule.nuleditor.gui.*;

/**
 *
 * @author mike
 */
public class BshLogicFrame extends JFrame {
    
    private AppController controller = new AppController();
    private BshLogicForm logicForm;
    
    private static BshLogicFrame me = null;
    
    private BshLogicFrame() {
        setTitle("HL7 Comm Logic Editor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        logicForm = new BshLogicForm();
        controller.addListener(logicForm);
        this.setJMenuBar(new BshLogicMenu(controller));
        setLayout(new BorderLayout());
        add(logicForm, BorderLayout.CENTER);
        pack();
        setSize(800, 600);
        setLocation(Positioner.getX(), Positioner.getY());
        setVisible(true);
        new BshLoaderFrame(logicForm);
    }
    
    public static BshLogicFrame getInstance() {
        if (me == null) {
            me = new BshLogicFrame();
        }
        return me;
    }
    
    public String getScript() {
        return logicForm.getScript();
    }
    
    public void showDialog(java.util.List lines) {
        toFront();
        logicForm.showError(lines);
    }
    
    public boolean runInit() {
        return logicForm.runInit();
    }
}
