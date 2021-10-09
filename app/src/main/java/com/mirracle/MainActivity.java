package com.mirracle;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
//import com.microsoft.windowsazure.messaging.NotificationHub;
//import com.google.firebase.messaging.RemoteMessage;
import com.microsoft.windowsazure.messaging.FcmNativeRegistration;
import com.microsoft.windowsazure.messaging.Registration;
import com.microsoft.windowsazure.messaging.notificationhubs.InstallationTemplate;
import com.microsoft.windowsazure.messaging.notificationhubs.NotificationHub;
import com.microsoft.windowsazure.messaging.notificationhubs.NotificationHubException;
import com.microsoft.windowsazure.messaging.notificationhubs.NotificationHubInstallationAdapter;
//import com.microsoft.windowsazure.messaging.notificationhubs.NotificationListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
//import java.util.Map;


//The default main activity loads on start of the app all basic operations will be done here
public class MainActivity extends AppCompatActivity {
    public static MainActivity mainActivity;
    private static final String TAG = "MainActivity";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final int REQUEST_READ_CONTACTS = 79;
    ArrayList<User> listOfContacts = new ArrayList<>();
    public DatabaseHelper mydb = null;
    ArrayList mobileArray;
    String urlscheme = "";

    String Tags;
    String deviceId;
    public boolean isNotifenabled;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        mainActivity = this;
        setContentView(R.layout.activity_main);

        // if the existig value is not same probably then we shall..
        Utility.SetUserPreferences(Constants.DeviceId, Utility.GetDeviceUniqueId(this), this);


        String regId = Utility.GetUserPreference(Constants.registrationID, this);




        if (null == mydb)
            mydb = new DatabaseHelper(this);
        isNotifenabled = isNotificationChannelEnabled(this);
        NotificationHub.setListener(new CustomNotificationListener());
        ListProvider.populateListItem();
        // ATTENTION: This was to handle app links from out side like Notifications and deep links.
        Intent appLinkIntent = getIntent();
        Bundle remoteInput = RemoteInput.getResultsFromIntent(appLinkIntent);


        Uri appLinkData = appLinkIntent.getData();
        String widgetURL = appLinkIntent.getStringExtra(Constants.WidgetItem);
        if(null == appLinkData && Utility.IsNullOrEmpty(widgetURL)){
            urlscheme = "https://mirracle.pinnacleblooms.org/epass?ct=android";
        }else if(!Utility.IsNullOrEmpty(widgetURL)){
            widgetURL = widgetURL.toLowerCase().contains("widgeturl") ? widgetURL.split("widgeturl=")[1] : widgetURL;
            urlscheme = widgetURL;
        }
        else{
            urlscheme = appLinkData.toString();
        }
        if (remoteInput != null) {
            String feedbacktext = remoteInput.getCharSequence(
                    "key_reply").toString();

            //Set the inline reply text in the TextView
            ToastNotify("Reply is :"+feedbacktext);
            if(urlscheme.contains("?")){
                urlscheme = urlscheme + "&rating=" + "&fb="+ feedbacktext;
            }
            else {
                urlscheme = urlscheme + "?rating=" + "&fb="+ feedbacktext;
            }

            //Update the notification to show that the reply was received.
            NotificationCompat.Builder repliedNotification =
                    new NotificationCompat.Builder(this, "MirracleNotificationChannel")
                            .setSmallIcon(
                                    R.drawable.not_icon)
                            .setContentText("feedback received");

            NotificationManager notificationManager =
                    (NotificationManager)
                            getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(1,
                    repliedNotification.build());

        }
        String token = Utility.GetUserPreference(Constants.Token, this);
        //Checking if network available and loading the web activity
       if(Utility.isNetworkAvailable(this) || !Utility.IsNullOrEmpty(token)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.READ_CONTACTS)
                            == PackageManager.PERMISSION_GRANTED && isNotifenabled ) {
                        StartPWA(mainActivity);
                    } else {
                        Intent intent = new Intent(mainActivity, SplashActivity.class);
                        startActivity(intent);
                        mainActivity.finish();
                    }
                }
            });
      }
        TextView mButton = findViewById(R.id.btnRetry);
        TextView mSettings = findViewById(R.id.gotosettings);
        //Setting icon click event
        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Utility.OpenNetworkSettings(MainActivity.mainActivity);
            }
        });
        ImageView imgPHone = (ImageView) findViewById(R.id.imgWapp);
        //Whats app icon click event for support
        imgPHone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utility.startSupportChat(MainActivity.mainActivity);
            }
        });
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if(Utility.isNetworkAvailable(MainActivity.this)){
                    StartPWA(MainActivity.this);
                }else{
                    Toast.makeText(getApplicationContext(),"No Internet Available",Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    // This will be used to start the web activity
    public void StartPWA(Activity context){
        try {
            Intent intent = new Intent(this, com.google.androidbrowserhelper.trusted.LauncherActivity.class);
            intent.setData(Uri.parse(urlscheme));
            intent.putExtra("url",urlscheme);
            startActivityForResult(intent, 55);
            //startActivityForResult(intent, 99);
            context.finish();
        }catch(Exception ex){
            Log.e(TAG, "run: " + ex.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 55){
            this.finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String token = Utility.GetUserPreference(Constants.Token, this);
       if(Utility.isNetworkAvailable(this) || !Utility.IsNullOrEmpty(token)){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.READ_CONTACTS)
                            == PackageManager.PERMISSION_GRANTED &&isNotifenabled ) {
                        StartPWA(mainActivity);
                    } else {
                        Intent intent = new Intent(mainActivity, SplashActivity.class);
                        startActivity(intent);
                        mainActivity.finish();
                    }
                }
            });
        }
    }

    private void showNotificationAlert(){
        AlertDialog alertDialog = new AlertDialog.Builder(this)
//set icon
                .setIcon(android.R.drawable.ic_dialog_alert)
//set title
                .setTitle("Info")
//set message
                .setMessage("Please do allow Notifications to server you better.")
//set positive button
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //set what would happen when positive button is clicked
                        finish();
                    }
                })
