package com.example.chris.kungsbrostrand;


import android.content.Context;
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
    private OnSessionClickedListener onSessionClickedListener;

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
        View sessionsAttendingHeadingView = inflater.inflate(R.layout.your_sessions_heading,listPlayerSessions,false);
        TextView sessionsAttendingHeading = sessionsAttendingHeadingView.findViewById(R.id.yourSessionsHeadingTV);
        sessionsAttendingHeading.setText(R.string.sessions_attending);
        listPlayerSessions.addView(sessionsAttendingHeadingView);

        final MyFirebaseDatabase myFirebaseDatabase = new MyFirebaseDatabase();

        /* Get the currents user's information from the database */
        myFirebaseDatabase.getUser(new OnUserFoundListener() {
            @Override
            public void OnUserFound(final User user) {
                /* If user is not attending any sessions set that the sessionsAttending content has beeen found*/
                if (user.sessionsAttending.size()==0){

                }
                myFirebaseDatabase.getSessions(new OnSessionsFoundListener() {
                    @Override
                    public void OnSessionsFound(ArrayList<Session> sessions) {
                        SessionRow sessionRow = new SessionRow();
                        sessionRow.populateList(sessions, getActivity(),listPlayerSessions, onSessionClickedListener);
                    }
                },user.sessionsAttending);
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSessionClickedListener) {
            onSessionClickedListener = (OnSessionClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSessionClickedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onSessionClickedListener = null;
    }



}
