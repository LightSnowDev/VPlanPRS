package com.lightSnowDev.VPlanPRS2.classes;

import android.app.Activity;
import android.content.Context;
import android.os.CountDownTimer;
import android.os.Looper;

import com.lightSnowDev.VPlanPRS2.helper.StorageHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Vertretungstag ist entweder morgen oder heute.
 * Ein Tag hat Seiten, wie zb. ["hzvp1.htm"] und ["hzvp2.htm"].
 * Ein Tag hat keine Stunden. Nur eine Seite hat Stunden.
 * <p>
 * Created by Jonathan on 11.04.2017.
 */

public class VertretungsTag {

    private Context context;
    private List<VertretungsSeite> vertretungsSeiten;
    private VertretungsTag.Day day;
    private AtomicInteger numberOfSuccessfullDownloads;
    private AtomicBoolean stopDownload;

    public enum Day {heute, morgen, beide}

    public VertretungsTag(Day day, Context context) {
        this.day = day;
        this.context = context;
        vertretungsSeiten = new ArrayList<>();
        numberOfSuccessfullDownloads = new AtomicInteger();
        stopDownload = new AtomicBoolean();
    }

    public VertretungsTag(String stringToParse, VertretungsTag.Day day, Context context) {
        this(day, context);
        String[] splitted = stringToParse.split(StorageHelper.SPLIT_SYMBOL_VERTRETUNGSSEITE);
        for (String seiteString : splitted) {
            vertretungsSeiten.add(new VertretungsSeite(seiteString, day, context));
        }
    }

    /**
     * Lädt die Seiten des Vertretungsplans.
     *
     * @param urls Liste der Urls, die geladen werden sollen
     */
    public void loadVertretungsSeiten(List<String> urls) {
        //Abbruchbedingung, falls komische Dinge passieren.
        setTimeoutTimer(10);

        //Festlegen der Anzahl der urls
        //Diese werden runtergezählt, und so die vollständigkeit der
        //async-abfragen gewährleistet.
        numberOfSuccessfullDownloads.set(urls.size());
        stopDownload.set(false);

        //Hier werden nur die Seiten zum runterladen aufgerufen.
        //Die überprüfung, ob alles korrekt war, findet im
        //async return der Vertretungsseiten statt.
        for (String url : urls) {
            //Wenn ein bereits ein Error aufgetreten ist, stoppe die Schleife.
            if (!stopDownload.get())
                loadSeite(url);
            else
                return;
        }
    }

    /**
     * Lade eine Seite runter.
     *
     * @param url Einfache String URL, die geladen werden soll.
     */
    private void loadSeite(String url) {
        VertretungsSeite vertretungsSeite = new VertretungsSeite(day, context);
        vertretungsSeite.addOnVPlanResultEvent(new VertretungsSeite.OnVPlanResultEvent() {
            @Override
            public void VPlanResultEvent(VertretungsSeite seite, VertretungsSeite.VertretungsplanResultType resultType) {
                //wenn schon zuvor ein Error aufgetreten ist, breche sofort ab.
                if (stopDownload.get())
                    return;

                //Überprüfen, ob es bei der aktuellen Seite einen Fehler gab.
                if (resultType == VertretungsSeite.VertretungsplanResultType.success) {
                    vertretungsSeiten.add(seite);

                    //reduziere bei erfolgreichem Download die Anzahl.
                    numberOfSuccessfullDownloads.decrementAndGet();

                    //Überprüfe, ob schon alle async events durchgelaufen sind.
                    //Wenn die Anzahl der Aufrufe 0 ist, dann sind alle async
                    //events aufgerufen worden. Dann soll das Run event aufgerufen werden.
                    if (numberOfSuccessfullDownloads.get() == 0) {

                        //Wenn es, zwischendrin beim Laden, einen Error gab, ist bereits das Run-Event aufgerufen worden.
                        //Deswegen wird es hier nochmal abgefragt. Es könnte ja ein anderes async-Event
                        //dazwischen gefunkt haben.
                        if (!stopDownload.get()) {
                            RunVPlanResultEvent(PRS.PRSResultType.success);
                        }
                    }

                } else if (resultType == VertretungsSeite.VertretungsplanResultType.parseError ||
                        resultType == VertretungsSeite.VertretungsplanResultType.downloadError) {

                    //Es gab antscheinend einen Fehler beim laden. So wird stopDownload auf true gesetzt,
                    //damit alle async-events wissen, dass sie stoppen können.
                    stopDownload.set(true);

                    //Wenn ein Error beim Laden aufgetreten ist, dann führe das Run Event mit einem Fehler aus.
                    RunVPlanResultEvent(PRS.PRSResultType.downloadError);
                }


            }
        });
        vertretungsSeite.downloadSeite(url);
    }

