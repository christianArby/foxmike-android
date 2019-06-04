package com.foxmike.android.utils;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.foxmike.android.R;

public class SmallHostAdvertisementViewHolder extends RecyclerView.ViewHolder {

    public ImageView sessionIV;
    public TextView text1TV;
    public TextView text2TV;
    public TextView text3TV;
    public TextView text4TV;
    public TextView cancelledFlag;


    public SmallHostAdvertisementViewHolder(View itemView) {
        super(itemView);
        sessionIV = (ImageView) itemView.findViewById(R.id.icon);
        text1TV = (TextView) itemView.findViewById(R.id.text1);
        text2TV = (TextView) itemView.findViewById(R.id.text2);
        text3TV = (TextView) itemView.findViewById(R.id.text3);
        text4TV = (TextView) itemView.findViewById(R.id.text4);
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

    public void setText4(String text4) {
        text4TV.setText(text4);
    }


    public void setCancelled(boolean cancelled, Context context) {
        if (cancelled) {
            cancelledFlag.setText(context.getResources().getString(R.string.cancelled));
            cancelledFlag.setPadding(16,8,16,8);
            /*LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, );
            cancelledFlag.setLayoutParams(params);*/

        } else {
            cancelledFlag.setText("");
            cancelledFlag.setPadding(0,0,0,0);

            /*LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
            cancelledFlag.setLayoutParams(params);*/
        }
    }


}
