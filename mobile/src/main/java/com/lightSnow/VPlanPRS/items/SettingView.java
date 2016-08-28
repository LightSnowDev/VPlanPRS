package com.lightSnow.VPlanPRS.items;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.lightSnow.VPlanPRS.FirstStartActivity;
import com.lightSnow.VPlanPRS.R;
import com.lightSnow.VPlanPRS.helper.StorageHelper;

/**
 * Created by Jonathan on 24.08.2016.
 */
public class SettingView extends RelativeLayout {

    public SettingView(String settingName, final Activity activity) {
        super(activity);
        inflate(getContext(), R.layout.settings_view_layout, this);

        //set switch
        Switch mySwitch = ((Switch) findViewById(R.id.switch_setting));
        mySwitch.setChecked(StorageHelper.loadBooleanFromSharedPreferences(settingName, activity));
        mySwitch.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                StorageHelper.saveToSharedPreferences(StorageHelper.VPLAN_USER_KLASSE_FILTER, buttonView.isChecked(), activity);
            }
        });
    }

    public SettingView(final Activity activity, String settingName, String mainText, String secondText) {
        this(settingName, activity);
        setMainText(mainText);
        setSecondText(secondText);
    }

    public void setMainText(String text) {
        ((TextView) findViewById(R.id.textView_settings_main)).setText(text);
    }

    public void setSecondText(String text) {
        ((TextView) findViewById(R.id.textView_settings_second)).setText(text);
    }

}
