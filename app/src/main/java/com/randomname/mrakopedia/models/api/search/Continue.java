package com.randomname.mrakopedia.models.api.search;

import com.google.gson.annotations.SerializedName;

/**
 * Created by vgrigoryev on 08.02.2016.
 */
public class Continue {

    @SerializedName("continue")
    private String continueString;

    private String sroffset;

    public void setContinueString(String continueString) {
        this.continueString = continueString;
    }

    public String getContinueString() {
        return continueString;
    }

    public String getSroffset ()
    {
        return sroffset;
    }

    public void setSroffset (String sroffset)
    {
        this.sroffset = sroffset;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [continue = "+continueString+", sroffset = "+sroffset+"]";
    }
}
