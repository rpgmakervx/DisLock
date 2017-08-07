package org.easyarch.dislock.kits;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * Created by xingtianyu(code4j) on 2017-7-10.
 */
public class PropertyKits {

    private static Logger logger = LoggerFactory.getLogger(PropertyKits.class.getName());

    private Properties prop;

    public PropertyKits(){
        prop = new Properties();
    }

    public PropertyKits(String filePath) throws Exception {
        prop = new Properties();
        init(filePath);
        debug();
    }

    private void init(String confPath) throws Exception {
        FileInputStream fis = new FileInputStream(confPath);
        prop.load(fis);
        fis.close();
    }

    public String getString(String key){
        return prop.getProperty(key);
    }

    /**
     * 解析出host相关信息
     * @param key
     * @return
     * @throws UnknownHostException
     */
    public List<InetSocketAddress> getAddress(String key) throws UnknownHostException {
        List<InetSocketAddress> addressLsit = new ArrayList<>();
        String value = prop.getProperty(key);
        String[] addresses = value.split(",");
        if (StringUtils.isEmpty(value)){
            return addressLsit;
        }
        for (String addr:addresses){
            String[] segments = addr.split(":");
            InetSocketAddress address = new InetSocketAddress(
                    InetAddress.getByName(segments[0]),Integer.valueOf(segments[1]));
            addressLsit.add(address);
        }
        return addressLsit;
    }

    private void debug(){
        Enumeration<?> enums = prop.propertyNames();
        while (enums.hasMoreElements()){
            Object key = enums.nextElement();
            String k = String.valueOf(key);
            logger.debug(k,getString(k));
        }
    }

    public void setString(String name,String value){
        prop.setProperty(name,value);
    }

}
