package com.lightSnow.VPlanPRS.fragments;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toolbar;

import com.lightSnow.VPlanPRS.R;

import java.util.Random;

/**
 * Created by Jonathan Schwarzenböck on 02.01.2016.
 */
public class fragmentStart extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        //Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_start_layout, container, false);
    }


    @Override
    public void onStart() {
        super.onStart();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Vertretungsplan PRS");
        setVPlanNumber(new Random().nextInt(8) + 1);
    }

    private void setVPlanNumber(final int input) {
        ((TextView) getView().findViewById(R.id.fragmentStart_Card_textView_number)).setText(String.valueOf(input));
        CardView card = (CardView) getView().findViewById(R.id.fragmentStart_CardVertretungsstunden);
        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment newFragment = null;
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, new fragmentVertretungsplan());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
    }

}