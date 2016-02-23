package com.randomname.mrakopedia.models.api.pagesummary;

import android.os.Parcel;
import android.os.Parcelable;

import com.randomname.mrakopedia.models.realm.RealmString;

import java.util.ArrayList;

import io.realm.RealmList;

/**
 * Created by vgrigoryev on 26.01.2016.
 */
public class CategoriesTextSection extends TextSection implements Parcelable {

    private ArrayList<String> categoriesArrayList;

    public CategoriesTextSection() {
        super(CATEGORY_TYPE, "Категории статьи");
        categoriesArrayList = new ArrayList<>();
    }

    public CategoriesTextSection(RealmList<RealmString> categoriesTitles) {
        super(CATEGORY_TYPE, "Категории статьи");
        categoriesArrayList = new ArrayList<>();

        for (RealmString realmString : categoriesTitles) {
            categoriesArrayList.add(realmString.getString());
        }
    }

    public CategoriesTextSection(Parcel in) {
        super(in);
        categoriesArrayList = new ArrayList<>();
        in.readStringList(categoriesArrayList);
    }

    public ArrayList<String> getCategoriesArrayList() {
        return categoriesArrayList;
    }

    public void setCategoriesArrayList(ArrayList<String> categoriesArrayList) {
        this.categoriesArrayList = categoriesArrayList;
    }

    public void addCategory(String categoryTitle) {
        categoriesArrayList.add(categoryTitle);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeStringList(categoriesArrayList);
    }

    public static final Parcelable.Creator<CategoriesTextSection> CREATOR = new Parcelable.Creator<CategoriesTextSection>() {
        public CategoriesTextSection createFromParcel(Parcel in) {
            return new CategoriesTextSection(in);
        }

        public CategoriesTextSection[] newArray(int size) {
            return new CategoriesTextSection[size];
        }
    };
}
