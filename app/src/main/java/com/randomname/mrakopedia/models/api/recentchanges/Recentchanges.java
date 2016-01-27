package com.randomname.mrakopedia.models.api.recentchanges;

/**
 * Created by Vlad on 27.01.2016.
 */
public class Recentchanges {
    private String timestamp;

    private String title;

    private String ns;

    private String revid;

    private String old_revid;

    private String type;

    private String rcid;

    private String pageid;

    private boolean isViewed;

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

    public String getRevid ()
    {
        return revid;
    }

    public void setRevid (String revid)
    {
        this.revid = revid;
    }

    public String getOld_revid ()
    {
        return old_revid;
    }

    public void setOld_revid (String old_revid)
    {
        this.old_revid = old_revid;
    }

    public String getType ()
    {
        return type;
    }

    public void setType (String type)
    {
        this.type = type;
    }

    public String getRcid ()
    {
        return rcid;
    }

    public void setRcid (String rcid)
    {
        this.rcid = rcid;
    }

    public String getPageid ()
    {
        return pageid;
    }

    public void setPageid (String pageid)
    {
        this.pageid = pageid;
    }

    public boolean isViewed() {
        return isViewed;
    }

    public void setIsViewed(boolean isViewed) {
        this.isViewed = isViewed;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [timestamp = "+timestamp+", title = "+title+", ns = "+ns+", revid = "+revid+", old_revid = "+old_revid+", type = "+type+", rcid = "+rcid+", pageid = "+pageid+"]";
    }
}
