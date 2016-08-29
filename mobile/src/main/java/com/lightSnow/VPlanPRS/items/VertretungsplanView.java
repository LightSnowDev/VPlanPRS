package com.lightSnow.VPlanPRS.items;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lightSnow.VPlanPRS.R;
import com.lightSnow.VPlanPRS.classes.Vertretungsstunde;

/**
 * Created by Jonathan on 24.08.2016.
 */
public class VertretungsplanView extends RelativeLayout{

    Vertretungsstunde vertretungsstunde;

    public VertretungsplanView(Vertretungsstunde vertretungsstunde, Activity activity)
    {
        super(activity);
        inflate(getContext(), R.layout.vertretungsplan_view_layout, this);
        this.vertretungsstunde = vertretungsstunde;
        this.setOnClickListener(listener);
        setSmallText();
    }

    public VertretungsplanView(String infoText, String datum, Activity activity)
    {
        super(activity);
        inflate(getContext(), R.layout.vertretungsplan_view_layout, this);
        findViewById(R.id.vplan_item_Button_expand).setVisibility(GONE);

        TextView textView = (TextView) findViewById(R.id.vertretungsplan_view_textView_headline);
        textView.setText("Nachrichten vom " + datum);

        TextView textView2 = (TextView) findViewById(R.id.vertretungsplan_view_textView_moreInformation);
        textView.setText(infoText);

        ((RelativeLayout)findViewById(R.id.vplan_layout)).setBackgroundColor(Color.argb(255,237,223,24));
    }

    OnClickListener listener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if(findViewById(R.id.vertretungsplan_view_textView_moreInformation).getVisibility() == VISIBLE)
            {
                findViewById(R.id.vertretungsplan_view_textView_moreInformation).setVisibility(GONE);
                findViewById(R.id.vertretungsplan_view_textView_moreInformation2).setVisibility(VISIBLE);
                ((ImageButton)findViewById(R.id.vplan_item_Button_expand)).setImageResource(R.drawable.ic_keyboard_arrow_up_black_48dp);
            }
            else
            {
                findViewById(R.id.vertretungsplan_view_textView_moreInformation).setVisibility(VISIBLE);
                findViewById(R.id.vertretungsplan_view_textView_moreInformation2).setVisibility(GONE);
                ((ImageButton)findViewById(R.id.vplan_item_Button_expand)).setImageResource(R.drawable.ic_keyboard_arrow_down_black_48dp);
            }
        }
    };

    private void setSmallText()
    {
        // Set item views based on the data model
        TextView textView = (TextView) findViewById(R.id.vertretungsplan_view_textView_headline);
        textView.setText("Vertretung in " + vertretungsstunde.Fach);

        TextView textView2 = (TextView) findViewById(R.id.vertretungsplan_view_textView_moreInformation);
        textView.setText(vertretungsstunde.Fach + " - " + vertretungsstunde.alterLehrer + " - " + vertretungsstunde.neuerRaum);
    }
}
