package com.randomname.mrakopedia.models.api.pagesummary;

/**
 * Created by Vlad on 20.01.2016.
 */
public class Sections
{
    private String index;

    private String level;

    private String byteoffset;

    private String line;

    private String number;

    private String fromtitle;

    private String anchor;

    private String toclevel;

    public String getIndex ()
    {
        return index;
    }

    public void setIndex (String index)
    {
        this.index = index;
    }

    public String getLevel ()
    {
        return level;
    }

    public void setLevel (String level)
    {
        this.level = level;
    }

    public String getByteoffset ()
    {
        return byteoffset;
    }

    public void setByteoffset (String byteoffset)
    {
        this.byteoffset = byteoffset;
    }

    public String getLine ()
    {
        return line;
    }

    public void setLine (String line)
    {
        this.line = line;
    }

    public String getNumber ()
    {
        return number;
    }

    public void setNumber (String number)
    {
        this.number = number;
    }

    public String getFromtitle ()
    {
        return fromtitle;
    }

    public void setFromtitle (String fromtitle)
    {
        this.fromtitle = fromtitle;
    }

    public String getAnchor ()
    {
        return anchor;
    }

    public void setAnchor (String anchor)
    {
        this.anchor = anchor;
    }

    public String getToclevel ()
    {
        return toclevel;
    }

    public void setToclevel (String toclevel)
    {
        this.toclevel = toclevel;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [index = "+index+", level = "+level+", byteoffset = "+byteoffset+", line = "+line+", number = "+number+", fromtitle = "+fromtitle+", anchor = "+anchor+", toclevel = "+toclevel+"]";
    }
}
