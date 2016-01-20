package com.randomname.mrakopedia.models.api.pagesummary;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Vlad on 20.01.2016.
 */
public class Categories {
    private String sortkey;

    @SerializedName("*")
    private String title;

    public String getSortkey ()
    {
        return sortkey;
    }

    public void setSortkey (String sortkey)
    {
        this.sortkey = sortkey;
    }

    public String getTitle ()
    {
        return title;
    }

    public void setTitle (String title)
    {
        this.title = title;
    }

    @Override
    public String toString()
    {
        return "Class Categories [sortkey = "+sortkey+", title = " + title +"]";
    }
}
