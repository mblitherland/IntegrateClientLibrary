/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.nule.integrateclient.samples;

import bsh.EvalError;
import bsh.Interpreter;
import bsh.ParseException;
import bsh.TargetError;

import java.util.*;

import org.nule.integrateclient.common.*;
import org.nule.lighthl7lib.hl7.*;

/**
 *
 * @author mike
 */
abstract public class BshLogicAgent extends LogicAgent {

    private Interpreter bsh = new Interpreter();
    
    protected String message;
    
    protected String name;
    protected Logger logger;
    
    public BshLogicAgent(String name, Logger logger) {
        super(name, logger);
        this.name = name;
        this.logger = logger;
    }
    
    abstract protected String getEval();
    
    abstract protected void showErrors(List e);
    
    abstract protected boolean runInit();

    public boolean loadMessage(String in) {
        List l = new ArrayList();
        String eval = getEval();
        if (runInit()) {
            logger.info(name+" (bsh): running init.");
            if (!processInit(l, eval)) {
                if (l.size() > 0) {
                    showErrors(l);
                }
                return false;
            }
        }
        try {
            bsh.set("ack", true);
            bsh.set("in_raw", in);
            try {
                bsh.set("in_hl7", new Hl7Record(Hl7Record.cleanString(in)));
                bsh.set("is_hl7", true);
            } catch (Exception e) {
                bsh.set("in_hl7", null);
                bsh.set("is_hl7", false);
            }
            bsh.set("out", null);
            bsh.set("log", null);
            bsh.eval(eval);
            Object out = bsh.get("out");
            if (out == null) {
                logger.info(name+" (bsh): null return value forcing NACK.");
                return false;
            }
            message = out.toString();
            Object o = bsh.get("ack");
            Object log = bsh.get("log");
            if (log != null && !log.toString().equals("")) {
                logger.info(name+" (bsh): "+log);
            }
            if (o instanceof Boolean) {
                return ((Boolean) o).booleanValue();
            }
        } catch (TargetError e) {
            l.add("TargetError: "+e);
        } catch (ParseException e) {
            l.add("ParseException: "+e);
        } catch (EvalError e) {
            int line = e.getErrorLineNumber();
            if (line >= 0) {
                l.add("EvalError at line "+line+":");
                String[] lines = eval.split("\n");
                l.add(lines[line - 1]);
            } else {
                l.add("EvalError at unspecified line.");
            }
            l.add("Error text: "+e.getErrorText());
            l.add("Full error message:");
            l.add(e.getMessage());
        }
        // Anything in l means we had an exception
        if (l.size() > 0) {
            showErrors(l);
        }
        return false;
    }
    
    private boolean processInit(List l, String eval) {
        StringBuilder sb = new StringBuilder();
        String[] lines = eval.split("\n");
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].startsWith("// INIT: ")) {
                sb.append(lines[i].substring(9) + "\n");
            }
        }
        try {
            bsh.eval(sb.toString());
            return true;
        } catch (TargetError e) {
            l.add("TargetError: "+e);
        } catch (ParseException e) {
            l.add("ParseException: "+e);
        } catch (EvalError e) {
            int line = e.getErrorLineNumber();
            l.add("EvalError at line "+line+":");
            lines = eval.split("\n");
            l.add(lines[line - 1]);
            l.add("Error text: "+e.getErrorText());
            l.add("Full error message:");
            l.add(e.getMessage());
        }
        return false;
    }

    public String getProcessed() {
        return message;
    }

    public static Properties getDefaults() {
        return null;
    }

    public static Properties getMandatory() {
        return null;
    }

    public static Properties getOptional() {
        return null;
    }

    public void loadProperties(Properties p) throws IllegalArgumentException {
        
    }

    public static Properties getTypes() {
        return null;
    }
    
    public static Properties getDescriptions() {
        return null;
    }
}
