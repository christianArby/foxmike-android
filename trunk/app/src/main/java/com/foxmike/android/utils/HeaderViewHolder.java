package com.foxmike.android.utils;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.foxmike.android.R;

/**
 * Created by chris on 2018-04-01.
 */

public class HeaderViewHolder extends RecyclerView.ViewHolder {
    public View mView;

    public HeaderViewHolder(View itemView) {
        super(itemView);

        mView = itemView;
    }

    public void setHeading(String letter) {
        TextView firstLetterHeading = (TextView) mView.findViewById(R.id.firstLetterTV);
        firstLetterHeading.setText(letter);
    }

    public void setVisibilityGone() {
        TextView firstLetterHeading = (TextView) mView.findViewById(R.id.firstLetterTV);
        firstLetterHeading.setVisibility(View.GONE);
    }
}
