package com.kristian.czepluch.snoozification.Datastructures;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

public class Database {

    private Context mContext;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;

    private boolean found;


    public Database(Context mContext) {
        this.mContext = mContext;
        prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        editor = prefs.edit();
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
    }



    public void storeRulesOnInternalStorage(String key, String value){
        editor.putString(key,value);
        editor.apply();
    }

    public String getRulesFromInternalStorage(String key){
        return prefs.getString(key, "");
    }


    public void createNewUser(String name){
        String uuid = UUID.randomUUID().toString();
        DatabaseReference myRef = database.getReference("uuids");
        myRef.child(mAuth.getUid()).setValue(uuid);

        // Create Settings for first Time users
        DatabaseReference myProfileRef = database.getReference("users");
        User user = new User(name, mAuth.getCurrentUser().getPhoneNumber(),"000000000");
        myProfileRef.child(uuid).setValue(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.e("KCCK", "success");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("KCCK", "Error: " + e.getMessage());
                    }
                });
        storeRulesOnInternalStorage("uuid", uuid);

    }

    public void overrideRules(String uuid, String rules){
        DatabaseReference myRef = database.getReference("users").child(uuid);
        myRef.setValue(new User(getRulesFromInternalStorage("username"),mAuth.getCurrentUser().getPhoneNumber(),rules));

    }

    public void closeFirebase(){
        database.goOffline();
    }
}
