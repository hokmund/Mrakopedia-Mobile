package com.randomname.mrakopedia.models.api.pagesummary;

import java.util.ArrayList;

/**
 * Created by Vlad on 20.01.2016.
 */
public class Parse {
    private String[] externallinks;

    private String displaytitle;

    private Text text;

    private ArrayList<TextSection> textSections;

    private Templates[] templates;

    private Links[] links;

    private Properties[] properties;

    private String title;

    private Sections[] sections;

    private String[] images;

    private Categories[] categories;

    private Iwlinks[] iwlinks;

    private String revid;

    private String[] langlinks;

    public String[] getExternallinks ()
    {
        return externallinks;
    }

    public void setExternallinks (String[] externallinks)
    {
        this.externallinks = externallinks;
    }

    public String getDisplaytitle ()
    {
        return displaytitle;
    }

    public void setDisplaytitle (String displaytitle)
    {
        this.displaytitle = displaytitle;
    }

    public Text getText ()
    {
        return text;
    }

    public void setText (Text text)
    {
        this.text = text;
    }

    public Templates[] getTemplates ()
    {
        return templates;
    }

    public void setTemplates (Templates[] templates)
    {
        this.templates = templates;
    }

    public ArrayList<TextSection> getTextSections() {
        if (textSections == null) {
            textSections = new ArrayList<>();
        }

        return textSections;
    }

    public void setTextSections(ArrayList<TextSection> textSections) {
        this.textSections = textSections;
    }

    public Links[] getLinks ()
    {
        return links;
    }

    public void setLinks (Links[] links)
    {
        this.links = links;
    }

    public Properties[] getProperties ()
    {
        return properties;
    }

    public void setProperties (Properties[] properties)
    {
        this.properties = properties;
    }

    public String getTitle ()
    {
        return title;
    }

    public void setTitle (String title)
    {
        this.title = title;
    }

    public Sections[] getSections ()
    {
        return sections;
    }

    public void setSections (Sections[] sections)
    {
        this.sections = sections;
    }

    public String[] getImages ()
    {
        return images;
    }

    public void setImages (String[] images)
    {
        this.images = images;
    }

    public Categories[] getCategories ()
    {
        return categories;
    }

    public void setCategories (Categories[] categories)
    {
        this.categories = categories;
    }

    public Iwlinks[] getIwlinks ()
    {
        return iwlinks;
    }

    public void setIwlinks (Iwlinks[] iwlinks)
    {
        this.iwlinks = iwlinks;
    }

    public String getRevid ()
    {
        return revid;
    }

    public void setRevid (String revid)
    {
        this.revid = revid;
    }

    public String[] getLanglinks ()
    {
        return langlinks;
    }

    public void setLanglinks (String[] langlinks)
    {
        this.langlinks = langlinks;
    }

    @Override
    public String toString()
    {
        return "Class Parse [externallinks = "+externallinks+", displaytitle = "+displaytitle+", text = "+text+", links = "+links+", properties = "+properties+", title = "+title+", sections = "+sections+", images = "+images+", categories = "+categories+", iwlinks = "+iwlinks+", revid = "+revid+", langlinks = "+langlinks+"]";
    }
}
