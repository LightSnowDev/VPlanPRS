package com.lightSnowDev.VPlanPRS2.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.lightSnowDev.VPlanPRS2.MainActivity;
import com.lightSnowDev.VPlanPRS2.R;
import com.lightSnowDev.VPlanPRS2.helper.StorageHelper;

/**
 * Created by Jonathan Schwarzenböck on 01.09.2016.
 */

public class fragmentStundenplan extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        //Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_stundenplan_layout, container, false);
    }

    /*
     * Durch die onResume funktion wird der Stundenplan jedes mal neu geladen, wenn er aufgeruden wird.
     * Das ist so beabsichtigt, da er sich auch mal schnell ändern kann.
     */
    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Stundenplan " +
                StorageHelper.loadStringFromSharedPreferences(StorageHelper.VPLAN_USER_KLASSE, getActivity()));

        WebView webView = (WebView) getView().findViewById(R.id.fragmentStundenplan_webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.setInitialScale(1);
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                if (!MainActivity.checkIfFragmentIsActive(fragmentStundenplan.class, getActivity()))
                    return;

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Fehler");
                builder.setMessage("Der Stundenplan konnte nicht geladen werden.");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                builder.show();

                super.onReceivedError(view, errorCode, description, failingUrl);
            }
        });
        webView.loadUrl("https://philipp-reis-schule.de/download/klassenplaene/Kla1_" +
                StorageHelper.loadStringFromSharedPreferences(StorageHelper.VPLAN_USER_KLASSE, getActivity()) +
                ".htm");
    }
}
