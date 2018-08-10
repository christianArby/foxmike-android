package com.foxmike.android.adapters;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.foxmike.android.R;
import com.foxmike.android.interfaces.OnSessionClickedListener;
import com.foxmike.android.models.Session;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by chris on 2018-08-08.
 */

public class ListSmallSessionsHorizontalAdapter extends RecyclerView.Adapter<ListSmallSessionsHorizontalAdapter.ListSmallSessionsHorizontalViewholder> {

    private ArrayList<Session> sessionArrayList;
    private Context context;
    private OnSessionClickedListener onSessionClickedListener;


    public ListSmallSessionsHorizontalAdapter(ArrayList<Session> sessionArrayList, Context context, OnSessionClickedListener onSessionClickedListener) {
        this.sessionArrayList = sessionArrayList;
        this.context = context;
        this.onSessionClickedListener = onSessionClickedListener;
    }


    @NonNull
    @Override
    public ListSmallSessionsHorizontalAdapter.ListSmallSessionsHorizontalViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.session_horizontal_layout, parent, false);
        return new ListSmallSessionsHorizontalAdapter.ListSmallSessionsHorizontalViewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListSmallSessionsHorizontalAdapter.ListSmallSessionsHorizontalViewholder holder, int position) {

        String sessionName = sessionArrayList.get(position).getSessionName();
        holder.setSessionName(sessionName);
    }

    @Override
    public int getItemCount() {
        return sessionArrayList.size();
    }

    public class ListSmallSessionsHorizontalViewholder extends RecyclerView.ViewHolder {

        View mView;

        public void setSessionClickedListener(final String sessionId) {
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onSessionClickedListener.OnSessionClicked(sessionId);
                }
            });
        }

        public ListSmallSessionsHorizontalViewholder(View itemView) {
            super(itemView);
            mView = itemView;



        }

        public void setSessionName(String sessionName) {
            TextView sessionNameTV = (TextView) mView.findViewById(R.id.sessionName);
            sessionNameTV.setText(sessionName);
        }
    }

    public void refreshData(ArrayList<Session> sessions) {
        this.sessionArrayList = sessions;
        this.notifyDataSetChanged();
    }

}


