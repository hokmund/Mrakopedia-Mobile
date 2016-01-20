package com.randomname.mrakopedia.models.api.allcategories;

/**
 * Created by vgrigoryev on 20.01.2016.
 */
public class Continue
{

    private String accontinue;

    public String getAccontinue ()
    {
        return accontinue;
    }

    public void setAccontinue (String accontinue)
    {
        this.accontinue = accontinue;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [accontinue = "+accontinue+"]";
    }
}

