package com.foxmike.android.utils;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.foxmike.android.R;
import com.foxmike.android.interfaces.SessionDateAndTimeClickedListener;

/**
 * Created by chris on 2018-09-16.
 */

public class SessionDateAndTimeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView sessionDateAndTimeText;
    public TextView participantsTV;

    SessionDateAndTimeClickedListener sessionDateAndTimeClickedListener;

    public void setSessionDateAndTimeClickedListener(SessionDateAndTimeClickedListener sessionDateAndTimeClickedListener) {
        this.sessionDateAndTimeClickedListener = sessionDateAndTimeClickedListener;
    }

    public SessionDateAndTimeViewHolder(View itemView) {
        super(itemView);
        sessionDateAndTimeText = (TextView) itemView.findViewById(R.id.sessionDateAndTimeTV);
        participantsTV = (TextView) itemView.findViewById(R.id.participantsTV);
        itemView.setOnClickListener(this);
    }

    public void setParticipantsTV(String numberOfParticipantsText) {
        participantsTV.setText(numberOfParticipantsText);
    }

    @Override
    public void onClick(View view) {
        sessionDateAndTimeClickedListener.OnSessionDateAndTimeClicked(view, getAdapterPosition());
    }

    public void setSelected(int textColor) {
        sessionDateAndTimeText.setTextColor(textColor);
    }
}
