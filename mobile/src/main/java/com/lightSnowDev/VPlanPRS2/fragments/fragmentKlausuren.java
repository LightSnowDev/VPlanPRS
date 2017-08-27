package com.lightSnowDev.VPlanPRS2.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lightSnowDev.VPlanPRS2.R;
import com.lightSnowDev.VPlanPRS2.helper.StorageHelper;

/**
 * Created by Jonathan on 26.06.2017.
 */

public class fragmentKlausuren extends Fragment{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_vertretungsplan_layout, container, false);
    }


    @Override
    public void onResume() {
        super.onResume();
        String klassenname = "Schule";
        if (StorageHelper.loadBooleanFromSharedPreferences(StorageHelper.VPLAN_USER_KLASSE_FILTER, getActivity()))
            klassenname = StorageHelper.loadStringFromSharedPreferences(StorageHelper.VPLAN_USER_KLASSE, getActivity());
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Klausuren");
    }

}
