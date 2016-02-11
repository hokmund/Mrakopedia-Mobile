package com.randomname.mrakopedia.models.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by vgrigoryev on 11.02.2016.
 */
public class ColorScheme extends RealmObject{

    @PrimaryKey
    private int schemeId;
    private int backgroundColor;
    private int textColor;
    private int selectedColor;

    public ColorScheme() {
    }

    public ColorScheme(int schemeId, int backgroundColor, int textColor, int selectedColor) {
        this.schemeId = schemeId;
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
        this.selectedColor = selectedColor;
    }

    public ColorScheme(ColorScheme colorScheme) {
        this.schemeId = schemeId;
        this.backgroundColor = colorScheme.getBackgroundColor();
        this.textColor = colorScheme.getTextColor();
        this.selectedColor = colorScheme.getSelectedColor();
    }

    public void setSchemeId(int schemeId) {
        this.schemeId = schemeId;
    }

    public int getSchemeId() {
        return schemeId;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public int getSelectedColor() {
        return selectedColor;
    }

    public void setSelectedColor(int selectedColor) {
        this.selectedColor = selectedColor;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }
}
