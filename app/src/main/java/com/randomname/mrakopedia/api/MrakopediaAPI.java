package com.randomname.mrakopedia.api;

import com.randomname.mrakopedia.models.api.allcategories.AllCategoriesResult;
import com.randomname.mrakopedia.models.api.categorydescription.CategoryDescription;
import com.randomname.mrakopedia.models.api.categorymembers.CategoryMembersResult;
import com.randomname.mrakopedia.models.api.pagesummary.PageSummaryResult;
import com.randomname.mrakopedia.models.api.recentchanges.RecentChangesResult;
import com.randomname.mrakopedia.models.api.search.SearchResult;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by Vlad on 18.01.2016.
 */
public interface MrakopediaAPI {

    @GET("api.php?action=query&cmprop=ids|title|type&continue=&format=json&list=categorymembers&cmlimit=500")
    Observable<CategoryMembersResult> getCategoryMembers(
            @Query("cmtitle") String categoryTitle,
            @Query("cmcontinue") String continueString
    );

    @GET("api.php?action=query&continue=&format=json&list=allcategories&acmin=1&aclimit=500&acprop=size|hidden")
    Observable<AllCategoriesResult> getAllCategories(
            @Query("accontinue") String continueString
    );

    @GET("api.php?action=parse&format=json")
    Observable<PageSummaryResult> getPageContent(
            @Query("pageid") String pageId
    );

    @GET("api.php?action=parse&format=json")
    Observable<PageSummaryResult> getPageContentByTitle(
            @Query("page") String pageTitle
    );

    @GET("api.php?action=parse&prop=text&format=json")
    Observable<CategoryDescription> getCategoryDescription(
            @Query("page") String pageTitle
    );

    @GET("api.php?action=query&list=recentchanges&rcshow=!redirect&rctype=new&format=json&continue=&rctype=new&rclimit=50&rcprop=redirect|timestamp|title|ids|sizes")
    Observable<RecentChangesResult> getRecentChanges(
        @Query("rccontinue") String continueString
    );

    @GET("api.php?action=query&list=recentchanges&format=json&continue=&rctype=new&rclimit=50&rcprop=redirect|timestamp|title|ids")
    Observable<RecentChangesResult> getRecentChanges(
    );

    @GET("api.php?action=query&format=json&continue=&list=search&srlimit=50")
    Observable<SearchResult> search(
        @Query("srsearch") String searchString,
        @Query("sroffset") String offset,
        @Query("srwhat") String whatToSearch
    );

    @GET("/wiki/рейтинг:{path}")
    Call<ResponseBody> getCategoryRating(@Path("path") String path);
}
