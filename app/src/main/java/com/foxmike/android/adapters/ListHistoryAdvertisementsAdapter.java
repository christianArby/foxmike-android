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
    private boolean isSuperAdmin;
    private boolean isHost;
    private DisplaySessionFragment.OnHistoryAdClickedListener onHistoryAdClickedListener;

    public ListHistoryAdvertisementsAdapter(ArrayList<Advertisement> advertisementArrayListArray, boolean isSuperHosted, boolean isSuperAdmin, boolean isHost, DisplaySessionFragment.OnHistoryAdClickedListener onHistoryAdClickedListener) {
        this.advertisementArrayListArray = advertisementArrayListArray;
        this.isSuperHosted = isSuperHosted;
        this.isHost = isHost;
        this.onHistoryAdClickedListener = onHistoryAdClickedListener;
        this.isSuperAdmin = isSuperAdmin;
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

        holder.toggleParticipantsLeftTV(false);
        holder.toggleParticipantsTV(false);

        if (isSuperAdmin || isHost) {
            holder.toggleParticipantsTV(true);
        }

        holder.toggleBooked(false);
        holder.toggleFullyBooked(false);

        if (!isSuperHosted) {
            if (advertisementArrayListArray.get(position).getParticipantsTimestamps().size()!=0) {
                if (advertisementArrayListArray.get(position).getParticipantsTimestamps().containsKey(FirebaseAuth.getInstance().getUid())) {
                    holder.toggleBooked(true);
                    holder.toggleFullyBooked(false);
                    holder.toggleParticipantsTV(true);
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

    }

    @Override
    public int getItemCount() {
        return advertisementArrayListArray == null ? 0 : advertisementArrayListArray.size();
    }

    public void clear() {
        while (getItemCount() > 0) { remove(getItem(0)); }
    }

    public void remove(Advertisement ad) {
        int position = advertisementArrayListArray.indexOf(ad);
        if (position > -1) {
            advertisementArrayListArray.remove(position);
            notifyItemRemoved(position);
        }
    }

    public Advertisement getItem(int position) {
        return advertisementArrayListArray.get(position);
    }

    public void addData(ArrayList<Advertisement> addedAdvertisements) {
        advertisementArrayListArray.addAll(addedAdvertisements);
        this.notifyDataSetChanged();
    }
}
