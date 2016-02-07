package com.randomname.mrakopedia.models.realm;

import io.realm.RealmObject;

/**
 * Created by Vlad on 07.02.2016.
 */
public class RealmString extends RealmObject {

    private String string;

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }
}
