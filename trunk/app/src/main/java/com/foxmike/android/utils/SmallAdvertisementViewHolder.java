package com.foxmike.android.utils;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.foxmike.android.R;

/**
 * Created by chris on 2018-09-22.
 */


public class SmallAdvertisementViewHolder extends RecyclerView.ViewHolder {

    public ImageView sessionIV;
    public TextView text1TV;
    public TextView text2TV;
    public TextView text3TV;
    public TextView cancelledFlag;


    public SmallAdvertisementViewHolder(View itemView) {
        super(itemView);
        sessionIV = (ImageView) itemView.findViewById(R.id.icon);
        text1TV = (TextView) itemView.findViewById(R.id.text1);
        text2TV = (TextView) itemView.findViewById(R.id.text2);
        text3TV = (TextView) itemView.findViewById(R.id.text3);
        cancelledFlag = (TextView) itemView.findViewById(R.id.cancelledFlag);
    }

    public void setSessionImage(String sessionImage, Context context) {
        Glide.with(context).load(sessionImage).into(sessionIV);
    }

    public void setText1(String text1) {
        text1TV.setText(text1);
    }

    public void setText2(String text2) {
        text2TV.setText(text2);
    }

    public void setText3(String text3) {
        text3TV.setText(text3);
    }


    public void setCancelled(boolean cancelled) {
        if (cancelled) {

        } else {
            cancelledFlag.setText("");
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
            cancelledFlag.setLayoutParams(params);
        }
    }
}