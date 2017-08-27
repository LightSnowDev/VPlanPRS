package com.lightSnowDev.VPlanPRS2.classes;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.lightSnowDev.VPlanPRS2.helper.DownloadHelper;
import com.lightSnowDev.VPlanPRS2.helper.StorageHelper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Die Überklasse, die den Vertretungsplan runterlädt.
 * <p>
 * Created by Jonathan Schwarzenböck on 24.08.2016.
 */
public class PRS implements Comparable<PRS> {

    private Context activity;
    private ProgressDialog progressDialog;
    private boolean showprogressDialogBoolean = false;
    private AtomicBoolean errorOccurred;
    private AtomicInteger daysParsed;
    private String klasseToFilter = "";

    public enum PRSResultType {success, successButStorrage, parseError, downloadError, downloadAndStorrageError}

    private VertretungsTag heuteTag;
    private VertretungsTag morgenTag;


    public PRS(Context context) {
        activity = context;
        heuteTag = new VertretungsTag(VertretungsTag.Day.heute, activity);
        morgenTag = new VertretungsTag(VertretungsTag.Day.morgen, activity);
        daysParsed = new AtomicInteger(0);
        errorOccurred = new AtomicBoolean(false);
    }

    /**
     * Download the top webpage ["links.htm"]
     */
    public void downloadPRS() {
        if (showprogressDialogBoolean)
            showProgressDialog(activity);
        DownloadHelper download = new DownloadHelper(activity);
        download.setEncoding("iso-8859-1");
        download.addOnRecievedEvent(new DownloadHelper.OnRecievedEvent() {
            @Override
            public void recievedEvent(String resultString, String resultBase64, boolean success) {
                if (success)
                    parsePRS(resultString);
                else
                    RunPRSBasedOnResultType(PRSResultType.downloadError);
            }
        });
        download.download("links.htm", "vplan", "2011");
    }

    /**
     * parse the result of downloadLinks()
     *
     * @param input html String of the ["links.htm"] webpage
     */
    public void parsePRS(final String input) {
        Document doc;
        Elements elements_vplan = new Elements();
        try {
            doc = Jsoup.parse(input);
            //Speichere die Links der Hauptseite.
            //Alle Links werden am html Attribut "target=right" identifiziert:
            //<a href="hvp3.htm" target="right">Heute 3</a>
            elements_vplan = doc.select("[target=right]");
        } catch (Exception e) {
            RunPRSBasedOnResultType(PRSResultType.parseError);
            return;
        }

        // Split links.htm into heute:hvp and morgen:mvp.
        // The individual links will be downloaded in the VertretungsSeite class.
        List<String> heuteLinkElements = new ArrayList<>();
        List<String> morgenLinkElements = new ArrayList<>();
        //List<String> othersLinkElements = new ArrayList<>();
        for (final Element e : elements_vplan) {
            String currentLink = e.attr("href");
            if (currentLink == null || currentLink.isEmpty() || currentLink.equals(""))
                break;
            if (currentLink.startsWith("hvp"))
                heuteLinkElements.add(currentLink);
            else if (currentLink.startsWith("mvp"))
                morgenLinkElements.add(currentLink);
            //else
            //    othersLinkElements.add(currentLink);
        }

        //async-events für die beiden Tage
        VertretungsTag.OnVertretungsTagResultEvent asnycEvent = new VertretungsTag.OnVertretungsTagResultEvent() {
            @Override
            public void VertretungsTagResultEvent(VertretungsTag tag, PRSResultType resultType) {
                //Es darf noch kein Fehler aufgetreten sein, sonst könnte das Run-event 2 mal aufgerufen werden.
                if (resultType != PRSResultType.success && !errorOccurred.get()) {
                    errorOccurred.set(true);
                    //Es wird nicht versucht sofort den gespeicherten vplan zu laden.
                    //Dies muss manuell aufgerufen werden.
                    RunPRSBasedOnResultType(resultType);
                    return;
                }
                daysParsed.incrementAndGet();
                //Erst wenn beide Tage als Rückgabe ankommen, kann gefeuert werden.
                if (daysParsed.get() == 2) {
                    if (!errorOccurred.get()) {
                        RunPRSBasedOnResultType(PRSResultType.success);
                        savePRSToStorrage(PRS.this);
                    }
                }
            }
        };
        heuteTag.addOnVPlanResultEvent(asnycEvent);
        morgenTag.addOnVPlanResultEvent(asnycEvent);

        //Übergibt die Links an die jeweiligen Tage, wo sie weiter analysiert werden.
        heuteTag.loadVertretungsSeiten(heuteLinkElements);
        morgenTag.loadVertretungsSeiten(morgenLinkElements);
    }

