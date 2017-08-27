package com.lightSnowDev.VPlanPRS2.items;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.lightSnowDev.VPlanPRS2.R;
import com.lightSnowDev.VPlanPRS2.helper.DownloadHelper;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Ein Termin der Schule aus dem Lanis-Portal.
 * Es wird als View der Name und das Datum angezeigt.
 * Beim OnClick werden Details des Termins aufgerufen.
 * <p>
 * Created by Jonathan on 27.07.2017.
 */

public class TerminView extends LinearLayout {

    private boolean isMessageBoxOpen = false;

    /**
     * Erstellt ein TerminView mit den Parametervariablen.
     *
     * @param activity Aktuelle Activity
     * @param eventId  Id des aktuellen Events als String. Typischerweise 3 Zeichen lang.
     * @param headline Die Überschrift des Events als String.
     * @param fromDate Begin des Events als String. Keine Formatvorgaben.
     * @param toDate   Ende des Events als String. Keine Formatvorgaben.
     */
    public TerminView(Activity activity, String eventId, String headline, String fromDate, String toDate) {
        super(activity);
        inflate(getContext(), R.layout.termin_view_layout, this);
        init(activity, eventId, headline, fromDate, toDate);
    }

    /**
     * Erstellt ein TerminView mit einem json Objekt des Lanis-Portals.
     * Es müssen folgende Attribute vorhanden sein:
     * 1. title String
     * 2. Anfang String. Format: "yyyy-MM-dd HH:mm:ss"
     * 3. Ende String. Format: "yyyy-MM-dd HH:mm:ss"
     * 4. Id
     *
     * @param activity Aktuelle Activity
     * @param obj      json Objekt, das vom Lanis Portal zurückgegeben wird.
     */
    public TerminView(Activity activity, JSONObject obj) {
        super(activity);
        inflate(getContext(), R.layout.termin_view_layout, this);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat df_new = new SimpleDateFormat("dd.MM.yyyy");
        Date anfang = new Date();
        Date ende = new Date();
        String headline = "";
        String eventId = "";

        try {
            headline = obj.getString("title");
            anfang = df.parse(obj.getString("Anfang"));
            ende = df.parse(obj.getString("Ende"));
            eventId = obj.getString("Id");
        } catch (Exception e) {
            throw new RuntimeException("Termine konnten nicht geladen werden. Fehler #fTV101");
        }

        init(activity, eventId, headline, df_new.format(anfang), df_new.format(ende));
    }

