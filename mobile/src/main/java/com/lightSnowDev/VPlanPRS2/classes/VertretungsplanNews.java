package com.lightSnowDev.VPlanPRS2.classes;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created by hansi on 08.08.2017.
 */

public class VertretungsplanNews {

    private VertretungsplanNewsResult result;
    private String toReturn = "";

    public enum VertretungsplanNewsResult {normalInfo, noInfo, parseError}

    ;

    public VertretungsplanNews(String input) throws Exception {
        try {
            parse(input);
        } catch (Exception e) {
            result = VertretungsplanNewsResult.parseError;
            throw new RuntimeException("Error #VpN100");
        }
    }

    public VertretungsplanNews(String input, boolean isSetByStringRead) {
        if (!isSetByStringRead)
            throw new RuntimeException("ERROR USELESS");
        this.toReturn = input;
        if (!input.trim().isEmpty())
            result = VertretungsplanNewsResult.normalInfo;
        else
            result = VertretungsplanNewsResult.noInfo;
    }

    public void parse(String input) {
        Document doc = Jsoup.parse(input);
        //parse top information
        Elements spalte_info = doc.select("table.info");
        if (spalte_info.size() == 0) {
            result = VertretungsplanNewsResult.noInfo;
            return;
        } else {
            result = VertretungsplanNewsResult.normalInfo;
            toReturn = spalte_info.html();
        }
    }

    public VertretungsplanNewsResult getResult() {
        return result;
    }

    public String getHTML() {
        if (toReturn == null)
            return "";
        return toReturn;
    }
}
