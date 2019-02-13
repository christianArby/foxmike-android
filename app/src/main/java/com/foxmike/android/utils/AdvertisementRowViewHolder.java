package com.foxmike.android.utils;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.foxmike.android.R;
import com.foxmike.android.interfaces.AdvertisementRowClickedListener;

/**
 * Created by chris on 2018-09-16.
 */

public class AdvertisementRowViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView advertisementRowDateAndTimeText;
    public TextView participantsTV;

    AdvertisementRowClickedListener advertisementRowClickedListener;

    public void setAdvertisementRowClickedListener(AdvertisementRowClickedListener advertisementRowClickedListener) {
        this.advertisementRowClickedListener = advertisementRowClickedListener;
    }

    public AdvertisementRowViewHolder(View itemView) {
        super(itemView);
        advertisementRowDateAndTimeText = (TextView) itemView.findViewById(R.id.sessionDateAndTimeTV);
        participantsTV = (TextView) itemView.findViewById(R.id.participantsTV);
        itemView.setOnClickListener(this);
    }

    public void setParticipantsTV(String numberOfParticipantsText) {
        participantsTV.setText(numberOfParticipantsText);
        participantsTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                advertisementRowClickedListener.OnParticipantsClicked(getAdapterPosition());
            }
        });
    }

    @Override
    public void onClick(View view) {
        advertisementRowClickedListener.OnAdvertisementRowClicked(view, getAdapterPosition());
    }

    public void setSelected(int textColor) {
        advertisementRowDateAndTimeText.setTextColor(textColor);
    }
}
