package com.randomname.mrakopedia.models.api.search;

import com.google.gson.annotations.SerializedName;

/**
 * Created by vgrigoryev on 08.02.2016.
 */
public class SearchResult {
    private Query query;

    @SerializedName("continue")
    private Continue searchContinue;

    private String batchcomplete;

    public Query getQuery ()
    {
        return query;
    }

    public void setQuery (Query query)
    {
        this.query = query;
    }

    public Continue getSearchContinue() {
        return searchContinue;
    }

    public void setSearchContinue(Continue searchContinue) {
        this.searchContinue = searchContinue;
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
        return "ClassPojo [query = "+query+", continue = "+searchContinue+", batchcomplete = "+batchcomplete+"]";
    }
}
