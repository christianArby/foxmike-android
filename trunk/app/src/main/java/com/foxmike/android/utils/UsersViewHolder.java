package com.foxmike.android.utils;
// Checked
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.foxmike.android.R;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by chris on 2018-01-03.
 */

public class UsersViewHolder extends RecyclerView.ViewHolder {

    public View mView;

    public UsersViewHolder(View itemView) {
        super(itemView);

        mView = itemView;
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
