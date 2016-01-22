package com.randomname.mrakopedia.utils;

import java.net.InetAddress;

/**
 * Created by vgrigoryev on 22.01.2016.
 */
public class NetworkUtils {
    public static boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com"); //You can replace it with your name

            if (ipAddr.equals("")) {
                return false;
            } else {
                return true;
            }

        } catch (Exception e) {
            return false;
        }

    }
}
