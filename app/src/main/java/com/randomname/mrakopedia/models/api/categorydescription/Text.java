package com.randomname.mrakopedia.models.api.categorydescription;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Vlad on 24.01.2016.
 */
public class Text {
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
        return "class Text [text = "+ text +"]";
    }
}