package com.foxmike.android.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.foxmike.android.R;
import com.foxmike.android.fragments.DisplaySessionFragment;
import com.foxmike.android.interfaces.AdvertisementRowClickedListener;
import com.foxmike.android.models.Advertisement;
import com.foxmike.android.utils.AdvertisementRowViewHolder;
import com.foxmike.android.utils.TextTimestamp;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class ListHistoryAdvertisementsAdapter extends RecyclerView.Adapter<AdvertisementRowViewHolder> {



    private ArrayList<Advertisement> advertisementArrayListArray = new ArrayList<>();
    private boolean isSuperHosted;
    private DisplaySessionFragment.OnHistoryAdClickedListener onHistoryAdClickedListener;

    public ListHistoryAdvertisementsAdapter(ArrayList<Advertisement> advertisementArrayListArray, boolean isSuperHosted, DisplaySessionFragment.OnHistoryAdClickedListener onHistoryAdClickedListener) {
        this.advertisementArrayListArray = advertisementArrayListArray;
        this.isSuperHosted = isSuperHosted;
        this.onHistoryAdClickedListener = onHistoryAdClickedListener;
    }

    @NonNull
    @Override
    public AdvertisementRowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.session_date_time_single_layout, parent, false);
        return new AdvertisementRowViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdvertisementRowViewHolder holder, int position) {
        long countParticipants;
        if (advertisementArrayListArray.get(position).getParticipantsTimestamps()!=null) {
            countParticipants = advertisementArrayListArray.get(position).getParticipantsTimestamps().size();
        } else {
            countParticipants = 0;
        }
        // set the text of each row in the list of advertisements
        int maxParticipants = advertisementArrayListArray.get(position).getMaxParticipants();
        holder.setParticipantsTV(countParticipants +"/" + maxParticipants);

        if (isSuperHosted) {
            holder.hideParticipantsTV();
        }

        Long endTimestamp = advertisementArrayListArray.get(position).getAdvertisementTimestamp() + (advertisementArrayListArray.get(position).getDurationInMin()*1000*60);

        holder.advertisementRowDateText.setText(TextTimestamp.textSessionDate(advertisementArrayListArray.get(position).getAdvertisementTimestamp()));
        holder.advertisementRowTimeText.setText(TextTimestamp.textTime(advertisementArrayListArray.get(position).getAdvertisementTimestamp()) + "-" + TextTimestamp.textTime(endTimestamp));
        // set the click listener on each row
        holder.setAdvertisementRowClickedListener(new AdvertisementRowClickedListener() {
            @Override
            public void OnAdvertisementRowClicked(View view, int position) {
                onHistoryAdClickedListener.OnHistoryAdClicked(advertisementArrayListArray.get(position));
            }

            @Override
            public void OnParticipantsClicked(int position) {
                onHistoryAdClickedListener.OnHistoryAdClicked(advertisementArrayListArray.get(position));
            }
        });

        holder.toggleBooked(false);
        holder.toggleFullyBooked(false);

        if (advertisementArrayListArray.get(position).getParticipantsTimestamps().size()!=0) {
            if (advertisementArrayListArray.get(position).getParticipantsTimestamps().containsKey(FirebaseAuth.getInstance().getUid())) {
                holder.toggleBooked(true);
                holder.toggleFullyBooked(false);
            } else {
                if (advertisementArrayListArray.get(position).getParticipantsTimestamps().size()>=advertisementArrayListArray.get(position).getMaxParticipants()) {
                    holder.toggleBooked(false);
                    holder.toggleFullyBooked(true);
                } else {
                    holder.toggleBooked(false);
                    holder.toggleFullyBooked(false);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return advertisementArrayListArray.size();
    }

    public void addData(ArrayList<Advertisement> addedAdvertisements) {
        advertisementArrayListArray.addAll(addedAdvertisements);
        this.notifyDataSetChanged();
    }
}
