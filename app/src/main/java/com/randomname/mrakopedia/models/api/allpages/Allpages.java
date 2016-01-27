package com.randomname.mrakopedia.models.api.allpages;

/**
 * Created by vgrigoryev on 27.01.2016.
 */
public class Allpages {

    private String title;

    private String ns;

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

    public String getPageid ()
    {
        return pageid;
    }

    public void setPageid (String pageid)
    {
        this.pageid = pageid;
    }

    public void setIsViewed(boolean isViewed) {
        this.isViewed = isViewed;
    }

    public boolean isViewed() {
        return isViewed;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [title = "+title+", ns = "+ns+", pageid = "+pageid+"]";
    }

}
