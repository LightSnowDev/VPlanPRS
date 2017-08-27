package com.lightSnowDev.VPlanPRS2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.lightSnowDev.VPlanPRS2.helper.ProgressBarAnimationHelper;
import com.lightSnowDev.VPlanPRS2.helper.StorageHelper;

public class FirstStartActivity extends Activity {

    private int page = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_start_layout);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        showFirstPage();
    }

    private void showFirstPage() {
        findViewById(R.id.firstStart_Page1_MainRelativeLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.firstStart_Page2_MainRelativeLayout).setVisibility(View.GONE);
        findViewById(R.id.firstStart_Page3_MainRelativeLayout).setVisibility(View.GONE);
        findViewById(R.id.firstStart_Page4_firstFilter_MainRelativeLayout).setVisibility(View.GONE);
        findViewById(R.id.firstStart_Page4_secondsChoose_MainRelativeLayout).setVisibility(View.GONE);
        findViewById(R.id.firstStart_Page5_MainRelativeLayout).setVisibility(View.GONE);
        findViewById(R.id.firstStart_Page6_MainRelativeLayout).setVisibility(View.GONE);
        findViewById(R.id.button_help).setVisibility(View.GONE);

        //set default status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.WHITE);
        }

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

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Achtung");
        builder.setMessage("Dieses Programm ist eine BETA!\nEs funktioniert nicht alles, überall sind Bugs. Diese App sollte so NICHT im Alltag verwendet werden!");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //no code here
            }
        });
        builder.show();
    }

    private void showSecondPage() {
        animatePageTransition(R.id.firstStart_Page1_MainRelativeLayout, R.id.firstStart_Page2_MainRelativeLayout, 1);
        setHeaderText("Neuigkeiten");
        Button bttnTEST = (Button) findViewById(R.id.firstStart_Page2_Button_expandable);

        ((TextView) findViewById(R.id.firstStart_Page2_secondText)).setText(StorageHelper.readFromAssetFile("news_new.txt", this));
        ((TextView) findViewById(R.id.firstStart_Page2_thirdText_expandable)).setText(StorageHelper.readFromAssetFile("news_old.txt", this));

        bttnTEST.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                findViewById(R.id.firstStart_Page2_thirdText_expandable).setVisibility(View.VISIBLE);
                findViewById(R.id.firstStart_Page2_Button_expandable).setVisibility(View.INVISIBLE);
            }
        });
    }

    private void showThirdPage() {
        animatePageTransition(R.id.firstStart_Page2_MainRelativeLayout, R.id.firstStart_Page3_MainRelativeLayout, 1);
        setHeaderText("Anmeldung");

        //set color indicators, to show correct credentials
        final EditText user = (EditText) findViewById(R.id.firstStart_Page2_editTextBenutzername);
        final EditText password = (EditText) findViewById(R.id.firstStart_Page2_editTextPasswort);
        TextWatcher w = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (user.getText().toString().equals("vplan"))
                    user.setTextColor(Color.parseColor("#4CAF50"));
                else
                    user.setTextColor(Color.BLACK);
                if (password.getText().toString().equals("2011"))
                    password.setTextColor(Color.parseColor("#4CAF50"));
                else
                    password.setTextColor(Color.BLACK);
            }
        };
        user.addTextChangedListener(w);
        password.addTextChangedListener(w);
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
            //correkt
            //todo: hide soft keyboard
            //show next page
            animatePageTransition(R.id.firstStart_Page3_MainRelativeLayout, R.id.firstStart_Page4_firstFilter_MainRelativeLayout, 1);
            setHeaderText("Stunden filtern");
            loadSwitchFilter();
        }
    }

    private void showFourth_choose_Page() {
        //set shared preferences defaults
        StorageHelper.saveToSharedPreferences(StorageHelper.VPLAN_USER_KLASSE, "05G1", this);
        if (StorageHelper.loadBooleanFromSharedPreferences(StorageHelper.VPLAN_USER_KLASSE_FILTER, this)) {
            //right credentials
            animatePageTransition(R.id.firstStart_Page4_firstFilter_MainRelativeLayout, R.id.firstStart_Page4_secondsChoose_MainRelativeLayout, 1);
            setHeaderText("Klasse auswählen");
            final Spinner spin1 = (Spinner) findViewById(R.id.spinner1);
            final Spinner spin2 = (Spinner) findViewById(R.id.spinner2);
            final Spinner spin3 = (Spinner) findViewById(R.id.spinner3);
            AdapterView.OnItemSelectedListener s = new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    String a = (String) spin1.getSelectedItem();
                    String b = (String) spin2.getSelectedItem();
                    String c = (String) spin3.getSelectedItem();
                    StorageHelper.saveToSharedPreferences(StorageHelper.VPLAN_USER_KLASSE, (a + b + c), FirstStartActivity.this);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            };
            spin1.setOnItemSelectedListener(s);
            spin2.setOnItemSelectedListener(s);
            spin3.setOnItemSelectedListener(s);
            findViewById(R.id.button_help).setVisibility(View.VISIBLE);
            findViewById(R.id.button_help).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new MaterialDialog.Builder(FirstStartActivity.this)
                            .title("Hilfe")
                            .content("Bitte trage hier genau den Wert ein, der unter 'Klasse(n)' bei Dir im Vertretungsplan angezeigt wird.\n\n" +
                                    "--Beipiele--:\n" +
                                    "Wenn Du in der 5G3 bist trage hier 05G3 ein.\n\n" +
                                    "Wenn Du in der 8H1 bist trage hier 08H1 ein.\n\n" +
                                    "Wenn Du in der 10R1 bist, trage 10R1 ein.\n\n" +
                                    "Wenn Du in der ET7 bist, trage ET7 ein. Du musst hierzu in der ersten Spalte das Leerzeichen ganz unten auswählen.\n\n" +
                                    "Wenn Du in der Q12 bist, trage Q12 ein. In der Oberstufe gibt es keine 'Klassen' mehr." +
                                    "Hier werden alle Vertretungsstunden des Jahrgangs angezeigt.\n\n" +
                                    "Allgemeiner Tip: Wähle in der ersten Spalte ganz unten das 'Leerzeichen' aus. Nur so kannst Du z.B. Q12 korrekt eintragen.\n\n" +
                                    "Solltest Du Deine Klasse nicht finden, schreibe mir bitte eine E-Mail: jonathan@lightsnowdev.com.")
                            .positiveText("Ok")
                            .show();
                }
            });
        } else {
            showFifthPage();
        }
    }

    private void showFifthPage() {
        if (StorageHelper.loadBooleanFromSharedPreferences(StorageHelper.VPLAN_USER_KLASSE_FILTER, this))
            animatePageTransition(R.id.firstStart_Page4_secondsChoose_MainRelativeLayout, R.id.firstStart_Page5_MainRelativeLayout, 1);
        else
            animatePageTransition(R.id.firstStart_Page4_firstFilter_MainRelativeLayout, R.id.firstStart_Page5_MainRelativeLayout, 2);
        findViewById(R.id.button_help).setVisibility(View.GONE);
        setHeaderText("Automatische Aktualisierung");
        loadSwitchAutoUpdate();
    }

    private void loadSwitchAutoUpdate() {
        //set defaults
        StorageHelper.saveToSharedPreferences(StorageHelper.VPLAN_USER_AUTO_UPDATE, true, FirstStartActivity.this);
        StorageHelper.saveToSharedPreferences(StorageHelper.VPLAN_USER_AUTO_UPDATE_PLAY_SOUND, true, FirstStartActivity.this);
        //load preferences
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(
                "com.lightSnow.VPlanPRS", Context.MODE_PRIVATE);

        //set switchs
        Switch mySwitch = (Switch) findViewById(R.id.firstStart_Page5_Switch);
        mySwitch.setChecked(prefs.getBoolean(StorageHelper.VPLAN_USER_AUTO_UPDATE, true));
        mySwitch.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                StorageHelper.saveToSharedPreferences(StorageHelper.VPLAN_USER_AUTO_UPDATE, buttonView.isChecked(), FirstStartActivity.this);
            }
        });
        Switch mySwitch2 = (Switch) findViewById(R.id.firstStart_Page5_Switch_sound);
        mySwitch2.setChecked(prefs.getBoolean(StorageHelper.VPLAN_USER_AUTO_UPDATE_PLAY_SOUND, true));
        mySwitch2.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                StorageHelper.saveToSharedPreferences(StorageHelper.VPLAN_USER_AUTO_UPDATE_PLAY_SOUND, buttonView.isChecked(), FirstStartActivity.this);
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

        //set default
        StorageHelper.saveToSharedPreferences(StorageHelper.VPLAN_USER_LEHRER_NAME_KUERZEL, true, FirstStartActivity.this);
        //set switch
        Switch mySwitch2 = (Switch) findViewById(R.id.firstStart_Page4_first_name_lehrer_Switch);
        mySwitch2.setChecked(StorageHelper.loadBooleanFromSharedPreferences(StorageHelper.VPLAN_USER_LEHRER_NAME_KUERZEL, this));
        mySwitch2.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                StorageHelper.saveToSharedPreferences(StorageHelper.VPLAN_USER_LEHRER_NAME_KUERZEL, buttonView.isChecked(), FirstStartActivity.this);
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
        StorageHelper.saveToSharedPreferences(StorageHelper.VPLAN_LAST_APP_NAME_VERSION, BuildConfig.VERSION_NAME, this);
        //close FirstStartActivity
        finish();
        //start normal activity
        Intent intent = new Intent(this, com.lightSnowDev.VPlanPRS2.MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void animatePageTransition(final int pageFrom, final int pageTo, int steps) {
        //fade in/out animation of pages
        findViewById(pageTo).setVisibility(View.VISIBLE);
        findViewById(pageTo).setAlpha(1f);
        final Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        findViewById(pageFrom).startAnimation(fadeOut);
        fadeOut.setFillAfter(true);
        findViewById(R.id.button_weiter).postDelayed(new Runnable() {
            @Override
            public void run() {
                fadeOut.cancel();
                findViewById(pageFrom).clearAnimation();
                findViewById(pageFrom).setVisibility(View.GONE);
            }
        }, 400);

        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fadeIn.setFillAfter(true);
        findViewById(pageTo).startAnimation(fadeIn);

        ProgressBar bar = (ProgressBar) findViewById(R.id.firstStart_progressBar);
        ProgressBarAnimationHelper anim = new ProgressBarAnimationHelper(bar, bar.getProgress(), bar.getProgress() + 17 * steps);
        bar.startAnimation(anim);
        page += steps;
    }

    private void setHeaderText(String input) {
        ((TextView) findViewById(R.id.firstStart_textView_Header)).setText(input);
    }

}
