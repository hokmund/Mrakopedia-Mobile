package com.randomname.mrakopedia.models.api.categorymembers;

/**
 * Created by Vlad on 18.01.2016.
 */
public class Categorymembers
{
    private String title;

    private String ns;

    private String type;

    private String pageid;

    private boolean isViewed;

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

    public String getType ()
    {
        return type;
    }

    public void setType (String type)
    {
        this.type = type;
    }

    public String getPageid ()
    {
        return pageid;
    }

    public void setPageid (String pageid)
    {
        this.pageid = pageid;
    }

    public boolean getIsViewed () {
        return isViewed;
    }

    public void setIsViewed(boolean isViewed) {
        this.isViewed = isViewed;
    }

    @Override
    public String toString()
    {
        return "class Categorymembers [\ntitle = "+title+",\n ns = "+ns+",\n type = "+type+",\n pageid = "+pageid+"]";
    }
}
