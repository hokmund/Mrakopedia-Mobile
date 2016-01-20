package com.randomname.mrakopedia.models.api.categorymembers;

/**
 * Created by Vlad on 18.01.2016.
 */
public class Continue
{
    private String cmcontinue;

    public String getCmcontinue ()
    {
        return cmcontinue;
    }

    public void setCmcontinue (String cmcontinue)
    {
        this.cmcontinue = cmcontinue;
    }

    @Override
    public String toString()
    {
        return "class Continue [\ncmcontinue = "+cmcontinue + "]";
    }
}
