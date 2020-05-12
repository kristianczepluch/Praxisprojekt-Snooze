package com.kristian.czepluch.snoozification.Datastructures;

public class User implements Cloneable {
    private String name;
    private String phoneNumber;
    private String rules;
    private String uuid;

    public User(){

    }

    public User(String name, String phoneNumber, String rules)  {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.rules = rules;
    }

    public User(User u){
        this.name = u.name;
        this.phoneNumber = u.phoneNumber;
        this.rules = u.rules;
        this.uuid = u.uuid;

    }


    public User(String name, String phoneNumber, String rules, String uuid) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.rules = rules;
        this.uuid = uuid;
    }

    public User(String uuid, String rules){
        this.uuid = uuid;
        this.rules = rules;
    }

    public String getRules() {
        return rules;
    }

    public void setRules(String rules) {
        this.rules = rules;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }


    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    protected User clone() {
        User clone = null;
        try
        {
            clone = (User) super.clone();

        }
        catch (CloneNotSupportedException e)
        {
            throw new RuntimeException(e);
        }
        return clone;
    }

}

