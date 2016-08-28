package com.lightSnow.VPlanPRS.classes;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.R.id.list;

/**
 * Created by Jonathan on 06.01.2016.
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
    private static final String SPLIT_SYMBOL = "⛔";

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
                output.add(klasse);
            }
            return output;
        } catch (Exception e) {
            return output;
        }

    }

    public Vertretungsstunde(String compact) {
        try {
            String[] splitComapct = compact.split(SPLIT_SYMBOL);
            this.Klassen = parseKlassen(splitComapct[0]);
            this.Stunden = splitComapct[0];

            this.neuerLehrer = splitComapct[0];
            this.neuerRaum = splitComapct[0];

            this.alterRaum = splitComapct[0];
            this.alterLehrer = splitComapct[0];

            this.Vertretungstext = splitComapct[0];
            this.Fach = splitComapct[0];
        } catch (Exception e) {
            throw new RuntimeException("Error #100: Fehlerhafte Daten.");
        }
    }

    @Override
    public String toString() {
        return Klassen + SPLIT_SYMBOL +
                Stunden + SPLIT_SYMBOL +
                neuerLehrer + SPLIT_SYMBOL +
                neuerRaum + SPLIT_SYMBOL +
                alterRaum + SPLIT_SYMBOL +
                alterLehrer + SPLIT_SYMBOL +
                Vertretungstext + SPLIT_SYMBOL +
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
}
