package com.kristian.czepluch.snoozification.Services;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.kristian.czepluch.snoozification.Datastructures.NotificationCreator;
import com.kristian.czepluch.snoozification.HelperClasses.TinyDB;
import com.kristian.czepluch.snoozification.Datastructures.User;
import com.kristian.czepluch.snoozification.Datastructures.UserListeModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class NotificationControlService extends NotificationListenerService implements UserListeModel.UserListObserver {

    private ArrayList<Integer> all_rules;
    private List<String> myFilter = new ArrayList<String>();
    private ArrayList<String> allCategories = new ArrayList<>();
    private UserListeModel myUserListeModel;
    private static final String TAG = "NotificationControlService";
    private String rules_as_string;
    private TinyDB db;
    private BroadcastReceiver broadcastReceiver;
    private String mPackageName;
    private String mClassName;
    private ComponentName mComponentName;

    //@Override
    //public int onStartCommand(Intent intent, int flags, int startId) {
    //    return START_STICKY;
    //}

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "NotificationListenerService started...");
        allCategories = new ArrayList<>(Arrays.asList("GAMES", "COMMUNICATION", "SOCIAL", "NEWS_AND_MAGAZINES", "PRODUCTIVITY", "BUSINESS", "HEALTH_AND_FITNESS", "FOOD_AND_DRINKS", "LIFESTYLE", "OTHERS"));
        all_rules = new ArrayList<>();
        rules_as_string = "0000000000";
        db = new TinyDB(getApplicationContext());
        NotificationCreator creator = NotificationCreator.getInstance(this);
        mPackageName = this.getPackageName();
        mClassName = getClass().getName();
        mComponentName = new ComponentName(mPackageName,mClassName);
        requestRebind(mComponentName);

        // listener that handles snooze calls
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.e(TAG,"Broadcast received");
                    requestUnbind();
                LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(this);
            }

        };
        //startForeground(459, creator.getNotification());

    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        for (int i = 0; i < myFilter.size(); ++i) {
            String name = myFilter.get(i);
            if (sbn.getPackageName().contains(name)) {
                snoozeNotification(sbn.getKey(), 1000 * 5);
            }
        }
    }

    @Override
    public void onListenerConnected() {
        myUserListeModel = UserListeModel.getInstance();
        myUserListeModel.registerObserver(this);

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter("snooze-listener"));

    }

    private void update_rules(ArrayList<User> users) {
        all_rules.clear();
        for (User user : users) {
            all_rules.add(Integer.parseInt(user.getRules(), 2));
        }
        int finaleRegeln = 0;
        for (int i : all_rules) {
            finaleRegeln = finaleRegeln | i;
        }
        rules_as_string = String.format(Locale.GERMAN, "%010d", Integer.parseInt(Integer.toBinaryString(finaleRegeln)));
        char[] rules_as_char_array = rules_as_string.toCharArray();
        for (int b = 0; b < rules_as_char_array.length; ++b) {
            if (rules_as_char_array[b] == '1') {
                for (String s : db.getListString(allCategories.get(b))) {
                    myFilter.add(s);
                }
            }
        }
    }


    @Override
    public void onDestroy() {
        Log.e(TAG,"Service was destroyed");
        if(myUserListeModel!=null) myUserListeModel.removeObserver(this);
    }

    @Override
    public void onListenerDisconnected() {
        Log.e(TAG, "NotificationControlService unbound");
        super.onListenerDisconnected();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(broadcastReceiver);
    }


    @Override
    public void onUserDataChanged(ArrayList<User> users) {
        if (!users.isEmpty()) {
            update_rules(users);
        } else {
            rules_as_string = "0000000000";
            myFilter.clear();
        }
    }

    private String getCategoryByPackageName(String packageName) {
        TinyDB tinyDB = new TinyDB(this);
        for (String category : allCategories) {
            ArrayList<String> allPackages = tinyDB.getListString(category);
            if (allPackages.contains(packageName)) {
                return category;
            }
        }
        return "No Category Found";
    }
}