package com.randomname.mrakopedia.models.api.allpages;

import com.google.gson.annotations.SerializedName;

/**
 * Created by vgrigoryev on 27.01.2016.
 */
public class Continue {

    @SerializedName("continue")
    private String continueString;

    private String apcontinue;

    public String getContinueString ()
    {
        return continueString;
    }

    public void setContinueString (String continueString)
    {
        this.continueString = continueString;
    }

    public String getApcontinue ()
    {
        return apcontinue;
    }

    public void setApcontinue (String apcontinue)
    {
        this.apcontinue = apcontinue;
    }

    @Override
    public String toString()
    {
        return "Class Continue [continueString = "+continueString+", apcontinue = "+apcontinue+"]";
    }
}
