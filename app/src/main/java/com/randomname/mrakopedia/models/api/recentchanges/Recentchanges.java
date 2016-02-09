package com.randomname.mrakopedia.models.api.recentchanges;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Vlad on 27.01.2016.
 */
public class Recentchanges implements Parcelable {
    private String timestamp;

    private String title;

    private String ns;

    private String revid;

    private String old_revid;

    private String type;

    private String rcid;

    private String pageid;

    private boolean isViewed;

    private String redirect;

    public Recentchanges() {
    }

    public Recentchanges(Parcel in) {
        timestamp = in.readString();
        title = in.readString();
        ns = in.readString();
        revid = in.readString();
        old_revid = in.readString();
        type = in.readString();
        rcid = in.readString();
        pageid = in.readString();
        redirect = in.readString();
        isViewed = in.readByte() != 0;
    }

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

    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }

    public String getRedirect() {
        return redirect;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [timestamp = "+timestamp+", title = "+title+", ns = "+ns+", revid = "+revid+", old_revid = "+old_revid+", type = "+type+", rcid = "+rcid+", pageid = "+pageid+"]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(timestamp);
        dest.writeString(title);
        dest.writeString(ns);
        dest.writeString(revid);
        dest.writeString(old_revid);
        dest.writeString(type);
        dest.writeString(rcid);
        dest.writeString(pageid);
        dest.writeString(redirect);
        dest.writeByte((byte) (isViewed ? 1 : 0));
    }

    public static final Parcelable.Creator<Recentchanges> CREATOR = new Parcelable.Creator<Recentchanges>() {
        public Recentchanges createFromParcel(Parcel in) {
            return new Recentchanges(in);
        }

        public Recentchanges[] newArray(int size) {
            return new Recentchanges[size];
        }
    };
}
