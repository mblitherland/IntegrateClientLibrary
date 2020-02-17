/*
 * CronTrigger.java
 * 
 * Copyright 2008-2012 M. Litherland
 */

package org.nule.integrateclient.util;

import java.text.SimpleDateFormat;
import java.util.*;
import org.nule.integrateclient.common.*;

/**
 *
 * @author mike
 */
public class CronTrigger extends InboundTrigger {

    public static final String DESCRIPTION = "CronTrigger allows you to schedule " +
            "jobs according to a cron-like format.  Fields may contain numbers, " +
            "'*', ',', '-' and '/' according to the conventions of cron.";
    
    private enum CronConst {
        Minute,
        Hour,
        DayOfMonth,
        Month,
        DayOfWeek
    }
    
    private static final long MINUTE = 60000;

    private String minute;
    private String hour;
    private String dayOfMonth;
    private String month;
    private String dayOfWeek;
    private CronField minField;
    private CronField hourField;
    private CronField domField;
    private CronField monField;
    private CronField dowField;

    private SimpleDateFormat logFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
    private SimpleDateFormat parse = new SimpleDateFormat("m,H,d,M,E");

    public CronTrigger(String name, Logger logger, ProcessorAgent pa) {
        super(name, logger, pa);
        this.name = name;
        this.logger = logger;
        this.pa = pa;
    }

    public CronTrigger(String name, Logger logger, ProcessorAgent pa, Properties p) {
        super(name, logger, pa, p);
        this.name = name;
        this.logger = logger;
        this.pa = pa;
        loadProperties(p);
    }

    public static Properties getMandatory() {
        Properties p = new Properties();
        p.put(CronConst.Minute, "");
        p.put(CronConst.Hour, "");
        p.put(CronConst.DayOfMonth, "");
        p.put(CronConst.Month, "");
        p.put(CronConst.DayOfWeek, "");
        return p;
    }

    public static Properties getOptional() {
        return null;
    }

    public static Properties getDefaults() {
        Properties p = new Properties();
        p.put(CronConst.Minute, "*");
        p.put(CronConst.Hour, "*");
        p.put(CronConst.DayOfMonth, "*");
        p.put(CronConst.Month, "*");
        p.put(CronConst.DayOfWeek, "*");
        return p;
    }

    public static Properties getTypes() {
        Properties p = new Properties();
        p.put(CronConst.Minute, "");
        p.put(CronConst.Hour, "");
        p.put(CronConst.DayOfMonth, "");
        p.put(CronConst.Month, "");
        p.put(CronConst.DayOfWeek, "");
        return p;
    }

    public static Properties getDescriptions() {
        Properties p = new Properties();
        p.put(CronConst.Minute, "Minute of hour, 0-59");
        p.put(CronConst.Hour, "Hour of day, 0-23");
        p.put(CronConst.DayOfMonth, "Day of month, 1-31");
        p.put(CronConst.Month, "Month of year, 1-12");
        p.put(CronConst.DayOfWeek, "Day of week, 0-6");
        return p;
    }

    @Override
    public Properties getStatus() {
        logger.info(name + ": Handling request for status.");
        Properties p = new Properties();
        p.put(CronConst.Minute, "");
        p.put(CronConst.Hour, "");
        p.put(CronConst.DayOfMonth, "");
        p.put(CronConst.Month, "");
        p.put(CronConst.DayOfWeek, "");
        return p;
    }


    @Override
    public void loadProperties(Properties p) throws IllegalArgumentException {
        minute = p.getProperty(CronConst.Minute.name());
        minField = new CronField(minute);
        if (minute == null) {
            logger.error(name + " minute must be provided.");
            throw new IllegalArgumentException("Invalid argument");
        }
        hour = p.getProperty(CronConst.Hour.name());
        hourField = new CronField(hour);
        if (hour == null) {
            logger.error(name + " hour must be provided.");
            throw new IllegalArgumentException("Invalid argument");
        }
        dayOfMonth = p.getProperty(CronConst.DayOfMonth.name());
        domField = new CronField(dayOfMonth);
        if (dayOfMonth == null) {
            logger.error(name + " dayOfMonth must be provided.");
            throw new IllegalArgumentException("Invalid argument");
        }
        month = p.getProperty(CronConst.Month.name());
        monField = new CronField(month);
        if (month == null) {
            logger.error(name + " month must be provided.");
            throw new IllegalArgumentException("Invalid argument");
        }
        dayOfWeek = p.getProperty(CronConst.DayOfWeek.name());
        dowField = new CronField(dayOfWeek);
        if (dayOfWeek == null) {
            logger.error(name + " dayOfWeek must be provided.");
            throw new IllegalArgumentException("Invalid argument");
        }
        status = "CronTrigger configured.";
    }

