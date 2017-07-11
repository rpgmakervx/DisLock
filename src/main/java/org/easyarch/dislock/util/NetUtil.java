package org.easyarch.dislock.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;

/**
 * Created by code4j on 2017-7-9.
 */
public class NetUtil {

    public static String mac() throws Exception {
        byte[] macAddr = NetworkInterface.getByInetAddress(
                InetAddress.getLocalHost()).getHardwareAddress();
        if (macAddr == null){
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        for (int index=0;index<macAddr.length;index++){
            if(index!=0){
                buffer.append("-");
            }
            String str = Integer.toHexString(macAddr[index] & 0xFF);
            buffer.append(str);
        }
        return buffer.toString();
    }

    public static void main(String[] args) throws Exception {
        System.out.println(mac());
    }
}
