package org.easyarch.dislock.sys;

import sun.management.VMManagement;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by xingtianyu(code4j) on 2017-7-15.
 */
public class SysProperties {

    public static String mac(){
        byte[] macAddr = new byte[0];
        try {
            macAddr = NetworkInterface.getByInetAddress(
                    InetAddress.getLocalHost()).getHardwareAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public static int jvmPid(){
        try {
            RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
            Field jvm = runtime.getClass().getDeclaredField("jvm");
            jvm.setAccessible(true);
            VMManagement mgmt = (VMManagement) jvm.get(runtime);
            Method pidMethod = mgmt.getClass().getDeclaredMethod("getProcessId");
            pidMethod.setAccessible(true);
            int pid = (Integer) pidMethod.invoke(mgmt);
            return pid;
        } catch (Exception e) {
            return -1;
        }
    }

    public static long threadId(){
        return Thread.currentThread().getId();
    }

    public static long sysMillisTime(){
        return System.currentTimeMillis();
    }

    public static String uniqueId(){
        return mac()+jvmPid()+threadId();
    }

    public static void main(String[] args) {
        System.out.println(jvmPid());;
    }

}
