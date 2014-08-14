package com.buddy.buddysampleapp;

import android.app.Application;
import android.content.Intent;
import android.content.res.Configuration;

import com.buddy.sdk.Buddy;
import com.buddy.sdk.BuddyClient;
import com.buddy.sdk.UserAuthenticationRequiredCallback;

public class SampleApplication extends Application {

    private static final String AppId = "bbbbbc.DdhbvfLhKgDn";
    private static final String AppKey = "5789A351-1144-49E6-8721-4C0DFCC226F2";


    public BuddyClient buddyClient;

    boolean loginVisible;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Don't forget to add your own AppId and AppKey!
        buddyClient = Buddy.init(this, AppId, AppKey);

        buddyClient.setUserAuthenticationRequiredCallback(new UserAuthenticationRequiredCallback() {
            @Override
            public void authenticate() {

                if (loginVisible) {
                    return;
                }

                loginVisible = true;

                Intent loginIntent = new Intent(SampleApplication.this, LoginActivity.class);
                loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(loginIntent);
            }
        });

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
