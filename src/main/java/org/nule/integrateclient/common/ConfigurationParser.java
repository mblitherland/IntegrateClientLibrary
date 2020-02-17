/*
 * ConfigurationParser.java
 *
 * Created on April 6, 2006, 12:15 PM
 *
 * Copyright (C) 2004-2012 M Litherland
 */

package org.nule.integrateclient.common;

import java.io.*;
import java.net.MalformedURLException;
import java.util.*;
import java.util.regex.*;

/**
 *
 * @author litherm
 *
 * This class is designed to parse "standard" configuration files used by
 * the Integrate system.  It's a delimited format with multiple tiers, 
 * much like a standard properties file or old dos configuration files.
 * XML was considered for the configuration files, but this approach
 * is marginally simpler, in my opinion, from the standpoints of hand-
 * editing, programmatic editing, and programmatic parsing.
 *
 * This is a quick sample of the format used:
 *
 * # Comment
 * %ManagerAgentName%
 * managerAgentProperty1=value
 * ...
 * {ProcessorAgentName}
 * processorAgentProperty1=value
 * [ComponentName|package.ComponentClass]
 * componentProperty1=value
 * componentProperty2=value
 * ...
 * [ComponentName2|package.ComponentClass]
 * componentProperty1=value
 * ...
 * {ProcessorAgentName2}
 *
 * So it's a heirarchical format with ManagerAgents being tier one, with 
 * one or more MAs listed.  Each MA may have one or more ProcessorAgents,
 * and each PA may have two or more components (at least logic and one
 * inbound).
 *
 * MAs and PAs may have properties that are specific to their functioning.
 * For example each MA and PA likely requires a logger.
 *
 */
public class ConfigurationParser {

    public static final String MAHEADER = "HL7 Comm";
    public static final String PAHEADER = "Simple Controller";
    
    private BufferedReader br;
    
    private ConfigurationMap<String, ConfigurationMap<String, ConfigurationMap>> managementAgents = 
            new ConfigurationMap<String, ConfigurationMap<String, ConfigurationMap>>();
    
    private Logger logger;
    
    private String lastError;
    
    /** Creates a new instance of ConfigurationParser */
    public ConfigurationParser(Logger logger, InputStream is) {
        this.logger = logger;
        br = new BufferedReader(new InputStreamReader(is));
    }
    
    public Set<String> getMaSet() {
        if (managementAgents == null) {
            lastError = "Management Agents haven't been set.";
            return null;
        } else if (managementAgents.isEmpty()) {
            lastError = "No Management Agents found.";
            return null;
        }
        return managementAgents.keySet();
    }
    
    public Set<String> getPaSet(String MA) {
        if (managementAgents == null) {
            lastError = "Management Agents haven't been set.";
            return null;
        } else if (managementAgents.containsKey(MA)) {
            ConfigurationMap<String, ConfigurationMap> hashPA = managementAgents.get(MA);
            if (hashPA == null) {
                lastError = "Processor Agent not set for "+MA;
                return null;
            } else {
                return hashPA.keySet();
            }
        } else {
            lastError = "Could not find matching management agent.";
            return null;
        }
    }
    
    public ConfigurationMap getCompMap(String MA, String PA) {
        if (managementAgents == null) {
            lastError = "Management Agents haven't been set.";
            return null;
        } else if (managementAgents.containsKey(MA)) {
            ConfigurationMap<String, ConfigurationMap> hashPA = managementAgents.get(MA);
            if (hashPA == null) {
                lastError = "Processor Agent not set for "+MA;
                return null;
            } else {
                if (hashPA.containsKey(PA)) {
                    ConfigurationMap<String, ConfigurationMap> hashComps = hashPA.get(PA);
                    if (hashComps == null) {
                        lastError = "Components not set for "+PA+" in "+MA;
                        return null;
                    } else {
                        return hashComps;
                    }
                } else {
                    lastError = "Components don't exist for "+PA+" in "+MA;
                    return null;
                }
            }
        } else {
            lastError = "Could not find matching management agent.";
            return null;
        }
    }
    
