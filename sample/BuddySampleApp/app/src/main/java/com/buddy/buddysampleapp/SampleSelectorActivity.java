package com.buddy.buddysampleapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.buddy.buddysampleapp.util.SystemUiHider;

import com.buddy.buddysampleapp.R;
import com.buddy.sdk.Buddy;
import com.buddy.sdk.BuddyCallback;
import com.buddy.sdk.BuddyResult;
import com.buddy.sdk.models.User;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class SampleSelectorActivity extends Activity {

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_selector);

        final SampleApplication mApplication = (SampleApplication)getApplicationContext();

        checkUser();

        ListView listView = (ListView)findViewById(R.id.list_samples);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch(i) {
                    case 0: startActivity(new Intent(getApplicationContext(), CheckinsActivity.class));
                            break;
                    case 1: startActivity(new Intent(getApplicationContext(), FileActivity.class));
                            break;
                }
            }
        });

        Button logout = (Button)findViewById(R.id.btn_logout);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mApplication.buddyClient.logoutUser(null);

            }
        });

    }

    @Override
    protected  void onResume() {
        super.onResume();

        checkUser();
    }

    private void checkUser() {

        Buddy.getCurrentUser(new BuddyCallback<User>(User.class) {
            @Override
            public void completed(BuddyResult<User> userBuddyResult) {
                TextView tv = (TextView) findViewById(R.id.text_greeting);

                if (userBuddyResult.getIsSuccess()) {
                    tv.setText(String.format("Hello %s", userBuddyResult.getResult().userName));
                }
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.sample_selector, menu);
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
}
