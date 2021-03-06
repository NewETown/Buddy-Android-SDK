package com.buddy.buddysampleapp;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckinsActivity extends Activity {

    // Prep the ListView
    ListView mCheckinList;
    List<String> mLi;
    ArrayAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkins);

        mLi = new ArrayList<String>();

        mCheckinList = (ListView)findViewById(R.id.list_checkins);
        mAdapter = new ArrayAdapter<String>(getBaseContext(), R.layout.list, mLi);
        mCheckinList.setAdapter(mAdapter);

        // Initialize the submit & search buttons
        Button btnSubmitCheckin = (Button)findViewById(R.id.btn_submit_checkin);
        btnSubmitCheckin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitCheckin();
            }
        });

        Button btnSearchCheckins = (Button)findViewById(R.id.btn_search_checkins);
        btnSearchCheckins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchCheckins();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.checkins, menu);
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

    public void searchCheckins() {
        EditText editText_lat = (EditText)findViewById(R.id.edit_search_lat);
        EditText editText_lng = (EditText)findViewById(R.id.edit_search_lng);
        EditText editText_range = (EditText)findViewById(R.id.edit_search_range);

        // Get the values we need from the user
        String query = editText_lat.getText().toString() + ", " + editText_lng.getText().toString() + ", " + editText_range.getText().toString();

        // Set up the toasts
        final Context context = getApplicationContext();
        final int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, "Searching: " + query, duration);
        toast.show();

        // Submit the values (note that this doesn't check for bad input)
        Map<String,Object> parameters = new HashMap<String,Object>();
        parameters.put("locationRange", query);
        Buddy.<JsonObject>get("/checkins", parameters, new BuddyCallback<JsonObject>(JsonObject.class) {
            @Override
            public void completed(BuddyResult<JsonObject> result) {
                // Clear the checkin list
                mLi.clear();

                if (result.getIsSuccess()) {
                    JsonObject obj = result.getResult();
                    JsonArray resultArray = obj.getAsJsonArray("pageResults");
                    // Initialize a list to supply to the ListView

                    // Add items to the list
                    if (resultArray.size() == 0) {
                        mLi.add("No results");
                        mAdapter.notifyDataSetChanged();
                    } else {
                        for (int i = 0; i < resultArray.size(); i++) {
                            JsonObject checkin = (JsonObject)resultArray.get(i);
                            mLi.add(checkin.get("id") + ": " + checkin.get("location"));
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                } else {
                    // Toast on error
                    Toast toast = Toast.makeText(context, String.format("Error: %i", result.getErrorCode()), duration);
                    toast.show();
                }
            }
        });
    }

    public void submitCheckin() {
        EditText editText_lat = (EditText)findViewById(R.id.edit_enter_lat);
        EditText editText_lng = (EditText)findViewById(R.id.edit_enter_lng);
        EditText editText_description = (EditText)findViewById(R.id.edit_enter_description);

        // Get the values we need from the user
        double lat = Double.parseDouble(editText_lat.getText().toString());
        double lng = Double.parseDouble(editText_lng.getText().toString());
        String description = editText_description.getText().toString();

        // Get a toast ready to show the result
        final Context context = getApplicationContext();
        final int duration = Toast.LENGTH_SHORT;

        // Submit the values (note that this doesn't check for bad input)
        Location location = new Location("");
        location.setLatitude(lat);
        location.setLongitude(lng);
        Map<String,Object> parameters = new HashMap<String,Object>();
        parameters.put("description", description);
        parameters.put("location", String.format("%f,%f", location.getLatitude(), location.getLongitude()));

        // Reset the input field values
        editText_description.setText("");
        editText_lat.setText("");
        editText_lng.setText("");

        // Make the call
        Buddy.<JsonObject>post("/checkins", parameters, new BuddyCallback<JsonObject>(JsonObject.class) {
            @Override
            public void completed(BuddyResult<JsonObject> result) {
                if (result.getIsSuccess()) {
                    JsonObject obj = result.getResult();
                    // Toast the ID of the checkin as confirmation
                    String id = obj.get("id").getAsString();
                    Toast toast = Toast.makeText(context, "Checkin complete!", duration);
                    toast.show();
                } else {
                    Toast toast = Toast.makeText(context, String.format("Error: %i", result.getErrorCode()), duration);
                    toast.show();
                }
            }
        });
    }
}
