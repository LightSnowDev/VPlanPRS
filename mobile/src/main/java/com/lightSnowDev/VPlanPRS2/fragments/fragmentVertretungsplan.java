package com.lightSnowDev.VPlanPRS2.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.crash.FirebaseCrash;
import com.lightSnowDev.VPlanPRS2.MainActivity;
import com.lightSnowDev.VPlanPRS2.R;
import com.lightSnowDev.VPlanPRS2.classes.PRS;
import com.lightSnowDev.VPlanPRS2.classes.VertretungsStunde;
import com.lightSnowDev.VPlanPRS2.classes.VertretungsTag;
import com.lightSnowDev.VPlanPRS2.classes.VertretungsplanNews;
import com.lightSnowDev.VPlanPRS2.helper.StorageHelper;
import com.lightSnowDev.VPlanPRS2.items.VertretungsplanNewsView;
import com.lightSnowDev.VPlanPRS2.items.VertretungsplanView;

import java.util.Date;
import java.util.List;

/**
 * Created by Jonathan Schwarzenböck on 05.01.2016.
 */
public class fragmentVertretungsplan extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_vertretungsplan_layout, container, false);
    }

    VertretungsTag.Day day;

    public void showVPlan() {
        MainActivity mainActivity = ((MainActivity) getActivity());
        String tagString = getArguments().getString("tag", "beide");
        //getVertretungsstundeTagFromString kann null sein
        day = VertretungsStunde.getVertretungsstundeTagFromString(tagString);
        if (day != null)
            loadNewVPlan(day);
        else {
            // Zeige Startseite an
            FirebaseCrash.log("Error fV100: getVertretungsstundeTagFromString is null");
            mainActivity.setFragmentPosition(0);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        String klassenname = "Schule";
        if (StorageHelper.loadBooleanFromSharedPreferences(StorageHelper.VPLAN_USER_KLASSE_FILTER, getActivity()))
            klassenname = StorageHelper.loadStringFromSharedPreferences(StorageHelper.VPLAN_USER_KLASSE, getActivity());
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(
                getArguments().getString("tag", "? Tag ?") + " - " + klassenname);
        //load vplan stunden
        showVPlan();
    }

    public void loadNewVPlan(VertretungsTag.Day tag) {
        PRS prs = new PRS(getActivity());
        prs.setIfProgressDialogIsShown(true);
        if (StorageHelper.loadBooleanFromSharedPreferences(StorageHelper.VPLAN_USER_KLASSE_FILTER, getActivity()))
            prs.setKlasseToFilter(StorageHelper.loadStringFromSharedPreferences(StorageHelper.VPLAN_USER_KLASSE, getActivity()));
        prs.addOnPRSResultEvent(new PRS.OnPRSResultEvent() {
            @Override
            public void PRSResultEvent(final PRS prs, final PRS.PRSResultType resultType) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!MainActivity.checkIfFragmentIsActive(fragmentVertretungsplan.class, getActivity()))
                            return;

                        if (resultType == PRS.PRSResultType.success) {
                            showVplanViews(
                                    prs.getVertretungsplanNewsString(day),
                                    prs.getRelatedDate(day),
                                    prs.getRelatedStunden(day),
                                    prs.getUnknownStunden(day), true);

                        } else if (resultType == PRS.PRSResultType.successButStorrage) {
                            showMessageTextView("Fehler beim Laden des Vertretungsplans.\n" +
                                    "Es konnte keine Verbindung zum PRS-Server hergestellt werden.\n\n" +
                                    "Es wird nun der zuletzt erfolgreich geladene Vertretungsplan angezeigt.\n" +
                                    "erschienen: " + prs.getPublishDateString() + "\n" +
                                    "heruntergeladen: " + prs.getParsedDateString(), true);
                            showVplanViews(
                                    prs.getVertretungsplanNewsString(day),
                                    prs.getRelatedDate(day),
                                    prs.getRelatedStunden(day),
                                    prs.getUnknownStunden(day), false);

                        } else if (resultType == PRS.PRSResultType.parseError) {
                            showMessageTextView("Fehler beim Laden des Vertretungsplans.\n" +
                                    "Es ist ein Fehler beim Analysieren des Vertretungsplans aufgetreten. " +
                                    "Bitte schaue auf den offiziellen Vertretungsplan.", false);
                            showVplanViews(
                                    prs.getVertretungsplanNewsString(day),
                                    prs.getRelatedDate(day),
                                    prs.getRelatedStunden(day),
                                    prs.getUnknownStunden(day), false);

                        } else if (resultType == PRS.PRSResultType.downloadAndStorrageError) {
                            showMessageTextView("Fehler beim Herunterladen des Vertretungsplans.\n" +
                                    "Es konnte keine Verbindung mit dem Server hergestellt werden.\n\n" +
                                    "Es konnte ebenso kein alter (gespeicherter) Vertretungsplan geladen werden.", true);
                        }
                    }
                });
            }
        });
        prs.downloadPRS();
    }

    public void showVplanViews(
            final String infoHtml,
            final Date dayDate,
            final List<VertretungsStunde> relatedStunden,
            final List<VertretungsStunde> unknownStunden,
            final boolean clearLayout) {
        LinearLayout lay = (LinearLayout) getActivity().findViewById(R.id.fragment_vertretungsplan_linearLayout_main);
        if (clearLayout)
            lay.removeAllViews();

        if (!StorageHelper.loadBooleanFromSharedPreferences(StorageHelper.VPLAN_COMPACT_NEWS, getActivity())) {
            VertretungsplanNewsView news = new VertretungsplanNewsView(day, dayDate, infoHtml, getActivity());
            lay.addView(news);
        }

        //Vertretungsstunden
        if (relatedStunden == null || relatedStunden.size() == 0) {
            String additionToString = "für die Schule";
            if (StorageHelper.loadBooleanFromSharedPreferences(StorageHelper.VPLAN_USER_KLASSE_FILTER, getActivity())) {
                additionToString = "für die " +
                        StorageHelper.loadStringFromSharedPreferences(StorageHelper.VPLAN_USER_KLASSE, getActivity());
            }
            showMessageTextView("Es wurden keine Vertretungsstunden " + additionToString + " gefunden.", false);
        } else {
            for (VertretungsStunde vStunde : relatedStunden) {
                VertretungsplanView viewStunde = new VertretungsplanView(vStunde, getActivity());
                lay.addView(viewStunde);
                FrameLayout line = new FrameLayout(getActivity());
                line.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 2));
                lay.addView(line);
            }
        }

        //Sonstige Stunden
        if (unknownStunden != null && unknownStunden.size() != 0) {
            showMessageTextView("Es folgen unbekannte Stunden, die zu keiner Klasse zugeordnet werden konnten:", false);
            for (VertretungsStunde vStunde : unknownStunden) {
                if (VertretungsStunde.isNullOrWhitespace(vStunde.getKlassenString())) {
                    VertretungsplanView viewStunde = new VertretungsplanView(vStunde, getActivity());
                    lay.addView(viewStunde);
                    FrameLayout line = new FrameLayout(getActivity());
                    line.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 2));
                    lay.addView(line);
                }
            }
        }
    }

    public void showMessageTextView(final String messg, final boolean clearLayout) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LinearLayout lay = (LinearLayout) getActivity().findViewById(R.id.fragment_vertretungsplan_linearLayout_main);
                if (clearLayout)
                    lay.removeAllViews();
                TextView txt = new TextView(getActivity());
                txt.setGravity(Gravity.CENTER);

                LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                llp.setMargins(50, 50, 50, 50); // llp.setMargins(left, top, right, bottom);
                txt.setLayoutParams(llp);

                txt.setText(messg);
                lay.addView(txt);
            }
        });
    }
}