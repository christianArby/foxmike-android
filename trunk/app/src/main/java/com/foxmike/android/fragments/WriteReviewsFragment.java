package com.foxmike.android.fragments;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatRatingBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;

import com.foxmike.android.R;
import com.foxmike.android.models.Advertisement;
import com.foxmike.android.models.Rating;
import com.foxmike.android.models.Review;
import com.foxmike.android.utils.TextTimestamp;
import com.foxmike.android.viewmodels.FirebaseDatabaseViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WriteReviewsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WriteReviewsFragment extends DialogFragment {

    public static final String TAG = WriteReviewsFragment.class.getSimpleName();

    @BindView(R.id.closeImageButton) ImageButton closeIcon;
    @BindView(R.id.ratingTitle) TextView ratingTitle;
    @BindView(R.id.reviewTitle) TextView reviewTitle;
    @BindView(R.id.ratingBar)
    AppCompatRatingBar ratingBar;
    @BindView(R.id.reviewText) EditText reviewText;

    private DatabaseReference rootDbRef = FirebaseDatabase.getInstance().getReference();
    private String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private InputMethodManager imm;



    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    // TODO: Rename and change types of parameters
    private String advertisementId;
    private View view;


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
        view = inflater.inflate(R.layout.fragment_write_reviews, container, false);
        ButterKnife.bind(this, view);

        closeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
                dismiss();
            }
        });

        FirebaseDatabaseViewModel reviewsToWriteUserIdViewModel = ViewModelProviders.of(this).get(FirebaseDatabaseViewModel.class);
        LiveData<DataSnapshot> reviewsToWriteLiveData = reviewsToWriteUserIdViewModel.getDataSnapshotLiveData(FirebaseDatabase.getInstance().getReference().child("reviewsToWrite").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(advertisementId));
        reviewsToWriteLiveData.observe(getViewLifecycleOwner(), new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()==null) {
                    return;
                }

                rootDbRef.child("advertisements").child(advertisementId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.getValue()==null) {
                            if (isAdded()) {
                                dismiss();
                            }
                            return;
                        }

                        if (!isAdded()) {
                            return;
                        }

                        Advertisement advertisement = dataSnapshot.getValue(Advertisement.class);
                        reviewTitle.setText(getString(R.string.you_have_been_on_the_session)+ advertisement.getSessionName() + " " + TextTimestamp.textSessionDate(advertisement.getAdvertisementTimestamp()) + getString(R.string.leave_your_review_below));
                        ratingTitle.setText(R.string.Rate);

                        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                            @Override
                            public void onRatingChanged(RatingBar ratingBar, final float v, boolean b) {
                                if (b) {
                                    float thisRating = (float)Math.ceil(v);
                                    Long currentTimestamp = System.currentTimeMillis();

                                    String ratingAndReviewId = rootDbRef.child("ratings").push().getKey();

                                    Rating rating = new Rating(advertisement.getHost(),currentUserId, advertisementId, advertisement.getSessionId(), (int) thisRating, currentTimestamp);
                                    rootDbRef.child("ratings").child(ratingAndReviewId).setValue(rating);

                                    if (reviewText.getText().toString().length()>0) {
                                        Review review = new Review(advertisement.getHost(), currentUserId, advertisementId, advertisement.getSessionId(), reviewText.getText().toString(), (int) thisRating, currentTimestamp);
                                        rootDbRef.child("reviews").child(ratingAndReviewId).setValue(review);
                                    }

                                    rootDbRef.child("reviewsToWrite").child(currentUserId).child(advertisementId).removeValue();
                                }
                                view.postDelayed(new Runnable() {

                                    @Override
                                    public void run() {
                                        hideKeyboard();
                                        dismiss();
                                    }

                                }, 200);
                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        imm = null;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    public void hideKeyboard() {
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

}
