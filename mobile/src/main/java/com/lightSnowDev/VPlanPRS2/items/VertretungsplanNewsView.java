package com.lightSnowDev.VPlanPRS2.items;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lightSnowDev.VPlanPRS2.R;
import com.lightSnowDev.VPlanPRS2.classes.VertretungsTag;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.Date;
import java.util.Locale;

/**
 * desc
 * <p>
 * Created by Jonathan on 03.08.2017.
 */

public class VertretungsplanNewsView extends RelativeLayout {

    public VertretungsplanNewsView(VertretungsTag.Day day, Date datum, String html, Activity activity) {
        super(activity);
        inflate(getContext(), R.layout.vertretungsplan_nachrichten_view_layout, this);

        init(day, datum, html);
    }

    private void init(VertretungsTag.Day day, Date datum, String html) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM", Locale.GERMANY);
        SimpleDateFormat dateFormatWeek = new SimpleDateFormat("w", Locale.GERMANY);
        ((TextView) findViewById(R.id.vertretungsplan_news_view_textView_headline)).setText(
                "News für \""
                        + day.name()
                        + "\" den "
                        + dateFormat.format(datum)
                        + ", Woche "
                        + dateFormatWeek.format(datum)
        );

        WebView view = ((WebView) findViewById(R.id.vertretungsplan_news_view_textView_moreInformation));
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        view.loadDataWithBaseURL("", combineHtmlCss(html), "text/html", "iso-8859-1", "");
    }

    private String combineHtmlCss(String input) {
        input = input.replace("<th class=\"info\" align=\"center\" colspan=\"2\">Nachrichten zum Tag</th>", "");
        return "<style type=\"text/css\">\n" +
                "    body {\n" +
                "        margin: 8px;\n" +
                "        background: #FFECB3;\n" +
                "        color: #272727;\n" +
                "        font: 80% Arial, Helvetica, sans-serif;\n" +
                "    }\n" +
                "    \n" +
                "    th {\n" +
                "        background: #000;\n" +
                "        color: #fff;\n" +
                "    }\n" +
                "    \n" +
                "    .inline_header {\n" +
                "        font-weight: bold;\n" +
                "    }\n" +
                "    \n" +
                "    table.info {\n" +
                "        width: 100%;\n" +
                "        height: auto;\n" +
                "        color: #000000;\n" +
                "        font-size: 100%;\n" +
                "        border: 1px;\n" +
                "        border-style: solid;\n" +
                "        border-collapse: collapse;\n" +
                "    }\n" +
                "    \n" +
                "    td.info {\n" +
                "        background: #fff;\n" +
                "        border: 1px;\n" +
                "        border-style: solid;\n" +
                "        border-color: black;\n" +
                "        margin: 0px;\n" +
                "        border-collapse: collapse;\n" +
                "        padding: 3px;\n" +
                "    }\n" +
                "</style>\n" +
                "\n" +
                "<table class=\"info\">\n" +
                input +
                "</table>";
    }
}
