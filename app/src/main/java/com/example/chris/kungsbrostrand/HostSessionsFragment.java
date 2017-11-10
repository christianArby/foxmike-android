package com.example.chris.kungsbrostrand;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;


/**
 *
 */
public class HostSessionsFragment extends Fragment {

    private LinearLayout listHostSessions;

    public HostSessionsFragment() {
        // Required empty public constructor
    }

    public static HostSessionsFragment newInstance() {
        HostSessionsFragment fragment = new HostSessionsFragment();
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
        final View view = inflater.inflate(R.layout.fragment_host_sessions, container, false);

        listHostSessions = view.findViewById(R.id.list_host_sessions);
        View sessionsHostingHeadingView = inflater.inflate(R.layout.your_sessions_heading,listHostSessions,false);
        TextView sessionsHostingHeading = sessionsHostingHeadingView.findViewById(R.id.yourSessionsHeadingTV);
        sessionsHostingHeading.setText("Sessions Hosting");
        listHostSessions.addView(sessionsHostingHeadingView);

        final MyFirebaseDatabase myFirebaseDatabase = new MyFirebaseDatabase();

        /* Get the currents user's information from the database */
        myFirebaseDatabase.getUser(new OnUserFoundListener() {
            @Override
            public void OnUserFound(final User user) {
                /* If user is not hosting any sessions set that the sessionsHosting content has beeen found*/
                if (user.sessionsHosting.size()==0){

                }
                myFirebaseDatabase.getSessions(new OnSessionsFoundListener() {
                    @Override
                    public void OnSessionsFound(ArrayList<Session> sessions) {
                        SessionRow sessionRow = new SessionRow();
                        sessionRow.populateList(sessions, getActivity(),listHostSessions);
                    }
                },user.sessionsHosting);
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

}
