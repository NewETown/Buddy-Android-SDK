package chat.sample.buddy.com.buddychat;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Created by shawn on 8/27/14.
 */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {


    public static final String ACTION_MESSAGE_RECEIVED =
            "chat.sample.buddy.com.buddychat.MESSAGE_RECEIVED";



    @Override
    public void onReceive(Context context, Intent intent) {
        // Explicitly specify that GcmIntentService will handle the intent.
        ComponentName comp = new ComponentName(context.getPackageName(),
                GcmIntentService.class.getName());
        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, (intent.setComponent(comp)));



        setResultCode(Activity.RESULT_OK);
    }
}