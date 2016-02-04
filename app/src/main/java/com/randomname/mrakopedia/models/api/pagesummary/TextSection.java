package com.randomname.mrakopedia.models.api.pagesummary;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by vgrigoryev on 22.01.2016.
 */
public class TextSection implements Parcelable {

    public static final int UNDEFINED_TYPE = 0;
    public static final int TEXT_TYPE = 1;
    public static final int IMAGE_TYPE = 2;
    public static final int TEMPLATE_TYPE = 3;
    public static final int LINK_TYPE = 4;
    public static final int CATEGORY_TYPE = 5;
    public static final int SPACER_TYPE = 6;
    public static final int SEPARATOR_TYPE = 7;
    public static final int YOUTUBE_TYPE = 8;
    public static final int GIF_TYPE = 9;

    private int type;
    private String text;

    public TextSection(int type, String text) {
        this.type = type;
        this.text = text;
    }

    public TextSection(Parcel in) {
        type = in.readInt();
        text = in.readString();
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeString(text);
    }

    public static final Parcelable.Creator<TextSection> CREATOR = new Parcelable.Creator<TextSection>() {
        public TextSection createFromParcel(Parcel in) {
            return new TextSection(in);
        }

        public TextSection[] newArray(int size) {
            return new TextSection[size];
        }
    };
}
