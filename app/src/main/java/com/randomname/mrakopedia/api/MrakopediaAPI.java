package com.randomname.mrakopedia.api;

import com.randomname.mrakopedia.models.api.categorymembers.CategoryMembersResult;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by Vlad on 18.01.2016.
 */
public interface MrakopediaAPI {

    @GET("api.php?action=query&cmprop=ids|title|type&continue=&format=json&list=categorymembers&cmlimit=100")
    Observable<CategoryMembersResult> getCategoryMembers(@Query("cmtitle") String categoryTitle);
}
