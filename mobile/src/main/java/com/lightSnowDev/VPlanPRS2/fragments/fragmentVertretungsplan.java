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

import com.lightSnowDev.VPlanPRS2.MainActivity;
import com.lightSnowDev.VPlanPRS2.R;
import com.lightSnowDev.VPlanPRS2.classes.Vertretungsstunde;
import com.lightSnowDev.VPlanPRS2.helper.StorageHelper;
import com.lightSnowDev.VPlanPRS2.items.VertretungsplanView;

import java.util.List;

/**
 * Created by Jonathan Schwarzenböck on 05.01.2016.
 */
public class fragmentVertretungsplan extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_vertretungsplan_layout, container, false);
    }

    public void showVPlan() {
        MainActivity mainActivity = ((MainActivity) getActivity());
        String tagString = getArguments().getString("tag", "beide");
        Vertretungsstunde.Tag tag = Vertretungsstunde.getVertretungsstundeTagFromString(tagString);
        mainActivity.loadNewVPlan(tag);
    }

    @Override
    public void onResume() {
        super.onResume();
        String klassenname = "Schule";
        if (StorageHelper.loadBooleanFromSharedPreferences(StorageHelper.VPLAN_USER_KLASSE_FILTER, getActivity()))
            klassenname = StorageHelper.loadStringFromSharedPreferences(StorageHelper.VPLAN_USER_KLASSE, getActivity());
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getArguments().getString("tag", "? Tag ?") + " - " + klassenname);

        //load vplan stunden
        showVPlan();
    }

    public void showVplanViews(final List<Vertretungsstunde> relatedStunden, final List<Vertretungsstunde> unknownStunden, final boolean clearLayout) {
        if (!MainActivity.checkIfFragmentIsActive(fragmentVertretungsplan.class, getActivity()))
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LinearLayout lay = (LinearLayout) getActivity().findViewById(R.id.fragment_vertretungsplan_linearLayout_main);
                if (clearLayout)
                    lay.removeAllViews();
                //Vertretungsstunden
                if (relatedStunden == null || relatedStunden.size() == 0)
                    showMessageTextView("Es wurden keine Stunden gefunden.", false);
                for (Vertretungsstunde vStunde : relatedStunden) {
                    VertretungsplanView viewStunde = new VertretungsplanView(vStunde, getActivity());
                    lay.addView(viewStunde);
                    FrameLayout line = new FrameLayout(getActivity());
                    line.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 2));
                    lay.addView(line);
                }

                //Sonstige Stunden
                if (unknownStunden != null && unknownStunden.size() != 0)
                    showMessageTextView("Es folgen unbekannte Stunden, die zu keiner Klasse gehören:", false);
                for (Vertretungsstunde vStunde : unknownStunden) {
                    //show only unknown classes
                    if (Vertretungsstunde.isNullOrWhitespace(vStunde.getKlassenString())) {
                        VertretungsplanView viewStunde = new VertretungsplanView(vStunde, getActivity());
                        lay.addView(viewStunde);
                        FrameLayout line = new FrameLayout(getActivity());
                        line.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 2));
                        lay.addView(line);
                    }
                }
            }
        });
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