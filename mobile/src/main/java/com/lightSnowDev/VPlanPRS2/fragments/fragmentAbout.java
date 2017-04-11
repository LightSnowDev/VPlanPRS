package com.lightSnowDev.VPlanPRS2.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lightSnowDev.VPlanPRS2.BuildConfig;
import com.lightSnowDev.VPlanPRS2.FirstStartActivity;
import com.lightSnowDev.VPlanPRS2.MainActivity;
import com.lightSnowDev.VPlanPRS2.R;

/**
 * Created by Jonathan on 29.08.2016.
 */

public class fragmentAbout extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        //Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_about_layout, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Über diese App");
        loadAbout();
    }

    private void loadAbout() {
        if (!MainActivity.checkIfFragmentIsActive(fragmentAbout.class, getActivity()))
            return;

        ((TextView) getView().findViewById(R.id.fragmentAbout_version_history)).setText(
                "Aktuelle Neuerungen:\n" +
                        FirstStartActivity.readFromAssetFile("news_new.txt", this.getActivity()).replace("\n\n", "\n") + "\n" +
                        FirstStartActivity.readFromAssetFile("news_old.txt", this.getActivity()).replace("\n\n", "\n")
        );
        ((TextView) getView().findViewById(R.id.fragmentAbout_version)).setText("Aktuelle Version: " +
                BuildConfig.VERSION_NAME + " Build: " + BuildConfig.VERSION_CODE);
    }
}
