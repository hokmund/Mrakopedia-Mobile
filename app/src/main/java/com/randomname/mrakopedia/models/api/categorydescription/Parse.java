package com.randomname.mrakopedia.models.api.categorydescription;

/**
 * Created by Vlad on 24.01.2016.
 */
public class Parse
{
    private Text text;

    private String title;

    public Text getText ()
    {
        return text;
    }

    public void setText (Text text)
    {
        this.text = text;
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
        return "ClassPojo [text = "+text+", title = "+title+"]";
    }
}
