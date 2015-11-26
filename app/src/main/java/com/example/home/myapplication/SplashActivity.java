package com.example.home.myapplication;


import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class SplashActivity extends Activity {
	static SplashActivity singleton;

	public static SplashActivity getInstance() {
		return singleton;
	}
	protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			singleton = this;
			//notification 없애기
			NotificationManager mNotificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.cancel(GcmIntentService.NOTIFICATION_ID);

			setContentView(R.layout.activity_splash);
			initialize();

		}

	
	private void initialize() {
		Handler handler = new Handler() {

			public void handleMessage(Message msg) {
				finish();

				Intent i = new Intent(getApplicationContext(), MainActivity.class);
			    startActivity(i);
				overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
			}
		};
		handler.sendEmptyMessageDelayed(0, 1000);
	}

}
