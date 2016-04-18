package com.randomname.mrakopedia;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.randomname.mrakopedia.realm.DBWorker;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.rx.RealmObservableFactory;

/**
 * Created by vgrigoryev on 22.01.2016.
 */
public class MrakopediaApplication extends Application {
    private Tracker mTracker;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        RealmConfiguration config = new RealmConfiguration.Builder(this)
                .schemaVersion(1)
                .deleteRealmIfMigrationNeeded()
                .rxFactory(new RealmObservableFactory())
                .build();
        Realm.setDefaultConfiguration(config);

        ImageLoaderConfiguration imageConfig = new ImageLoaderConfiguration.Builder(this)
                .threadPoolSize(1)
                .memoryCache(new WeakMemoryCache())
                .build();
        ImageLoader.getInstance().init(imageConfig);

        DBWorker.generateDefaultColorSchemes(this);
    }

    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            analytics.getLogger().setLogLevel(0);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.global_tracker);
        }
        return mTracker;
    }
}
