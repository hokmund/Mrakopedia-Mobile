package com.randomname.mrakopedia.realm;

import com.randomname.mrakopedia.models.api.pagesummary.PageSummaryResult;
import com.randomname.mrakopedia.models.api.pagesummary.TextSection;
import com.randomname.mrakopedia.models.realm.PageSummaryRealm;
import com.randomname.mrakopedia.models.realm.TextSectionRealm;

import org.w3c.dom.Text;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import rx.Observable;

/**
 * Created by vgrigoryev on 22.01.2016.
 */
public class DBWorker {

    public static boolean isPageSummarySaved(String title) {
        return !Realm.getDefaultInstance().where(PageSummaryRealm.class).equalTo("pageTitle", title).findAll().isEmpty();
    }

    public static void savePageSummary(PageSummaryResult pageSummaryResult, boolean isRead) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        PageSummaryRealm pageSummaryToSave = new PageSummaryRealm();
        pageSummaryToSave.setPageTitle(pageSummaryResult.getParse().getTitle());
        pageSummaryToSave.setIsFavorite(getPageIsFavorite(pageSummaryResult.getParse().getTitle()));
        pageSummaryToSave.setIsRead(isRead);
        pageSummaryToSave.setTextSections(new RealmList<TextSectionRealm>());

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
        realm.close();
    }

    public static void setPageFavoriteStatus(String pageTitle, boolean status) {
        Realm realm = Realm.getDefaultInstance();
        PageSummaryRealm pageSummaryRealm = realm.where(PageSummaryRealm.class).equalTo("pageTitle", pageTitle).findFirst();

        realm.beginTransaction();
        pageSummaryRealm.setIsFavorite(status);
        realm.commitTransaction();

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

    public static Observable<PageSummaryRealm> getFavoritePages() {

        RealmResults<PageSummaryRealm> pageSummaryRealms = Realm.getDefaultInstance()
                .where(PageSummaryRealm.class)
                .equalTo("isFavorite", true)
                .findAll();

        return Observable.from(pageSummaryRealms);
    }
}
