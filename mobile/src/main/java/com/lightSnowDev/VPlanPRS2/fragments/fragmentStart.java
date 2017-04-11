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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.crash.FirebaseCrash;
import com.lightSnowDev.VPlanPRS2.BuildConfig;
import com.lightSnowDev.VPlanPRS2.FirstStartActivity;
import com.lightSnowDev.VPlanPRS2.MainActivity;
import com.lightSnowDev.VPlanPRS2.R;
import com.lightSnowDev.VPlanPRS2.classes.PRS;
import com.lightSnowDev.VPlanPRS2.classes.Vertretungsstunde;
import com.lightSnowDev.VPlanPRS2.helper.DownloadHelper;
import com.lightSnowDev.VPlanPRS2.helper.SensibleDataHelper;
import com.lightSnowDev.VPlanPRS2.helper.StorageHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Jonathan Schwarzenböck on 02.01.2016.
 */
public class fragmentStart extends Fragment {

    private View mainView = null;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        //Inflate the layout for this fragment
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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Vertretungsplan PRS");

        // onResume has to be finished to draw the GUI. Since we need a drawn GUI to determine weather our fragment is visible we use an event:
        // post is called if the view is drawn, so we prevent a not-visible-collision even tough the fragment is indeed visible.
        mainView.post(new Runnable() {
            @Override
            public void run() {
                // code you want to run when view is visible for the first time
                loadVPlanOnStart();
                loadSVNews();
                loadUpdateNews();
                loadRickNRoll();
            }
        });

    }

    private void setAndSaveWebViewHTML(String htmlString, boolean save) {
        WebView webView = new WebView(getActivity());
        /*
        webView.clearCache(true);
        webView.clearView();
        webView.reload();
        webView.getSettings().setAppCacheEnabled(false);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        */
        webView.loadData(htmlString, "text/html; charset=utf-8", "UTF-8");
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        //disable loading bar
        mainView.findViewById(R.id.fragmentStart_CardSV_progressBar).setVisibility(View.GONE);
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

    private void setVPlanNumber(final int input) {
        ((TextView) mainView.findViewById(R.id.fragmentStart_Card_textView_number)).setText(String.valueOf(input));
        CardView card = (CardView) mainView.findViewById(R.id.fragmentStart_CardVertretungsstunden);
        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).setCustomFragmentandDrawer(1);
            }
        });
    }

    private void loadVPlanOnStart() {
        PRS prs = new PRS(getActivity());
        prs.setTagToFilter(Vertretungsstunde.Tag.heute);
        prs.addOnPRSResultEvent(new PRS.OnPRSResultEvent() {
            @Override
            public void PRSResultEvent(final List<Vertretungsstunde> relatedStunden, List<Vertretungsstunde> unknownStunden,
                                       boolean changedKlassenSpecificData, final PRS.PRSResultType resultType) {
                try {
                    if (!MainActivity.checkIfFragmentIsActive(fragmentStart.class, getActivity()))
                        return;

                    MainActivity mA = (MainActivity) getActivity();
                    ProgressBar progressBar = (ProgressBar) mainView.findViewById(R.id.fragmentStart_Card_progressBar);
                    if (progressBar == null) {
                        try {
                            Fragment myFragment = getFragmentManager().findFragmentByTag("fragment");
                            FirebaseCrash.log(
                                    "ProgressBar is null. fragmentTag:" +
                                            myFragment.getTag() +
                                            " toString:" + myFragment.toString() +
                                            " resultType:" + resultType.toString()
                            );
                            Toast.makeText(getActivity(), "Fehler beim Laden des Vertretungsplans.\nBitte App neu starten.", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            FirebaseCrash.report(e);
                        }
                        return;
                    }

                    progressBar.setVisibility(View.GONE);
                    if (resultType == PRS.PRSResultType.success) {
                        setVPlanNumber(relatedStunden.size());
                        ((TextView) mainView.findViewById(R.id.fragmentStart_Card_textView_aktualisierung)).setText("V-Plan vom: " +
                                StorageHelper.loadStringFromSharedPreferences(StorageHelper.VPLAN_LAST_UPDATE_PARSED, getActivity()));
                    } else if (resultType == PRS.PRSResultType.successButStorrage) {
                        setVPlanNumber(relatedStunden.size());
                        ((TextView) mainView.findViewById(R.id.fragmentStart_Card_textView_aktualisierung)).setText("Es konnte kein aktueller Vertretungsplan heruntergeladen werden, da keine Internetverbindung existiert. Lade letzte gespeicherte Version vom:\n" +
                                StorageHelper.loadStringFromSharedPreferences(StorageHelper.VPLAN_LAST_UPDATE_PARSED, getActivity()));
                    } else /*if (resultType == PRS.PRSResultType.downloadAndStorrageError || resultType == PRS.PRSResultType.parseError)*/ {
                        setVPlanNumber(0);
                        ((TextView) mainView.findViewById(R.id.fragmentStart_Card_textView_aktualisierung)).setText("Es konnte kein aktueller Vertretungsplan heruntergeladen werden. Es wurde keine gespeicherte Version gefunden.");
                    }


                } catch (Exception e) {
                    FirebaseCrash.report(e);
                }
            }
        });
        prs.downloadLinks();
    }

    private void loadSVNews() {
        DownloadHelper downloadHelper = new DownloadHelper(getActivity());
        downloadHelper.setIfProgressDialogIsShown(false);
        downloadHelper.FIXED_URL = SensibleDataHelper.PRIVATE_SERVER_PATH_ROOT_DIR;
        downloadHelper.addOnRecievedEvent(new DownloadHelper.OnRecievedEvent() {
            @Override
            public void recievedEvent(String resultString, String resultBase64, boolean success) {
                Log.d("result fStart", String.valueOf(success));
                try {
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
                        Log.d("result fStart oldDate", oldDate);
                        Log.d("result fStart oldHTML", oldHTML);
                        if (!oldDate.isEmpty() && !oldHTML.isEmpty()) {
                            setAndSaveSVDate(oldDate, false);
                            setAndSaveWebViewHTML(oldHTML, false);
                        } else {
                            setAndSaveSVDate("??", false);
                            setAndSaveWebViewHTML("Nachrichten konnten nicht geladen werden.", false);
                        }
                    }
                } catch (Exception e) {
                    FirebaseCrash.log(e.getStackTrace().toString());
                }
            }
        });
        downloadHelper.download(SensibleDataHelper.PRIVATE_SERVER_PATH_SV_NEWS_DIR, SensibleDataHelper.PRIVATE_SERVER_USERNAME, SensibleDataHelper.PRIVATE_SERVER_PASSWORD);
    }

    private void setDateText(String input) {
        try {
            SimpleDateFormat dateParser = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.GERMANY);
            Date date = dateParser.parse(input);
            // Then convert the Date to a String, formatted as you dd/MM/yyyy
            SimpleDateFormat dateFormatter = new SimpleDateFormat("d MMMM");
            setAndSaveSVDate(dateFormatter.format(date), true);
        } catch (Exception e) {
            ((TextView) mainView.findViewById(R.id.fragmentStart_CardSV_textView_date)).setText("Letzte Nachricht vom: ??");
        }
    }

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
                ((TextView) mainView.findViewById(R.id.fragmentStart_update_news_textView_version)).setText("Update auf Version " + BuildConfig.VERSION_NAME);
                ((TextView) mainView.findViewById(R.id.fragmentStart_update_news_textView_text)).setText(
                        FirstStartActivity.readFromAssetFile("news_new.txt", this.getActivity()) +
                                "\nDu findest diese Informationen auch unter 'Über diese App' im Menü."
                );
                mainView.findViewById(R.id.ImageView_update_news_closeIcon).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LayoutTransition layoutTransition = new LayoutTransition();
                        layout.setLayoutTransition(layoutTransition);
                        StorageHelper.saveToSharedPreferences(StorageHelper.VPLAN_LAST_APP_NAME_VERSION, BuildConfig.VERSION_NAME, getActivity());
                        mainView.findViewById(R.id.fragmentStart_CardUpdate_news).setVisibility(View.GONE);
                    }
                });
            } catch (Exception e) {
                FirebaseCrash.report(e);
            }
        }
    }

    private void loadRickNRoll() {
        if (!MainActivity.checkIfFragmentIsActive(fragmentStart.class, getActivity()))
            return;

        DateFormat dateFormat = new SimpleDateFormat("MM/dd");
        String currentDate = dateFormat.format(new Date());
        String april = "04/01";
        //Toast.makeText(getActivity(),april +" | " +currentDate,Toast.LENGTH_LONG).show();
        if (!april.equals(currentDate) || !DownloadHelper.isNetworkAvailable(getActivity()))
            return;

        final CardView c = (CardView) mainView.findViewById(R.id.fragmentStart_CardRickNRoll);
        c.setVisibility(View.VISIBLE);
        final Button b = (Button) mainView.findViewById(R.id.Button_RickNRoll);
        final TextView t = (TextView) mainView.findViewById(R.id.TextView_rickNRoll);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                b.setVisibility(View.GONE);

                t.setText("APRIL APRIL!1!");

                final WebView webView = (WebView) mainView.findViewById(R.id.webView_rickNRoll);
                webView.setVisibility(View.VISIBLE);
                webView.getSettings().setJavaScriptEnabled(true);
                int SDK_INT = android.os.Build.VERSION.SDK_INT;
                if (SDK_INT > 16) {
                    webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
                }
                webView.loadUrl("https://app:9WEFGbF2QWyn6@lightsnowdev.com/gcm/public/video.html");
                //webView.performClick();
            }
        });


    }
}