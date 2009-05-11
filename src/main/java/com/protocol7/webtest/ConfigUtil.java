package com.protocol7.webtest;

import java.io.IOException;
import java.util.Properties;

public class ConfigUtil {

    private static Properties properties;
    
    public synchronized static String getString(String name) {
        if(properties == null) {
            properties = new Properties();
            try {
                properties.load(ConfigUtil.class.getClassLoader().getResourceAsStream("test.properties"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }            
        
        String value = properties.getProperty(name);
        
        if(value != null) {
            return value.trim();
        } else {
            return null;
        }
    }
    
    
    public static boolean getBoolean(String name) {
        String value = getString(name);
        
        return Boolean.parseBoolean(value);
    }
}
