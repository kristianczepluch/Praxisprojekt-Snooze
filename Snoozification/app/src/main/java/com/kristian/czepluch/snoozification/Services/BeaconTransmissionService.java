package com.kristian.czepluch.snoozification.Services;

import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.kristian.czepluch.snoozification.BeaconConsumerApplication;
import com.kristian.czepluch.snoozification.Datastructures.Database;
import com.kristian.czepluch.snoozification.Datastructures.NotificationCreator;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;

import java.util.Arrays;

public class BeaconTransmissionService extends Service {

    private BeaconTransmitter beaconTransmitter;
    public final String TAG = this.getClass().getName();

    public BeaconTransmissionService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.e(TAG,"BeaconTransmission started..");
        super.onCreate();
        Beacon myBeacon = createBeacon();
        BeaconParser beaconParser = new BeaconParser()
                .setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25");
        beaconTransmitter = new BeaconTransmitter(getApplicationContext(), beaconParser);
        beaconTransmitter.startAdvertising(myBeacon, new AdvertiseCallback() {

            @Override
            public void onStartFailure(int errorCode) {
                Log.e(TAG, "Advertisement start failed with code: "+errorCode);
            }

            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                Log.i(TAG, "Advertisement start succeeded.");
            }
        });

        NotificationCreator creator = NotificationCreator.getInstance(this);
        startForeground(BeaconConsumerApplication.notificationID1, creator.getNotification());
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Beacon createBeacon(){
        Database db = new Database(getApplicationContext());
        String uuid = db.getRulesFromInternalStorage("uuid");

        Beacon beacon = new Beacon.Builder()
                .setId1(uuid)
                .setId2("1")
                .setId3("2")
                .setManufacturer(0x0118)
                .setTxPower(-59)
                .setDataFields(Arrays.asList(new Long[] {0l}))
                .build();
        return beacon;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        beaconTransmitter.stopAdvertising();
        Log.e(TAG, "Beacon Advertising stopped");
    }
}
