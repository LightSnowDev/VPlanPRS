package com.lightSnow.VPlanPRS.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
    public final static String VPLAN_USER_AUTO_UPDATE = "vplan_user_auto_update";
    public final static String VPLAN_FIRST_START = "vplan_user_first_start";
    /*
     * Variablen für gcm
     */
    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    /*
     * Allgemeine Variablen
     */
    public static final String SPLIT_SYMBOL_VERTRETUNGSSTUNDE_ITEM = "⦿";
    public static final String SPLIT_SYMBOL_VERTRETUNGSSTUNDE_DETAILS = "│";

    /*
     * Einen String möglichst sicher speichern. Dabei werdern 2 Methoden verwendet:
     * 1. Als Datei speichern.
     * 2. In den SharedPreferences speichern.
     * #doppelthältbesser
     */
    public static void logAll(String input, Context context) {
        saveToFile(input, context);
        loadFromFile(context);
    }

    /*
     * String in eine Log Datei Speichern.
     * Der String wird dem Inhalt der Datei hinzugefügt.
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

    /*
     * Log txt Datei auslesen
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

    /*
     * String in die StaredPreferences schreiben.
     */
    public static void saveToSharedPreferences(String name, String myText, Context context) {
        if (context == null)
            return;
        // Access the default SharedPreferences
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        // The SharedPreferences editor - must use commit() to submit changes
        SharedPreferences.Editor editor = preferences.edit();
        // Edit the saved preferences
        editor.putString(name, myText);
        editor.commit();
    }

    /*
    * boolean in die StaredPreferences schreiben.
    */
    public static void saveToSharedPreferences(String name, boolean b, Context context) {
        if (context == null)
            return;
        // Access the default SharedPreferences
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        // The SharedPreferences editor - must use commit() to submit changes
        SharedPreferences.Editor editor = preferences.edit();
        // Edit the saved preferences
        editor.putBoolean(name, b);
        editor.commit();
    }

    /*
     * String aus den SharedPreferences auslesen
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

    /*
    * Boolean aus den SharedPreferences auslesen
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
}
