package com.lightSnow.VPlanPRS.classes;

import android.text.TextUtils;

import com.lightSnow.VPlanPRS.helper.StorageHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.R.id.list;

/**
 * Created by Jonathan Schwarzenböck on 06.01.2016.
 */
public class Vertretungsstunde {
    public String neuerLehrer;
    public String alterLehrer;
    public List<String> Klassen;
    public String Stunden;
    public String neuerRaum;
    public String alterRaum;
    public String Vertretungstext;
    public String Fach;
    private boolean isSchulnachrichten = false;

    //in html:
    // 0.Klasse(n)	1. Std.
    // 2.Vertreter	3.Raum
    // 4.(Raum)	5.(Lehrer)
    // 6.Vertretungs-Text	7.(Fach)
    // 8.(Klasse(n))
    public Vertretungsstunde(String Klassen, String Stunden,
                             String neuerLehrer, String neuerRaum,
                             String alterRaum, String alterLehrer,
                             String Vertretungstext, String Fach) {
        this.neuerLehrer = neuerLehrer;
        this.alterLehrer = alterLehrer;
        this.Klassen = parseKlassen(Klassen);
        this.Stunden = Stunden;
        this.alterRaum = alterRaum;
        this.neuerRaum = neuerRaum;
        this.Vertretungstext = Vertretungstext;
        this.Fach = Fach;
    }

    private List<String> parseKlassen(String input) {
        List<String> output = new ArrayList<>();
        try {
            String[] klassenSplitted = input.split(",");
            for (String klasse : klassenSplitted) {
                output.add(klasse.replace(" ", ""));
            }
            return output;
        } catch (Exception e) {
            return output;
        }

    }

    public Vertretungsstunde(String compact) {
        try {
            String[] splitComapct = compact.split(StorageHelper.SPLIT_SYMBOL_VERTRETUNGSSTUNDE_DETAILS);
            this.Klassen = parseKlassen(splitComapct[0]);
            this.Stunden = splitComapct[1];

            this.neuerLehrer = splitComapct[2];
            this.neuerRaum = splitComapct[3];

            this.alterRaum = splitComapct[4];
            this.alterLehrer = splitComapct[5];

            this.Vertretungstext = splitComapct[6];
            this.Fach = splitComapct[7];
        } catch (Exception e) {
            throw new RuntimeException("Error #100: Fehlerhafte Daten.");
        }
    }

    @Override
    public String toString() {
        return getKlassenString() + StorageHelper.SPLIT_SYMBOL_VERTRETUNGSSTUNDE_DETAILS +
                Stunden + StorageHelper.SPLIT_SYMBOL_VERTRETUNGSSTUNDE_DETAILS +
                neuerLehrer + StorageHelper.SPLIT_SYMBOL_VERTRETUNGSSTUNDE_DETAILS +
                neuerRaum + StorageHelper.SPLIT_SYMBOL_VERTRETUNGSSTUNDE_DETAILS +
                alterRaum + StorageHelper.SPLIT_SYMBOL_VERTRETUNGSSTUNDE_DETAILS +
                alterLehrer + StorageHelper.SPLIT_SYMBOL_VERTRETUNGSSTUNDE_DETAILS +
                Vertretungstext + StorageHelper.SPLIT_SYMBOL_VERTRETUNGSSTUNDE_DETAILS +
                Fach;
    }

    /*
     * Does this Vertretungsstunde affect inputKlasse?
     */
    public boolean matchesKlasse(String inputKlasse) {
        for (String klasse : Klassen) {
            // !! unbekannte klassen werden trotzdem angezeigt !!
            if (klasse.equals("") || klasse.equals(" ") || klasse.equals(inputKlasse))
                return true;
        }
        return false;
    }

    public String getKlassenString() {
        return TextUtils.join(",", Klassen);
    }

    public static boolean areVertretungsstundenTheSame(List<Vertretungsstunde> v1, List<Vertretungsstunde> v2) {
        // Optional quick test since size must match
        if (v1.size() != v2.size()) {
            return false;
        }
        for (Vertretungsstunde stunde1 : v1) {
            for (Vertretungsstunde stunde2 : v2) {
                if (!stunde1.toString().equals(stunde2.toString())) {
                    return false;
                } else {
                    ;
                }
            }
        }
        return true;
    }
}
