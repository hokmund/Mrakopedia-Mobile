package com.randomname.mrakopedia.models.api.pagesummary;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Vlad on 20.01.2016.
 */
public class Text
{
    @SerializedName("*")
    private String text;

    public String getText ()
    {
        return text;
    }

    public void setText (String text)
    {
        this.text = text;
    }

    @Override
    public String toString()
    {
        return "Class text [text = " + text +"]";
    }
}
