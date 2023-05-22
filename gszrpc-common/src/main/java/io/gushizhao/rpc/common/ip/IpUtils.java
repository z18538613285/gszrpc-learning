package io.gushizhao.rpc.common.ip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/5/12 16:33
 */
public class IpUtils {
    private static final Logger logger = LoggerFactory.getLogger(IpUtils.class);

    public static InetAddress getLocalInetAddress() {
        try {
            return InetAddress.getLocalHost();
        } catch (Exception e) {
            logger.error("get local ip address throws exception: {}", e);
        }
        return null;
    }

    public static String getLocalAddress() {
        return getLocalInetAddress().toString();
    }

    public static String getLocalHostName() {
        return getLocalInetAddress().getHostName();
    }

    public static String getLocalHostIp() {
        return getLocalInetAddress().getHostAddress();
    }



}
