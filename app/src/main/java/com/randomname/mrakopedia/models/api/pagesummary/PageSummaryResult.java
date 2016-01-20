package com.randomname.mrakopedia.models.api.pagesummary;

/**
 * Created by Vlad on 20.01.2016.
 */
public class PageSummaryResult {
    private Parse parse;

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
        return "Class PageSummary [parse = "+parse+"]";
    }
}
