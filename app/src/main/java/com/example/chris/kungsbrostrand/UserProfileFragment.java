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
        View switchMode = view.findViewById(R.id.switchModeLL);
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

    // Method to populate the LinearLayout list with multiple session_row_view's
    private void populateList(final ArrayList<Session> sessionArray) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        for (int i=0; i < sessionArray.size(); i++) {
            View sessionRowView  = inflater.inflate(R.layout.session_row_view, list, false);
            ImageView images = sessionRowView.findViewById(R.id.icon);
            TextView myTitle = sessionRowView.findViewById(R.id.text1);
            TextView myDescription = sessionRowView.findViewById(R.id.text2);
            myTitle.setText(sessionArray.get(i).getSessionName());
            myDescription.setText(sessionArray.get(i).getSessionType());
            setImage(sessionArray.get(i).getImageUri(),images);
            // set item content in view
            list.addView(sessionRowView);
            final int t = i;

            // When session_row_view is clicked start the DisplaySessionActivity
            sessionRowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sessionLatLng = new LatLng(sessionArray.get(t).getLatitude(), sessionArray.get(t).getLongitude());
                    displaySession(sessionLatLng);
                }
            });
        }
    }

    private void displaySession(LatLng markerLatLng) {
        Intent intent = new Intent(getActivity(), DisplaySessionActivity.class);
        intent.putExtra("LatLng", markerLatLng);
        startActivity(intent);
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