//set negative button
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //set what should happen when negative button is clicked
                        Toast.makeText(getApplicationContext(),"Nothing Happened",Toast.LENGTH_LONG).show();
                    }
                })
                .show();
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog box that enables  users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported by Google Play Services.");
                ToastNotify("This device is not supported by Google Play Services.");
                finish();
            }
            return false;
        }
        return true;
    }

    //A sample toast message to call everywhere if needed
    public void ToastNotify(final String notificationMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, notificationMessage, Toast.LENGTH_LONG).show();
                //TextView helloText = (TextView) findViewById(R.id.text_hello);
                // helloText.setText(notificationMessage);
            }
        });
    }

    //This method will let you know isNotification Channele enabled or not
    public boolean isNotificationChannelEnabled(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (!manager.areNotificationsEnabled()) {
                return false;
            }
            List<NotificationChannel> channels = manager.getNotificationChannels();
            for (NotificationChannel channel : channels) {
                if (channel.getImportance() == NotificationManager.IMPORTANCE_NONE) {
                    return false;
                }
            }
            return true;
        } else {
            return NotificationManagerCompat.from(context).areNotificationsEnabled();
        }
    }

    //This method will let you know the concept of reading contacts
    public ArrayList ReadAndContacts() {
        ArrayList<String> nameList = new ArrayList<>();
        String userId =  Utility.GetUserPreference(Constants.UseruniqueId, mainActivity);
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ContentResolver cr = getContentResolver();
                        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
                        if ((cur != null ? cur.getCount() : 0) > 0) {
                            while (cur != null && cur.moveToNext()) {
                                User contact = new User();
                                String id = cur.getString(
                                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                                String name = cur.getString(cur.getColumnIndex(
                                        ContactsContract.Contacts.DISPLAY_NAME));
                                contact.Name = name;

                                if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                                    Cursor pCur = cr.query(
                                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                            null,
                                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                            new String[]{id}, null);
                                    while (pCur.moveToNext()) {
                                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                                ContactsContract.CommonDataKinds.Phone.NUMBER));

                                        if (Utility.IsNullOrEmpty(phoneNo)) {
                                            continue;
                                        }

                                        if (phoneNo.contains("*") || phoneNo.contains("#")) {
                                            continue;
                                        }

                                        phoneNo = phoneNo.trim().replace(" ", Constants.EMPTY_STRING).replace("+", Constants.EMPTY_STRING).replace("(", Constants.EMPTY_STRING).replace(")", Constants.EMPTY_STRING).trim();
                                        if (phoneNo.length() < 10) {
                                            continue;
                                        }

                                        if (phoneNo.length() == 10) {
                                            phoneNo = phoneNo;
                                        }

                                        contact.MobileNumber = phoneNo;
                                        contact.DeviceType = "ANDROID";
                                        contact.RefferedBy = userId;
                                    }
                                    pCur.close();
                                }

                                Cursor ce = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[]{id}, null);
                                if (ce != null && ce.moveToFirst()) {
                                   String email = ce.getString(ce.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                                    if (!Utility.IsNullOrEmpty(email)) {
                                        contact.Email = email;
                                    }

                                    ce.close();
                                }
                                if (!Utility.IsNullOrEmpty(contact.MobileNumber)) {
                                    listOfContacts.add(contact);
                                    mydb.addContact(contact);
                                }
                            }
                        }else{
                            Log.d(TAG, "run: Naveen closed");
                        }
                        if (cur != null) {
                            cur.close();
                        }
                    } catch (Exception ex) {
                        Log.e(TAG, "run: " + ex.getMessage());
                    }
                }

            }).start();
        } catch (Exception ex) {
            Log.e("getAllContacts: ", ex.getMessage());
        }

        return listOfContacts;
    }
}