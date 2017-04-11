package com.lightSnowDev.VPlanPRS2.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.iid.FirebaseInstanceId;
import com.lightSnowDev.VPlanPRS2.R;
import com.lightSnowDev.VPlanPRS2.gcm.MyInstanceIDListenerService;
import com.lightSnowDev.VPlanPRS2.helper.StorageHelper;
import com.lightSnowDev.VPlanPRS2.items.SettingView;

/**
 * Created by Jonathan Schwarzenböck on 05.01.2016.
 */
public class fragmentSettings extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        //Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings_layout, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Einstellungen");
        LinearLayout lay = (LinearLayout) getView().findViewById(R.id.linearLayout_settings_main);
        //remove double views. preserve setting "change klasse"
        for (int i = lay.getChildCount() - 1; i >= 0; i--) {
            final View child = lay.getChildAt(i);
            if (child instanceof SettingView) {
                lay.removeView(child);
            }
        }

        SettingView s1 = new SettingView(StorageHelper.VPLAN_USER_AUTO_UPDATE, getActivity());
        s1.setMainText("Automatisch aktualisieren");
        s1.setSecondText("Soll automatisch nach neuen Vertretungsstunden gesucht werden? Es wird dann automatisch eine Benachrichtigung an dein Handy geschickt, wenn sich etwas für Dich geändert hat.");
        s1.addOnSwitchEvent(new SettingView.OnSwitchEvent() {
            @Override
            public void switchEvent(boolean newValue) {
                String token = FirebaseInstanceId.getInstance().getToken();
                if (newValue == true)
                    MyInstanceIDListenerService.sendGCMServerData(token, getActivity());
                else
                    MyInstanceIDListenerService.removeGCMServerData(token, getActivity());
            }
        });
        lay.addView(s1, 0);

        SettingView s2 = new SettingView(StorageHelper.VPLAN_USER_AUTO_UPDATE_PLAY_SOUND, getActivity());
        s2.setMainText("Ton Benachrichtigungen");
        s2.setSecondText("Sollen die Benachrichtigungen einen Ton haben?");
        lay.addView(s2, 1);

        SettingView s3 = new SettingView(StorageHelper.VPLAN_USER_KLASSE_FILTER, getActivity());
        s3.setMainText("Nach Klasse filtern");
        s3.setSecondText("Soll der Vertretungsplan nach deiner Klasse durchsucht werden, oder sollen alle Klassen angezeigt werden?");
        lay.addView(s3, 2);

        SettingView s4 = new SettingView(StorageHelper.VPLAN_USER_LEHRER_NAME_KUERZEL, getActivity());
        s4.setMainText("Namen der Lehrer");
        s4.setSecondText("Soll zusätzlich zum Kürzel eines Lehrers der Nachname angezeigt werden?");
        lay.addView(s4, 3);

        getView().findViewById(R.id.relativeLayout_settings_setKlassen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showKlasseChangeDialog();
            }
        });
    }

    private void showKlasseChangeDialog() {
        final String alteKlasse = StorageHelper.loadStringFromSharedPreferences(StorageHelper.VPLAN_USER_KLASSE, getActivity());
        final StringBuffer a, b, c;
        a = new StringBuffer("5");
        b = new StringBuffer("G");
        c = new StringBuffer("1");
        MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(getActivity())
                .title("Klasse ändern")
                .customView(R.layout.dialog_change_klasse, false);
        dialogBuilder.positiveText("Ändern");
        dialogBuilder.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                StorageHelper.saveToSharedPreferences(StorageHelper.VPLAN_USER_KLASSE, (a + b.toString() + c.toString()), getActivity());
            }
        });
        dialogBuilder.neutralText("Hilfe");
        dialogBuilder.onNeutral(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                new MaterialDialog.Builder(getActivity())
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
                                "Solltest Du deine Klasse nicht finden, schreibe mir bitte eine E-Mail. Du findest die E-Mail Adresse später in der App bei 'Über diese App'. Oder du guckst im Play-Store nach meiner Webseite.")
                        .positiveText("Ok")
                        .show();
            }
        });
        dialogBuilder.negativeText("Abbrechen");
        dialogBuilder.onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                dialog.dismiss();
            }
        });
        final MaterialDialog dialog = dialogBuilder.build();


        final Spinner spin1 = (Spinner) dialog.getCustomView().findViewById(R.id.spinner1);
        final Spinner spin2 = (Spinner) dialog.getCustomView().findViewById(R.id.spinner2);
        final Spinner spin3 = (Spinner) dialog.getCustomView().findViewById(R.id.spinner3);
        AdapterView.OnItemSelectedListener s = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                a.setLength(0);
                b.setLength(0);
                c.setLength(0);
                a.append((String) spin1.getSelectedItem());
                b.append((String) spin2.getSelectedItem());
                c.append((String) spin3.getSelectedItem());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        };
        spin1.setOnItemSelectedListener(s);
        spin2.setOnItemSelectedListener(s);
        spin3.setOnItemSelectedListener(s);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
}