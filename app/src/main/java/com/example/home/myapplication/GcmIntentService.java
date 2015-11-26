package com.example.home.myapplication;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.home.myapplication.Data.ChattingData;
import com.example.home.myapplication.Server.Common;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.util.Iterator;
import java.util.List;

/**
 * Created by icelancer on 15. 2. 21..
 */
public class GcmIntentService extends IntentService {
    public static final String TAG = "icelancer";
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    String update_give,update_msg;
    public GcmIntentService() {
//        Used to name the worker thread, important only for debugging.
        super("GcmIntentService");
    }
    public static String message ="";

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {
           if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
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
    private void get_generated_activity(){//Chatting_Activity의 리스트뷰의 아이템을 갱신한다.
        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(getApplicationContext().ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> info;
        info = activityManager.getRunningTasks(1);
        for (Iterator iterator = info.iterator(); iterator.hasNext();)  {
            ActivityManager.RunningTaskInfo runningTaskInfo = (ActivityManager.RunningTaskInfo) iterator.next();
            if(runningTaskInfo.topActivity.getClassName().equals("com.example.home.myapplication.Chatting_Activity")) {
                Chatting_Activity.chatting_list.add((new ChattingData(update_give, update_msg)));
                CountThread Tread = new CountThread();
                Tread.start();
            }
        }
    }
    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent contentIntent;
        message = msg;
        if(!msg.contains("message")){
            contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_notification)
                            .setContentTitle("Tock Tock")
                            .setStyle(new NotificationCompat.BigTextStyle().bigText("설치가 완료되었습니다.")).setContentText("");
            mBuilder.setContentIntent(contentIntent);
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        }
        else{
            contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, Chatting_Activity.class), 0);
            String info[]= parsing_string(msg);
            Common.chatting_friend_name =new String(info[0]);
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_notification)
                            .setContentTitle("Tock Tock")
                            .setStyle(new NotificationCompat.BigTextStyle().bigText("보낸이 : "+info[0]+" 내용 : "+info[1])).setContentText("");
            mBuilder.setContentIntent(contentIntent);
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
            update_give=info[0];
            update_msg=info[1];
            get_generated_activity();
        }
    }
    public String[] parsing_string(String msg){
        String totalmsg=msg.substring(8,msg.length()-2);
        String info[]=new String[2];
        ContentValues values = new ContentValues();
        int[] index = new int[5];
        info[0] = "";
        info[1] = "";
        index[0] = totalmsg.indexOf("_from=");
        index[1] = totalmsg.indexOf(", android.support");
        index[2] = totalmsg.indexOf("_message=");
        index[3] = totalmsg.indexOf(", from=");
        index[4] = totalmsg.indexOf(", collapse_key=");

        int i, j, temp;

        for (i = 0; i < index.length - 1; i++) {
            for (j = 0; j < index.length - 1 - i; j++) {
                if (index[j] > index[j + 1]) {
                    temp = index[j];
                    index[j] = index[j + 1];
                    index[j + 1] = temp;
                }
            }
        }
        for (i = 0; i < index.length; i++) {
            if(totalmsg.indexOf("_from=")==index[i]){
                if(i==0){
                    info[0] = totalmsg.substring(6,index[1]);
                }
                else if(i==4){
                    info[0] = totalmsg.substring(index[4]+6);
                }
                else{
                    info[0] = totalmsg.substring(index[i]+6, index[i+1]);
                }
            }
            if(totalmsg.indexOf("_message=")==index[i]){
                if(i==0){
                    info[1] = totalmsg.substring(9,index[1]);
                }
                else if(i==4){
                    info[1] = totalmsg.substring(index[4]+9);
                }
                else{
                    info[1] = totalmsg.substring(index[i]+9, index[i+1]);
                }
            }
        }
        values.put(DataProvider.COL_TAKE, Common.getMyemail());
        values.put(DataProvider.COL_GIVE, info[0]);  //from
        values.put(DataProvider.COL_MSG, info[1]); //message
        Log.d("저장", msg);
        Log.d("저장",info[0]+info[1]);
        getContentResolver().insert(DataProvider.CONTENT_URI_MESSAGES,values);

        return info;
    }
}
class CountThread extends Thread {
    public void run() {
        Chatting_Activity.mHandler.post(new Runnable() {
            @Override
            public void run() {
                Chatting_Activity.adapter.notifyDataSetChanged();
            }
        });
    }
}