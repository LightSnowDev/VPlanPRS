package com.lightSnow.VPlanPRS.gcm;

/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.lightSnow.VPlanPRS.MainActivity;
import com.lightSnow.VPlanPRS.PRS;
import com.lightSnow.VPlanPRS.R;
import com.lightSnow.VPlanPRS.classes.Vertretungsstunde;

import java.util.List;

public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.toString();
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);

        //Analyse des V-Plans...
        //Message überträgt Anzahl der Änderungen

        PRS prs = new PRS(this.getBaseContext());
        prs.addOnVPlanResultEvent(new PRS.OnVPlanResultEvent() {
            @Override
            public void VPlanResultEvent(List<Vertretungsstunde> alleStunden, boolean changedKlassenSpecificData, boolean success) {
                sendNotification(alleStunden.size());
            }
        });
        prs.downloadLinks();
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param count Anzahl der Anderungen des V-Plans
     */
    private void sendNotification(int count) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder bldr = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle("Neuer Vertretungsplan")
                .setContentText(String.valueOf(count) + " neue Ausfälle/Vertretungen")
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        Notification notify;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            notify = bldr.getNotification();
        } else {
            notify = bldr.build();
        }


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notify);
    }
}