    public boolean genConfig() {
        boolean retVal = true;
        // Pattern for the management agent header
        Pattern ma = Pattern.compile("%([^)]+)%");
        // Pattern for the processor agent header
        Pattern pa = Pattern.compile("\\{([^\\}]+)\\}");
        // Pattern for a component header including class
        Pattern comp = Pattern.compile("\\[([^\\|]+)\\|([^\\]]+)\\]");
        String currentMA = "";
        String currentPA = "";
        String currentCompName;
        String currentCompClass;
        String compKey = "";
        Matcher m;
        while (true) {
            try {
                String s = br.readLine();
                logger.trace("Loading config line: " + s);
                if (s == null) {
                    break;
                }
                if (s.startsWith("#")) {
                    continue;
                }
                m = ma.matcher(s);
                if (m.find()) {
                    currentMA = m.group(1);
                    currentPA = "";
                    compKey = "";
                    if (managementAgents.containsKey(currentMA)) {
                        lastError = "Duplicate management agent: "+currentMA;
                        retVal = false;
                    } else {
                        managementAgents.put(currentMA, new ConfigurationMap<String, ConfigurationMap>());
                    }
                } else {
                    m = pa.matcher(s);
                    if (m.find()) {
                        currentPA = m.group(1);
                        compKey = "";
                        ConfigurationMap<String, ConfigurationMap> hashMA = managementAgents.get(currentMA);
                        if (hashMA.containsKey(currentPA)) {
                            lastError = "Duplicate processor agent: "+currentPA+
                                    " in "+currentMA;
                            retVal = false;
                        } else {
                            hashMA.put(currentPA, new ConfigurationMap<String, ConfigurationMap>());
                        }
                    } else {
                        m = comp.matcher(s);
                        if (m.find()) {
                            currentCompName = m.group(1);
                            currentCompClass = m.group(2);
                            compKey = currentCompName+"|"+currentCompClass;
                            ConfigurationMap<String, ConfigurationMap> hashMA = managementAgents.get(currentMA);
                            ConfigurationMap<String, ComponentBean> hashPA = hashMA.get(currentPA);
                            if (hashPA.containsKey(compKey)) {
                                lastError = "Duplicate component: "+compKey+
                                        " in "+currentPA+" in "+currentMA;
                                retVal = false;
                            } else {
                                try {
                                    Class compClass = IclClassLoader.getInstance().loadClass(currentCompClass);
                                    ComponentBean cb = new ComponentBean(compClass, currentCompName);
                                    hashPA.put(compKey, cb);
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                    lastError = "Class Not Found: "+currentCompClass;
                                    retVal = false;
                                }
                            }
                        } else {
                            String[] pair = s.split("\\s*=\\s*", 2);
                            if (pair.length == 2) {
                                if ("".equals(currentMA)) {
                                    lastError = "Got data before a management agent was set.";
                                    retVal = false;
                                } else if ("".equals(compKey) && "".equals(currentPA)) {
                                    ConfigurationMap<String, ConfigurationMap> hashMA = (ConfigurationMap) managementAgents.get(currentMA);
                                    hashMA.addConfig(pair[0], pair[1]);
                                } else if ("".equals(compKey)) {
                                    ConfigurationMap<String, ConfigurationMap> hashMA = (ConfigurationMap) managementAgents.get(currentMA);
                                    ConfigurationMap<String, ComponentBean> hashPA = (ConfigurationMap) hashMA.get(currentPA);
                                    hashPA.addConfig(pair[0], pair[1]);
                                    if ("jarUrls".equals(pair[0])) {
                                        String[] urls = pair[1].split(",");
                                        for (int i = 0; i < urls.length; i++) {
                                            try {
                                                IclClassLoader.getInstance().addUrl(new File(urls[i]).toURI().toURL());
                                                logger.info("Added jar URL to loader "+urls[i]);
                                            } catch (MalformedURLException e) {
                                                e.printStackTrace();
                                                logger.warn("Malformed URL loading jar "+urls[i]);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                                logger.warn("IO Exception in URL for loading jar "+urls[i]);
                                            }
                                        }
                                    }
                                } else {
                                    ConfigurationMap<String, ConfigurationMap> hashMA = managementAgents.get(currentMA);
                                    ConfigurationMap<String, ComponentBean> hashPA = hashMA.get(currentPA);
                                    ComponentBean cb = hashPA.get(compKey);
                                    cb.addProperties(pair[0], pair[1]);
                                }
                            } else {
                                lastError = "Invalid configuration line: "+s;
                                retVal = false;
                            }
                        }
                    }
                }
            } catch (IOException e) {
                lastError = "IOException reading in file: "+e.getLocalizedMessage();
                retVal = false;
            }
        }
        // No point in confirming the configuration if it hasn't passed thus far.
        if (retVal) {
            retVal = confirmConfig();
        }
        return retVal;
    }
    
    public String getLastError() {
        return lastError;
    }
    
    private boolean confirmConfig() {
        boolean retVal = true;
        for (Map.Entry<String, ConfigurationMap<String, ConfigurationMap>> entryMA : managementAgents.entrySet()) {
            String currentMA = entryMA.getKey();
            ConfigurationMap<String, ConfigurationMap> hashMA = entryMA.getValue();
            for (Map.Entry<String, ConfigurationMap> entryPA : hashMA.entrySet()) {
                String currentPA = entryPA.getKey();
                ConfigurationMap<String, ComponentBean> hashPA = entryPA.getValue();
                for (Map.Entry<String, ComponentBean> entryComp : hashPA.entrySet()) {
                    String compKey = entryComp.getKey();
                    ComponentBean cb = entryComp.getValue();
                    if (!cb.testProperties()) {
                        lastError = "Component properties failure: "+compKey+
                                " in "+currentPA+" in "+currentMA;
                        retVal = false;
                    }
                }
            }
        }
        return retVal;
    }
}
