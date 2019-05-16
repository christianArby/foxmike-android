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
import com.foxmike.android.activities.BecomeFTActivity;
import com.foxmike.android.activities.CreateTrainerActivity;
import com.foxmike.android.activities.PaymentPreferencesActivity;
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

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

/**
 * This Fragment creates the user profile view by using the xml files:
 *      - fragment_user_account.xml
 *      - user_profile_info.xml
 *      - session_small_single_layout.xml_layout.xml
 * The data (used to generate the views) is retrieved by using an object of the class PlayerSessionsContent in order to generate the fill the views with content in the correct order.
 */

public class UserAccountFragment extends Fragment {

    public static final String TAG = UserAccountFragment.class.getSimpleName();

    private FirebaseAuth mAuth;
    private OnUserAccountFragmentInteractionListener mListener;
    private ValueEventListener currentUserListener;
    private String currentUserID;
    private DatabaseReference rootDbRef;
    private LinearLayout list;
    private View profile;
    private TextView addPaymentMethod;
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

    public UserAccountFragment() {
        // Required empty public constructor
    }

    public static UserAccountFragment newInstance() {
        UserAccountFragment fragment = new UserAccountFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        /* Get the view fragment_user_account */
        view = inflater.inflate(R.layout.fragment_user_account, container, false);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        /* Inflate the LinearLayout list (in fragment_user_account) with the layout user_profile_info */
        list = view.findViewById(R.id.list1);
        profile = inflater.inflate(R.layout.user_account_player_layout,list,false);
        list.addView(profile);
        final ProgressBar progressBar = view.findViewById(R.id.progressBar_cyclic);
        final MyProgressBar myProgressBar = new MyProgressBar(progressBar, getActivity());

        fullNameTV = profile.findViewById(R.id.profileTV);
        userNameTV = profile.findViewById(R.id.userNameTV);
        aboutTV = profile.findViewById(R.id.aboutTV);
        TextView editProfileTV = profile.findViewById(R.id.edit_session_question);
        progressBackground = view.findViewById(R.id.progressBackground);
        final MyFirebaseDatabase myFirebaseDatabase = new MyFirebaseDatabase();
        /* Find and set the clickable LinearLayout switchModeLL and write the trainerMode status to the database */
        switchModeTV = view.findViewById(R.id.switchModeTV);
        addPaymentMethod = profile.findViewById(R.id.addPaymentMethodTV);

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

        FirebaseDatabaseViewModel firebaseDatabaseUserViewModel = ViewModelProviders.of(this).get(FirebaseDatabaseViewModel.class);
        LiveData<DataSnapshot> firebaseDatabaseUserLiveData = firebaseDatabaseUserViewModel.getDataSnapshotLiveData(rootDbRef.child("users").child(currentUserID));
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
                Intent paymentPreferencesIntent = new Intent(getActivity().getApplicationContext(),PaymentPreferencesActivity.class);
                startActivity(paymentPreferencesIntent);
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

            if (!currentUser.isAdmin()) {
                switchModeTV.setText(R.string.become_trainer);

                switchModeTV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent becomeFtIntent = new Intent(getActivity().getApplicationContext(),BecomeFTActivity.class);
                        startActivity(becomeFtIntent);
                    }
                });
            } else {
                if (currentUser.getStripeAccountId()!=null) {
                    switchModeTV.setText(R.string.switch_to_host_mode_text);
                } else {
                    switchModeTV.setText(R.string.become_trainer);
                }

                switchModeTV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (!connected) {
                            Toast.makeText(getActivity().getApplicationContext(),"No internet connection", Toast.LENGTH_LONG).show();
                            return;
                        }

                        if (!currentUser.trainerMode && currentUser.getStripeAccountId()==null) {
                            Intent createIntent = new Intent(getActivity().getApplicationContext(),CreateTrainerActivity.class);
                            startActivityForResult(createIntent, 1);
                            return;
                        }

                        Intent fadeIntent = new Intent(getActivity().getApplicationContext(),SwitchModeActivity.class);
                        fadeIntent.putExtra("trainerMode", false);
                        startActivity(fadeIntent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
                    }
                });
            }

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            Intent fadeIntent = new Intent(getActivity().getApplicationContext(),SwitchModeActivity.class);
            fadeIntent.putExtra("trainerMode", false);
            startActivity(fadeIntent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
        }
    }



    // Method to set and scale an image into an circular imageView
    private void setCircleImage(String image, CircleImageView imageView) {
        Glide.with(this).load(image).into(imageView);
    }

    private void logout() {
        Intent welcomeIntent = new Intent(getActivity(),WelcomeActivity.class);
        welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(welcomeIntent);
        mAuth.signOut();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
