package com.kristian.czepluch.snoozification.Datastructures;

import android.util.Log;

import java.util.ArrayList;

public class UserListeModel {

    private final String TAG = "UserListeModel";
    private ArrayList<User> connectedUsers;
    private ArrayList<UserListObserver> myObservers;
    private static UserListeModel INSTANCE = null;
    private ArrayList<String> allUUIDs;


    private UserListeModel(){
        connectedUsers = new ArrayList<>();
        myObservers = new ArrayList<>();
        allUUIDs = new ArrayList<>();
    }

    public static UserListeModel getInstance(){
        if(INSTANCE == null){
            INSTANCE = new UserListeModel();
        }
        return INSTANCE;
    }

    public void addUser(User user){
        if(!allUUIDs.contains(user.getUuid())) {
            connectedUsers.add(user);
            allUUIDs.add(user.getUuid());
            notifyObservers();
        }
        Log.e(TAG, "Aktuelle Liste enthält: " + connectedUsers.size() + " User");
    }

    public void removeUser(String uuid){
        if(allUUIDs.contains(uuid)){
            for(User user:  connectedUsers){
                if(uuid.equals(user.getUuid())){
                    connectedUsers.remove(user);
                }
            }
            connectedUsers.trimToSize();
            allUUIDs.remove(uuid);
            notifyObservers();
        }
    }

    public void clearUser(){
        connectedUsers.clear();
        allUUIDs.clear();
        notifyObservers();
    }

    public  void registerObserver(UserListObserver observer){
        if(!myObservers.contains(observer)) myObservers.add(observer);
    }

    public void removeObserver(UserListObserver observer){
        if(myObservers.contains(observer)) myObservers.remove(observer);
    }

    public void notifyObservers(){
        for(UserListObserver observer: myObservers){
            observer.onUserDataChanged(connectedUsers);
        }
    }

    public ArrayList<User> getCurrentList(){
        return connectedUsers;
    }


    public interface UserListObserver {
        void onUserDataChanged(ArrayList<User> users);
    }
}


