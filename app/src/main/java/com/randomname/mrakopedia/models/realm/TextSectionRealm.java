package com.randomname.mrakopedia.models.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by vgrigoryev on 22.01.2016.
 */
public class TextSectionRealm extends RealmObject {

    private int type;
    private String text;
    @PrimaryKey
    private String id;

    public TextSectionRealm () {
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
