package com.foxmike.android.utils;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.foxmike.android.R;

/**
 * Created by chris on 2018-09-23.
 */

public class ListSmallSessionsViewHolder extends RecyclerView.ViewHolder {

    View mView;

    public ListSmallSessionsViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
    }

    public void setSessionImage(String sessionImage, Context context) {
        ImageView sessionIV = (ImageView) mView.findViewById(R.id.session_image);
        Glide.with(context).load(sessionImage).into(sessionIV);
    }

    public void setText0(String text0) {
        TextView text0TV = (TextView) mView.findViewById(R.id.text0);
        text0TV.setText(text0);
    }

    public void setText1(String text1) {
        TextView text1TV = (TextView) mView.findViewById(R.id.text1);
        text1TV.setText(text1);
    }

    public void setText2(long timestamp, Context context) {
        TextView text2TV = (TextView) mView.findViewById(R.id.text2);
        if (timestamp==0) {
            text2TV.setText(context.getString(R.string.no_advertisements));
        } else {
            String sessionDateAndTime = TextTimestamp.textSessionDateAndTime(timestamp);
            sessionDateAndTime = sessionDateAndTime.substring(0,1).toUpperCase() + sessionDateAndTime.substring(1);
            text2TV.setText(context.getString(R.string.next_session) + sessionDateAndTime);
        }

    }

    public void setText3(String text3) {
        TextView text3TV = (TextView) mView.findViewById(R.id.text3);
        text3TV.setText(text3);
    }


}
