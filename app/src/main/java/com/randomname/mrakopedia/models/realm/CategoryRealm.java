package com.randomname.mrakopedia.models.realm;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Vlad on 04.02.2016.
 */
public class CategoryRealm extends RealmObject {

    @PrimaryKey
    private String title;
    private RealmList<TextSectionRealm> descriptionSections;
    private RealmList<RealmString> categoryMembersTitles;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public RealmList<RealmString> getCategoryMembersTitles() {
        return categoryMembersTitles;
    }

    public void setCategoryMembersTitles(RealmList<RealmString> categoryMembersTitles) {
        this.categoryMembersTitles = categoryMembersTitles;
    }

    public RealmList<TextSectionRealm> getDescriptionSections() {
        return descriptionSections;
    }

    public void setDescriptionSections(RealmList<TextSectionRealm> textSections) {
        this.descriptionSections = textSections;
    }
}
