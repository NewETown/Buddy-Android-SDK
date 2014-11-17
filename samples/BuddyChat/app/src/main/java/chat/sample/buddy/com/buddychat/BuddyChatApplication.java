package chat.sample.buddy.com.buddychat;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.util.Log;

import com.buddy.sdk.Buddy;
import com.buddy.sdk.BuddyCallback;
import com.buddy.sdk.BuddyClient;
import com.buddy.sdk.BuddyClientOptions;
import com.buddy.sdk.BuddyResult;
import com.buddy.sdk.UserAuthenticationRequiredCallback;
import com.buddy.sdk.models.User;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Main Application class.  We use this mainly for push token registration.
 */
public class BuddyChatApplication extends Application {

    public static final String TAG = "BUDDYCHAT";

    // GCM Stuff
    public static final String EXTRA_MESSAGE = "message";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    /**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     */
    public static final String SENDER_ID = "Your GCM Sender ID";
    public static final String APPID = "Your App ID";
    public static final String APPKEY = "Your App Key";


    public static BuddyChatApplication instance;
    public static Chat activeChat;
    public static boolean isPushSupported;
    public User currentUser;
    boolean loginVisible;

    public BuddyChatApplication() {
        instance = this;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // initialize the Buddy SDK
        Buddy.init(getApplicationContext(), APPID, APPKEY);

        // automatically show the Login activity whenever
        // authentication fails for a user-level API call
        Buddy.setUserAuthenticationRequriedCallback(new UserAuthenticationRequiredCallback() {
            @Override
            public void authenticate() {

                if (loginVisible) {
                    return;
                }
                loginVisible = true;
                Intent loginIntent = new Intent(BuddyChatApplication.this, Login.class);
                loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(loginIntent);
            }
        });


        // Check device for Play Services APK. If check succeeds, proceed with
        //  GCM registration.
        isPushSupported = checkPlayServices();
        if (isPushSupported) {

            String regId = getRegistrationId(getBaseContext());

            if (regId.isEmpty()) {
                registerInBackground();
            }
        }
    }



    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }


    public void getCurrentUser(final boolean refresh, final GetCurrentUserCallback callback) {

        if (currentUser != null && !refresh) {
           if (callback != null) {
               callback.complete(currentUser);
           }
        }
        else {

            Buddy.getCurrentUser(new BuddyCallback<User>(User.class) {
                @Override
                public void completed(BuddyResult<User> result) {
                    if (result.getIsSuccess() && result.getResult() != null) {
                       currentUser = result.getResult();
                    }
                    if (callback != null) {
                        callback.complete(currentUser);
                    }
                }
            });
        }

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }


    //
    // GCM Stuff, verbatim from http://developer.android.com/google/gcm/client.html
    //

    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";


    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

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

    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(BuddyChatApplication.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    private void registerInBackground() {

        new AsyncTask<Object, Object, String>() {

            @Override
            protected String doInBackground(Object... objects) {
                String msg = "";
                try {

                    GoogleCloudMessaging    gcm = GoogleCloudMessaging.getInstance(getApplicationContext());

                    String id = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + id;

                    // Send the push token for this device over to Buddy
                    //
                    Buddy.setPushToken(id, null);
                    storeRegistrationId(getApplicationContext(), id);

                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }
        }.execute(null, null, null);

    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (!GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                Log.i(TAG, "This device is not supported for push");
            }
            return false;
        }
        return true;
    }

}
