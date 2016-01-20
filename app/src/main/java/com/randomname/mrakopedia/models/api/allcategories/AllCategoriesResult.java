package com.randomname.mrakopedia.models.api.allcategories;

import com.google.gson.annotations.SerializedName;

/**
 * Created by vgrigoryev on 20.01.2016.
 */
public class AllCategoriesResult {
    private Query query;

    @SerializedName("continue")
    private Continue accContinue;

    private String batchcomplete;

    public Query getQuery ()
    {
        return query;
    }

    public void setQuery (Query query)
    {
        this.query = query;
    }

    public Continue getAccContinue ()
    {
        return accContinue;
    }

    public void setAccContinue (Continue accContinue)
    {
        this.accContinue = accContinue;
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
        return "ClassPojo [query = "+query+", accContinue = "+accContinue+", batchcomplete = "+batchcomplete+"]";
    }
}
