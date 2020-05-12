package com.kristian.czepluch.snoozification.Datastructures;

public class MyContact {

    private String benutzername;
    private String kontaktname;
    private int avatar;

    public MyContact(){

    }

    public MyContact(String benutzername, String kontaktname, int avatar) {
        this.benutzername = benutzername;
        this.kontaktname = kontaktname;
        this.avatar = avatar;
    }

    public String getBenutzername() {
        return benutzername;
    }

    public void setBenutzername(String benutzername) {
        this.benutzername = benutzername;
    }

    public String getKontaktname() {
        return kontaktname;
    }

    public void setKontaktname(String kontaktname) {
        this.kontaktname = kontaktname;
    }

    public int getAvatar() {
        return avatar;
    }

    public void setAvatar(int avatar) {
        this.avatar = avatar;
    }
}
