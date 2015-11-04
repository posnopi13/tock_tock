package com.example.home.myapplication.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.home.myapplication.Data.ChattingData;
import com.example.home.myapplication.R;
import com.example.home.myapplication.Server.Common;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HOME on 2015-09-13.
 */
public class ChattingAdapter extends ArrayAdapter<ChattingData> {
    private LayoutInflater mInflater;
    ArrayList<ChattingData> values;
    public ChattingAdapter(Context context, ArrayList<ChattingData> values) {
        super(context, R.layout.listitem_chatting, values);
        mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        //if(convertView==null) {
            holder = new Holder();
            convertView = mInflater.inflate(R.layout.listitem_chatting, parent, false);
            holder.chatting_give = (RelativeLayout) convertView.findViewById(R.id.give_chatting);
            holder.chatting_take = (RelativeLayout) convertView.findViewById(R.id.take_chatting);

            if ( (values.get(position).getName()).equals(Common.getMyemail()) ) {
                //내가 보낸거
                holder.chatting_give.setVisibility(View.VISIBLE);
                holder.chatting_take.setVisibility(View.GONE);

                holder.take = (TextView) convertView.findViewById(R.id.give_chatting_name);
                holder.msg = (TextView) convertView.findViewById(R.id.give_chatting_message);

                holder.take.setText(Common.getMyemail());
            } else {
                holder.chatting_give.setVisibility(View.GONE);
                holder.chatting_take.setVisibility(View.VISIBLE);

                holder.take = (TextView) convertView.findViewById(R.id.take_chatting_name);
                holder.msg = (TextView) convertView.findViewById(R.id.take_chatting_message);

                holder.take.setText(values.get(position).getName());
            }
            convertView.setTag(holder);
        //}
        //else{
           // holder = (Holder) convertView.getTag();
        //}

        holder.msg.setText(values.get(position).getMessage());


        return convertView;
    }
   // public void setterPo(int po){}

    private class Holder {
        public RelativeLayout chatting_give;
        public RelativeLayout chatting_take;
        public TextView take;
        public TextView msg;
        public TextView user;

        public Holder(){
            chatting_take=null;
            chatting_give=null;
            take=null;
            msg=null;
            user=null;
        }
    }

}

