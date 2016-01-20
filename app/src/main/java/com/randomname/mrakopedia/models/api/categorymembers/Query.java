package com.randomname.mrakopedia.models.api.categorymembers;

/**
 * Created by Vlad on 18.01.2016.
 */
public class Query
{
    private Categorymembers[] categorymembers;

    public Categorymembers[] getCategorymembers ()
    {
        return categorymembers;
    }

    public void setCategorymembers (Categorymembers[] categorymembers)
    {
        this.categorymembers = categorymembers;
    }

    @Override
    public String toString()
    {
        return "class Query [\ncategorymembers = "+categorymembers.toString()+"]";
    }
}
