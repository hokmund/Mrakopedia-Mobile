package com.randomname.mrakopedia.models.api.categorydescription;

import com.randomname.mrakopedia.models.api.pagesummary.TextSection;

import java.util.ArrayList;

/**
 * Created by Vlad on 24.01.2016.
 */
public class CategoryDescription {
    private Parse parse;

    private ArrayList<TextSection> textSections;

    public ArrayList<TextSection> getTextSections() {
        if (textSections == null) {
            textSections = new ArrayList<>();
        }

        return textSections;
    }

    public void setTextSections(ArrayList<TextSection> textSections) {
        this.textSections = textSections;
    }

    public Parse getParse ()
    {
        return parse;
    }

    public void setParse (Parse parse)
    {
        this.parse = parse;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [parse = "+parse+"]";
    }
}
