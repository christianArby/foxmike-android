package com.foxmike.android.utils;

import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.foxmike.android.R;
import com.foxmike.android.interfaces.AdvertisementRowClickedListener;

/**
 * Created by chris on 2018-09-16.
 */

public class AdvertisementRowViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView advertisementRowDateText;
    public TextView advertisementRowTimeText;
    public TextView participantsTV;
    private AppCompatTextView bookedFlag;
    private AppCompatTextView fullyBookedFlag;
    private TextView participantsLeftTV;

    AdvertisementRowClickedListener advertisementRowClickedListener;

    public void setAdvertisementRowClickedListener(AdvertisementRowClickedListener advertisementRowClickedListener) {
        this.advertisementRowClickedListener = advertisementRowClickedListener;
    }

    public AdvertisementRowViewHolder(View itemView) {
        super(itemView);
        advertisementRowDateText = (TextView) itemView.findViewById(R.id.sessionDateTV);
        advertisementRowTimeText = (TextView) itemView.findViewById(R.id.timeTV);
        participantsTV = (TextView) itemView.findViewById(R.id.participantsTV);
        bookedFlag = (AppCompatTextView) itemView.findViewById(R.id.bookedFlag);
        fullyBookedFlag = (AppCompatTextView) itemView.findViewById(R.id.fullyBookedFlag);
        participantsLeftTV = (TextView) itemView.findViewById(R.id.participantsLeftTV);

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

    public void setParticipantsLeftTV(String numberOfParticipantsLeftText) {
        participantsLeftTV.setText(numberOfParticipantsLeftText);
    }

    public void toggleBooked(boolean booked) {
        if (booked) {
            bookedFlag.setVisibility(View.VISIBLE);
        } else {
            bookedFlag.setVisibility(View.GONE);
        }
    }

    public void toggleFullyBooked(boolean fullyBooked) {
        if (fullyBooked) {
            fullyBookedFlag.setVisibility(View.VISIBLE);
        } else {
            fullyBookedFlag.setVisibility(View.GONE);
        }
    }

    public void toggleParticipantsTV(boolean show) {
        if (show){
            participantsTV.setVisibility(View.VISIBLE);
        } else {
            participantsTV.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        advertisementRowClickedListener.OnAdvertisementRowClicked(view, getAdapterPosition());
    }

    public void toggleParticipantsLeftTV(boolean show) {
        if (show) {
            participantsLeftTV.setVisibility(View.VISIBLE);
        } else {
            participantsLeftTV.setVisibility(View.GONE);
        }

    }

}
