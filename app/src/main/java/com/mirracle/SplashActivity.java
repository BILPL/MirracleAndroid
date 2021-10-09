package com.mirracle;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
//The default splash activity used for showing the splash screen
public class SplashActivity extends AppCompatActivity {
    public static SplashActivity splashActivity;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final int REQUEST_READ_CONTACTS = 79;
    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        splashActivity = this;

        if(!MainActivity.mainActivity.isNotifenabled){
            Utility.showDialog(this, "notification");
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (ActivityCompat.checkSelfPermission(splashActivity, Manifest.permission.READ_CONTACTS)
                        == PackageManager.PERMISSION_GRANTED) {
                    //Utility.SyncContactsToServer(splashActivity);
                } else {
                    Pokhran();
                }
            }
        });
    }

    public void Pokhran() {
        try {
            String uId = Utility.GetUserPreference(Constants.UseruniqueId, splashActivity);
            String token = Utility.GetUserPreference(Constants.Token, splashActivity);
            if (!Utility.IsNullOrEmpty(token)) {
               requestPermission();
            }
        } catch (Exception ex) {

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null == Utility.dialog) {
            requestPermission();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        requestPermission();
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
        try {

            if (requestCode == REQUEST_READ_CONTACTS) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (ActivityCompat.checkSelfPermission(splashActivity, Manifest.permission.READ_CONTACTS)
                                == PackageManager.PERMISSION_GRANTED) {
                           // Utility.SyncContactsToServer(splashActivity);
                        } else {
                            requestPermission();
                        }
                    }
                });

            }

        } catch (Exception ex) {

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_READ_CONTACTS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Utility.SyncContactsToServer(this);
                    this.StartPWA(this, "");
                } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    // MainActivity.mainActivity.StartPWA(this);
                    if (null == Utility.dialog) {
                        Utility.showDialog(this, "contact");
                    }
                }
                break;
            }

        }
    }

    public void StartPWA(Activity context, String urlscheme) {
        try {
            if (Utility.IsNullOrEmpty(urlscheme)) {
                urlscheme = "https://mirracle.pinnacleblooms.org/epass?ct=android";
            }
            Intent intent = new Intent(this, com.google.androidbrowserhelper.trusted.LauncherActivity.class);
            intent.setData(Uri.parse(urlscheme));
            intent.putExtra("url", urlscheme);
            startActivityForResult(intent, 55);
            //startActivityForResult(intent, 99);
            context.finish();
        } catch (Exception ex) {
            Log.e(TAG, "run: " + ex.getMessage());
        }
    }

    private void requestPermission() {
        try {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                // show UI part if you want here to show some rationale !!!
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS},
                        REQUEST_READ_CONTACTS);
            }
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                    this.requestPermissions(new String[]{
                            Manifest.permission.READ_CONTACTS,

                    }, REQUEST_READ_CONTACTS);
                }
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS},
                        REQUEST_READ_CONTACTS);
            }

            // StartPWA();
        } catch (Exception ex) {
            Log.e("requestPermission: ", ex.getMessage());
        }

    }
}