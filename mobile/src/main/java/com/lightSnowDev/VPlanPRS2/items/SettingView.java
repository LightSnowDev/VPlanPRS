package com.lightSnowDev.VPlanPRS2.items;

import android.app.Activity;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.lightSnowDev.VPlanPRS2.R;
import com.lightSnowDev.VPlanPRS2.helper.StorageHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jonathan on 24.08.2016.
 */
public class SettingView extends RelativeLayout {

    //region /// OnSwitchEvent ... ///
    List<OnSwitchEvent> mOnSwitchlistenerList = new ArrayList<OnSwitchEvent>();

    public SettingView(final String settingName, final Activity activity) {
        super(activity);
        inflate(getContext(), R.layout.settings_view_layout, this);

        //set switch
        Switch mySwitch = ((Switch) findViewById(R.id.switch_setting));
        mySwitch.setChecked(StorageHelper.loadBooleanFromSharedPreferences(settingName, activity));
        mySwitch.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                StorageHelper.saveToSharedPreferences(settingName, buttonView.isChecked(), activity);
                RunSwitchEvent(isChecked);
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

    public void addOnSwitchEvent(OnSwitchEvent listener) {
        mOnSwitchlistenerList.add(listener);
    }

    protected void RunSwitchEvent(boolean newValue) {
        for (OnSwitchEvent event : mOnSwitchlistenerList) {
            event.switchEvent(newValue);
        }
    }

    public interface OnSwitchEvent {
        void switchEvent(boolean newValue);
    }
    //endregion


}
