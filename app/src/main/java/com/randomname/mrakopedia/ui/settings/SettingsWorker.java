package com.randomname.mrakopedia.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import com.randomname.mrakopedia.models.realm.ColorScheme;
import com.randomname.mrakopedia.models.realm.PageSummaryRealm;
import com.randomname.mrakopedia.realm.DBWorker;

import java.util.prefs.Preferences;

/**
 * Created by vgrigoryev on 11.02.2016.
 */
public class SettingsWorker {

    private static final String CACHING_PHOTO_ENABLED = "cachingPhotoEnabled";
    private static final String CACHING_PAGES_ENABLED = "cachingPagesEnabled";
    private static final String CURRENT_COLOR_SCHEME = "currentColorScheme";

    private static final String PREFERENCES_FILE = "mrak_prefs";

    private static Context context;
    private static SettingsWorker preferences;

    public static SettingsWorker getInstance(Context context) {
        if (preferences == null) {
            preferences = new SettingsWorker();
        }
        SettingsWorker.context = context;
        return preferences;
    }

    private SettingsWorker() {
    }

    public  void clear() {
        //clear all preferences
        open().edit().clear().commit();
    }

    protected SharedPreferences open() {
        return context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    protected SharedPreferences.Editor edit() {
        return open().edit();
    }

    private boolean getBoolean(String key) { return open().getBoolean(key, false); }

    private boolean getBoolean(String key, boolean defaultValue) { return open().getBoolean(key, defaultValue); }

    private void  putBoolean(String key, boolean value) { edit().putBoolean(key, value).commit(); }

    private String getString(String key) { return open().getString(key, ""); }

    private void putString(String key, String value) { edit().putString(key, value).commit(); }

    private int getInt(String key) { return open().getInt(key, 0); }

    private void putInt(String key, int value) { edit().putInt(key, value).commit(); }

    public boolean isPhotoCachingEnabled() {
        return getBoolean(CACHING_PHOTO_ENABLED, true);
    }

    public void setIsPhotoCachingEnabled(boolean value) {
        putBoolean(CACHING_PHOTO_ENABLED, value);
    }

    public boolean isPagesCachingEnabled() {
        return getBoolean(CACHING_PAGES_ENABLED, true);
    }

    public void setIsPagesCachingEnabled(boolean value) {
        putBoolean(CACHING_PAGES_ENABLED, value);
    }

    public void setCurrentColorScheme(ColorScheme colorScheme) {
        putInt(CURRENT_COLOR_SCHEME, colorScheme.getSchemeId());
    }

    public ColorScheme getCurrentColorScheme() {
        int schemeId = getInt(CURRENT_COLOR_SCHEME);

        ColorScheme colorScheme = DBWorker.getColorScheme(schemeId);

        if (colorScheme != null) {
            return colorScheme;
        }

        return new ColorScheme(DBWorker.getNextColorSchemeId(), Color.WHITE, Color.BLACK, Color.GREEN);
    }
}
