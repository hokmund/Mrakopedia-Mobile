package com.randomname.mrakopedia.models.realm;

import com.randomname.mrakopedia.models.api.pagesummary.PageSummaryResult;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by vgrigoryev on 22.01.2016.
 */
public class PageSummaryRealm extends RealmObject {

    @PrimaryKey
    private String pageTitle;
    private RealmList<TextSectionRealm> textSections;

    public PageSummaryRealm() {
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    public RealmList<TextSectionRealm> getTextSections() {
        return textSections;
    }

    public void setTextSections(RealmList<TextSectionRealm> textSections) {
        this.textSections = textSections;
    }
}
