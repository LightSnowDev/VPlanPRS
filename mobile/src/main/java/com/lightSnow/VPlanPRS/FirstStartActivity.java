package com.lightSnow.VPlanPRS;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.lightSnow.VPlanPRS.helper.ProgressBarAnimationHelper;
import com.lightSnow.VPlanPRS.helper.StorageHelper;

public class FirstStartActivity extends Activity implements AdapterView.OnItemSelectedListener {

    private int page = 1;
    static FirstStartActivity thisActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_start_layout);
        thisActivity = this;

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            //getWindow().getDecorView().setSystemUiVisibility(
            //        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            //                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        showFirstPage();
    }

    private void showFirstPage() {
        ((RelativeLayout) findViewById(R.id.firstStart_Page1_MainRelativeLayout)).setVisibility(View.VISIBLE);
        ((RelativeLayout) findViewById(R.id.firstStart_Page2_MainRelativeLayout)).setVisibility(View.GONE);
        ((RelativeLayout) findViewById(R.id.firstStart_Page3_MainRelativeLayout)).setVisibility(View.GONE);
        ((RelativeLayout) findViewById(R.id.firstStart_Page4_firstFilter_MainRelativeLayout)).setVisibility(View.GONE);
        ((RelativeLayout) findViewById(R.id.firstStart_Page4_secondsChoose_MainRelativeLayout)).setVisibility(View.GONE);
        ((RelativeLayout) findViewById(R.id.firstStart_Page5_MainRelativeLayout)).setVisibility(View.GONE);
        ((RelativeLayout) findViewById(R.id.firstStart_Page6_MainRelativeLayout)).setVisibility(View.GONE);

        //set default status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.WHITE);
        }

        //Animation Text welcome
        //TextView hiText = (TextView) findViewById(R.id.firstStart_Page1_textView_firstExplanation);
        //Animation fadein = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        //fadein.setFillAfter(true);
        //hiText.startAnimation(fadein);

        //set button click
        Button fab = (Button) findViewById(R.id.button_weiter);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (page) {
                    case 1:
                        showSecondPage();
                        break;
                    case 2:
                        showThirdPage();
                        break;
                    case 3:
                        showFourth_filter_Page();
                        break;
                    case 4:
                        showFourth_choose_Page();
                        break;
                    case 5:
                        showFifthPage();
                        break;
                    case 6:
                        showSixthPage();
                        break;
                    case 7:
                        exitFirstStartActivity();
                        break;
                }
            }
        });
    }

    private void showSecondPage() {
        animatePageTransition(R.id.firstStart_Page1_MainRelativeLayout, R.id.firstStart_Page2_MainRelativeLayout,1);
        setHeaderText("Neuigkeiten");
        Button bttnTEST = (Button) findViewById(R.id.firstStart_Page2_Button_expandable);
        bttnTEST.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                findViewById(R.id.firstStart_Page2_thirdText_expandable).setVisibility(View.VISIBLE);
                findViewById(R.id.firstStart_Page2_Button_expandable).setVisibility(View.INVISIBLE);
            }
        });
    }

    private void showThirdPage() {
        animatePageTransition(R.id.firstStart_Page2_MainRelativeLayout, R.id.firstStart_Page3_MainRelativeLayout,1);
        setHeaderText("Anmeldung");
    }

    private void showFourth_filter_Page() {
        //check correct credentials
        EditText user = (EditText) findViewById(R.id.firstStart_Page2_editTextBenutzername);
        EditText password = (EditText) findViewById(R.id.firstStart_Page2_editTextPasswort);
        if (!user.getText().toString().equals("vplan") | !password.getText().toString().equals("2011")) {
            //wrong credentials
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Nachricht");
            builder.setMessage("Das Passwort oder der Nutzername wurde falsch eingegeben.");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //no code here
                }
            });
            builder.show();
        } else {
            animatePageTransition(R.id.firstStart_Page3_MainRelativeLayout, R.id.firstStart_Page4_firstFilter_MainRelativeLayout,1);
            setHeaderText("Stunden filtern");
            loadSwitchFilter();
        }
    }

    private void showFourth_choose_Page() {
        if (StorageHelper.loadBooleanFromSharedPreferences(StorageHelper.VPLAN_USER_KLASSE_FILTER, this)) {
            //right credentials
            animatePageTransition(R.id.firstStart_Page4_firstFilter_MainRelativeLayout, R.id.firstStart_Page4_secondsChoose_MainRelativeLayout,1);
            setHeaderText("Klasse auswählen");
            Spinner spin = (Spinner) findViewById(R.id.spinner1);
            spin.setOnItemSelectedListener(this);
            //set shared preferences defaults
            StorageHelper.saveToSharedPreferences(StorageHelper.VPLAN_USER_KLASSE, "5G1", this);
        } else {
            showFifthPage();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // An item was selected. You can retrieve the selected item using:
        // parent.getItemAtPosition(pos)

        StorageHelper.saveToSharedPreferences(StorageHelper.VPLAN_USER_KLASSE, (String) parent.getItemAtPosition(pos), this);
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    private void showFifthPage() {
        if (StorageHelper.loadBooleanFromSharedPreferences(StorageHelper.VPLAN_USER_KLASSE_FILTER, this))
            animatePageTransition(R.id.firstStart_Page4_secondsChoose_MainRelativeLayout, R.id.firstStart_Page5_MainRelativeLayout, 1);
        else
            animatePageTransition(R.id.firstStart_Page4_firstFilter_MainRelativeLayout, R.id.firstStart_Page5_MainRelativeLayout, 2);

        setHeaderText("Automatische Aktualisierung");
        loadSwitchAutoUpdate();
    }

    private void loadSwitchAutoUpdate() {
        //set default
        StorageHelper.saveToSharedPreferences(StorageHelper.VPLAN_USER_AUTO_UPDATE, true, FirstStartActivity.this);
        //load preferences
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(
                "com.lightSnow.VPlanPRS", Context.MODE_PRIVATE);
        //set switch
        Switch mySwitch = (Switch) findViewById(R.id.firstStart_Page5_Switch);
        mySwitch.setChecked(prefs.getBoolean(getString(R.string.settingUpdateBool), true));
        mySwitch.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                StorageHelper.saveToSharedPreferences(StorageHelper.VPLAN_USER_AUTO_UPDATE, buttonView.isChecked(), FirstStartActivity.this);
            }
        });
    }

    private void loadSwitchFilter() {
        //set default
        StorageHelper.saveToSharedPreferences(StorageHelper.VPLAN_USER_KLASSE_FILTER, true, FirstStartActivity.this);
        //set switch
        Switch mySwitch = (Switch) findViewById(R.id.firstStart_Page4_first_filter_Switch);
        mySwitch.setChecked(StorageHelper.loadBooleanFromSharedPreferences(StorageHelper.VPLAN_USER_KLASSE_FILTER, this));
        mySwitch.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                StorageHelper.saveToSharedPreferences(StorageHelper.VPLAN_USER_KLASSE_FILTER, buttonView.isChecked(), FirstStartActivity.this);
            }
        });
    }

    private void showSixthPage() {
        animatePageTransition(R.id.firstStart_Page5_MainRelativeLayout, R.id.firstStart_Page6_MainRelativeLayout, 1);
        setHeaderText("Fertig");
        ((Button) findViewById(R.id.button_weiter)).setText("SETUP BEENDEN");
    }

    private void exitFirstStartActivity() {
        //save setting
        StorageHelper.saveToSharedPreferences(StorageHelper.VPLAN_FIRST_START, true, this);
        //close FirstStartActivity
        finish();
        //start normal activity
        Intent intent = new Intent(this, com.lightSnow.VPlanPRS.MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void animatePageTransition(final int pageFrom, final int pageTo, int steps) {
        //fade in/out animation of pages
        ((View) findViewById(pageTo)).setVisibility(View.VISIBLE);
        ((View) findViewById(pageTo)).setAlpha(1f);
        final Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        ((View) findViewById(pageFrom)).startAnimation(fadeOut);
        fadeOut.setFillAfter(true);
        ((Button) findViewById(R.id.button_weiter)).postDelayed(new Runnable() {
            @Override
            public void run() {
                fadeOut.cancel();
                ((View) findViewById(pageFrom)).clearAnimation();
                ((View) findViewById(pageFrom)).setVisibility(View.GONE);
            }
        }, 400);

        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fadeIn.setFillAfter(true);
        ((View) findViewById(pageTo)).startAnimation(fadeIn);

        ProgressBar bar = (ProgressBar) findViewById(R.id.firstStart_progressBar);
        //bar.setProgress(50);
        ProgressBarAnimationHelper anim = new ProgressBarAnimationHelper(bar, bar.getProgress(), bar.getProgress() + 17 * steps);
        bar.startAnimation(anim);
        page += steps;
    }

    private void setHeaderText(String input) {
        ((TextView) findViewById(R.id.firstStart_textView_Header)).setText(input);
    }

}
