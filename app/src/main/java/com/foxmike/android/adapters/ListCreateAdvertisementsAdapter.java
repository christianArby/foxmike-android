package com.foxmike.android.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.foxmike.android.R;
import com.foxmike.android.interfaces.OnAdvertisementArrayListChangedListener;
import com.foxmike.android.models.Advertisement;
import com.foxmike.android.utils.TextTimestamp;

import org.joda.time.DateTime;

import java.util.ArrayList;

import static com.foxmike.android.utils.Price.PRICES_STRINGS_SE;

/**
 * Created by chris on 2019-04-09.
 */

public class ListCreateAdvertisementsAdapter extends RecyclerView.Adapter<ListCreateAdvertisementsAdapter.ListCreateAdvertisementsViewHolder>{

    private ArrayList<Advertisement> advertisementArrayList;
    private ArrayList<Advertisement> totalAdvertisementArrayList = new ArrayList<>();
    private OnAdvertisementArrayListChangedListener onAdvertisementArrayListChangedListener;
    private DateTime selectedDate;
    private boolean hasContent = false;

    public ListCreateAdvertisementsAdapter(ArrayList<Advertisement> existingAdvertisementArrayList, ArrayList<Advertisement> advertisementArrayList, DateTime pickedDate, OnAdvertisementArrayListChangedListener onAdvertisementArrayListChangedListener) {
        this.advertisementArrayList = advertisementArrayList;
        this.totalAdvertisementArrayList.addAll(existingAdvertisementArrayList);
        this.totalAdvertisementArrayList.addAll(advertisementArrayList);
        this.onAdvertisementArrayListChangedListener = onAdvertisementArrayListChangedListener;
        this.selectedDate = pickedDate;
    }

    @NonNull
    @Override
    public ListCreateAdvertisementsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view;
        view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.created_ad_single_layout, viewGroup, false);
        return new ListCreateAdvertisementsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListCreateAdvertisementsViewHolder listCreateAdvertisementsViewHolder, int i) {

        DateTime adDateTime = new DateTime(totalAdvertisementArrayList.get(i).getAdvertisementTimestamp());

        if (!selectedDate.toLocalDate().equals(adDateTime.toLocalDate())) {
            listCreateAdvertisementsViewHolder.mView.setVisibility(View.GONE);
            listCreateAdvertisementsViewHolder.mView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            hasContent = false;
        } else {
            hasContent = true;

            listCreateAdvertisementsViewHolder.mView.setVisibility(View.VISIBLE);
            listCreateAdvertisementsViewHolder.mView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            listCreateAdvertisementsViewHolder.setTextLeft(TextTimestamp.textTime(totalAdvertisementArrayList.get(i).getAdvertisementTimestamp()) + " - " + PRICES_STRINGS_SE.get(totalAdvertisementArrayList.get(i).getPrice()));
            if (totalAdvertisementArrayList.get(i).getSessionId()!=null) {
                listCreateAdvertisementsViewHolder.setIsAdvertised(true);
            } else {
                listCreateAdvertisementsViewHolder.setIsAdvertised(false);
            }

            listCreateAdvertisementsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int removePosition = -1;
                    for (Advertisement advertisement: advertisementArrayList) {
                        if (totalAdvertisementArrayList.get(i).getAdvertisementTimestamp() == advertisement.getAdvertisementTimestamp()) {
                            removePosition = advertisementArrayList.indexOf(advertisement);
                        }
                    }
                    if (removePosition > -1) {
                        advertisementArrayList.remove(removePosition);
                        ListCreateAdvertisementsAdapter.this.notifyDataSetChanged();
                        onAdvertisementArrayListChangedListener.OnAdvertisementArrayList(advertisementArrayList);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return totalAdvertisementArrayList.size();
    }

    public class ListCreateAdvertisementsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public ListCreateAdvertisementsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setTextLeft(String text) {
            TextView textLeftTV = mView.findViewById(R.id.textLeft);
            textLeftTV.setText(text);
        }

        public void setIsAdvertised(boolean isAdvertised) {
            AppCompatTextView advertisedFlag = mView.findViewById(R.id.advertisedFlag);
            ImageView delete = mView.findViewById(R.id.delete);
            if (isAdvertised) {
                delete.setVisibility(View.GONE);
                advertisedFlag.setVisibility(View.VISIBLE);
            } else {
                delete.setVisibility(View.VISIBLE);
                advertisedFlag.setVisibility(View.GONE);
            }

        }
    }

    public void updateAdvertisements(ArrayList<Advertisement> existingAdvertisementArrayList,ArrayList<Advertisement> advertisementArrayList, DateTime selectedDate) {
        hasContent = false;
        this.totalAdvertisementArrayList.clear();
        this.selectedDate = selectedDate;
        this.advertisementArrayList = advertisementArrayList;
        this.totalAdvertisementArrayList.addAll(existingAdvertisementArrayList);
        this.totalAdvertisementArrayList.addAll(advertisementArrayList);
        this.notifyDataSetChanged();
    }

    public boolean isHasContent() {
        return hasContent;
    }

    public void setHasContent(boolean hasContent) {
        this.hasContent = hasContent;
    }
}
