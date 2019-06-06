package com.foxmike.android.fragments;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.foxmike.android.R;
import com.foxmike.android.models.Advertisement;
import com.foxmike.android.models.Session;
import com.foxmike.android.models.UserPublic;
import com.foxmike.android.utils.TextTimestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.functions.FirebaseFunctions;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WriteReviewsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WriteReviewsFlagTrainerFragment extends Fragment {

    public static final String TAG = WriteReviewsFlagTrainerFragment.class.getSimpleName();
    private FirebaseFunctions mFunctions;
    private View mainView;

    @BindView(R.id.closeImageButton) ImageButton closeIcon;
    @BindView(R.id.flagTitle) TextView flagTitle;
    @BindView(R.id.a) AppCompatButton aBtn;
    @BindView(R.id.b) AppCompatButton bBtn;
    @BindView(R.id.c) AppCompatButton cBtn;
    @BindView(R.id.d) AppCompatButton dBtn;
    @BindView(R.id.cancelledContainer)
    LinearLayout cancelledContainer;
    @BindView(R.id.icon)
    ImageView sessionImage;
    @BindView(R.id.text1) TextView text1;
    @BindView(R.id.text2) TextView text2;
    @BindView(R.id.text3) TextView text3;

    private OnWriteReviewsFlagTrainerFragmentInteractionListener onWriteReviewsFlagTrainerFragmentInteractionListener;



    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    // TODO: Rename and change types of parameters
    private View view;
    private float thisRating;
    private HashMap reportData = new HashMap();
    private Advertisement advertisement;
    private Session session;
    private UserPublic host;
    private String ratingAndReviewId;


    public WriteReviewsFlagTrainerFragment() {
        // Required empty public constructor
    }

    public static WriteReviewsFlagTrainerFragment newInstance(Session session, Advertisement advertisement, UserPublic host, String ratingAndReviewId) {
        WriteReviewsFlagTrainerFragment fragment = new WriteReviewsFlagTrainerFragment();
        Bundle args = new Bundle();
        args.putSerializable("session", session);
        args.putSerializable("advertisement", advertisement);
        args.putSerializable("host", host);
        args.putSerializable("ratingAndReviewId", ratingAndReviewId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            session = (Session) getArguments().getSerializable("session");
            advertisement = (Advertisement) getArguments().getSerializable("advertisement");
            host = (UserPublic) getArguments().getSerializable("host");
            ratingAndReviewId = getArguments().getString("ratingAndReviewId");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_write_reviews_flag_trainer, container, false);
        ButterKnife.bind(this, mainView);

        mFunctions = FirebaseFunctions.getInstance();

        cancelledContainer.setVisibility(View.GONE);

        closeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reportData.put("reasonStandard", "x");
                sendFlagMail(reportData);
                onWriteReviewsFlagTrainerFragmentInteractionListener.onFinishedWriteReviewsFlagTrainer();
            }
        });


        Glide.with(getActivity().getApplicationContext()).load(session.getImageUrl()).into(sessionImage);
        text1.setText(session.getSessionName());
        text2.setText(getResources().getString(R.string.hosted_by_text) + " " + host.getFullName());
        text3.setText(TextTimestamp.textSessionDateAndTime(advertisement.getAdvertisementTimestamp()));

        flagTitle.setText(R.string.flag_title);

        reportData.put("sessionId", session.getSessionId());
        reportData.put("advertisementId", advertisement.getAdvertisementId());
        reportData.put("hostId", host.getUserId());
        reportData.put("ratingAndReviewId", ratingAndReviewId);
        reportData.put("currentUserId", FirebaseAuth.getInstance().getCurrentUser().getUid());
        reportData.put("currentUserEmail", FirebaseAuth.getInstance().getCurrentUser().getEmail());



        aBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportData.put("reasonStandard", "a");
                reportData.put("reasonStandardText", getResources().getString(R.string.flag_trainer_text_a));
                sendFlagMail(reportData);
                onWriteReviewsFlagTrainerFragmentInteractionListener.onFinishedWriteReviewsFlagTrainer();
            }
        });
        bBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportData.put("reasonStandard", "b");
                reportData.put("reasonStandardText", getResources().getString(R.string.flag_trainer_text_b));
                sendFlagMail(reportData);
                onWriteReviewsFlagTrainerFragmentInteractionListener.onFinishedWriteReviewsFlagTrainer();
            }
        });
        cBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportData.put("reasonStandard", "c");
                reportData.put("reasonStandardText", getResources().getString(R.string.flag_trainer_text_c));
                sendFlagMail(reportData);
                onWriteReviewsFlagTrainerFragmentInteractionListener.onFinishedWriteReviewsFlagTrainer();
            }
        });
        dBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onWriteReviewsFlagTrainerFragmentInteractionListener.onWriteCustomTextReason(session, advertisement, host, ratingAndReviewId);
            }
        });

        return mainView;
    }

    private void sendFlagMail(HashMap reportData) {
        mFunctions.getHttpsCallable("reportTrainer").call(reportData);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnWriteReviewsFlagTrainerFragmentInteractionListener) {
            onWriteReviewsFlagTrainerFragmentInteractionListener = (OnWriteReviewsFlagTrainerFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnWriteReviewsFlagTrainerFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onWriteReviewsFlagTrainerFragmentInteractionListener = null;
    }

    public interface OnWriteReviewsFlagTrainerFragmentInteractionListener {
        void onFinishedWriteReviewsFlagTrainer();
        void onWriteCustomTextReason(Session session, Advertisement advertisement,UserPublic host, String ratingAndReviewId);
    }

}
