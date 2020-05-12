package com.kristian.czepluch.snoozification;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.github.tamir7.contacts.Contacts;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kristian.czepluch.snoozification.Activities.TabViewActivity;
import com.kristian.czepluch.snoozification.Datastructures.NotificationCreator;
import com.kristian.czepluch.snoozification.Datastructures.User;
import com.kristian.czepluch.snoozification.Datastructures.UserListeModel;
import com.kristian.czepluch.snoozification.HelperClasses.TinyDB;
import com.kristian.czepluch.snoozification.Services.BeaconTransmissionService;
import com.kristian.czepluch.snoozification.Services.NotificationControlService;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import java.util.ArrayList;
import java.util.Collection;

public class BeaconConsumerApplication extends Application implements BeaconConsumer {

    public final static int notificationID1 = 456;
    private static BeaconManager mbeaconManager;
    private UserListeModel myUserListeModel;
    private int counter = 0;
    private ArrayList<String> uuidList;
    private final String TAG = this.getClass().getName();
    public static final String CHANNEL_1_ID = "Geräte suchen";
    private ArrayList<String> lastScan;
    private ArrayList<String> add_users;
    private TinyDB tinyDB;
    private NotificationCreator creator;
    private String notificationTextFound = "Benachrichtigungen werden manipuliert!";
    private String notificationTextNotFound = "Alle Benachrichtigungen werden zugelassen";


    @Override
    public void onCreate() {
        super.onCreate();
        Contacts.initialize(this);
        createNotificationChannel();
        tinyDB = new TinyDB(this);
        boolean app_running = tinyDB.getBoolean("app_running");

        // Create Notification for the Foreground Service
        Intent intent = new Intent(this, TabViewActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        );

        if(app_running){
            startBeaconSearch();
        }

    }

    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel1 = new NotificationChannel(CHANNEL_1_ID, "Geräte suchen",
                    NotificationManager.IMPORTANCE_LOW);
            channel1.setDescription("Das ist der Channel kommuniziert die Suche nach anderen Geräten im Hintergrund!");
            channel1.setVibrationPattern(null);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
        }
    }

    /* Description onBeaconServiceConnect: This function get called second, when the service to search for beacon was startet and a scan performed.
       This function collects the scan results from 10 Scans and then compares the results first with the previous scan to obtain items that are new
       or have to be removed. New items will be removed imediatly, while for new items request are made to the firebase realtime database and are then added
       via the UserListModel that hols the current data and notifys others bound components like the NotificationControlService or the HomeFragment.
     */


    @Override
    public void onBeaconServiceConnect() {
        mbeaconManager.removeAllRangeNotifiers();
        mbeaconManager.addRangeNotifier(new RangeNotifier() {

        @Override
        public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        if(counter==5){
            if (uuidList.size() > 0) {
                // get elements which have been removed/added
                ArrayList<String> copy_lastScan;
                ArrayList<String> copy_newScan;
                copy_lastScan = (ArrayList<String>)lastScan.clone();
                copy_newScan = (ArrayList<String>) uuidList.clone();

                // remove list
                copy_lastScan.removeAll(uuidList);
                for(String uuid: copy_lastScan){
                    myUserListeModel.removeUser(uuid);
                }
                //add
                uuidList.removeAll(lastScan);
                add_users = (ArrayList<String>) uuidList.clone();

                if(add_users.size()>0) updateNotification(notificationTextFound);

                // request data and send it to the UserListModel
                for (String user: (ArrayList<String>)add_users.clone()) {
                    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(user);
                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            user.setUuid(dataSnapshot.getKey());
                            myUserListeModel.addUser(user);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e(TAG, "Firebase Realtime Database Request Error: " + databaseError.toString());
                        }
                    });
                }
                lastScan.clear();
                lastScan = (ArrayList<String>) copy_newScan.clone();
                uuidList.clear();
                counter=0;
            } else{
                lastScan.clear();
                uuidList.clear();
                myUserListeModel.clearUser();
                updateNotification(notificationTextNotFound);
                counter=0;
            }
        } else{
                for (Beacon item : beacons) {
                    if(item.getDistance() < 3) {
                        if(!uuidList.contains(item.getId1().toString())){
                            uuidList.add(item.getId1().toString());
                        }
                    }
                }}
            counter++;
    }



});
        try {
        mbeaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
            Log.e(TAG, "startRangingBeacons failed: " + e.getMessage());
        }
    }
    @Override
    public void onTerminate() {
        getApplicationContext().stopService(new Intent(getApplicationContext(), NotificationControlService.class));
        myUserListeModel.clearUser();
        super.onTerminate();
    }

    public void stopBeaconSearch(){
        mbeaconManager.unbind(this);

        Intent aintent = new Intent("snooze-listener");
        LocalBroadcastManager.getInstance(this).sendBroadcast(aintent);

        Intent nIntent = new Intent(this, NotificationControlService.class);
        this.stopService(nIntent);

        Intent intent = new Intent(this, BeaconTransmissionService.class);
        this.stopService(intent);

        myUserListeModel.clearUser();

        Log.e(TAG, "Beacon search stopped..");
    }

    public void startBeaconSearch(){
        // set up lists
        myUserListeModel =  UserListeModel.getInstance();
        uuidList = new ArrayList<>();
        lastScan  = new ArrayList<>();

        // Set Scanning Settings
        creator = NotificationCreator.getInstance(this);
        mbeaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);
        mbeaconManager.setDebug(true);
        mbeaconManager.enableForegroundServiceScanning(creator.getNotification(), notificationID1);
        mbeaconManager.setEnableScheduledScanJobs(false);
        mbeaconManager.setForegroundBetweenScanPeriod(110);
        mbeaconManager.bind(this);

        Intent intent = new Intent(this,BeaconTransmissionService.class);
        this.startForegroundService(intent);

        Intent nIntent = new Intent(this, NotificationControlService.class);
        this.startService(nIntent);

        myUserListeModel.clearUser();

        Log.e(TAG, "Beacon search started..");
    }

    public void updateNotification(String text){
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        Intent intent = new Intent(this, TabViewActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        );

        Notification newNotification = new NotificationCompat.Builder(this, BeaconConsumerApplication.CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_notifications_paused_black_24dp)
                .setContentTitle(text)
                .setContentIntent(pendingIntent)
                .build();

        creator.updateNotification(newNotification);
        notificationManager.notify(notificationID1, newNotification);
    }
}

