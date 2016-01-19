package com.randomname.mrakopedia.models.api.categorymembers;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Vlad on 18.01.2016.
 */
public class CategoryMembersResult {

    private Query query;

    private String batchcomplete;

    @SerializedName("continue")
    private Continue mContinue;

    public CategoryMembersResult() {
    }

    public Query getQuery ()
    {
        return query;
    }

    public void setQuery (Query query)
    {
        this.query = query;
    }

    public String getBatchcomplete ()
    {
        return batchcomplete;
    }

    public void setBatchcomplete (String batchcomplete)
    {
        this.batchcomplete = batchcomplete;
    }

    public Continue getmContinue() {
        return mContinue;
    }

    public void setmContinue(Continue mContinue) {
        this.mContinue = mContinue;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [query = " + query + ", batchcomplete = " + batchcomplete.toString() + ", continue = " + mContinue.toString() +"]";
    }
}

