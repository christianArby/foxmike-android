package com.foxmike.android.fragments;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatRatingBar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.foxmike.android.R;
import com.foxmike.android.models.Advertisement;
import com.foxmike.android.models.Rating;
import com.foxmike.android.models.Review;
import com.foxmike.android.utils.TextTimestamp;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
    private Advertisement advertisement;
    private View view;
    private float thisRating;


    public WriteReviewsFragment() {
        // Required empty public constructor
    }

    public static WriteReviewsFragment newInstance(Advertisement advertisement) {
        WriteReviewsFragment fragment = new WriteReviewsFragment();
        Bundle args = new Bundle();
        args.putSerializable("advertisement", advertisement);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            advertisement = (Advertisement) getArguments().getSerializable("advertisement");
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

        reviewTitle.setText(getString(R.string.you_have_been_on_the_session)+ advertisement.getSessionName() + " " + TextTimestamp.textSessionDateAndTime(advertisement.getAdvertisementTimestamp()) + getString(R.string.leave_your_review_below));
        ratingTitle.setText(R.string.Rate);

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, final float v, boolean b) {
                if (b) {
                    thisRating = (float)Math.ceil(v);

                    rootDbRef.child("reviewsToWrite").child(currentUserId).child(advertisement.getAdvertisementId()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue()!=null) {
                                Long currentTimestamp = System.currentTimeMillis();
                                String ratingAndReviewId = rootDbRef.child("ratings").push().getKey();
                                Rating rating = new Rating(advertisement.getHost(),currentUserId, advertisement.getAdvertisementId(), advertisement.getSessionId(), (int) thisRating, currentTimestamp);
                                rootDbRef.child("ratings").child(ratingAndReviewId).setValue(rating);

                                if (reviewText.getText().toString().length()>0) {
                                    Review review = new Review(advertisement.getHost(), currentUserId, advertisement.getAdvertisementId(), advertisement.getSessionId(), reviewText.getText().toString(), (int) thisRating, currentTimestamp);
                                    rootDbRef.child("reviews").child(ratingAndReviewId).setValue(review);
                                }
                                rootDbRef.child("reviewsToWrite").child(currentUserId).child(advertisement.getAdvertisementId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        hideKeyboard();
                                        dismiss();
                                    }
                                });
                            } else {
                                hideKeyboard();
                                dismiss();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(getActivity().getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
                            hideKeyboard();
                            dismiss();
                        }
                    });
/**/

                }
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
