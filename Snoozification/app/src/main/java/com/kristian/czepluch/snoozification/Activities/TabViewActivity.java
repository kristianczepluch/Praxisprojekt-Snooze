package com.kristian.czepluch.snoozification.Activities;


import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.tamir7.contacts.Contact;
import com.github.tamir7.contacts.Contacts;
import com.github.tamir7.contacts.Query;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kristian.czepluch.snoozification.Interfaces.AsyncUser;
import com.kristian.czepluch.snoozification.HelperClasses.CategoryAsyncTask;
import com.kristian.czepluch.snoozification.HelperClasses.FragmentHome;
import com.kristian.czepluch.snoozification.HelperClasses.FragmentRules;
import com.kristian.czepluch.snoozification.R;
import com.kristian.czepluch.snoozification.Services.SynchronisationJobService;
import com.kristian.czepluch.snoozification.HelperClasses.TinyDB;
import com.kristian.czepluch.snoozification.Adapters.ViewPagerAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class TabViewActivity extends AppCompatActivity implements AsyncUser {

    private TabLayout tabLayout;
    private ViewPagerAdapter adapter;
    private ViewPager viewPager;
    private Fragment homeFragment;
    private Fragment rulesFragment;
    private static final String TAG = "TabViewActivity";
    private ArrayList<String> allCategories;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_view);

        tabLayout = findViewById(R.id.tablayout_id);
        viewPager = findViewById(R.id.viewpager_id);
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        homeFragment = new FragmentHome();
        rulesFragment = new FragmentRules();
        adapter.addFragment(homeFragment, "");
        adapter.addFragment(rulesFragment, "");
        allCategories = new ArrayList<>(Arrays.asList("GAMES", "COMMUNICATION", "SOCIAL", "NEWS_AND_MAGAZINES", "PRODUCTIVITY", "BUSINESS", "HEALTH_AND_FITNESS", "FOOD_AND_DRINKS", "LIFESTYLE", "OTHERS"));
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        Objects.requireNonNull(tabLayout.getTabAt(0)).setIcon(R.drawable.home_icon);
        Objects.requireNonNull(tabLayout.getTabAt(1)).setIcon(R.drawable.better_rules_icon);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setElevation(0);
        getSupportActionBar().getThemedContext().setTheme(R.style.menu);
        restartJobService();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.contacts_sync:
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
                Log.e(TAG, myRef.toString());
                myRef.updateChildren(myContacts)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getApplicationContext(), "Konakte wurden synchronisiert", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(getApplicationContext(), "Etwas ist schief gelaufen..", Toast.LENGTH_SHORT).show();

                            }
                        });
                return true;

            case R.id.app_sync:
                TinyDB tinyDB = new TinyDB(this);
                ArrayList<String> allPackages = new ArrayList<>();
                for(String string: allCategories){
                    allPackages.addAll(tinyDB.getListString(string));
                }
                for(String pack: allPackages){
                    Log.e(TAG,"Package: " + pack);
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
                        CategoryAsyncTask categoryAsyncTask = new CategoryAsyncTask(this,getApplicationContext(),"ADD");
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

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private String formatPhoneNumber(String phoneNumber){
        char[] number_as_array = phoneNumber.toCharArray();
        if(number_as_array[0]=='0'){
            return phoneNumber = "+49" + phoneNumber.substring(1);
        } else return phoneNumber;
    }

    @Override
    public boolean onFinish() {
        Toast.makeText(this, "Applications updated", Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public boolean onFinishJob(JobParameters params) {
        return false;
    }

    private void restartJobService(){
            TinyDB tinyDB = new TinyDB(getApplicationContext());
            boolean job_started = tinyDB.getBoolean("job_started");
            if(!job_started){
                ComponentName componentName = new ComponentName(this, SynchronisationJobService.class);
                JobInfo jobInfo = new JobInfo.Builder(781298, componentName)
                        .setPersisted(true)
                        .setPeriodic(1000*60*15)
                        .build();
                JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
                int result = jobScheduler.schedule(jobInfo);
                if(result==JobScheduler.RESULT_SUCCESS){
                    Log.e(TAG, "Job successfully scheduled");
                    tinyDB.putBoolean("job_started", true);
                }
                else{
                    Log.e(TAG, "Scheduling Job failed");
                    tinyDB.putBoolean("job_started", false);
                }
            }
     }
}
