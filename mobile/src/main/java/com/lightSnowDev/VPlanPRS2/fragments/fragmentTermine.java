package com.lightSnowDev.VPlanPRS2.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lightSnowDev.VPlanPRS2.MainActivity;
import com.lightSnowDev.VPlanPRS2.R;
import com.lightSnowDev.VPlanPRS2.helper.DownloadHelper;
import com.lightSnowDev.VPlanPRS2.helper.StorageHelper;
import com.lightSnowDev.VPlanPRS2.items.TerminView;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import belka.us.androidtoggleswitch.widgets.BaseToggleSwitch;
import belka.us.androidtoggleswitch.widgets.ToggleSwitch;

/**
 * Created by Jonathan on 26.06.2017.
 */

public class fragmentTermine extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_termine_layout, container, false);
    }

    // URL:
    // https://portal.lanis-system.de/kalender.php?
    // f=getEvents&
    // start=2017-06-26&
    // end=2017-08-07&
    // i=6110&
    // key=8d76d4f44b21cd130cd9b5ae060ade4f5f832e753cec07e1d5d5b36fa9aa8a01f71d4b1d2f382f8063d7c4e875f64c21
    //
    // = https://portal.lanis-system.de/kalender.php?f=getEvents&start=2017-06-26&end=2017-08-07&i=6110&key=8d76d4f44b21cd130cd9b5ae060ade4f5f832e753cec07e1d5d5b36fa9aa8a01f71d4b1d2f382f8063d7c4e875f64c21

    //Ein Termin
    /*
        {
           "Institution":"6110",
           "Id":"671",
           "FremdUID":null,
           "LetzteAenderung":"2016-09-20 15:14:53",
           "Anfang":"2017-06-22 00:00:00",
           "Ende":"2017-06-28 23:59:00",
           "Verantwortlich":null,
           "Ort":null,
           "Oeffentlich":"ja",
           "Privat":"nein",
           "Geheim":"nein",
           "Neu":"nein",
           "title":"Projektwoche",
           "category":"22",
           "description":"",
           "allDay":true,
           "start":"2017-06-22",
           "end":"2017-06-29"
        }
     */

    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Paris"), Locale.GERMANY);

    @Override
    public void onResume() {
        super.onResume();
        String klassenname = "Schule";
        if (StorageHelper.loadBooleanFromSharedPreferences(StorageHelper.VPLAN_USER_KLASSE_FILTER, getActivity()))
            klassenname = StorageHelper.loadStringFromSharedPreferences(StorageHelper.VPLAN_USER_KLASSE, getActivity());
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Termine: " + getMonthName());

        init();
        loadEvents();
    }

    private void init() {
        if (!MainActivity.checkIfFragmentIsActive(fragmentTermine.class, getActivity()))
            return;

        cal.set(Calendar.DAY_OF_MONTH, 1);

        final ToggleSwitch heute_morgen = (ToggleSwitch) getView().findViewById(R.id.fragment_termine_toggleSwitch_heute_morgen);
        heute_morgen.setOnToggleSwitchChangeListener(new BaseToggleSwitch.OnToggleSwitchChangeListener() {
            @Override
            public void onToggleSwitchChangeListener(int position, boolean isChecked) {
                loadEvents();
            }
        });

        final ToggleSwitch prev_next = (ToggleSwitch) getView().findViewById(R.id.fragment_termine_toggleSwitch_prev_next);
        prev_next.setOnToggleSwitchChangeListener(new BaseToggleSwitch.OnToggleSwitchChangeListener() {
            @Override
            public void onToggleSwitchChangeListener(int position, boolean isChecked) {
                if (prev_next.getCheckedTogglePosition() == 0) {
                    //Eine einheit abziehen
                    if (isMonthActive())
                        if (isMonthActive())
                            cal.add(Calendar.MONTH, -1);
                        else
                            cal.add(Calendar.DATE, -7);
                } else {
                    //Eine Einheit hinzufügen
                    if (isMonthActive())
                        cal.add(Calendar.MONTH, 1);
                    else
                        cal.add(Calendar.DATE, 7);
                }
                loadEvents();
            }
        });
    }

    private void loadEvents() {
        if (!MainActivity.checkIfFragmentIsActive(fragmentTermine.class, getActivity()))
            return;

        String startDateString = (new SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY).format(cal.getTime()));

        Calendar endCalendar = (Calendar) cal.clone();
        //Eine Einheit hinzufügen; Abstand zum Start.
        if (isMonthActive())
            endCalendar.add(Calendar.MONTH, 1);
        else
            endCalendar.add(Calendar.WEEK_OF_YEAR, 1);

        String endDateString = (new SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY).format(endCalendar.getTime()));

        DownloadHelper helper = new DownloadHelper(getActivity());
        helper.setFIXED_URL("https://portal.lanis-system.de/");
        helper.addOnRecievedEvent(new DownloadHelper.OnRecievedEvent() {
            @Override
            public void recievedEvent(String resultString, String resultBase64, boolean success) {
                if (!MainActivity.checkIfFragmentIsActive(fragmentTermine.class, getActivity()))
                    return;

                LinearLayout layout = (LinearLayout) getView().findViewById(R.id.fragment_termine_linearLayoutList);
                layout.removeAllViews();

                if (!success) {
                    addErrorToLayout("Wahrscheinlich besteht keine Internetverbindung.\n" +
                            "Termine werden nicht zwischengepeichert.", layout);
                    return;
                }

                try {
                    parseTermine(resultString, layout);
                } catch (RuntimeException e) {
                    layout.removeAllViews();
                    addErrorToLayout(e.getMessage(), layout);
                }
            }
        });
        HashMap k = new HashMap();
        k.put("f", "getEvents");
        k.put("start", startDateString);
        k.put("end", endDateString);
        k.put("ki", "6110");
        k.put("kkey", "8d76d4f44b21cd130cd9b5ae060ade4f5f832e753cec07e1d5d5b36fa9aa8a01f71d4b1d2f382f8063d7c4e875f64c21");
        helper.post("kalender.php", k, null);

        if (isMonthActive())
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Termine: " + getMonthName());
        else
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Termine: " + getWeekNumberString() + ". Woche");
    }

    private void parseTermine(String input, LinearLayout layoutToAdd) {
        if (!MainActivity.checkIfFragmentIsActive(fragmentTermine.class, getActivity()))
            return;

        if (input == null || input.trim().equals(""))
            throw new RuntimeException("Fehler #ft102");

        try {
            JSONArray c = new JSONArray(input);
            for (int i = 0; i < c.length(); i++) {
                try {
                    TerminView termin = new TerminView(getActivity(), c.getJSONObject(i));
                    layoutToAdd.addView(termin);
                } catch (Exception e) {
                    throw new RuntimeException("Fehler #fT101");
                }
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Fehler #fT100");
        }
    }

    private boolean isMonthActive() {
        ToggleSwitch heute_morgen = (ToggleSwitch) getView().findViewById(R.id.fragment_termine_toggleSwitch_heute_morgen);
        return (heute_morgen.getCheckedTogglePosition() == 0);
    }

    private String getMonthName() {
        return new SimpleDateFormat("MMMM", Locale.GERMANY).format(cal.getTime());
    }

    private String getWeekNumberString() {
        return String.valueOf(cal.get(Calendar.WEEK_OF_YEAR));
    }

    private void addErrorToLayout(String error, LinearLayout layout) {
        TextView t = new TextView(getActivity());
        t.setGravity(Gravity.CENTER);
        t.setText("\r\nTermine konnten nicht geladen werden.\r\n" + error);
        layout.addView(t);
    }

}
