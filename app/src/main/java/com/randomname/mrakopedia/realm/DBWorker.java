package com.randomname.mrakopedia.realm;

import com.randomname.mrakopedia.models.api.categorydescription.CategoryDescription;
import com.randomname.mrakopedia.models.api.pagesummary.Categories;
import com.randomname.mrakopedia.models.api.pagesummary.PageSummaryResult;
import com.randomname.mrakopedia.models.api.pagesummary.TextSection;
import com.randomname.mrakopedia.models.realm.CategoryRealm;
import com.randomname.mrakopedia.models.realm.PageSummaryRealm;
import com.randomname.mrakopedia.models.realm.RealmString;
import com.randomname.mrakopedia.models.realm.TextSectionRealm;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;
import rx.Observable;
import rx.functions.Func1;

/**
 * Created by vgrigoryev on 22.01.2016.
 */
public class DBWorker {

    public static boolean isPageSummarySaved(String title) {
        return !Realm.getDefaultInstance().where(PageSummaryRealm.class).equalTo("pageTitle", title).findAll().isEmpty();
    }

    public static void savePageSummary(PageSummaryResult pageSummaryResult, boolean isRead, String pageId, String pageTitle) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        PageSummaryRealm pageSummaryToSave = new PageSummaryRealm();
        pageSummaryToSave.setPageTitle(pageTitle);
        pageSummaryToSave.setIsFavorite(getPageIsFavorite(pageSummaryResult.getParse().getTitle()));
        pageSummaryToSave.setIsRead(isRead);
        pageSummaryToSave.setTextSections(new RealmList<TextSectionRealm>());
        pageSummaryToSave.setPageId(pageId);

        TextSectionRealm textSectionRealm;

        for (TextSection textSection : pageSummaryResult.getParse().getTextSections()) {
            if (textSection.getType() == TextSection.SPACER_TYPE) {
                continue;
            }

            textSectionRealm = new TextSectionRealm();
            textSectionRealm.setText(textSection.getText());
            textSectionRealm.setType(textSection.getType());
            textSectionRealm.setId(pageSummaryResult.getParse().getTitle() + pageSummaryToSave.getTextSections().size());

            pageSummaryToSave.getTextSections().add(textSectionRealm);
        }

        realm.copyToRealmOrUpdate(pageSummaryToSave);
        realm.commitTransaction();

        for (Categories category : pageSummaryResult.getParse().getCategories()) {
            addPageToCategory(pageTitle, category.getTitle());
        }

