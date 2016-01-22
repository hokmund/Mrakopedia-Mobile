package com.randomname.mrakopedia.models.api.pagesummary;

/**
 * Created by vgrigoryev on 22.01.2016.
 */
public class TextSection {

    public static final int UNDEFINED_TYPE = 0;
    public static final int TEXT_TYPE = 1;
    public static final int IMAGE_TYPE = 2;

    private int type;
    private String text;

    public TextSection(int type, String text) {
        this.type = type;
        this.text = text;
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
}
