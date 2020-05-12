package com.kristian.czepluch.snoozification.Datastructures;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.kristian.czepluch.snoozification.Activities.TabViewActivity;
import com.kristian.czepluch.snoozification.BeaconConsumerApplication;
import com.kristian.czepluch.snoozification.R;

public class NotificationCreator {

    private static NotificationCreator INSTANCE = null;
    private Notification notification;
    private Context mContext;

    private NotificationCreator(Context mContext){
        Intent intent = new Intent(mContext, TabViewActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        );
        notification = new NotificationCompat.Builder(mContext, BeaconConsumerApplication.CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_notifications_paused_black_24dp)
                .setContentTitle("Alle Benachrichtigungen werden zugelassen")
                .setContentIntent(pendingIntent)
                .build();
        this.mContext = mContext;
    }

    public static NotificationCreator getInstance(Context context){
        if(INSTANCE == null){
            INSTANCE = new NotificationCreator(context);
        }

        return INSTANCE;
    }

    public void updateNotification(Notification notification){
        this.notification = notification;
    }

    public Notification getNotification(){
        return notification;
    }
}
