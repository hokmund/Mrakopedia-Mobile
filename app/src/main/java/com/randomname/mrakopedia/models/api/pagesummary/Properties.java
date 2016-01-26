package com.randomname.mrakopedia.models.api.pagesummary;

import com.google.gson.annotations.SerializedName;

/**
 * Created by vgrigoryev on 26.01.2016.
 */
public class Properties {
    private String name;

    @SerializedName("*")
    private String title;

    public String getName ()
    {
        return name;
    }

    public void setName (String name)
    {
        this.name = name;
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
        return "class Properties [name = "+name+", title = "+ title +"]";
    }
}
