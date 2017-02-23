package com.umer.towtruckdriver.Firebase;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.umer.towtruckdriver.Constants.Url;
import com.umer.towtruckdriver.activty.LoginActivity;
import com.umer.towtruckdriver.MainActivity;
import com.umer.towtruckdriver.Models.CustomerRequestInfo;
import com.umer.towtruckdriver.utils.NotificationID;
import com.umer.towtruckdriver.R;
import com.umer.towtruckdriver.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by MuhammadUmer on 10/20/2016.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Utils.showLoger(" MyFirebaseMsgService From: " + remoteMessage.getFrom());

        if(remoteMessage.getNotification() != null){
            sendNotification(remoteMessage.getNotification().getTitle(),remoteMessage.getNotification().getBody());
        }

        if (remoteMessage.getData().size() > 0) {
            Utils.showLoger(" MyFirebaseMsgService Data Payload: " + remoteMessage.getData().toString());

            try {
                JSONObject json = new JSONObject(remoteMessage.getData());
                handleDataMessage(json);
            } catch (Exception e) {
                Utils.showLoger(" MyFirebaseMsgService Exception: " + e.getMessage());
            }
        }

    }

    private void handleDataMessage(JSONObject json) {
        Utils.showLoger(TAG+" push json: " + json.toString());

        try {

            String title = json.getString("title");
            String message = json.getString("message");
            String type = json.getString("type");

            String info = json.getString("info");

            info = info.replaceAll("^\"|\"$", "");

            LoginActivity.customerRequestInfo = new Gson().fromJson(info,CustomerRequestInfo.class);

            checkCustomerInfo();

            if (!Utils.isAppIsInBackground(getApplicationContext())) {
                Utils.showLoger(TAG+" isAppIsInBackground: " +message);
                Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(),sound);
                r.play();

                Intent intent = new Intent("really");
                intent.putExtra("title",title);
                intent.putExtra("message",message);
                intent.putExtra("type",type);

                getApplicationContext().sendBroadcast(intent);
            } else {

                sendNotification(title,message);

            }
        } catch (JSONException e) {
            Utils.showLoger(TAG+" Json Exception: " + e.getMessage());
        } catch (Exception e) {
            Utils.showLoger(TAG+"Exception: " + e.getMessage());
        }
    }

    private void checkCustomerInfo() {

        if(LoginActivity.customerRequestInfo.getDisplayPicture() != null && !LoginActivity.customerRequestInfo.getDisplayPicture().isEmpty()){
            LoginActivity.customerRequestInfo.setDisplayPicture(Url.NoDpUrl);
        }
    }


    private void sendNotification(String title,String messageBody) {
        Utils.showLoger(TAG+" messageBody: " + messageBody);
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int requestCode = 0;
        PendingIntent pendingIntent = PendingIntent.getActivity(this, requestCode, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder noBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText(messageBody)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setSound(sound)
                .setContentTitle(title).setTicker("ticker");
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NotificationID.getid(), noBuilder.build());
    }
}
