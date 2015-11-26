package com.example.home.myapplication;

import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.home.myapplication.Adapter.ChattingAdapter;
import com.example.home.myapplication.Data.ChattingData;
import com.example.home.myapplication.Server.Common;
import com.example.home.myapplication.Server.SeverUtilities;
import java.io.IOException;
import java.util.ArrayList;
import android.os.Handler;

//친구추가시 서버db에 그 아이디가 있다는 가정하에 서버 db에 친구아이디 물어봤고 그 아이디 있다는걸 서버db가 ok했다.
//그래서 내 디바이스는 그 아이디를 sqlite에 저장했고 내 디바이스는 sqlite에서 그 아이디에 해당하는 정보를 ui에 뿌렷다.

//내가 메세지를 send하면 그 메세지를 먼저 sqlite에 저장하고 서버 db에 보낸다. 그때 서버db에 잘 저장됬으면 내 디바이스 ui에 메세지 옆에 1이뜨고
//실패됬으면 메세지 옆에 x와 재전송버튼이 붙는다.

//gcm 합치기
public class Chatting_Activity extends ActionBarActivity {
    static Handler mHandler = new Handler();
    ListView listView;
    static ChattingAdapter adapter;
    static ArrayList<ChattingData> chatting_list;
    EditText send_Text;
    Button send_button;
    //채팅창에 있을 때 메세지가 온 경우 메세지를 개신한다.

    //액티비티를 일정 시간 동안 계속 갱싱신을 하기 위한 쓰레드 클래스
    CountThread thread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting);
        listView = (ListView)findViewById(R.id.chatting_list);
        chatting_list = new ArrayList<>();
        send_Text = (EditText)findViewById(R.id.send_text);
        send_button = (Button)findViewById(R.id.send_button);

        listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

        NotificationManager mNotificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(GcmIntentService.NOTIFICATION_ID);//Activity_Play가 실행되면 클릭을하든 클릭하지 않든 notification은 사라진다.

        //db에서 상대와의 메세지 내용 가저오는 cursor
        Cursor c = getContentResolver().query(DataProvider.CONTENT_URI_MESSAGES, new String[] {DataProvider.COL_GIVE, DataProvider.COL_MSG},
                new String("("+DataProvider.COL_GIVE + " = " + "'"+Common.chatting_friend_name+"' "
                        +"and "+DataProvider.COL_TAKE + " = "+"'"+Common.getMyemail()+"')"
                            +" or ("+DataProvider.COL_TAKE+" = "+"'"+Common.chatting_friend_name+"' "+"and "+
                        DataProvider.COL_GIVE + " = " + "'"+Common.getMyemail()+"') ") , null, null);
        //화면에 메세지를 뛰우는 과정 리스트자료구조에 메세지를 다 넣는다.
        //앞으로 포커스를 맨 아래로 하는 과정이 필요하다.
        if(c.moveToFirst()) {
            do{
                chatting_list.add(new ChattingData(c.getString(0), c.getString(1)));
            } while (c.moveToNext());
        }
        adapter = new ChattingAdapter(this, chatting_list);
        listView.setAdapter(adapter);
        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String take = Common.chatting_friend_name;
                String message = send_Text.getText().toString();
                //서버로 보내기
                send(message, take);
                chatting_list.add(new ChattingData(Common.getMyemail(), message));
                //어댑터가 바뀐것을 알려준다.
                adapter.notifyDataSetChanged();
                //채팅을 입력하는 곳을 변경
                send_Text.setText("");
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
    }
    private void send(final String message,final String take) {

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                Log.i("MainActivity","is working");
                try {
                    msg = SeverUtilities.send(message, take);
                    Log.i("MainActivity","message :"+message+"take : "+take);
                    return msg;
                }
                catch (IOException e) {
                    Log.e("ServerUtilities",e.toString());
                }
                return null;
            }
            @Override
            protected void onPostExecute(String msg) {
                if (!TextUtils.isEmpty(msg)) {
                    Toast.makeText(getApplicationContext(), "성공 : " + msg, Toast.LENGTH_LONG).show();
                    ContentValues values = new ContentValues();
                    values.put(DataProvider.COL_GIVE, Common.getMyemail());
                    values.put(DataProvider.COL_TAKE, take);
                    values.put(DataProvider.COL_MSG, message);
                    getContentResolver().insert(DataProvider.CONTENT_URI_MESSAGES, values);
                    Log.i("MainActivity", "성공적으로 도착"+msg);
                    //사용자 UI에 보여주기//추후 바뀔수 있다.
                }
                else{
                    Toast.makeText(getApplicationContext(), "실패"+msg, Toast.LENGTH_LONG).show();
                    chatting_list.remove(chatting_list.size() - 1);
                    //어댑터가 바뀐것을 알려준다.
                    adapter.notifyDataSetChanged();
                    Log.i("MainActivity", "실패적으로 도착 : "+msg);
                }
            }
        }.execute(null, null, null);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
                return super.onOptionsItemSelected(item);
    }

}
