package com.randomname.mrakopedia.models.api.categorymembers;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Vlad on 18.01.2016.
 */
public class Categorymembers implements Parcelable {

    private String title;

    private String ns;

    private String type;

    private String pageid;

    private boolean isViewed;

    public Categorymembers() {
    }

    public Categorymembers(Parcel in) {
        title = in.readString();
        ns = in.readString();
        type = in.readString();
        pageid = in.readString();
        isViewed = in.readByte() != 0;
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
    public String toString() {
        return "class Categorymembers [\ntitle = "+title+",\n ns = "+ns+",\n type = "+type+",\n pageid = "+pageid+"]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(ns);
        dest.writeString(type);
        dest.writeString(pageid);
        dest.writeByte((byte) (isViewed ? 1 : 0));
    }

    public static final Parcelable.Creator<Categorymembers> CREATOR = new Parcelable.Creator<Categorymembers>() {
        public Categorymembers createFromParcel(Parcel in) {
            return new Categorymembers(in);
        }

        public Categorymembers[] newArray(int size) {
            return new Categorymembers[size];
        }
    };
}
