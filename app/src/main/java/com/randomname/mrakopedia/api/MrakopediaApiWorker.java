package com.randomname.mrakopedia.api;

import com.randomname.mrakopedia.models.api.allcategories.AllCategoriesResult;
import com.randomname.mrakopedia.models.api.categorydescription.CategoryDescription;
import com.randomname.mrakopedia.models.api.categorymembers.CategoryMembersResult;
import com.randomname.mrakopedia.models.api.pagesummary.PageSummaryResult;
import com.randomname.mrakopedia.models.api.recentchanges.RecentChangesResult;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
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
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            // set your desired log level
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            // add your other interceptors …

            // add logging as last interceptor
            httpClient.interceptors().add(logging);  // <-- this is the important line!

            Retrofit retrofit = new Retrofit.Builder()
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://mrakopedia.ru/w/")
                    .client(httpClient.build())
                    .build();

            mrakopediaAPI = retrofit.create(MrakopediaAPI.class);
        }

        return mrakopediaAPI;
    }

    public Observable<CategoryMembersResult> getCategoryMembers(String category, String continueString) {
        return getMrakopediaAPI().getCategoryMembers(category, continueString);
    }

    public Observable<AllCategoriesResult> getAllCategories(String continueString) {
        return getMrakopediaAPI().getAllCategories(continueString);
    }

    public Observable<PageSummaryResult> getPageSummary(String pageTitle) {
        return getMrakopediaAPI().getPageContent(pageTitle);
    }

    public Observable<CategoryDescription> getCategoryDescription(String categoryTitle) {
        return getMrakopediaAPI().getCategoryDescription("Категория:" + categoryTitle);
    }

    public Observable<RecentChangesResult> getRecentChanges(String continueString) {
        if (continueString.isEmpty()) {
            return getMrakopediaAPI().getRecentChanges();
        } else {
            return getMrakopediaAPI().getRecentChanges(continueString);
        }
    }
}
