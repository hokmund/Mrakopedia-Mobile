package com.randomname.mrakopedia.models.api.allcategories;

/**
 * Created by vgrigoryev on 20.01.2016.
 */
public class Query {
    private Allcategories[] allcategories;

    public Allcategories[] getAllcategories ()
    {
        return allcategories;
    }

    public void setAllcategories (Allcategories[] allcategories)
    {
        this.allcategories = allcategories;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [allcategories = "+allcategories+"]";
    }
}
