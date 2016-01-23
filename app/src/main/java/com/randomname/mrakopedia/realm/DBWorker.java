package com.randomname.mrakopedia.realm;

import android.content.Context;
import android.util.Log;

import com.randomname.mrakopedia.models.api.pagesummary.PageSummaryResult;
import com.randomname.mrakopedia.models.api.pagesummary.TextSection;
import com.randomname.mrakopedia.models.realm.PageSummaryRealm;
import com.randomname.mrakopedia.models.realm.TextSectionRealm;

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

    public static void savePageSummary(PageSummaryResult pageSummaryResult) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        PageSummaryRealm pageSummaryToSave = new PageSummaryRealm();
        pageSummaryToSave.setPageTitle(pageSummaryResult.getParse().getTitle());
        pageSummaryToSave.setTextSections(new RealmList<TextSectionRealm>());

        TextSectionRealm textSectionRealm;

        for (TextSection textSection : pageSummaryResult.getParse().getTextSections()) {
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

    public static Observable<PageSummaryRealm> getPageSummary(String title) {
        return Realm.getDefaultInstance()
                .where(PageSummaryRealm.class)
                .equalTo("pageTitle", title)
                .findFirst()
                .asObservable();
    }

    public static void log() {
        RealmResults<TextSectionRealm> result = Realm.getDefaultInstance().where(TextSectionRealm.class).findAll();

        Log.e("bla", result.size() + "");
    }
}
