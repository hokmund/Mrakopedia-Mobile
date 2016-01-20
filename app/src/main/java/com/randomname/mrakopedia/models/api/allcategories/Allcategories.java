package com.randomname.mrakopedia.models.api.allcategories;

import com.google.gson.annotations.SerializedName;

/**
 * Created by vgrigoryev on 20.01.2016.
 */
public class Allcategories {
    private String files;

    private String pages;

    @SerializedName("*")
    private String title;

    private String subcats;

    private String size;

    public String getFiles ()
    {
        return files;
    }

    public void setFiles (String files)
    {
        this.files = files;
    }

    public String getPages ()
    {
        return pages;
    }

    public void setPages (String pages)
    {
        this.pages = pages;
    }

    public String getTitle ()
    {
        return title;
    }

    public void setTitle (String title)
    {
        this.title = title;
    }

    public String getSubcats ()
    {
        return subcats;
    }

    public void setSubcats (String subcats)
    {
        this.subcats = subcats;
    }

    public String getSize ()
    {
        return size;
    }

    public void setSize (String size)
    {
        this.size = size;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [files = "+files+", pages = "+pages+", title = "+title+", subcats = "+subcats+", size = "+size+"]";
    }
}
