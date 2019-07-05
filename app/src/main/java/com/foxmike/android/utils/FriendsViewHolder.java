package com.foxmike.android.utils;

import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.foxmike.android.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsViewHolder extends RecyclerView.ViewHolder {

    public View mView;

    public FriendsViewHolder(View itemView) {
        super(itemView);

        mView = itemView;
    }

    public void setFirstLetter(String letter) {
        TextView firstLetterTV = (TextView) mView.findViewById(R.id.firstLetterTV);
        if (letter.equals("no")) {
            firstLetterTV.setVisibility(View.GONE);
        } else {
            firstLetterTV.setVisibility(View.VISIBLE);
            firstLetterTV.setText(letter);
        }
    }

    public void setHeading(String heading) {
        TextView headingTV = (TextView) mView.findViewById(R.id.user_single_name);
        headingTV.setText(heading);
    }

    public void setText(String text, Boolean normal) {
        TextView userStatusView = (TextView) mView.findViewById(R.id.user_single_status);
        userStatusView.setText(text);

        if (!normal) {
            userStatusView.setTypeface(null, Typeface.BOLD);
        } else {
            userStatusView.setTypeface(null, Typeface.NORMAL);
        }
    }

    public void setUserImage(String thumb_image, android.content.Context context) {
        CircleImageView userProfileImageIV = (CircleImageView) mView.findViewById(R.id.user_single_image);
        Glide.with(context).load(thumb_image).into(userProfileImageIV);
    }

    public void setOnlineIcon(boolean userOnlineStatus) {

        ImageView userOnlineView = (ImageView) mView.findViewById(R.id.user_single_online_iconIV);

        if (userOnlineStatus) {
            userOnlineView.setVisibility(View.VISIBLE);
        } else {
            userOnlineView.setVisibility(View.INVISIBLE);
        }

    }
}
