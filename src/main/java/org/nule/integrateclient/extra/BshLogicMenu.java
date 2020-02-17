/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nule.integrateclient.extra;

import java.awt.event.*;

import javax.swing.*;
import javax.swing.text.*;

import org.nule.nuleditor.gui.*;

/**
 *
 * @author mike
 */
public class BshLogicMenu extends JMenuBar {
    
    private AppController controller;
    
    public BshLogicMenu(AppController controller) {
        this.controller = controller;
        JMenu file = new JMenu("File");
        file.setMnemonic(KeyEvent.VK_F);
        add(file);
        
        JMenuItem mapping = new JMenuItem("New logic script");
        mapping.setMnemonic(KeyEvent.VK_M);
        mapping.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                newEvent(ae);
            }
        });
        file.add(mapping);
        file.addSeparator();
        JMenuItem save = new JMenuItem("Save script");
        save.setMnemonic(KeyEvent.VK_S);
        save.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                saveEvent(ae);
            }
        });
        file.add(save);
        JMenuItem load = new JMenuItem("Load script");
        load.setMnemonic(KeyEvent.VK_L);
        load.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                loadEvent(ae);
            }
        });
        file.add(load);
        JMenu edit = new JMenu("Edit");
        edit.setMnemonic(KeyEvent.VK_E);
        add(edit);
        JMenuItem undo = new JMenuItem("Undo (C-z)");
        undo.setMnemonic(KeyEvent.VK_U);
        undo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                undoEvent(ae);
            }
        });
        edit.add(undo);
        JMenuItem redo = new JMenuItem("Redo (C-y)");
        redo.setMnemonic(KeyEvent.VK_R);
        redo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                redoEvent(ae);
            }
        });
        edit.add(redo);
        edit.addSeparator();
        JMenuItem cut = new JMenuItem(new DefaultEditorKit.CutAction());
        cut.setText("Cut (C-x)");
        cut.setMnemonic(KeyEvent.VK_X);
        edit.add(cut);
        JMenuItem copy = new JMenuItem(new DefaultEditorKit.CopyAction());
        copy.setText("Copy (C-c)");
        copy.setMnemonic(KeyEvent.VK_C);
        edit.add(copy);
        JMenuItem paste = new JMenuItem(new DefaultEditorKit.PasteAction());
        paste.setText("Paste (C-v)");
        paste.setMnemonic(KeyEvent.VK_V);
        edit.add(paste);
        edit.addSeparator();
        JMenuItem blockComment = new JMenuItem("Block comment");
        blockComment.setMnemonic(KeyEvent.VK_B);
        blockComment.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                blockCommentEvent(ae);
            }
        });
        edit.add(blockComment);
        JMenuItem blockUncomment = new JMenuItem("Block uncomment");
        blockUncomment.setMnemonic(KeyEvent.VK_K);
        blockUncomment.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                blockUncommentEvent(ae);
            }
        });
        edit.add(blockUncomment);
        edit.addSeparator();
        JMenuItem clearTabs = new JMenuItem("Convert tabs to spaces");
        clearTabs.setMnemonic(KeyEvent.VK_T);
        clearTabs.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                clearTabsEvent(ae);
            }
        });
        edit.add(clearTabs);
    }
    
    private void newEvent(ActionEvent ae) {
        int result = JOptionPane.showConfirmDialog(this,
                "Are you sure you wish to replace the script with a new one?\n" +
                "Changes will be lost.", "Confirm clear", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            controller.performEvent(new AppEvent(AppEvent.LOAD_NEW_SNIPPIT_1));
        }
    }
    
    private void saveEvent(ActionEvent ae) {
        controller.performEvent(new AppEvent(AppEvent.SAVE_SNIPPIT));
    }
    
    private void loadEvent(ActionEvent ae) {
        int result = JOptionPane.showConfirmDialog(this,
                "Are you sure you wish to replace the script with a new one?\n" +
                "Changes will be lost.", "Confirm clear", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            controller.performEvent(new AppEvent(AppEvent.LOAD_SNIPPIT));
        }
    }
    
    private void undoEvent(ActionEvent ae) {
        controller.performEvent(new AppEvent(AppEvent.UNDO_ACTION));
    }
    
    private void redoEvent(ActionEvent ae) {
        controller.performEvent(new AppEvent(AppEvent.REDO_ACTION));
    }
    
    private void blockCommentEvent(ActionEvent ae) {
        controller.performEvent(new AppEvent(AppEvent.BLOCK_COMMENT));
    }
    
    private void blockUncommentEvent(ActionEvent ae) {
        controller.performEvent(new AppEvent(AppEvent.BLOCK_UNCOMMENT));
    }
    
    private void clearTabsEvent(ActionEvent ae) {
        controller.performEvent(new AppEvent(AppEvent.CLEAR_TABS_ACTION));
    }
}
