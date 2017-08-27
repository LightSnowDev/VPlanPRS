package com.lightSnowDev.VPlanPRS2.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.lightSnowDev.VPlanPRS2.MainActivity;
import com.lightSnowDev.VPlanPRS2.R;
import com.lightSnowDev.VPlanPRS2.classes.PRS;
import com.lightSnowDev.VPlanPRS2.classes.VertretungsTag;
import com.lightSnowDev.VPlanPRS2.classes.VertretungsplanNews;
import com.lightSnowDev.VPlanPRS2.helper.StorageHelper;
import com.lightSnowDev.VPlanPRS2.items.VertretungsplanNewsView;

/**
 * Created by Jonathan on 29.08.2016.
 */

public class fragmentDebug extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        //Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_debug_layout, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("DEBUG MENÜ");
        init();
    }

    private void init() {
        if (!MainActivity.checkIfFragmentIsActive(fragmentDebug.class, getActivity()))
            return;

        addButton("Zeige SharedPreferences VPlan", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sP = StorageHelper.loadStringFromSharedPreferences(StorageHelper.VPLAN_LIST, getActivity());
                if (sP == null)
                    sP = "Fehler: VPLAN_LIST ist null";
                showMessageBox(sP);
            }
        });

        addButton("Speicher Scheiße inm SharedPreferences VPlan", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StorageHelper.saveToSharedPreferences(StorageHelper.VPLAN_LIST,"lalalalalallaallalalalaalal",getActivity());
            }
        });

        addButton("Erstelle VPlanNews", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PRS prs = new PRS(getActivity());
                prs.setIfProgressDialogIsShown(true);
                prs.addOnPRSResultEvent(new PRS.OnPRSResultEvent() {
                    @Override
                    public void PRSResultEvent(PRS prs, PRS.PRSResultType resultType) {
                        try {
                            VertretungsTag.Day date = VertretungsTag.Day.heute;
                            String html = prs.getVertretungsplanNewsString(date);
                            VertretungsplanNewsView news =
                                    new VertretungsplanNewsView(date, prs.getPublishDate(), html, getActivity());
                            ((LinearLayout) getActivity().findViewById(R.id.fragment_debug_mainLinearLayout)).addView(news);
                        } catch (Exception e) {
                            Log.d("DEBUG MENU", e.getMessage());
                        }
                    }
                });
                prs.downloadPRS();
            }
        });

        addButton("Ist Vplan gleich geblieben?", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    PRS prs = PRS.loadPRSFromStorrage("", getActivity());
                    showMessageBox(PRS.checkIfVPlanIsEqual(prs) ? "true" : "false");
                } catch (Exception e) {
                    showMessageBox(e.getMessage());
                }
            }
        });

    }

    private void addButton(String name, View.OnClickListener listener) {
        Button b = new Button(getActivity());
        b.setText(name);
        b.setOnClickListener(listener);
        ((LinearLayout) getActivity().findViewById(R.id.fragment_debug_mainLinearLayout)).addView(b);

    }

    private void showMessageBox(String message) {
        new MaterialDialog.Builder(getActivity())
                .title("Debug result")
                .content(message)
                .show();
    }
}
