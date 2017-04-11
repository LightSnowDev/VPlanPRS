package com.lightSnowDev.VPlanPRS2.fragments;

import android.app.Fragment;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.google.firebase.crash.FirebaseCrash;
import com.lightSnowDev.VPlanPRS2.MainActivity;
import com.lightSnowDev.VPlanPRS2.R;
import com.lightSnowDev.VPlanPRS2.helper.DownloadHelper;
import com.lightSnowDev.VPlanPRS2.helper.StorageHelper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Jonathan on 04.03.2017.
 */

public class fragmentBusfahrplan extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        //Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_busfahrplan_layout, container, false);
        return v;
    }


    @Override
    public void onResume() {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Busfahrplan");
        downloadHtml();
        super.onResume();
    }


    private void downloadHtml() {
        DownloadHelper helper = new DownloadHelper(getActivity());
        helper.addOnRecievedEvent(new DownloadHelper.OnRecievedEvent() {
            @Override
            public void recievedEvent(String resultString, String resultBase64, boolean success) {
                if (!MainActivity.checkIfFragmentIsActive(fragmentBusfahrplan.class, getActivity()))
                    return;

                String result = resultString;
                if (success == false || result == null || result.isEmpty()) {
                    String savedHtml = loadHtmlFromStorrage();
                    if (savedHtml == null || savedHtml.isEmpty() || savedHtml.equals(""))
                        showWebView("Fehler: Der Busfahrplan konnte nicht geladen werden.<br>Wahrscheinlich existiert keine Internetverbindung.");
                    else
                        showWebView(savedHtml);
                } else
                    splitHtml(result);
            }
        });
        helper.FIXED_URL = "https://philipp-reis-schule.de/prs/plaene/busfahrplan";
        helper.setEncoding("ISO-8859-1");
        helper.download("/", null, null);
    }

    private void splitHtml(String htmlCodeInput) {
        String htmlCode = "";
        Document doc = Jsoup.parse(htmlCodeInput);
        Element content = doc.select("div.content").first();
        htmlCode = content.html().replace("<table width=\"100%\"", "<table width=\"100%\" style=\"display: none\"");

        boolean isOnline_ = isOnline();
        if (isOnline_)
            showWebViewWithBaseURL(htmlCode);
        downloadImages(htmlCode, !isOnline_);
    }

    private void showWebView(String html) {
        if (!MainActivity.checkIfFragmentIsActive(fragmentBusfahrplan.class, getActivity()))
            return;

        WebView webView = (WebView) getView().findViewById(R.id.fragmentBusfahrplan_webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.setInitialScale(1);
        webView.loadData(html, "text/html; charset=UTF-8", null);
    }

    private void showWebViewWithBaseURL(String inputHtml) {
        if (!MainActivity.checkIfFragmentIsActive(fragmentBusfahrplan.class, getActivity()))
            return;

        WebView webView = (WebView) getView().findViewById(R.id.fragmentBusfahrplan_webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.setInitialScale(1);

        webView.loadDataWithBaseURL("https://philipp-reis-schule.de/prs/plaene/busfahrplan", inputHtml, "text/html", "UTF-8", null);
    }

    private void downloadImages(String inputURL, final boolean loadIntoWebview) {
        //Connect to the website and get the html
        final Document doc = Jsoup.parse(inputURL.toString());
        //Get all elements with img tag
        Elements img = doc.getElementsByTag("img");
        final AtomicInteger imgCount = new AtomicInteger(img.size());
        for (final Element el : img) {
            //for each element get the src url
            final String src = el.attr("src");
            //Log.d("Image Source get", "src attribute is : " + src);

            DownloadHelper helper = new DownloadHelper(getActivity());
            helper.FIXED_URL = "";
            helper.addOnRecievedEvent(new DownloadHelper.OnRecievedEvent() {
                @Override
                public void recievedEvent(String resultString, String resultBase64, boolean success) {
                    imgCount.decrementAndGet();
                    //Log.d("Image Source result", result);
                    Elements x = doc.getElementsByAttributeValue("src", src);
                    if (success)
                        x.attr("src", "data:image/png;base64," + resultBase64);
                    if (imgCount.get() == 0) {
                        String finalHtml = doc.toString();
                        //Log.d("final html busfahrplan", finalHtml);
                        //show the html
                        if (loadIntoWebview)
                            showWebView(finalHtml);
                        //save the html
                        saveHtmlToStorrage(finalHtml);
                    }
                }
            });
            if (src.startsWith("http"))
                helper.download(src, null, null);
            else
                helper.download("https://philipp-reis-schule.de" + src, null, null);
        }
    }

    private void saveHtmlToStorrage(String inputHtml) {
        StorageHelper helper = new StorageHelper();
        StorageHelper.saveToSharedPreferences(StorageHelper.BUSPLAN_HTML, inputHtml, getActivity());
    }

    private String loadHtmlFromStorrage() {
        StorageHelper helper = new StorageHelper();
        return StorageHelper.loadStringFromSharedPreferences(StorageHelper.BUSPLAN_HTML, getActivity());
    }

    public boolean isOnline() {
        try {
            ConnectivityManager cm =
                    (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnectedOrConnecting();
        } catch (Exception e) {
            FirebaseCrash.report(e);
            return false;
        }
    }
}
