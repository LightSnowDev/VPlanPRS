package com.lightSnow.VPlanPRS.items;

import android.app.Activity;
import android.view.View;
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

    OnClickListener listener = new OnClickListener() {
        @Override
        public void onClick(View view) {

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
