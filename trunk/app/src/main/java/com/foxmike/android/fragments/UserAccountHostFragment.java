package com.foxmike.android.fragments;
// Checked

import android.app.ActivityOptions;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.foxmike.android.R;
import com.foxmike.android.activities.AboutActivity;
import com.foxmike.android.activities.PaymentPreferencesActivity;
import com.foxmike.android.activities.PayoutPreferencesActivity;
import com.foxmike.android.activities.SwitchModeActivity;
import com.foxmike.android.activities.WelcomeActivity;
import com.foxmike.android.models.User;
import com.foxmike.android.utils.MyFirebaseDatabase;
import com.foxmike.android.utils.MyProgressBar;
import com.foxmike.android.viewmodels.FirebaseDatabaseViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * This Fragment creates the user profile view by using the xml files:
 *      - fragment_user_account.xml
 *      - user_profile_info.xml
 *      - session_small_single_layout.xml_layout.xml
 * The data (used to generate the views) is retrieved by using an object of the class PlayerSessionsContent in order to generate the fill the views with content in the correct order.
 */

public class UserAccountHostFragment extends Fragment {

    public static final String TAG = CreateTrainerAboutMeFragment.class.getSimpleName();

    private FirebaseAuth mAuth;
    private OnUserAccountFragmentInteractionListener mListener;
    private ValueEventListener currentUserListener;
    private String currentUserID;
    private DatabaseReference rootDbRef;
    private LinearLayout list;
    private LinearLayout payoutMethodContainer;
    private View profile;
    private TextView addPaymentMethod;
    private TextView addPayoutMethod;
    private FrameLayout progressBackground;
    private View view;
    private TextView aboutTV;
    private DatabaseReference usersDbRef;
    private boolean connected;
    private User currentUser;
    private boolean userLoaded;
    private boolean userAndViewUsed;

    private TextView fullNameTV;
    private TextView userNameTV;
    private TextView switchModeTV;
    private long mLastClickTime = 0;
    private TextView depositionTV;

    public UserAccountHostFragment() {
        // Required empty public constructor
    }

    public static UserAccountHostFragment newInstance() {
        UserAccountHostFragment fragment = new UserAccountHostFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        /* Get the view fragment_user_account */
        view = inflater.inflate(R.layout.fragment_user_account_host, container, false);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        /* Inflate the LinearLayout list (in fragment_user_account) with the layout user_profile_info */
        list = view.findViewById(R.id.list1);
        profile = inflater.inflate(R.layout.user_account_host_layout,list,false);
        list.addView(profile);
        final ProgressBar progressBar = view.findViewById(R.id.progressBar_cyclic);
        final MyProgressBar myProgressBar = new MyProgressBar(progressBar, getActivity());

        fullNameTV = profile.findViewById(R.id.profileTV);
        userNameTV = profile.findViewById(R.id.userNameTV);
        aboutTV = profile.findViewById(R.id.aboutTV);
        TextView editProfileTV = profile.findViewById(R.id.edit_session_question);
        progressBackground = view.findViewById(R.id.progressBackground);
        final MyFirebaseDatabase myFirebaseDatabase = new MyFirebaseDatabase();
        depositionTV = profile.findViewById(R.id.depositionTV);
        /* Find and set the clickable LinearLayout switchModeLL and write the trainerMode status to the database */
        switchModeTV = view.findViewById(R.id.switchModeTV);
        addPaymentMethod = profile.findViewById(R.id.addPaymentMethodTV);
        addPayoutMethod = profile.findViewById(R.id.addPayoutMethodTV);
        payoutMethodContainer = profile.findViewById(R.id.payoutMethodContainer);

        FirebaseDatabaseViewModel firebaseDatabaseViewModel = ViewModelProviders.of(this).get(FirebaseDatabaseViewModel.class);
        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        LiveData<DataSnapshot> firebaseDatabaseLiveData = firebaseDatabaseViewModel.getDataSnapshotLiveData(connectedRef);
        firebaseDatabaseLiveData.observe(getViewLifecycleOwner(), new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()!=null) {
                    connected = dataSnapshot.getValue(Boolean.class);
                }
            }
        });

        rootDbRef = FirebaseDatabase.getInstance().getReference();

        // GET CURRENT USER FROM DATABASE
        FirebaseDatabaseViewModel firebaseDatabaseUserViewModel = ViewModelProviders.of(this).get(FirebaseDatabaseViewModel.class);
        LiveData<DataSnapshot> firebaseDatabaseUserLiveData = firebaseDatabaseUserViewModel.getDataSnapshotLiveData(rootDbRef.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()));
        firebaseDatabaseUserLiveData.observe(getViewLifecycleOwner(), new Observer<DataSnapshot>() {
            @Override
            public void onChanged(@Nullable DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()!=null) {
                    currentUser = dataSnapshot.getValue(User.class);
                    userLoaded = true;
                    userAndViewUsed = false;
                    onAsyncTaskFinished();
                }
            }
        });

        depositionTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.OnUserAccountFragmentInteraction("DEPOSITION");
            }
        });

        aboutTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                Intent aboutIntent = new Intent(getActivity().getApplicationContext(), AboutActivity.class);
                startActivity(aboutIntent);
            }
        });

        editProfileTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                if (mListener != null) {
                    mListener.OnUserAccountFragmentInteraction("edit");
                }
            }
        });

        fullNameTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                if (mListener != null) {
                    mListener.OnUserAccountFragmentInteraction("edit");
                }
            }
        });

        addPaymentMethod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                Intent paymentPreferencesIntent = new Intent(getActivity(),PaymentPreferencesActivity.class);
                startActivity(paymentPreferencesIntent);
            }
        });

        addPayoutMethod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                Intent payoutPreferencesIntent = new Intent(getActivity(),PayoutPreferencesActivity.class);
                startActivity(payoutPreferencesIntent);
            }
        });


        TextView logOutView = view.findViewById(R.id.logOutTV);
        logOutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onAsyncTaskFinished();
    }

    private void onAsyncTaskFinished() {

        if (getView()!=null && userLoaded && !userAndViewUsed) {
            userAndViewUsed = true;

            fullNameTV.setText(currentUser.getFullName());
            userNameTV.setText(currentUser.getUserName());

            setCircleImage(currentUser.getImage(), (CircleImageView) profile.findViewById(R.id.profileIV));

            switchModeTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();

                    if (!connected) {
                        Toast.makeText(getActivity().getApplicationContext(),"No internet connection", Toast.LENGTH_LONG).show();
                        return;
                    }

                    Intent fadeIntent = new Intent(getActivity().getApplicationContext(),SwitchModeActivity.class);
                    fadeIntent.putExtra("trainerMode", true);
                    startActivity(fadeIntent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
                }
            });
        }
    }

    // Method to set and scale an image into an circular imageView
    private void setCircleImage(String image, CircleImageView imageView) {
        Glide.with(this).load(image).into(imageView);
    }

    private void logout() {
        String deviceToken = FirebaseInstanceId.getInstance().getToken();
        rootDbRef.child("users").child(currentUserID).child("device_token").child(deviceToken).setValue(null);
        Intent welcomeIntent = new Intent(getActivity(),WelcomeActivity.class);
        welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(welcomeIntent);
        mAuth.signOut();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnUserAccountFragmentInteractionListener) {
            mListener = (OnUserAccountFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnUserAccountFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnUserAccountFragmentInteractionListener {
        void OnUserAccountFragmentInteraction(String type);
    }

}
