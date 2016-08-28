package com.lightSnow.VPlanPRS.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.lightSnow.VPlanPRS.DividerItemDecoration;
import com.lightSnow.VPlanPRS.MainActivity;
import com.lightSnow.VPlanPRS.R;
import com.lightSnow.VPlanPRS.classes.Vertretungsstunde;
import com.lightSnow.VPlanPRS.helper.StorageHelper;
import com.lightSnow.VPlanPRS.items.VertretungsplanView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jonathan on 05.01.2016.
 */
public class fragmentVertretungsplan extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        //Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_vertretungsplan_layout, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Vertretungsplan");
        //load the current saved vplans
        String allVString = StorageHelper.loadStringFromSharedPreferences(StorageHelper.VPLAN_LIST, getActivity());

    }

    public void showVplanViews(final List<Vertretungsstunde> vStunden) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LinearLayout lay = (LinearLayout) getActivity().findViewById(R.id.fragment_vertretungsplan_linearLayout_main);
                //lay.removeAllViews();
                for (Vertretungsstunde vStunde : vStunden) {
                    VertretungsplanView viewStunde = new VertretungsplanView(vStunde, getActivity());
                    lay.addView(viewStunde);
                    FrameLayout line = new FrameLayout(getActivity());
                    line.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 2));
                    lay.addView(line);
                }
            }
        });
    }

    public void showVplanViews(String input) {
        LinearLayout lay = (LinearLayout) getActivity().findViewById(R.id.fragment_vertretungsplan_linearLayout_main);
        String[] vArray = input.split("|");
        for (String vString : vArray) {
            Vertretungsstunde vClass = new Vertretungsstunde(vString);
            VertretungsplanView vView = new VertretungsplanView(vClass, getActivity());
            lay.addView(vView);
            FrameLayout line = new FrameLayout(getActivity());
            line.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 2));
            lay.addView(line);
        }
    }
}