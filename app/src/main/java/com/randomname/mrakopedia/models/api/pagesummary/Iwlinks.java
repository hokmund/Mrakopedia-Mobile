package com.randomname.mrakopedia.models.api.pagesummary;

import com.google.gson.annotations.SerializedName;

/**
 * Created by vgrigoryev on 27.01.2016.
 */
public class Iwlinks {
    private String prefix;

    @SerializedName("*")
    private String title;

    private String url;

    public String getPrefix ()
    {
        return prefix;
    }

    public void setPrefix (String prefix)
    {
        this.prefix = prefix;
    }

    public String getTitle ()
    {
        return title;
    }

    public void setTitle (String title)
    {
        this.title = title;
    }

    public String getUrl ()
    {
        return url;
    }

    public void setUrl (String url)
    {
        this.url = url;
    }

    @Override
    public String toString()
    {
        return "class iwlinks [prefix = "+prefix+", title = "+ title +", url = "+url+"]";
    }
}
