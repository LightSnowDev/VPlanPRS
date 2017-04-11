package com.lightSnowDev.VPlanPRS2.classes;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.lightSnowDev.VPlanPRS2.helper.DownloadHelper;
import com.lightSnowDev.VPlanPRS2.helper.StorageHelper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Jonathan Schwarzenböck on 24.08.2016.
 */
public class PRS {

    Context activity;
    //region /// OnVPlanResultEvent ... ///
    List<OnPRSResultEvent> mOnPRSResultlistenerList = new ArrayList<OnPRSResultEvent>();
    private ProgressDialog progressDialog;
    private boolean showprogressDialogBoolean = false;
    private boolean loadOldDataIfFail = true;

    private Vertretungsstunde.Tag tagToFilter = Vertretungsstunde.Tag.beide;

    public PRS(Context context) {
        activity = context;
    }

    public void downloadLinks() {
        if (showprogressDialogBoolean)
            showProgressDialog(activity);
        DownloadHelper download = new DownloadHelper(activity);
        download.setEncoding("iso-8859-1");
        download.addOnRecievedEvent(new DownloadHelper.OnRecievedEvent() {
            @Override
            public void recievedEvent(String resultString, String resultBase64, boolean success) {

            }
        });
        download.addOnRecievedEvent(new DownloadHelper.OnRecievedEvent() {
            @Override
            public void recievedEvent(String resultString, String resultBase64, boolean success) {
                if (success)
                    parseLinks(resultString);
                else
                    onVPlanFail();
            }
        });
        download.download("links.htm", "vplan", "2011");
    }

    private void parseLinks(final String input) {
        Document doc = null;
        Elements elements_vplan = null;
        try {
            doc = Jsoup.parse(input);
            //store the vertretungsstunden links in a list
            elements_vplan = doc.select("[target=right]");
        } catch (Exception e) {
            onVPlanFail();
        }
        final List<Vertretungsstunde> vStundenRelated = new ArrayList<>();
        final List<Vertretungsstunde> vStundenUnknown = new ArrayList<>();
        final AtomicInteger counter = new AtomicInteger(elements_vplan.size());
        final AtomicBoolean hasFailed = new AtomicBoolean(false);
        for (final Element e : elements_vplan) {
            String savedklasse = StorageHelper.loadStringFromSharedPreferences(StorageHelper.VPLAN_USER_KLASSE, activity);
            Vertretungsplan vPlan = new Vertretungsplan(savedklasse, tagToFilter, activity);
            vPlan.setFilterKlassen(StorageHelper.loadBooleanFromSharedPreferences(StorageHelper.VPLAN_USER_KLASSE_FILTER, activity));
            vPlan.addOnVPlanResultEvent(new Vertretungsplan.OnVPlanResultEvent() {
                @Override
                public void VPlanResultEvent(Vertretungsplan plan, Vertretungsplan.VertretungsplanResultType resultType) {
                    if (resultType == Vertretungsplan.VertretungsplanResultType.success) {
                        vStundenRelated.addAll(plan.getKlassenStunden());
                        vStundenUnknown.addAll(plan.getUnbekannteStunden());
                    } else if (resultType == Vertretungsplan.VertretungsplanResultType.parseError ||
                            resultType == Vertretungsplan.VertretungsplanResultType.downloadError) {
                        hasFailed.set(true);
                        onVPlanFail();
                        return;
                    }
                    counter.decrementAndGet();
                    if (counter.get() == 0) {
                        if (hasFailed.get() == false) {
                            //check if vplan is new
                            boolean isNew = checkIfVPlanIsNew(sortVertretungsstunden(vStundenRelated), sortVertretungsstunden(vStundenUnknown), activity);
                            if (isNew) {
                                //save the newly downloaded vplan
                                getVertretungsstundenString(sortVertretungsstunden(vStundenRelated), sortVertretungsstunden(vStundenUnknown),
                                        true, activity);
                            }
                            //fire result event
                            RunPRSResultEvent(sortVertretungsstunden(vStundenRelated),
                                    sortVertretungsstunden(vStundenUnknown), isNew, PRSResultType.success);
                        } else {
                            tryLoadVertretungsstundenFromStorrage();
                        }
                    }
                }
            });
            vPlan.downloadVPlan(e);
        }
    }

