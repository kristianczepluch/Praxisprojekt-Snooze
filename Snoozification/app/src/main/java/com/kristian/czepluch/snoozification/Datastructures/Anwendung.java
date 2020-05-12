package com.kristian.czepluch.snoozification.Datastructures;

import android.graphics.drawable.Drawable;

public class Anwendung {

    private String packageName;
    private String category;
    private Drawable logo;

    public Anwendung(String packageName, String category,Drawable logo) {
        this.packageName = packageName;
        this.category = category;
        this.logo = logo;
    }

    public Anwendung(String packageName, Drawable logo) {
        this.packageName = packageName;
        this.logo = logo;
    }

    public Anwendung(String packageName, String category) {
        this.packageName = packageName;
        this.category = category;
    }

    public Anwendung(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Drawable getLogo() {
        return logo;
    }

    public void setLogo(Drawable logo) {
        this.logo = logo;
    }
}
