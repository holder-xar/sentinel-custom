package com.alibaba.csp.sentinel.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * @author holder
 * @date 2019/7/4
 * @Description:
 */
public class ConfigUtil {
    public static final String CLASSPATH_FILE_FLAG = "classpath:";

    /**
     * Return null if the file not exist
     *
     * @param fileName
     * @return
     */
    public static Properties loadProperties(String fileName) {
        if (StringUtil.isNotBlank(fileName)) {
            if (fileName.startsWith(File.separator)) {
                return loadPropertiesFromAbsoluteFile(fileName);
            } else if (fileName.startsWith(CLASSPATH_FILE_FLAG)) {
                return loadPropertiesFromClasspathFile(fileName);
            } else {
                return loadPropertiesFromRelativeFile(fileName);
            }
        }
        return null;

    }

    private static Properties loadPropertiesFromAbsoluteFile(String fileName) {
        Properties properties = null;
        try {

            File file = new File(fileName);
            if (!file.exists()) {
                return null;
            }

            FileInputStream input = new FileInputStream(file);
            try {
                properties = new Properties();
                properties.load(input);
            } finally {
                input.close();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return properties;
    }


    private static Properties loadPropertiesFromClasspathFile(String fileName) {

        fileName = fileName.substring(CLASSPATH_FILE_FLAG.length()).trim();

        List<URL> list = new ArrayList<URL>();
        try {
            Enumeration<URL> urls = getClassLoader().getResources(fileName);
            list = new ArrayList<URL>();
            while (urls.hasMoreElements()) {
                list.add(urls.nextElement());
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        if (list.isEmpty()) {
            return null;
        }

        Properties properties = new Properties();
        for (URL url : list) {
            try {
                Properties p = new Properties();
                InputStream input = url.openStream();
                if (input != null) {
                    try {
                        p.load(input);
                        properties.putAll(p);
                    } finally {
                        try {
                            input.close();
                        } catch (Throwable t) {
                        }
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return properties;
    }

    private static Properties loadPropertiesFromRelativeFile(String fileName) {
        String userDir = System.getProperty("user.dir");
        String realFilePath = addSeparator(userDir) + fileName;
        return loadPropertiesFromAbsoluteFile(realFilePath);
    }

    private static ClassLoader getClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = ConfigUtil.class.getClassLoader();
        }
        return classLoader;
    }

    public static String addSeparator(String dir) {
        if (!dir.endsWith(File.separator)) {
            dir += File.separator;
        }
        return dir;
    }

    public static void main(String[] args) {
        String classPathfileName = "classpath:sentinel.properties";
        Properties p1 = loadPropertiesFromClasspathFile(classPathfileName);
        System.out.println(p1.getProperty("project.name"));

        String absolutePath = "/Users/holder/Downloads/Sentinel-release-1.6/sentinel-core/target/classes/sentinel.properties";
        Properties p2 = loadPropertiesFromAbsoluteFile(absolutePath);
        System.out.println(p2.getProperty("project.name"));

        Properties p3 = loadPropertiesFromRelativeFile("sentinel-core/src/main/resources/sentinel.properties");
        System.out.println(p3.getProperty("project.name"));


    }

}
