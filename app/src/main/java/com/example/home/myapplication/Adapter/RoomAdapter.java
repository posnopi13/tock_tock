package com.example.home.myapplication.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.home.myapplication.Chatting_Activity;
import com.example.home.myapplication.Data.FriendData;
import com.example.home.myapplication.Data.RoomData;
import com.example.home.myapplication.R;
import com.example.home.myapplication.Server.Common;

import java.util.List;

/**
 * Created by HOME on 2015-09-15.
 */
public class RoomAdapter extends ArrayAdapter<RoomData> {
    private LayoutInflater mInflater;

    public RoomAdapter(Context context, List<RoomData> values){
        super(context, R.layout.listitem_room, values);
        mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    //커스텀 리스트뷰를 구현하는 부분
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder_Area holder;
        if (convertView == null) {
            holder = new Holder_Area();
            convertView = mInflater.inflate(R.layout.listitem_room, parent, false);
            holder.text = (TextView) convertView.findViewById(R.id.room_text);
            holder.email = (TextView) convertView.findViewById(R.id.room_email);
            convertView.setTag(holder);
        }
        else {
            holder = (Holder_Area) convertView.getTag();
        }

        // Populate the text
        holder.email.setText(getItem(position).getEmail());
        holder.text.setText(getItem(position).getText());

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Common.chatting_friend_name = ((TextView)v.findViewById(R.id.room_email)).getText().toString();
                Intent intent = new Intent(getContext(), Chatting_Activity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(intent);
            }
        });


        return convertView;
    }
    //static으로 생성
    private static class Holder_Area {
        public TextView text;
        public TextView email;
    }

}
