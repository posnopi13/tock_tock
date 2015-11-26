package com.example.home.myapplication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.example.home.myapplication.Adapter.GroupAdapter;
import com.example.home.myapplication.Adapter.RoomAdapter;
import com.example.home.myapplication.Data.FriendData;
import com.example.home.myapplication.Data.RoomData;
import com.example.home.myapplication.Server.Common;
import com.example.home.myapplication.Server.SeverUtilities;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class MainActivity extends ActionBarActivity {
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    // SharedPreferences에 저장할 때 key 값으로 사용됨
    public static final String PROPERTY_REG_ID = "registration_id";

    // SharedPreferences에 저장할 때 key 값으로 사용됨.
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final String TAG = "MainActivity";
    ProgressDialog dialog;
    //gcm 사용에 필요한 SENDER_ID
    String SENDER_ID = "96476306944";

    GoogleCloudMessaging gcm;
    Context context;
    String redID;

    private String name = null;
    public static final String MYNAME = "myname";
    public static final String MYEMAIL = "myemail";
    public static final String MYREDID = "redid";

    SharedPreferences myinfo;

    ListView listView1, listView2;
    GroupAdapter adapter_friendlist;
    RoomAdapter adapter_roomlist;
    ArrayList<FriendData> data;
    ArrayList<RoomData> chatting_data;
    ArrayList<String> items;
    EditText friend_mail,friend_name;
    ProgressDialog progressDialog;
    AutoCompleteTextView autocompletetextview;
    ImageButton ib_find;
    //ProgressThread progressThreda;
    public String delete_room;
    public String delete_friend;
    Uri URL_URL_FRIENDS = Uri.parse(DataProvider.URL_FRIENDS);
    Uri URL_MESSAGES = Uri.parse(DataProvider.URL_MESSAGES);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myinfo = PreferenceManager.getDefaultSharedPreferences(this);
        //StrictMode" on MainActivity Class
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        ib_find =(ImageButton)findViewById(R.id.ib_find);
        TabHost tabHost = (TabHost) findViewById(R.id.tabHost); //탭 인식
        listView1 = (ListView) findViewById(R.id.friend_list); // 친구목록 리스트1 인식
        listView2 = (ListView) findViewById(R.id.listView2); // 채팅리스트 리스트2 인식
        ib_find.setOnClickListener(new View.OnClickListener() {//autocomplete 선택시 넘어가는 버튼 설정
            @Override
            public void onClick(View v) {
                String name = ((AutoCompleteTextView)findViewById(R.id.autocomplete)).getText().toString();
                Log.d("button",name);
                Cursor c = getContentResolver().query(URL_URL_FRIENDS, null, "name=\"" + name + "\"", null, null);
                if (c.moveToFirst()) {
                    Log.d("button", c.getString(c.getColumnIndex(DataProvider.COL_EMAIL)));
                    c.moveToFirst();
                    Common.chatting_friend_name=new String(c.getString(c.getColumnIndex(DataProvider.COL_EMAIL)));
                    ((AutoCompleteTextView) findViewById(R.id.autocomplete)).setText("");
                    Intent intent = new Intent(getApplicationContext(), Chatting_Activity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        });
        //탭버튼 초기화
        tabHost.setup();
        //탭 1 친구목록 설정하는 소스
        TabHost.TabSpec tabSpec1 = tabHost.newTabSpec("FriendList");
        tabSpec1.setIndicator("친구목록");
        tabSpec1.setContent(R.id.tab1);
        tabHost.addTab(tabSpec1);

        //탭 2 채팅목록 설정하는 소스
        TabHost.TabSpec tabSpec2 = tabHost.newTabSpec("ChattingList");
        tabSpec2.setIndicator("채팅목록");
        tabSpec2.setContent(R.id.tab2);
        tabHost.addTab(tabSpec2);
        tabHost.setCurrentTab(0);//탭호스트의 처음 화면은 0번째 화면 즉 FriendList로 선책한다.
        tabHost.setOnTabChangedListener(new interface_tab() {
            @Override
            public void onTabChanged(String id) {
                switch (id) {
                    case "FriendList":
                        break;
                    case "ChattingList":
                        chatting_data.clear();
                        adapter_roomlist.notifyDataSetChanged();
                        ArrayList<String> friend_list = new ArrayList<>();
                        String[] projection = {"distinct " + DataProvider.COL_GIVE + ", " + DataProvider.COL_TAKE};
                        Cursor cc = getContentResolver().query(URL_MESSAGES, projection, null, null, null);
                        if (!cc.moveToFirst()) {
                            //추후 TextView로 교체예정
                            //만약 없다면 toast메세지 뛰운다.
                            Toast.makeText(getApplicationContext(), "no friends", Toast.LENGTH_SHORT);
                        } else {
                            HashMap<String, Integer> name_map = new HashMap();
                            do {
                                name_map.put(cc.getString(0), 1);
                                name_map.put(cc.getString(1), 1);
                            } while (cc.moveToNext());
                            name_map.remove(Common.getMyemail());
                            Iterator<String> name_entry = (name_map.keySet()).iterator();
                            while (name_entry.hasNext()) {
                                chatting_data.add(new RoomData(name_entry.next(), "채팅방"));
                            }
                        }
                        adapter_roomlist.notifyDataSetChanged();
                        break;
                }
            }
        });
        items = new ArrayList<>();
        autocompletetextview = (AutoCompleteTextView) findViewById(R.id.autocomplete);
        data = new ArrayList<>();
        chatting_data = new ArrayList<>();
        adapter_friendlist = new GroupAdapter(getApplicationContext(),data);
        adapter_roomlist = new RoomAdapter(getApplicationContext(),chatting_data);
        listView1.setAdapter(adapter_friendlist);
        listView2.setAdapter(adapter_roomlist);
        listView1.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            //길게 클릭될 경우는 삭제를 위한 용도로 사용될 것이다.
            //그렇기에 삭제를 확인할 대화상자를 열도록 한다.
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tv_friend = (TextView) parent.getChildAt(position).findViewById(R.id.friend_email);
                delete_friend =tv_friend.getText().toString()+tv_friend.getText().toString();
                Log.v(TAG,"LongClick");
                showDialog(1);
                //삭제를 하려면 지금 클릭된 아이템의 title과 date의 조합으로 delete_title을 설정한다.
                return true;
            }
        });

        listView2.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            //길게 클릭될 경우는 삭제를 위한 용도로 사용도리 것이다.
            //그렇기에 삭제를 확인할 대화상자를 열도록 한다.
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showDialog(2);
                return true;
            }
        });
        if (checkPlayServices()) {//gcm을 사용가능 한다면
            gcm = GoogleCloudMessaging.getInstance(this);
            redID = getRegistrationId(context);//버전과 redID를 반환 반환은 redID OR ""

            if (redID.isEmpty()) {//반환이 ""이라면
                registerInBackground();
            }
            //사용가능하다면 redID를 내정보파일에 저장
            SharedPreferences.Editor editor = myinfo.edit();
            editor.putString(this.MYREDID, redID);
            editor.commit();
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
        if (getemail() == "") {//아직 등록되지 않으면 등록한다.
            register_myinfo();
        }
        setMyinfo_text();//등록이 되었다면 내정보를 text란에 기입
    }
    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
        data.clear();
        items.clear();
        String URL_TAB1 = DataProvider.URL_FRIENDS;
        Uri friend_tab1 = Uri.parse(URL_TAB1);
        Cursor c = getContentResolver().query(friend_tab1, null, null, null, null);
        if(!c.moveToFirst()){
            Toast.makeText(this,"no friends",Toast.LENGTH_SHORT);
        }else{
            do{
                Log.d("Count_number", "abcd" + c.getString(c.getColumnIndex(DataProvider.COL_EMAIL)));
                items.add(new String(c.getString(c.getColumnIndex(DataProvider.COL_NAME))));
                data.add(new FriendData(c.getString(c.getColumnIndex(DataProvider.COL_NAME)), c.getString(c.getColumnIndex(DataProvider.COL_EMAIL))));
            }while(c.moveToNext());
        }
        autocompletetextview.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, items));
        adapter_friendlist.notifyDataSetChanged();
    }
    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Log.v(TAG,"id : "+id);
        switch(id){
            case 1://친구삭제시 - 메세지, 친구db에서 모두 삭제
                builder.setTitle("메모 확인 대화 상자")
                        .setMessage("해당 메모를 삭제하시겠습니까?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getContentResolver().delete(DataProvider.CONTENT_URI_FRIENDS, delete_friend ,null);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //getContentResolver().insert(DataProvider.CONTENT_URI_FRIENDS, values);
                            }
                        });
                break;
            case 2://메세지만 삭제
                break;
            case 3://나 등록
                break;
            case 4://친구 등록
                break;
        }
        AlertDialog alert = builder.create();
        return alert;
    }
    //getemail()은 기존에 내가 저장한 나의 email이 있으면 메일 값을 넘겨준다.
    //즉, 없다면 내 메일은 등록이 되지 않은 것이다.
    private String getemail() {
        //SharedPreferences pref = getSharedPreferences("myinfo", MODE_PRIVATE);
        return myinfo.getString(MYEMAIL, "");
    }

    private void setMyinfo_text() {
        //SharedPreferences myinfo = getSharedPreferences(MYINFO, MODE_PRIVATE);
        TextView text = (TextView) findViewById(R.id.mystoryText);
        text.setText(myinfo.getString(MYNAME, ""));
    }

    //*********************************************************************************************
    //내정보 setting 및 서버에 확인
    //*********************************************************************************************
    public void register_myinfo() {    // 맨처음 내정보 등록 다이어로그
        LayoutInflater inflater = getLayoutInflater();
        final View view_enroll = inflater.inflate(R.layout.dialogue_main, null);
        final EditText myname = (EditText) view_enroll.findViewById(R.id.dialogue_myname);
        final EditText myemail = (EditText) view_enroll.findViewById(R.id.dialogue_myemail);
        final AlertDialog.Builder buider = new AlertDialog.Builder(this); //AlertDialog.Builder 객체 생성

        buider.setTitle("등록하시겠습니까?"); //Dialog 제목
        buider.setView(view_enroll); //위에서 inflater가 만든 dialogView 객체 세팅 (Customize)
        buider.setPositiveButton("등록", new DialogInterface.OnClickListener() {
            //Dialog에 "Complite"라는 타이틀의 버튼을 설정
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (myname.getText().length() >= 3 && myemail.getText().length() >= 3) {
                    String name = myname.getText().toString();//사용자이름
                    String email = myemail.getText().toString();//사용자id
                    //서버에 등록
                    register(email, redID, name);
                    //액티비티상에 내정보 등록 & 어플리케이션에 내정보 저장
                } else {
                    Toast.makeText(MainActivity.this, "NAME : 3글자이상\nE-MAIL : 3글자이상 입력하시오.", Toast.LENGTH_LONG).show();
                    register_myinfo();
                }
            }
        });
        buider.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        buider.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        buider.show();
    }
    //*********************************************************************************************
    //친구등록 다이어로그
    //*********************************************************************************************
    private void register_friend() {
        LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout linearLayout = (LinearLayout) vi.inflate(R.layout.dialog_add_friend, null);
        friend_mail = (EditText) linearLayout.findViewById(R.id.dialogue_friend_email);
        friend_name = (EditText) linearLayout.findViewById(R.id.dialogue_friend_name);
        final AlertDialog.Builder buider = new AlertDialog.Builder(this); //AlertDialog.Builder 객체 생성
        buider.setTitle("추가할 친구를 입력하십시오."); //Dialog 제목
        buider.setView(linearLayout); //위에서 inflater가 만든 dialogView 객체 세팅 (Customize)
        buider.setPositiveButton("등록", new DialogInterface.OnClickListener() {
            //Dialog에 "Complite"라는 타이틀의 버튼을 설정
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email = friend_mail.getText().toString();
                String URL = DataProvider.URL_FRIENDS;
                Uri friends = Uri.parse(URL);
                Cursor c = getContentResolver().query(friends, null, "email=\"" + email + "\"", null, null);
                if (!c.moveToFirst()) {// 이친구가 없다면 등록을 합니다.
                    check(email);
                } else {
                    Toast.makeText(MainActivity.this, "이미 추가 되어 있습니다.", Toast.LENGTH_LONG).show();
                }
            }
        });
        buider.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        buider.show();
    }
    //*********************************************************************************************
    //친구추가 작업 : DB작업 & ui조작
    //*********************************************************************************************
    private void check(final String email) {

        new AsyncTask<Void, Void, String>() {
            @Override
            protected void onPreExecute() {//Background 작업 시작전에 프로그레스바 시작
                super.onPreExecute();
                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setTitle("Lodaing....");
                progressDialog.setMessage("Please wait....");
                progressDialog.setCancelable(false);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
            }
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                Log.i("MainActivity","is working");
                msg = SeverUtilities.check(email);
                return msg;
            }
            @Override
            protected void onPostExecute(String msg) {
                try{
                    if (!TextUtils.isEmpty(msg) && msg.equals("200")) {
                        Toast.makeText(getApplicationContext(), "새로운 친구를 등록하였습니다.", Toast.LENGTH_LONG).show();
                        register_friend_db();//DB에 친구정보 저장
                    }
                    else if(msg.equals("300")){
                        Toast.makeText(getApplicationContext(), "존재하지 않는 친구입니다.", Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(getApplicationContext(), "서버로부터 응답이 없습니다.", Toast.LENGTH_LONG).show();
                    }
                    progressDialog.cancel();
                }catch (Exception e){

                }

            }
        }.execute(null, null, null);

    }
    private void register_friend_db(){
        String email = friend_mail.getText().toString();
        String name = friend_name.getText().toString();
        ContentValues values = new ContentValues();
        values.put(DataProvider.COL_NAME, name);
        values.put(DataProvider.COL_EMAIL, email); // values 라는 박스에  key1, value1 을 넣음.
        getContentResolver().insert(DataProvider.CONTENT_URI_FRIENDS, values); //CONTENT_URI_FRIENDS 테이블에 values값을 넣기.
        onResume();
    }
    //*********************************************************************************************
    //서버에 요청
    //*********************************************************************************************

    private void register(final String email,final String redID, final String name) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected void onPreExecute() {//Background 작업 시작전에 프로그레스바 시작
                super.onPreExecute();
                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setTitle("Lodaing....");
                progressDialog.setMessage("Please wait....");
                progressDialog.setCancelable(false);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                //프로그레스바 여기서 시작
            }
            @Override
            protected String doInBackground(Void... params) {//Background로 서버에 작업요청 && 테스트를 위한 DB에 내이름 넣기
                String msg = "";
                Log.i("MainActivity","is working");
                //서버에 등록요청 및 return 값은 msg로 받는다.
                msg = SeverUtilities.register(email, redID);
                //DB에 등록한다  | 체크를 위해서 ##################################################################################
                ContentValues values = new ContentValues();
                values.put(DataProvider.COL_NAME,name);
                values.put(DataProvider.COL_EMAIL,email); // values 라는 박스에  key1, value1 을 넣음.
                getContentResolver().insert(DataProvider.CONTENT_URI_FRIENDS, values);
                return msg;
            }
            @Override
            protected void onPostExecute(String msg) {//작업이 종료된 뒤
                try{
                    if (!TextUtils.isEmpty(msg) && msg.equals("200")) {//성공적으로 등록을 했다면
                        Toast.makeText(getApplicationContext(), "성공적으로 등록되었습니다.", Toast.LENGTH_SHORT).show();
                        //공유문서에 내 정보 저장합니다.
                        SharedPreferences.Editor editor = myinfo.edit();
                        editor.putString(MYNAME, name);
                        editor.putString(MYEMAIL, email);
                        editor.commit();
                        setMyinfo_text();
                        //프로그레스 멈추기
                    }//실패했다면
                    else{
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                        register_myinfo();
                    }
                    progressDialog.cancel();
                }catch(Exception e){

                }

            }
        }.execute(null,null,null);

    }

    private void unregister(final String email) {

        new AsyncTask<Void, Void, String>() {
            @Override
            protected void onPreExecute() {//Background 작업 시작전에 프로그레스바 시작
                super.onPreExecute();
                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setTitle("Lodaing....");
                progressDialog.setMessage("Please wait....");
                progressDialog.setCancelable(false);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                //프로그레스바 여기서 시작
            }

            @Override
            protected String doInBackground(Void... params) {
                //StrictMode" on MainActivity Class
                String msg = "";
                Log.i("MainActivity","is working");
                msg = SeverUtilities.unregister(email);
                return msg;
            }
            @Override
            protected void onPostExecute(String msg) {
                try{
                    if (!TextUtils.isEmpty(msg)) {
                        Toast.makeText(getApplicationContext(), "성공적으로 삭제되었습니다.", Toast.LENGTH_LONG).show();
                        progressDialog.cancel();
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "서버로부터 응답이 없습니다.", Toast.LENGTH_LONG).show();
                    }
                }catch(Exception e){

                }
            }
        }.execute(null, null, null);

    }
    //*********************************************************************************************
    //gcm등록
    //*********************************************************************************************
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    redID = gcm.register(SENDER_ID);
                    msg = redID;
                    SharedPreferences.Editor editor = myinfo.edit();
                    editor.putString(MYREDID, msg);
                    editor.commit();
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();

                }
                return msg;
            }
            @Override
            protected void onPostExecute(String msg) {
            }
        }.execute(null, null, null);
    }

    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // 앱이 업데이트 되었는지 확인하고, 업데이트 되었다면 기존 등록 아이디를 제거한다.
        // 새로운 버전에서도 기존 등록 아이디가 정상적으로 동작하는지를 보장할 수 없기 때문이다.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    private SharedPreferences getGCMPreferences(Context context) {
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }



    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {//실패한다면
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {//복구가능한 에러라면
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i("ICELANCER", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
    //*********************************************************************************************
    //메뉴 설정
    //*********************************************************************************************
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.friendadd) {
            register_friend();
        }
        else if(id == R.id.unregister) {
            String email = myinfo.getString(this.MYEMAIL, "default value");
            SeverUtilities.unregister(email);
            Toast.makeText(MainActivity.this,"서버제거 : " +  email + "\n" + name + "\n" + redID , Toast.LENGTH_LONG).show();
            unregister(email);
        }
        return super.onOptionsItemSelected(item);
    }

}
