package com.randomname.mrakopedia.models.api.pagesummary;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Vlad on 20.01.2016.
 */
public class Links
{
    private String ns;

    @SerializedName("*")
    private String title;

    private String exists;

    public String getNs ()
    {
        return ns;
    }

    public void setNs (String ns)
    {
        this.ns = ns;
    }

    public String getTitle ()
    {
        return title;
    }

    public void setTitle (String title)
    {
        this.title = title;
    }

    public String getExists ()
    {
        return exists;
    }

    public void setExists (String exists)
    {
        this.exists = exists;
    }

    @Override
    public String toString()
    {
        return "Class Links [ns = "+ns+", title = "+ title +", exists = "+exists+"]";
    }
}

