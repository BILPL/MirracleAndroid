package com.mirracle;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.androidbrowserhelper.trusted.LauncherActivity;
import com.microsoft.windowsazure.messaging.Registration;
import com.microsoft.windowsazure.messaging.notificationhubs.NotificationHub;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {
    public static DashboardActivity dashboardActivity;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        dashboardActivity = this;
        Intent intent1 = this.getIntent();
        //We are reading url scheme here and storing in local variables for future use
        if (null != intent1 && null != intent1.getData() && null != intent1.getData().getHost()) {
            String urlscheme = intent1.getData().getHost();
            String scheme = intent1.getData().toString();
            switch (urlscheme){
                case "loginsuccess":
                    String uId = scheme.split("id=")[1].split("&t=")[0];
                    String token = scheme.split("&t=")[1].split("&r=")[0];
                    String andReg = scheme.split("&ans=")[1];
                    try {
                        token = java.net.URLEncoder.encode(token, StandardCharsets.UTF_8.displayName());
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    Utility.SetUserPreferences(Constants.UseruniqueId, uId, this);
                    Utility.SetUserPreferences(Constants.Token, token, this);
                    Utility.SetUserPreferences(Constants.Tags, andReg, this);
                    NotificationHub.setListener(new CustomNotificationListener());
                    //Here we are registering the notification with server
                    NotificationHub.start(this.getApplication(),NotificationSettings.HubName, NotificationSettings.HubListenConnectionString);

                    NotificationHub.setInstallationSavedListener(i -> {
                        Toast.makeText(this, "SUCCESS", Toast.LENGTH_LONG).show();
                        String a = NotificationHub.getInstallationId();
                    });
                    NotificationHub.setInstallationSaveFailureListener(e -> Toast.makeText(this,e.getMessage(), Toast.LENGTH_LONG).show());
                    String[] tags=andReg.split(",");
                    NotificationHub.addTags(Arrays.asList(tags));

                    break;
                case "settings":
                    Utility.OpenNetworkSettings(this);
                    break;
                case "logoff":
                    Utility.RemovePreference(this);
                    break;
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            //Utility.SyncContactsToServer(this);
        } else {
            SplashActivity.splashActivity.Pokhran();
        }

        this.finish();
    }
}