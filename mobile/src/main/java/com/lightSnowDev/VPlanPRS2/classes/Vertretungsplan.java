package com.lightSnowDev.VPlanPRS2.classes;

import android.content.Context;

import com.lightSnowDev.VPlanPRS2.helper.DownloadHelper;
import com.lightSnowDev.VPlanPRS2.helper.SensibleDataHelper;
import com.lightSnowDev.VPlanPRS2.helper.StorageHelper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.lightSnowDev.VPlanPRS2.classes.Vertretungsplan.VertretungsplanResultType.filteredNotUsed;

/**
 * Created by Jonathan on 01.09.2016.
 */

public class Vertretungsplan {

    //region /// OnVPlanResultEvent ... ///
    List<Vertretungsplan.OnVPlanResultEvent> mOnVPlanResultlistenerList = new ArrayList<Vertretungsplan.OnVPlanResultEvent>();
    private List<Vertretungsstunde> klassenStunden = new ArrayList<>();
    private List<Vertretungsstunde> unbekannteStunden = new ArrayList<>();
    private List<Vertretungsstunde> nichtRelevanteStunden = new ArrayList<>();
    private String informationenText;
    private String lastDateInformationText;
    private String klasse;
    private Vertretungsstunde.Tag tag;
    private Context activity;
    private boolean filterKlassen = true;

    public Vertretungsplan(String klasse, Vertretungsstunde.Tag tag, Context activity) {
        this.activity = activity;
        this.klasse = klasse;
        this.tag = tag;
    }

    private void parseKlassenStunden(String input) {
        try {
            Document doc = Jsoup.parse(input);
            //parse the Stunden
            Elements vplanElements = doc.select("tr.list.odd, tr.list.even");
            for (Element e : vplanElements) {
                Vertretungsstunde v = new Vertretungsstunde(e, tag);
                //If class is empty it is not filtered
                if (!filterKlassen) {
                    //klassen are NOT being filtered
                    klassenStunden.add(v);
                } else {
                    //klassen are being filtered
                    if (v.matchesKlasse(klasse))
                        //Klassen match filtered name
                        klassenStunden.add(v);
                    else if (v.klassenIsEmpty())
                        //Klassenname is unknown
                        unbekannteStunden.add(v);
                    else
                        nichtRelevanteStunden.add(v);
                }
            }
        } catch (Exception e) {
            RunVPlanResultEvent(VertretungsplanResultType.parseError);
        }
    }

    private void parseNewsInformation(String input) {
        try {
            Document doc = Jsoup.parse(input);
            //parse top information
            Elements elements_info = doc.select("table.info");
            String sumText = getRecursiveChildrenText(elements_info.first());
            sumText += "ende";
            //not null and more than whitespaces
            if (sumText != null && !sumText.matches("\\s")) {
                informationenText = sumText;
            }
        } catch (Exception e) {
            //No parse error here, since this is not this important.
            //RunVPlanResultEvent(VertretungsplanResultType.parseError);
        }
    }

    private String getRecursiveChildrenText(Element e) {
        String output = "";
        if (e == null || e.hasText() == false || e.children().size() == 0)
            return null;
        for (Node c : e.childNodes()) {
            if (c instanceof TextNode) {
                output += ((TextNode) c).text();
            } else if (c instanceof Element) {
                Element d = (Element) c;
                if (d.children().size() > 9)
                    output += " ";
                if (d.children().size() > 0)
                    output += getRecursiveChildrenText(d);
                else
                    output += d.text() + "\n";
            } else {
                output += c.toString();
            }
        }

        return output;
    }

    private void parseDateInformation(String input) {
        try {
            Document doc = Jsoup.parse(input);
            //parse top information
            Elements elements_info = doc.select("table.info");

            //parse the Stunden
            Element datumElement = doc.select("div.mon_title").first();
            String datumString = datumElement.text().split(",")[0];//(" \\(")[0];
            //save the last date a new plan was released and downloaded
            StorageHelper.saveToSharedPreferences(StorageHelper.VPLAN_LAST_UPDATE_PARSED, datumString, activity);
            lastDateInformationText = datumString;
        } catch (Exception e) {
            //No parse error here, since this is not this important.
            //RunVPlanResultEvent(VertretungsplanResultType.parseError);
        }
    }

    public void downloadVPlan(Element e) {
        String currentLink = e.attr("href");
        if (currentLink.isEmpty()) {
            RunVPlanResultEvent(VertretungsplanResultType.noStunden);
            return;
        }
        //filter the day: skip if (tag != link)
        if ((tag == Vertretungsstunde.Tag.heute && currentLink.contains("mvp")) ||
                (tag == Vertretungsstunde.Tag.morgen && currentLink.contains("hvp"))) {
            RunVPlanResultEvent(filteredNotUsed);
            return;
        }

        //download content of the links
        DownloadHelper download = new DownloadHelper(activity);
        download.setIfProgressDialogIsShown(false);
        download.setEncoding("iso-8859-1");
        download.addOnRecievedEvent(new DownloadHelper.OnRecievedEvent() {
            @Override
            public void recievedEvent(String resultString, String resultBase64, boolean success) {
                if (success) {
                    parseNewsInformation(resultString);
                    parseDateInformation(resultString);
                    parseKlassenStunden(resultString);
                    RunVPlanResultEvent(VertretungsplanResultType.success);
                } else {
                    RunVPlanResultEvent(VertretungsplanResultType.downloadError);
                }
            }
        });
        download.download(currentLink, SensibleDataHelper.PRS_SERVER_USERNAME, SensibleDataHelper.PRS_SERVER_PASSWORD);
    }


    public String getInformationenText() {
        return informationenText;
    }

    public List<Vertretungsstunde> getUnbekannteStunden() {
        Collections.sort(unbekannteStunden, new Comparator<Vertretungsstunde>() {
            @Override
            public int compare(Vertretungsstunde fruit2, Vertretungsstunde fruit1) {
                return fruit1.toString().compareTo(fruit2.toString());
            }
        });
        Collections.reverse(unbekannteStunden);
        return unbekannteStunden;
    }

    public List<Vertretungsstunde> getKlassenStunden() {
        Collections.sort(klassenStunden, new Comparator<Vertretungsstunde>() {
            @Override
            public int compare(Vertretungsstunde fruit2, Vertretungsstunde fruit1) {
                return fruit1.toString().compareTo(fruit2.toString());
            }
        });
        Collections.reverse(klassenStunden);
        return klassenStunden;
    }

    public boolean isFilterKlassen() {
        return filterKlassen;
    }

    public void setFilterKlassen(boolean filterKlassen) {
        this.filterKlassen = filterKlassen;
    }

    public List<Vertretungsstunde> getNichtRelevanteStunden() {
        return nichtRelevanteStunden;
    }

    public void addOnVPlanResultEvent(Vertretungsplan.OnVPlanResultEvent listener) {
        mOnVPlanResultlistenerList.add(listener);
    }

    protected void RunVPlanResultEvent(VertretungsplanResultType resultType) {
        for (Vertretungsplan.OnVPlanResultEvent event : mOnVPlanResultlistenerList) {
            event.VPlanResultEvent(this, resultType);
        }
    }

    public enum VertretungsplanResultType {success, noStunden, parseError, downloadError, filteredNotUsed}

    public interface OnVPlanResultEvent {
        void VPlanResultEvent(Vertretungsplan plan, VertretungsplanResultType resultType);
    }
//endregion
}
