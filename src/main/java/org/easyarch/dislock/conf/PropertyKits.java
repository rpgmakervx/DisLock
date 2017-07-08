package org.easyarch.dislock.conf;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by code4j on 2017-7-8.
 */
public class PropertyKits {

    public static Properties loadProperties(String configPath){
        Properties properties = new Properties();
        InputStream is = null;
        try {
            is = new FileInputStream(configPath);
            properties.load(is);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return properties;
    }

}