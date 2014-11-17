package com.buddy.sdk;

import android.content.Context;
import android.content.Intent;
import android.location.Location;

import com.buddy.sdk.models.NotificationResult;
import com.buddy.sdk.models.TimedMetric;
import com.buddy.sdk.models.User;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.Future;

public class Buddy {
    private static BuddyClient _currentClient;

    private static BuddyClient getCurrentClient()
    {
        if(_currentClient == null)
            throw new RuntimeException("Buddy.init must be called before calling any service methods.");

        return _currentClient;
    }

    private static Map<String, BuddyClient> _clients = new HashMap<String, BuddyClient>();

    private static String getClientKey(String appId, BuddyClientOptions options)
    {
        return appId + options.instanceName;
    }

    public static BuddyClient init(Context context, String appId, String appKey)
    {
        return  init(context, appId,appKey, null);
    }

    public static BuddyClient init(Context context, String appId, String appKey, BuddyClientOptions options)
    {
        if(options == null)
        {
            options = new BuddyClientOptions();
        }

        BuddyClient client = null;

        String key = getClientKey(appId, options);

        if(_clients.containsKey(key))
        {
            client = _clients.get(key);
        }
        else {
            client = new BuddyClientImpl(context, appId, appKey, options);
            _clients.put(key, client);
        }

        _currentClient = client;

        return client;
    }

    public static <T> Future<BuddyResult<T>> get(String path, Map<String, Object> parameters, Class<T> clazz) {
        return getCurrentClient().<T>get(path, parameters, clazz);
    }

    public static <T> Future<BuddyResult<T>> get(String path, Map<String, Object> parameters, final BuddyCallback<T> callback)
    {
        return getCurrentClient().<T>get(path, parameters, callback);
    }

    public static <T> Future<BuddyResult<T>> post(String path, Map<String, Object> parameters, Class<T> clazz)
    {
        return getCurrentClient().<T>post(path, parameters, clazz);
    }

    public static <T> Future<BuddyResult<T>> post(String path, Map<String, Object> parameters, final BuddyCallback<T> callback)
    {
        return getCurrentClient().<T>post(path, parameters, callback);
    }

    public static <T> Future<BuddyResult<T>> patch(String path, Map<String, Object> parameters, Class<T> clazz)
    {
        return getCurrentClient().<T>patch(path, parameters, clazz);
    }

    public static <T> Future<BuddyResult<T>> patch(String path, Map<String, Object> parameters, final BuddyCallback<T> callback)
    {
        return getCurrentClient().<T>patch(path, parameters, callback);
    }

    public static <T> Future<BuddyResult<T>> delete(String path, Map<String, Object> parameters, Class<T> clazz)
    {
        return getCurrentClient().<T>delete(path, parameters, clazz);
    }

    public static <T> Future<BuddyResult<T>> delete(String path, Map<String, Object> parameters, final BuddyCallback<T> callback)
    {
        return getCurrentClient().<T>delete(path, parameters, callback);
    }

    public static <T> Future<BuddyResult<T>> put(String path, Map<String, Object> parameters, Class<T> clazz)
    {
        return getCurrentClient().<T>put(path, parameters, clazz);
    }

    public static <T> Future<BuddyResult<T>> put(String path, Map<String, Object> parameters, final BuddyCallback<T> callback)
    {
        return getCurrentClient().<T>put(path, parameters, callback);
    }

    public static void setUserAuthenticationRequriedCallback(UserAuthenticationRequiredCallback callback)
    {
        getCurrentClient().setUserAuthenticationRequiredCallback(callback);
    }

    public static void setLastLocation(Location loc){
        getCurrentClient().setLastLocation(loc);
    }

    public static Location getLastLocation()
    {
        return getCurrentClient().getLastLocation();
    }

    private static String getServiceRoot()
    {
        return getCurrentClient().getServiceRoot();
    }

    public static Future<BuddyResult<User>> getCurrentUser(final BuddyCallback<User> callback){
        return getCurrentClient().getCurrentUser(callback);
    }

    public static Future<BuddyResult<User>> createUser(String username, String password,
            String firstName, String lastName, String email, Date dateOfBirth, String gender,
            String tag, final BuddyCallback<User> callback)
    {
        return getCurrentClient().createUser(username, password, firstName, lastName, email, dateOfBirth,
                gender, tag, callback);
    }

    public static Future<BuddyResult<User>> loginUser(String username, String password, final BuddyCallback<User> callback)
    {
        return getCurrentClient().loginUser(username, password, callback);
    }

      public static Future<BuddyResult<User>> socialLogin(String identityProviderName, String identityId, String identityAccessToken, final BuddyCallback<User> callback)
    {
        return getCurrentClient().socialLogin(identityProviderName, identityId, identityAccessToken, callback);
    }

    public static Future<BuddyResult<Boolean>> logoutUser(final BuddyCallback<Boolean> callback)
    {
        return getCurrentClient().logoutUser(callback);
    }

    public static Future<BuddyResult<TimedMetric>> recordMetricEvent(String eventName, Map<String,Object> values,
        final int timeoutInSeconds, final BuddyCallback<TimedMetric> callback)
    {
        return getCurrentClient().recordMetricEvent(eventName, values, timeoutInSeconds, callback);
    }

    public static Future<BuddyResult<Boolean>> setPushToken(String pushToken, final BuddyCallback<Boolean> callback)
    {
        return getCurrentClient().setPushToken(pushToken, callback);
    }

    public static Future<BuddyResult<NotificationResult>> sendPushNotification(List<String> recipientIds, String title, String message, String payload) {
        return getCurrentClient().sendPushNotification(recipientIds, title, message, payload);
    }

    public static Future<BuddyResult<NotificationResult>> sendPushNotification(List<String> recipientIds, String title, String message, String payload, int counterValue) {
         return getCurrentClient().sendPushNotification(recipientIds, title, message, payload, counterValue);
    }

    public static Future<BuddyResult<NotificationResult>> sendPushNotification(List<String> recipientIds, Map<String,Object> osCustomData){
        return getCurrentClient().sendPushNotification(recipientIds, osCustomData);
    }

    public static void recordNotificationReceived(Intent message)
    {
        getCurrentClient().recordNotificationReceived(message);
    }
}