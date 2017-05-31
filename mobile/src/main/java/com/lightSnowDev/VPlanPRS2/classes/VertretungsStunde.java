package com.lightSnowDev.VPlanPRS2.classes;

import android.content.Context;
import android.text.TextUtils;

import com.lightSnowDev.VPlanPRS2.helper.StorageHelper;

import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jonathan Schwarzenböck on 06.01.2016.
 */
public class VertretungsStunde {

    private String neuerLehrer;
    private String alterLehrer;
    private List<String> klassen;
    private String stunden;
    private String neuerRaum;
    private String alterRaum;
    private String vertretungstext;
    private String fach;
    private boolean isSchulnachrichten = false;
    private VertretungsTag.Day day;

    public VertretungsStunde(String Klassen, String Stunden,
                             String neuerLehrer, String neuerRaum,
                             String alterRaum, String alterLehrer,
                             String Vertretungstext, String Fach,
                             VertretungsTag.Day day) {
        try {
            this.neuerLehrer = neuerLehrer;
            this.alterLehrer = alterLehrer;
            this.klassen = parseKlassen(Klassen);
            this.stunden = Stunden;
            this.alterRaum = alterRaum;
            this.neuerRaum = neuerRaum;
            this.vertretungstext = Vertretungstext;
            this.fach = Fach;
            this.day = day;
        } catch (Exception e) {
            throw new RuntimeException("Error #100: Fehlerhafte Daten.");
        }
    }

    public VertretungsStunde(Element e, VertretungsTag.Day day) {
        //in html:  0.Klasse(n)	1. Std.	2.Vertreter	3.Raum	 4.(Raum)	5.(Lehrer)	6.Vertretungs-Text	7.(Fach)	8.(Klasse(n))
        //in klasse: (String Klassen, String Stunden, String neuerLehrer, String neuerRaum, String alterRaum, String alterLehrer, String Vertretungstext, String Fach)
        this(e.child(0).text(),
                e.child(1).text(),
                e.child(2).text(),
                e.child(3).text(),
                e.child(4).text(),
                e.child(5).text(),
                e.child(6).text(),
                e.child(7).text(),
                day);
    }

    public VertretungsStunde(String Klassen, String Stunden,
                             String neuerLehrer, String neuerRaum,
                             String alterRaum, String alterLehrer,
                             String Vertretungstext, String Fach,
                             String dayString) {
        this(Klassen, Stunden, neuerLehrer, neuerRaum, alterRaum, alterLehrer, Vertretungstext, Fach,
                VertretungsTag.Day.valueOf(dayString));
    }

    public VertretungsStunde(String compact) {
        try {
            String[] splitComapct = compact.split(StorageHelper.SPLIT_SYMBOL_VERTRETUNGSSTUNDE_DETAILS);
            this.klassen = parseKlassen(splitComapct[0]);
            this.stunden = splitComapct[1];

            this.neuerLehrer = splitComapct[2];
            this.neuerRaum = splitComapct[3];

            this.alterRaum = splitComapct[4];
            this.alterLehrer = splitComapct[5];

            this.vertretungstext = splitComapct[6];
            this.fach = splitComapct[7];

            this.day = VertretungsTag.Day.valueOf(splitComapct[8]);
        } catch (Exception e) {
            throw new RuntimeException("Error #100: Fehlerhafte Daten.");
        }
    }

    public static VertretungsTag.Day getVertretungsstundeTagFromString(String input) {
        if (input.equals("heute"))
            return VertretungsTag.Day.heute;
        else if (input.equals("morgen"))
            return VertretungsTag.Day.morgen;
        else if (input.equals("beide"))
            return VertretungsTag.Day.beide;
        else
            return null;
    }

