package com.kristian.czepluch.snoozification.Activities;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.tamir7.contacts.Contact;
import com.github.tamir7.contacts.Contacts;
import com.github.tamir7.contacts.Query;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kristian.czepluch.snoozification.Datastructures.Anwendung;
import com.kristian.czepluch.snoozification.Interfaces.AsyncUser;
import com.kristian.czepluch.snoozification.HelperClasses.CategoryAsyncTask;
import com.kristian.czepluch.snoozification.Datastructures.Database;
import com.kristian.czepluch.snoozification.R;
import com.kristian.czepluch.snoozification.Services.SynchronisationJobService;
import com.kristian.czepluch.snoozification.HelperClasses.TinyDB;
import com.kristian.czepluch.snoozification.Datastructures.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class SetupActivity extends AppCompatActivity implements AsyncUser {

    private TextView regTextView;
    private TextView syncTextView;
    private Button finish_btn;
    private ProgressBar progressBar;
    private ProgressBar progressBar_finish;
    private Database db;
    private TextView bartext;
    private EditText userName;
    private ImageView profiePic;
    private View view;
    private PackageManager pm;
    private ArrayList<String> allNames = new ArrayList<>();
    private ArrayList<Drawable> allLogos = new ArrayList<>();
    private static ArrayList<Anwendung> anwendungArrayList = new ArrayList<>();
    private static final String TAG = "SetupActivity";
    private FirebaseAuth mAuth;
    private String uuid;
    private String user;
    private String action;
    private FirebaseDatabase database;
    private TinyDB tinyDB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent myIntent = getIntent();
        action = myIntent.getStringExtra("action");
        mAuth = FirebaseAuth.getInstance();
        regTextView = findViewById(R.id.registrierungstext_view);
        progressBar = findViewById(R.id.setup_progressBar);
        bartext = findViewById(R.id.bar_textView);
        finish_btn = findViewById(R.id.finish_btn);
        userName = findViewById(R.id.benutzername_editText);
        progressBar_finish = findViewById(R.id.progressBar_finish);
        finish_btn.setVisibility(View.INVISIBLE);
        profiePic = findViewById(R.id.imageView1);
        db = new Database(getApplicationContext());
        tinyDB = new TinyDB(getApplicationContext());


        if(action.equals("reg")){
        }else if(action.equals("sync")){
            userName.setVisibility(View.INVISIBLE);
            profiePic.setVisibility(View.INVISIBLE);
            view.setVisibility(View.INVISIBLE);
            regTextView.setVisibility(View.INVISIBLE);

        }

        // read all apps from storage
        pm = getPackageManager();
        ArrayList<String> allpkgs = new ArrayList<>();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo packageInfo : packages) {
            if(pm.getLaunchIntentForPackage(packageInfo.packageName) != null) {
                allpkgs.add(packageInfo.packageName);
            }
        }

        CategoryAsyncTask categoryAsyncTask = new CategoryAsyncTask(this,getApplicationContext(),"INIT");
        categoryAsyncTask.execute(allpkgs);

        finish_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user = userName.getText().toString();
                progressBar_finish.setVisibility(View.VISIBLE);
                finish_btn.setVisibility(View.INVISIBLE);
                if(user.isEmpty() && action.equals("reg")){
                    progressBar.setVisibility(View.INVISIBLE);
                    finish_btn.setVisibility(View.VISIBLE);
                    Toast.makeText(SetupActivity.this, "Bitte gib erst einen Benutzernamen an", Toast.LENGTH_SHORT).show();
                }else if(action.equals("reg")){
                    // Create User In Realtime Database
                    database = FirebaseDatabase.getInstance();
                    uuid = UUID.randomUUID().toString();
                    DatabaseReference myRef = database.getReference("uuids");
                    myRef.child(Objects.requireNonNull(mAuth.getUid())).child("uuid").setValue(uuid)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Create Settings for first Time users

                                    DatabaseReference myProfileRef = database.getReference("users");
                                    User userObj = new User(user, Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber(),"000000000");
                                    myProfileRef.child(uuid).setValue(userObj)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(Task<Void> task) {

                                                    Query q = Contacts.getQuery();
                                                    q.hasPhoneNumber();
                                                    List<Contact> contacts = q.find();
                                                    Map<String,Object> myContacts = new HashMap<>();
                                                    for(Contact contact : contacts){
                                                        String name = contact.getDisplayName();
                                                        String phonenumber = formatPhoneNumber(contact.getPhoneNumbers().get(0).getNumber().trim());
                                                        myContacts.put(phonenumber,name);
                                                    }

                                                    DatabaseReference myRef = database.getReference().child("uuids").child(mAuth.getCurrentUser().getUid());
                                                    Log.e(TAG, myRef.toString());
                                                    myRef.updateChildren(myContacts)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {


                                                                    DatabaseReference myRef = database.getReference("phones").child(Objects.requireNonNull(mAuth.getCurrentUser().getPhoneNumber()));
                                                                    myRef.setValue(user)
                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void aVoid) {
                                                                                    Database db = new Database(getApplicationContext());
                                                                                    db.storeRulesOnInternalStorage("uuid", uuid);
                                                                                    db.storeRulesOnInternalStorage("loggedIn", "true");
                                                                                    db.storeRulesOnInternalStorage("username", user);
                                                                                    Toast.makeText(SetupActivity.this, "Willkommen", Toast.LENGTH_SHORT).show();
                                                                                    Intent intent = new Intent(getApplicationContext(),TabViewActivity.class);
                                                                                    startActivity(intent);
                                                                                }
                                                                            })
                                                                            .addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(Exception e) {
                                                                                    Toast.makeText(SetupActivity.this, "Etwas ist schief gelaufen..", Toast.LENGTH_SHORT).show();
                                                                                    progressBar_finish.setVisibility(View.INVISIBLE);
                                                                                    finish_btn.setVisibility(View.VISIBLE);
                                                                                }
                                                                            });
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(Exception e) {
                                                                    Toast.makeText(SetupActivity.this, "Etwas ist schief gelaufen..", Toast.LENGTH_SHORT).show();
                                                                    progressBar_finish.setVisibility(View.INVISIBLE);
                                                                    finish_btn.setVisibility(View.VISIBLE);
                                                                }
                                                            });

                                                }
                                            });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(Exception e) {
                                    Toast.makeText(SetupActivity.this, "Etwas ist schief gelaufen..", Toast.LENGTH_SHORT).show();
                                    progressBar_finish.setVisibility(View.INVISIBLE);
                                    finish_btn.setVisibility(View.VISIBLE);
                                }
                            });
                } else {
                    sync_finished();
                }

            }
        });
    }

    private String formatPhoneNumber(String phoneNumber){
        char[] number_as_array = phoneNumber.toCharArray();
        if(number_as_array[0]=='0'){
            return phoneNumber = "+49" + phoneNumber.substring(1);
        } else return phoneNumber;
    }

    public void sync_finished(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myProfileRef = database.getReference("uuids").child(Objects.requireNonNull(mAuth.getUid()));
        myProfileRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String myUuid = dataSnapshot.child("uuid").getValue().toString();
                db.storeRulesOnInternalStorage("uuid", myUuid);
                Log.e(TAG,"UUID: " + myUuid);

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myProfileRef = database.getReference("users").child(myUuid);
                myProfileRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String myName = dataSnapshot.child("name").getValue().toString();
                        db.storeRulesOnInternalStorage("username", myName);
                        Log.e(TAG,"Mein Name: " + myName);
                        db.storeRulesOnInternalStorage("loggedIn", "true");
                        startSyncJob();
                        Toast.makeText(SetupActivity.this, "Willkommen", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(),TabViewActivity.class);
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onFinish() {
        finish_btn.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        bartext.setVisibility(View.INVISIBLE);
        progressBar_finish.setVisibility(View.INVISIBLE);
        return true;
    }

    @Override
    public boolean onFinishJob(JobParameters params) {
        return false;
    }

    public void startSyncJob(){
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
