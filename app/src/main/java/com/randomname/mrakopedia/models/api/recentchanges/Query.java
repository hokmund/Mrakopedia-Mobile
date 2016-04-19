package com.randomname.mrakopedia.models.api.recentchanges;

/**
 * Created by Vlad on 27.01.2016.
 */
public class Query {
    private Recentchanges[] recentchanges;

    public Recentchanges[] getRecentchanges ()
    {
        return recentchanges;
    }

    public void setRecentchanges (Recentchanges[] recentchanges)
    {
        this.recentchanges = recentchanges;
    }

}
