package com.foxmike.android.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.foxmike.android.R;
import com.foxmike.android.interfaces.OnSessionClickedListener;
import com.foxmike.android.models.Session;

import java.util.ArrayList;

/**
 * Created by chris on 2018-08-09.
 */


public class ListSmallRecyclerViewsAdapter extends RecyclerView.Adapter<ListSmallRecyclerViewsAdapter.ListSmallRecyclerViewsViewholder> {

    private Context context;
    private OnSessionClickedListener onSessionClickedListener;
    private ArrayList<ArrayList<Session>> sessionArrayArrayList;


    public ListSmallRecyclerViewsAdapter(ArrayList<ArrayList<Session>> sessionArrayArrayList, Context context, OnSessionClickedListener onSessionClickedListener) {
        this.sessionArrayArrayList = sessionArrayArrayList;
        this.context = context;
        this.onSessionClickedListener = onSessionClickedListener;
    }


    @NonNull
    @Override
    public ListSmallRecyclerViewsAdapter.ListSmallRecyclerViewsViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        if (viewType==1) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.session_horizontal_recyclerview_for_one, parent, false);
            return new ListSmallRecyclerViewsAdapter.ListSmallRecyclerViewsViewholder(view);
        }
        if (viewType==2) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.session_horizontal_recyclerview_for_two, parent, false);
            return new ListSmallRecyclerViewsAdapter.ListSmallRecyclerViewsViewholder(view);
        }
        if (viewType==3) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.session_horizontal_recyclerview_for_three, parent, false);
            return new ListSmallRecyclerViewsAdapter.ListSmallRecyclerViewsViewholder(view);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.session_horizontal_recyclerview_for_three, parent, false);
            return new ListSmallRecyclerViewsAdapter.ListSmallRecyclerViewsViewholder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ListSmallRecyclerViewsAdapter.ListSmallRecyclerViewsViewholder holder, int position) {

        holder.setVerticalRecyclerView(sessionArrayArrayList.get(position));
    }

    @Override
    public int getItemViewType(int position) {
        if (sessionArrayArrayList.get(position).size() == 1) {
            return 1;
        }
        if (sessionArrayArrayList.get(position).size() == 2) {
            return 2;
        }
        if (sessionArrayArrayList.get(position).size() == 3) {
            return 3;
        }
        if (sessionArrayArrayList.get(position).size() > 3) {
            return 3;
        }
        return 0;
    }

    @Override
    public int getItemCount() {
        return sessionArrayArrayList.size();
    }

    public class ListSmallRecyclerViewsViewholder extends RecyclerView.ViewHolder {

        View mView;

        public ListSmallRecyclerViewsViewholder(View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setVerticalRecyclerView(ArrayList<Session> sessionArrayList) {

            RecyclerView mSessionList = (RecyclerView) mView.findViewById(R.id.verticalRecyclerView);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
            mSessionList.setLayoutManager(linearLayoutManager);

            ListSmallSessionsHorizontalAdapter listSmallSessionsHorizontalAdapter = new ListSmallSessionsHorizontalAdapter(sessionArrayList, context, onSessionClickedListener);
            mSessionList.setAdapter(listSmallSessionsHorizontalAdapter);

        }

    }

    public void refreshData(ArrayList<ArrayList<Session>> sessionArrayArrayList) {
        this.sessionArrayArrayList = sessionArrayArrayList;
        this.notifyDataSetChanged();
    }



}
