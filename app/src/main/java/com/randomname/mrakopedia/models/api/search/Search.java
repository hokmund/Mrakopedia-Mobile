package com.randomname.mrakopedia.models.api.search;

/**
 * Created by vgrigoryev on 08.02.2016.
 */
public class Search {
    private String timestamp;

    private String title;

    private String ns;

    private String snippet;

    private String wordcount;

    private String size;

    public String getTimestamp ()
    {
        return timestamp;
    }

    public void setTimestamp (String timestamp)
    {
        this.timestamp = timestamp;
    }

    public String getTitle ()
    {
        return title;
    }

    public void setTitle (String title)
    {
        this.title = title;
    }

    public String getNs ()
    {
        return ns;
    }

    public void setNs (String ns)
    {
        this.ns = ns;
    }

    public String getSnippet ()
    {
        return snippet;
    }

    public void setSnippet (String snippet)
    {
        this.snippet = snippet;
    }

    public String getWordcount ()
    {
        return wordcount;
    }

    public void setWordcount (String wordcount)
    {
        this.wordcount = wordcount;
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
        return "ClassPojo [timestamp = "+timestamp+", title = "+title+", ns = "+ns+", snippet = "+snippet+", wordcount = "+wordcount+", size = "+size+"]";
    }
}
