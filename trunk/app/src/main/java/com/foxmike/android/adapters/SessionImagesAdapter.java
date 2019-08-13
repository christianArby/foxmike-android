package com.foxmike.android.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.foxmike.android.R;

import java.util.ArrayList;

public class SessionImagesAdapter extends RecyclerView.Adapter<SessionImagesAdapter.SessionImagesViewHolder>{

    private Context context;
    private ArrayList<String> imageURLs = new ArrayList<>();
    private String sessionName;
    private String sessionType;
    private String sessionAddress;
    private Boolean hasRating;
    private String ratingText;

    public SessionImagesAdapter(Context context, ArrayList<String> imageURLs, String sessionName, String sessionType, String sessionAddress, Boolean hasRating, String ratingText) {
        this.context = context;
        this.imageURLs = imageURLs;
        this.sessionName = sessionName;
        this.sessionType = sessionType;
        this.sessionAddress = sessionAddress;
        this.hasRating = hasRating;
        this.ratingText = ratingText;
    }

    @NonNull
    @Override
    public SessionImagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.session_image_single_layout, parent, false);
        return new SessionImagesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionImagesViewHolder holder, int position) {
        if (position==0) {
            holder.setIsFirstImage(true);
            holder.setImage(context,imageURLs.get(position));
            holder.setSessionName(sessionName);
            holder.setSessionType(sessionType);
            holder.setSessionAddress(sessionAddress);
            holder.setSessionRating(ratingText);
            holder.setHasRating(hasRating);
            return;
        }
        holder.setIsFirstImage(false);
        holder.setImage(context,imageURLs.get(position));
    }

    @Override
    public int getItemCount() {
        return imageURLs.size();
    }

    public class SessionImagesViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public SessionImagesViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setImage(Context context, String imageURL) {
            ImageView sessionImageView = mView.findViewById(R.id.displaySessionImage);
            Glide.with(context).load(imageURL).into(sessionImageView);
        }

        public void setSessionName(String sessionName) {
            TextView sessionNameTV = mView.findViewById(R.id.sessionName);
            sessionNameTV.setText(sessionName);
        }
        public void setSessionType(String sessionType) {
            TextView sessionTypeTV = mView.findViewById(R.id.sessionType);
            sessionTypeTV.setText(sessionType);
        }
        public void setSessionAddress(String sessionAddress) {
            TextView sessionAddressTV = mView.findViewById(R.id.addressTV);
            sessionAddressTV.setText(sessionAddress);
        }
        public void setSessionRating(String sessionRating) {
            TextView sessionRatingTV = mView.findViewById(R.id.ratingsAndReviewsText);
            sessionRatingTV.setText(sessionRating);
        }

        public void setHasRating(Boolean hasRating) {
            TextView sessionRatingTV = mView.findViewById(R.id.ratingsAndReviewsText);
            TextView newFlag = mView.findViewById(R.id.newFlag);
            if (hasRating) {
                newFlag.setVisibility(View.GONE);
                sessionRatingTV.setVisibility(View.VISIBLE);
            } else {
                newFlag.setVisibility(View.VISIBLE);
                sessionRatingTV.setVisibility(View.GONE);
            }
        }

        public void setIsFirstImage(boolean firstImage) {
            ImageView sessionImageView = mView.findViewById(R.id.displaySessionImage);
            TextView sessionNameTV = mView.findViewById(R.id.sessionName);
            TextView sessionTypeTV = mView.findViewById(R.id.sessionType);
            TextView sessionAddressTV = mView.findViewById(R.id.addressTV);
            TextView sessionRatingTV = mView.findViewById(R.id.ratingsAndReviewsText);
            TextView newFlag = mView.findViewById(R.id.newFlag);
            if (firstImage) {
                sessionImageView.setColorFilter(0x55000000, PorterDuff.Mode.SRC_ATOP);
                sessionNameTV.setVisibility(View.VISIBLE);
                sessionTypeTV.setVisibility(View.VISIBLE);
                sessionAddressTV.setVisibility(View.VISIBLE);
                sessionRatingTV.setVisibility(View.VISIBLE);
                newFlag.setVisibility(View.VISIBLE);
            } else {
                sessionImageView.setColorFilter(null);
                sessionNameTV.setVisibility(View.GONE);
                sessionTypeTV.setVisibility(View.GONE);
                sessionAddressTV.setVisibility(View.GONE);
                sessionRatingTV.setVisibility(View.GONE);
                newFlag.setVisibility(View.GONE);
            }
        }

    }
}
