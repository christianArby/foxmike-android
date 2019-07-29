package com.foxmike.android.fragments;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
public class WriteReviewsFlagTrainerWriteReportFragment extends Fragment {

    public static final String TAG = WriteReviewsFlagTrainerWriteReportFragment.class.getSimpleName();
    private FirebaseFunctions mFunctions;
    private View mainView;

    @BindView(R.id.closeImageButton) ImageButton closeIcon;
    @BindView(R.id.flagTitle) TextView flagTitle;
    @BindView(R.id.send) AppCompatButton sendBtn;
    @BindView(R.id.reportText) EditText reportTextET;
    @BindView(R.id.cancelledContainer)
    LinearLayout cancelledContainer;
    @BindView(R.id.icon)
    ImageView sessionImage;
    @BindView(R.id.text1) TextView text1;
    @BindView(R.id.text2) TextView text2;
    @BindView(R.id.text3) TextView text3;

    private OnWriteReviewsFlagTrainerWriteReportFragmentInteractionListener onWriteReviewsFlagTrainerWriteReportFragmentInteractionListener;

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


    public WriteReviewsFlagTrainerWriteReportFragment() {
        // Required empty public constructor
    }

    public static WriteReviewsFlagTrainerWriteReportFragment newInstance(Session session, Advertisement advertisement, UserPublic host, String ratingAndReviewId) {
        WriteReviewsFlagTrainerWriteReportFragment fragment = new WriteReviewsFlagTrainerWriteReportFragment();
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
        mainView = inflater.inflate(R.layout.fragment_write_reviews_flag_trainer_write_report, container, false);
        ButterKnife.bind(this, mainView);

        mFunctions = FirebaseFunctions.getInstance();

        cancelledContainer.setVisibility(View.GONE);

        closeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reportData.put("reasonStandard", "x");
                sendFlagMail(reportData);
                onWriteReviewsFlagTrainerWriteReportFragmentInteractionListener.onWriteReportFinished();
            }
        });

        Glide.with(getActivity().getApplicationContext()).load(session.getImageUrl()).into(sessionImage);
        text1.setText(session.getSessionName());
        text2.setText(getResources().getString(R.string.hosted_by_text) + " " + host.getFullName());
        text3.setText(TextTimestamp.textSessionDateAndTime(advertisement.getAdvertisementTimestamp()));

        reportData.put("sessionId", session.getSessionId());
        reportData.put("advertisementId", advertisement.getAdvertisementId());
        reportData.put("hostId", host.getUserId());
        reportData.put("ratingAndReviewId", ratingAndReviewId);
        reportData.put("currentUserId", FirebaseAuth.getInstance().getCurrentUser().getUid());
        reportData.put("currentUserEmail", FirebaseAuth.getInstance().getCurrentUser().getEmail());

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportData.put("reasonStandard", "d");
                reportData.put("reasonStandardText", getResources().getString(R.string.flag_trainer_text_d));
                reportData.put("reasonText", reportTextET.getText().toString());
                sendFlagMail(reportData);
                onWriteReviewsFlagTrainerWriteReportFragmentInteractionListener.onWriteReportFinished();
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
        if (context instanceof OnWriteReviewsFlagTrainerWriteReportFragmentInteractionListener) {
            onWriteReviewsFlagTrainerWriteReportFragmentInteractionListener = (OnWriteReviewsFlagTrainerWriteReportFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnWriteReviewsFlagTrainerWriteReportFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onWriteReviewsFlagTrainerWriteReportFragmentInteractionListener = null;
    }

    public interface OnWriteReviewsFlagTrainerWriteReportFragmentInteractionListener {
        void onWriteReportFinished();
    }

}