    private static void savePRSToStorrage(PRS prsToSave) {
        String toSave = prsToSave.toString();
        StorageHelper.saveToSharedPreferences(StorageHelper.VPLAN_LIST, toSave, prsToSave.activity);
    }

    public static PRS loadPRSFromStorrage(String klasseToFilter, Context ac) {
        PRS prs = new PRS(ac);
        if (klasseToFilter != null)
            prs.setKlasseToFilter(klasseToFilter);
        String orignial = StorageHelper.loadStringFromSharedPreferences(StorageHelper.VPLAN_LIST, ac);
        String[] splitted = orignial.split(StorageHelper.SPLIT_SYMBOL_VERTRETUNGSSTAG);
        if (splitted.length != 2)
            throw new RuntimeException("#PRS100");
        prs.heuteTag = new VertretungsTag(splitted[0], VertretungsTag.Day.heute, ac);
        prs.morgenTag = new VertretungsTag(splitted[1], VertretungsTag.Day.morgen, ac);

        return prs;
    }

    //region /// ProgressDialog ... ///

    /**
     * Erstelle und zeige den 'loading-Dialog'.
     *
     * @param context aktueller Context einer Activity
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

    /**
     * Vergleicht eine PRS Instanz mit der zuletzt gespeicherten.
     *
     * @param prsToCompare PRS Instanz
     * @return true, wenn die toString() Methoden gleich sind.
     */
    public static boolean checkIfVPlanIsEqual(PRS prsToCompare) {
        String savedString = StorageHelper.loadStringFromSharedPreferences(StorageHelper.VPLAN_LIST, prsToCompare.activity);
        String currentString = prsToCompare.toString();
        return savedString.equals(currentString);
    }

    @Override
    public int compareTo(PRS otherPRS) {
        return -1 * this.toString().compareTo(otherPRS.toString());
    }

    public void setKlasseToFilter(String klasseToFilter) {
        if (klasseToFilter == null)
            klasseToFilter = "";
        this.klasseToFilter = klasseToFilter;
    }

    //region /// getter  ///

    /**
     * Das Veröffentlichungsdatum des ganzen PRS Vertretungsplans.
     * Alle Daten der einzellnen Seiten sollten identisch sein.
     *
     * @return Veröffentlichungsdatum (Date)
     */
    public Date getPublishDate() {
        return heuteTag.getVertretungsSeiten().get(0).getPublishDate();
    }

    public String getPublishDateString() {
        return heuteTag.getVertretungsSeiten().get(0).getPublishDateString();
    }

    /**
     * Datum das zu dem VertretungsTag.Day gehört.
     *
     * @param day VertretungsTag.Day, der abgefragt werden soll
     * @return Datum von heute oder morgen. Null, falls input null ist.
     */
    public Date getRelatedDate(VertretungsTag.Day day) {
        if (day == VertretungsTag.Day.heute)
            return heuteTag.getVertretungsSeiten().get(0).getRelatedDateDate();
        else if (day == VertretungsTag.Day.morgen)
            return morgenTag.getVertretungsSeiten().get(0).getRelatedDateDate();
        else
            return null;
    }

    public String getParsedDateString() {
        return heuteTag.getVertretungsSeiten().get(0).getParseDateString();
    }

    //region /// getter VertretungsStunde(n) ///

    public String getVertretungsplanNewsString(VertretungsTag.Day tagToFilter) {
        if (tagToFilter == VertretungsTag.Day.heute)
            return heuteTag.getVertretungsplanNews().getHTML();
        else if (tagToFilter == VertretungsTag.Day.morgen)
            return morgenTag.getVertretungsplanNews().getHTML();
        else if (tagToFilter == null || tagToFilter == VertretungsTag.Day.beide) {
            return heuteTag.getVertretungsplanNews().getHTML() +
                    morgenTag.getVertretungsplanNews().getHTML();
        } else
            return "";
    }

