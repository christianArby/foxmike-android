package com.example.chris.kungsbrostrand;


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

import java.util.ArrayList;


/**
 *
 */
public class PlayerSessionsFragment extends Fragment {

    private LinearLayout listPlayerSessions;
    private LatLng sessionLatLng;
    private View profile;

    public PlayerSessionsFragment() {
        // Required empty public constructor
    }

    public static PlayerSessionsFragment newInstance() {
        PlayerSessionsFragment fragment = new PlayerSessionsFragment();
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
        final View view = inflater.inflate(R.layout.fragment_player_sessions, container, false);

        listPlayerSessions = view.findViewById(R.id.list_player_sessions);

        /* Create an object of the class playerSessionsContent and use the function getPlayerSessionsContent in order to get all the data for the layouts in this fragment*/
        PlayerSessionsContent playerSessionsContent = new PlayerSessionsContent();
        playerSessionsContent.getPlayerSessionsContent(new OnPlayerSessionsContentReadyListener() {
            @Override
            public void OnPlayerSessionsContentReady(ArrayList<Session> sessionsHostingOrAttending) {

                // Create heading sessionAttending and populate the session_row view with the data from the sessions the user is attending
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                View sessionsAttendingHeadingView = inflater.inflate(R.layout.your_sessions_heading,listPlayerSessions,false);
                TextView sessionsAttendingHeading = sessionsAttendingHeadingView.findViewById(R.id.yourSessionsHeadingTV);
                sessionsAttendingHeading.setText(R.string.sessions_attending);
                listPlayerSessions.addView(sessionsAttendingHeadingView);
                populateList(sessionsHostingOrAttending);
            }
        });


        // Inflate the layout for this fragment
        return view;
    }

    // Method to populate the LinearLayout list with multiple session_row_view's
    private void populateList(final ArrayList<Session> sessionArray) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        for (int i=0; i < sessionArray.size(); i++) {
            View sessionRowView  = inflater.inflate(R.layout.session_row_view, listPlayerSessions, false);
            ImageView images = sessionRowView.findViewById(R.id.icon);
            TextView myTitle = sessionRowView.findViewById(R.id.text1);
            TextView myDescription = sessionRowView.findViewById(R.id.text2);
            myTitle.setText(sessionArray.get(i).getSessionName());
            myDescription.setText(sessionArray.get(i).getSessionType());
            setImage(sessionArray.get(i).getImageUri(),images);
            // set item content in view
            listPlayerSessions.addView(sessionRowView);
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

    // Method to set and scale an image into an imageView
    private void setImage(String image, ImageView imageView) {
        Glide.with(this).load(image).into(imageView);
    }

    private void displaySession(LatLng markerLatLng) {
        Intent intent = new Intent(getActivity(), DisplaySessionActivity.class);
        intent.putExtra("LatLng", markerLatLng);
        startActivity(intent);
    }

}
