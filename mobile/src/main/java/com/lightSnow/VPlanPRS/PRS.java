package com.lightSnow.VPlanPRS;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import com.lightSnow.VPlanPRS.classes.Vertretungsstunde;
import com.lightSnow.VPlanPRS.helper.SensibleDataHelper;
import com.lightSnow.VPlanPRS.helper.StorageHelper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Jonathan Schwarzenböck on 24.08.2016.
 */
public class PRS {

    private ProgressDialog progressDialog;
    private boolean showprogressDialogBoolean = false;

    Context activity;

    String filterKlasse = "";

    public PRS(Context context) {
        activity = context;
    }

    public void downloadLinks() {
        if (showprogressDialogBoolean)
            showProgressDialog(activity);
        com.lightSnow.VPlanPRS.DownloadHelper download = new com.lightSnow.VPlanPRS.DownloadHelper(activity);
        download.setIfProgressDialogIsShown(false);
        download.addOnRecievedEvent(new com.lightSnow.VPlanPRS.DownloadHelper.OnRecievedEvent() {
            @Override
            public void recievedEvent(String result, boolean success) {
                if (success)
                    parseLinks(result);
                else
                    RunVPlanResultEvent(null, false, false);
            }
        });
        HashMap loginData = new HashMap();
        loginData.put("name", "vplan");
        loginData.put("password", "2011");
        download.download("links.htm", "vplan", "2011");
    }

    private void parseLinks(String input) {
        Document doc = Jsoup.parse(input);
        //save all the vertretungsstunden in a list
        Elements elements_vplan = doc.select("[target=right]");
        final List<Vertretungsstunde> vStunden = new ArrayList<>();
        final AtomicInteger counter = new AtomicInteger(elements_vplan.size());
        for (Element e : elements_vplan) {
            String currentLink = e.attr("href");

            //download content of the links
            com.lightSnow.VPlanPRS.DownloadHelper download = new com.lightSnow.VPlanPRS.DownloadHelper(activity);
            download.setIfProgressDialogIsShown(false);
            download.addOnRecievedEvent(new com.lightSnow.VPlanPRS.DownloadHelper.OnRecievedEvent() {
                @Override
                public void recievedEvent(String result, boolean success) {
                    if (success) {
                        String savedklasse = StorageHelper.loadStringFromSharedPreferences(StorageHelper.VPLAN_USER_KLASSE, activity);
                        for (Vertretungsstunde vStunde : parseSingleVPlan(result)) {
                            //filter the Klassen
                            if (StorageHelper.loadBooleanFromSharedPreferences(StorageHelper.VPLAN_USER_KLASSE_FILTER, activity)) {
                                if (vStunde.matchesKlasse(savedklasse))
                                    vStunden.add(vStunde);
                            } else {
                                vStunden.add(vStunde);
                            }
                        }
                    } else
                        RunVPlanResultEvent(null, false, false);
                    counter.decrementAndGet();
                    if (counter.intValue() == 0) {
                        //all vplans are parsed
                        List<Vertretungsstunde> lastParsedVPlan = loadVertretungsstundenFromStorrage();
                        boolean newVplan = !Vertretungsstunde.areVertretungsstundenTheSame(lastParsedVPlan, vStunden);
                        saveVertretungsstundenToStorrage(vStunden, activity);
                        RunVPlanResultEvent(vStunden, newVplan, true);
                    }
                }
            });
            download.download(currentLink, SensibleDataHelper.prsName, SensibleDataHelper.prsPassword);
        }
    }

