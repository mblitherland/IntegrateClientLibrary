/* 
 * Copyright (C) 2008-2012 M. Litherland
 */

package org.nule.integrateclient.extra;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.Date;
import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import org.nule.lighthl7lib.util.LineReader;
import org.nule.nuleditor.gui.*;

/**
 *
 * @author mike
 */
public class BshLogicForm extends JPanel implements AppListener {
    
    private SimpleEditor editor = new SimpleEditor();
    private JLabel position = new JLabel("Pos 0:0");
    private JTextArea error;
    private JButton evalContinue, evalIgnore, evalResume;
    private JCheckBox runInitCheck;
    private boolean waitOnError = true;
    private static final Object ignoreSync = new Object();
    private Boolean ignoreFlag = false;
    private int errorCount;
    
    public BshLogicForm() {
        setLayout(new BorderLayout());
        JPanel top = new JPanel();
        top.setLayout(new BorderLayout());
        top.add(new JScrollPane(editor), BorderLayout.CENTER);
        top.add(position, BorderLayout.SOUTH);
        editor.addCaretListener(new CaretListener() {
            public void caretUpdate(CaretEvent arg0) {
                try {
                    // the line seperator is *always* \n when in the editor
                    int pos = editor.getCaretPosition();
                    String s = editor.getText(0, pos);
                    int row = 1;
                    int col = pos + 1;
                    int index = s.indexOf('\n');
                    while (index >= 0) {
                        row++;
                        col = pos - index;
                        index = s.indexOf('\n', index+1);
                        if (row > 100) {
                            break;
                        }
                    }
                    position.setText("Pos "+row+":"+col);
                } catch (BadLocationException e) {
                    // that's ok
                }
            }
        });
        JPanel bottom = new JPanel();
        bottom.setLayout(new BorderLayout());
        error = new JTextArea();
        error.setEditable(false);
        error.setLineWrap(true);
        error.setWrapStyleWord(true);
        error.setText("No recent errors.");
        bottom.add(new JScrollPane(error), BorderLayout.CENTER);
        JPanel control = new JPanel();
        control.setLayout(new FlowLayout(FlowLayout.LEFT));
        evalContinue = new JButton("Continue processing");
        evalContinue.setEnabled(false);
        evalContinue.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                evalContinueEvent();
            }
        });
        evalIgnore = new JButton("Continue ignoring errors");
        evalIgnore.setEnabled(false);
        evalIgnore.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                evalIgnoreEvent();
            }
        });
        evalResume = new JButton("Pause on errors");
        evalResume.setEnabled(false);
        evalResume.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                evalResumeEvent();
            }
        });
        runInitCheck = new JCheckBox("Run initialization code");
        runInitCheck.setSelected(true);
        control.add(evalContinue);
        control.add(evalIgnore);
        control.add(evalResume);
        control.add(runInitCheck);
        bottom.add(control, BorderLayout.SOUTH);
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.add(top);
        split.add(bottom);
        split.setDividerLocation(400);
        add(split);
    }
    
    public boolean runInit() {
        if (runInitCheck.isSelected()) {
            runInitCheck.setSelected(false);
            return true;
        }
        return false;
    }
    
    public String getScript() {
        return editor.getText();
    }
    
    public void evalContinueEvent() {
        evalContinue.setEnabled(false);
        evalIgnore.setEnabled(false);
        synchronized (ignoreSync) {
            ignoreFlag = true;
        }
    }
    
    public void evalIgnoreEvent() {
        evalContinue.setEnabled(false);
        evalIgnore.setEnabled(false);
        waitOnError = false;
        evalResume.setEnabled(true);
        synchronized (ignoreSync) {
            ignoreFlag = true;
        }
    }
    
    public void evalResumeEvent() {
        waitOnError = true;
        evalResume.setEnabled(false);
    }
    
    public void appEventPerformed(AppEvent ae) {
        if (ae.getEventCode() == AppEvent.LOAD_NEW_SNIPPIT_1) {
            new BshLoaderFrame(this);
        } else if (ae.getEventCode() == AppEvent.SAVE_SNIPPIT) {
            JFileChooser jfc = FileHandler.getFileHandler().getFileChooser("BSH");
            int result = jfc.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File s = jfc.getSelectedFile();
                if (s.exists()) {
                    result = JOptionPane.showConfirmDialog(this, "File exists, overwrite?",
                            "File exists", JOptionPane.YES_NO_OPTION);
                    if (result == JOptionPane.OK_OPTION) {
                        saveFile(s);
                    }
                } else {
                    saveFile(s);
                }
            }
        } else if (ae.getEventCode() == AppEvent.LOAD_SNIPPIT) {
            JFileChooser jfc = FileHandler.getFileHandler().getFileChooser("BSH");
            int result = jfc.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File o = jfc.getSelectedFile();
                openFile(o);
            }
        } else if (ae.getEventCode() == AppEvent.UNDO_ACTION) {
            editor.undo();
        } else if (ae.getEventCode() == AppEvent.REDO_ACTION) {
            editor.redo();
        } else if (ae.getEventCode() == AppEvent.BLOCK_COMMENT) {
            editor.blockComment();
        } else if (ae.getEventCode() == AppEvent.BLOCK_UNCOMMENT) {
            editor.blockUncomment();
        } else if (ae.getEventCode() == AppEvent.CLEAR_TABS_ACTION) {
            editor.clearTabs();
        }
    }
    
    public void setEditor(String text) {
        editor.setText(text);
    }
    
    public void showError(java.util.List l) {
        errorCount++;
        StringBuilder errorText = new StringBuilder();
        errorText.append("Error #").append(errorCount).append(" at ").append(new Date()).append("\n");
        for (int i = 0; i < l.size(); i++) {
            errorText.append(l.get(i).toString()).append("\n");
        }
        error.setText(errorText.toString());
        if (waitOnError) {
            evalContinue.setEnabled(true);
            evalIgnore.setEnabled(true);
            while (true) {
                try {
                    Thread.sleep(1000);
                    synchronized (ignoreSync) {
                        if (ignoreFlag.booleanValue()) {
                            ignoreFlag = false;
                            break;
                        }
                    }
                } catch (InterruptedException e) {

                }
            }
        }
    }
    
    private void saveFile(File f) {
        FileWriter fw = null;
        try {
            fw = new FileWriter(f);
            fw.write(editor.getText());
        } catch (IOException e){
            JOptionPane.showMessageDialog(this, "Error saving file: "+e);
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e) {}
            }
        }
    }
    
    private void openFile(File f) {
        try {
            LineReader br = new LineReader(new FileReader(f));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                line = br.readLine();
            }
            editor.setText(sb.toString());
        } catch (IOException e){
            JOptionPane.showMessageDialog(this, "Error loading file: "+e);
        }
    }
}
