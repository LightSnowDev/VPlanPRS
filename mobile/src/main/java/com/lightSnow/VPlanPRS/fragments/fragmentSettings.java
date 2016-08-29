package com.lightSnow.VPlanPRS.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toolbar;

import com.lightSnow.VPlanPRS.R;
import com.lightSnow.VPlanPRS.classes.Vertretungsstunde;
import com.lightSnow.VPlanPRS.helper.StorageHelper;
import com.lightSnow.VPlanPRS.items.SettingView;
import com.lightSnow.VPlanPRS.items.VertretungsplanView;

/**
 * Created by Jonathan Schwarzenböck on 05.01.2016.
 */
public class fragmentSettings  extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        //Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_settings_layout, container, false);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Einstellungen");
        LinearLayout lay = (LinearLayout) getView().findViewById(R.id.linearLayout_settings_main);

        SettingView s1 = new SettingView(StorageHelper.VPLAN_USER_AUTO_UPDATE,getActivity());
        s1.setMainText("Automatisch aktualieren");
        s1.setSecondText("Soll automatisch nach neuen Vertretungsstunden gesucht werden?");
        lay.addView(s1);

        SettingView s2 = new SettingView(StorageHelper.VPLAN_USER_KLASSE_FILTER,getActivity());
        s2.setMainText("Nach Klasse filtern");
        s2.setSecondText("Soll der Vertretungsplan nach deiner Klasse durchsucht werden, oder sollen alle Klassen angezeigt werden?");
        lay.addView(s2);
    }
}