    public static boolean areVertretungsstundenTheSame(List<VertretungsStunde> v1, List<VertretungsStunde> v2) {
        // Optional quick test since size must match
        if (v1.size() != v2.size()) {
            return false;
        }
        for (VertretungsStunde stunde1 : v1) {
            for (VertretungsStunde stunde2 : v2) {
                if (!stunde1.toString().equals(stunde2.toString())) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isNullOrWhitespace(CharSequence value) {
        if (value == null)
            return true;
        value = value.toString().replaceFirst("^[\\x00-\\x200\\xA0]+", "").replaceFirst("[\\x00-\\x20\\xA0]+$", "");

        for (int i = 0; i < value.length(); i++) {
            if (!Character.isWhitespace(value.charAt(i)) && String.valueOf(value.charAt(i)).replace('\u00A0', ' ').trim().length() != 0 &&
                    value.charAt(i) != "\u00a0".charAt(0)) {
                return false;
            }
        }

        return value.toString().isEmpty();
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

    @Override
    public String toString() {
        // Die tag = "" default eigenschaft wurde ohne wissen eingefügt. Sie könnte fehler produzieren.
        // Vorher war nur "tag.toString()" vorhanden und hat somit nullpointer exceptions geworfen.
        String tagString = "";
        if (day != null)
            tagString = day.toString();
        return getKlassenString() + StorageHelper.SPLIT_SYMBOL_VERTRETUNGSSTUNDE_DETAILS +
                stunden + StorageHelper.SPLIT_SYMBOL_VERTRETUNGSSTUNDE_DETAILS +
                neuerLehrer + StorageHelper.SPLIT_SYMBOL_VERTRETUNGSSTUNDE_DETAILS +
                neuerRaum + StorageHelper.SPLIT_SYMBOL_VERTRETUNGSSTUNDE_DETAILS +
                alterRaum + StorageHelper.SPLIT_SYMBOL_VERTRETUNGSSTUNDE_DETAILS +
                alterLehrer + StorageHelper.SPLIT_SYMBOL_VERTRETUNGSSTUNDE_DETAILS +
                vertretungstext + StorageHelper.SPLIT_SYMBOL_VERTRETUNGSSTUNDE_DETAILS +
                fach + StorageHelper.SPLIT_SYMBOL_VERTRETUNGSSTUNDE_DETAILS +
                tagString;
    }

    /*
     * Does this VertretungsStunde affect inputKlasse?
     */
    public boolean matchesKlasse(String inputKlasse) {
        for (String klasse : klassen) {
            // !! unbekannte klassen werden trotzdem angezeigt !!
            if (klasse.equals("") || klasse.equals(" ") || klasse.equals(inputKlasse))
                return true;
        }
        return false;
    }

    public String getKlassenString() {
        if (klassen.isEmpty())
            return "";
        else
            return TextUtils.join(",", klassen);
    }

    public boolean neuerLehrerIsNew() {
        return !neuerLehrer.equals(alterLehrer);
    }

    public boolean stundeIsNotHappening() {
        return neuerLehrer.equals("+");
    }

    public boolean neuerRaumIsNew() {
        return !neuerRaum.equals(alterRaum);
    }

    public boolean vertretungsTextIsEmpty() {
        return isNullOrWhitespace(vertretungstext);
    }

    public boolean isSubjectEmpty() {
        return isNullOrWhitespace(fach);
    }

    public boolean klassenIsEmpty() {
        if (klassen == null || klassen.size() == 0)
            return true;
        boolean onlyWhitespace = true;
        for (String i : klassen)
            if (!isNullOrWhitespace(i))
                onlyWhitespace = false;
        return onlyWhitespace;
    }

    /*
     * Abkürzung eines Faches in den Namen konvertieren.
     */
    public String getRealFachName() {
        // @formatter:off
        switch (fach) {
            case "ENG":
                return "Englisch";
            case "PHY":
                return "Physik";
            case "PHYB":
                return "Physik B-LK";
            case "PHYL":
                return "Physik LK";
            case "DEU":
                return "Deutsch";
            case "POW":
                return "Politik und Wirtschaft";
            case "EVR":
                return "Ev. Religion";
            case "KAT":
                return "Kat. Religion";
            case "ETH":
                return "Ethik";
            case "SPO":
                return "Sport";
            case "LAT":
                return "Latein";
            case "GES":
                return "Geschichte";
            case "KUN":
                return "Kunst";
            case "MAT":
                return "Mathe";
            case "ERD":
                return "Erdkunde";
            case "BIO":
                return "Biologie";
            case "FRZ":
                return "Französisch";
            case "SPA":
                return "Spanisch";
            case "KUNL":
                return "Kunst LK";
            case "ENGL":
                return "Englisch LK";
            case "ENGB":
                return "Englisch B-LK";
            case "DEUL":
                return "Deutsch LK";
            case "GESL":
                return "Geschichte LK";
            case "MATL":
                return "Mathe LK";
            case "BIOL":
                return "Biologie LK";
            case "BIOB":
                return "Biologie B-LK";
            case "POWL":
                return "PoWi LK";
            case "POWB":
                return "PoWi B-LK";
            case "KUNB":
                return "Kunst B-LK";
            case "MATB":
                return "Mathe B-LK";
            case "CHEB":
                return "Chemie B-LK";
            case "INF":
                return "Informatik";
            case "MUS":
                return "Musik";
            case "KTR":
                return "Kat. Religion";
            case "SWI":
                return "Schwimmen";
            case "AUS":
                return "Auszeitraum";
            case "Wahl":
                return "Wahlfach";
            case "DSP":
                return "Darstellendes Spiel";
            case "":
                return "";
            //... needs more subjects
            default:
                return fach;
        }
        // @formatter:on
    }

    private String kuerzelToFullLehrerName(String input) {
        // @formatter:off
        switch (input) {
            case "AR":
                return "Ahlemeyer (AR)";
            case "AGN":
                return "Aigner (AGN)";
            case "AIG":
                return "Aigner (AIG)";
            case "AT":
                return "Albrecht (AT)";
            case "ALT":
                return "Altintas (ALT)";
            case "AD":
                return "Aschendorf (AD)";
            case "BH":
                return "Dr.Barth (BH)";
            case "BI":
                return "Barbotin (BI)";
            case "BK":
                return "Barwinek (BK)";
            case "BA":
                return "Bauer (BA)";
            case "BY":
                return "Baysal (BY)";
            case "BD":
                return "Bechtold (BD)";
            case "BER":
                return "Behr (BER)";
            case "BB":
                return "Bensberg (BB)";
            case "BGH":
                return "Dr.Berghäuser (BGH)";
            case "BT":
                return "Bertsch (BT)";
            case "BL":
                return "Bögel (BL)";
            case "BO":
                return "Boomgaarden (BO)";
            case "BON":
                return "Born (BON)";
            case "BRN":
                return "Brünner (BRN)";
            case "BN":
                return "Buschmann (BN)";
            case "CY":
                return "Canenbley (CY)";
            case "CA":
                return "Cati (CA)";
            case "CC":
                return "Convertino (CC)";
            case "CM":
                return "Cullmann (CM)";
            case "DB":
                return "DeBoer (DB)";
            case "DK":
                return "Dr.Denk (DK)";
            case "DI":
                return "Dierschke (DI)";
            case "DZ":
                return "Drewanz (DZ)";
            case "ED":
                return "Edelmann (ED)";
            case "ER":
                return "Edler (ER)";
            case "EN":
                return "Dr.Eißner (EN)";
            case "EK":
                return "Engelke (EK)";
            case "ENZ":
                return "Enzmann (ENZ)";
            case "EL":
                return "Euler (EL)";
            case "FX":
                return "Faix (FX)";
            case "FD":
                return "Fiedler (FD)";
            case "FI":
                return "Dr.Finger (FI)";
            case "FLA":
                return "Flasch (FLA)";
            case "FLH":
                return "Flasch (FLH)";
            case "FRI":
                return "Friedek (FRI)";
            case "FH":
                return "Fröhlich (FH)";
            case "GA":
                return "Gabel (GA)";
            case "GZ":
                return "Gerz (GZ)";
            case "GF":
                return "Graf (GF)";
            case "GI":
                return "Gramowski (GI)";
            case "GP":
                return "Gratopp (GP)";
            case "GRE":
                return "Greif (GRE)";
            case "GO":
                return "Grove (GO)";
            case "HHN":
                return "Hahn (HHN)";
            case "HN":
                return "Hauptmann (HN)";
            case "HC":
                return "Hedderich-Cöster (HC)";
            case "HM":
                return "Heilmann (HM)";
            case "HS":
                return "Heß (HS)";
            case "HO":
                return "Hollenstein (HO)";
            case "HR":
                return "Hübner-Exel (HR)";
            case "HU":
                return "Humm (HU)";
            case "JK":
                return "Jakob (JK)";
            case "JH":
                return "JostvonHayn (JH)";
            case "HEL":
                return "Jung (HEL)";
            case "KB":
                return "Käberich (KB)";
            case "KNR":
                return "Kappner (KNR)";
            case "KS":
                return "Kaps (KS)";
            case "KR":
                return "Kasper (KR)";
            case "KL":
                return "Klein (KL)";
            case "KNE":
                return "Knebel (KNE)";
            case "K":
                return "Kobs (K)";
            case "KN":
                return "Koppmann (KN)";
            case "KK":
                return "Kowalczyk (KK)";
            case "KW":
                return "Kowalewski (KW)";
            case "KRE":
                return "Krause-Zeiß (KRE)";
            case "KRS":
                return "Kreß (KRS)";
            case "KC":
                return "Kröcker (KC)";
            case "LR":
                return "Lechthaler (LR)";
            case "LW":
                return "Lehwalder (LW)";
            case "LE":
                return "Lenz (LE)";
            case "LIP":
                return "Liepe (LIP)";
            case "LB":
                return "Limbacher (LB)";
            case "LST":
                return "Ludwig-Stein (LST)";
            case "LU":
                return "Ludig (LU)";
            case "MH":
                return "Maibach (MH)";
            case "MR":
                return "Maurer (MR)";
            case "MK":
                return "Mecke (MK)";
            case "MEL":
                return "Mehler (MEL)";
            case "ME":
                return "Meier (ME)";
            case "MEB":
                return "Mertiny-Berg (MEB)";
            case "MI":
                return "Miladi (MI)";
            case "MER":
                return "Müller (MER)";
            case "MO":
                return "Moser (MO)";
            case "NÄ":
                return "Nägle (NÄ)";
            case "NM":
                return "Naim (NM)";
            case "NEU":
                return "Neumann (NEU)";
            case "NH":
                return "Nikisch (NH)";
            case "PB":
                return "Pabst (PB)";
            case "PW":
                return "Pawlytta (PW)";
            case "PN":
                return "Perrin (PN)";
            case "P":
                return "Pflanzl (P)";
            case "PA":
                return "Piecha (PA)";
            case "PH":
                return "Plüntsch (PH)";
            case "PS":
                return "Plüntsch (PS)";
            case "PI":
                return "Proszowski (PI)";
            case "RS":
                return "Ramos-Izquierdo (RS)";
            case "RAU":
                return "Rau (RAU)";
            case "RT":
                return "Rebuschat (RT)";
            case "RH":
                return "Rehm (RH)";
            case "REI":
                return "Reimann (REI)";
            case "RM":
                return "Reimers (RM)";
            case "RL":
                return "Reul (RL)";
            case "SF":
                return "SaghafeeYazdi (SF)";
            case "SAT":
                return "Sattler (SAT)";
            case "SC":
                return "Schalk (SC)";
            case "SR":
                return "Schauer (SR)";
            case "SLO":
                return "Schlosser (SLO)";
            case "SMI":
                return "Schmidt (SMI)";
            case "SI":
                return "Schmitt (SI)";
            case "SO":
                return "Schott (SO)";
            case "SDR":
                return "Schröder (SDR)";
            case "SLG":
                return "Seeling (SLG)";
            case "SA":
                return "Sekula (SA)";
            case "SL":
                return "Sennlaub (SL)";
            case "ST":
                return "Siebert (ST)";
            case "SH":
                return "Sohn (SH)";
            case "SON":
                return "Sondermann (SON)";
            case "SB":
                return "Staab (SB)";
            case "STA":
                return "Stamboulidis (STA)";
            case "STK":
                return "Steinke (STK)";
            case "STE":
                return "Steinmüller (STE)";
            case "BS":
                return "Steuer (BS)";
            case "STM":
                return "Sturm (STM)";
            case "TM":
                return "Thalheim (TM)";
            case "TI":
                return "Tokai (TI)";
            case "TR":
                return "Tratberger (TR)";
            case "TYL":
                return "Tylewski (TYL)";
            case "VT":
                return "Vogt (VT)";
            case "VL":
                return "vonLüde (VL)";
            case "WE":
                return "Weber (WE)";
            case "WG":
                return "Weisenburger (WG)";
            case "WH":
                return "Wengenroth (WH)";
            case "WI":
                return "Wichert (WI)";
            case "WA":
                return "Wiecha (WA)";
            case "WHA":
                return "Wihan (WHA)";
            case "WX":
                return "Wirxel (WX)";
            case "ZAK":
                return "Zakrzewski (ZAK)";
            case "ZE":
                return "Zeiß (ZE)";
            default:
                return input;
        }
        // @formatter:on
    }

    public String getNeuerLehrer(Context context) {
        if (StorageHelper.loadBooleanFromSharedPreferences(StorageHelper.VPLAN_USER_LEHRER_NAME_KUERZEL, context))
            return kuerzelToFullLehrerName(neuerLehrer);
        else
            return neuerLehrer;
    }

    public String getAlterLehrer(Context context) {
        if (StorageHelper.loadBooleanFromSharedPreferences(StorageHelper.VPLAN_USER_LEHRER_NAME_KUERZEL, context))
            return kuerzelToFullLehrerName(alterLehrer);
        else
            return alterLehrer;
    }

    public List<String> getKlassen() {
        return klassen;
    }

    public String getStunden() {
        return stunden;
    }

    public String getNeuerRaum() {
        return neuerRaum;
    }

    public String getAlterRaum() {
        return alterRaum;
    }

    public boolean getNeuerRaumIsEmpty() {
        return isNullOrWhitespace(neuerRaum);
    }

    public boolean getAlterRaumIsEmpty() {
        return isNullOrWhitespace(alterRaum);
    }

    public String getVertretungstext() {
        return vertretungstext;
    }

    public String getFach() {
        return fach;
    }
}
