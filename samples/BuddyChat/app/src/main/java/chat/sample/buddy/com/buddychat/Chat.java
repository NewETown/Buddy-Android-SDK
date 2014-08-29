package chat.sample.buddy.com.buddychat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.buddy.sdk.Buddy;
import com.buddy.sdk.BuddyCallback;
import com.buddy.sdk.BuddyResult;
import com.buddy.sdk.DateRange;
import com.buddy.sdk.models.Message;
import com.buddy.sdk.models.NotificationResult;
import com.buddy.sdk.models.PagedResult;
import com.buddy.sdk.models.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import chat.sample.buddy.com.buddychat.R;


// This is the main activity that does the chatting
//
// This activity listens for notifications from the device that we are chatting with
// and updates it's state accordingly.  This uses the Buddy push notification APIs.
//
// The notifications tell this chat app what the state is and what to do next so that
// the client doesn't have to poll for changes.
//
//
public class Chat extends ActionBarActivity {


    // what state is our chat in?
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CHATTING     = 2;
    private static final int STATE_WAITING      = 1;

    private int state = STATE_DISCONNECTED;


    // tell the other client to check for new messages.
    private static final String CHECK_MESSAGES = "check_messsages";

    // tell the other client that we are waiting to chat with them
    private static final String CHAT_WAITING = "chat_waiting";

    // tell the other client that we are happily connected
    private static final String CHAT_CONNECTED = "chat_connected";

    // tell the other client that we are leving the session;
    private static final String CHAT_END = "chat_end";


    // information about the user we are chatting with
    //
    String userId;
    String userName;

    String myUserId;
    String myUserName;

    // the thread id of this chat, which is a combo of
    // the logged in user's ID and the remote chat user's ID
    String threadId;

    List<String> toList = new ArrayList<String>();
    Date lastPing;
    Timer  timer;


    // our list of messages
    //
    ListView _messages;
    MessagesSimpleAdapter _adapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        lastPing = new Date();

        Intent i = getIntent();

        userId = i.getStringExtra("userId");
        userName = i.getStringExtra("userName");

        if (userId == null || BuddyChatApplication.instance.currentUser == null) {
            // something bad happened.
            finish();
            return;
        }
        toList.add(userId);

        myUserId = BuddyChatApplication.instance.currentUser.id;
        myUserName = BuddyChatApplication.instance.currentUser.userName;

