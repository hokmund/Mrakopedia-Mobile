package com.randomname.mrakopedia.models.api.recentchanges;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Vlad on 27.01.2016.
 */
public class RecentChangesResult {
    private Query query;

    @SerializedName("continue")
    private Continue mContinue;

    private String batchcomplete;

    public Query getQuery ()
    {
        return query;
    }

    public void setQuery (Query query)
    {
        this.query = query;
    }

    public Continue getmContinue ()
    {
        return mContinue;
    }

    public void setmContinue (Continue mContinue)
    {
        this.mContinue = mContinue;
    }

    public String getBatchcomplete ()
    {
        return batchcomplete;
    }

    public void setBatchcomplete (String batchcomplete)
    {
        this.batchcomplete = batchcomplete;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [query = "+query+", continue = "+mContinue+", batchcomplete = "+batchcomplete+"]";
    }
}
