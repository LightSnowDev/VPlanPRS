package com.lightSnowDev.VPlanPRS2.gcm;

/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.lightSnowDev.VPlanPRS2.BuildConfig;
import com.lightSnowDev.VPlanPRS2.helper.DownloadHelper;
import com.lightSnowDev.VPlanPRS2.helper.SensibleDataHelper;

public class MyInstanceIDListenerService extends FirebaseInstanceIdService {

    private static final String TAG = "MyInstanceIDLS";

    public static void sendGCMServerData(String token, Context context) {
        DownloadHelper downloadHelper = new DownloadHelper(context);
        downloadHelper.FIXED_URL = SensibleDataHelper.PRIVATE_SERVER_GCM_PATH_DIR;
        // no log needed. this works.
        //if (BuildConfig.DEBUG) {
        //    downloadHelper.addOnRecievedEvent(new DownloadHelper.OnRecievedEvent() {
        //        @Override
        //        public void recievedEvent(String resultString, String resultBase64, boolean success) {
        //            if (success)
        //                Log.d("GCM event", resultString);
        //        }
        //    });
        //}
        downloadHelper.download(SensibleDataHelper.PRIVATE_SERVER_GCM_FILE_DIR + "?addKey=" + token +
                        "&version=" + String.valueOf(BuildConfig.VERSION_CODE),
                SensibleDataHelper.PRIVATE_SERVER_USERNAME,
                SensibleDataHelper.PRIVATE_SERVER_PASSWORD);
    }

    public static void removeGCMServerData(String token, Context context) {
        DownloadHelper downloadHelper = new DownloadHelper(context);
        downloadHelper.FIXED_URL = SensibleDataHelper.PRIVATE_SERVER_GCM_PATH_DIR;
        // no log needed. this works.
        //if (BuildConfig.DEBUG) {
        //    downloadHelper.addOnRecievedEvent(new DownloadHelper.OnRecievedEvent() {
        //        @Override
        //        public void recievedEvent(String resultString, String resultBase64, boolean success) {
        //            if (success)
        //                Log.d("GCM event", resultString);
        //        }
        //    });
        //}
        downloadHelper.download(SensibleDataHelper.PRIVATE_SERVER_GCM_FILE_DIR + "?removeKey=" + token,
                SensibleDataHelper.PRIVATE_SERVER_USERNAME,
                SensibleDataHelper.PRIVATE_SERVER_PASSWORD);
    }

    @Override
    public void onTokenRefresh() {
        //Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        sendGCMServerData(FirebaseInstanceId.getInstance().getToken(), this);
    }
}