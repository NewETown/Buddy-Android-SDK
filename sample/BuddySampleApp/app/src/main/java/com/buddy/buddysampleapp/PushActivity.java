package com.buddy.buddysampleapp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.buddy.buddysampleapp.R;
import com.buddy.sdk.Buddy;
import com.buddy.sdk.BuddyCallback;
import com.buddy.sdk.BuddyResult;
import com.buddy.sdk.models.User;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class PushActivity extends Activity {

    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    /**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     */
    String SENDER_ID = "305253179453";

    ListView messageList;
    List<String> _li = new ArrayList<String>();
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    SharedPreferences prefs;
    Context context;

    String regid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push);

        // Initialize Buddy
        Buddy.init(null, "bbbbbc.DdhbvfLhKgDn", "5789A351-1144-49E6-8721-4C0DFCC226F2");
        // Check device for Play Services APK. If check succeeds, proceed with
        //  GCM registration.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);

            if (regid.isEmpty()) {
                registerInBackground();
            }
        } else {
            Log.i("GCM Registration", "No valid Google Play Services APK found.");
        }

        Button mSubmitPush = (Button)findViewById(R.id.btn_push_send);
        mSubmitPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendPush();
            }
        });

        messageList = (ListView)findViewById(R.id.push_message_list);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(), R.layout.list, _li);
        messageList.setAdapter(adapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.push, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void sendPush() {
        // This sample sends a push to the current user
        // While not the most practical in a production environment, it is the easiest way to demonstrate the functionality on a single device

        // Set up the toasts
        final Context context = getApplicationContext();
        final int duration = Toast.LENGTH_SHORT;

        // Grab the message text
        EditText message = (EditText)findViewById(R.id.enter_push_message);
        final String _m = message.getText().toString();
        message.setText("");

        Buddy.getCurrentUser(new BuddyCallback<User>(User.class) {
            @Override
            public void completed(BuddyResult<User> result) {
                if(result.getIsSuccess()) {
                    String user = result.getResult().id.toString();
                    Map<String, Object> parameters = new HashMap<String, Object>();
                    parameters.put("message", _m);
                    // Note that this test sends out a push to the whole app, include "recipients" param to send to a specific user list
                    Buddy.post("/notifications", parameters, new BuddyCallback<JsonObject>(JsonObject.class) {
                        @Override
                        public void completed(BuddyResult<JsonObject> result) {
                            if (result.getIsSuccess()) {
                                Toast toast = Toast.makeText(context, "Push successful!", duration);
                                toast.show();
                            } else {
                                Toast toast = Toast.makeText(context, "Push failed! Check error output", duration);
                                toast.show();
                            }
                        }
                    });
                } else {
                    Toast toast = Toast.makeText(context, "Get user failed! Check error output", duration);
                    toast.show();
                }
            }
        });

    }

    public void onPushReceived() {

    }

    public class PushNotificationReceived extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.w("PUSH RECEIVED", intent.getStringExtra("message").toString());
        }
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i("CHECK_PLAY_SERVICES", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i("GCM Registration", "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i("GCM Registration", "App version changed.");
            return "";
        }
        return registrationId;
    }
    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(PushActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }
    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask() {
            @Override
            protected String doInBackground(Object... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device
                    // will send upstream messages to a server that echo back the
                    // message using the 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            // @Override
            protected void onPostExecute(String msg) {
                _li.add(msg);
            }

        }.execute(null, null, null);
    }

    private void sendRegistrationIdToBackend() {
        Buddy.setPushToken(regid, new BuddyCallback<Boolean>(Boolean.class) {
            @Override
            public void completed(BuddyResult<Boolean> result) {
                if(result.getIsSuccess()) {
                    Log.w("PUSH", "Push token registered successfully.");
                } else {
                    Log.w("PUSH", "Push token registration error, check logs.");
                }
            }
        });
    }

    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i("GCM", "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }
}