    public void init(final Activity activity, final String eventID, String headline, String fromDate, String toDate) {
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //Verhindert, dass mehrfach geklickt wird und sich so mehrere MessageBoxen öffnen.
                if (isMessageBoxOpen == true)
                    return;
                isMessageBoxOpen = true;

                //lade details runter
                DownloadHelper helper = new DownloadHelper(activity);
                helper.setFIXED_URL("https://portal.lanis-system.de/");
                helper.addOnRecievedEvent(new DownloadHelper.OnRecievedEvent() {
                    @Override
                    public void recievedEvent(String resultString, String resultBase64, boolean success) {
                        if (success && resultString != null)
                            parseAndShowDetailsMessageBox(activity, resultString);
                        else
                            showErrorMessageBox(activity);
                    }
                });
                HashMap k = new HashMap();
                k.put("f", "getEvent");
                k.put("id", eventID);
                k.put("ki", "6110");
                k.put("kkey", "8d76d4f44b21cd130cd9b5ae060ade4f5f832e753cec07e1d5d5b36fa9aa8a01f71d4b1d2f382f8063d7c4e875f64c21");
                k.put("ki", "6110");
                helper.post("kalender.php", k, null);
            }
        });
        setHeadlineText(headline);
        setDateText(fromDate, toDate);
    }

    /**
     * Wenn eine Information (zb. der Ort des Termins) im json nur ein leerer String ist,
     * dann wird er in "Keine Informationen" umgewandelt.
     *
     * @param input String-Value des json Objektes.
     * @return input, falls nicht leer. Ansonsten Information, dass keine Daten vorhanden sind.
     */
    private String reformatEmptyString(String input) {
        String emptyReturn = "Keine Informationen";
        if (input == null)
            return emptyReturn;
        else if (input.trim().equals(""))
            return emptyReturn;
        else
            return input;
    }

    /**
     * Zeit eine Fehlernachricht als MessageBox an.
     *
     * @param activity
     */
    private void showErrorMessageBox(Activity activity) {
        MaterialDialog.SingleButtonCallback click = new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                isMessageBoxOpen = false;
            }
        };
        new MaterialDialog.Builder(activity)
                .title("Fehler")
                .content("Beim Laden der Details dieses Events ist ein Fehler aufgetreten. Fehler: #tv102")
                .neutralText("Zurück")
                .onAny(click)
                .canceledOnTouchOutside(true)
                .showListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                    }
                })
                .cancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        isMessageBoxOpen = false;
                    }
                })
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        isMessageBoxOpen = false;
                    }
                })
                .show();
    }

    /**
     * Analysiert den runtergeladenen json-String und zeigt die wichtigsten Daten in einer MessageBox an.
     *
     * @param activity
     * @param input    nicht leerer json String
     */
    private void parseAndShowDetailsMessageBox(Activity activity, String input) {

        //region // sample Response
                        /*
                RESPONSE:
                   {   "id":"671",
                       "title":"Projektwoche",
                       "editable":false,
                       "category":"22",
                       "description":"",
                       "allDay":true,
                       "start":{        "date":"2017-06-22 00:00:00.000000",
                                        "timezone_type":3,
                                        "timezone":"Europe\/Berlin"             },
                       "end":{          "date":"2017-06-29 00:00:00.000000",
                                        "timezone_type":3,
                                        "timezone":"Europe\/Berlin"             },
                       "properties":{   "id":"671",
                                        "categorie":"22",
                                        "description":"",
                                        "zielgruppen":["\u00d6ffentlich"],
                                        "verantwortlich":null,
                                        "ort":"",
                                        "editable":false                        }
                    }
                    */
        //endregion
		
		        String headline = "";
        String description = "";
        String ort = "";

        try {
            JSONObject obj = new JSONObject(input);
            headline = reformatEmptyString(obj.getString("title"));
            description = reformatEmptyString(obj.getString("description"));
            ort = reformatEmptyString(obj.getJSONObject("properties").getString("ort"));
        } catch (Exception e) {
            showErrorMessageBox(activity);
            return;
        }

        new MaterialDialog.Builder(activity)
                .title(headline)
                .content("Beschreibung: " + description
                        + "\nOrt: " + ort)
                .positiveText("Zurück")
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        isMessageBoxOpen = false;
                    }
                })
                .canceledOnTouchOutside(true)
                .showListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                    }
                })
                .cancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        isMessageBoxOpen = false;
                    }
                })
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        isMessageBoxOpen = false;
                    }
                })
                .show();
    }

    /**
     * Setzt den Namen im Überschrift-TextView.
     *
     * @param text Name des Events
     */
    public void setHeadlineText(String text) {
        ((TextView) findViewById(R.id.termin_view_headline_textView)).setText(text);
    }

    /**
     * Anzeigen des Zeitraums eines Events in einem TextView.
     *
     * @param fromDate Anfangszeitpunkt
     * @param toDate   Endzeitpunkt
     */
    public void setDateText(String fromDate, String toDate) {
        //Überprüfen, ob nur ein Tag betroffen ist. Dann Uhrzeit anzeigen.
        if (fromDate.equals(toDate))
            ((TextView) findViewById(R.id.termin_view_date_textView)).setText(fromDate);
        else
            ((TextView) findViewById(R.id.termin_view_date_textView)).setText(fromDate + " - " + toDate);
    }
}
