package com.kristian.czepluch.snoozification.Services;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.github.tamir7.contacts.Contact;
import com.github.tamir7.contacts.Contacts;
import com.github.tamir7.contacts.Query;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kristian.czepluch.snoozification.HelperClasses.CategoryAsyncTask;
import com.kristian.czepluch.snoozification.Interfaces.AsyncUser;
import com.kristian.czepluch.snoozification.HelperClasses.TinyDB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SynchronisationJobService extends JobService implements AsyncUser {

    private static final String TAG = "SynchronisationJobService";
    private JobParameters jobParams;
    private ArrayList<String> allCategories = new ArrayList<>(Arrays.asList("GAMES", "COMMUNICATION", "SOCIAL", "NEWS_AND_MAGAZINES", "PRODUCTIVITY", "BUSINESS", "HEALTH_AND_FITNESS", "FOOD_AND_DRINKS", "LIFESTYLE", "OTHERS"));

    @Override
    public boolean onStartJob(JobParameters params) {
        jobParams = jobParams;
        Log.d(TAG, "onStartJob has been called.");
        sync_contacts();
        sync_apps(params);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    @Override
    public boolean onFinish() {
        return true;
    }

    @Override
    public boolean onFinishJob(JobParameters params) {
        jobFinished(params, false);
        return true;
    }

    public void sync_contacts(){
        Query q = Contacts.getQuery();
        q.hasPhoneNumber();
        List<Contact> contacts = q.find();

        Map<String,Object> myContacts = new HashMap<>();
        for(Contact contact : contacts){
            String name = contact.getDisplayName();
            String phonenumber = formatPhoneNumber(contact.getPhoneNumbers().get(0).getNumber().trim());
            myContacts.put(phonenumber,name);
        }

        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference().child("uuids").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        myRef.updateChildren(myContacts)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.e(TAG, "Contacts have been synced.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,"Synchronizing contacts failed");
                    }
                });
    }

    private String formatPhoneNumber(String phoneNumber){
        char[] number_as_array = phoneNumber.toCharArray();
        if(number_as_array[0]=='0'){
            return phoneNumber = "+49" + phoneNumber.substring(1);
        } else return phoneNumber;
    }

    private void sync_apps(JobParameters mParams){
        TinyDB tinyDB = new TinyDB(this);
        ArrayList<String> allPackages = new ArrayList<>();
        for(String string: allCategories){
            allPackages.addAll(tinyDB.getListString(string));
        }

        PackageManager pm = getPackageManager();
        ArrayList<String> allpkgs = new ArrayList<>();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo packageInfo : packages) {
            if(pm.getLaunchIntentForPackage(packageInfo.packageName) != null) {
                allpkgs.add(packageInfo.packageName);
            }
        }
        Collections.sort(allPackages);
        Collections.sort(allpkgs);
        if(allpkgs.equals(allPackages)){
            Log.e(TAG, "No new Applications added since last synchronisation.");
            jobFinished(mParams,false);
        } else {
            Log.e(TAG, "Something changed!");
            ArrayList<String> tmp = new ArrayList<>(allpkgs);

            // Zum neu hinzufügen
            allpkgs.removeAll(allPackages);
            for(String pkg: allpkgs){
                Log.e(TAG, "To Add: " + pkg);
            }

            Log.e(TAG, "Size of games to add: " + allpkgs.size());

            // Removed Packages
            allPackages.removeAll(tmp);

            Log.e(TAG, "Size of games to remove: " + allPackages.size());


            if(!allpkgs.isEmpty()){
                CategoryAsyncTask categoryAsyncTask = new CategoryAsyncTask(this,getApplicationContext(),"ADD", mParams);
                categoryAsyncTask.execute(allpkgs);
            }

            if(!allPackages.isEmpty()){
                ArrayList<String> tmp1 = new ArrayList<>();
                for(String pack: allPackages){
                    for(String category: allCategories){
                        tmp1 = tinyDB.getListString(category);
                        if(tmp1.contains(pack)){
                            tmp1.remove(pack);
                            Log.e(TAG, "Removed: " + pack + " from category: " + category);
                            tinyDB.putListString(category,tmp1);
                        }
                    }

                }
            }
        }
    }
}