    //region /// ProgressDialog ... ///
    /*
     * Show loading Dialog.
     */
    private void showProgressDialog(Context context) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("lade...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface arg0) {
                //stop client
            }
        });
    }

    private void closeProgressDialog() {
        progressDialog.cancel();
    }

    /*
     * Set if a loading dialog is shown.
     * Only needed for longer downloads or to prevent the user from input.
     */
    public void setIfProgressDialogIsShown(boolean b) {
        showprogressDialogBoolean = b;
    }
    //endregion

    private String saveVertretungsstundenToStorrage(List<Vertretungsstunde> vertretungsstunden, Context activity) {
        String all = "";
        for (Vertretungsstunde vStunde : vertretungsstunden) {
            all += vStunde.toString() + StorageHelper.SPLIT_SYMBOL_VERTRETUNGSSTUNDE_ITEM;
        }
        StorageHelper.saveToSharedPreferences(StorageHelper.VPLAN_LIST, all, activity);
        return all;
    }

    private List<Vertretungsstunde> loadVertretungsstundenFromStorrage() {
        List<Vertretungsstunde> output = new ArrayList<>();
        try {
            String savedString = StorageHelper.loadStringFromSharedPreferences(StorageHelper.VPLAN_LIST, activity);
            String[] splitted = savedString.split(StorageHelper.SPLIT_SYMBOL_VERTRETUNGSSTUNDE_ITEM);
            for (String vStundeString : splitted) {
                if (vStundeString != null && !vStundeString.equals("") && !vStundeString.equals(" ") && !vStundeString.equals("-")) {
                    output.add(new Vertretungsstunde(vStundeString));
                }
            }
            return output;
        } catch (Exception e) {
            return new ArrayList<Vertretungsstunde>();
        }
    }

    private void checkIfVPlanIsNew(List<Vertretungsstunde> vPlanList) {

    }

    private void setFilterKlasse(String klasse) {
        filterKlasse = klasse;
    }

    private List<Vertretungsstunde> parseSingleVPlan(String input) {
        Document doc = Jsoup.parse(input);
        //parse top information
        Elements elements_info = doc.select("table.info");

        //parse the Stunden
        Element datum = doc.select("div.mon_title").first();
        //save the last date a new plan was released and downloaded
        StorageHelper.saveToSharedPreferences("last_success_download", datum.text(), activity);
        //now parse the elements
        Elements vplanElements = doc.select("tr.list.odd, tr.list.even");
        List<Vertretungsstunde> vStunden = new ArrayList<>();
        for (Element e : vplanElements) {
            //in html:  0.Klasse(n)	1. Std.	2.Vertreter	3.Raum	 4.(Raum)	5.(Lehrer)	6.Vertretungs-Text	7.(Fach)	8.(Klasse(n))
            //in klasse: (String Klassen, String Stunden, String neuerLehrer, String neuerRaum, String alterRaum, String alterLehrer, String Vertretungstext, String Fach)
            Vertretungsstunde vStunde = new Vertretungsstunde(e.child(0).text(),
                    e.child(1).text(),
                    e.child(2).text(),
                    e.child(3).text(),
                    e.child(4).text(),
                    e.child(5).text(),
                    e.child(6).text(),
                    e.child(7).text());
            vStunden.add(vStunde);
        }
        return vStunden;
    }

    //region /// OnVPlanResultEvent ... ///
    List<OnVPlanResultEvent> mOnVPlanResultlistenerList = new ArrayList<OnVPlanResultEvent>();

    public void addOnVPlanResultEvent(OnVPlanResultEvent listener) {
        mOnVPlanResultlistenerList.add(listener);
    }

    public void removeAllOnVPlanResultEvents() {
        mOnVPlanResultlistenerList.clear();
    }

    public void removeOnVPlanResultEvent(OnVPlanResultEvent listener) {
        mOnVPlanResultlistenerList.remove(listener);
    }

    protected void RunVPlanResultEvent(List<Vertretungsstunde> alleStunden, boolean changedKlassenSpecificData, boolean success) {
        if (showprogressDialogBoolean)
            progressDialog.dismiss();
        for (OnVPlanResultEvent event : mOnVPlanResultlistenerList) {
            event.VPlanResultEvent(alleStunden, changedKlassenSpecificData, success);
        }
    }

    public interface OnVPlanResultEvent {
        void VPlanResultEvent(List<Vertretungsstunde> alleStunden, boolean changedKlassenSpecificData, boolean success);
    }
    //endregion
}
