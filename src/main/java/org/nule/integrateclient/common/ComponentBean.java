/*
 * ComponentBean.java
 *
 * Created on April 3, 2006, 1:21 PM
 *
 * Copyright (C) 2006-8 M Litherland
 */

package org.nule.integrateclient.common;

import java.lang.reflect.*;
import java.util.*;

/**
 *
 * @author litherm
 *
 * This bean abstracts the data for the various components that can be loaded
 * by the comm program in its advanced mode.  This includes, business logic, 
 * inbound and outboud clients.  It also keeps track of whether the component
 * has been configured as well as its general type and class information.
 */
public class ComponentBean {
    
    public static final String INBOUND = "Inbound";
    public static final String OUTBOUND = "Outbound";
    public static final String LOGIC = "Logic";
    
    private boolean configured = false;
    
    private Class component;
    
    private String name, className, type;
    
    private Properties properties = null;
    
    /** Creates a new instance of ComponentBean */
    public ComponentBean(Class component, String name) {
        this.component = component;
        this.name = name;
        String[] packages = component.toString().split("\\.");
        className = packages[packages.length - 1];
        if (extendsClass(component, IntegrateClient.class) &&
                hasInterface(component, InboundClient.class)) {
            type = INBOUND;
        } else if (extendsClass(component, IntegrateClient.class) &&
                hasInterface(component, OutboundClient.class)) {
            type = OUTBOUND;
        } else if (extendsClass(component, LogicAgent.class)) {
            type = LOGIC;
        } else {
            throw new IllegalArgumentException("Class provided is not of an " +
                    "appropriate type.");
        }
        properties = new Properties();
        Properties p = getTypes();
        if (p != null && p.size() > 0) {
            configured = false;
        } else {
            configured = true;
            return;
        }
        Iterator keys = p.keySet().iterator();
        while (keys.hasNext()) {
            properties.put(keys.next(), "");
        }
    }
    
    public Class getActualClass() {
        return component;
    }
    
    /**
     * Return a string containing the client type.
     */
    public String getType() {
        return type;
    }
    
    /**
     * Return a string of our specified name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Allow our name to be set or updated.
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Currently we just make sure that any 'mandatory' property exists in
     * the properties has.  Future versions may want to test that the field
     * type matches that specified by getTypes().
     */
    public boolean testProperties() {
        Properties mandatory = getMandatory();
        if (mandatory == null) {
            return true;
        }
        Iterator it = mandatory.keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            if (!properties.containsKey(key)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * This class is provided for the configuration parser as it can only add
     * properties one line at a time.  At the end it should perform a test
     * on the properties just as it should when performing a setProperties.
     */
    public void addProperties(String key, String value) {
        configured = true;
        if (properties == null) {
            properties = new Properties();
        }
        properties.setProperty(key, value);
    }
    
    /**
     * Set all the properties at once - this is more for the convenience of
     * the GUI builders.
     */
    public void setProperties(Properties p) {
        configured = true;
        properties = p;
    }
    
    public Properties getProperties() {
        return properties;
    }
    
    public String toString() {
        String retVal;
        if (configured)
            retVal = className;
        else
            retVal = className+" [select to configure]";
        return retVal;
    }
    
    public String getClassName() {
        return className;
    }
    
    public String getConfigName() {
        return "["+name+"|"+component.getCanonicalName()+"]";
    }
    
    public boolean isConfigured() {
        return configured;
    }
    
    public String getDescription() {
        try {
            if (extendsClass(component, IntegrateClient.class) ||
                    extendsClass(component, LogicAgent.class)) {
                Field f = component.getDeclaredField("DESCRIPTION");
                return f.get(null).toString();
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return "Description could not be gathered.";
    }
    
    public Properties getMandatory() {
        return getProperties("getMandatory");
    }
    
    public Properties getOptional() {
        return getProperties("getOptional");
    }
    
    public Properties getDefaults() {
        return getProperties("getDefaults");
    }
    
    public Properties getTypes() {
        return getProperties("getTypes");
    }
    
    public Properties getDescriptions() {
        return getProperties("getDescriptions");
    }
    
    private Properties getProperties(String type) {
        try {
            Method m;
            if (extendsClass(component, IntegrateClient.class)) {
                m = getMethod(component, type);
            } else if (extendsClass(component, LogicAgent.class)) {
                m = getMethod(component, type);
            } else {
                return null;
            }
            return (Properties) m.invoke(null, (Object[]) null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private Method getMethod(Class c, String method) {
        try{
            Method m = c.getDeclaredMethod(method, (Class[]) null);
            return m;
        } catch (NoSuchMethodException e) {
            
        }
        Class superclass = c.getSuperclass();
        if (superclass == null) {
            return null;
        }
        return getMethod(superclass, method);
    }
    
    private boolean extendsClass(Class c, Class ce) {
        Class superclass = c.getSuperclass();
        if (ce.equals(superclass)) {
            return true;
        }
        if (superclass == null) {
            return false;
        } 
        return extendsClass(superclass, ce);
    }
    
    private boolean hasInterface(Class c, Class ci) {
        Class[] interfaces = c.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            if (ci.equals(interfaces[i])) {
                return true;
            }
        }
        Class superclass = c.getSuperclass();
        if (superclass == null) {
            return false;
        }
        return hasInterface(superclass, ci);
    }
}
