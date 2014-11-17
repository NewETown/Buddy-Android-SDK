package chat.sample.buddy.com.buddychat;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.buddy.sdk.Buddy;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.util.Date;

/**
 * Created by shawn on 8/27/14.
 */
public class GcmIntentService extends IntentService {


    public static final String ACTION_MESSAGE_RECEIVED =
            "chat.sample.buddy.com.buddychat.MESSAGE_RECEIVED";


    public GcmIntentService() {
        super(BuddyChatApplication.SENDER_ID);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                Log.e(BuddyChatApplication.TAG,"Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
                Log.w(BuddyChatApplication.TAG, "Deleted messages on server: " +
                        extras.toString());
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {

                // Post notification of received message.
                Buddy.recordNotificationReceived(intent);

                String userName = extras.getString("userName");
                String message = extras.getString("message");
                String payload = extras.getString("payload");


                sendNotification(userName, message, payload);
            Log.i(BuddyChatApplication.TAG, "Received: " + extras.toString());
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }


    private void populateIntent(Intent intent,String u, String m, String p) {

        String uid = null;
        if (p != null) {

            String[] parts = Chat.crackPayload(p);
            if (parts.length == 2) {
                uid = parts[Chat.PART_USERID];
            }
        }

        intent.putExtra("payload", p);
        intent.putExtra("userId", uid);
        intent.putExtra("userName", u);
        intent.putExtra("message", m);
    }

    // Do something with the push info we got
    //
    private void sendNotification(String userName, String message, String payload) {



        Intent payloadIntent = new Intent(ACTION_MESSAGE_RECEIVED);


        // send message notification
        if (payload != null && BuddyChatApplication.activeChat != null) {

            // if we got a payload and a chatwindow is being shown
            // then just send a payload-only message
            //
            payloadIntent.putExtra("payload", payload);
        }
        else {

            // otherwise, no chat is shown so show the push UI
            //

            populateIntent(payloadIntent, userName, message, payload);

            if (message != null) {

                Intent innerIntent = new Intent(this, Chat.class);

                NotificationManager notificationManager = (NotificationManager)
                        this.getSystemService(Context.NOTIFICATION_SERVICE);


                populateIntent(innerIntent, userName, message, payload);


                NotificationCompat.Builder builder =
                        new NotificationCompat.Builder(this)
                                .setSmallIcon(R.drawable.ic_launcher)
                                .setContentText(message)
                                .setContentTitle("Buddy Chat")
                                .setContentIntent(PendingIntent.getActivity(this, 0, innerIntent, PendingIntent.FLAG_CANCEL_CURRENT))
                                .setAutoCancel(true)
                                .setPriority(1)
                                .setDefaults(Notification.DEFAULT_ALL);

                notificationManager.notify((int)new Date().getTime(), builder.build());
            }

        }

        LocalBroadcastManager.getInstance(getApplicationContext())
                .sendBroadcast(payloadIntent);


    }
}