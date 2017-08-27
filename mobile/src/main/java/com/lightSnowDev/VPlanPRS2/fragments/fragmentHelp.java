package com.lightSnowDev.VPlanPRS2.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lightSnowDev.VPlanPRS2.R;

/**
 * Created by Jonathan on 01.09.2016.
 */

public class fragmentHelp extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        //Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_help_layout, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Hilfe & Fragen");
        ((TextView) getActivity().findViewById(R.id.fragment_help_impressum_textView)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://lightsnowdev.com/impressum_ger.html"));
                startActivity(browserIntent);
            }
        });

        ((TextView) getActivity().findViewById(R.id.fragment_help_email_adress_textView)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* Create the Intent */
                Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

                /* Fill it with Data */
                emailIntent.setType("plain/text");
                emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"jonathan@lightsnowdev.com"});
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Frage PRS App");
                emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");

                /* Send it off to the Activity-Chooser */
                getActivity().startActivity(Intent.createChooser(emailIntent, "Sende e-mail..."));
            }
        });
    }
}
