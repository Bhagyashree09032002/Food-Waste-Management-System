package com.example.ne.foodneed.firebase;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.speech.tts.TextToSpeech;
import android.util.Log;


import com.example.ne.foodneed.NgoHomepage;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCM Service";
    private static TextToSpeech tts;
    Geocoder geocoder;
    String message="",lat,lon,id="",title="";
    private String sharedPrefFile = "com.example.source.foodneedsharedprefs";
    private SharedPreferences.Editor editor;
    private SharedPreferences sharedPreferences;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        System.out.print(remoteMessage);

        tts=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.ENGLISH);
                }
            }
        });

        if (remoteMessage.getData().size() > 0) {
            Log.e(TAG, "Data Payload: " + remoteMessage.getData().toString());
            try {
                JSONObject json = new JSONObject(remoteMessage.getData().toString());
                JSONObject data = json.getJSONObject("data");

                title = data.getString("title");
                message = data.getString("message");
                id = data.getString("id");

                sharedPreferences = getSharedPreferences(sharedPrefFile,MODE_PRIVATE);
                editor = sharedPreferences.edit();
                editor.putString("id",id);
                editor.commit();

                geocoder = new Geocoder(MyFirebaseMessagingService.this, Locale.getDefault());

                sendPushNotification(json);

            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
            }
        }
    }

    private void sendPushNotification(JSONObject json) {
        //optionally we can display the json into log
        Log.e(TAG, "Notification JSON " + json.toString());
        try {
            //getting the json data

            String imageUrl = "null";




            //creating MyNotificationManager object
            MyNotificationManager mNotificationManager = new MyNotificationManager(getApplicationContext());

            //creating an intent for the notification
            Intent intent = new Intent(getApplicationContext(), NgoHomepage.class);

            //if there is no image
            if(imageUrl.equals("null")){
                //displaying small notification
                mNotificationManager.showSmallNotification(title, message, intent);
            }else{
                //if there is an image
                //displaying a big notification
                mNotificationManager.showBigNotification(title, message, imageUrl, intent);
            }
        } catch (Exception e) {
            Log.e(TAG, "Json Exception: " + e.getMessage());
        }
    }
}