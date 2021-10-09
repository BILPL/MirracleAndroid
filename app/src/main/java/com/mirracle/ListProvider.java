package com.mirracle;

import android.Manifest;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.Console;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

//This class provides list of items to show on widget
public class ListProvider implements RemoteViewsService.RemoteViewsFactory {
    /// <summary>
    /// Context holds the UI
    /// </summary>
    private static Context context;

    private static String language;

    /// <summary>
    /// List of Items
    /// </summary>
    private static List<ListItem> listItemList = new ArrayList<ListItem>();

    @Override
    public void onCreate() {

    }
    /// <summary>
    /// Instance of List Provider
    /// </summary>
    /// <param name="contextNew">context of UI</param>
    public ListProvider(Context contextNew)

    {
        context = contextNew;

        populateListItem();
    }
    /// <summary>
    /// This method used to Populate the list of items which we want to show in Widget
    /// </summary>
    public static void populateListItem()
   {
        try
        {
            String token = Utility.GetUserPreference(Constants.Token, context);
            if(null == context && null != MainActivity.mainActivity){
                context = MainActivity.mainActivity;
                token = Utility.GetUserPreference(Constants.Token, MainActivity.mainActivity);
            }

            if(Utility.IsNullOrEmpty(token)){
                return;
            }
            String endPoint = "https://pinnacleblooms.org/api/getwidgetdata?at=" + token;//"https://www.indiaherald.com/api/editorpicksdata?lang="+language +"&count=10&type=todaywidget";
            final ArrayList<Activity2Person> result = new ArrayList<Activity2Person>();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    HttpURLConnection connection = null;
                    try {
                        URL url = new URL(endPoint);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setDoOutput(true);
                        connection.setRequestMethod("GET"); // hear you are telling that it is a POST request, which can be changed into "PUT", "GET", "DELETE" etc.
                        connection.setRequestProperty("Content-Type", "application/json"); // here you are setting the `Content-Type` for the data you are sending which is `application/json`"application/octet-stream"
                        connection.connect();
                        StringBuilder result = new StringBuilder();

                        int responseCode = connection.getResponseCode();
                        if (responseCode >= 200 && responseCode <= 399) {
                            BufferedReader in = new BufferedReader(new InputStreamReader(
                                    connection.getInputStream()));
                            String inputLine;
                            StringBuffer response = new StringBuffer();

                            while ((inputLine = in.readLine()) != null) {
                                response.append(inputLine);
                            }
                            if(null != response){

                                Gson gson = new Gson();
                                //MainActivity.mainActivity.ToastNotify("received response :: " + responseCode + "   " + response.toString());
                                 listItemList = new ArrayList<ListItem>();
                                JSONObject object = new JSONObject(response.toString());
                                String res = object.getJSONArray("result").toString();
                                String lsd = object.getString("lsd");
                                Widget.updateViews.setTextViewText(R.id.syncDateTime, lsd);
                                JSONArray jarray= new JSONArray(res);
                                for(int i=0; i< jarray.length(); i++){
                                    Activity2Person item = new Gson().fromJson(jarray.get(i).toString(),Activity2Person.class );
                                    ListItem listItem = new ListItem();
                                    listItem.heading = item.value;
                                    listItem.content =item.key;
                                    listItem.address = item.url;
                                    int resourceId = context.getResources().getIdentifier(item.img.replace(".png", ""), "drawable", context.getPackageName());
                                    try {
                                        listItem.imageView = BitmapFactory.decodeResource(context.getResources(), resourceId);
                                        listItemList.add(listItem);
                                    } catch(Exception e) {
                                        System.out.println(e);
                                        e.printStackTrace();
                                        listItem.imageView = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
                                    }
                                }
                                if (null != context)
                                {
                                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                                    ComponentName thisWidget = new ComponentName(context, Widget.class);
                                    int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
                                    RemoteViews remoteViews = Widget.buildUpdate(context, lsd);
                                    appWidgetManager.updateAppWidget(thisWidget, remoteViews);
                                    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.gridViewWidget);
                                }
                                Log.d(TAG, "run: ");
                            }
                            in.close();
                        } else {

                        }

                    } catch (Exception ex) {
                        Log.e(TAG, "RegisterOrUpdateDeviceToken: ");
                        ex.printStackTrace();
                    } finally {
                        if(connection !=null){connection.disconnect(); 	}
                    }
                }

            }).start();

        }
        catch (Exception ex)
        {
            Log.e(TAG, "ListProvider, populateListItem " + ex.getMessage());
            ex.printStackTrace();
        }
   }


    @Override
    public void onDataSetChanged() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        if(null !=listItemList){
            return listItemList.size();
        }else{
            return 0;
        }

    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.widget_item_layout);
        try
        {
            ListItem listItem = listItemList.get(position);
            Intent fillInIntent = new Intent();
            Bundle extras = new Bundle();
            extras.putString(Constants.WidgetItem, listItem.address);
            fillInIntent.putExtras(extras);
            remoteView.setTextViewText(R.id.heading1, listItem.heading);
            remoteView.setTextViewText(R.id.content1, listItem.content);
            //remoteView.setTextViewText(R.id.syncDateTime, listItem.dateTime);
            remoteView.setImageViewBitmap(R.id.imageView, listItem.imageView);
            Widget.updateViews.setTextViewText(R.id.syncDateTime, "Naveen");
            remoteView.setOnClickFillInIntent(R.id.heading1, fillInIntent);
            remoteView.setOnClickFillInIntent(R.id.content1, fillInIntent);
            remoteView.setOnClickFillInIntent(R.id.imageView, fillInIntent);
        }
        catch (Exception ex)
        {
            Log.e(TAG, "ListProvider, GetViewAt " + ex.getMessage());
        }



        return remoteView;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}

//This class gives the respective fields to list provider
class ListItem
{
    public String heading, content, address, dateTime;

    public Bitmap imageView;
}
