package com.foxmike.android.adapters;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.foxmike.android.R;
import com.foxmike.android.fragments.SortAndFilterFragment;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by chris on 2019-04-03.
 */

public class ListSessionTypesAdapter extends RecyclerView.Adapter<ListSessionTypesAdapter.ListSessionTypesViewHolder>{

    private ArrayList<String> sessionTypeArraylist;
    private HashMap<String, Boolean> sessionTypeChosen = new HashMap<>();
    private HashMap<String, Drawable> sessionTypeDrawables = new HashMap<>();
    private HashMap<String, ColorStateList> checkedColors = new HashMap<>();
    private SortAndFilterFragment.OnFilterChangedListener onFilterChangedListener;

    public ListSessionTypesAdapter(ArrayList<String> sessionTypeArraylist, HashMap<String, Boolean> sessionTypeChosen, HashMap<String, Drawable> sessionTypeDrawables, HashMap<String, ColorStateList> checkedColors, SortAndFilterFragment.OnFilterChangedListener onFilterChangedListener) {
        this.sessionTypeArraylist = sessionTypeArraylist;
        this.sessionTypeChosen = sessionTypeChosen;
        this.sessionTypeDrawables = sessionTypeDrawables;
        this.checkedColors = checkedColors;
        this.onFilterChangedListener = onFilterChangedListener;
    }

    @NonNull
    @Override
    public ListSessionTypesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view;
        view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.session_type_single_layout, viewGroup, false);
        return new ListSessionTypesAdapter.ListSessionTypesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListSessionTypesViewHolder listSessionTypesViewHolder, int i) {
        listSessionTypesViewHolder.setTypeText(sessionTypeArraylist.get(i));

        // set Chosen status
        setChosenStatus(sessionTypeArraylist.get(i), listSessionTypesViewHolder);


        listSessionTypesViewHolder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sessionTypeChosen.get(sessionTypeArraylist.get(i))==null) {
                    sessionTypeChosen.put(sessionTypeArraylist.get(i), true);
                    setChosenStatus(sessionTypeArraylist.get(i), listSessionTypesViewHolder);
                    onFilterChangedListener.OnSessionTypeChanged(sessionTypeChosen);
                } else if (sessionTypeChosen.get(sessionTypeArraylist.get(i))) {
                    sessionTypeChosen.put(sessionTypeArraylist.get(i), false);
                    setChosenStatus(sessionTypeArraylist.get(i), listSessionTypesViewHolder);
                    onFilterChangedListener.OnSessionTypeChanged(sessionTypeChosen);
                } else {
                    sessionTypeChosen.put(sessionTypeArraylist.get(i), true);
                    setChosenStatus(sessionTypeArraylist.get(i), listSessionTypesViewHolder);
                    onFilterChangedListener.OnSessionTypeChanged(sessionTypeChosen);
                }
            }
        });
    }

    private void setChosenStatus(String sessionType, ListSessionTypesViewHolder listSessionTypesViewHolder) {
        // If chosen map has key
        if (sessionTypeChosen.containsKey(sessionType)) {
            // if that key is true
            if (sessionTypeChosen.get(sessionType)) {
                listSessionTypesViewHolder.setTypeIcon(sessionTypeDrawables.get("checked"));
                listSessionTypesViewHolder.setTypeIconChecked(true);
            } else {
                // if that key is false
                listSessionTypesViewHolder.setTypeIconChecked(false);
                if (sessionTypeDrawables.containsKey(sessionType)) {
                    // if it has an icon
                    listSessionTypesViewHolder.setTypeIcon(sessionTypeDrawables.get(sessionType));
                } else {
                    listSessionTypesViewHolder.setTypeIcon(sessionTypeDrawables.get("default"));
                }
            }
            // If chosen map does not has key it is by default false
        } else {
            listSessionTypesViewHolder.setTypeIconChecked(false);
            // if it has an icon
            if (sessionTypeDrawables.containsKey(sessionType)) {
                listSessionTypesViewHolder.setTypeIcon(sessionTypeDrawables.get(sessionType));
            } else {
                listSessionTypesViewHolder.setTypeIcon(sessionTypeDrawables.get("default"));
            }
        }
    }

    @Override
    public int getItemCount() {
        return sessionTypeArraylist.size();
    }

    public class ListSessionTypesViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public ListSessionTypesViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setTypeText(String typeText) {
            TextView sessionTypeTV = mView.findViewById(R.id.sessionTypeTV);
            sessionTypeTV.setText(typeText);
        }

        public void setTypeIcon(Drawable typeDrawable) {
            FloatingActionButton sessionTypeFAB = mView.findViewById(R.id.sessionTypeFAB);
            sessionTypeFAB.setImageDrawable(typeDrawable);
        }

        public void setTypeIconChecked(Boolean isChecked) {
            FloatingActionButton sessionTypeFAB = mView.findViewById(R.id.sessionTypeFAB);
            if (isChecked) {
                sessionTypeFAB.setBackgroundTintList(checkedColors.get("isChecked"));
            } else {
                sessionTypeFAB.setBackgroundTintList(checkedColors.get("isNotChecked"));
            }

        }
    }
}
