package com.randomname.mrakopedia.models.api.search;

/**
 * Created by vgrigoryev on 08.02.2016.
 */
public class Searchinfo {
    private String totalhits;

    public String getTotalhits ()
    {
        return totalhits;
    }

    public void setTotalhits (String totalhits)
    {
        this.totalhits = totalhits;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [totalhits = "+totalhits+"]";
    }
}
