package com.randomname.mrakopedia.models.api.allcategories;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by vgrigoryev on 20.01.2016.
 */
public class Allcategories implements Parcelable {
    private String files;

    private String pages;

    @SerializedName("*")
    private String title;

    private String subcats;

    private String size;

    private int id;
    private static int globalId = 0;

    public Allcategories() {
        id = globalId++;
    }

    public Allcategories(Parcel in) {
        files = in.readString();
        pages = in.readString();
        title = in.readString();
        subcats = in.readString();
        size = in.readString();
        id = in.readInt();
    }

    public int getId() {
        return id;
    }

    public String getFiles ()
    {
        return files;
    }

    public void setFiles (String files)
    {
        this.files = files;
    }

    public String getPages ()
    {
        return pages;
    }

    public void setPages (String pages)
    {
        this.pages = pages;
    }

    public String getTitle ()
    {
        return title;
    }

    public void setTitle (String title)
    {
        this.title = title;
    }

    public String getSubcats ()
    {
        return subcats;
    }

    public void setSubcats (String subcats)
    {
        this.subcats = subcats;
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
        return "ClassPojo [files = "+files+", pages = "+pages+", title = "+title+", subcats = "+subcats+", size = "+size+"]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(files);
        dest.writeString(pages);
        dest.writeString(title);
        dest.writeString(subcats);
        dest.writeString(size);
        dest.writeInt(id);
    }

    public static final Parcelable.Creator<Allcategories> CREATOR = new Parcelable.Creator<Allcategories>() {
        public Allcategories createFromParcel(Parcel in) {
            return new Allcategories(in);
        }

        public Allcategories[] newArray(int size) {
            return new Allcategories[size];
        }
    };
}
