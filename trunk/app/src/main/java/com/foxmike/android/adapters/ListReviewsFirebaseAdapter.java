package com.foxmike.android.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatRatingBar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.foxmike.android.R;
import com.foxmike.android.models.Review;
import com.foxmike.android.models.UserPublic;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

/**
 * Created by chris on 2019-03-20.
 */

public class ListReviewsFirebaseAdapter extends FirebaseRecyclerAdapter<Review, ListReviewsFirebaseAdapter.ListReviewsViewHolder> {
    private Context context;
    HashMap<String, UserPublic> userPublicHashMap = new HashMap<>();


    public ListReviewsFirebaseAdapter(@NonNull FirebaseRecyclerOptions<Review> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ListReviewsViewHolder holder, int position, @NonNull Review model) {
        holder.setRating(model.getRating());
        populateUserPublicHashMap(model.getAuthorId(), new OnUsersLoadedListener() {
            @Override
            public void OnUsersLoaded() {
                holder.setName(userPublicHashMap.get(model.getAuthorId()).firstName);
            }
        });
        holder.setText(model.getReviewText());
    }

    @NonNull
    @Override
    public ListReviewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.review_single_layout, parent, false);
        return new ListReviewsViewHolder(view);
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

    }

    private void populateUserPublicHashMap(String userId, OnUsersLoadedListener onUsersLoadedListener) {
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