        if (userName == null) {

            // we didn't get a user name (usually due to being launched by a notification
            // so fetch the user info.
            //
            Buddy.get("/users/" + userId, null, new BuddyCallback<User>(User.class) {
                @Override
                public void completed(BuddyResult<User> result) {
                    if (result.getIsSuccess()) {
                        userName = result.getResult().userName;

                    }
                    initialize();
                }
            });
        }
        else {
            initialize();
        }
    }


    private void initialize() {



        // create a thread ID that is unique between
        // each chat party, that is also unique.
        // alphabetizing and concat'ing is an easy solution:
        //
        List<String> ids = new ArrayList<String>();
        ids.add(BuddyChatApplication.instance.currentUser.id);
        ids.add(userId);
        Collections.sort(ids);
        threadId = String.format("%s+%s", ids.get(0), ids.get(1));


        // set up our UI
        Button btnSend = (Button)findViewById(R.id.btnSend);
        final EditText textMsg = (EditText)findViewById(R.id.textMsg);


        setState(STATE_WAITING);

        // handle click of the send button
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                // pull together the params for the message
                final Map<String,Object> params = new HashMap<String, Object>();

                params.put("thread", threadId);
                params.put("addressees", toList);
                params.put("subject", "chat");
                params.put("body", textMsg.getText().toString());

                if (state != STATE_CHATTING) {
                    setState(STATE_WAITING);
                }

                Buddy.<Message>post("/messages", params, new BuddyCallback<Message>(Message.class) {
                    @Override
                    public void completed(BuddyResult<Message> result) {

                        // if the send is successful
                        if (result.getIsSuccess()) {

                            // send a push notifying that we've sent a new message
                            // which means the other client doesn't need to poll.
                            //
                            // reset our waiting counter.
                            lastPing = new Date();

                            // send the notification so the client knows to check for messages
                            //
                            sendNotification(CHECK_MESSAGES, "Buddy Chat", String.format("%s: %s", myUserName, textMsg.getText().toString()));
                            textMsg.setText("");

                            // refresh the messages to include the one we just sent
                            loadMessages();
                        }
                        else {
                            // TODO: properly handle a send failure...hey, it's just a sample.
                            Log.e(BuddyChatApplication.TAG, "Error sending message: " + result.getError());
                        }
                    }
                });

            }
        });


        // set up the messages list
        _messages = (ListView)findViewById(R.id.lvMessages);
        _adapter = new MessagesSimpleAdapter(getBaseContext());
        _messages.setAdapter(_adapter);


        // finally, set up a timer to check status every 15 seconds.
        timer = new Timer();

        ChatUpdateTask task = new ChatUpdateTask(this);

        // this is sort of for emulator only - push should always
        // be supported otherwise, but in this case, check every 15 seconds
        timer.schedule(task, 100, 15000);

        // tell the other client we are waiting on them.
        sendNotification(CHAT_WAITING, null, null);

        loadMessages();
    }

    Date lastMessageDate;
    boolean loadingMessages;

    // refresh the list of messages
    //
    private void loadMessages() {

        if (loadingMessages) {
            return;
        }

        Map<String,Object> params = new HashMap<String, Object>();

        // specify the thread we are on
        params.put("thread", threadId);

        // we pull the chats newest first so old ones just page off the top
        params.put("sortOrder", "-created");

        // only get the 20 most recent messages
        params.put("pagingToken", "20;0");

        if (lastMessageDate != null) {
            DateRange dr =  new DateRange(new Date(lastMessageDate.getTime()+1000), new Date(new Date().getTime() + 60000));
            params.put("created",dr);
        }

        loadingMessages = true;
        Buddy.<PagedResult>get("/messages", params, new BuddyCallback<PagedResult>(PagedResult.class) {
            @Override
            public void completed(BuddyResult<PagedResult> result) {
                loadingMessages = false;
                if (result.getIsSuccess()) {

                    List<Message> msgList = result.getResult().convertPageResults(Message.class);

                    // reverse the list so it's oldest first
                    //
                    Collections.reverse(msgList);

                    // get the date of the last message we retrieved,
                    // and use this as the starting point for the next query so we don't always
                    // ask for the whole batch.
                    //
                    if (msgList.size() > 0) {
                        lastMessageDate = msgList.get(msgList.size()-1).created;
                    }

                    // look for the newest received message, and use that as the last ping.
                    //
                    for (Message m : msgList) {

                        if (m.type == Message.MessageType.Received) {

                            if (lastPing == null || m.created.getTime() > lastPing.getTime()) {
                                lastPing = m.created;
                            }
                            break;
                        }
                    }


                    _adapter.appendItemList(msgList);
                    _adapter.notifyDataSetChanged();

                   scrollMyListViewToBottom();
                }
            }
        });

    }


    // sets the current chat state
    private void setState(int newState) {
        if (this.state == newState) return;


        String strState = null;

        switch(newState) {
            case STATE_CHATTING:
                strState = "Connected";
                break;
            case STATE_WAITING:
                strState = "Waiting";
                break;
            case STATE_DISCONNECTED:
                strState = "Disconnected";
                break;
        }

        TextView lblMessage = (TextView)findViewById(R.id.lblMsg);

        lblMessage.setText(String.format("Chatting with %s (%s)", userName, strState));
        this.state = newState;
    }

    // helper for sending notifications to the other client
    private void sendNotification(String type, String title, String message) {
        String payload = String.format("%s\t%s", type, myUserId);
        Buddy.sendPushNotification(toList, title, message, payload);
    }


    public static final int PART_MESSAGE = 0;
    public static final int PART_USERID = 1;
    public static String[] crackPayload(String payload) {
        String[] parts = payload.split("\t");

        return parts;
    }


    // Receiver for messages, push notification values from the other client
    // end up in here
    //
    private BroadcastReceiver onEvent=new BroadcastReceiver() {
        public void onReceive(Context ctx, Intent i) {

            // make sure we have a payload and a user id
            //
            String payload = i.getStringExtra("payload");

            if (payload == null) {
                return;
            }

            String[] parts = crackPayload(payload);

            String msg = parts[PART_MESSAGE];

            String id = parts[PART_USERID];


            // this isn't meant for this chat, ignore.
            //
            if (!userId.equals(id)) {
                return;
            }

            // set the time that we had last comms from the other client
            //
            lastPing = new Date();

            // walk through the message types
            //
            if (CHECK_MESSAGES.equals(msg)) {
                setState(STATE_CHATTING);
                loadMessages();
            }
            else if (CHAT_WAITING.equals(msg)) {
                // the client is waiting for us, so we know
                // they are connected.  Send a push response.
                //
                setState(STATE_CHATTING);
                sendNotification(CHAT_CONNECTED, null, null);
            }
            else if (CHAT_CONNECTED.equals(msg)) {

                // client has told us they are connected,
                // transition to chatting
                //
                setState(STATE_CHATTING);
            }
            else if (CHAT_END.equals(msg)) {
                setState(STATE_DISCONNECTED);
            }
        }
    };



    @Override
    public void onResume() {
        super.onResume();


        // on resume we hook up our listener and let the other
        // client know we are in the waiting state.
        //
        BuddyChatApplication.activeChat = this;

        // hook up our receiver
        IntentFilter f= new IntentFilter(GcmBroadcastReceiver.ACTION_MESSAGE_RECEIVED);

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(onEvent, f);

        // send a message letting the other client know that we are waiting
        //
        sendNotification(CHAT_WAITING, null, null);
    }

    @Override
    public void onPause() {

        // on pause we unhook our receiver and let the other client know we're not
        // chatting anymore.


        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(onEvent);


        sendNotification(CHAT_END, null, null);

        BuddyChatApplication.activeChat = null;

        super.onPause();
    }


    // for updating on a timer, we user this task to poke at load messages
    // on a schedule
    class ChatUpdateTask extends TimerTask {
        Chat parent;

        public ChatUpdateTask(Chat parent) {
            this.parent = parent;
        }

        public void run() {
            parent.runOnUiThread(new Runnable() {
                public void run() {

                    if (!BuddyChatApplication.isPushSupported) {
                        // pretty much just for the emulator.
                        parent.loadMessages();
                    }
                    // tell the other client we are still here.
                    //
                    long lastPingDelta = new Date().getTime() - lastPing.getTime();

                    if (lastPingDelta > 120000) {
                        // after two minutes, consider ourselves disconnected.
                        sendNotification(CHAT_WAITING, null,null);
                        setState(STATE_DISCONNECTED);
                    }
                    else if (lastPingDelta > 60000) {
                        setState(STATE_WAITING);
                        sendNotification(CHAT_WAITING, null,null);
                    }
                }
            });
        }
    }

    private void scrollMyListViewToBottom() {

        // when messages come in,
        // scroll to bottom to show new ones
        //
        _messages.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                _messages.setSelection(_messages.getCount() - 1);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.chat, menu);
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

    @Override
    public android.support.v4.app.FragmentManager getSupportFragmentManager() {
        return null;
    }

    class MessagesSimpleAdapter extends SimpleAdapter<Message> {

        public MessagesSimpleAdapter(Context c) {
            super(null, c);
        }

        protected <T> void populateView(View v, T u) {
            Message m = (Message)u;
            TextView text2 = (TextView) v.findViewById(android.R.id.text1);
            TextView text1 = (TextView) v.findViewById(android.R.id.text2);

            if (m.type == Message.MessageType.Received) {
                text1.setText(String.format("%s (%s)", userName, android.text.format.DateFormat.format("MM-dd hh:mm", m.created)));

            }
            else {
                text1.setText(String.format("You (%s)", android.text.format.DateFormat.format("MM-dd hh:mm", m.created)));
            }
            text2.setText(m.body);
        }
    }





}
