package com.randomname.mrakopedia.api;

import com.randomname.mrakopedia.models.api.allcategories.AllCategoriesResult;
import com.randomname.mrakopedia.models.api.categorymembers.CategoryMembersResult;
import com.randomname.mrakopedia.models.api.pagesummary.PageSummaryResult;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by Vlad on 18.01.2016.
 */
public interface MrakopediaAPI {

    @GET("api.php?action=query&cmprop=ids|title|type&continue=&format=json&list=categorymembers&cmlimit=100")
    Observable<CategoryMembersResult> getCategoryMembers(
            @Query("cmtitle") String categoryTitle,
            @Query("cmcontinue") String continueString
    );

    @GET("api.php?action=query&continue=&format=json&list=allcategories&acmin=1&aclimit=100&acprop=size|hidden")
    Observable<AllCategoriesResult> getAllCategories(
            @Query("accontinue") String continueString
    );

    @GET("api.php?action=parse&format=json")
    Observable<PageSummaryResult> getPageContent(
            @Query("page") String pageTitle
    );
}
