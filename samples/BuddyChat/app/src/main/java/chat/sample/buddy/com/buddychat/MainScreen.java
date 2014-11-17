package chat.sample.buddy.com.buddychat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.buddy.sdk.Buddy;
import com.buddy.sdk.BuddyCallback;
import com.buddy.sdk.BuddyResult;
import com.buddy.sdk.models.PagedResult;
import com.buddy.sdk.models.User;
import com.google.gson.JsonObject;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import chat.sample.buddy.com.buddychat.R;

// The main screen lists the apps users, so we can pick who to chat with
// and allows the current user to logout.
public class MainScreen extends Activity {

    ListView _users;
    UsersSimpleAdapter _adapter;

    User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);


        final Button btnLogout = (Button)findViewById(R.id.btnLogout);

        //
        // Handle logout
        //
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Buddy.logoutUser(new BuddyCallback<Boolean>(Boolean.class) {
                    @Override
                    public void completed(BuddyResult<Boolean> result) {
                        Intent i = new Intent(getBaseContext(), Login.class);
                        BuddyChatApplication.instance.setCurrentUser(null);

                        // trigger the login dialog.
                        BuddyChatApplication.instance.getCurrentUser(true, null);
                        startActivity(i);
                    }
                });
            }
        });

        final TextView lblHello = (TextView)findViewById(R.id.lblHello);

        lblHello.setText(String.format("Hello %s!", BuddyChatApplication.instance.currentUser.userName));

        // set up our users list
        //
        _users = (ListView)findViewById(R.id.lvUsers);

        _users.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                // on click of an item, fire up a chat
                //
                User u = (User)_users.getItemAtPosition(i);

                startChat(u);
            }
        });
        _adapter = new UsersSimpleAdapter(getBaseContext());
        _users.setAdapter(_adapter);
        refreshList();
    }

    private void startChat(User u) {
        Intent ci = new Intent(getBaseContext(), Chat.class);
        ci.putExtra("userName", u.userName);
        ci.putExtra("name", u.firstName + " " + u.lastName);
        ci.putExtra("userId", u.id);
        startActivity(ci);
    }

    private void refreshList() {


        Map<String, Object> params = new HashMap<String, Object>();
        params.put("sortOrder", "-lastModified");

        // load the list of users, sorting by last login, descending
        //
        Buddy.<PagedResult>get("/users", params, new BuddyCallback<PagedResult>(PagedResult.class) {
            @Override
            public void completed(BuddyResult<PagedResult> result) {

                if (result.getIsSuccess()) {

                    List<User> userList = result.getResult().convertPageResults(User.class);

                    // remove current user from the list
                    //
                    for (int i=0 ; i < userList.size(); i++) {

                        User usr = userList.get(i);
                            if (usr.userName.equals(currentUser.userName)) {
                                userList.remove(i);
                                break;
                            }
                    }
                    _adapter.setItemList(userList);
                    _adapter.notifyDataSetChanged();
                }
            }
        });

    }

    private BroadcastReceiver onEvent= new BroadcastReceiver() {
        public void onReceive(Context ctx, Intent i) {
            String payload = i.getStringExtra("payload");

            if (payload == null) {
                return;
            }

            // if we get a chat message, then go ahead and
            // fire upt the chat activity.

            String[] parts = payload.split("\t");

            String msg = parts[0];

            String id = parts[1];

            // find the user, then start the chat.
            if (_adapter.getItemList() != null) {
                for (User u : _adapter.getItemList()) {
                    if (u.id.equals((id))) {
                        startChat(u);
                        break;
                    }
                }
            }
        }
    };


    @Override
    public void onPause() {

        // unhook our receiver
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(onEvent);



        super.onPause();
    }
    @Override
    public void onResume() {
        super.onResume();


        // hook up our receiver
        IntentFilter f=new IntentFilter(GcmBroadcastReceiver.ACTION_MESSAGE_RECEIVED);

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(onEvent, f);


        // make sure we have a current user.  Doing this will fire up the login dialog
        // if we don't have one for some reason
        BuddyChatApplication.instance.getCurrentUser(false, new GetCurrentUserCallback() {

            public void complete(User u) {
                currentUser = u;
                if (u != null) {
                    refreshList();
                }
            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_screen, menu);
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


    class UsersSimpleAdapter extends SimpleAdapter<User> {

        public UsersSimpleAdapter(Context c) {
            super(null, c);
        }

        protected <T> void populateView(View v, T u) {
            User user = (User)u;
            TextView text1 = (TextView) v.findViewById(android.R.id.text1);
            text1.setText(String.format("%s %s (%s)", user.firstName, user.lastName, user.userName));

            TextView text2 = (TextView) v.findViewById(android.R.id.text2);

            Date now = new Date();
            long delta = now.getTime() - user.lastLogin.getTime();

            text2.setText(String.format("Last Seen: %d minutes ago", delta / (60000)));

        }
    }





}
