package com.randomname.mrakopedia.models.api.allpages;

/**
 * Created by vgrigoryev on 27.01.2016.
 */
public class Query {
    private Allpages[] allpages;

    public Allpages[] getAllpages ()
    {
        return allpages;
    }

    public void setAllpages (Allpages[] allpages)
    {
        this.allpages = allpages;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [allpages = "+allpages+"]";
    }
}
