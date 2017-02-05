package de.metacode.grip.core.classloaderhack;

import java.io.IOException;
import java.io.File;
import java.net.URLClassLoader;
import java.net.URL;
import java.lang.reflect.Method;

//dirty hack inspired http://www.javafaq.nu/java-example-code-895.html

class ClassPathHacker {
    private static final Class[] parameters = new Class[]{URL.class};

    public static void addFile(String s) throws IOException {
        addURL(new File(s).toURI().toURL());
    }

    public static void addURL(URL u) throws IOException {
        final URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        final Class sysclass = URLClassLoader.class;
        try {
            Method method = sysclass.getDeclaredMethod("addURL", parameters);
            method.setAccessible(true);
            method.invoke(sysloader, u);
        } catch (Throwable t) {
            t.printStackTrace();
            throw new IOException("Error, could not add URL to system classloader");
        }
    }
}