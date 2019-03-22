package com.foxmike.android.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;

import com.foxmike.android.R;
import com.foxmike.android.models.Advertisement;
import com.foxmike.android.models.Rating;
import com.foxmike.android.models.Review;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.foxmike.android.activities.MainPlayerActivity.hideKeyboard;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WriteReviewsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WriteReviewsFragment extends DialogFragment {

    @BindView(R.id.closeImageButton) ImageButton closeIcon;
    @BindView(R.id.ratingTitle) TextView ratingTitle;
    @BindView(R.id.reviewTitle) TextView reviewTitle;
    @BindView(R.id.ratingBar) RatingBar ratingBar;
    @BindView(R.id.reviewText) EditText reviewText;

    private DatabaseReference rootDbRef = FirebaseDatabase.getInstance().getReference();
    private String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();



    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    // TODO: Rename and change types of parameters
    private String advertisementId;


    public WriteReviewsFragment() {
        // Required empty public constructor
    }

    public static WriteReviewsFragment newInstance(String advertisementId) {
        WriteReviewsFragment fragment = new WriteReviewsFragment();
        Bundle args = new Bundle();
        args.putString("advertisementId", advertisementId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            advertisementId = getArguments().getString("advertisementId");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_write_reviews, container, false);
        ButterKnife.bind(this, view);

        closeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(getActivity());
                dismiss();
            }
        });

        if (advertisementId!=null) {

            rootDbRef.child("advertisements").child(advertisementId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.getValue()==null) {
                        return;
                    }

                    Advertisement advertisement = dataSnapshot.getValue(Advertisement.class);
                    reviewTitle.setText(getString(R.string.you_have_been_on_the_session)+ advertisement.getSessionName() + getString(R.string.leave_your_review_below));
                    ratingTitle.setText(R.string.Rate);

                    ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                        @Override
                        public void onRatingChanged(RatingBar ratingBar, final float v, boolean b) {
                            if (b) {
                                float thisRating = (float)Math.ceil(v);
                                ratingBar.setRating(thisRating);
                                Long currentTimestamp = System.currentTimeMillis();


                                Rating rating = new Rating(currentUserId, advertisementId, advertisement.getSessionId(), (int)thisRating, currentTimestamp);
                                String ratingAndReviewId = rootDbRef.child("ratings").push().getKey();



                                rootDbRef.child("ratings").child(ratingAndReviewId).setValue(rating);
                                rootDbRef.child("userRatings").child(currentUserId).child(advertisement.getSessionId()).child(ratingAndReviewId).setValue(true);
                                rootDbRef.child("sessionRatings").child(advertisement.getSessionId()).child(ratingAndReviewId).setValue(true);
                                rootDbRef.child("trainerRatings").child(advertisement.getHost()).child(ratingAndReviewId).setValue(true);

                                if (reviewText.getText().toString().length()>0) {
                                    Review review = new Review(currentUserId, advertisementId, advertisement.getSessionId(), reviewText.getText().toString(), thisRating, currentTimestamp);
                                    rootDbRef.child("reviews").child(ratingAndReviewId).setValue(review);
                                    rootDbRef.child("userReviews").child(currentUserId).child(advertisement.getSessionId()).child(ratingAndReviewId).setValue(true);
                                    rootDbRef.child("sessionReviews").child(advertisement.getSessionId()).child(ratingAndReviewId).setValue(currentTimestamp);
                                    rootDbRef.child("trainerReviews").child(advertisement.getHost()).child(ratingAndReviewId).setValue(true);
                                }

                                rootDbRef.child("reviewsToWrite").child(currentUserId).child(advertisementId).removeValue();
                            }
                            hideKeyboard(getActivity());
                            dismiss();
                        }
                    });

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }





        return view;
    }

}