package com.lightSnow.VPlanPRS;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;
import android.view.View;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.lightSnow.VPlanPRS.classes.Vertretungsstunde;
import com.lightSnow.VPlanPRS.fragments.fragmentSettings;
import com.lightSnow.VPlanPRS.fragments.fragmentStart;
import com.lightSnow.VPlanPRS.fragments.fragmentVertretungsplan;
import com.lightSnow.VPlanPRS.gcm.MyInstanceIDListenerService;
import com.lightSnow.VPlanPRS.gcm.RegistrationIntentService;
import com.lightSnow.VPlanPRS.helper.StorageHelper;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private int currentFragmentId = 0;

    //gcm
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "MainActivity";
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private ProgressBar mRegistrationProgressBar;
    //gcm end

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //load correct Activity on startup
        boolean firstRunComplete = StorageHelper.loadBooleanFromSharedPreferences(StorageHelper.VPLAN_FIRST_START, this);
        if (!firstRunComplete) {
            Intent intent = new Intent(this, com.lightSnow.VPlanPRS.FirstStartActivity.class);
            startActivity(intent);
            finish();
        }
        setContentView(R.layout.activity_main_layout);

        //load Navigation drawer
        new DrawerBuilder().withActivity(this).build();
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        DrawerBuilder builder = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .addDrawerItems(
                        new SecondaryDrawerItem().withIcon(R.drawable.ic_home_black_48dp).withName(R.string.drawer_title1),
                        new SecondaryDrawerItem().withIcon(R.drawable.ic_event_note_black_48dp).withName(R.string.drawer_title2),
                        new SecondaryDrawerItem().withIcon(R.drawable.ic_event_note_black_48dp).withName(R.string.drawer_title3),
                        new SecondaryDrawerItem().withIcon(R.drawable.ic_view_module_black_48dp).withName(R.string.drawer_title4),
                        new SecondaryDrawerItem().withIcon(R.drawable.ic_settings_black_48dp).withName(R.string.drawer_title5),
                        new SecondaryDrawerItem().withIcon(R.drawable.icon).withName("DEBUG: START SETUP")
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        setFragmentPosition(position);
                        currentFragmentId = position;
                        return false;
                    }
                });
        Drawer drawer = builder.withActionBarDrawerToggleAnimated(true).build();

        //load gcm settings
        boolean autoUpdate = StorageHelper.loadBooleanFromSharedPreferences(StorageHelper.VPLAN_USER_AUTO_UPDATE, this);
        if (autoUpdate) {
            initGCM();
        }
    }

    private void setFragmentPosition(int position) {
        // Create new fragment and transaction
        Fragment newFragment = null;
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        // do something with the clicked item
        switch (position) {
            case 0:
                newFragment = new fragmentStart();
                break;
            case 1:
                newFragment = new fragmentVertretungsplan();
                break;
            case 2:
                newFragment = new fragmentVertretungsplan();
                break;
            case 3:
                newFragment = new fragmentSettings();
                break;
            case 4:
                newFragment = new fragmentSettings();
                break;
            case 5:
                Intent intent = new Intent(getApplicationContext(), com.lightSnow.VPlanPRS.FirstStartActivity.class);
                StorageHelper.saveToSharedPreferences(StorageHelper.VPLAN_FIRST_START, false, this);
                startActivity(intent);
                finish();
                return;
            default:
                newFragment = new fragmentStart();
                break;
        }

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack if needed
        transaction.replace(R.id.fragment_container, newFragment, "fragment");
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void initGCM() {
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (StorageHelper.loadBooleanFromSharedPreferences(StorageHelper.SENT_TOKEN_TO_SERVER, MainActivity.this)) {
                    Toast.makeText(getApplicationContext(), getString(R.string.gcm_send_message), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.token_error_message), Toast.LENGTH_LONG).show();
                }
            }
        };

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }

    public void showNotification() {

        // define sound URI, the sound to be played when there's a notification
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // intent triggered, you can add other intent for other actions
        Intent intent = new Intent(MainActivity.this, MyInstanceIDListenerService.class);
        PendingIntent pIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, 0);

        // this is it, we'll build the notification!
        // in the addAction method, if you don't want any icon, just set the first param to 0
        Notification mNotification = new Notification.Builder(this)

                .setContentTitle("New Post!")
                .setContentText("Here's an awesome update for you!")
                .setContentIntent(pIntent)
                .setSound(soundUri)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // If you want to hide the notification after it was selected, do the code below
        // myNotification.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(0, mNotification);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(StorageHelper.REGISTRATION_COMPLETE));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private void loadNewVPlan() {
        PRS prs = new PRS(this);
        prs.setIfProgressDialogIsShown(true);
        prs.addOnVPlanResultEvent(new PRS.OnVPlanResultEvent() {
            @Override
            public void VPlanResultEvent(List<Vertretungsstunde> result, boolean success) {
                //check if vplan fragment is visible
                Fragment myFragment = (Fragment) getFragmentManager().findFragmentByTag("fragment");
                if (myFragment != null && myFragment instanceof fragmentVertretungsplan && myFragment.isVisible()) {
                    //load the vplan items and show them
                    ((fragmentVertretungsplan) myFragment).showVplanViews(result);
                }
            }
        });
        prs.downloadLinks();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_button_reload:
                loadNewVPlan();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

}


