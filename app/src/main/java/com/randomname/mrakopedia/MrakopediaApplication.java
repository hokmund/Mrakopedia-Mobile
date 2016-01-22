package com.randomname.mrakopedia;

import android.app.Application;

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
    }
}
