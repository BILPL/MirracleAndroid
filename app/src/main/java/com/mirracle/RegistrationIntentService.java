package com.mirracle;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.microsoft.windowsazure.messaging.NotificationHub;
import com.microsoft.windowsazure.messaging.Registration;
import java.util.concurrent.TimeUnit;

public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";
    static String FCM_token = null;
    private static final int JOB_ID = 2;
    private static NotificationHub hub;

    public RegistrationIntentService() {
        super(TAG);
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        //enqueueWork(context, RegistrationIntentService.class, JOB_ID, work);
        SharedPreferences sharedPreferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(MainActivity.mainActivity);
        String resultString = null;
        String regID = null;
        String storedToken = null;

        try {
            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                @Override
                public void onSuccess(InstanceIdResult instanceIdResult) {
                    FCM_token = instanceIdResult.getToken();
                    Log.d(TAG, "FCM Registration Token: " + FCM_token);
                    Utility.SetUserPreferences(Constants.DeviceToken, FCM_token, MainActivity.mainActivity);
                }
            });
            TimeUnit.SECONDS.sleep(1);
            String userId = Utility.GetUserPreference(Constants.Tags, MainActivity.mainActivity);
            String fcm = Utility.GetUserPreference(Constants.DeviceToken, MainActivity.mainActivity);
            // Storing the registration ID that indicates whether the generated token has been
            // sent to your server. If it is not stored, send the token to your server.
            // Otherwise, your server should have already received the token.
            if (((regID=sharedPreferences.getString("registrationID", null)) == null)){
                //if(null == hub){
                    NotificationHub hub1 = new NotificationHub(NotificationSettings.HubName,
                            NotificationSettings.HubListenConnectionString, this);
                //}
                Log.d(TAG, "Attempting a new registration with NH using FCM token : " + FCM_token);
                if(!Utility.IsNullOrEmpty(userId)){
                    Registration registration = hub1.register(fcm, userId);
                    regID = registration.getRegistrationId();
                }
                //  regID = Utility.IsNullOrEmpty(FCM_token) ? hub.register(fcm, userId).getRegistrationId() : hub.register(FCM_token, userId).getRegistrationId();//hub.register(FCM_token, userId).getRegistrationId();

                // If you want to use tags...
                // Refer to : https://azure.microsoft.com/documentation/articles/notification-hubs-routing-tag-expressions/
                // regID = hub.register(token, "tag1,tag2").getRegistrationId();

                resultString = "New NH Registration Successfully - RegId : " + regID;
                Log.d(TAG, resultString);

                // sharedPreferences.edit().putString("registrationID", regID ).apply();
                //sharedPreferences.edit().putString("FCMtoken", FCM_token ).apply();
            }

            // Check to see if the token has been compromised and needs refreshing.
            else if (!(storedToken = sharedPreferences.getString("FCMtoken", "")).equals(FCM_token)) {

                NotificationHub hub = new NotificationHub(NotificationSettings.HubName,
                        NotificationSettings.HubListenConnectionString, MainActivity.mainActivity);
                Log.d(TAG, "NH Registration refreshing with token : " + FCM_token);
                if(!Utility.IsNullOrEmpty(userId))
                    regID = Utility.IsNullOrEmpty(FCM_token) ? hub.register(fcm, userId).getRegistrationId() : hub.register(FCM_token, userId).getRegistrationId();//regID = hub.register(FCM_token, userId).getRegistrationId();

                // If you want to use tags...
                // Refer to : https://azure.microsoft.com/documentation/articles/notification-hubs-routing-tag-expressions/
                // regID = hub.register(token, "tag1,tag2").getRegistrationId();

                resultString = "New NH Registration Successfully - RegId : " + regID;
                Log.d(TAG, resultString);

                sharedPreferences.edit().putString("registrationID", regID ).apply();
                sharedPreferences.edit().putString("FCMtoken", FCM_token ).apply();
            }

            else {
                resultString = "Previously Registered Successfully - RegId : " + regID;
            }
        } catch (Exception e) {
            Log.e(TAG, resultString="Failed to complete registration", e);
            // If an exception happens while fetching the new token or updating registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
        }

        // Notify UI that registration has completed.
//        if (MainActivity.isVisible) {
//            MainActivity.mainActivity.ToastNotify(resultString);
//        }
    }
}