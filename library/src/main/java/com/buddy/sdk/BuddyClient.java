package com.buddy.sdk;

import android.content.Intent;
import android.location.Location;

import com.buddy.sdk.models.NotificationResult;
import com.buddy.sdk.models.TimedMetric;
import com.buddy.sdk.models.User;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public interface BuddyClient{
    public void setUserAuthenticationRequiredCallback(UserAuthenticationRequiredCallback callback);
    public void setLastLocation(Location loc);
    public Location getLastLocation();
    public String getServiceRoot();

    public <T> Future<BuddyResult<T>> get(String path, Map<String,Object> parameters, Class<T> clazz);
    public <T> Future<BuddyResult<T>> get(String path, Map<String,Object> parameters, final BuddyCallback<T> callback);
    public <T> Future<BuddyResult<T>> post(String path, Map<String,Object> parameters, Class<T> clazz);
    public <T> Future<BuddyResult<T>> post(String path, Map<String,Object> parameters, final BuddyCallback<T> callback);
    public <T> Future<BuddyResult<T>> patch(String path, Map<String,Object> parameters, Class<T> clazz);
    public <T> Future<BuddyResult<T>> patch(String path, Map<String,Object> parameters, final BuddyCallback<T> callback);
    public <T> Future<BuddyResult<T>> delete(String path, Map<String,Object> parameters, Class<T> clazz);
    public <T> Future<BuddyResult<T>> delete(String path, Map<String,Object> parameters, final BuddyCallback<T> callback);
    public <T> Future<BuddyResult<T>> put(String path, Map<String,Object> parameters,  Class<T> clazz);
    public <T> Future<BuddyResult<T>> put(String path, Map<String,Object> parameters, final BuddyCallback<T> callback);

    public Future<BuddyResult<User>> getCurrentUser(final BuddyCallback<User> callback);
    public Future<BuddyResult<User>> createUser(String username, String password, String firstName, String lastName, String email, Date dateOfBirth, String gender, String tag, final BuddyCallback<User> callback);
    public Future<BuddyResult<User>> loginUser(String username, String password, final BuddyCallback<User> callback);
    public Future<BuddyResult<User>> socialLogin(String identityProviderName, String identityId, String identityAccessToken, final BuddyCallback<User> callback);
    public Future<BuddyResult<Boolean>> logoutUser(final BuddyCallback<Boolean> callback);
    public Future<BuddyResult<TimedMetric>> recordMetricEvent(String eventName, Map<String,Object> values, final int timeoutInSeconds, final BuddyCallback<TimedMetric> callback);
    public Future<BuddyResult<Boolean>> setPushToken(String pushToken, final BuddyCallback<Boolean> callback);
    public Future<BuddyResult<NotificationResult>> sendPushNotification(List<String> recipientIds, String title, String message, String payload);
    public Future<BuddyResult<NotificationResult>> sendPushNotification(List<String> recipientIds, String title, String message, String payload, int counterValue);
    public void recordNotificationReceived(Intent message);
    void handleError(BuddyResult result);
    void getAccessToken(boolean autoRegister, final AccessTokenCallback callback);
}
