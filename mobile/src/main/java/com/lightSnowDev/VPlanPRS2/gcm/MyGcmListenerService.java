package com.lightSnowDev.VPlanPRS2.gcm;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.lightSnowDev.VPlanPRS2.MainActivity;
import com.lightSnowDev.VPlanPRS2.R;
import com.lightSnowDev.VPlanPRS2.classes.PRS;
import com.lightSnowDev.VPlanPRS2.classes.VertretungsStunde;
import com.lightSnowDev.VPlanPRS2.classes.VertretungsTag;
import com.lightSnowDev.VPlanPRS2.helper.StorageHelper;

import java.util.List;

public class MyGcmListenerService extends FirebaseMessagingService {

    private static final String TAG = "MyGcmListenerService";

    /**
     * Called when message is received.
     * NOTE: use 'data messages' from FCM
     *
     * @param remoteMessage Message from firebase-google-server
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        RemoteMessage.Notification note = remoteMessage.getNotification();
        //Log.d(TAG, "got message from: " + remoteMessage.getFrom());
        //Breche ab, falls der User keine Nachrichten will
        StorageHelper.saveToSharedPreferences("test", "0", this);
        if (!StorageHelper.loadBooleanFromSharedPreferences(StorageHelper.VPLAN_USER_AUTO_UPDATE, this)) {
            return;
        }
        StorageHelper.saveToSharedPreferences("test", "1", this);
        //Analyse des V-Plans...
        PRS prs = new PRS(this);
        if (StorageHelper.loadBooleanFromSharedPreferences(StorageHelper.VPLAN_USER_KLASSE_FILTER, this))
            prs.setKlasseToFilter(StorageHelper.loadStringFromSharedPreferences(StorageHelper.VPLAN_USER_KLASSE, this));
        prs.addOnPRSResultEvent(new PRS.OnPRSResultEvent() {
            @Override
            public void PRSResultEvent(final PRS prs, PRS.PRSResultType resultType) {
                StorageHelper.saveToSharedPreferences("test", "2a", prs.getContext());
                if (resultType == PRS.PRSResultType.success)
                    sendNotification(prs.getAllStundenOfDay(VertretungsTag.Day.beide).size());
                StorageHelper.saveToSharedPreferences("test", "2b", prs.getContext());
            }
        });
        prs.downloadPRS();
        StorageHelper.saveToSharedPreferences("test", "2", this);
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param count Anzahl der Änderungen des V-Plans
     */
    private void sendNotification(int count) {
        StorageHelper.saveToSharedPreferences("test", "3", this);
        //Erstelle ein neues Intent, da die App wahrscheinlich nicht mehr läuft
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        String message = "";
        if (count == 0)
            message = "keine Ausfälle/Vertretungen (heute & morgen)";
        else if (count == 1)
            message = "1 Ausfall/Vertretung (heute & morgen)";
        else
            message = String.valueOf(count) + " Ausfälle/Vertretungen (heute & morgen)";


        StorageHelper.saveToSharedPreferences("test", "4", this);
        NotificationCompat.Builder noBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.prs_white_transparent)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),
                        R.drawable.prs_white_transparent))
                .setContentTitle("Neuer Vertretungsplan")
                .setContentText(message)
                .setAutoCancel(true)
                .setColor(Color.argb(255, 119, 179, 28))
                .setContentIntent(pendingIntent);

        if (StorageHelper.loadBooleanFromSharedPreferences(StorageHelper.VPLAN_USER_AUTO_UPDATE_PLAY_SOUND, this))
            noBuilder.setSound(sound);
        Notification notify = noBuilder.build();
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notify);

        StorageHelper.saveToSharedPreferences("test", "5", this);
    }

}