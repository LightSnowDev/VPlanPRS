package com.lightSnowDev.VPlanPRS2.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Diese Klasse logt jeden und alles.
 * <p/>
 * Created by Jonathan on 02.05.2016.
 */
public class StorageHelper {

    /*
     * Speicher Varibalen
     */
    public final static String log_filename = "prs_log";
    public final static String VPLAN_LIST = "vplan_list";
    public final static String VPLAN_LAST_UPDATE_PARSED = "vplan_list_update_parsed";
    public final static String VPLAN_USER_KLASSE = "vplan_list_user_klasse";
    public final static String VPLAN_USER_KLASSE_FILTER = "vplan_list_user_klasse_filter";
    public final static String VPLAN_USER_LEHRER_NAME_KUERZEL = "vplan_list_user_lehrer_name_kuerzel";
    public final static String VPLAN_USER_AUTO_UPDATE = "vplan_user_auto_update";
    public final static String VPLAN_USER_AUTO_UPDATE_PLAY_SOUND = "vplan_user_auto_update_sound";
    public final static String VPLAN_FIRST_START = "vplan_user_first_start";
    public final static String VPLAN_LAST_APP_NAME_VERSION = "vplan_last_app_name_version";
    public final static String VPLAN_COMPACT_UNBEKANNTE_STUNDEN = "vplan_compact_unbekannte_stunden";
    public final static String VPLAN_COMPACT_NEWS = "vplan_compact_news";
    /*
     * SV Information
     */
    public final static String VPLAN_SV_LAST_UPDATE = "vplan_sv_last_update";
    public final static String VPLAN_SV_HTML = "vplan_sv_html";
    /*
     * Variablen für gcm
     */
    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    /*
     * Variablen für Busfahrplan
     */
    public static final String BUSPLAN_HTML = "busplan_Html";
    /*
     * Allgemeine Variablen
     */
    public static final String SPLIT_SYMBOL_VERTRETUNGSSTAG = "✪";
    public static final String SPLIT_SYMBOL_VERTRETUNGSSEITE = "✴";
    public static final String SPLIT_SYMBOL_VERTRETUNGSSEITE_DATEHEADER = "➧";
    public static final String SPLIT_SYMBOL_VERTRETUNGSSTUNDE = "Δ";
    public static final String SPLIT_SYMBOL_VERTRETUNGSSTUNDE_EIGENSCHAFTEN = "•";

    /*
     * Einen String möglichst sicher speichern. Dabei werdern 2 Methoden verwendet:
     * 1. Als Datei speichern.
     * 2. In den SharedPreferences speichern.
     */
    public static void logAll(String input, Context context) {
        saveToFile(input, context);
        loadFromFile(context);
    }

    /**
     * String in eine Log Datei Speichern
     * Der String wird dem Inhalt der Datei hinzugefügt
     *
     * @param mytext  String, der gespeichert werden soll
     * @param context Ohne Context geht es nicht
     * @return boolean, ob die Operation erfolgreich war
     */
    private static boolean saveToFile(String mytext, Context context) {
        if (context == null)
            return false;
        try {
            FileOutputStream fos = context.openFileOutput(log_filename + ".txt", Context.MODE_PRIVATE);
            Writer out = new OutputStreamWriter(fos);
            out.write(loadFromFile(context) + mytext);
            out.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Log txt Datei auslesen
     *
     * @param context Ohne Context geht es nicht
     * @return String-Wert, der ausgelsen wird
     */
    private static String loadFromFile(Context context) {
        if (context == null)
            return null;
        try {
            FileInputStream fis = context.openFileInput(log_filename + ".txt");
            BufferedReader r = new BufferedReader(new InputStreamReader(fis));
            String line = r.readLine();
            r.close();
            return line;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * String in die StaredPreferences schreiben
     *
     * @param name        Name der Einstellung
     * @param stringValue String, der gespeichert werden soll
     * @param context     Ohne Context geht es nicht
     */
    public static void saveToSharedPreferences(String name, String stringValue, Context context) {
        if (context == null)
            return;
        // Access the default SharedPreferences
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        // The SharedPreferences editor - must use commit() to submit changes
        SharedPreferences.Editor editor = preferences.edit();
        // Edit the saved preferences
        editor.putString(name, stringValue);
        editor.commit();
    }

    /**
     * boolean in die StaredPreferences schreiben
     *
     * @param name         Name der Einstellung
     * @param booleanValue Wert, der gespeichert werden soll
     * @param context      Ohne Context geht es nicht
     */
    public static void saveToSharedPreferences(String name, boolean booleanValue, Context context) {
        if (context == null)
            return;
        // Access the default SharedPreferences
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        // The SharedPreferences editor - must use commit() to submit changes
        SharedPreferences.Editor editor = preferences.edit();
        // Edit the saved preferences
        editor.putBoolean(name, booleanValue);
        editor.commit();
    }

    /**
     * String aus den SharedPreferences auslesen
     *
     * @param name    Name der Einstellungen
     * @param context Ohne Context geht es nicht
     * @return gespeicherter String-Wert
     */
    public static String loadStringFromSharedPreferences(String name, Context context) {
        if (context == null)
            return null;
        // Access the default SharedPreferences
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        // load the saved preferences
        return preferences.getString(name, "");
    }

    /**
     * Boolean aus den SharedPreferences auslesen
     *
     * @param name    Name der Einstellungen
     * @param context Ohne Context geht es nicht
     * @return gespeicherter boolean-Wert
     */
    public static boolean loadBooleanFromSharedPreferences(String name, Context context) {
        if (context == null)
            return false;
        // Access the default SharedPreferences
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        // load the saved preferences
        return preferences.getBoolean(name, false);
    }

    /**
     * Lade Datei aus den Assets
     *
     * @param fileName Name der Datei
     * @param context  Ein context wird benötigt
     * @return String-Inhalt der Datei
     */
    public static String readFromAssetFile(String fileName, Context context) {
        StringBuilder returnString = new StringBuilder();
        InputStream fIn = null;
        InputStreamReader isr = null;
        BufferedReader input = null;
        try {
            fIn = context.getResources().getAssets()
                    .open(fileName, Context.MODE_WORLD_READABLE);
            isr = new InputStreamReader(fIn);
            input = new BufferedReader(isr);
            String line = "";
            while ((line = input.readLine()) != null) {
                returnString.append(line + "\n");
            }
        } catch (Exception e) {
            e.getMessage();
        } finally {
            try {
                if (isr != null)
                    isr.close();
                if (fIn != null)
                    fIn.close();
                if (input != null)
                    input.close();
            } catch (Exception e2) {
                e2.getMessage();
            }
        }
        return returnString.toString();
    }
}
