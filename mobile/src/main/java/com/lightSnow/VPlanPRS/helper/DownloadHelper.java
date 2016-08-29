package com.lightSnow.VPlanPRS;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Proxy;
import android.util.Log;

import java.io.IOException;
import java.net.Authenticator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;
import okhttp3.TlsVersion;

/**
 * Created by Jonathan Schwarzenböck on 02.05.2016.
 * <p>
 * Source: http://stackoverflow.com/questions/13196234/simple-parse-json-from-url-on-android-and-display-in-listview
 */
public class DownloadHelper {

    public String FIXED_URL = "https://philipp-reis-schule.de/download/vplan/"; //https is a must
    RequestBody formBody;
    //region /// OnRecieved Event ... ///
    private ProgressDialog progressDialog;
    private Activity currentActivity = null;
    private Context currentContext = null;
    private String progressDialogMessage = "wird geladen...";
    private boolean showprogressDialogBoolean;
    private boolean runOnUiThread = true;

    public DownloadHelper(Activity currentActivity) {
        this.currentActivity = currentActivity;
        this.currentContext = currentActivity;
    }

    public DownloadHelper(Context currentContext) {
        runOnUiThread = false;
        currentContext = currentContext;
    }

    public static boolean isNetworkAvailable(Activity activity) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /*
     * Methods which are the same on get / post
     */
    private OkHttpClient setUsualVariables(String url) {
        OkHttpClient client;
        if (url.isEmpty())
            throw new RuntimeException("No URL path specified.");
        if (showprogressDialogBoolean)
            showProgressDialog(currentActivity);

        ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_0)
                .cipherSuites(CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA)
                .supportsTlsExtensions(true)
                .build();

        client = new OkHttpClient.Builder()
                .connectionSpecs(Collections.singletonList(spec))
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .build();
        return client;
    }

    /*
     * Downloads a content of a URL.
     * Does not return anything, since the result is handled in a 'onRespons' event.
     * Your URL MUST start with http/https.
     *
     * The url path should (not a must) start with a "/".
     */
    public void download(String url, String name, String password) {
        OkHttpClient client = setUsualVariables(url);

        Request.Builder requestB = new Request.Builder()
                .url(FIXED_URL + url);


        String credential = Credentials.basic(name, password);

        // Put the download in a async download queue.
        // Start the download.
        // onCallback specifies the response events.
        client.newCall(requestB.header("Authorization", credential).build()).enqueue(onCallback);
    }

    Callback onCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            Log.d("Error get/post result", "onFailure: " + e.getMessage());
            if (runOnUiThread) {
                currentActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        RunRecievedEvent("", false);
                    }
                });
                if (showprogressDialogBoolean)
                    closeProgressDialog();
            } else
                RunRecievedEvent("", false);
        }

        @Override
        public void onResponse(Call call, final Response response) throws IOException {
            final String result = response.body().string();
            Log.d("LOG get/post result", result);
            if (runOnUiThread) {
                currentActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        RunRecievedEvent(result, response.isSuccessful());
                    }
                });
                if (showprogressDialogBoolean)
                    closeProgressDialog();
            } else
                RunRecievedEvent(result, response.isSuccessful());
        }
    };

    /*
     * POST request
     */
    public void post(String url, HashMap form_urlEncded, HashMap headerMap) {
        OkHttpClient client = setUsualVariables(url);

        FormBody.Builder fBuilder = new FormBody.Builder();
        if (form_urlEncded != null) {
            Iterator it = form_urlEncded.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                fBuilder.add(pair.getKey().toString(), pair.getValue().toString());
                it.remove(); // avoids a ConcurrentModificationException
            }
        }
        RequestBody formBody = fBuilder.build();

        Request.Builder requestB = new Request.Builder()
                .url(FIXED_URL + url)
                .post(formBody);

        if (headerMap != null) {
            Iterator it_headerMap = headerMap.entrySet().iterator();
            while (it_headerMap.hasNext()) {
                Map.Entry pair_headerMap = (Map.Entry) it_headerMap.next();
                requestB.addHeader(pair_headerMap.getKey().toString(), pair_headerMap.getValue().toString());
                it_headerMap.remove(); // avoids a ConcurrentModificationException
            }
        }

        // Put the download in a async download queue.
        // Start the download.
        // onCallback specifies the response events.
        client.newCall(requestB.build()).enqueue(onCallback);
    }

    public void setRunOnUiThread(boolean b) {
        runOnUiThread = b;
    }

    //region /// ProgressDialog ... ///
    /*
     * Show loading Dialog.
     */
    private void showProgressDialog(Context context) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(progressDialogMessage);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface arg0) {
                //stop client
            }
        });
    }

    private void closeProgressDialog() {
        progressDialog.cancel();
    }

    /*
     * Set if a loading dialog is shown.
     * Only needed for longer downloads or to prevent the user from input.
     */
    public void setIfProgressDialogIsShown(boolean b) {
        showprogressDialogBoolean = b;
    }

    /*
     * Set the message of the loading Dialog.
     * The default is "wird geladen...".
     */
    public void setProgressDialogMessage(String progressDialogMessage) {
        this.progressDialogMessage = progressDialogMessage;
    }
    //endregion

    //region /// OnRecievedEvent ... ///
    List<OnRecievedEvent> mOnRecievedlistenerList = new ArrayList<OnRecievedEvent>();

    public void addOnRecievedEvent(OnRecievedEvent listener) {
        mOnRecievedlistenerList.add(listener);
    }

    public void removeAllOnRecievedEvents() {
        mOnRecievedlistenerList.clear();
    }

    public void removeOnRecievedEvent(OnRecievedEvent listener) {
        mOnRecievedlistenerList.remove(listener);
    }

    protected void RunRecievedEvent(String result, boolean success) {
        for (OnRecievedEvent event : mOnRecievedlistenerList) {
            event.recievedEvent(result, success);
        }
    }

    public interface OnRecievedEvent {
        void recievedEvent(String result, boolean success);
    }
    //endregion
}
