package com.lightSnowDev.VPlanPRS2.items;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lightSnowDev.VPlanPRS2.R;
import com.lightSnowDev.VPlanPRS2.classes.Vertretungsstunde;
import com.lightSnowDev.VPlanPRS2.helper.StorageHelper;

/**
 * Created by Jonathan on 24.08.2016.
 */
public class VertretungsplanView extends RelativeLayout {

    Vertretungsstunde vertretungsstunde;
    OnClickListener listener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (findViewById(R.id.vertretungsplan_view_textView_moreInformation).getVisibility() == VISIBLE) {
                findViewById(R.id.vertretungsplan_view_textView_moreInformation).setVisibility(GONE);
                findViewById(R.id.vertretungsplan_view_textView_moreInformation2).setVisibility(VISIBLE);
                ((ImageButton) findViewById(R.id.vplan_item_Button_expand)).setImageResource(R.drawable.ic_keyboard_arrow_up_black_48dp);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) findViewById(R.id.vplan_item_Button_expand).getLayoutParams();
                params.addRule(RelativeLayout.BELOW, R.id.LinearLayout_vertretungsplan_list);
            } else {
                findViewById(R.id.vertretungsplan_view_textView_moreInformation).setVisibility(VISIBLE);
                findViewById(R.id.vertretungsplan_view_textView_moreInformation2).setVisibility(GONE);
                ((ImageButton) findViewById(R.id.vplan_item_Button_expand)).setImageResource(R.drawable.ic_keyboard_arrow_down_black_48dp);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) findViewById(R.id.vplan_item_Button_expand).getLayoutParams();
                params.addRule(RelativeLayout.BELOW, R.id.LinearLayout_vertretungsplan_list);
            }
        }
    };

    public VertretungsplanView(Vertretungsstunde vertretungsstunde, Activity activity) {
        super(activity);
        inflate(getContext(), R.layout.vertretungsplan_view_layout, this);
        this.vertretungsstunde = vertretungsstunde;
        this.setOnClickListener(listener);
        findViewById(R.id.vplan_item_Button_expand).setOnClickListener(listener);
        setSmallText();
    }

    public VertretungsplanView(String infoText, String datum, Activity activity) {
        super(activity);
        inflate(getContext(), R.layout.vertretungsplan_view_layout, this);
        findViewById(R.id.vplan_item_Button_expand).setVisibility(GONE);

        TextView textView = (TextView) findViewById(R.id.vertretungsplan_view_textView_headline);
        textView.setText("Nachrichten vom " + datum);

        TextView textView2 = (TextView) findViewById(R.id.vertretungsplan_view_textView_moreInformation);
        textView.setText(infoText);

        findViewById(R.id.vplan_layout).setBackgroundColor(Color.argb(255, 237, 223, 24));
    }

    private void setSmallText() {
        // check the type of the stunde
        TextView textView = (TextView) findViewById(R.id.vertretungsplan_view_textView_headline);
        if (vertretungsstunde.stundeIsNotHappening())
            textView.setText("Ausfall");
        else if (vertretungsstunde.neuerLehrerIsNew() && vertretungsstunde.neuerRaumIsNew())
            textView.setText("Vertretung & Raumänderung");
        else if (vertretungsstunde.neuerLehrerIsNew() && !vertretungsstunde.neuerRaumIsNew())
            textView.setText("Vertretung");
        else if (!vertretungsstunde.neuerLehrerIsNew() && vertretungsstunde.neuerRaumIsNew())
            textView.setText("Raumänderung");
        else
            textView.setText("Unbekannt");

        if (!StorageHelper.loadBooleanFromSharedPreferences(StorageHelper.VPLAN_USER_KLASSE_FILTER, getContext())) {
            String klassenName = vertretungsstunde.getKlassenString();
            if (!Vertretungsstunde.isNullOrWhitespace(vertretungsstunde.getKlassenString()))
                textView.setText(vertretungsstunde.getKlassenString() + "\n" + textView.getText());
        }

        if (!vertretungsstunde.isSubjectEmpty())
            textView.setText(textView.getText() + " in " + vertretungsstunde.getRealFachName());

        TextView textView2 = (TextView) findViewById(R.id.vertretungsplan_view_textView_moreInformation);
        if (!vertretungsstunde.neuerLehrerIsNew())
            textView2.setText("Lehrer: " + vertretungsstunde.getNeuerLehrer(getContext()));
        else
            textView2.setText("Neuer Lehrer: " + vertretungsstunde.getNeuerLehrer(getContext()));
        if (!vertretungsstunde.getNeuerRaumIsEmpty() || !vertretungsstunde.getAlterRaumIsEmpty()) {
            if (!vertretungsstunde.neuerRaumIsNew())
                textView2.setText(textView2.getText() + "\nRaum: " + vertretungsstunde.getNeuerRaum());
            else
                textView2.setText(textView2.getText() + "\nNeuer Raum: " + vertretungsstunde.getNeuerRaum());
        }

        if (!Vertretungsstunde.isNullOrWhitespace(vertretungsstunde.getStunden()))
            textView2.setText(vertretungsstunde.getStunden() + " Stunde\n" + textView2.getText());

        if (!vertretungsstunde.vertretungsTextIsEmpty()) {
            textView2.setText(textView2.getText() + "\n" + vertretungsstunde.getVertretungstext());
        }

        TextView textView3 = (TextView) findViewById(R.id.vertretungsplan_view_textView_moreInformation2);
        textView3.setText(vertretungsstunde.getStunden() + " Stunde" +
                "\nNeuer Lehrer: " + vertretungsstunde.getNeuerLehrer(getContext()) +
                "\nNeuer Raum: " + vertretungsstunde.getNeuerRaum() +
                "\nAlter Lehrer: " + vertretungsstunde.getAlterLehrer(getContext()) +
                "\nAlter Raum: " + vertretungsstunde.getAlterRaum() +
                "\nBetroffene Klassen: " + vertretungsstunde.getKlassenString() +
                "\nVertretungstext: " + vertretungsstunde.getVertretungstext());

    }
}
