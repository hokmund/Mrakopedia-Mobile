package com.randomname.mrakopedia.utils;

/**
 * Created by vgrigoryev on 20.01.2016.
 */
public class StringUtils {
    public static String declarationOfNum (int number, String[] titles) {
        int[] cases = {2, 0, 1, 1, 1, 2};

        return titles[(number % 100 > 4 && number % 100 < 20) ? 2 : cases[(number % 10 < 5) ? number % 10 : 5]];
    }
}
