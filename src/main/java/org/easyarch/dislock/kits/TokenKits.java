package org.easyarch.dislock.kits;

import org.easyarch.dislock.sys.SysProperties;

/**
 * Created by xingtianyu(code4j) on 2017-7-15.
 */
public class TokenKits {

    public static String getToken(){
        StringBuffer buffer = new StringBuffer();
        try {
            buffer.append(SysProperties.mac())
                    .append("|")
                    .append(SysProperties.jvmPid())
                    .append("|")
                    .append(SysProperties.threadId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buffer.toString();
    }

}
