package com.mirracle;

import android.content.Context;

import com.microsoft.windowsazure.messaging.notificationhubs.NotificationHubInstallationAdapter;

public class MirracleNHInstallationAdapter extends NotificationHubInstallationAdapter {

    public MirracleNHInstallationAdapter(Context context, String hubName, String connectionString) {
        super(context, hubName, connectionString);
    }

}