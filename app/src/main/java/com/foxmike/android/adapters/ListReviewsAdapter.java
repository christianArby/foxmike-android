package com.foxmike.android.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatRatingBar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.foxmike.android.R;
import com.foxmike.android.models.Review;
import com.foxmike.android.models.UserPublic;
import com.foxmike.android.utils.TextTimestamp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by chris on 2019-03-20.
 */

public class ListReviewsAdapter extends RecyclerView.Adapter<ListReviewsAdapter.ListReviewsViewHolder>{

    private ArrayList<Review> reviewArray = new ArrayList<>();
    private HashMap<String, UserPublic> userPublicHashMap = new HashMap<>();

    @NonNull
    @Override
    public ListReviewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.review_single_layout, parent, false);
        return new ListReviewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListReviewsViewHolder holder, int position) {
        holder.setRating(reviewArray.get(position).getRating());

        populateUserPublicHashMap(reviewArray.get(position).getAuthorId(), new ListReviewsAdapter.OnUsersLoadedListener() {
            @Override
            public void OnUsersLoaded() {
                holder.setName(userPublicHashMap.get(reviewArray.get(position).getAuthorId()).getFirstName());
            }
        });
        holder.setText(reviewArray.get(position).getReviewText());
        holder.setReviewDate(TextTimestamp.textDateAndTime(reviewArray.get(position).getTimestamp()));

    }

    @Override
    public int getItemCount() {
        return reviewArray.size();
    }

    public void addData(ArrayList<Review> addedReviews) {
        reviewArray.addAll(addedReviews);
        this.notifyDataSetChanged();
    }

    public class ListReviewsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public ListReviewsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String reviewerName) {
            TextView reviewerNameTV = mView.findViewById(R.id.reviewerName);
            reviewerNameTV.setText(reviewerName);
        }

        public void setText(String reviewText) {
            TextView reviewTextTV = mView.findViewById(R.id.reviewText);
            reviewTextTV.setText(reviewText);
        }

        public void setRating(float rating) {
            AppCompatRatingBar ratingBar = mView.findViewById(R.id.ratingBar);
            ratingBar.setRating(rating);
        }

        public void setReviewDate(String date) {
            TextView reviewDate = mView.findViewById(R.id.reviewDate);
            reviewDate.setText(date);
        }

    }

    private void populateUserPublicHashMap(String userId, ListReviewsAdapter.OnUsersLoadedListener onUsersLoadedListener) {
        if (!userPublicHashMap.containsKey(userId)) {
            FirebaseDatabase.getInstance().getReference().child("usersPublic").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue()==null) {
                        return;
                    }
                    UserPublic userPublic = dataSnapshot.getValue(UserPublic.class);
                    userPublicHashMap.put(userId, userPublic);
                    onUsersLoadedListener.OnUsersLoaded();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        } else {
            onUsersLoadedListener.OnUsersLoaded();
        }
    }
    public interface OnUsersLoadedListener{
        void OnUsersLoaded();
    }
}
