package com.randomname.mrakopedia.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.randomname.mrakopedia.MrakopediaApplication;

/**
 * Created by vgrigoryev on 22.01.2016.
 */
public class NetworkUtils {
    public static boolean isInternetAvailable(Context c) {
        Context context = c;
        if (context == null) {
            context = MrakopediaApplication.getContext();
        }

        if (context == null) {
            return false;
        }


        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }
}
