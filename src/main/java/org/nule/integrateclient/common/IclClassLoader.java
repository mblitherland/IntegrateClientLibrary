/*
 * IclClassLoader
 *
 * Copyright (C) M. Litherland 2009-2012
 */

package org.nule.integrateclient.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * IclClassLoader is a utility class designed to assist with adding URLs
 * to the classpath (specifically jars) without resorting to "black magic".
 * As long as the container this is running in extends URLClassLoader (and
 * virtually all do), the addUrl method will work fine.  Even if the container
 * does something unconventional with the classloader, it shouldn't break
 * ICL if you don't call the addUrl method, or if you surround the call with
 * trycatch
 *
 * @author mike
 */
public class IclClassLoader {

    private volatile static IclClassLoader me = null;

    private URLClassLoader ucl = null;

    private List urlList = new ArrayList();

    private IclClassLoader() {

    }

    /**
     * Normal singleton pattern to make sure with a VM we're not dealing
     * with more than one classloader.
     *
     * @return an instance of me.
     */
    public static IclClassLoader getInstance() {
        if (me == null) {
            me = new IclClassLoader();
        }
        return me;
    }

    /**
     * Add a URL to our internal classloader.  This should always get the
     * classloader that loaded ICL and LHL, so, in theory, if you've extended
     * the classes (or implemented interfaces) within these projects, it should
     * work without messing about with the classloader or classpath in other
     * ways.  I hope.
     *
     * If you're attempting to run ICL inside of a container where the
     * classloader does not extend URLClassLoader, this is probably going
     * to really wreck things for ICL.
     *
     * @param url - the URL of the class you wish to load.
     */
    public void addUrl(URL url) {
        if (urlList.contains(url)) {
            return;
        }
        urlList.add(url);
        if (ucl == null) {
            URL[] urls = new URL[1];
            urls[0] = url;
            ucl = new URLClassLoader(urls, getClass().getClassLoader());
        } else {
            try {
                Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", new
                Class[] {URL.class});
                addURL.setAccessible(true);
                addURL.invoke(ucl, new Object[] { url });
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Load a class by fully qualified class name.  Uses Class.forName
     * if no additional URLs have been added to this classloader.
     *
     * @param name - the fully qualified class name
     * @return the class.
     * @throws ClassNotFoundException
     */
    public Class loadClass(String name) throws ClassNotFoundException {
        if (ucl == null) {
            return Class.forName(name);
        }
        return ucl.loadClass(name);
    }

    /**
     * Return a list of all the URLs that have been added to us.
     *
     * @return urlList
     */
    public List getUrlList() {
        return urlList;
    }

}
