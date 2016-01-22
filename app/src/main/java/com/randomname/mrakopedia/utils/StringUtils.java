package com.randomname.mrakopedia.utils;

/**
 * Created by vgrigoryev on 20.01.2016.
 */
public class StringUtils {
    public static String declarationOfNum (int number, String[] titles) {
        int[] cases = {2, 0, 1, 1, 1, 2};

        return titles[(number % 100 > 4 && number % 100 < 20) ? 2 : cases[(number % 10 < 5) ? number % 10 : 5]];
    }

    public static CharSequence trimTrailingWhitespace(CharSequence source) {

        if(source == null)
            return "";

        int i = source.length();

        // loop back to the first non-whitespace character
        while(--i >= 0 && Character.isWhitespace(source.charAt(i))) {
        }

        return source.subSequence(0, i+1);
    }
}
