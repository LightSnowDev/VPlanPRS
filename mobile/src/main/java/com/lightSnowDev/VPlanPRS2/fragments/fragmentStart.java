package com.lightSnowDev.VPlanPRS2.fragments;

import android.animation.LayoutTransition;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.crash.FirebaseCrash;
import com.lightSnowDev.VPlanPRS2.BuildConfig;
import com.lightSnowDev.VPlanPRS2.MainActivity;
import com.lightSnowDev.VPlanPRS2.R;
import com.lightSnowDev.VPlanPRS2.classes.PRS;
import com.lightSnowDev.VPlanPRS2.classes.VertretungsTag;
import com.lightSnowDev.VPlanPRS2.helper.DownloadHelper;
import com.lightSnowDev.VPlanPRS2.helper.SensibleDataHelper;
import com.lightSnowDev.VPlanPRS2.helper.StorageHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.view.View.GONE;

/**
 * Created by Jonathan Schwarzenböck on 02.01.2016.
 */
public class fragmentStart extends Fragment {

    private View mainView = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_start_layout, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getView() != null)
            mainView = getView();
        else if (getActivity() != null && getActivity().findViewById(R.id.TopofTheTops) != null)
            mainView = getActivity().findViewById(R.id.TopofTheTops);
        else
            return;
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Startseite");

        // onResume has to be finished to draw the GUI. Since we need a drawn GUI to determine weather our fragment is visible we use an event:
        // post is called if the view is drawn, so we prevent a not-visible-collision even tough the fragment is indeed visible.
        mainView.post(new Runnable() {
            @Override
            public void run() {
                // code you want to run when view is visible for the first time
                loadVPlanOnStart();
                loadSVNews();
                loadUpdateNews();
                initVPlanControls();
                loadRickNRoll();
            }
        });

    }

    private void setAndSaveWebViewHTML(String htmlString, boolean save) {
        WebView webView = new WebView(getActivity());
        webView.loadData(htmlString, "text/html; charset=utf-8", "UTF-8");
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        //disable loading bar
        mainView.findViewById(R.id.fragmentStart_CardSV_progressBar).setVisibility(GONE);
        //disable scroll input
        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return (event.getAction() == MotionEvent.ACTION_MOVE);
            }
        });
        if (((ScrollView) mainView.findViewById(R.id.fragmentStart_CardSV_scrollView)).getChildCount() == 0) {
            ((ScrollView) mainView.findViewById(R.id.fragmentStart_CardSV_scrollView)).removeAllViews();
            ((ScrollView) mainView.findViewById(R.id.fragmentStart_CardSV_scrollView)).addView(webView);
        }
        webView.setVisibility(View.VISIBLE);
        if (save)
            StorageHelper.saveToSharedPreferences(StorageHelper.VPLAN_SV_HTML, htmlString, getActivity());
    }

    private void setAndSaveSVDate(String date, boolean save) {
        ((TextView) mainView.findViewById(R.id.fragmentStart_CardSV_textView_date)).setText("Letzte Nachricht vom: " + date);
        if (save)
            StorageHelper.saveToSharedPreferences(StorageHelper.VPLAN_SV_LAST_UPDATE, date, getActivity());
    }

    private void initVPlanControls() {
        if (!MainActivity.checkIfFragmentIsActive(fragmentStart.class, getActivity()))
            return;

        if (StorageHelper.loadBooleanFromSharedPreferences(StorageHelper.VPLAN_USER_KLASSE_FILTER, getActivity()))
            ((TextView) getActivity().findViewById(R.id.vplanCard_headlineText)).setText("Vertretungsplan der " +
                    StorageHelper.loadStringFromSharedPreferences(StorageHelper.VPLAN_USER_KLASSE, getActivity()));

        RelativeLayout card_heute = (RelativeLayout) mainView.findViewById(R.id.fragmentStart_CardVPlan_heute);
        card_heute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).setCustomFragmentandDrawer(2);
            }
        });
        RelativeLayout card_morgen = (RelativeLayout) mainView.findViewById(R.id.fragmentStart_CardVPlan_morgen);
        card_morgen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).setCustomFragmentandDrawer(3);
            }
        });
    }

    private void loadVPlanOnStart() {
        PRS prs = new PRS(getActivity());
        if (StorageHelper.loadBooleanFromSharedPreferences(StorageHelper.VPLAN_USER_KLASSE_FILTER, getActivity()))
            prs.setKlasseToFilter(StorageHelper.loadStringFromSharedPreferences(StorageHelper.VPLAN_USER_KLASSE, getActivity()));
        prs.addOnPRSResultEvent(new PRS.OnPRSResultEvent() {
            @Override
            public void PRSResultEvent(final PRS prs, final PRS.PRSResultType resultType) {
                if (!MainActivity.checkIfFragmentIsActive(fragmentStart.class, getActivity()))
                    return;

                if (resultType == PRS.PRSResultType.success) {
                    setHeuteMorgenNumber(prs.getRelatedStunden(VertretungsTag.Day.heute).size(),
                            prs.getRelatedStunden(VertretungsTag.Day.morgen).size());
                    ((TextView) mainView.findViewById(R.id.vplanCard_mainText)).setText(
                            "V-Plan vom: " + prs.getPublishDateString());

                } else if (resultType == PRS.PRSResultType.successButStorrage) {
                    setHeuteMorgenNumber(prs.getRelatedStunden(VertretungsTag.Day.heute).size(),
                            prs.getRelatedStunden(VertretungsTag.Day.morgen).size());
                    ((TextView) mainView.findViewById(R.id.vplanCard_mainText)).setText(
                            "Gespeicherter V-Plan vom: " + prs.getPublishDateString());

                } else /* downloadAndStorrageError || parseError) */ {
                    ((TextView) mainView.findViewById(R.id.vplanCard_mainText)).setText(
                            "V-Plan konnte nicht geladen werden.");
                    setHeuteMorgenString("?", "?");
                    //"Es konnte kein aktueller Vertretungsplan heruntergeladen werden. " +
                    //"Es wurde keine gespeicherte Version gefunden.");
                }
            }
        });
        prs.downloadPRS();
    }

    private void loadSVNews() {
        DownloadHelper downloadHelper = new DownloadHelper(getActivity());
        downloadHelper.setIfProgressDialogIsShown(false);
        downloadHelper.FIXED_URL = SensibleDataHelper.PRIVATE_SERVER_PATH_ROOT_DIR;
        downloadHelper.addOnRecievedEvent(new DownloadHelper.OnRecievedEvent() {
            @Override
            public void recievedEvent(String resultString, String resultBase64, boolean success) {
                if (!MainActivity.checkIfFragmentIsActive(fragmentStart.class, getActivity()))
                    return;

                if (success) {
                    setAndSaveWebViewHTML(resultString, true);
                    DownloadHelper downloadHelper = new DownloadHelper(getActivity());
                    downloadHelper.addOnRecievedEvent(new DownloadHelper.OnRecievedEvent() {
                        @Override
                        public void recievedEvent(String resultString, String resultBase64, boolean success) {
                            if (!MainActivity.checkIfFragmentIsActive(fragmentStart.class, getActivity()))
                                return;

                            if (success) {
                                setDateText(resultString);
                            } else {
                                setAndSaveSVDate("??", false);
                            }
                        }
                    });
                    downloadHelper.setIfProgressDialogIsShown(false);
                    downloadHelper.FIXED_URL = SensibleDataHelper.PRIVATE_SERVER_PATH_ROOT_DIR;
                    downloadHelper.download(SensibleDataHelper.PRIVATE_SERVER_PATH_SV_NEWS_DATE_DIR, SensibleDataHelper.PRIVATE_SERVER_USERNAME, SensibleDataHelper.PRIVATE_SERVER_PASSWORD);
                } else {
                    //try to laod old information
                    String oldDate = StorageHelper.loadStringFromSharedPreferences(StorageHelper.VPLAN_SV_LAST_UPDATE, getActivity());
                    String oldHTML = StorageHelper.loadStringFromSharedPreferences(StorageHelper.VPLAN_SV_HTML, getActivity());
                    if (!oldDate.isEmpty() && !oldHTML.isEmpty()) {
                        setAndSaveSVDate(oldDate, false);
                        setAndSaveWebViewHTML(oldHTML, false);
                    } else {
                        setAndSaveSVDate("??", false);
                        setAndSaveWebViewHTML("Nachrichten konnten nicht geladen werden.", false);
                    }
                }
            }
        });
        downloadHelper.download(SensibleDataHelper.PRIVATE_SERVER_PATH_SV_NEWS_DIR, SensibleDataHelper.PRIVATE_SERVER_USERNAME, SensibleDataHelper.PRIVATE_SERVER_PASSWORD);
    }

    private void setDateText(String input) {
        try {
            SimpleDateFormat dateParser = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.GERMANY);
            Date date = dateParser.parse(input);
            SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.YYYY", Locale.GERMANY);
            setAndSaveSVDate(dateFormatter.format(date), true);
        } catch (Exception e) {
            ((TextView) mainView.findViewById(R.id.fragmentStart_CardSV_textView_date)).setText("Letzte Nachricht vom: ??");
        }
    }

    /**
     * Überprüfe, ob die App geupdated wurde.
     * Zeige dann den Changelog als Karte im LinearLayout an.
     */
    private void loadUpdateNews() {
        if (!MainActivity.checkIfFragmentIsActive(fragmentStart.class, getActivity()))
            return;

        //disable animations, since the 'gone' to 'visible' animation is ugly. We only use it the other way around.
        final LinearLayout layout = (LinearLayout) mainView.findViewById(R.id.fragmentStart_mainLinearLayout);
        layout.setLayoutTransition(null);

        String lastVersionName = StorageHelper.loadStringFromSharedPreferences(StorageHelper.VPLAN_LAST_APP_NAME_VERSION, getActivity());
        String currentVersionName = BuildConfig.VERSION_NAME;

        if (!lastVersionName.equals(currentVersionName)) {
            try {
                CardView c = ((CardView) mainView.findViewById(R.id.fragmentStart_CardUpdate_news));

                if (c == null)
                    return;
                c.setVisibility(View.VISIBLE);
                ((TextView) mainView.findViewById(R.id.fragmentStart_update_news_textView_version)).setText(
                        "Update auf Version " + BuildConfig.VERSION_NAME);
                ((TextView) mainView.findViewById(R.id.fragmentStart_update_news_textView_text)).setText(
                        StorageHelper.readFromAssetFile("news_new.txt", this.getActivity()) +
                                "\nDu findest diese Informationen auch unter 'Über diese App' im Menü."
                );
                mainView.findViewById(R.id.ImageView_update_news_closeIcon).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LayoutTransition layoutTransition = new LayoutTransition();
                        layout.setLayoutTransition(layoutTransition);
                        StorageHelper.saveToSharedPreferences(
                                StorageHelper.VPLAN_LAST_APP_NAME_VERSION, BuildConfig.VERSION_NAME, getActivity());
                        mainView.findViewById(R.id.fragmentStart_CardUpdate_news).setVisibility(GONE);
                    }
                });
            } catch (Exception e) {
                FirebaseCrash.report(e);
            }
        }
    }

    /**
     * Aprilscherz erscheint nur am 1. April und lädt ein Video als Karte im LinearLayout.
     */
    private void loadRickNRoll() {
        if (!MainActivity.checkIfFragmentIsActive(fragmentStart.class, getActivity()))
            return;

        DateFormat dateFormat = new SimpleDateFormat("dd.MM", Locale.GERMANY);
        String currentDate = dateFormat.format(new Date());
        String april = "01.04";
        if (!april.equals(currentDate) || !DownloadHelper.isNetworkAvailable(getActivity()))
            return;

        CardView c = (CardView) mainView.findViewById(R.id.fragmentStart_CardRickNRoll);
        c.setVisibility(View.VISIBLE);
        final Button b = (Button) mainView.findViewById(R.id.Button_RickNRoll);
        final TextView t = (TextView) mainView.findViewById(R.id.TextView_rickNRoll);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                b.setVisibility(GONE);
                t.setText("APRIL APRIL!1!");

                WebView webView = (WebView) mainView.findViewById(R.id.webView_rickNRoll);
                webView.setVisibility(View.VISIBLE);
                webView.getSettings().setJavaScriptEnabled(true);
                int SDK_INT = android.os.Build.VERSION.SDK_INT;
                if (SDK_INT > 16) {
                    webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
                }
                webView.loadUrl(SensibleDataHelper.PRIVATE_SERVER_APRIL_VIDEO_URL);
            }
        });
    }

    public void setHeuteMorgenNumber(int i, int j) {
        try {
            ((TextView) mainView.findViewById(R.id.fragmentStart_CardVPlan_heuteText)).setText(String.valueOf(i));
            ((TextView) mainView.findViewById(R.id.fragmentStart_CardVPlan_morgenText)).setText(String.valueOf(j));
            (mainView.findViewById(R.id.fragmentStart_CardVPlan_heuteProgressBar)).setVisibility(GONE);
            (mainView.findViewById(R.id.fragmentStart_CardVPlan_morgenProgressBar)).setVisibility(GONE);
            (mainView.findViewById(R.id.fragmentStart_CardVPlan_heuteText)).setVisibility(View.VISIBLE);
            (mainView.findViewById(R.id.fragmentStart_CardVPlan_morgenText)).setVisibility(View.VISIBLE);
        } catch (Exception e) {
            setHeuteMorgenString("?", "?");
        }
    }

    public void setHeuteMorgenString(String heute, String morgen) {
        ((TextView) mainView.findViewById(R.id.fragmentStart_CardVPlan_heuteText)).setText(heute);
        ((TextView) mainView.findViewById(R.id.fragmentStart_CardVPlan_morgenText)).setText(morgen);
        (mainView.findViewById(R.id.fragmentStart_CardVPlan_heuteProgressBar)).setVisibility(GONE);
        (mainView.findViewById(R.id.fragmentStart_CardVPlan_morgenProgressBar)).setVisibility(GONE);
        (mainView.findViewById(R.id.fragmentStart_CardVPlan_heuteText)).setVisibility(View.VISIBLE);
        (mainView.findViewById(R.id.fragmentStart_CardVPlan_morgenText)).setVisibility(View.VISIBLE);
    }
}