package org.inwi.finanzentablet.helper;

import android.app.Activity;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Jonathan on 06.05.2016.
 */
public class JSONHelper {

    /*
     * returns null if login failed
     * return token if successful
     */
    public static String parseLogIn(String inputJSON) {
        try {
            JSONObject obj = new JSONObject(inputJSON);
            boolean success = obj.getBoolean("success");
            if (success)
                return obj.getString("token");
            else
                return null;

        } catch (JSONException exJSON) {
            Log.d("PARSE JSONException", exJSON.getLocalizedMessage());
            throw new RuntimeException("JSON parse error. " +
                    "Bitte dem Admin (webbau@inwi.org) melden.'");
        }
    }
}
