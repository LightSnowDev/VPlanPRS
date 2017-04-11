package com.lightSnowDev.VPlanPRS2;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.lightSnowDev.VPlanPRS2.classes.PRS;
import com.lightSnowDev.VPlanPRS2.classes.Vertretungsstunde;
import com.lightSnowDev.VPlanPRS2.fragments.fragmentAbout;
import com.lightSnowDev.VPlanPRS2.fragments.fragmentBusfahrplan;
import com.lightSnowDev.VPlanPRS2.fragments.fragmentHelp;
import com.lightSnowDev.VPlanPRS2.fragments.fragmentSettings;
import com.lightSnowDev.VPlanPRS2.fragments.fragmentStart;
import com.lightSnowDev.VPlanPRS2.fragments.fragmentStundenplan;
import com.lightSnowDev.VPlanPRS2.fragments.fragmentVertretungsplan;
import com.lightSnowDev.VPlanPRS2.gcm.MyInstanceIDListenerService;
import com.lightSnowDev.VPlanPRS2.helper.StorageHelper;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    //gcm
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "MainActivity";
    boolean vis = false;
    private int currentFragmentId = 0;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    //gcm end
    private ProgressBar mRegistrationProgressBar;
    private Drawer drawer = null;

    public static boolean checkIfFragmentIsActive(Class<?> inputFragment, Activity ac) {
        if (ac == null || ac.isFinishing())
            return false;
        FragmentManager fManager = ac.getFragmentManager();
        if (fManager == null || ac == null)
            return false;
        Fragment myFragment = fManager.findFragmentByTag("fragment");
        if (myFragment == null)
            return false;
        boolean visible = myFragment.isVisible();
        boolean instance = inputFragment.isInstance(myFragment);
        if (!instance || !visible)
            return false;
        //else
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //load correct Activity on startup
        boolean firstRunComplete = StorageHelper.loadBooleanFromSharedPreferences(StorageHelper.VPLAN_FIRST_START, this);
        if (!firstRunComplete) {
            Intent intent = new Intent(this, com.lightSnowDev.VPlanPRS2.FirstStartActivity.class);
            startActivity(intent);
            finish();
        }
        setContentView(R.layout.activity_main_layout);

        //load Navigation drawer
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DrawerBuilder builder = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .addDrawerItems(
                        new PrimaryDrawerItem().withIcon(R.drawable.ic_home_black_48dp).withName("Start"),
                        new PrimaryDrawerItem().withIcon(R.drawable.ic_event_note_black_48dp).withName("Vertretungsplan heute"),
                        new PrimaryDrawerItem().withIcon(R.drawable.ic_event_note_black_48dp).withName("Vertretungsplan morgen"),
                        new PrimaryDrawerItem().withIcon(R.drawable.ic_view_module_black_48dp).withName("Stundenplan"),
                        new PrimaryDrawerItem().withIcon(R.drawable.ic_directions_bus_black_48dp).withName("Busfahrplan"),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withIcon(R.drawable.ic_help_outline_black_48dp).withName("Hilfe/Fragen"),
                        new SecondaryDrawerItem().withIcon(R.drawable.ic_info_black_48dp).withName("Über diese App"),
                        new SecondaryDrawerItem().withIcon(R.drawable.ic_settings_black_48dp).withName("Einstellungen")
                );
        if (BuildConfig.DEBUG) {
            builder.addDrawerItems(
                    new SecondaryDrawerItem().withIcon(R.drawable.ic_restore_black_48dp).withName("DEBUG: RESTART SETUP")
            );
        }
        builder.withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                setFragmentPosition(position);
                return false;
            }
        });
        drawer = builder.withActionBarDrawerToggleAnimated(true).build();

        //load gcm settings
        boolean autoUpdate = StorageHelper.loadBooleanFromSharedPreferences(StorageHelper.VPLAN_USER_AUTO_UPDATE, this);
        if (autoUpdate) {
            initGCM();
        }

        setReloadButtonVisibility(false);

        //check if a shortcut was pressed
        if ("android.intent.action.heute".equals(getIntent().getAction())) {
            setFragmentPosition(1);
        } else if ("android.intent.action.morgen".equals(getIntent().getAction())) {
            setFragmentPosition(2);
        }
    }

    public void setFragmentPosition(int position) {
        // set selection
        drawer.setSelection(-1);
        // Create new fragment and transaction
        Fragment newFragment = null;
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        // do something with the clicked item
        switch (position) {
            //same as default
            //case 0:
            //    newFragment = new fragmentStart();
            //    setReloadButtonVisibility(false);
            //    break;
            case 1:
                newFragment = new fragmentVertretungsplan();
                Bundle bundleH = new Bundle();
                bundleH.putString("tag", "heute");
                newFragment.setArguments(bundleH);
                setReloadButtonVisibility(true);
                break;
            case 2:
                newFragment = new fragmentVertretungsplan();
                Bundle bundleM = new Bundle();
                bundleM.putString("tag", "morgen");
                newFragment.setArguments(bundleM);
                setReloadButtonVisibility(true);
                break;
            case 3:
                newFragment = new fragmentStundenplan();
                setReloadButtonVisibility(false);
                break;
            case 4:
                newFragment = new fragmentBusfahrplan();
                setReloadButtonVisibility(false);
                break;
            //no 5 since its the divider element
            case 6:
                newFragment = new fragmentHelp();
                setReloadButtonVisibility(false);
                break;
            case 7:
                newFragment = new fragmentAbout();
                setReloadButtonVisibility(false);
                break;
            case 8:
                newFragment = new fragmentSettings();
                setReloadButtonVisibility(false);
                break;
            case 9:
                if (BuildConfig.DEBUG) {
                    //Intent intent = new Intent(getApplicationContext(), com.lightSnowDev.VPlanPRS2.FirstStartActivity.class);
                    //StorageHelper.saveToSharedPreferences(StorageHelper.VPLAN_FIRST_START, false, this);
                    StorageHelper.saveToSharedPreferences(StorageHelper.VPLAN_LAST_APP_NAME_VERSION, "", this);
                    //startActivity(intent);
                    //finish();
                }
                return;
            default:
                newFragment = new fragmentStart();
                setReloadButtonVisibility(false);
                break;
        }

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack if needed
        transaction.replace(R.id.fragment_container, newFragment, "fragment");
        transaction.addToBackStack(null);
        transaction.commit();
        currentFragmentId = position;
    }

    public void setCustomFragmentandDrawer(int position) {
        drawer.setSelectionAtPosition(position);
        //setFragmentPosition(position);
    }

    private void initGCM() {
        if (checkPlayServices()) {
            (new MyInstanceIDListenerService()).onTokenRefresh();
        }
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

    private void setReloadButtonVisibility(boolean v) {
        vis = v;
        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.menu_button_reload).setVisible(vis);
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

    public void loadNewVPlan(Vertretungsstunde.Tag tag) {
        PRS prs = new PRS(this);
        prs.setTagToFilter(tag);
        prs.setIfProgressDialogIsShown(true);
        prs.addOnPRSResultEvent(new PRS.OnPRSResultEvent() {
            @Override
            public void PRSResultEvent(final List<Vertretungsstunde> relatedStunden,
                                       final List<Vertretungsstunde> unknownStunden,
                                       final boolean changedKlassenSpecificData,
                                       final PRS.PRSResultType resultType) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Fragment myFragment = getFragmentManager().findFragmentByTag("fragment");
                        //check if vplan fragment is visible
                        if (myFragment == null || !(myFragment instanceof fragmentVertretungsplan) || !myFragment.isVisible())
                            return;

                        //do specific things based on the return value
                        if (resultType == PRS.PRSResultType.success) {
                            //Toast.makeText(MainActivity.this, "newVPlan: " + String.valueOf(changedKlassenSpecificData), Toast.LENGTH_SHORT).show();
                            ((fragmentVertretungsplan) myFragment).showVplanViews(relatedStunden, unknownStunden, true);
                        } else if (resultType == PRS.PRSResultType.successButStorrage) {
                            ((fragmentVertretungsplan) myFragment).showMessageTextView("Fehler beim Laden des Vertretungsplans.\n" +
                                    "Es konnte keine Verbindung zum PRS-Server hergestellt werden.\n\n" +
                                    "Es wird nun der zuletzt erfolgreich geladene Vertretungsplan angezeigt:\n" +
                                    StorageHelper.loadStringFromSharedPreferences(StorageHelper.VPLAN_LAST_UPDATE_PARSED, MainActivity.this), true);
                            ((fragmentVertretungsplan) myFragment).showVplanViews(relatedStunden, unknownStunden, false);
                        } else if (resultType == PRS.PRSResultType.parseError) {
                            ((fragmentVertretungsplan) myFragment).showMessageTextView("Fehler beim Laden des Vertretungsplans.\n" +
                                    "Es ist ein fehler beim Analysieren des Vertretungsplans aufgetreten. Bitte schaue zur Sicherheit nochmal auf den offiziellen Vertretungsplan.\n\nEs wird nun der zuletzt erfolgreich geladene Vertretungsplan angezeigt:", false);
                            ((fragmentVertretungsplan) myFragment).showVplanViews(relatedStunden, unknownStunden, false);
                        } else if (resultType == PRS.PRSResultType.downloadAndStorrageError) {
                            ((fragmentVertretungsplan) myFragment).showMessageTextView("Fehler beim Laden des Vertretungsplans.\n" +
                                    "Es konnte nach 5 Sekunden keine Verbindung hergestellt werden.\n\n" +
                                    "Es konnte ebenso kein Vertretungsplan aus dem Speicher geladen werden.", true);
                        }
                    }
                });
            }
        });
        prs.downloadLinks();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_button_reload:
                Fragment myFragment = getFragmentManager().findFragmentByTag("fragment");
                //check if vplan fragment is visible
                if (myFragment == null || !(myFragment instanceof fragmentVertretungsplan) || !myFragment.isVisible())
                    return true;
                else {
                    ((fragmentVertretungsplan) myFragment).showVPlan();
                    return true;
                }
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
}


