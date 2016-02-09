package com.randomname.mrakopedia.models.api.search;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by vgrigoryev on 08.02.2016.
 */
public class Search implements Parcelable {
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

    public Search() {
    }

    public Search(Parcel in) {
        timestamp = in.readString();
        title = in.readString();
        ns = in.readString();
        snippet = in.readString();
        wordcount = in.readString();
        size = in.readString();
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
        dest.writeString(snippet);
        dest.writeString(wordcount);
        dest.writeString(size);
    }

    public static final Parcelable.Creator<Search> CREATOR = new Parcelable.Creator<Search>() {
        public Search createFromParcel(Parcel in) {
            return new Search(in);
        }

        public Search[] newArray(int size) {
            return new Search[size];
        }
    };

    @Override
    public String toString()
    {
        return "ClassPojo [timestamp = "+timestamp+", title = "+title+", ns = "+ns+", snippet = "+snippet+", wordcount = "+wordcount+", size = "+size+"]";
    }
}
