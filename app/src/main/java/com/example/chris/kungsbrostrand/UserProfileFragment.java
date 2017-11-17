package com.example.chris.kungsbrostrand;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * This Fragment creates the user profile view by using the xml files:
 *      - fragment_user_profile.xml
 *      - user_profile_info.xml
 *      - session_row_view.xml
 * The data (used to generate the views) is retrieved by using an object of the class PlayerSessionsContent in order to generate the fill the views with content in the correct order.
 */

public class UserProfileFragment extends Fragment {

    private final DatabaseReference usersDbRef = FirebaseDatabase.getInstance().getReference().child("users");
    private final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseAuth mAuth;

    private LinearLayout list;
    private LatLng sessionLatLng;
    private View profile;

    public UserProfileFragment() {
        // Required empty public constructor
    }

    public static UserProfileFragment newInstance() {
        UserProfileFragment fragment = new UserProfileFragment();
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

        /* Get the view fragment_user_profile */
        final View view = inflater.inflate(R.layout.fragment_user_profile, container, false);
        mAuth = FirebaseAuth.getInstance();

        /* Inflate the LinearLayout list (in fragment_user_profile) with the layout user_profile_info */
        list = view.findViewById(R.id.list1);
        profile = inflater.inflate(R.layout.user_profile_info,list,false);
        list.addView(profile);

        final TextView userNameTV = profile.findViewById(R.id.profileTV);
        final MyFirebaseDatabase myFirebaseDatabase = new MyFirebaseDatabase();
        /* Find and set the clickable LinearLayout switchModeLL and write the trainerMode status to the database */
        final TextView switchModeTV = view.findViewById(R.id.switchModeTV);
        final View switchMode = view.findViewById(R.id.switchModeLL);
        switchMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                myFirebaseDatabase.getUser(new OnUserFoundListener() {
                    @Override
                    public void OnUserFound(User user) {
                        if (user.trainerMode) {
                            user.setTrainerMode(false);
                            usersDbRef.child(currentFirebaseUser.getUid()).child("trainerMode").setValue(false);
                            changeMode(user.trainerMode);
                        } else {
                            user.setTrainerMode(true);
                            usersDbRef.child(currentFirebaseUser.getUid()).child("trainerMode").setValue(true);
                            changeMode(user.trainerMode);
                        }
                    }
                });
            }
        });

        myFirebaseDatabase.getUser(new OnUserFoundListener() {
            @Override
            public void OnUserFound(User user) {
                userNameTV.setText(user.getName());
                setCircleImage(user.image,(CircleImageView) profile.findViewById(R.id.profileIV));
                if (user.trainerMode) {
                    switchModeTV.setText("Switch to participant mode");
                } else {
                    switchModeTV.setText("Switch to trainer mode");
                }
            }
        });

        View logOutView = view.findViewById(R.id.logOutLL);
        logOutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    // Method to set and scale an image into an imageView
    private void setImage(String image, ImageView imageView) {
        Glide.with(this).load(image).into(imageView);
    }
    // Method to set and scale an image into an circular imageView
    private void setCircleImage(String image, CircleImageView imageView) {
        Glide.with(this).load(image).into(imageView);
    }

    private void logout() {
        mAuth.signOut();
    }

    private void changeMode(Boolean trainerMode) {

        if (trainerMode) {
            Intent intent = new Intent(getActivity(), MainHostActivity.class);
            getActivity().startActivity(intent);
        } else {
            Intent intent = new Intent(getActivity(), MainPlayerActivity.class);
            getActivity().startActivity(intent);
        }


    }

}
