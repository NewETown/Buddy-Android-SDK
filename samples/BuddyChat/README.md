# Buddy Chat Sample

This application demonstrates a simple chat application using the following Buddy features:

* User creation, login, logout
* Message send and receive
* Push notifications 

Buddy Chat is fully functional, but it's a sample so it's rough around the edges and needs work to be production ready.

## Running the Sample

To run the sample:

1. Open the project in Android Studio
2. Open BuddyChatApplication.java
3. Replace `SENDER_ID` with the GCM Sender ID, which is the Google API Project Number, 13-digit number which you will find in the URL of the Google API Console website.  Go to the [main console page](https://console.developers.google.com/project), then choose a project name.  You'll see the SENDER_ID as the project number.
4. Create an application at [the Buddy Dashboard](https://buddyplatform.com), and replace `APPID` and 'APPKEY' withe the appropriate values from your application.
5. To get an API key for GCM, follow the steps [here](http://developer.android.com/google/gcm/gs.html), then under the Google Project, find "APIs & Auth / Credentials" and copy the "Key for server applications / API KEY".  Paste this value into the Buddy Dashboard. Now, your Buddy application dashboard, choose "Push", then "Add GCM API Key", and paste in the GCM API Key.

You're now ready to run the sample. 

## Key Points

The sample uses messaging to save chat state.  All chats are done via the Buddy Messages APIs.  However, the application uses push notifications to communicate state information back-and-forth between the chat apps.  This prevents the applicaiton from needing to poll the messages API, which is very bad for battery life.

When the application receives a push notification, it checks the notification for what action it should perform, such as updating the chatting state with the other client, or fetching new messages.

This also allows the chat to be launched from a notification in the device tray.

The code for the chat scenario can primarily be found in Chat.java.