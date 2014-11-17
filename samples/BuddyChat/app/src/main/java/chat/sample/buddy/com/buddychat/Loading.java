package chat.sample.buddy.com.buddychat;

import com.buddy.sdk.Buddy;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.buddy.sdk.Buddy;
import com.buddy.sdk.BuddyCallback;
import com.buddy.sdk.BuddyResult;
import com.buddy.sdk.models.User;

import chat.sample.buddy.com.buddychat.R;

public class Loading extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);


        // initialize the Buddy SDK
        //

        Buddy.init(getApplicationContext(), "bbbbbc.rcdbvlNmjKbj", "BDE88F15-D1DA-4DD2-BA8B-566B9F33385E");

        // see if there is a logged in user
        //


       BuddyChatApplication.instance.getCurrentUser(false, new GetCurrentUserCallback() {
           @Override
           public void complete(User user) {
               if (user != null) {
                   Intent i = new Intent(getBaseContext(), MainScreen.class);
                   startActivity(i);
                   finish();
               }
           }
       });

    }
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.loading, menu);
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
