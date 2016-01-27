package com.randomname.mrakopedia.models.api.recentchanges;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Vlad on 27.01.2016.
 */
public class Continue {
    private String rccontinue;

    @SerializedName("continue")
    private String mContinue;

    public String getRccontinue ()
    {
        return rccontinue;
    }

    public void setRccontinue (String rccontinue)
    {
        this.rccontinue = rccontinue;
    }

    public String getmContinue ()
    {
        return mContinue;
    }

    public void setmContinue (String mContinue)
    {
        this.mContinue = mContinue;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [rccontinue = "+rccontinue+", continue = "+ mContinue +"]";
    }
}
