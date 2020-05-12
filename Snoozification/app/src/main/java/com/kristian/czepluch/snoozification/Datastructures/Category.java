package com.kristian.czepluch.snoozification.Datastructures;

import android.graphics.drawable.Drawable;
import android.widget.Switch;

public class Category {

    private Drawable icon;
    private String name;
    private boolean mySwitch;

    public Category(Drawable icon, String name, boolean mySwitch) {
        this.icon = icon;
        this.name = name;
        this.mySwitch = mySwitch;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public boolean isMySwitch() {
        return mySwitch;
    }

    public void setMySwitch(boolean mySwitch) {
        this.mySwitch = mySwitch;
    }
}
