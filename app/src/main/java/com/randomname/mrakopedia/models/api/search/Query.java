package com.randomname.mrakopedia.models.api.search;

/**
 * Created by vgrigoryev on 08.02.2016.
 */
public class Query {
    private Search[] search;

    private Searchinfo searchinfo;

    public Search[] getSearch ()
    {
        return search;
    }

    public void setSearch (Search[] search)
    {
        this.search = search;
    }

    public Searchinfo getSearchinfo ()
    {
        return searchinfo;
    }

    public void setSearchinfo (Searchinfo searchinfo)
    {
        this.searchinfo = searchinfo;
    }
}
