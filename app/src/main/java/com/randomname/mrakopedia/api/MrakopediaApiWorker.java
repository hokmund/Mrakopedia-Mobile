package com.randomname.mrakopedia.api;

import com.randomname.mrakopedia.models.api.categorymembers.CategoryMembersResult;

import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;
import retrofit2.RxJavaCallAdapterFactory;
import rx.Observable;

/**
 * Created by Vlad on 19.01.2016.
 */
public class MrakopediaApiWorker {
    private MrakopediaAPI mrakopediaAPI = null;
    private static MrakopediaApiWorker instance = null;

    public static MrakopediaApiWorker getInstance() {
        if (instance == null) {
            instance = new MrakopediaApiWorker();
        }

        return instance;
    }

    private MrakopediaAPI getMrakopediaAPI() {
        if (mrakopediaAPI == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://mrakopedia.ru/w/")
                    .build();

            mrakopediaAPI = retrofit.create(MrakopediaAPI.class);
        }

        return mrakopediaAPI;
    }

    public Observable<CategoryMembersResult> getCategoryMembers(String category) {
        return getMrakopediaAPI().getCategoryMembers(category);
    }
}
