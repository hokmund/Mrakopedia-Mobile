package com.randomname.mrakopedia;

import android.app.Application;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.rx.RealmObservableFactory;

/**
 * Created by vgrigoryev on 22.01.2016.
 */
public class MrakopediaApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        RealmConfiguration config = new RealmConfiguration.Builder(this).rxFactory(new RealmObservableFactory()).build();
        Realm.setDefaultConfiguration(config);

        ImageLoaderConfiguration imageConfig = new ImageLoaderConfiguration.Builder(this)
                .threadPoolSize(1)
                .memoryCache(new WeakMemoryCache())
                .build();
        ImageLoader.getInstance().init(imageConfig);
    }
}
