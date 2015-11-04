package com.example.home.myapplication.Server;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;

import com.example.home.myapplication.MainActivity;

/**
 * Created by HOME on 2015-09-02.
 */
public class Common extends Application{
    private static String ServerUrl = "http://aaa.cafe24app.com";
    public static final  String EMAIL = "email";//등록시 내 이메일 - 메세지보낼 때는 암묵적으로 간다.
    public static final  String REDID = "redID";//등록시 내 redID - 등록 때만 사용
    public static final  String _MESSAGE = "_message";//메세지 보낼 때 메세지 내용
    public static final  String _FROM = "_from";//메세지 보낼 때 내 이메일
    public static final  String _TO = "_to";//메세지 보낼 때 상대방 이메일
    public static String chatting_friend_name;
    public static String redID;

    private static SharedPreferences myinfo;

    public static String getServerUrl(){
        return ServerUrl;
    }
    public static String getMyemail(){
        return myinfo.getString(MainActivity.MYEMAIL,"");
    }
    @Override
    public void onCreate(){
        super.onCreate();
        myinfo = PreferenceManager.getDefaultSharedPreferences(this);
    }

}