    public List<VertretungsStunde> getAllStundenOfDay(VertretungsTag.Day tagToFilter) {
        StorageHelper.saveToSharedPreferences("test", "2c", activity);
        List<VertretungsStunde> toReturn = new ArrayList<>();
        StorageHelper.saveToSharedPreferences("test", "2d", activity);
        if (tagToFilter == null || tagToFilter == VertretungsTag.Day.beide) {
            StorageHelper.saveToSharedPreferences("test", "2e", activity);
            toReturn.addAll(heuteTag.getVertretungsStunden());
            StorageHelper.saveToSharedPreferences("test", "2f", activity);
            toReturn.addAll(morgenTag.getVertretungsStunden());
            StorageHelper.saveToSharedPreferences("test", "2g", activity);
        } else if (tagToFilter == VertretungsTag.Day.heute)
            toReturn.addAll(heuteTag.getVertretungsStunden());
        else if (tagToFilter == VertretungsTag.Day.morgen)
            toReturn.addAll(morgenTag.getVertretungsStunden());

        Collections.sort(toReturn);
        return toReturn;
    }

    public List<VertretungsStunde> getRelatedStunden(VertretungsTag.Day tagToFilter) {
        //Kein Filter gesetzt, deswegen alle Stunden zurückgeben.
        if (klasseToFilter.equals(""))
            return getAllStundenOfDay(tagToFilter);

        List<VertretungsStunde> toReturn = new ArrayList<>();
        for (VertretungsStunde stunde : getAllStundenOfDay(tagToFilter)) {
            //Klasse muss zur Stunde gehören
            if (stunde.matchesKlasse(klasseToFilter))
                toReturn.add(stunde);
        }
        Collections.sort(toReturn);
        return toReturn;
    }

    public List<VertretungsStunde> getUnknownStunden(VertretungsTag.Day tagToFilter) {
        //Wenn kein Filter gesetzt ist, gibt es auch keine UnrelatedStunden.
        if (klasseToFilter.equals(""))
            return new ArrayList<>();

        List<VertretungsStunde> toReturn = new ArrayList<>();
        for (VertretungsStunde stunde : getAllStundenOfDay(tagToFilter)) {
            //Stunde darf zu keiner Klasse gehören
            if (stunde.klassenIsEmpty())
                toReturn.add(stunde);
        }
        Collections.sort(toReturn);
        return toReturn;
    }

    public Context getContext() {
        return activity;
    }


    //endregion

    //region /// OnVPlanResultEvent ... ///
    private List<OnPRSResultEvent> mOnPRSResultlistenerList = new ArrayList<>();

    public void addOnPRSResultEvent(OnPRSResultEvent listener) {
        mOnPRSResultlistenerList.add(listener);
    }

    private void RunPRSBasedOnResultType(PRSResultType resultType) {
        if (resultType == PRSResultType.downloadError) {
            PRS prsLoadedFromStorrage = null;
            try {
                prsLoadedFromStorrage = PRS.loadPRSFromStorrage(klasseToFilter, activity);
            } catch (Exception e) {
                RunPRSResultEvent(this, PRSResultType.downloadAndStorrageError);
                return;
            }
            RunPRSResultEvent(prsLoadedFromStorrage, PRSResultType.successButStorrage);
        } else
            RunPRSResultEvent(this, resultType);
    }

    private void RunPRSResultEvent(PRS prsToSend, PRSResultType resultType) {
        if (showprogressDialogBoolean && progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
        for (OnPRSResultEvent event : mOnPRSResultlistenerList) {
            event.PRSResultEvent(prsToSend, resultType);
        }
    }

    public interface OnPRSResultEvent {
        void PRSResultEvent(PRS prs, PRSResultType resultType);
    }
    //endregion

    @Override
    public String toString() {
        String toReturn = "";
        toReturn += heuteTag.toString();
        toReturn += StorageHelper.SPLIT_SYMBOL_VERTRETUNGSSTAG;
        toReturn += morgenTag.toString();
        return toReturn;
    }
}
