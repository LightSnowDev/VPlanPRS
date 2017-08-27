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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * desc..
 * <p>
 * Created by Jonathan on 01.09.2016.
 */

public class VertretungsSeite {

    private List<VertretungsSeite.OnVPlanResultEvent> mOnVPlanResultlistenerList = new ArrayList<VertretungsSeite.OnVPlanResultEvent>();
    private List<VertretungsStunde> klassenStunden = new ArrayList<>();
    private VertretungsplanNews vertretungsplanNews;
    private Date publishDate;
    private Date parseDate;
    private Date relatedDate;
    private VertretungsTag.Day day;
    private Context context;

    public VertretungsSeite(VertretungsTag.Day day, Context context) {
        this.day = day;
        this.context = context;
        parseDate = new Date();
    }

    public VertretungsSeite(String stringToParse, VertretungsTag.Day day, Context context) {
        this(day, context);
        String[] splitted = stringToParse.split(StorageHelper.SPLIT_SYMBOL_VERTRETUNGSSEITE_DATEHEADER);
        try {
            vertretungsplanNews = new VertretungsplanNews(splitted[0], true);
            publishDate = parsePublishDateFromString(splitted[1]);
            relatedDate = parseRelatedDateFromString(splitted[2]);
            parseDate = parseParseDateFromString(splitted[3]);
        } catch (Exception e) {
            throw new RuntimeException("#VS102");
        }
        String[] splitted_mainData = splitted[4].split(StorageHelper.SPLIT_SYMBOL_VERTRETUNGSSTUNDE);
        for (String stundeString : splitted_mainData) {
            klassenStunden.add(new VertretungsStunde(stundeString));
        }
    }

    /**
     * Lade alle Seiten runter
     * kein filter, da alle Seiten gespeichert werden
     * um die klasse ändern zu können.
     * gefiltert wird erst in der PRS Klasse.
     *
     * @param inputUrl zb: "hvpm1.htm"
     */
    public void downloadSeite(String inputUrl) {
        DownloadHelper download = new DownloadHelper(context);
        download.setIfProgressDialogIsShown(false);
        download.setEncoding("iso-8859-1");
        download.addOnRecievedEvent(new DownloadHelper.OnRecievedEvent() {
            @Override
            public void recievedEvent(String resultString, String resultBase64, boolean success) {
                if (success) {
                    try {
                        parseNewsInformation(resultString);
                        parseDateInformation(resultString);
                        parseKlassenStunden(resultString);
                    } catch (Exception e) {
                        RunVPlanResultEvent(VertretungsplanResultType.parseError);
                    }
                    RunVPlanResultEvent(VertretungsplanResultType.success);
                } else {
                    RunVPlanResultEvent(VertretungsplanResultType.downloadError);
                }
            }
        });
        download.download(inputUrl, SensibleDataHelper.PRS_SERVER_USERNAME, SensibleDataHelper.PRS_SERVER_PASSWORD);
    }

    /**
     * Does not hande the parse Exceptions itself.
     */
    private void parseKlassenStunden(String input) throws Exception {
        Document doc = Jsoup.parse(input);
        //parse the Stunden
        Elements vplanElements = doc.select("tr.list.odd, tr.list.even");
        for (Element e : vplanElements) {
            VertretungsStunde v = new VertretungsStunde(e);
            klassenStunden.add(v);
        }
    }

    /**
     * Does not hande the parse Exceptions itself.
     */
    private void parseNewsInformation(String input) throws Exception {
        vertretungsplanNews = new VertretungsplanNews(input);
    }

    public VertretungsplanNews getVertretungsplanNews() {
        return vertretungsplanNews;
    }

    private String getRecursiveChildrenText(Element e) {
        String output = "";
        if (e == null || !e.hasText() || e.children().size() == 0)
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

    private void parseDateInformation(String input) throws Exception {
        try {
            //Veröffentlichungssdatum
            String splitted = input.split("Stand: ")[1].split("<p>\r\n<body bgcolor=\"#F0F0F0\">")[0];
            publishDate = parsePublishDateFromString(splitted);
            //Tag, den der Vplan betrifft
            Document doc = Jsoup.parse(input);
            String zugehörigerTagString = doc.select("div.mon_title").text().split(" ")[0];
            SimpleDateFormat dateFormat = new SimpleDateFormat("d.M.yyyy", Locale.GERMANY);
            relatedDate = dateFormat.parse(zugehörigerTagString);
        } catch (Exception e) {
            RunVPlanResultEvent(VertretungsplanResultType.parseError);
        }
    }

    //region /// getter ... ///

    public List<VertretungsStunde> getKlassenStunden() {
        return klassenStunden;
    }

    public Date getPublishDate() {
        return publishDate;
    }

    public Date getRelatedDateDate() {
        return relatedDate;
    }

    public Date parsePublishDateFromString(String input) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY);
        return dateFormat.parse(input);
    }

    public Date parseRelatedDateFromString(String input) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("d.M.yyyy", Locale.GERMANY);
        return dateFormat.parse(input);
    }

    public Date parseParseDateFromString(String input) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY);
        return dateFormat.parse(input);
    }

    public String getParseDateString() {
        return (new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY).format(parseDate));
    }

    public String getPublishDateString() {
        return (new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY).format(publishDate));
    }

    public String getRelatedDateString() {
        return (new SimpleDateFormat("d.M.yyyy", Locale.GERMANY).format(relatedDate));
    }
    //endregion

    //region /// OnVPlanResultEvent ... ///
    public void addOnVPlanResultEvent(VertretungsSeite.OnVPlanResultEvent listener) {
        mOnVPlanResultlistenerList.add(listener);
    }

    protected void RunVPlanResultEvent(VertretungsplanResultType resultType) {
        for (VertretungsSeite.OnVPlanResultEvent event : mOnVPlanResultlistenerList) {
            event.VPlanResultEvent(this, resultType);
        }
    }

    public enum VertretungsplanResultType {success, parseError, downloadError}

    public interface OnVPlanResultEvent {
        void VPlanResultEvent(VertretungsSeite seite, VertretungsplanResultType resultType);
    }
    //endregion

    @Override
    public String toString() {
        String toReturn = vertretungsplanNews.getHTML()
                + StorageHelper.SPLIT_SYMBOL_VERTRETUNGSSEITE_DATEHEADER
                + getPublishDateString()
                + StorageHelper.SPLIT_SYMBOL_VERTRETUNGSSEITE_DATEHEADER
                + getRelatedDateString()
                + StorageHelper.SPLIT_SYMBOL_VERTRETUNGSSEITE_DATEHEADER
                + getParseDateString()
                + StorageHelper.SPLIT_SYMBOL_VERTRETUNGSSEITE_DATEHEADER;
        if (klassenStunden.size() > 0) {
            for (VertretungsStunde stunde : klassenStunden) {
                toReturn += stunde.toString();
                toReturn += StorageHelper.SPLIT_SYMBOL_VERTRETUNGSSTUNDE;
            }
            toReturn = toReturn.substring(0, toReturn.length() - 1);
        }
        return toReturn;
    }
}
