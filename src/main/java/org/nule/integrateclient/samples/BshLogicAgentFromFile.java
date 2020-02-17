/*
 * Copyright (C) 2008-2012 M. Litherland
 */

package org.nule.integrateclient.samples;

import java.io.*;
import java.util.*;

import org.nule.integrateclient.common.*;

/**
 *
 * @author mike
 */
public class BshLogicAgentFromFile extends BshLogicAgent {

    public static final String DESCRIPTION = "This agent utilizes beanshell " +
            "to run a script you create against the messages as they are " +
            "processed.  This is the non-interactive form of the beanshell " +
            "agent, so you must specify the beanshell script file for it to " +
            "operate on.  Errors will be displayed in the log.  To create a new " +
            "beanshell script for HL7 Comm use the BshLogicAgentInteractive " +
            "client, and save it from there for this agent to run.";
    
    private String script = null;
    private boolean init = true;
    
    public BshLogicAgentFromFile(String name, Logger logger) {
        super(name, logger);
    }
    
    protected String getEval() {
        return script;
    }

    protected void showErrors(List e) {
        for (int i = 0; i < e.size(); i++) {
            logger.error(name+" (bsh): "+e.get(i));
        }
    }

    public static Properties getDefaults() {
        Properties p = new Properties();
        p.setProperty("scriptFile", "scriptfile.bsh");
        return p;
    }

    public static Properties getMandatory() {
        Properties p = new Properties();
        p.setProperty("scriptFile", "");
        return p;
    }

    public void loadProperties(Properties p) throws IllegalArgumentException {
        BufferedReader br = null;
        try {
            String scriptFile = p.getProperty("scriptFile");
            File f = new File(scriptFile);
            if (!f.exists()) {
                throw new FileNotFoundException("Unable to find file, "+scriptFile);
            }
            br = new BufferedReader(new FileReader(f));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line+"\n");
                line = br.readLine();
            }
            script = sb.toString();
            System.out.println("script: "+script);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to load script file", e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {}
            }
        }
    }

    public static Properties getTypes() {
        Properties p = new Properties();
        p.setProperty("scriptFile", FieldTypes.FILE);
        return p;
    }
    
    public static Properties getDescriptions() {
        Properties p = new Properties();
        p.setProperty("scriptFile", "This is the file to read for the beanshell script");
        return p;
    }

    /**
     * Only run the initialization code the first time we load.
     * @return
     */
    protected boolean runInit() {
        if (init) {
            init = false;
            return true;
        }
        return false;
    }

}
