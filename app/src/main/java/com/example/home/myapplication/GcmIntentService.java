package com.example.home.myapplication;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.home.myapplication.Server.Common;
import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * Created by icelancer on 15. 2. 21..
 */
public class GcmIntentService extends IntentService {
    public static final String TAG = "icelancer";
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public GcmIntentService() {
//        Used to name the worker thread, important only for debugging.
        super("GcmIntentService");
    }
    public static String message ="";
    public static String getMsg(){
        return GcmIntentService.message;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {
           if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
               // This loop represents the service doing some work.
               for (int i=0; i<5; i++) {
                   Log.i(TAG, "Working... " + (i + 1) + "/5 @ " + SystemClock.elapsedRealtime());
                   try {
                       Thread.sleep(10);
                   } catch (InterruptedException e) {
                   }
               }
               Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());
               // Post notification of received message.
               //-------------------------------------------------------------------------------
                //맨처음 깔렸을 때 notification 실행 안되도록
               //-------------------------------------------------------------------------------

               sendNotification(extras.toString());
               Log.i(TAG, "Received: " + extras.toString());
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
        message = msg;
        if(!msg.contains("message")){
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.onepieace)
                            .setContentTitle("MCN_TALKK")
                            .setStyle(new NotificationCompat.BigTextStyle().bigText("설치가 완료되었습니다.")).setContentText("");
            mBuilder.setContentIntent(contentIntent);
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        }
        else{
            String info[]=parsing_string(msg);
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.onepieace)
                            .setContentTitle("MCN_TALKK")
                            .setStyle(new NotificationCompat.BigTextStyle().bigText("from : "+info[0]+" message : "+info[1])).setContentText("");
            mBuilder.setContentIntent(contentIntent);
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        }
    }
    public String[] parsing_string(String msg){
        String totalmsg=msg;
        String info[]=new String[2];
        int _fromidx = 9999;
        int _fromind = 9999;
        int _toidx = 9999;
        int _msgidx = 9999;
        ContentValues values = new ContentValues();
        _fromidx = totalmsg.indexOf("_from=");
        _fromind = totalmsg.indexOf(", android.support");
        _toidx = totalmsg.indexOf("_message=");
        _msgidx = totalmsg.indexOf(", from=");

        info[0]=totalmsg.substring(_fromidx + 6, _fromind);
        info[1]=totalmsg.substring(_toidx + 9, _msgidx);
        values.put(DataProvider.COL_TAKE, Common.getMyemail());
        values.put(DataProvider.COL_GIVE, info[0]);  //from
        values.put(DataProvider.COL_MSG, info[1]); //message
        Log.d("저장",info[0]+info[1]);
        getContentResolver().insert(DataProvider.CONTENT_URI_MESSAGES,values);

        return info;
    }
}