    private void onVPlanFail() {
        if (loadOldDataIfFail) {
            tryLoadVertretungsstundenFromStorrage();
        } else {
            RunPRSResultEvent(null, null, false, PRSResultType.downloadAndStorrageError);
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

    private String getVertretungsstundenString(List<Vertretungsstunde> vStunden, List<Vertretungsstunde> unknownStunden,
                                               boolean saveToStorrage, Context activity) {
        String all = "";
        List<Vertretungsstunde> allStunden = new ArrayList<>();
        allStunden.addAll(vStunden);
        allStunden.addAll(unknownStunden);
        for (Vertretungsstunde vStunde : allStunden) {
            all += vStunde.toString() + StorageHelper.SPLIT_SYMBOL_VERTRETUNGSSTUNDE_ITEM;
        }
        if (saveToStorrage)
            StorageHelper.saveToSharedPreferences(StorageHelper.VPLAN_LIST, all, activity);
        return all;
    }

    private void tryLoadVertretungsstundenFromStorrage() {
        List<Vertretungsstunde> outputKlassenstunden = new ArrayList<>();
        List<Vertretungsstunde> outputUnbekanntestunden = new ArrayList<>();
        String filterKlasse = StorageHelper.loadStringFromSharedPreferences(StorageHelper.VPLAN_USER_KLASSE, activity);
        try {
            String savedString = StorageHelper.loadStringFromSharedPreferences(StorageHelper.VPLAN_LIST, activity);
            if (savedString.isEmpty()) {
                RunPRSResultEvent(null, null, false, PRSResultType.downloadAndStorrageError);
                return;
            }
            String[] splitted = savedString.split(StorageHelper.SPLIT_SYMBOL_VERTRETUNGSSTUNDE_ITEM);
            for (String vStundeString : splitted) {
                if (vStundeString != null && !vStundeString.equals("") && !vStundeString.equals(" ") && !vStundeString.equals("&nbsp;") && !vStundeString.equals("-")) {
                    Vertretungsstunde tempStunde = new Vertretungsstunde(vStundeString);
                    if (tempStunde.matchesKlasse(filterKlasse))
                        outputKlassenstunden.add(tempStunde);
                    else if (tempStunde.klassenIsEmpty())
                        //Klassenname is unknown
                        outputUnbekanntestunden.add(tempStunde);
                }
            }
            RunPRSResultEvent(outputKlassenstunden, outputUnbekanntestunden, false, PRSResultType.successButStorrage);
        } catch (Exception e) {
            RunPRSResultEvent(null, null, false, PRSResultType.downloadAndStorrageError);
        }
    }

    private boolean checkIfVPlanIsNew(List<Vertretungsstunde> vStunden, List<Vertretungsstunde> unknownStunden, Context activity) {
        //load String from Storrage:
        String savedString = StorageHelper.loadStringFromSharedPreferences(StorageHelper.VPLAN_LIST, activity);
        String currentString = getVertretungsstundenString(vStunden, unknownStunden, false, activity);

        return !savedString.equals(currentString);
    }


    public boolean isLoadOldDataIfFail() {
        return loadOldDataIfFail;
    }

    public void setLoadOldDataIfFail(boolean loadOldDataIfFail) {
        this.loadOldDataIfFail = loadOldDataIfFail;
    }

    public Vertretungsstunde.Tag getTagToFilter() {
        return tagToFilter;
    }

    public void setTagToFilter(Vertretungsstunde.Tag tag) {
        this.tagToFilter = tag;
    }

    public void addOnPRSResultEvent(OnPRSResultEvent listener) {
        mOnPRSResultlistenerList.add(listener);
    }

    public List<Vertretungsstunde> sortVertretungsstunden(List<Vertretungsstunde> input) {
        Collections.sort(input, new Comparator<Vertretungsstunde>() {
            @Override
            public int compare(Vertretungsstunde fruit2, Vertretungsstunde fruit1) {
                return fruit1.toString().compareTo(fruit2.toString());
            }
        });
        Collections.reverse(input);
        return input;
    }

    protected void RunPRSResultEvent(List<Vertretungsstunde> relatedStunden, List<Vertretungsstunde> unknownStunden, boolean changedKlassenSpecificData,
                                     PRSResultType resultType) {
        if (showprogressDialogBoolean && progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
        for (OnPRSResultEvent event : mOnPRSResultlistenerList) {
            event.PRSResultEvent(relatedStunden, unknownStunden, changedKlassenSpecificData, resultType);
        }
    }

    public enum PRSResultType {success, successButStorrage, parseError, downloadAndStorrageError}

    public interface OnPRSResultEvent {
        void PRSResultEvent(List<Vertretungsstunde> relatedStunden, List<Vertretungsstunde> unknownStunden, boolean changedKlassenSpecificData,
                            PRS.PRSResultType resultType);
    }
    //endregion
}