    public VertretungsplanNews getVertretungsplanNews() {
        for (VertretungsSeite seite : vertretungsSeiten) {
            if (seite.getVertretungsplanNews().getResult() == VertretungsplanNews.VertretungsplanNewsResult.normalInfo)
                return seite.getVertretungsplanNews();
        }
        return null;
    }

    /**
     * Ein Timer, der nach x Sekunden alle Operationen abbricht.
     * Er wird benötigt, weil die async-events ziemlich unberechenbar sind.
     *
     * @param seconds Anzahl der Sekunden, nachdem der Timer auslöst.
     */
    private void setTimeoutTimer(int seconds) {

        if (Looper.myLooper() == null)
            Looper.prepare();
        new CountDownTimer(seconds * 1000, seconds * 1000) {

            public void onTick(long millisUntilFinished) {
                return;
            }

            public void onFinish() {
                //wurde schon ein Fehler gemeldet? Wir wollen nicht doppelt aufrufen.
                if (!stopDownload.get())
                    return;
                //wurden erfolgreich alle downloads beendet? Dann brechen wir auch ab.
                if (numberOfSuccessfullDownloads.get() == 0)
                    return;

                stopDownload.set(true);
                RunVPlanResultEvent(PRS.PRSResultType.downloadError);
            }
        }.start();
    }

    @Override
    public String toString() {
        String toReturn = "";
        if (vertretungsSeiten.size() > 0) {
            for (VertretungsSeite seite : vertretungsSeiten) {
                toReturn += seite.toString();
                toReturn += StorageHelper.SPLIT_SYMBOL_VERTRETUNGSSEITE;
            }

            toReturn = toReturn.substring(0, toReturn.length() - 1);
        }
        return toReturn.toString();
    }

    //region /// getter ... ///

    /**
     * Liste aller VertretungsSEITEN dieses Tages.
     * Normalerweise zwischen 1-3 Seiten.
     *
     * @return unsortierte Rückgabe
     */
    public List<VertretungsSeite> getVertretungsSeiten() {
        return vertretungsSeiten;
    }

    /**
     * Eine Liste aller VertretungsSTUNDEN dieses Tages.
     *
     * @return unsortierte Rückgabe
     */
    public List<VertretungsStunde> getVertretungsStunden() {
        List<VertretungsStunde> toReturn = new ArrayList<>();
        for (VertretungsSeite s : getVertretungsSeiten())
            toReturn.addAll(s.getKlassenStunden());
        return toReturn;
    }
    //endregion

    //region /// OnVertretungsTagResultEvent ... ///
    List<VertretungsTag.OnVertretungsTagResultEvent> mOnVertretungsTagResultlistenerList =
            new ArrayList<VertretungsTag.OnVertretungsTagResultEvent>();

    public void addOnVPlanResultEvent(VertretungsTag.OnVertretungsTagResultEvent listener) {
        mOnVertretungsTagResultlistenerList.add(listener);
    }

    /**
     * Starte das asnyc-event für alle Methoden, die aboniert haben.
     *
     * @param resultType Ist die Operation erfolgreich gewesen?
     */
    protected void RunVPlanResultEvent(PRS.PRSResultType resultType) {
        for (VertretungsTag.OnVertretungsTagResultEvent event : mOnVertretungsTagResultlistenerList) {
            event.VertretungsTagResultEvent(this, resultType);
        }
    }

    public interface OnVertretungsTagResultEvent {
        void VertretungsTagResultEvent(VertretungsTag tag, PRS.PRSResultType resultType);
    }
    //endregion
}
