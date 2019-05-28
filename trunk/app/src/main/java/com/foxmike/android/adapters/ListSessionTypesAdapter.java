package com.foxmike.android.adapters;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.foxmike.android.R;
import com.foxmike.android.fragments.SortAndFilterFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by chris on 2019-04-03.
 */

public class ListSessionTypesAdapter extends RecyclerView.Adapter<ListSessionTypesAdapter.ListSessionTypesViewHolder>{

    private HashMap<String,String> sessionTypeDictionary;
    private HashMap<String,String> sessionTypeDictionaryReversed = new HashMap<>();
    private ArrayList<String> sessionTypeArray;
    private HashMap<String, Boolean> sessionTypeChosen = new HashMap<>();
    private HashMap<String, Drawable> sessionTypeDrawables = new HashMap<>();
    private HashMap<String, ColorStateList> checkedColors = new HashMap<>();
    private SortAndFilterFragment.OnFilterChangedListener onFilterChangedListener;

    public ListSessionTypesAdapter(HashMap<String,String> sessionTypeDictionary, HashMap<String, Boolean> sessionTypeChosen, HashMap<String, Drawable> sessionTypeDrawables, HashMap<String, ColorStateList> checkedColors, SortAndFilterFragment.OnFilterChangedListener onFilterChangedListener) {
        this.sessionTypeDictionary = sessionTypeDictionary;
        this.sessionTypeChosen = sessionTypeChosen;
        this.sessionTypeDrawables = sessionTypeDrawables;
        this.checkedColors = checkedColors;
        this.onFilterChangedListener = onFilterChangedListener;
        this.sessionTypeArray = new ArrayList<>(sessionTypeDictionary.values());
        for (String sessionTypeCode: sessionTypeDictionary.keySet()) {
            this.sessionTypeDictionaryReversed.put(sessionTypeDictionary.get(sessionTypeCode), sessionTypeCode);
        }
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
        listSessionTypesViewHolder.setTypeText(sessionTypeArray.get(i));

        String chosesTypeInCode = sessionTypeDictionaryReversed.get(sessionTypeArray.get(i));
        // set Chosen status
        setChosenStatus(chosesTypeInCode, listSessionTypesViewHolder);


        listSessionTypesViewHolder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String chosesTypeInCode = sessionTypeDictionaryReversed.get(sessionTypeArray.get(i));
                if (sessionTypeChosen.get(chosesTypeInCode)==null) {
                    sessionTypeChosen.put(chosesTypeInCode, true);
                    setChosenStatus(chosesTypeInCode, listSessionTypesViewHolder);
                    onFilterChangedListener.OnSessionTypeChanged(sessionTypeChosen);
                } else if (sessionTypeChosen.get(chosesTypeInCode)) {
                    sessionTypeChosen.put(chosesTypeInCode, false);
                    setChosenStatus(chosesTypeInCode, listSessionTypesViewHolder);
                    onFilterChangedListener.OnSessionTypeChanged(sessionTypeChosen);
                } else {
                    sessionTypeChosen.put(chosesTypeInCode, true);
                    setChosenStatus(chosesTypeInCode, listSessionTypesViewHolder);
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
        return sessionTypeDictionary.size();
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