        realm.close();
    }

    public static void addPageToCategory(String pageTitle, String categoryTitle) {
        Realm realm = Realm.getDefaultInstance();

        CategoryRealm categoryRealm = realm.where(CategoryRealm.class).equalTo("title", categoryTitle).findFirst();

        realm.beginTransaction();

        if (categoryRealm == null) {
            categoryRealm = new CategoryRealm();
            categoryRealm.setTitle(categoryTitle);
            categoryRealm.setCategoryMembersTitles(new RealmList<RealmString>());
            realm.copyToRealmOrUpdate(categoryRealm);
        }

        RealmString realmString = new RealmString();
        realmString.setString(pageTitle);

        boolean toAdd = true;

        for (RealmString categoryMemberTitle : categoryRealm.getCategoryMembersTitles()) {
            if (categoryMemberTitle.getString().equals(pageTitle)) {
                toAdd = false;
                break;
            }
        }

        if (toAdd) {
            categoryRealm.getCategoryMembersTitles().add(realmString);
        }

        realm.commitTransaction();

        realm.close();
    }

    public static void setPageFavoriteStatus(String pageTitle, boolean status) {
        Realm realm = Realm.getDefaultInstance();
        PageSummaryRealm pageSummaryRealm = realm.where(PageSummaryRealm.class).equalTo("pageTitle", pageTitle).findFirst();

        if (pageSummaryRealm != null) {
            realm.beginTransaction();
            pageSummaryRealm.setIsFavorite(status);
            realm.commitTransaction();
        }

        realm.close();
    }

    public static boolean getPageIsFavorite(String pageTitle) {
        Realm realm = Realm.getDefaultInstance();
        PageSummaryRealm pageSummaryRealm = realm.where(PageSummaryRealm.class).equalTo("pageTitle", pageTitle).findFirst();
        boolean status;

        if (pageSummaryRealm == null) {
            status = false;
        } else {
            status = pageSummaryRealm.isFavorite();
        }

        realm.close();

        return status;
    }

    public static void setPageReadStatus(String pageTitle, boolean status) {
        Realm realm = Realm.getDefaultInstance();
        PageSummaryRealm pageSummaryRealm = realm.where(PageSummaryRealm.class).equalTo("pageTitle", pageTitle).findFirst();

        if (pageSummaryRealm != null) {
            realm.beginTransaction();
            pageSummaryRealm.setIsRead(status);
            realm.commitTransaction();
        }

        realm.close();
    }

    public static boolean getPageIsRead(String pageTitle) {
        Realm realm = Realm.getDefaultInstance();
        PageSummaryRealm pageSummaryRealm = realm.where(PageSummaryRealm.class).equalTo("pageTitle", pageTitle).findFirst();
        boolean status;

        if (pageSummaryRealm == null) {
            status = false;
        } else {
            status = pageSummaryRealm.isRead();
        }

        realm.close();

        return status;
    }

    public static ArrayList<String> getReadPages() {
        Realm realm = Realm.getDefaultInstance();
        ArrayList<String> result = new ArrayList<>();
        RealmResults<PageSummaryRealm> pageSummaryRealm = realm.where(PageSummaryRealm.class).equalTo("isRead", true).findAll();

        for (PageSummaryRealm pageSummary : pageSummaryRealm) {
            result.add(pageSummary.getPageTitle());
        }

        realm.close();
        return result;
    }

    public static Observable<PageSummaryRealm> getPageSummary(String title) {
        Realm realm = Realm.getDefaultInstance();

        Observable<PageSummaryRealm> result = realm
                .where(PageSummaryRealm.class)
                .equalTo("pageTitle", title)
                .findFirst()
                .asObservable();

        return result;
    }

    public static Observable<PageSummaryRealm> getPageSummaryById(String pageId) {
        Realm realm = Realm.getDefaultInstance();

        Observable<PageSummaryRealm> result = realm
                .where(PageSummaryRealm.class)
                .equalTo("pageId", pageId)
                .findFirst()
                .asObservable();

        return result;
    }

    public static Observable<PageSummaryRealm> getFavoritePages() {

        Realm realm = Realm.getDefaultInstance();

        RealmResults<PageSummaryRealm> pageSummaryRealms = realm
                .where(PageSummaryRealm.class)
                .equalTo("isFavorite", true)
                .findAll();

        return Observable.from(pageSummaryRealms);
    }

    public static void saveCategoryDescription(CategoryDescription categoryDescription, String title) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        CategoryRealm categoryToSave = realm.where(CategoryRealm.class).equalTo("title", title).findFirst();

        if (categoryToSave == null) {
            categoryToSave = new CategoryRealm();
            categoryToSave.setTitle(title);
        }

        categoryToSave.setDescriptionSections(new RealmList<TextSectionRealm>());


        TextSectionRealm textSectionRealm;

        for (TextSection textSection : categoryDescription.getTextSections()) {
            if (textSection.getType() == TextSection.SPACER_TYPE ||textSection.getType() == TextSection.SEPARATOR_TYPE) {
                continue;
            }

            textSectionRealm = new TextSectionRealm();
            textSectionRealm.setText(textSection.getText());
            textSectionRealm.setType(textSection.getType());
            textSectionRealm.setId(title + "_category_" + categoryToSave.getDescriptionSections().size());

            categoryToSave.getDescriptionSections().add(textSectionRealm);
        }

        realm.copyToRealmOrUpdate(categoryToSave);
        realm.commitTransaction();
        realm.close();
    }

    public static Observable<TextSection> getCategoryDescription(String title) {
        Realm realm = Realm.getDefaultInstance();

        CategoryRealm categoryRealm = realm
                .where(CategoryRealm.class)
                .equalTo("title", title)
                .findFirst();

        if (categoryRealm == null || categoryRealm.getDescriptionSections() == null) {
            return Observable.empty();
        }

        return Observable.from(categoryRealm.getDescriptionSections())
                .map(new Func1<TextSectionRealm, TextSection>() {
                    @Override
                    public TextSection call(TextSectionRealm textSectionRealm) {
                        return new TextSection(textSectionRealm.getType(), textSectionRealm.getText());
                    }
                });
    }

    public static Observable<String> getCategoryMembers(String categoryTitle) {
        Realm realm = Realm.getDefaultInstance();

        CategoryRealm categoryRealm = realm
                .where(CategoryRealm.class)
                .equalTo("title", categoryTitle)
                .findFirst();

        if (categoryRealm == null || categoryRealm.getDescriptionSections() == null) {
            return Observable.empty();
        }

        return Observable.from(categoryRealm.getCategoryMembersTitles())
                .map(new Func1<RealmString, String>() {
                    @Override
                    public String call(RealmString realmString) {
                        return realmString.getString();
                    }
                });
    }

    public static Observable<CategoryRealm> getAllCategories() {
        Realm realm = Realm.getDefaultInstance();

        RealmResults<CategoryRealm> categoryRealm = realm
                .where(CategoryRealm.class)
                .findAll();

        if (categoryRealm == null) {
            return Observable.empty();
        }

        return Observable.from(categoryRealm);
    }
}
