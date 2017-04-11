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
import com.lightSnowDev.VPlanPRS2.classes.Vertretungsstunde;
import com.lightSnowDev.VPlanPRS2.helper.StorageHelper;

import java.util.List;

public class MyGcmListenerService extends FirebaseMessagingService {

    private static final String TAG = "MyGcmListenerService";

    /**
     * Called when message is received.
     * NOTE: use 'data messages' from fcm
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        RemoteMessage.Notification note = remoteMessage.getNotification();
        Log.d(TAG, "got message from: " + remoteMessage.getFrom());
        //Log.d(TAG, "Notification Message Body: " + note.getBody());
        //check if notification user setting
        if (!StorageHelper.loadBooleanFromSharedPreferences(StorageHelper.VPLAN_USER_AUTO_UPDATE, this)) {

            return;
        }
        //Analyse des V-Plans...
        PRS prs = new PRS(this);
        prs.setTagToFilter(Vertretungsstunde.Tag.beide);
        prs.addOnPRSResultEvent(new PRS.OnPRSResultEvent() {
            @Override
            public void PRSResultEvent(List<Vertretungsstunde> relatedStunden, List<Vertretungsstunde> unknownStunden, boolean changedKlassenSpecificData, PRS.PRSResultType resultType) {
                if (resultType == PRS.PRSResultType.success)
                    sendNotification(relatedStunden.size());
            }
        });
        prs.downloadLinks();

    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param count Anzahl der Änderungen des V-Plans
     */
    private void sendNotification(int count) {
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

        Notification notify;
        //build level is 16, so this is ok
        notify = noBuilder.build();


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notify);
    }

}