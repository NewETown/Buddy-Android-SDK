package com.buddy.buddysampleapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.buddy.buddysampleapp.R;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class SampleSelectorActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_selector);

        TextView greeting = (TextView)findViewById(R.id.greeting);

        // Collect the values we stored after login
        Intent i = getIntent();
        greeting.setText("Welcome " + i.getStringExtra("username") + "!");

        ListView listView = (ListView)findViewById(R.id.sampleList);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch(i) {
                    case 0: startActivity(new Intent(getApplicationContext(), CheckinsActivity.class));
                            break;
                    case 1: startActivity(new Intent(getApplicationContext(), FileActivity.class));
                            break;
                    case 2: startActivity(new Intent(getApplicationContext(), PushActivity.class));
                            break;
                }
            }
        });

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
