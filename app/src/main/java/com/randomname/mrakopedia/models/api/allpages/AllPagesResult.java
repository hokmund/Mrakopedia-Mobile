package com.randomname.mrakopedia.models.api.allpages;

import com.google.gson.annotations.SerializedName;

/**
 * Created by vgrigoryev on 27.01.2016.
 */
public class AllPagesResult {
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

    public Continue getContinue ()
    {
        return mContinue;
    }

    public void setContinue (Continue mContinue)
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
