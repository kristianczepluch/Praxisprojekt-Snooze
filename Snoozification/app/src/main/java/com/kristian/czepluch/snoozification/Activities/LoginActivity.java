package com.kristian.czepluch.snoozification.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kristian.czepluch.snoozification.Datastructures.Database;
import com.kristian.czepluch.snoozification.R;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    // Code to check permissions
    private final int INTERNET_CODE = 32112;
    private FirebaseAuth mAuth;
    private EditText phone_editText;
    private Button signin_Button;
    private Activity myActivity = this;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private Database myDatabase;
    private ProgressBar mProgressbar;
    private EditText verificationInput;
    private Button verficationBtn;
    private String verificationToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("uuids");

        phone_editText = findViewById(R.id.phoneNumber_editText);
        signin_Button = findViewById(R.id.signIn_btn);
        mProgressbar = findViewById(R.id.login_progressBar);
        verficationBtn = findViewById(R.id.verification_btn);
        verificationInput = findViewById(R.id.verification_textInput);
        verificationInput.setVisibility(View.INVISIBLE);
        verficationBtn.setVisibility(View.INVISIBLE);

        verficationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationToken, verificationInput.getText().toString().trim());
                signInWithPhoneAuthCredential(credential);
            }
        });
        mProgressbar.setVisibility(View.INVISIBLE);
        signin_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                if(!isNetworkAvailable()){
                    Toast.makeText(myActivity, "Keine Internetverbindung vorhanden", Toast.LENGTH_SHORT).show();
                    return;
                }
                //mProgressbar.setVisibility(View.INVISIBLE);
                signin_Button.setVisibility(View.INVISIBLE);
                verficationBtn.setVisibility(View.VISIBLE);
                verificationInput.setVisibility(View.VISIBLE);
                String phoneInput = phone_editText.getText().toString().trim();
                String pre = "+49";
                String phoneNumber = phoneInput;

                if(!phoneNumber.isEmpty()){
                    PhoneAuthProvider.getInstance()
                            .verifyPhoneNumber(phoneNumber, 120, TimeUnit.SECONDS, myActivity,
                                    new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        @Override
                        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                            signInWithPhoneAuthCredential(phoneAuthCredential);
                        }

                        @Override
                        public void onVerificationFailed(FirebaseException e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                            verificationToken = s;
                            super.onCodeSent(s, forceResendingToken);
                            verficationBtn.setVisibility(View.VISIBLE);
                            verificationInput.setVisibility(View.VISIBLE);
                            mProgressbar.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onCodeAutoRetrievalTimeOut(String s) {
                            Toast.makeText(myActivity, "Something went wrong..", Toast.LENGTH_SHORT).show();
                            mProgressbar.setVisibility(View.INVISIBLE);
                            signin_Button.setVisibility(View.VISIBLE);
                        }
                    });
                } else {
                    mProgressbar.setVisibility(View.INVISIBLE);
                    signin_Button.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(), "Sie haben keine Telefonnummer angegeben", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // First check for Permissions
        if(!checkforPermissions()){
            requestPermissions();
        }

        myDatabase = new Database(getApplicationContext());
        String firsttime = myDatabase.getRulesFromInternalStorage("firsttime");
        if(firsttime.equals("")){
            myDatabase.storeRulesOnInternalStorage("rules", "0000000000");
            myDatabase.storeRulesOnInternalStorage("firsttime", "true");
        }

        String loggedIn = myDatabase.getRulesFromInternalStorage("loggedIn");
        if(loggedIn.equals("true")){
            Intent intent = new Intent(this,TabViewActivity.class);
            startActivity(intent);
        }

    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                                checkIfUserExists();
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(LoginActivity.class.getName(), "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(myActivity, "Verfication Code was wrong!", Toast.LENGTH_SHORT).show();
                                verficationBtn.setVisibility(View.INVISIBLE);
                                mProgressbar.setVisibility(View.INVISIBLE);
                                signin_Button.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
    }

    public void checkIfUserExists(){
        // Check if the user already has a BeaconID, if not create a new one
        DatabaseReference myRef = database.getReference("uuids").child(mAuth.getUid());
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // If the user is new, he will go to the setup screen
                Object uuid = dataSnapshot.child("uuid").getValue();
                if(uuid == null ) {
                  Intent intent = new Intent(getApplicationContext(), SetupActivity.class);
                  intent.putExtra("action","reg");
                  startActivity(intent);
                } else{
                    // if the user is known, it will be checked if the app is new installed
                    String firstInstall = myDatabase.getRulesFromInternalStorage("firstInstall");
                    if(firstInstall.equals("no")){
                        Intent intent = new Intent(getApplicationContext(),TabViewActivity.class);
                        startActivity(intent);
                    }else{
                        Intent intent = new Intent(getApplicationContext(), SetupActivity.class);
                        intent.putExtra("action","sync");
                        startActivity(intent);
                        myDatabase.storeRulesOnInternalStorage("firstInstall", "no");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }


        });
    }

    private boolean checkforPermissions(){
        boolean result = true;
        if(!(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED)){ result = false; }
        //if(!(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED)){ result = false; }
        if(!(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)){ result = false; }
        if(!(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)){ result = false; }
        if(!(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED)){ result = false; }
        if(!(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)){ result = false; }
        if(!(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED)){ result = false; }
        return result;
    }

    private void requestPermissions(){
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION
                ,Manifest.permission.ACCESS_NETWORK_STATE,Manifest.permission.READ_CONTACTS,Manifest.permission.WRITE_CONTACTS}, INTERNET_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions, int[] grantResults) {
        if(requestCode == INTERNET_CODE){

            if((grantResults.length > 0)){
                for(int i: grantResults){
                    if(!(i == PackageManager.PERMISSION_GRANTED)){
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(1);
                    }
                }
            }
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