    @Override
    public void run() {
        running = true;
        status = "CronTrigger running.";
        try {
            logger.info(name + " CronTrigger starting with schedule '"+
                    scheduleString()+"'");
            while (running) {
                Date now = new Date();
                logger.trace("CronTrigger running now "+logFormat.format(now));
                String[] current = parse.format(now).split(",");
                //"m,H,d,M,F"
                if (minField.matchAny(current[0])) {
                    logger.trace("Comparing min "+current[0]+" to "+
                            minField.toString()+" matched");
                    if (hourField.matchAny(current[1])) {
                        logger.trace("Comparing hour "+current[1]+" to "+
                                hourField.toString()+" matched");
                        if (domField.matchAny(current[2])) {
                            logger.trace("Comparing day of month "+current[2]+" to "+
                                    domField.toString()+" matched");
                            if (monField.matchAny(current[3])) {
                                logger.trace("Comparing month "+current[3]+" to "+
                                        monField.toString()+" matched");
                                if (dowField.matchAny(dowField.dayOfWeekToInt(current[4]))) {
                                    logger.trace("Comparing day of week "+current[4]+" to "+
                                            dowField.toString()+" matched");
                                    logger.info(name+" Cron matched schedule '"+
                                            scheduleString()+"', sending message");
                                    count++;
                                    pa.dataTransfer("CronTrigger "+logFormat.format(now));
                                } else {
                                    logger.trace("Comparing day of week "+current[4]+" to "+
                                            dowField.toString()+" failed");
                                }
                            } else {
                                logger.trace("Comparing month "+current[3]+" to "+
                                        monField.toString()+" failed");
                            }
                        } else {
                            logger.trace("Comparing day of month "+current[2]+" to "+
                                    domField.toString()+" failed");
                        }
                    } else {
                        logger.trace("Comparing hour "+current[1]+" to "+
                                hourField.toString()+" failed");
                    }
                } else {
                    logger.trace("Comparing min "+current[0]+" to "+
                            minField.toString()+" failed");
                }
                Thread.sleep(MINUTE - System.currentTimeMillis() % MINUTE);
            }
        } catch (InterruptedException e) {
            status = "Interrupted exception.";
            logger.error(name + ": InterruptedException - "+e);
        } catch (Exception e) {
            status = "Some other exception in run loop.";
            logger.error(name + ": Some other exception - "+e);
        } finally {
            running = false;
        }
    }

    private String scheduleString() {
        return minute+" "+hour+" "+dayOfMonth+" "+month+" "+dayOfWeek;
    }

    private static class CronField {

        private String in;
        private boolean all = false;
        private boolean range = false;
        private int r1, r2;
        private boolean period = false;
        private int p1;
        private boolean many = false;
        private int[] m1;
        private boolean numeric = false;

        public CronField(String in) {
            this.in = in;
            int count = 0;
            if (in.equals("*")) {
                all = true;
                count++;
            }
            if (in.contains("-")) {
                String[] s = in.split("-");
                if (s.length == 2) {
                    try {
                        r1 = Integer.parseInt(s[0]);
                        r2 = Integer.parseInt(s[1]);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Cron range without numeric values: "+in);
                    }
                } else {
                    throw new IllegalArgumentException("Cron range without two values: "+in);
                }
                range = true;
                count++;
            }
            if (in.contains("/")) {
                String[] s = in.split("/");
                if (s.length == 2) {
                    try {
                        p1 = Integer.parseInt(s[1]);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Cron period without numeric value: "+in);
                    }
                } else {
                    throw new IllegalArgumentException("Cron period without two values: "+in);
                }
                period = true;
                count++;
            }
            if (in.contains(",")) {
                String[] s = in.split(",");
                m1 = new int[s.length];
                for (int i = 0; i < s.length; i++) {
                    try {
                        m1[i] = Integer.parseInt(s[i]);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Cron many without numeric value: "+in);
                    }
                }
                many = true;
                count++;
            }
            numeric = !(all || range || period || many || numeric);
            if (numeric) {
                try {
                    p1 = Integer.parseInt(in);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Cron numeric without numeric value: "+in);
                }
                count++;
            }
            if (count != 1) {
                throw new IllegalArgumentException("Cron specifier not unique: "+in);
            }
        }

        private int getCronTypeIndex() {
            if (all) {
                return 0;
            }
            if (range) {
                return 1;
            }
            if (period) {
                return 2;
            }
            if (many) {
                return 3;
            }
            return 4;
        }

        public boolean matchAny(String comp) {
            switch (getCronTypeIndex()) {
                case 0:
                    return true;
                case 1:
                    return matchRange(comp);
                case 2:
                    return matchPeriod(comp);
                case 3:
                    return matchMany(comp);
                case 4:
                    return matchNumeric(comp);
            }
            return false;
        }

        private boolean matchRange(String comp) {
            try {
                int i = Integer.parseInt(comp);
                if (i >= r1 && i <= r2) {
                    return true;
                }
            } catch (NumberFormatException e) {}
            return false;
        }

        private boolean matchPeriod(String comp) {
            try {
                int i = Integer.parseInt(comp);
                if (i % p1 == 0) {
                    return true;
                }
            } catch (NumberFormatException e) {}
            return false;
        }

        private boolean matchMany(String comp) {
            try {
                int i = Integer.parseInt(comp);
                for (int j = 0; j < m1.length; j++) {
                    if (i == m1[j]) {
                        return true;
                    }
                }
            } catch (NumberFormatException e) {}
            return false;
        }

        private boolean matchNumeric(String comp) {
            return in.equals(comp);
        }

        @Override
        public String toString() {
            return in;
        }

        public String dayOfWeekToInt(String dow) {
            if (dow.equalsIgnoreCase("SUN")) {
                return "0";
            }
            if (dow.equalsIgnoreCase("MON")) {
                return "1";
            }
            if (dow.equalsIgnoreCase("TUE")) {
                return "2";
            }
            if (dow.equalsIgnoreCase("WED")) {
                return "3";
            }
            if (dow.equalsIgnoreCase("THU")) {
                return "4";
            }
            if (dow.equalsIgnoreCase("FRI")) {
                return "5";
            }
            if (dow.equalsIgnoreCase("SAT")) {
                return "6";
            }
            return "-1";
        }
    }
}
